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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.v2;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerVersion;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.datasource.BaseDataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.datasource.DataSourceFactory;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DolphinSchedulerV2Context;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.ProcessDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.DbType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.TaskType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.process.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.subprocess.SubProcessParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwDatasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.connection.JdbcConnection;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.tenant.EnvType;
import com.aliyun.dataworks.migrationx.transformer.core.loader.ProjectAssetLoader;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.AbstractDolphinSchedulerConverter;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.common.base.Joiner;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * for dolphinscheduler v2.0.1 and later convert dolphinscheduler process to dataworks model
 *
 * @author 聿剑
 * @date 2022/10/12
 */
@Slf4j
public class DolphinSchedulerV2Converter extends AbstractDolphinSchedulerConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DolphinSchedulerV2Converter.class);

    public static final DolphinSchedulerVersion version = DolphinSchedulerVersion.V2;

    private List<DagData> dagDataList = new ArrayList<>();
    private List<DwWorkflow> dwWorkflowList = new ArrayList<>();
    private DolphinSchedulerPackage<com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project, DagData, DataSource, ResourceInfo, UdfFunc> dolphinSchedulerPackage;

    public DolphinSchedulerV2Converter(DolphinSchedulerPackage dolphinSchedulerPackage) {
        super(AssetType.DOLPHINSCHEDULER, DolphinSchedulerV2Converter.class.getSimpleName());
        this.dolphinSchedulerPackage = dolphinSchedulerPackage;
    }

    public DolphinSchedulerV2Converter(AssetType assetType, String name) {
        super(assetType, name);
    }

    public DolphinSchedulerV2Converter(AssetType assetType, String name,
            ProjectAssetLoader projectAssetLoader) {
        super(assetType, name, projectAssetLoader);
    }

    @Override
    public List<DwWorkflow> convert(Asset asset) throws Exception {
        this.dagDataList = dolphinSchedulerPackage.getProcessDefinitions().values().stream().flatMap(List::stream).collect(Collectors.toList());
        if (dagDataList.isEmpty()) {
            throw new RuntimeException("process list empty");
        }
        log.info("dagDataList size: {}", CollectionUtils.size(dagDataList));
        findAllSubProcessDefinition(dagDataList);
        dwWorkflowList = convertProcessMetaListToDwWorkflowList(dagDataList);
        //processSubProcessDefinitionDepends();
        setProjectRootDependForNoInputNode(project, dwWorkflowList);
        convertDataSources(project);
        return dwWorkflowList;
    }

    private List<DwWorkflow> convertProcessMetaListToDwWorkflowList(List<DagData> dataList) {
        return ListUtils.emptyIfNull(dataList).stream()
                .map(this::convertProcessMetaToDwWorkflow)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<DwWorkflow> convertProcessMetaToDwWorkflow(DagData processMeta) {
        DolphinSchedulerConverterContext<com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project, DagData,
                DataSource, ResourceInfo, UdfFunc>
                converterContext = new DolphinSchedulerConverterContext<>();
        converterContext.setProject(project);
        converterContext.setProperties(properties);
        converterContext.setDolphinSchedulerPackage(dolphinSchedulerPackage);
        V2ProcessDefinitionConverter definitionConverter = new V2ProcessDefinitionConverter(converterContext, processMeta);
        return definitionConverter.convert();
    }

    private void convertDataSources(com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project project) {
        project.setDatasources(ListUtils.emptyIfNull(dolphinSchedulerPackage.getDatasources()).stream().map(ds -> {
            DwDatasource dwDatasource = new DwDatasource();
            dwDatasource.setName(ds.getName());
            dwDatasource.setType(StringUtils.lowerCase(ds.getType().name()));
            DbType dbType = DbType.valueOf(ds.getType().name());
            BaseDataSource baseDataSource = DataSourceFactory.getDatasource(ds.getType().name(), ds.getConnectionParams());
            Optional.ofNullable(baseDataSource).ifPresent(datasource -> {
                switch (dbType) {
                    case MYSQL:
                    case POSTGRESQL:
                    case ORACLE:
                    case H2:
                    case DB2:
                    case CLICKHOUSE:
                    case SQLSERVER:
                        setJdbcConnection(datasource, dwDatasource);
                        break;
                    case SPARK:
                    case HIVE:
                }
            });

            dwDatasource.setEnvType(EnvType.PRD.name());

            dwDatasource.setDescription(ds.getNote());
            return dwDatasource;
        }).collect(Collectors.toList()));
    }

    /**
     * 1. create virtual begin node (SubProcessParameterConverter)
     * 2. find root tasks of process definition of subprocess
     * 3. set task pre dependent to virtual start node
     */
    private void findAllSubProcessDefinition(List<DagData> dagDataList) {
        for (DagData dagData : ListUtils.emptyIfNull(dagDataList)) {
            ProcessDefinition processDefinition = dagData.getProcessDefinition();
            dagData.getTaskDefinitionList().stream()
                    .filter(task -> TaskType.SUB_PROCESS.name().equalsIgnoreCase(task.getTaskType()))
                    .forEach(task -> {
                                JsonObject jsonObject = GsonUtils.fromJsonString(task.getTaskParams(), JsonObject.class);
                                if (jsonObject.has("processDefinitionCode")) {
                                    Long processDefCode = jsonObject.get("processDefinitionCode").getAsLong();
                                    String out = getDefaultNodeOutput(processDefinition, task.getName());
                                    DolphinSchedulerV2Context.getContext().putSubProcessCodeOutMap(processDefCode, out + ".virtual.start");
                                }
                            }
                    );
        }
    }

    protected String getDefaultNodeOutput(ProcessDefinition processMeta, String taskName) {
        return Joiner.on(".").join(
                //dataworks project
                project.getName(),
                processMeta.getProjectName(),
                processMeta.getName(),
                taskName);
    }

    /**
     * SubProcess dependents handling logic
     */
    private void processSubProcessDefinitionDepends() {
        // for SubProcess Type
        ListUtils.emptyIfNull(dwWorkflowList).forEach(workflow -> ListUtils.emptyIfNull(workflow.getNodes()).stream()
                .filter(n -> StringUtils.equalsIgnoreCase(TaskType.SUB_PROCESS.name(), ((DwNode) n).getRawNodeType()))
                .forEach(subProcessNode -> {
                    processSubProcessDefinitionDependNode(subProcessNode, workflow);
                }));
    }

    private void processSubProcessDefinitionDependNode(Node subProcessNode, DwWorkflow workflow) {
        SubProcessParameters subProcessParameter = GsonUtils.fromJsonString(subProcessNode.getCode(), new TypeToken<SubProcessParameters>() {}.getType());
        if (subProcessParameter == null) {
            LOGGER.warn("subProcessParameter null, with node code {}", subProcessNode.getCode());
            return;
        }
        this.dagDataList.stream().map(dag -> dag.getProcessDefinition())
                .filter(processDef -> Objects.equals(processDef.getCode(), subProcessParameter.getProcessDefinitionCode()))
                .findFirst()
                .flatMap(processDef -> dwWorkflowList.stream()
                        .filter(wf -> StringUtils.equals(V2ProcessDefinitionConverter.toWorkflowName(processDef), wf.getName()))
                        .findFirst()
                        .map(wf -> addStartEndNodeToDependedWorkflow(subProcessNode, processDef, wf)))
                .filter(node -> node.getData() != null)
                .ifPresent(endNodeOut -> addSubProcess(subProcessNode, workflow, endNodeOut));
    }

    private void addSubProcess(Node subProcessNode, DwWorkflow workflow, DwNodeIo endNodeOut) {
        ListUtils.emptyIfNull(workflow.getNodes()).stream()
                // set children of sub process node depends on end node of depend workflow
                .filter(n -> ListUtils.emptyIfNull(n.getInputs()).stream()
                        .anyMatch(in -> ListUtils.emptyIfNull(subProcessNode.getOutputs())
                                .stream().anyMatch(out -> StringUtils.equalsIgnoreCase(out.getData(), in.getData()))))
                .forEach(child -> ListUtils.emptyIfNull(child.getInputs()).stream()
                        .filter(in -> ListUtils.emptyIfNull(subProcessNode.getOutputs()).stream().anyMatch(depOut ->
                                StringUtils.equalsIgnoreCase(in.getData(), depOut.getData())))
                        .forEach(in -> in.setData(endNodeOut.getData())));
    }

    /**
     * add start, end node for sub process workflow - set start as parent of all nodes that has no parents - set end as
     * child of all nodes that has no children - set start as child of sub process node
     *
     * @param subProcessNode
     * @param proDef
     * @param wf
     * @return output of end node
     */
    private DwNodeIo addStartEndNodeToDependedWorkflow(Node subProcessNode, ProcessDefinition proDef, DwWorkflow wf) {
        DwNode startNode = new DwNode();
        startNode.setDescription("node added by dataworks migration service");
        startNode.setRawNodeType(CodeProgramType.VIRTUAL.name());
        startNode.setDependentType(0);
        startNode.setCycleType(0);
        startNode.setNodeUseType(NodeUseType.SCHEDULED);
        startNode.setCronExpress("day");
        startNode.setName(Joiner.on("_").join("start", proDef.getProjectName(), proDef.getName()));
        startNode.setType(CodeProgramType.VIRTUAL.name());
        startNode.setWorkflowRef(wf);
        DwNodeIo startNodeOutput = new DwNodeIo();
        startNodeOutput.setData(Joiner.on(".")
                .join(project.getName(), proDef.getProjectName(), proDef.getName(), "start"));
        startNodeOutput.setParseType(1);
        startNode.setOutputs(Collections.singletonList(startNodeOutput));
        startNode.setInputs(new ArrayList<>());
        ListUtils.emptyIfNull(subProcessNode.getOutputs()).stream().findFirst().ifPresent(
                depOut -> startNode.getInputs().add(depOut));

        DwNode endNode = new DwNode();
        endNode.setDescription("node added by dataworks migration service");
        endNode.setRawNodeType(CodeProgramType.VIRTUAL.name());
        endNode.setDependentType(0);
        endNode.setCycleType(0);
        endNode.setNodeUseType(NodeUseType.SCHEDULED);
        endNode.setCronExpress("day");
        endNode.setName(Joiner.on("_").join("end", proDef.getProjectName(), proDef.getName()));
        endNode.setType(CodeProgramType.VIRTUAL.name());
        endNode.setWorkflowRef(wf);
        DwNodeIo endNodeOutput = new DwNodeIo();
        endNodeOutput.setData(
                Joiner.on(".").join(project.getName(), proDef.getProjectName(), proDef.getName(), "end"));
        endNodeOutput.setParseType(1);
        endNode.setOutputs(Collections.singletonList(endNodeOutput));
        endNode.setInputs(new ArrayList<>());

        ListUtils.emptyIfNull(wf.getNodes()).forEach(node -> {
            String prefix = Joiner.on(".").join(project.getName(), proDef.getProjectName());
            if (ListUtils.emptyIfNull(node.getInputs()).stream()
                    .noneMatch(in -> StringUtils.startsWithIgnoreCase(in.getData(), prefix))) {
                node.getInputs().add(startNodeOutput);
            }

            if (ListUtils.emptyIfNull(wf.getNodes()).stream()
                    .map(Node::getInputs)
                    .flatMap(List::stream)
                    .noneMatch(in -> ListUtils.emptyIfNull(node.getOutputs()).stream().anyMatch(out ->
                            StringUtils.equalsIgnoreCase(in.getData(), out.getData())))) {
                ListUtils.emptyIfNull(node.getOutputs()).stream().findFirst().ifPresent(
                        out -> endNode.getInputs().add(out));
            }
        });
        wf.getNodes().add(startNode);
        wf.getNodes().add(endNode);

        return endNodeOutput;
    }

    private void setJdbcConnection(BaseDataSource datasource, DwDatasource dwDatasource) {
        JdbcConnection conn = new JdbcConnection();
        conn.setUsername(datasource.getUser());
        conn.setPassword(datasource.getPassword());
        conn.setDatabase(datasource.getDatabase());
        conn.setJdbcUrl(datasource.getAddress());
        conn.setTag("public");
        dwDatasource.setConnection(GsonUtils.defaultGson.toJson(conn));
    }

    private List<Node> convertDagDataTasks(DwWorkflow dwWorkflow, DagData dagData, List<DagData> dagDataList) {
        return ListUtils.emptyIfNull(dagData.getTaskDefinitionList()).stream()
                .map(taskDefinition -> convertDagDataTaskDefinitionToDwNode(dwWorkflow, taskDefinition, dagData,
                        dagDataList))
                .collect(Collectors.toList());
    }

    private DwNode convertDagDataTaskDefinitionToDwNode(DwWorkflow dwWorkflow, TaskDefinition taskDefinition,
            DagData dagData, List<DagData> dagDataList) {
        DwNode dwNode = new DwNode();
        dwNode.setName(taskDefinition.getName());
        dwNode.setWorkflowRef(dwWorkflow);
        dwNode.setType(convertDagDataTaskDefinitionType(taskDefinition, dagData));
        return dwNode;
    }

    private String convertDagDataTaskDefinitionType(TaskDefinition taskDefinition, DagData dagData) {
        taskDefinition.getTaskParams();
        return null;
    }

    private List<DagData> readDagDataList(File rootDir) {
        if (!rootDir.exists()) {
            log.info("root directory not exits: {}", rootDir);
            return ListUtils.emptyIfNull(null);
        }

        return Optional.ofNullable(rootDir.listFiles(f -> f.isFile() && f.getName().endsWith(".json")))
                .map(Arrays::asList)
                .map(files -> files.stream().map(this::readDagDataJson).collect(Collectors.toList()))
                .orElse(ListUtils.emptyIfNull(null));
    }

    private DagData readDagDataJson(File jsonFile) {
        try {
            String json = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
            return GsonUtils.fromJsonString(json, new TypeToken<DagData>() {}.getType());
        } catch (IOException e) {
            log.error("read json file: {} error: ", jsonFile, e);
            throw new RuntimeException(e);
        }
    }
}
