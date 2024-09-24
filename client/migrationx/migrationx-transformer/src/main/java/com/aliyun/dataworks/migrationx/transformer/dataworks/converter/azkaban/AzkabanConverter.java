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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.azkaban;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.azkaban.objects.CommandJob;
import com.aliyun.dataworks.migrationx.domain.dataworks.azkaban.objects.Job;
import com.aliyun.dataworks.migrationx.domain.dataworks.azkaban.objects.JobType;
import com.aliyun.dataworks.migrationx.domain.dataworks.azkaban.service.AzkabanPackageParser;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwResource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowVersion;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DataStudioCodeUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DefaultNodeTypeUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.NodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.RawNodeType;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.AbstractBaseConverter;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;

import azkaban.utils.Props;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sam.liux
 * @date 2020/07/30
 */
public class AzkabanConverter extends AbstractBaseConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzkabanConverter.class);

    private Project project;
    private Properties properties = new Properties();

    public AzkabanConverter() {
        super(AssetType.AZKABAN, AzkabanConverter.class.getSimpleName());
    }

    public AzkabanConverter(AssetType assetType, String name) {
        super(assetType, name);
    }

    /**
     * 解析azkaban的包，支持多个azkaban project
     *
     * @param asset 包路径信息
     * @return 工作流列表
     * @throws Exception ex
     */
    @Override
    public List<DwWorkflow> convert(Asset asset) throws Exception {
        Preconditions.checkArgument(this.project != null, "project not set");
        Preconditions.checkArgument(asset.getPath() != null && asset.getPath().exists(), "invalid asset path");

        File[] subDirs = asset.getPath().listFiles(File::isDirectory);
        File[] files = asset.getPath().listFiles(File::isFile);
        List<File> dirs = new ArrayList<>();
        if (subDirs == null || subDirs.length == 0) {
            dirs.add(asset.getPath());
        } else if (files == null || files.length == 0) {
            dirs.addAll(Arrays.asList(subDirs));
        } else {
            dirs.add(asset.getPath());
        }

        workflowList = dirs.stream().map(this::processDirWorkflow).flatMap(List::stream).collect(Collectors.toList());
        ListUtils.emptyIfNull(workflowList).forEach(wf ->
                ListUtils.emptyIfNull(wf.getNodes()).forEach(n ->
                        NodeUtils.setProjectRootDependencyIfIsolated(project, wf, n)));
        return workflowList;
    }

    private List<DwWorkflow> processDirWorkflow(File dir) {
        AzkabanPackageParser parser = new AzkabanPackageParser();
        try {
            parser.parse(dir);
        } catch (Exception e) {
            LOGGER.error("parse azkaban package error: ", e);
            throw BizException.of(ErrorCode.PACKAGE_ANALYZE_FAILED).with(e.getMessage());
        }

        return convertAzkabanPackage(parser);
    }

    private List<DwWorkflow> convertAzkabanPackage(AzkabanPackageParser parser) {
        Map<String, List<Job>> flowJobs = parser.getFlowJobs();
        return MapUtils.emptyIfNull(flowJobs).entrySet().stream().map(ent -> {
            String flowName = ent.getKey();
            List<Job> jobs = ent.getValue();
            DwWorkflow workflow = new DwWorkflow();
            workflow.setScheduled(true);
            workflow.setName(NodeUtils.normalizedFileName(flowName));
            workflow.setVersion(WorkflowVersion.V3);

            ListUtils.emptyIfNull(jobs).stream().forEach(job -> {
                Props props = Optional.ofNullable(parser.getJobProperties(flowName, job.getName())).orElse(new Props());
                List<Node> nodes = jobToNode(job, workflow, props.toAllProperties());
                nodes.stream().forEach(n -> ((DwNode) n).setWorkflowRef(workflow));
                workflow.getNodes().addAll(nodes);
                List<DwResource> dwResources = jobToResource(job, workflow);
                ListUtils.emptyIfNull(dwResources).stream().forEach(r -> r.setWorkflowRef(workflow));
                workflow.getResources().addAll(dwResources);
            });
            return workflow;
        }).collect(Collectors.toList());
    }

    private List<DwResource> jobToResource(Job job, DwWorkflow workflow) {
        String engineType = this.properties.getProperty(Constants.CONVERTER_TARGET_ENGINE_TYPE);
        CalcEngineType calcEngineType = CalcEngineType.valueOf(engineType);
        String nodeMarketEngineType = DefaultNodeTypeUtils.convertToNodeMarketModelEngineTypes(calcEngineType);
        return ListUtils.emptyIfNull(job.getRelatedFiles()).stream().map(file -> {
            DwResource dwResource = new DwResource();
            dwResource.setName(file.getName());
            dwResource.setOdps(false);
            dwResource.setLocalPath(file.getAbsolutePath());
            dwResource.setWorkflowRef(workflow);
            if (file.getName().endsWith(".jar")) {
                CodeProgramType type = DefaultNodeTypeUtils.getJarResourceType(nodeMarketEngineType);
                dwResource.setType(type.name());
            } else {
                CodeProgramType type = DefaultNodeTypeUtils.getFileResourceType(nodeMarketEngineType);
                dwResource.setType(type.name());
            }
            dwResource.setOriginResourceName(file.getName());
            return dwResource;
        }).collect(Collectors.toList());
    }

    private List<String> parseCodeParameters(String code) {
        if (StringUtils.isBlank(code)) {
            return ListUtils.emptyIfNull(null);
        }

        Pattern pattern = Pattern.compile("\\$\\{[\\w\\.]+\\}");
        Matcher matcher = pattern.matcher(code);
        Set<String> params = new HashSet<>();
        while (matcher.find()) {
            String param = matcher.group();
            param = param.replaceAll("\\$\\{", "").replaceAll("\\}", "");
            params.add(param);
        }
        return new ArrayList<>(params);
    }

    private String processJobParameters(Job job, Properties jobProperty, String type, List<String> params) {
        AtomicInteger paramIndex = new AtomicInteger(0);
        return Joiner.on(" ").join(ListUtils.emptyIfNull(params).stream().map(p -> {
            final String rawValue = Optional.ofNullable(jobProperty.getProperty(p)).orElse(
                    Constants.UNDEFINED_VARIABLE_VALUE);
            String value = ListUtils.emptyIfNull(parseCodeParameters(rawValue)).stream().filter(StringUtils::isNotBlank)
                    .map(
                            paramInValue -> Optional.ofNullable(jobProperty.getProperty(paramInValue))
                                    .map(val -> StringUtils.replace(rawValue, "${" + paramInValue + "}", val)).orElse(rawValue))
                    .reduce((a, b) -> b).orElse(rawValue);
            int index = paramIndex.incrementAndGet();
            if (DefaultNodeTypeUtils.isNoCalcEngineShell(type)) {
                ((CommandJob) job).getCommands().add(index - 1, Joiner.on("=").join(p, "$" + index));
            } else {
                return Joiner.on("=").join(p, value);
            }
            return value;
        }).collect(Collectors.toList()));
    }

    private List<Node> jobToNode(Job job, DwWorkflow workflow, Properties jobProperty) {
        List<Node> nodes = new ArrayList<>();

        DwNode dwNode = new DwNode();
        dwNode.setName(job.getName());
        dwNode.setIsAutoParse(0);
        dwNode.setType(getNodeType(job.getType()));
        dwNode.setRawNodeType(Optional.ofNullable(getRawNodeType(job.getType())).map(Enum::name)
                .orElse(job.getType().name()));
        dwNode.setNodeUseType(
                BooleanUtils.isTrue(workflow.getScheduled()) ? NodeUseType.SCHEDULED : NodeUseType.MANUAL_WORKFLOW);
        dwNode.setWorkflowRef(workflow);
        DwNodeIo dwNodeIo = new DwNodeIo();
        dwNodeIo.setNodeRef(dwNode);
        dwNodeIo.setData(Joiner.on(".").join(project.getName(), workflow.getName(), dwNode.getName()));
        dwNodeIo.setParseType(1);
        dwNodeIo.setNodeRef(dwNode);
        dwNode.setOutputs(Collections.singletonList(dwNodeIo));
        dwNode.setCronExpress("day");

        dwNode.setInputs(ListUtils.emptyIfNull(job.getDependencies()).stream().map(dependency -> {
            DwNodeIo input = new DwNodeIo();
            // 依赖嵌套flow
            if (StringUtils.contains(dependency, AzkabanPackageParser.FLOW_NODE_DELIMITER)) {
                input.setData(StringUtils.replace(dependency, AzkabanPackageParser.FLOW_NODE_DELIMITER, "."));
            } else {
                input.setData(Joiner.on(".").join(workflow.getName(), dependency));
            }
            input.setData(Joiner.on(".").join(project.getName(), input.getData()));
            input.setParseType(1);
            input.setNodeRef(dwNode);
            return input;
        }).collect(Collectors.toList()));
        dwNode.setParameter(
                processJobParameters(job, jobProperty, dwNode.getType(), parseCodeParameters(job.getCode())));
        // 放到setParameter后面，processJobParameter会改写commands
        dwNode.setCode(job.getCode());
        if (CollectionUtils.isNotEmpty(job.getRelatedFiles())) {
            dwNode.setCode(DataStudioCodeUtils.addResourceReference(CodeProgramType.valueOf(dwNode.getType()),
                    dwNode.getCode(),
                    ListUtils.emptyIfNull(job.getRelatedFiles()).stream().map(File::getName).collect(Collectors.toList())));
        }
        dwNode.setCode(EmrCodeUtils.toEmrCode(dwNode));

        // 有多行command的情况，将任务拆成串行的流程
        LOGGER.info("jobType: {}, jobClz: {}", job.getType(), job.getClass());
        nodes.add(dwNode);
        return nodes;
    }

    private RawNodeType getRawNodeType(JobType type) {
        switch (type) {
            case noop:
                return RawNodeType.AZKABAN_NOOP;
            case command:
                return RawNodeType.AZKABAN_COMMAND;
            case hive:
                return RawNodeType.AZKABAN_HIVE;
            case flow:
                return RawNodeType.AZKABAN_FLOW;
            default:
        }
        return null;
    }

    private String getNodeType(JobType type) {
        switch (type) {
            case noop:
            case flow:
                return CodeProgramType.VIRTUAL.name();
            case command:
                return properties.getProperty(Constants.CONVERTER_TARGET_SHELL_NODE_TYPE_AS, CodeProgramType.EMR_SHELL.name());
            case hive:
                return properties.getProperty(Constants.CONVERTER_TARGET_SQL_NODE_TYPE_AS, CodeProgramType.EMR_HIVE.name());
            case unknown:
            default: {
                String unsupportedType = getDefaultTypeIfNotSupported(properties, CodeProgramType.DIDE_SHELL);
                if (StringUtils.isNotBlank(unsupportedType)) {
                    return unsupportedType;
                }
                throw new RuntimeException("unsupported azkaban job type: " + type);
            }
        }
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
