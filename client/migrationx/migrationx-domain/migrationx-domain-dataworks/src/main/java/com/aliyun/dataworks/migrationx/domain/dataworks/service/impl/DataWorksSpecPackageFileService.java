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

package com.aliyun.dataworks.migrationx.domain.dataworks.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.adapter.SpecHandlerContext;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.Code;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.LabelType;
import com.aliyun.dataworks.common.spec.domain.dw.types.ModelTreeRoot;
import com.aliyun.dataworks.common.spec.domain.dw.types.ProductModule;
import com.aliyun.dataworks.common.spec.domain.enums.ArtifactType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeInstanceModeType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRecurrenceType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRerunModeType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDatasource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecRuntimeResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DataWorksPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Datasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwResource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Resource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Workflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.RerunMode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.tenant.EnvType;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.NodeSpecAdapter;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntityAdapter;
import com.aliyun.dataworks.migrationx.domain.dataworks.standard.service.AbstractPackageFileService;
import com.aliyun.migrationx.common.context.TransformerContext;
import com.aliyun.migrationx.common.utils.Config;
import com.aliyun.migrationx.common.utils.ZipUtils;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 基于Spec描述文件的包格式文件服务
 *
 * @author 聿剑
 * @date 2023/02/15
 */
@Slf4j
public class DataWorksSpecPackageFileService extends AbstractPackageFileService<DataWorksPackage> {
    private static final String SPEC_FILE_SUFFIX = ".schedule.json";
    private static final String SPEC_FORMAT_FILE_NAME = "SPEC.FORMAT";

    @Override
    protected boolean isProjectRoot(File file) {
        return false;
    }

    @Override
    public void load(DataWorksPackage packageObj) throws Exception {

    }

    @Override
    public DataWorksPackage getPackage() throws Exception {
        return null;
    }

    @Override
    public void write(DataWorksPackage packageModelObject, File targetPackageFile) throws Exception {
        String targetDirName = RegExUtils.replaceAll(targetPackageFile.getName(), "\\..*$", "");
        File targetTmp = new File(targetPackageFile.getParentFile(), ".tmp");
        if (targetTmp.exists()) {
            FileUtils.deleteDirectory(targetTmp);
        }

        File targetWorkspace = new File(targetTmp, targetDirName);
        FileUtils.forceMkdir(targetWorkspace);
        writeSpecFormatFile(targetWorkspace);

        writeDataStudioDirectory(packageModelObject, targetWorkspace);
        writeDatasourceDirectory(packageModelObject, targetWorkspace);
        if (Config.INSTANCE.isZipSpec()) {
            ZipUtils.zipDir(targetTmp, targetPackageFile);
            log.info("zipped file: {}", targetPackageFile);
        } else {
            log.warn("unzip file : {}", targetWorkspace);
        }
    }

    private void writeDatasourceDirectory(DataWorksPackage packageModelObject, File targetWorkspace) throws IOException {
        File rootDir = new File(targetWorkspace, ProductModule.DATA_SOURCE.getName());
        if (rootDir.exists()) {
            log.info("deleting existing root directory: {}", rootDir);
            FileUtils.deleteDirectory(rootDir);
        }

        Optional.ofNullable(packageModelObject.getDwProject()).map(Project::getDatasources).map(ListUtils::emptyIfNull).ifPresent(datasources -> {
            datasources.stream().parallel().forEach(dmDatasourceBO -> {
                String path = String.join(File.separator,
                    dmDatasourceBO.getType(),
                    StringUtils.defaultIfBlank(dmDatasourceBO.getSubType(), dmDatasourceBO.getType()),
                    Arrays.stream(EnvType.values()).filter(env -> Objects.equals(env.name(), dmDatasourceBO.getEnvType())).findAny()
                        .orElse(EnvType.PRD).name(),
                    dmDatasourceBO.getName());
                path = String.join(File.separator, rootDir.getAbsolutePath(), path);
                File dataSourceDir = new File(path);
                boolean ret = dataSourceDir.mkdirs();
                log.info("create data source directory: {}, {}", dataSourceDir, ret);
                File dataSourceFile = new File(dataSourceDir, dmDatasourceBO.getName() + ".json");
                try {
                    FileUtils.writeStringToFile(dataSourceFile, getDataSourceSpec(dmDatasourceBO), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private String getDataSourceSpec(Datasource datasource) {
        Specification<DataWorksWorkflowSpec> specSpecification = new Specification<>();
        specSpecification.setVersion(SpecVersion.V_1_1_0.getLabel());
        specSpecification.setKind(ProductModule.DATA_SOURCE.getName());
        Map<String, Object> metadata = new HashMap<>();
        specSpecification.setMetadata(metadata);
        DataWorksWorkflowSpec dwSpec = new DataWorksWorkflowSpec();
        specSpecification.setSpec(dwSpec);

        dwSpec.setDatasources(Optional.ofNullable(datasource).map(ds -> {
            SpecDatasource specDatasource = new SpecDatasource();
            specDatasource.setId(ds.getName());
            specDatasource.setName(ds.getName());
            specDatasource.setMetadata(metadata);
            specDatasource.setType(ds.getType());
            specDatasource.setSubType(ds.getSubType());
            specDatasource.setConfig(ds.getConnection());
            return specDatasource;
        }).map(Collections::singletonList).orElse(Collections.emptyList()));
        return SpecUtil.writeToSpec(specSpecification);
    }

    private void writeDataStudioDirectory(DataWorksPackage packageModelObject, File targetWorkspace) throws IOException {
        File dataStudioDir = new File(targetWorkspace, ProductModule.DATA_STUDIO.getName());
        FileUtils.forceMkdir(dataStudioDir);
        writeWorkflows(packageModelObject, dataStudioDir);
    }

    private void writeSpecFormatFile(File targetWorkspace) {
        try {
            FileUtils.writeStringToFile(new File(targetWorkspace, SPEC_FORMAT_FILE_NAME), "", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeWorkflows(DataWorksPackage packageModelObject, File targetWorkspace) {
        Optional.ofNullable(packageModelObject)
            .map(DataWorksPackage::getDwProject)
            .map(Project::getWorkflows)
            .orElse(new ArrayList<>())
            .forEach(w -> {
                try {
                    this.writeWorkflow(w, targetWorkspace);
                } catch (Exception e) {
                    log.error("write workflow error: ", e);
                    if (!Config.INSTANCE.isSpecContinueWithError()) {
                        throw new RuntimeException(e);
                    }
                }
            });
    }

    private void writeWorkflow(Workflow workflow, File targetWorkspace) throws IOException {
        File workflowDir = makeWorkflowDirectory(targetWorkspace, workflow);
        log.info("workflow directory: {}", workflowDir);

        writeWorkflowNodes(workflowDir, workflow);
        writeResourceFiles(workflowDir, workflow.getResources());
    }

    private void writeWorkflowNodes(File workflowDir, Workflow workflow) {
        ListUtils.emptyIfNull(workflow.getNodes()).forEach(node -> {
            try {
                writeNodeSpec(workflowDir, node);
                writeNodeCode(workflowDir, node);
                TransformerContext.getCollector().markSuccessSpecProcess(workflow.getName(), node.getName());
            } catch (Throwable e) {
                TransformerContext.getCollector().markFailedSpecProcess(workflow.getName(), node.getName());
                throw e;
            }
        });
    }

    private void writeResourceFiles(File workflowPath, List<Resource> resources) {
        resources.stream().filter(r -> r instanceof DwResource).forEach(resource -> {
            writeResourceSpec(workflowPath, resource);
            writeResourceFile(workflowPath, resource);
        });
    }

    private void writeResourceSpec(File workflowDir, Resource resource) {
        NodeSpecAdapter adapter = new NodeSpecAdapter();
        SpecHandlerContext context = new SpecHandlerContext();
        context.setLocale(locale);
        //resource.setPath(resFile.getName());
        Specification<DataWorksWorkflowSpec> specification = adapter.getResourceSpecObject(resource, context);
        // write node spec
        String spec = SpecUtil.writeToSpec(specification);
        File resFile = new File(workflowDir, Joiner.on(File.separator).join(resource.getFolder(), resource.getName() + SPEC_FILE_SUFFIX));

        try {
            FileUtils.writeStringToFile(resFile, spec, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeResourceFile(File workflowPath, Resource resource) {
        try {
            File resFile = new File(workflowPath, Joiner.on(File.separator).join(resource.getFolder(), resource.getName()));
            FileUtils.copyFile(new File(((DwResource)resource).getLocalPath()), resFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeNodeCode(File workflowDir, Node node) {
        CodeProgramType prgType = CodeProgramType.of(node.getType());
        File nodeDir = new File(workflowDir, Joiner.on(File.separator).join(node.getFolder(), node.getName()));
        String fileName = Optional.ofNullable(prgType.getExtension()).map(ext -> node.getName() + ext).orElse(node.getName());
        File codeFile = new File(nodeDir, fileName);
        try {
            CodeModel<Code> code = CodeModelFactory.getCodeModel(node.getType(), node.getCode());
            if (code.getCodeModel() instanceof EmrCode) {
                String content = code.getCodeModel().getSourceCode();
                FileUtils.writeStringToFile(codeFile, content, StandardCharsets.UTF_8);
            } else {
                FileUtils.writeStringToFile(codeFile, node.getCode(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeNodeSpec(File workflowDir, Node node) {
        NodeSpecAdapter adapter = new NodeSpecAdapter();
        SpecHandlerContext context = new SpecHandlerContext();
        context.setLocale(locale);
        node.setGlobalUuid(UUID.randomUUID().toString());
        completeNodeFolder(node);

        Specification<DataWorksWorkflowSpec> specification = adapter.getNodeSpecObject(new DwNodeEntityAdapter((DwNode)node), context);
        // write node spec
        String spec = SpecUtil.writeToSpec(specification);
        File nodeDir = new File(workflowDir, Joiner.on(File.separator).join(node.getFolder(), node.getName()));
        File specFile = new File(nodeDir, node.getName() + SPEC_FILE_SUFFIX);
        try {
            FileUtils.writeStringToFile(specFile, spec, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SpecNode toSpecNode(File workflowDir, Node node) {
        SpecNode specNode = new SpecNode();
        specNode.setId(node.getGlobalUuid());
        specNode.setName(node.getName());
        specNode.setOwner(node.getOwner());

        // set Recurrence
        if (Objects.equals(node.getNodeUseType(), NodeUseType.SKIP)) {
            specNode.setRecurrence(NodeRecurrenceType.SKIP);
        } else {
            if (BooleanUtils.isTrue(node.getPauseSchedule())) {
                specNode.setRecurrence(NodeRecurrenceType.PAUSE);
            } else {
                specNode.setRecurrence(NodeRecurrenceType.NORMAL);
            }
        }

        // set rerun mode
        node.setRerunMode(Optional.ofNullable(node.getRerunMode()).orElse(RerunMode.ALL_ALLOWED));
        switch (node.getRerunMode()) {
            case FAILURE_ALLOWED: {
                specNode.setRerunMode(NodeRerunModeType.FAILURE_ALLOWED);
                break;
            }
            case ALL_ALLOWED: {
                specNode.setRerunMode(NodeRerunModeType.ALL_ALLOWED);
                break;
            }
            case ALL_DENIED: {
                specNode.setRerunMode(NodeRerunModeType.ALL_DENIED);
                break;
            }
        }
        specNode.setRerunTimes(node.getTaskRerunTime());
        specNode.setRerunInterval(node.getTaskRerunInterval());

        // set runtime resource
        if (StringUtils.isNotBlank(node.getResourceGroup())) {
            SpecRuntimeResource rt = new SpecRuntimeResource();
            rt.setResourceGroup(node.getResourceGroup());
            specNode.setRuntimeResource(rt);
        }

        // set instance mode
        if (BooleanUtils.isTrue(node.getStartRightNow())) {
            specNode.setInstanceMode(NodeInstanceModeType.IMMEDIATELY);
        } else {
            specNode.setInstanceMode(NodeInstanceModeType.T_PLUS_1);
        }

        setScript(workflowDir, node, specNode);
        specNode.setInputs(ListUtils.emptyIfNull(node.getInputs()).stream().map(in -> {
            SpecNodeOutput specArtifact = new SpecNodeOutput();
            specArtifact.setData(in.getData());
            specArtifact.setRefTableName(in.getRefTableName());
            specArtifact.setArtifactType(ArtifactType.NODE_OUTPUT);
            return specArtifact;
        }).collect(Collectors.toList()));
        specNode.setOutputs(ListUtils.emptyIfNull(node.getOutputs()).stream().map(in -> {
            SpecNodeOutput specArtifact = new SpecNodeOutput();
            specArtifact.setData(in.getData());
            specArtifact.setRefTableName(in.getRefTableName());
            specArtifact.setArtifactType(ArtifactType.NODE_OUTPUT);
            return specArtifact;
        }).collect(Collectors.toList()));

        //
        if (node.getConnection() != null) {
            SpecDatasource datasource = new SpecDatasource();
            datasource.setName(node.getConnection());
            specNode.setDatasource(datasource);
        }

        return specNode;
    }

    private void setScript(File workflowDir, Node node, SpecNode specNode) {
        SpecScript script = new SpecScript();

        SpecScriptRuntime runtime = new SpecScriptRuntime();
        CalcEngineType engineType = CodeProgramType.of(node.getType()).getCalcEngineType();
        runtime.setCommand(node.getType());
        runtime.setEngine(engineType.getName());
        CodeModel<Code> codeModel = CodeModelFactory.getCodeModel(node.getType(), null);
        runtime.setTemplate(codeModel.getTemplate());

        script.setRuntime(runtime);
        script.setId(specNode.getId());
        script.setParameters(toNodeParameters(node));
        CodeProgramType prgType = CodeProgramType.valueOf(node.getType());
        String pathToFile = Joiner.on(File.separator).join(node.getFolder(),
            Optional.ofNullable(prgType.getExtension()).map(ext -> node.getName() + ext).orElse(node.getName()));

        File folder = new File(workflowDir, pathToFile);
        List<String> paths = new ArrayList<>();
        while (!folder.equals(workflowDir)) {
            folder = folder.getParentFile();
            paths.add(folder.getName());
        }
        paths.add(workflowDir.getParentFile().getName());
        String[] arr = new String[paths.size()];
        paths.toArray(arr);
        CollectionUtils.reverseArray(arr);
        script.setPath(Joiner.on(File.separator).join(arr));
        specNode.setScript(script);
    }

    private static List<SpecVariable> toNodeParameters(Node node) {
        return Arrays.stream(StringUtils.split(StringUtils.trim(StringUtils.defaultIfBlank(node.getParameter(), "")), " "))
            .filter(StringUtils::isNotBlank)
            .map(kv -> StringUtils.split(kv, "="))
            .filter(Objects::nonNull)
            .filter(kvPair -> kvPair.length > 0)
            .map(kvPair -> {
                SpecVariable var = new SpecVariable();
                var.setName(kvPair[0]);
                var.setType(VariableType.CONSTANT);
                var.setScope(VariableScopeType.NODE_PARAMETER);
                var.setValue(kvPair.length > 1 ? kvPair[1] : "");
                return var;
            }).collect(Collectors.toList());
    }

    private void completeNodeFolder(Node node) {
        CodeProgramType prgType = CodeProgramType.valueOf(node.getType());
        CalcEngineType engineType = prgType.getCalcEngineType();
        LabelType labelType = prgType.getLabelType();
        List<String> paths = new ArrayList<>();
        Optional.ofNullable(engineType).ifPresent(e -> paths.add(e.getDisplayName(locale)));
        Optional.ofNullable(labelType).ifPresent(e -> paths.add(e.getDisplayName(locale)));
        node.setFolder(Joiner.on(File.separator).join(paths));
    }

    private File makeWorkflowDirectory(File targetWorkspace, Workflow workflow) throws IOException {
        List<String> paths = new ArrayList<>();
        WorkflowType type = Optional.ofNullable(workflow.getType()).orElse(WorkflowType.BUSINESS);
        switch (type) {
            case BUSINESS: {
                paths.add(ModelTreeRoot.BIZ_ROOT.getDisplayName(locale));
                break;
            }
            case OLD_WORKFLOW: {
                paths.add(ModelTreeRoot.WORK_FLOW_ROOT_NEW.getDisplayName(locale));
                break;
            }
            case MANUAL_BUSINESS: {
                paths.add(ModelTreeRoot.MANUAL_WORK_FLOW_ROOT.getDisplayName(locale));
                break;
            }
        }
        paths.add(workflow.getName());
        File directory = new File(targetWorkspace, Joiner.on(File.separator).join(paths));
        FileUtils.forceMkdir(directory);
        return directory;
    }
}
