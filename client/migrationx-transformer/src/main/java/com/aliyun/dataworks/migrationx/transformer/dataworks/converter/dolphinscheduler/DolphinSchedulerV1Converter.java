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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DbType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerVersion;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwDatasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.connection.JdbcConnection;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.tenant.EnvType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Datasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.service.DolphinSchedulerPackageLoader;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.ProcessMeta;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.TaskType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.datasource.BaseDataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.datasource.DataSourceFactory;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.subprocess.SubProcessParameters;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.AbstractBaseConverter;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.google.common.base.Joiner;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * for dolphinscheduler common conversion logic convert dolphinscheduler process to dataworks model
 *
 * @author 聿剑
 * @date 2022/10/12
 */
@Slf4j
public class DolphinSchedulerV1Converter extends AbstractBaseConverter {
    protected DolphinSchedulerVersion version;
    protected com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project project;
    protected Properties properties = new Properties();
    protected List<ProcessMeta> processMetaList = new ArrayList<>();
    protected List<DwWorkflow> dwWorkflowList = new ArrayList<>();
    protected Map<String, CodeProgramType> nodeTypeMap = new HashMap<>();
    private DolphinSchedulerPackage<Project, ProcessMeta,
        Datasource, ResourceInfo, UdfFunc>
        dolphinSchedulerPackage;

    public DolphinSchedulerV1Converter() {
        super(AssetType.DOLPHINSCHEDULER, DolphinSchedulerV1Converter.class.getSimpleName());
    }

    public DolphinSchedulerV1Converter setProject(com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project project) {
        this.project = project;
        return this;
    }

    public DolphinSchedulerV1Converter setProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public List<DwWorkflow> convert(Asset asset) throws Exception {
        File rootDir = asset.getPath();
        if (!rootDir.exists()) {
            throw new FileNotFoundException(rootDir.getAbsolutePath());
        }

        DolphinSchedulerPackageLoader<Project, ProcessMeta,
            Datasource, ResourceInfo, UdfFunc> loader = DolphinSchedulerPackageLoader.create(rootDir);
        loader.loadPackage();
        dolphinSchedulerPackage = loader.getDolphinSchedulerPackage();
        processMetaList = dolphinSchedulerPackage.getProcessDefinitions().values()
            .stream().flatMap(List::stream).collect(Collectors.toList());
        dwWorkflowList = convertProcessMetaListToDwWorkflowList(processMetaList);
        processSubProcessDefinitionDepends();
        setProjectRootDependForNoInputNode(project, dwWorkflowList);
        convertDatasources(project);
        return dwWorkflowList;
    }

    private void convertDatasources(com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project project) {
        project.setDatasources(ListUtils.emptyIfNull(dolphinSchedulerPackage.getDatasources()).stream().map(ds -> {
            DwDatasource dwDatasource = new DwDatasource();
            dwDatasource.setName(ds.getName());
            dwDatasource.setType(StringUtils.lowerCase(ds.getType()));
            DbType dbType = DbType.valueOf(ds.getType());
            BaseDataSource baseDataSource = DataSourceFactory.getDatasource(DbType.valueOf(ds.getType()),
                ds.getConnectionParams());
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

    private void setJdbcConnection(BaseDataSource datasource, DwDatasource dwDatasource) {
        JdbcConnection conn = new JdbcConnection();
        conn.setUsername(datasource.getUser());
        conn.setPassword(datasource.getPassword());
        conn.setDatabase(datasource.getDatabase());
        conn.setJdbcUrl(datasource.getAddress());
        conn.setTag("public");
        dwDatasource.setConnection(GsonUtils.defaultGson.toJson(conn));
    }

    /**
     * SubProcess dependents handling logic
     */
    private void processSubProcessDefinitionDepends() {
        // for SubProcess Type
        ListUtils.emptyIfNull(dwWorkflowList).forEach(workflow -> ListUtils.emptyIfNull(workflow.getNodes()).stream()
            .filter(n -> StringUtils.equalsIgnoreCase(TaskType.SUB_PROCESS.name(), ((DwNode)n).getRawNodeType()))
            .forEach(subProcessNode -> {
                SubProcessParameters subProcessParameter =
                    GsonUtils.fromJsonString(subProcessNode.getCode(),
                        new TypeToken<SubProcessParameters>() {}.getType());
                dolphinSchedulerPackage.getProcessDefinitions().values().stream().map(defList ->
                        ListUtils.emptyIfNull(defList).stream()
                            .filter(df -> subProcessParameter != null)
                            .filter(df -> Objects.equals(df.getProcessDefinitionId(),
                                subProcessParameter.getProcessDefinitionId()))
                            .findFirst()
                            .flatMap(proDef -> dwWorkflowList.stream()
                                .filter(wf -> StringUtils.equals(V1ProcessDefinitionConverter.toWorkflowName(proDef),
                                    wf.getName()))
                                .findFirst()
                                .map(wf -> addStartEndNodeToDependedWorkflow(subProcessNode, proDef, wf)))
                            .orElse(new DwNodeIo()))
                    .filter(io -> StringUtils.isNotBlank(io.getData()))
                    .findFirst()
                    .ifPresent(endNodeOut -> ListUtils.emptyIfNull(workflow.getNodes()).stream()
                        // set children of sub process node depends on end node of depend workflow
                        .filter(n -> ListUtils.emptyIfNull(n.getInputs()).stream().anyMatch(in ->
                            ListUtils.emptyIfNull(subProcessNode.getOutputs()).stream().anyMatch(out ->
                                StringUtils.equalsIgnoreCase(out.getData(), in.getData()))))
                        .forEach(child -> ListUtils.emptyIfNull(child.getInputs()).stream()
                            .filter(in -> ListUtils.emptyIfNull(subProcessNode.getOutputs()).stream().anyMatch(depOut ->
                                StringUtils.equalsIgnoreCase(in.getData(), depOut.getData())))
                            .forEach(in -> in.setData(endNodeOut.getData()))));
            }));
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
    private DwNodeIo addStartEndNodeToDependedWorkflow(Node subProcessNode, ProcessMeta proDef, DwWorkflow wf) {
        DwNode startNode = new DwNode();
        startNode.setDescription("node added by dataworks migration service");
        startNode.setRawNodeType(CodeProgramType.VIRTUAL.name());
        startNode.setDependentType(0);
        startNode.setCycleType(0);
        startNode.setNodeUseType(NodeUseType.SCHEDULED);
        startNode.setCronExpress("day");
        startNode.setName(Joiner.on("_").join("start", proDef.getProjectName(), proDef.getProcessDefinitionName()));
        startNode.setType(CodeProgramType.VIRTUAL.name());
        startNode.setWorkflowRef(wf);
        DwNodeIo startNodeOutput = new DwNodeIo();
        startNodeOutput.setData(Joiner.on(".")
            .join(project.getName(), proDef.getProjectName(), proDef.getProcessDefinitionName(), "start"));
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
        endNode.setName(Joiner.on("_").join("end", proDef.getProjectName(), proDef.getProcessDefinitionName()));
        endNode.setType(CodeProgramType.VIRTUAL.name());
        endNode.setWorkflowRef(wf);
        DwNodeIo endNodeOutput = new DwNodeIo();
        endNodeOutput.setData(
            Joiner.on(".").join(project.getName(), proDef.getProjectName(), proDef.getProcessDefinitionName(), "end"));
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

    protected Map<String, CodeProgramType> getNodeTypeMap() {
        String map = properties.getProperty(Constants.CONVERTER_DOLPHINSCHEDULER_TO_DATAWORKS_NODE_TYPE_MAP, "{}");
        Map<String, CodeProgramType> mapping = GsonUtils.fromJsonString(map,
            new TypeToken<Map<String, CodeProgramType>>() {}.getType());
        return MapUtils.emptyIfNull(mapping);
    }

    private List<DwWorkflow> convertProcessMetaListToDwWorkflowList(List<ProcessMeta> processMetaList) {
        return ListUtils.emptyIfNull(processMetaList).stream()
            .map(this::convertProcessMetaToDwWorkflow)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private List<DwWorkflow> convertProcessMetaToDwWorkflow(ProcessMeta processMeta) {
        DolphinSchedulerConverterContext<Project, ProcessMeta,
            Datasource, ResourceInfo, UdfFunc>
            converterContext = new DolphinSchedulerConverterContext<>();
        converterContext.setProject(project);
        converterContext.setProperties(properties);
        converterContext.setDolphinSchedulerPackage(dolphinSchedulerPackage);
        V1ProcessDefinitionConverter converter = new V1ProcessDefinitionConverter(converterContext, processMeta);
        converter.convert();
        return converter.getWorkflowList();
    }
}
