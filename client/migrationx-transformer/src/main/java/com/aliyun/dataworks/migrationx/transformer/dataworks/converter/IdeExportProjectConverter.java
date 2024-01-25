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

import com.aliyun.dataworks.migrationx.domain.dataworks.constants.DataWorksConstants;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.CostumedDateFormat;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwFolder;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwFunction;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwResource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwUserDefinedNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Function;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeContext;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Solution;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeApplication;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeBizInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeEngineInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeExportVersion;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeFile;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeNodeInputOutput;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeSolution;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeUdfContent;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeUserDefinedNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.DmMajorVersion;
import com.aliyun.dataworks.common.spec.domain.dw.types.ModelTreeRoot;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.RerunMode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowVersion;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DefaultNodeTypeUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.NodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.annotation.DependsOn;
import com.aliyun.dataworks.migrationx.transformer.core.loader.ConfigPropertiesLoader;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItem;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItemType;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportRiskLevel;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author sam.liux
 * @date 2019/09/02
 */
public class IdeExportProjectConverter extends AbstractBaseConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdeExportProjectConverter.class);

    public static final String DIR_FILE_CONTENT = "file_content";
    public static final String DIR_RESOURCE = "resource";
    public static final String FILE_ENGINE_INFO = "engineinfo.txt";
    public static final String FILE_USER_NODE_INFO = "userNodeInfo.txt";
    public static final String FILE_VERSION = "version.txt";
    public static final Pattern IO_PATTERN_ID = Pattern.compile("(\\w+)\\.\\d+_out");

    @DependsOn
    private ConfigPropertiesLoader propertiesLoader;
    private Project project;
    private File unzippedPath;

    public IdeExportProjectConverter() {
        super(AssetType.DW_EXPORT, IdeExportProjectConverter.class.getSimpleName());
    }

    private void convertWorkflow(IdeApplication ideApp) {
        project.setWorkflows(ListUtils.emptyIfNull(ideApp.getBizInfos()).stream()
            .map(ideBizInfo -> convertBizInfo(ideApp, ideBizInfo))
            .collect(Collectors.toList())
        );

        project.setAdHocQueries(ListUtils.emptyIfNull(ideApp.getOtherFiles()).stream()
            .filter(ideFile -> ideFile.getUseType().equals(NodeUseType.AD_HOC.getValue()))
            .map(ideFile -> convertIdeFileToNode(ideFile))
            .collect(Collectors.toList())
        );

        File fileContentDir = new File(unzippedPath, DIR_FILE_CONTENT);
        project.setComponents(
            restoreOthers(fileContentDir, NodeUseType.COMPONENT, ModelTreeRoot.COMPONENT_ROOT.getRootKey()));
        processOldWorkflow(project, ideApp);
    }

    private List<Node> restoreOthers(File unzippedDir, NodeUseType nodeUseType, String dirName) {
        File tablesDir = new File(unzippedDir, dirName);
        if (!tablesDir.exists()) {
            return null;
        }

        List<Node> nodes = FileUtils.listFiles(tablesDir, new String[] {"xml"}, false).stream()
            .map(file -> {
                try {
                    XmlMapper xmlMapper = new XmlMapper();
                    xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    DwNode node = xmlMapper.readValue(file, DwNode.class);
                    node.setNodeUseType(nodeUseType);
                    return node;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        return nodes;
    }

    private void processOldWorkflow(Project project, IdeApplication ideApp) {
        boolean hasOldWorkflow = ListUtils.emptyIfNull(ideApp.getFolders()).stream()
            .anyMatch(ideFolder ->
                StringUtils.startsWith(ideFolder.getFolderItemPath(), DataWorksConstants.OLD_VERSION_WORKFLOW_FOLDER));
        if (!hasOldWorkflow) {
            return;
        }

        DwWorkflow oldWorkflow = new DwWorkflow();
        oldWorkflow.setScheduled(true);
        oldWorkflow.setName(DataWorksConstants.OLD_VERSION_WORKFLOW_NAME);
        oldWorkflow.setProjectRef(project);

        ListUtils.emptyIfNull(ideApp.getOtherFiles()).stream()
            .filter(ideFile -> StringUtils.isBlank(ideFile.getBizIdStr())
                && StringUtils.startsWith(ideFile.getFolderPath(), DataWorksConstants.OLD_VERSION_WORKFLOW_FOLDER))
            .forEach(ideFile -> convertIdeFile(oldWorkflow, ideFile));

        // 最后处理innerFiles
        ListUtils.emptyIfNull(ideApp.getInnerFiles()).stream()
            .filter(ideFile -> StringUtils.isNotBlank(ideFile.getOuterFile()) &&
                StringUtils.startsWith(ideFile.getOuterFile(), DataWorksConstants.OLD_VERSION_WORKFLOW_FOLDER))
            .forEach(ideFile -> convertInnerFile(oldWorkflow, ideFile));

        ListUtils.emptyIfNull(ideApp.getUdfFiles()).stream()
            .filter(ideFile -> StringUtils.isBlank(ideFile.getBizIdStr())
                && StringUtils.startsWith(ideFile.getFolderPath(), DataWorksConstants.OLD_VERSION_WORKFLOW_FOLDER))
            .forEach(ideFile -> convertIdeFile(oldWorkflow, ideFile));

        ListUtils.emptyIfNull(ideApp.getResourceFiles()).stream()
            .filter(ideFile -> StringUtils.isBlank(ideFile.getBizIdStr())
                && StringUtils.startsWith(ideFile.getFolderPath(), DataWorksConstants.OLD_VERSION_WORKFLOW_FOLDER))
            .forEach(ideFile -> convertIdeFile(oldWorkflow, ideFile));

        project.getWorkflows().add(oldWorkflow);
    }

    private Solution convertSolution(IdeSolution ideSolution) {
        Solution solution = new Solution();
        solution.setName(ideSolution.getSolutionName());
        solution.setDescription(ideSolution.getSolDesc());
        solution.setOwner(ideSolution.getOwner());
        solution.setDmMajorVersion(project.getDmMajorVersion());
        return solution;
    }

    private DwWorkflow convertBizInfo(IdeApplication ideApp, IdeBizInfo ideBizInfo) {
        DwWorkflow dwWorkflow = new DwWorkflow();
        dwWorkflow.setScheduled(ideBizInfo.getUseType() == 0);
        dwWorkflow.setName(ideBizInfo.getBizName());
        dwWorkflow.setProjectRef(project);
        dwWorkflow.setDmMajorVersion(project.getDmMajorVersion());
        dwWorkflow.setOwner(ideBizInfo.getOwner());
        dwWorkflow.setParameters(ideBizInfo.getParameters());
        if (ideBizInfo.getVersion() != null && Integer.valueOf(1).equals(ideBizInfo.getVersion())) {
            dwWorkflow.setVersion(WorkflowVersion.V3);
        } else {
            dwWorkflow.setVersion(WorkflowVersion.V2);
        }
        LOGGER.debug("dwWorkflow: {}", dwWorkflow);

        ListUtils.emptyIfNull(ideApp.getUdfFiles()).stream()
            .filter(ideFile -> ideBizInfo.getBizName().equals(ideFile.getBizIdStr()))
            .filter(ideFile -> ideFile.getBizUseType().equals(ideBizInfo.getUseType()))
            .forEach(ideFile -> convertIdeFile(dwWorkflow, ideFile));

        ListUtils.emptyIfNull(ideApp.getResourceFiles()).stream()
            .filter(ideFile -> ideBizInfo.getBizName().equals(ideFile.getBizIdStr()))
            .filter(ideFile -> ideFile.getBizUseType().equals(ideBizInfo.getUseType()))
            .forEach(ideFile -> convertIdeFile(dwWorkflow, ideFile));

        ListUtils.emptyIfNull(ideApp.getOtherFiles()).stream()
            .filter(ideFile -> ideBizInfo.getBizName().equals(ideFile.getBizIdStr()))
            .filter(ideFile -> ideFile.getBizUseType().equals(ideBizInfo.getUseType()))
            .forEach(ideFile -> convertIdeFile(dwWorkflow, ideFile));

        // 最后处理innerFiles
        ListUtils.emptyIfNull(ideApp.getInnerFiles()).stream()
            .filter(ideFile -> StringUtils.isNotBlank(ideFile.getOuterFile()))
            .forEach(ideFile -> convertInnerFile(dwWorkflow, ideFile));
        return dwWorkflow;
    }

    private void convertInnerFile(DwWorkflow dwWorkflow, IdeFile ideFile) {
        String outerFile = ideFile.getOuterFile();
        String[] folders = StringUtils.split(outerFile, "/");
        if (!StringUtils.startsWith(outerFile, DataWorksConstants.OLD_VERSION_WORKFLOW_FOLDER)) {
            if (folders.length < 4) {
                LOGGER.warn("outer file path folder invalid: {}", outerFile);
                return;
            }

            String workflowName = folders[1];
            if (!workflowName.equals(dwWorkflow.getName())) {
                return;
            }
        }

        String nodeName = folders[folders.length - 1];
        dwWorkflow.getNodes().stream()
            .filter(node -> node.getName().equals(nodeName))
            .filter(node -> outerFile.startsWith(node.getFolder()))
            .findAny().ifPresent(node -> node.getInnerNodes().add(convertIdeFileToNode(ideFile)));
    }

    private void convertIdeFile(DwWorkflow dwWorkflow, IdeFile ideFile) {
        if (DefaultNodeTypeUtils.isResource(ideFile.getFileType())) {
            dwWorkflow.getResources().add(convertResource(dwWorkflow, ideFile));
        } else if (DefaultNodeTypeUtils.isFunction(ideFile.getFileType())) {
            dwWorkflow.getFunctions().add(convertFunction(dwWorkflow, ideFile));
        } else {
            dwWorkflow.getNodes().add(convertIdeFileToNode(ideFile));
        }
        dwWorkflow.setDmMajorVersion(project.getDmMajorVersion());
    }

    private DwNode convertIdeFileToNode(IdeFile ideFile) {
        LOGGER.debug("ideFile: {}", ideFile);
        DwNode node = new DwNode();
        node.setDmMajorVersion(project.getDmMajorVersion());
        node.setName(ideFile.getFileName());
        CodeProgramType type = CodeProgramType.getNodeTypeByCode(ideFile.getFileType());
        if (type == null) {
            node.setType(CodeProgramType.VIRTUAL.name());
            ReportItem reportItem = new ReportItem();
            reportItem.setName(ideFile.getFileName());
            reportItem.setType(ReportItemType.UNSUPPORTED_JOB_TYPE.name());
            reportItem.setMessage("set node as default virtual type: " + CodeProgramType.VIRTUAL.name());
            reportItems.add(reportItem);
        } else {
            node.setType(type.name());
        }
        node.setCode(ideFile.getContent());
        if (CollectionUtils.isNotEmpty(ideFile.getFileRelations())) {
            node.setFileRelations(GsonUtils.gson.toJson(ideFile.getFileRelations()));
        }

        // for node def
        if (ideFile.getNodeDef() != null) {
            node.setPauseSchedule(ideFile.getNodeDef().getIsStop() != null && ideFile.getNodeDef().getIsStop() == 1);
            node.setStartRightNow(ideFile.getNodeDef().getStartRightNow());
            node.setCronExpress(ideFile.getNodeDef().getCronExpress());
            node.setParameter(ideFile.getNodeDef().getParaValue());
            if (ideFile.getNodeDef().getReRunAble() == null) {
                node.setRerunMode(RerunMode.ALL_ALLOWED);
            } else {
                node.setRerunMode(RerunMode.getByValue(ideFile.getNodeDef().getReRunAble()));
            }
            node.setResourceGroup(ideFile.getNodeDef().getResgroupId());
            node.setCycleType(ideFile.getNodeDef().getCycleType());
            node.setLastModifyTime(ideFile.getNodeDef().getLastModifyTime());
            node.setLastModifyUser(ideFile.getNodeDef().getLastModifyUser());
            node.setMultiInstCheckType(ideFile.getNodeDef().getMultiinstCheckType());
            node.setPriority(ideFile.getNodeDef().getPriority());
            node.setDependentType(ideFile.getNodeDef().getDependentType());
            node.setStartEffectDate(ideFile.getNodeDef().getStartEffectDate());
            node.setEndEffectDate(ideFile.getNodeDef().getEndEffectDate());
            node.setTaskRerunInterval(ideFile.getNodeDef().getTaskRerunInterval());
            node.setTaskRerunTime(ideFile.getNodeDef().getTaskRerunTime());
            node.setDependentDataNode(ideFile.getNodeDef().getDependentDataNode());
            Type typeToken = new TypeToken<List<IdeNodeInputOutput>>() {}.getType();
            List<IdeNodeInputOutput> nodeDefIos = new ArrayList<>();
            if (StringUtils.isNotBlank(ideFile.getNodeDef().getInput())) {
                List<IdeNodeInputOutput> inputs = GsonUtils.gson.fromJson(ideFile.getNodeDef().getInput(), typeToken);
                if (CollectionUtils.isNotEmpty(inputs)) {
                    inputs.stream().forEach(i -> i.setType(0));
                    nodeDefIos.addAll(inputs);
                }
            }

            if (StringUtils.isNotBlank(ideFile.getNodeDef().getOutput())) {
                List<IdeNodeInputOutput> outputs = GsonUtils.gson.fromJson(ideFile.getNodeDef().getOutput(), typeToken);
                if (CollectionUtils.isNotEmpty(outputs)) {
                    outputs.stream().forEach(o -> o.setType(1));
                    nodeDefIos.addAll(outputs);
                }
            }
            ideFile.setNodeInputOutputs(ListUtils.emptyIfNull(ideFile.getNodeInputOutputs()));
            nodeDefIos.stream().forEach(nodeDefIo -> {
                if (!ideFile.getNodeInputOutputs().stream()
                    .filter(io -> io.getType() != null && io.getStr() != null)
                    .anyMatch(io -> io.getType().equals(nodeDefIo.getType()) && io.getStr()
                        .equalsIgnoreCase(nodeDefIo.getStr()))) {
                    ideFile.getNodeInputOutputs().add(nodeDefIo);
                }
            });
        }
        LOGGER.debug("ideFile: {}", ideFile);
        node.setInputs(ListUtils.emptyIfNull(ideFile.getNodeInputOutputs()).stream()
            .filter(io -> Integer.valueOf(0).equals(io.getType()))
            .map(io -> convertIdeNodeInputOutput(io))
            .collect(Collectors.toList()));
        node.setOutputs(ListUtils.emptyIfNull(ideFile.getNodeInputOutputs()).stream()
            .filter(io -> Integer.valueOf(1).equals(io.getType()))
            .map(io -> convertIdeNodeInputOutput(io))
            .collect(Collectors.toList()));

        LOGGER.debug("node: {}", node);
        node.getInputs().stream().forEach(nodeIo -> convertIoStr(node, nodeIo));
        node.getOutputs().stream().forEach(nodeIo -> convertIoStr(node, nodeIo));
        node.setOwner(ideFile.getOwner());

        node.setExtend(ideFile.getExtend());
        node.setFileDelete(ideFile.getFileDelete());
        node.setIsAutoParse(ideFile.getIsAutoParse());
        node.setCommitStatus(ideFile.getCommitStatus());
        node.setIsUserNode(ideFile.getIsUserNode());
        node.setSourcePath(ideFile.getSourcePath());
        node.setFolder(ideFile.getFolderPath());
        node.setNodeUseType(NodeUseType.getNodeUseTypeByValue(ideFile.getUseType()));
        node.setConnection(ideFile.getConnName());

        node.setInputContexts(CollectionUtils.emptyIfNull(ideFile.getNodeInputOutputContexts()).stream()
            .filter(io -> Integer.valueOf(0).equals(io.getType()))
            .map(io -> {
                NodeContext ctx = new NodeContext();
                BeanUtils.copyProperties(io, ctx);
                return ctx;
            }).collect(Collectors.toList()));
        node.setOutputContexts(CollectionUtils.emptyIfNull(ideFile.getNodeInputOutputContexts()).stream()
            .filter(io -> Integer.valueOf(1).equals(io.getType()))
            .map(io -> {
                NodeContext ctx = new NodeContext();
                BeanUtils.copyProperties(io, ctx);
                return ctx;
            }).collect(Collectors.toList()));
        NodeUtils.parseNodeDiResGroupInfo(node);
        return node;
    }

    private void convertIoStr(DwNode node, NodeIo nodeIo) {
        if (project == null || project.getExportProject() == null) {
            return;
        }

        Matcher m = IO_PATTERN_ID.matcher(nodeIo.getData());
        String io = nodeIo.getData();
        // for ${projectIdentifier}.${fileId}_out
        if (m.matches()) {
            io = RegExUtils.replaceFirst(io, "^\\w+", project.getName());
        }

        String exportProjectIdentifier = project.getExportProject().getName();
        String nodeName = node.getName();
        String pattern = exportProjectIdentifier + "." + nodeName;
        // for ${projectIdentifier}.${nodeName}
        if (Pattern.matches(pattern, io)) {
            io = RegExUtils.replaceFirst(io, exportProjectIdentifier, project.getName());
        }

        // for ${projectIdentifier}_root
        if (Pattern.matches(exportProjectIdentifier + "_root", io)) {
            io = RegExUtils.replaceFirst(io, exportProjectIdentifier, project.getName());
        }

        nodeIo.setData(io);
    }

    private NodeIo convertIdeNodeInputOutput(IdeNodeInputOutput ideNodeInputOutput) {
        DwNodeIo nodeIo = new DwNodeIo();
        nodeIo.setRefTableName(ideNodeInputOutput.getRefTableName());
        nodeIo.setData(ideNodeInputOutput.getStr());
        nodeIo.setParseType(ideNodeInputOutput.getParseType());
        nodeIo.setParentId(ideNodeInputOutput.getParentId());
        nodeIo.setIsDifferentApp(ideNodeInputOutput.getIsDifferentApp());
        return nodeIo;
    }

    private Function convertFunction(DwWorkflow dwWorkflow, IdeFile ideFile) {
        DwFunction function = new DwFunction();
        function.setName(ideFile.getFileName());
        function.setType(CodeProgramType.getNodeTypeNameByCode(ideFile.getFileType()));
        if (StringUtils.isNotBlank(ideFile.getContent())) {
            IdeUdfContent ideUdfContent = GsonUtils.gson.fromJson(ideFile.getContent(),
                new TypeToken<IdeUdfContent>() {}.getType());
            function.setArguments(ideUdfContent.getParamDesc());
            function.setDescription(ideUdfContent.getDescription());
            function.setCommand(ideUdfContent.getCmdDesc());
            function.setClazz(ideUdfContent.getClassName());
            function.setResource(ideUdfContent.getResources());
            function.setExample(ideUdfContent.getExample());
            function.setReturnValue(ideUdfContent.getReturnValue());
        }
        function.setFolder(ideFile.getFolderPath());
        function.setOwner(project.getOpUser());
        function.setWorkflowRef(dwWorkflow);
        function.setDmMajorVersion(project.getDmMajorVersion());
        return function;
    }

    private DwResource convertResource(DwWorkflow dwWorkflow, IdeFile resource) {
        DwResource dwResource = new DwResource();
        dwResource.setOriginResourceName(resource.getOriginResourceName());
        dwResource.setLocalPath(resource.getSourcePath());
        dwResource.setName(resource.getFileName());
        dwResource.setType(CodeProgramType.getNodeTypeByCode(resource.getFileType()).name());
        dwResource.setLargeFile(resource.getIsLarge() != null && resource.getIsLarge() == 1);
        dwResource.setOdps(resource.getIsOdps() != null && resource.getIsOdps() == 1);
        dwResource.setOwner(project.getOpUser());
        dwResource.setWorkflowRef(dwWorkflow);
        dwResource.setDmMajorVersion(project.getDmMajorVersion());
        dwResource.setFolder(resource.getFolderPath());
        return dwResource;
    }

    private File getApplicationXml(File unzippedPath) {
        File[] xmlFiles = unzippedPath.listFiles(f -> f.isFile() && f.getName().endsWith(".xml"));
        if (xmlFiles.length == 0) {
            throw new RuntimeException("application xml not found in path: " + unzippedPath.getAbsolutePath());
        }

        if (xmlFiles.length > 1) {
            throw new RuntimeException("multiple xml found in path: " + unzippedPath.getAbsolutePath());
        }
        return xmlFiles[0];
    }

    private IdeApplication readIdeApplication(File unzippedPath) throws IOException {
        String wholeXmlContent = IOUtils.toString(new FileInputStream(getApplicationXml(unzippedPath)), "utf-8");
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        xmlMapper.setDateFormat(new CostumedDateFormat());
        IdeApplication ideApp = xmlMapper.readValue(wholeXmlContent, IdeApplication.class);

        Optional.ofNullable(ideApp.getUdfFiles()).ifPresent(
            list -> list.stream().forEach(ideFile -> readIdeFileContent(unzippedPath, ideFile)));
        Optional.ofNullable(ideApp.getOtherFiles()).ifPresent(
            list -> list.stream().forEach(ideFile -> readIdeFileContent(unzippedPath, ideFile)));
        Optional.ofNullable(ideApp.getInnerFiles()).ifPresent(
            list -> list.stream().forEach(ideFile -> readIdeFileContent(unzippedPath, ideFile)));

        CollectionUtils.union(ListUtils.emptyIfNull(ideApp.getResourceFiles()),
                ListUtils.emptyIfNull(ideApp.getOtherFiles()))
            .stream().filter(f -> DefaultNodeTypeUtils.isResource(f.getFileType()))
            .forEach(ideFile -> {
                try {
                    adjustSourcePath(unzippedPath, ideFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        readIdeEngineInfos(unzippedPath, ideApp);
        readIdeUseDefinedNodes(unzippedPath, ideApp);
        readIdeExportVersion(unzippedPath, ideApp);
        return ideApp;
    }

    private void readIdeExportVersion(File unzippedPath, IdeApplication ideApp) throws IOException {
        File versionTxtFile = new File(Joiner.on(File.separator).join(unzippedPath.getAbsolutePath(), FILE_VERSION));
        if (versionTxtFile.exists()) {
            String versionTxt = IOUtils.toString(new FileReader(versionTxtFile));
            IdeExportVersion ideExportVersion = IdeExportVersion.valueOf(versionTxt);
            ideApp.setVersion(ideExportVersion);
        }
    }

    private void readIdeUseDefinedNodes(File unzippedPath, IdeApplication ideApp) throws IOException {
        File userNodeInfoTxtFile = new File(Joiner.on(File.separator).join(
            unzippedPath.getAbsolutePath(), FILE_USER_NODE_INFO));
        if (userNodeInfoTxtFile.exists()) {
            String userNodeInfoTxt = IOUtils.toString(new FileReader(userNodeInfoTxtFile));
            Map<String, List<IdeUserDefinedNode>> userNodeInfo = GsonUtils.gson.fromJson(
                userNodeInfoTxt, new TypeToken<HashMap<String, List<IdeUserDefinedNode>>>() {}.getType());
            ideApp.setUserDefinedNodeList(userNodeInfo.get("userNodeList"));
        }
    }

    private void readIdeEngineInfos(File unzippedPath, IdeApplication ideApp) throws IOException {
        File engineInfoTxtFile = new File(Joiner.on(File.separator).join(
            unzippedPath.getAbsolutePath(), FILE_ENGINE_INFO));
        if (engineInfoTxtFile.exists()) {
            String engineInfoTxt = IOUtils.toString(new FileReader(engineInfoTxtFile));
            IdeEngineInfo engineInfo = GsonUtils.gson.fromJson(engineInfoTxt,
                new TypeToken<IdeEngineInfo>() {}.getType());
            ideApp.setEngineInfo(engineInfo);
        }
    }

    private void adjustSourcePath(File unzippedPath, IdeFile ideFile) throws IOException {
        String resourceName = StringUtils.isNotBlank(ideFile.getOriginResourceName()) ?
            ideFile.getOriginResourceName() : ideFile.getFileName();

        if (Integer.valueOf(CodeProgramType.ODPS_PYTHON.getCode()).equals(ideFile.getFileType()) ||
            Integer.valueOf(CodeProgramType.ODPS_FILE.getCode()).equals(ideFile.getFileType())) {
            resourceName = resourceName + ".txt";
        }

        File sourcePath = new File(unzippedPath, Joiner.on(File.separator).join(DIR_RESOURCE, resourceName));
        if (!sourcePath.exists() && StringUtils.isNotBlank(ideFile.getSourcePath()) &&
            ideFile.getSourcePath().startsWith(File.separator)) {
            sourcePath = new File(unzippedPath, ideFile.getSourcePath());
        }

        if (!sourcePath.exists()) {
            sourcePath = new File(unzippedPath, Joiner.on(File.separator).join(
                DIR_FILE_CONTENT, ideFile.getFolderPath(), resourceName));
        }

        if (!sourcePath.exists()) {
            String msg = String.format("resource file not found: %s, fake it instead.", sourcePath.getAbsoluteFile());
            LOGGER.warn(msg);
            sourcePath.getParentFile().mkdirs();
            FileUtils.writeStringToFile(sourcePath, "this is a fake file for not existed resource", "utf-8");
            ReportItem reportItem = new ReportItem();
            reportItem.setName(ideFile.getFileName());
            reportItem.setMessage(msg);
            reportItem.setRiskLevel(ReportRiskLevel.STRONG_WARNINGS);
            reportItem.setType(ReportItemType.RESOURCE_FILE_NOT_FOUND.name());
            reportItems.add(reportItem);
        }

        ideFile.setSourcePath(sourcePath.getAbsolutePath());
    }

    private void readIdeFileContent(File unzippedPath, IdeFile ideFile) {
        if (StringUtils.isBlank(ideFile.getSourcePath())) {
            return;
        }

        File contentFile = new File(unzippedPath, Joiner.on(File.separator)
            .join(DIR_FILE_CONTENT, ideFile.getSourcePath()));
        if (contentFile.exists()) {
            try {
                String content = IOUtils.toString(new FileInputStream(contentFile), "utf-8");
                ideFile.setContent(content);
            } catch (IOException e) {
                LOGGER.warn("ide file content not found: {}", ideFile.getSourcePath());
            }
        }
    }

    @Override
    public List<DwWorkflow> convert(Asset asset) throws Exception {
        Preconditions.checkNotNull(asset, "asset is null");
        Preconditions.checkNotNull(asset.getPath(), "dw export unzipped directory is null");
        File unzippedPath = asset.getPath();
        Preconditions.checkArgument(unzippedPath.exists(), "unzipped export project directory not exists");

        this.unzippedPath = unzippedPath;
        IdeApplication ideApp = readIdeApplication(unzippedPath);
        Preconditions.checkNotNull(ideApp, "read null IdeApplication");

        convertProject(ideApp);
        convertWorkflow(ideApp);
        convertFolder(ideApp);
        convertUserDefinedNodes(ideApp);
        convertSolution(ideApp);

        workflowList = ListUtils.emptyIfNull(project.getWorkflows()).stream().map(workflow -> {
            DwWorkflow dwWorkflow = new DwWorkflow();
            BeanUtils.copyProperties(workflow, dwWorkflow);
            return dwWorkflow;
        }).collect(Collectors.toList());
        return workflowList;
    }

    private void convertSolution(IdeApplication ideApp) {
        project.setSolutions(ListUtils.emptyIfNull(ideApp.getSolutions()).stream()
            .map(ideSolution -> convertSolution(ideSolution)).collect(Collectors.toList()));
    }

    private void convertProject(IdeApplication ideApp) {
        project.setEngineInfo(ideApp.getEngineInfo());
        project.setDmMajorVersion(DmMajorVersion.DATAWORKS_V2.name());
        if (IdeExportVersion.V2.equals(ideApp.getVersion())) {
            project.setDmMajorVersion(DmMajorVersion.DATAWORKS_V2.name());
        }

        if (IdeExportVersion.V3.equals(ideApp.getVersion())) {
            project.setDmMajorVersion(DmMajorVersion.DATAWORKS_V3.name());
        }

        if (StringUtils.isNotBlank(ideApp.getProjectIdentifier())) {
            project.setName(ideApp.getProjectIdentifier());
        }
    }

    private void convertUserDefinedNodes(IdeApplication ideApp) {
        List<IdeUserDefinedNode> userDefinedNodes = ideApp.getUserDefinedNodeList();
        project.setUserDefinedNodes(ListUtils.emptyIfNull(userDefinedNodes).stream().map(ideUserDefinedNode -> {
            DwUserDefinedNode userDefinedNode = new DwUserDefinedNode();
            BeanUtils.copyProperties(ideUserDefinedNode, userDefinedNode);
            userDefinedNode.setProjectRef(project);
            userDefinedNode.setDmMajorVersion(project.getDmMajorVersion());
            return userDefinedNode;
        }).collect(Collectors.toList()));
    }

    private void convertFolder(IdeApplication ideApp) {
        project.setFolders(ListUtils.emptyIfNull(ideApp.getFolders()).stream().map(ideFolder -> {
            DwFolder folder = new DwFolder();
            BeanUtils.copyProperties(ideFolder, folder);
            folder.setDmMajorVersion(project.getDmMajorVersion());
            return folder;
        }).collect(Collectors.toList()));

        project.getWorkflows().stream().forEach(workflow ->
            project.getFolders().stream()
                .filter(folder -> workflow.getName().equals(folder.getBizId()))
                .forEach(folder -> ((DwFolder)folder).setWorkflowRef((DwWorkflow)workflow))
        );
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}