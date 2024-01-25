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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.enums.FunctionType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwFunction;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwResource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Function;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Workflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v1.DeployType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v1.Flow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v1.FunctionCode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v1.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v1.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.DmMajorVersion;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.utils.ZipUtils;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * V1 Migration工具的导出包转换器，转换为dataworks-model
 *
 * @author sam.liux
 * @date 2019/12/18
 */
public class V1MigrationPackageConverter extends AbstractBaseConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(V1MigrationPackageConverter.class);
    public static final String FULL_DIR = "full";
    public static final String NOT_FULL_DIR = "notfull";
    public static final String CODES_DIR = "codes";
    public static final String NODES_DIR = "nodes";
    public static final String FUNCTIONS_DIR = "functions";
    public static final String RESOURCES_DIR = "resources";
    public static final String NODE_RUN_TYPE_MANUAL = "manual";
    public static final String NODE_RUN_TYPE_PAUSE = "pause";
    public static final String NODE_RUN_TYPE_NORMAL = "normal";

    private static final String DEFAULT_WORKFLOW_NAME = "migration_workflows";
    private static final char WORKFLOW_NAME_SEPARATOR = '_';

    public static final Map<String, String> NODE_TYPE_TO_FILE_EXT = new HashMap<>();
    public static final Map<String, String> RESOURCE_TYPE_TO_FILE_EXT = new HashMap<>();
    public static final Map<String, String> NODE_TYPE_MAP = new HashMap<>();

    static {
        NODE_TYPE_TO_FILE_EXT.put("odps_sql", "sql");
        NODE_TYPE_TO_FILE_EXT.put("odps_mr", "mr");
        NODE_TYPE_TO_FILE_EXT.put("shell", "sh");
        NODE_TYPE_TO_FILE_EXT.put("cdp", "cdp");

        RESOURCE_TYPE_TO_FILE_EXT.put("python", "py");
        RESOURCE_TYPE_TO_FILE_EXT.put(CodeProgramType.ODPS_PYTHON.name().toLowerCase(), "py");
        RESOURCE_TYPE_TO_FILE_EXT.put("jar", "jar");
        RESOURCE_TYPE_TO_FILE_EXT.put(CodeProgramType.ODPS_JAR.name().toLowerCase(), "jar");
        RESOURCE_TYPE_TO_FILE_EXT.put("file", "");
        RESOURCE_TYPE_TO_FILE_EXT.put(CodeProgramType.ODPS_FILE.name().toLowerCase(), "");
        RESOURCE_TYPE_TO_FILE_EXT.put("archive", "zip,tgz,tar.gz,tar,jar");
        RESOURCE_TYPE_TO_FILE_EXT.put(CodeProgramType.ODPS_ARCHIVE.name().toLowerCase(), "tgz");

        NODE_TYPE_MAP.put(CodeProgramType.CDP.name(), CodeProgramType.DI.name());
        NODE_TYPE_MAP.put(CodeProgramType.SHELL.name(), CodeProgramType.DIDE_SHELL.name());
        NODE_TYPE_MAP.put(CodeProgramType.ODPS_MR.name(), CodeProgramType.ODPS_MR.name());
        NODE_TYPE_MAP.put(CodeProgramType.ODPS_SQL.name(), CodeProgramType.ODPS_SQL.name());
    }

    private List<Function> functionList;
    private List<DwResource> resourceList;

    public V1MigrationPackageConverter() {
        super(AssetType.V1_EXPORT, V1MigrationPackageConverter.class.getSimpleName());
    }

    @Override
    public List<DwWorkflow> convert(Asset asset) throws Exception {
        Preconditions.checkNotNull(asset, "asset is null");
        Preconditions.checkNotNull(asset.getPath(), "vi migration package directory is null");
        Preconditions.checkArgument(asset.getPath().exists(), "unzipped v1 migration package directory not exists");

        LOGGER.info("unzipping v1 migration package ...");
        File unzippedPath = ZipUtils.unzipExportFile(asset.getPath());
        LOGGER.info("unzip v1 migration package success");

        Project project = readV1MigrationProject(unzippedPath);
        return convertV1Project(project);
    }

    private List<DwWorkflow> convertV1Project(Project project) {
        List<DwWorkflow> workflowList = convertWorkflows(project);
        resourceList = convertResources(project);
        functionList = convertFunctions(project);
        return workflowList;
    }

    private List<Function> convertFunctions(Project project) {
        return ListUtils.emptyIfNull(project.getFunctions()).stream().map(function -> {
            DwFunction dwFunction = new DwFunction();
            dwFunction.setDmMajorVersion(DmMajorVersion.DATAWORKS_V1.name());
            dwFunction.setName(function.getName());
            dwFunction.setType(CodeProgramType.ODPS_FUNCTION.name());
            dwFunction.setFunctionType(FunctionType.OTHER.name());
            dwFunction.setIsOdps(function.getIsOdps());
            dwFunction.setConnection(function.getConnection());

            if (StringUtils.isNotBlank(function.getFolderPath())) {
                String[] folders = function.getFolderPath().split("/");
                if (folders.length > 1) {
                    dwFunction.setFolder(Joiner.on("/").join(Arrays.asList(folders).subList(1, folders.length)));
                }

                if (StringUtils.isNotBlank(function.getFilePath()) && new File(function.getFilePath()).exists()) {
                    try {
                        String code = IOUtils.toString(new FileReader(function.getFilePath()));
                        FunctionCode functionCode = GsonUtils.gson.fromJson(code,
                            new TypeToken<FunctionCode>() {}.getType());
                        if (functionCode != null) {
                            dwFunction.setClazz(functionCode.getClassName());
                            dwFunction.setResource(functionCode.getResources());
                        }
                    } catch (IOException e) {
                        LOGGER.error("read function code failed: ", e);
                    }
                }
            }
            return dwFunction;
        }).collect(Collectors.toList());
    }

    private List<DwResource> convertResources(Project project) {
        List<DwResource> resList = ListUtils.emptyIfNull(project.getResources()).stream().map(resource -> {
            DwResource dwResource = new DwResource();
            dwResource.setDmMajorVersion(DmMajorVersion.DATAWORKS_V1.name());
            dwResource.setName(resource.getName());
            if (StringUtils.isNotBlank(resource.getFolderPath())) {
                String[] folders = resource.getFolderPath().split("/");
                if (folders.length > 1) {
                    dwResource.setFolder(Joiner.on("/").join(Arrays.asList(folders).subList(1, folders.length)));
                }
            }
            dwResource.setOdps(resource.getIsOdps());
            dwResource.setType(resource.getType());
            dwResource.setConnection(resource.getConnection());
            dwResource.setLocalPath(resource.getFilePath());
            return dwResource;
        }).collect(Collectors.toList());

        // 去重
        Set<String> keySet = new HashSet<>();
        return resList.stream().filter(res -> {
            if (!keySet.contains(res.getUniqueKey())) {
                keySet.add(res.getUniqueKey());
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    /**
     * 统一计算生成多少个workflow，V1没有业务流程概念需要将文件夹映射到业务流程
     *
     * @param project
     * @return
     */
    private List<DwWorkflow> convertWorkflows(Project project) {
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(ListUtils.emptyIfNull(project.getNodes()));
        nodes.addAll(ListUtils.emptyIfNull(project.getFlows()));

        Map<String, DwWorkflow> workflowMap = new HashMap<>();
        ListUtils.emptyIfNull(nodes).forEach(node -> {
            com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node dwNode = convertNode(node, null);
            String workflowName = mapWorkflowNameByNode(node);
            Boolean scheduled = mapWorkflowScheduled(node);
            String key = workflowName + "_" + scheduled;

            DwWorkflow dwWorkflow;
            if (workflowMap.containsKey(key)) {
                dwWorkflow = workflowMap.get(key);
            } else {
                dwWorkflow = new DwWorkflow();
                dwWorkflow.setDmMajorVersion(DmMajorVersion.DATAWORKS_V1.name());
                dwWorkflow.setName(workflowName);
                dwWorkflow.setScheduled(scheduled);
                dwWorkflow.setOwner(node.getOwner());
                workflowMap.put(key, dwWorkflow);
            }
            dwNode.setNodeUseType(dwWorkflow.getScheduled() ? NodeUseType.SCHEDULED : NodeUseType.MANUAL_WORKFLOW);
            dwWorkflow.getNodes().add(dwNode);
        });

        List<com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node> dwNodes = workflowMap.values().stream()
            .map(Workflow::getNodes).flatMap(List::stream).collect(Collectors.toList());
        convertNodeRelations(nodes, dwNodes);
        return workflowMap.values().stream().collect(Collectors.toList());
    }

    private void convertNodeRelations(List<Node> nodes, List<com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node> dwNodes) {
        Map<String, String> nodeParentRelations = new HashMap<>(100);
        Map<String, com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node> nodeNameMap = dwNodes.stream().collect(Collectors.toMap(
            com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node::getName, node -> node));

        // 普通节点
        nodes.stream().forEach(node -> {
            String parents = node.getParents();
            if (StringUtils.isBlank(parents)) {
                return;
            }

            ListUtils.emptyIfNull(Arrays.asList(parents.split(","))).stream().forEach(parentNodeName -> {
                if (!nodeNameMap.containsKey(parentNodeName)) {
                    return;
                }

                nodeParentRelations.put(parentNodeName, node.getName());
            });
        });

        nodeParentRelations.forEach((parentName, childName) -> {
            com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node parentNode = nodeNameMap.get(parentName);
            com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node childNode = nodeNameMap.get(childName);

            NodeIo input = parentNode.getOutputs().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("get no outputs of node: " + parentNode.getName()));
            childNode.getInputs().add(input);
        });

        // 工作流节点内部的关系依赖
        dwNodes.stream().filter(node -> CollectionUtils.isNotEmpty(node.getInnerNodes())).forEach(flowNode -> {
            Flow outerNode = (Flow)nodes.stream()
                .filter(node -> node.getName().equals(flowNode.getName()) && node instanceof Flow).findFirst()
                .orElseThrow(() -> new RuntimeException("flow node not found: " + flowNode.getName()));

            Map<String, List<String>> innerNodeParentsMap = ListUtils.emptyIfNull(outerNode.getNodes()).stream()
                .filter(node -> StringUtils.isNotBlank(node.getParents()))
                .collect(Collectors.toMap(
                    innerNode -> innerNode.getName(),
                    innerNode -> Arrays.asList(innerNode.getParents().split(","))));

            Map<String, com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node> innerNodesMap =
                ListUtils.emptyIfNull(flowNode.getInnerNodes()).stream()
                    .collect(Collectors.toMap(com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node::getName, innerNode -> innerNode));

            ListUtils.emptyIfNull(flowNode.getInnerNodes()).forEach(node -> {
                List<String> parents = innerNodeParentsMap.get(node.getName());
                ListUtils.emptyIfNull(parents).stream().map(innerNodesMap::get).forEach(
                    parentNode -> node.getInputs().add(
                        ListUtils.emptyIfNull(parentNode.getOutputs()).stream().findAny()
                            .orElseThrow(() -> new RuntimeException("get no outputs of node: " + parentNode.getName()))
                    ));
            });
        });
    }

    /**
     * 处理普通节点和工作流节点的转换，不处理依赖关系
     *
     * @param node
     * @param parentFlowNode
     * @return
     */
    private com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node convertNode(Node node, Flow parentFlowNode) {
        DwNode dwNode = new DwNode();
        dwNode.setDmMajorVersion(DmMajorVersion.DATAWORKS_V1.name());
        dwNode.setName(node.getName());
        dwNode.setFolder(node.getFolderPath());
        dwNode.setRef(node.getFilePath());
        try {
            if (StringUtils.isNotBlank(node.getFilePath())) {
                dwNode.setCode(IOUtils.toString(new FileReader(node.getFilePath())));
            }
        } catch (IOException e) {
            LOGGER.error("error read node code: {}", e);
        }

        dwNode.setStartRightNow(false);
        dwNode.setCronExpress(node.getScheduleExpression());
        dwNode.setPauseSchedule(NODE_RUN_TYPE_PAUSE.equalsIgnoreCase(node.getRunType()));
        dwNode.setOwner(node.getOwner());
        dwNode.setStartEffectDate(node.getStartEffectDate());
        dwNode.setEndEffectDate(node.getEndEffectDate());
        dwNode.setDescription(node.getDescription());
        dwNode.setIsRoot(node.getIsRoot());
        if (NODE_TYPE_MAP.containsKey(StringUtils.upperCase(node.getType()))) {
            dwNode.setType(NODE_TYPE_MAP.get(StringUtils.upperCase(node.getType())));
        } else {
            dwNode.setType(StringUtils.upperCase(node.getType()));
        }
        dwNode.setConnection(node.getConnection());
        Optional.ofNullable(node.getExtension()).ifPresent(extension ->
            dwNode.setExtension(GsonUtils.gson.toJson(node.getExtension())));

        // 生成默认的节点输出，V1里面节点名称唯一
        DwNodeIo output = new DwNodeIo();
        output.setDmMajorVersion(DmMajorVersion.DATAWORKS_V1.name());
        if (parentFlowNode != null) {
            output.setData(parentFlowNode.getName() + "." + node.getName());
        } else {
            output.setData(node.getName());
        }
        output.setParseType(1);
        output.setNodeRef(dwNode);
        output.setType("OUTPUT");
        dwNode.setOutputs(Arrays.asList(output));

        // 处理工作流节点的内部节点
        if (node instanceof Flow) {
            Flow flowNode = (Flow)node;
            List<com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node> innerDwNodes = ListUtils.emptyIfNull(flowNode.getNodes())
                .stream().map(innerNode -> convertNode(innerNode, flowNode)).collect(Collectors.toList());
            dwNode.setInnerNodes(innerDwNodes);
            dwNode.setNodeUseType(
                BooleanUtils.isTrue(flowNode.getAutoSchedule()) ? NodeUseType.SCHEDULED : NodeUseType.MANUAL);
            innerDwNodes.forEach(n -> n.setNodeUseType(dwNode.getNodeUseType()));
            dwNode.setType(CodeProgramType.COMBINED_NODE.name());
            dwNode.setDeployType(DeployType.getDeployTypeByValue(flowNode.getDeployType()));
        }
        return dwNode;
    }

    private Boolean mapWorkflowScheduled(Node node) {
        return !NODE_RUN_TYPE_MANUAL.equalsIgnoreCase(node.getRunType());
    }

    private String mapWorkflowNameByNode(Node node) {
        String name = DEFAULT_WORKFLOW_NAME;
        if (StringUtils.isNotBlank(node.getFolderPath())) {
            int code = node.getFolderPath().hashCode();
            // example: 任务开发/folder01/folder02
            String[] folderPath = node.getFolderPath().split("/");
            name = folderPath[folderPath.length - 1] + WORKFLOW_NAME_SEPARATOR + code;
        }
        return name;
    }

    private Project readV1MigrationProject(File unzippedPath) throws IOException {
        LOGGER.info("reading v1 migration project ...");
        File fullDir = new File(unzippedPath.getAbsoluteFile() + File.separator + FULL_DIR);

        Project fullProject = parseProjectDir(fullDir);
        LOGGER.info("read v1 migration package success");
        return fullProject;
    }

    private Project parseProjectDir(File fullDir) throws IOException {
        List<Project> batchProjects = new ArrayList<>();
        File[] batchDirs = fullDir.listFiles(File::isDirectory);
        if (batchDirs == null) {
            throw new RuntimeException("invalid project package file: " + fullDir);
        }

        for (File batchDir : batchDirs) {
            LOGGER.info("parsing project dir: {}", batchDir.getAbsolutePath());
            File[] zips = batchDir.listFiles((dir, name) -> name.endsWith(".zip"));
            if (zips.length > 0) {
                File unzippedDir = ZipUtils.unzipExportFile(zips[0]);
                batchProjects.add(parseBatchDir(unzippedDir));
            }
        }

        if (CollectionUtils.isEmpty(batchProjects)) {
            throw new RuntimeException("no project dir found, invalid project package file: " + fullDir);
        }

        // merge batch projects into single one
        Project project = new Project();
        BeanUtils.copyProperties(batchProjects.get(0), project, "flows", "nodes", "functions", "resources");
        project.setNodes(batchProjects.stream()
            .filter(p -> CollectionUtils.isNotEmpty(p.getNodes()))
            .map(Project::getNodes).flatMap(List::stream).collect(Collectors.toList()));
        project.setFlows(batchProjects.stream()
            .filter(p -> CollectionUtils.isNotEmpty(p.getFlows()))
            .map(Project::getFlows).flatMap(List::stream).collect(Collectors.toList()));
        project.setFunctions(batchProjects.stream()
            .filter(p -> CollectionUtils.isNotEmpty(p.getFunctions()))
            .map(Project::getFunctions).flatMap(List::stream).collect(Collectors.toList()));
        project.setResources(batchProjects.stream()
            .filter(p -> CollectionUtils.isNotEmpty(p.getResources()))
            .map(Project::getResources).flatMap(List::stream).collect(Collectors.toList()));
        return project;
    }

    private Project parseBatchDir(File unzippedDir) throws IOException {
        File workflowXml = new File(unzippedDir.getAbsolutePath() + File.separator + Constants.WORKFLOW_XML);
        Preconditions.checkArgument(workflowXml.exists(), workflowXml + " file not exists");

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // for startEffectDate/endEffectDate
        xmlMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        Project project = xmlMapper.readValue(IOUtils.toString(new FileReader(workflowXml)), Project.class);
        ListUtils.emptyIfNull(project.getNodes()).stream().forEach(node -> {
            File filePath = new File(Joiner.on(File.separator).join(unzippedDir.getAbsolutePath(), node.getFilePath()));
            node.setFilePath(filePath.getAbsolutePath());
        });

        ListUtils.emptyIfNull(project.getFlows()).stream().forEach(flow -> {
            ListUtils.emptyIfNull(flow.getNodes()).stream().forEach(node -> {
                File filePath = new File(
                    Joiner.on(File.separator).join(unzippedDir.getAbsolutePath(), node.getFilePath()));
                node.setFilePath(filePath.getAbsolutePath());
            });
        });

        ListUtils.emptyIfNull(project.getResources()).stream().forEach(resource -> {
            File filePath = new File(Joiner.on(File.separator).join(
                unzippedDir.getAbsolutePath(), resource.getFilePath()));
            resource.setFilePath(filePath.getAbsolutePath());
        });

        ListUtils.emptyIfNull(project.getFunctions()).stream().forEach(function -> {
            File filePath = new File(
                Joiner.on(File.separator).join(unzippedDir.getAbsolutePath(), function.getFilePath()));
            function.setFilePath(filePath.getAbsolutePath());
        });
        return project;
    }

    public List<DwResource> getResourceList() {
        return resourceList;
    }

    public List<Function> getFunctionList() {
        return functionList;
    }
}