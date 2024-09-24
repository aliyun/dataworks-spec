/*
 * Copyright (c) 2024, Alibaba Cloud;
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aliyun.dataworks.migrationx.domain.dataworks.azkaban.service;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import azkaban.Constants;
import azkaban.flow.Edge;
import azkaban.flow.Flow;
import azkaban.flow.Node;
import azkaban.project.DirectoryFlowLoader;
import azkaban.project.DirectoryYamlFlowLoader;
import azkaban.project.FlowLoader;
import azkaban.project.FlowLoaderFactory;
import azkaban.project.Project;
import azkaban.utils.Props;
import com.aliyun.dataworks.common.spec.utils.ReflectUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.azkaban.objects.ConfigProperty;
import com.aliyun.dataworks.migrationx.domain.dataworks.azkaban.objects.Job;
import com.aliyun.dataworks.migrationx.domain.dataworks.azkaban.objects.JobType;
import com.aliyun.migrationx.common.utils.FileNameUtils;
import com.google.common.base.Joiner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sam.liux
 * @date 2020/08/04
 */
public class AzkabanPackageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzkabanPackageParser.class);
    public static final String FLOW_NODE_DELIMITER = "@";
    private Map<String, Map<String, Props>> jobProperties = new HashMap<>();
    private Map<String, List<Job>> flowJobs = new HashMap<>();
    private FlowLoader flowLoader = null;
    private Project project = null;
    private File projectDir = null;

    public Map<String, List<Job>> getFlowJobs() {
        return flowJobs;
    }

    public void parse(File dir) throws Exception {
        project = new Project(0, "tmpAzkabanProject");
        projectDir = dir;
        FlowLoaderFactory flowLoaderFactory = new FlowLoaderFactory(new Props(null));
        flowLoader = flowLoaderFactory.createFlowLoader(dir);
        flowLoader.loadProjectFlow(project, dir);

        convertAzkabanFlows(flowLoader, dir);
    }

    private Object convertType(Class<?> fieldType, String value) {
        if (value == null) {
            return null;
        }

        if (fieldType.equals(Integer.class)) {
            return Integer.valueOf(value);
        }

        if (fieldType.equals(String.class)) {
            return value;
        }

        if (fieldType.equals(Long.class)) {
            return Long.valueOf(value);
        }

        throw new RuntimeException("unsupported field type convert: " + fieldType);
    }

    private Object convertType(Field field, ConfigProperty anno, String value) {
        if (value == null) {
            return null;
        }

        Class<?> fieldType = field.getType();
        if (fieldType.equals(List.class)) {
            List<String> parts = Arrays.asList(value.split(anno.delimiter()));
            Type[] actualTypes = ((ParameterizedType)field.getGenericType()).getActualTypeArguments();
            if (actualTypes != null && actualTypes.length == 1) {
                Class<?> typeParameter = (Class<?>)actualTypes[0];
                return parts.stream().map(v -> convertType(typeParameter, v)).collect(Collectors.toList());
            } else {
                throw new RuntimeException("unsupported multiple parameter types");
            }
        }

        return convertType(fieldType, value);
    }

    private void convertAzkabanFlows(FlowLoader flowLoader, File dir) {
        Map<String, Flow> flowMap = MapUtils.emptyIfNull(flowLoader.getFlowMap());

        new ArrayList<>(flowMap.values()).forEach(flow -> CollectionUtils.emptyIfNull(flow.getNodes())
            .forEach(node -> preProcessDependencies(flowLoader, flow, node)));

        flowMap.forEach((key, flow) -> {
            File flowFile = new File(dir, flow.getId() + Constants.FLOW_FILE_SUFFIX);
            parseFlow(flowLoader, flowFile, key, flow);
        });
    }

    private Props getNodeProps(FlowLoader flowLoader, File flowFile, Flow flow, String nodeName) {
        Props defaultProps = new Props(null);
        Node flowNode = CollectionUtils.emptyIfNull(flow.getNodes()).stream()
            .filter(n -> StringUtils.equals(n.getId(), nodeName)).findFirst()
            .orElseThrow(() ->
                new RuntimeException("node: " + nodeName + "˜not found in flow: " + flow.getId()));
        defaultProps.setSource(flowNode.getPropsSource());

        if (flowLoader instanceof DirectoryYamlFlowLoader) {
            return CollectionUtils.emptyIfNull(flow.getNodes()).stream().filter(n -> n.getId().equals(nodeName)).map(
                node -> {
                    String nodePropPath = Joiner.on(Constants.PATH_DELIMITER).join(flow.getId(), node.getId());
                    try {
                        Field field = ((DirectoryYamlFlowLoader)flowLoader).getClass().getDeclaredField("jobPropsMap");
                        field.setAccessible(true);
                        Props prop = MapUtils.emptyIfNull((Map<String, Props>)field.get(flowLoader)).get(nodePropPath);
                        prop.setSource(StringUtils.defaultIfBlank(flowNode.getPropsSource(), flowNode.getJobSource()));
                        return prop;
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        LOGGER.error("failed to get jobPropsMap from flow loader: {}", flowLoader.getClass());
                        return defaultProps;
                    }
                }).filter(Objects::nonNull).findFirst().orElse(defaultProps);
        }

        if (flowLoader instanceof DirectoryFlowLoader) {
            Props prop = Optional
                .ofNullable(MapUtils.emptyIfNull(((DirectoryFlowLoader)flowLoader).getJobPropsMap()).get(nodeName))
                .orElse(new Props(defaultProps));
            prop.setSource(StringUtils.defaultIfBlank(flowNode.getJobSource(), flowNode.getPropsSource()));
            return prop;
        }

        return defaultProps;
    }

    private void parseFlow(FlowLoader flowLoader, File flowFile, String flowName, Flow flow) {
        List<Job> nodes = CollectionUtils.emptyIfNull(flow.getNodes()).stream()
            .map(node -> parseJobFile(flowLoader, flowFile, flow, node))
            .collect(Collectors.toList());
        flowJobs.put(flowName, nodes);
    }

    private Node preProcessDependencies(FlowLoader flowLoader, Flow flow, Node node) {
        List<String> depends = CollectionUtils.emptyIfNull(flow.getEdges()).stream()
            .filter(edge -> StringUtils.equals(edge.getTargetId(), node.getId()))
            .map(Edge::getSourceId).collect(Collectors.toList());
        LOGGER.info("flow: {}, job: {}, depends: {}", flow.getId(), node.getId(), depends);

        // flow内部的所有start节点依赖flow虚节点的上游
        Flow embeddedFlow = MapUtils.emptyIfNull(flowLoader.getFlowMap()).get(node.getEmbeddedFlowId());
        if (embeddedFlow != null) {
            embeddedFlow.setEmbeddedFlow(true);
            ListUtils.emptyIfNull(depends).forEach(dep ->
                embeddedFlow.getStartNodes().forEach(n -> {
                    Edge edge = new Edge(Joiner.on(FLOW_NODE_DELIMITER).join(flow.getId(), dep), n.getId());
                    embeddedFlow.addEdge(edge);
                    LOGGER.info("add edge: {} for flow: {}, inner node: {}",
                        Joiner.on(">>").join(edge.getSourceId(), edge.getTargetId()),
                        embeddedFlow.getId(), n.getId());
                }));
        }
        return node;
    }

    private Job parseJobFile(FlowLoader flowLoader, File flowFile, Flow flow, Node node) {
        Props props = getNodeProps(flowLoader, flowFile, flow, node.getId());
        Map<String, Props> nodeProps = jobProperties.computeIfAbsent(flow.getId(), flowName -> new HashMap<>());
        nodeProps.put(node.getId(), props);

        Properties properties = props.toAllProperties();

        String type = properties.getProperty("type");
        JobType jobType = JobType.getByName(type);
        Job job = JobType.newJobInstance(jobType);
        job.setName(node.getId());
        if (StringUtils.isNotBlank(props.getSource())) {
            job.setJobFile(new File(projectDir, props.getSource()));
        }
        List<Field> fields = ReflectUtils.getPropertyFields(job);
        fields.removeIf(
            field -> field.getDeclaringClass().equals(Job.class) && "type".equalsIgnoreCase(field.getName()));

        Enumeration<?> e = properties.propertyNames();
        Set<String> keys = new HashSet<>();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            keys.add(key);
        }

        fields.forEach(field -> {
            ConfigProperty anno = field.getDeclaredAnnotation(ConfigProperty.class);
            if (anno == null) {
                return;
            }
            try {
                field.setAccessible(true);

                if (StringUtils.isNotBlank(anno.pattern())) {
                    Pattern pattern = Pattern.compile(anno.pattern());
                    List<String> values = keys.stream()
                        .filter(key -> pattern.matcher(key).matches())
                        .map(properties::getProperty)
                        .collect(Collectors.toList());
                    field.set(job, values);
                }

                if (StringUtils.isNotBlank(anno.value())) {
                    String value = properties.getProperty(anno.value(), null);
                    Object objectValue = convertType(field, anno, value);
                    field.set(job, objectValue);
                }
            } catch (IllegalAccessException illegalAccessException) {
                throw new RuntimeException(illegalAccessException);
            }
        });
        job.processJobRelativeFiles();

        if (CollectionUtils.isEmpty(job.getDependencies())) {
            job.setDependencies(CollectionUtils.emptyIfNull(flow.getEdges()).stream()
                .filter(edge -> StringUtils.equals(edge.getTargetId(), node.getId()))
                .map(Edge::getSourceId).collect(Collectors.toList()));
            LOGGER.info("flow: {}, job: {}, depends: {}", flow.getId(), job.getName(), job.getDependencies());
        }

        // 如果自己是一个flow节点
        if (JobType.flow.equals(job.getType())) {
            List<String> depends = ListUtils.emptyIfNull(job.getDependencies());
            // flow虚节点设置为依赖flow内部所有节点的end节点
            List<String> flowDepends = Optional.ofNullable(Optional.ofNullable(
                        MapUtils.emptyIfNull(flowLoader.getFlowMap())
                            .get(Joiner.on(Constants.PATH_DELIMITER).join(flow.getId(), job.getName())))
                    .orElse(flowLoader.getFlowMap().values().stream()
                        .filter(f -> StringUtils.equals(f.getId(), node.getEmbeddedFlowId()))
                        .findFirst().orElse(null)))
                .map(f -> ListUtils.emptyIfNull(f.getEndNodes()).stream()
                    .map(n -> Joiner.on(FLOW_NODE_DELIMITER).join(FileNameUtils.normalizedFileName(f.getId()), n.getId()))
                    .collect(Collectors.toList()))
                .orElse(new ArrayList<>());

            // flow内部的所有start节点依赖flow虚节点的上游
            // Optional.ofNullable(MapUtils.emptyIfNull(flowLoader.getFlowMap()).get(node.getEmbeddedFlowId()))
            // .ifPresent(embeddedFlow -> {
            //    ListUtils.emptyIfNull(depends).forEach(dep -> {
            //        embeddedFlow.getStartNodes().stream().forEach(n -> {
            //            Edge edge = new Edge(Joiner.on(FLOW_NODE_DELIMITER).join(flow.getId(), dep), n.getId());
            //            embeddedFlow.addEdge(edge);
            //            //LOGGER.info("add edge: {} for flow: {}, inner node: {}",
            //            //    Joiner.on(">>").join(edge.getSourceId(), edge.getTargetId()),
            //            //    embeddedFlow.getId(), n.getId());
            //        });
            //    });
            //});

            Optional.ofNullable(MapUtils.emptyIfNull(flowLoader.getFlowMap()).get(node.getEmbeddedFlowId()))
                .ifPresent(f -> f.setEmbeddedFlow(true));
            job.setDependencies(new ArrayList<>(CollectionUtils.union(depends, flowDepends)));
            LOGGER.info("flow: {}, job: {}, depends: {}", flow.getId(), job.getName(), job.getDependencies());
        } else {
            // flow内部的start节点, 依赖flow节点的上游
            // if (CollectionUtils.isEmpty(job.getDependencies()) && BooleanUtils.isTrue(flow.isEmbeddedFlow())) {
            //    // 找到flow节点
            //    flowLoader.getFlowMap().values().stream()
            //        .filter(f -> f.getNodes().stream().anyMatch(n -> flow.getId().equals(n.getEmbeddedFlowId())))
            //        .forEach(f ->
            //            f.getNodes().stream().filter(n -> flow.getId().equals(n.getEmbeddedFlowId()))
            //                .findFirst()
            //                .ifPresent(flowNode -> job.setDependencies(
            //                    Collections.singletonList(Joiner.on(FLOW_NODE_DELIMITER).join(f.getId(), flowNode
            //                    .getId())))));
            //}
        }
        return job;
    }

    public Props getJobProperties(String flowName, String nodeName) {
        return MapUtils.emptyIfNull(MapUtils.emptyIfNull(jobProperties).get(flowName)).get(nodeName);
    }

    public Map<String, Map<String, Props>> getJobProperties() {
        return jobProperties;
    }
}
