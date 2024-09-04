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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.v1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.utils.ReflectUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.ProcessData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.ProcessMeta;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.TaskNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.TaskNodeConnect;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.datasource.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.entity.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.enums.DbType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.enums.TaskType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.AbstractParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.conditions.ConditionsParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.datax.DataxParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.dependent.DependentParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.flink.FlinkParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.http.HttpParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.mr.MapReduceParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.procedure.ProcedureParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.python.PythonParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.shell.ShellParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.spark.SparkParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.sql.SqlParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.sqoop.SqoopParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.subprocess.SubProcessParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.RerunMode;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.utils.ConverterTypeUtils;
import com.aliyun.migrationx.common.context.TransformerContext;
import com.aliyun.migrationx.common.metrics.DolphinMetrics;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.common.base.Joiner;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2022/10/18
 */
@Slf4j
public abstract class AbstractParameterConverter<Parameter extends AbstractParameters> {
    protected final DolphinSchedulerConverterContext<Project, ProcessMeta, DataSource, ResourceInfo, UdfFunc>
            converterContext;
    protected final DwWorkflow dwWorkflow;
    protected final ProcessMeta processMeta;
    protected final ProcessData processData;
    protected final TaskNode taskDefinition;
    protected final Properties properties;

    protected Parameter parameter;
    protected List<DwWorkflow> workflowList = new ArrayList<>();
    protected static Map<TaskType, Class<? extends AbstractParameters>> taskTypeClassMap;

    static {
        taskTypeClassMap = new HashMap<>();
        taskTypeClassMap.put(TaskType.SQL, SqlParameters.class);
        taskTypeClassMap.put(TaskType.DEPENDENT, DependentParameters.class);
        taskTypeClassMap.put(TaskType.FLINK, FlinkParameters.class);
        taskTypeClassMap.put(TaskType.SPARK, SparkParameters.class);
        taskTypeClassMap.put(TaskType.DATAX, DataxParameters.class);
        taskTypeClassMap.put(TaskType.SHELL, ShellParameters.class);
        taskTypeClassMap.put(TaskType.HTTP, HttpParameters.class);
        taskTypeClassMap.put(TaskType.PROCEDURE, ProcedureParameters.class);
        taskTypeClassMap.put(TaskType.CONDITIONS, ConditionsParameters.class);
        taskTypeClassMap.put(TaskType.SQOOP, SqoopParameters.class);
        taskTypeClassMap.put(TaskType.SUB_PROCESS, SubProcessParameters.class);
        taskTypeClassMap.put(TaskType.PYTHON, PythonParameters.class);
        taskTypeClassMap.put(TaskType.MR, MapReduceParameters.class);
    }

    public AbstractParameterConverter(ProcessMeta processMeta, TaskNode taskDefinition,
            DolphinSchedulerConverterContext<Project, ProcessMeta, DataSource, ResourceInfo
                    , UdfFunc> converterContext) {
        this.converterContext = converterContext;
        this.processMeta = processMeta;
        this.processData = processMeta.getProcessDefinitionJson();
        this.dwWorkflow = converterContext.getDwWorkflow();
        this.taskDefinition = taskDefinition;
        this.workflowList.add(dwWorkflow);
        this.properties = converterContext.getProperties();
    }

    public void convert() throws IOException {
        try {
            List<DwNode> dwNodes = doConvert();
            markSuccessProcess(dwNodes);
        } catch (Throwable e) {
            markFailedProcess(e.getMessage());
            throw e;
        }
    }

    private List<DwNode> doConvert() throws IOException {
        if (dwWorkflow.getNodes() == null) {
            dwWorkflow.setNodes(new ArrayList<>());
        }

        if (dwWorkflow.getResources() == null) {
            dwWorkflow.setResources(new ArrayList<>());
        }

        if (dwWorkflow.getFunctions() == null) {
            dwWorkflow.setFunctions(new ArrayList<>());
        }

        TaskType taskType = taskDefinition.getType();

        log.info("converting parameter of task: {}, type: {}", taskDefinition.getName(), taskType);

        try {
            parameter = GsonUtils.fromJsonString(
                    taskDefinition.getParams(), TypeToken.get(taskTypeClassMap.get(taskType)).getType());
        } catch (Exception ex) {
            log.error("parse task {}, {}, parameter {} error: ", taskType, taskTypeClassMap.get(taskType), ex);
        }

        List<DwNode> dwNodes = convertParameter();

        log.info("convert task: {}, type: {} done.", taskDefinition.getName(), taskType);
        return dwNodes;
    }

    protected abstract List<DwNode> convertParameter() throws IOException;

    protected String getDefaultNodeOutput(ProcessMeta processMeta, String taskName) {
        return Joiner.on(".").join(
                converterContext.getProject().getName(),
                processMeta.getProjectName(),
                processMeta.getProcessDefinitionName(),
                taskName);
    }

    protected DwNode newDwNode(ProcessMeta processMeta, TaskNode taskDefinition) {
        DwNode dwNode = new DwNode();
        dwNode.setWorkflowRef(dwWorkflow);
        dwWorkflow.getNodes().add(dwNode);
        dwNode.setName(com.aliyun.dataworks.migrationx.domain.dataworks.utils.StringUtils.toValidName(taskDefinition.getName()));
        dwNode.setDescription(taskDefinition.getDesc());
        dwNode.setRawNodeType(Optional.ofNullable(ReflectUtils.getFieldValue(parameter, "type"))
                .filter(type -> type instanceof DbType)
                .map(type -> Joiner.on(".").join(taskDefinition.getType(), type))
                .orElse(taskDefinition.getType().name()));
        dwNode.setDependentType(0);
        dwNode.setCycleType(0);
        dwNode.setNodeUseType(NodeUseType.SCHEDULED);
        if (taskDefinition.getMaxRetryTimes() > 0) {
            dwNode.setRerunMode(RerunMode.ALL_ALLOWED);
        } else {
            dwNode.setRerunMode(RerunMode.FAILURE_ALLOWED);
        }
        dwNode.setTaskRerunTime(taskDefinition.getMaxRetryTimes());
        dwNode.setTaskRerunInterval(taskDefinition.getRetryInterval() * 1000 * 60);

        // outputs
        DwNodeIo output = new DwNodeIo();
        output.setData(getDefaultNodeOutput(processMeta, taskDefinition.getName()));

        output.setParseType(1);
        output.setNodeRef(dwNode);
        dwNode.setOutputs(new ArrayList<>(Collections.singletonList(output)));
        dwNode.setParameter(Joiner.on(" ").join(ListUtils.emptyIfNull(parameter.getLocalParams()).stream()
                .map(property -> property.getProp() + "=" + property.getValue())
                .collect(Collectors.toList())));

        // inputs
        List<TaskNodeConnect> connects = processMeta.getProcessDefinitionConnects();

        dwNode.setInputs(ListUtils.emptyIfNull(connects).stream()
                .filter(connect -> StringUtils.equals(connect.getEndPointTargetId(), taskDefinition.getId()))
                .map(connect -> {
                    List<TaskNode> upstreamTask = Optional.ofNullable(processData)
                            .map(ProcessData::getTasks)
                            .map(tasks -> ListUtils.emptyIfNull(tasks).stream()
                                    .filter(task -> StringUtils.equals(task.getId(), connect.getEndPointSourceId()))
                                    .collect(Collectors.toList()))
                            .orElse(null);

                    return ListUtils.emptyIfNull(upstreamTask).stream().map(upTask -> {
                        DwNodeIo input = new DwNodeIo();
                        input.setParseType(1);
                        input.setNodeRef(dwNode);
                        input.setData(Joiner.on(".").join(
                                converterContext.getProject().getName(),
                                processMeta.getProjectName(),
                                processMeta.getProcessDefinitionName(),
                                upTask.getName()));
                        return input;
                    }).collect(Collectors.toList());
                }).flatMap(List::stream).collect(Collectors.toList()));

        // 本节点的条件结果执行
        List<String> preTasks = GsonUtils.fromJsonString(taskDefinition.getPreTasks(),
                new TypeToken<List<String>>() {}.getType());
        ListUtils.emptyIfNull(processMeta.getProcessDefinitionJson().getTasks()).stream()
                .filter(taskNode -> ListUtils.emptyIfNull(preTasks).stream()
                        .anyMatch(preTask -> StringUtils.equals(preTask, taskNode.getName())))
                .filter(preTaskNode -> TaskType.CONDITIONS.equals(preTaskNode.getType()))
                .forEach(preConditionTaskNode -> {
                    /**
                     * 如果依赖Conditions节点，则增加依赖 节点名_join_success和节点名_join_failure个输入
                     * @see ConditionsParameterConverter
                     */
                    ConditionsParameters conditionResult = preConditionTaskNode.getConditionResult();
                    log.info("condition result: {}", GsonUtils.toJsonString(conditionResult));
                    Optional.ofNullable(conditionResult).map(ConditionsParameters::getSuccessNode)
                            .filter(successNode -> ListUtils.emptyIfNull(successNode).stream()
                                    .anyMatch(n -> StringUtils.equals(n, taskDefinition.getName())))
                            .ifPresent(successNode -> {
                                String successInput = getDefaultNodeOutput(processMeta, Joiner.on("_").join(
                                        preConditionTaskNode.getName(), "join", "success"));
                                ListUtils.emptyIfNull(dwNode.getInputs()).stream()
                                        .filter(in -> StringUtils.equals(
                                                getDefaultNodeOutput(processMeta, preConditionTaskNode.getName()), in.getData()))
                                        .findFirst().ifPresent(in -> in.setData(successInput));
                            });
                    Optional.ofNullable(conditionResult).map(ConditionsParameters::getFailedNode)
                            .filter(failureNode -> ListUtils.emptyIfNull(failureNode).stream()
                                    .anyMatch(n -> StringUtils.equals(n, taskDefinition.getName())))
                            .ifPresent(failureNode -> {
                                String failureInput = getDefaultNodeOutput(processMeta, Joiner.on("_").join(
                                        preConditionTaskNode.getName(), "join", "failure"));
                                ListUtils.emptyIfNull(dwNode.getInputs()).stream()
                                        .filter(in -> StringUtils.equals(
                                                getDefaultNodeOutput(processMeta, preConditionTaskNode.getName()), in.getData()))
                                        .findFirst().ifPresent(in -> in.setData(failureInput));
                            });
                });
        return dwNode;
    }

    public List<DwWorkflow> getWorkflowList() {
        return workflowList;
    }

    protected void markSuccessProcess(List<DwNode> nodes) {
        for (DwNode node : nodes) {
            DolphinMetrics metrics = DolphinMetrics.builder()
                    .projectName(processMeta.getProjectName())
                    .processName(processMeta.getProcessDefinitionName())
                    .taskName(taskDefinition.getName())
                    .taskType(taskDefinition.getType().name())
                    .build();
            metrics.setWorkflowName(dwWorkflow.getName());
            metrics.setDwName(node.getName());
            metrics.setDwType(node.getType());
            TransformerContext.getCollector().markSuccessMiddleProcess(metrics);
        }
    }

    protected void markFailedProcess(String errorMsg) {
        DolphinMetrics metrics = DolphinMetrics.builder()
                .projectName(processMeta.getProjectName())
                .processName(processMeta.getProcessDefinitionName())
                .taskName(taskDefinition.getName())
                .taskType(taskDefinition.getType().name())
                .build();
        metrics.setWorkflowName(dwWorkflow.getName());
        metrics.setErrorMsg(errorMsg);
        TransformerContext.getCollector().markFailedMiddleProcess(metrics);
    }

    protected String getConverterType(String convertType, String defaultConvertType) {
        String projectName = processMeta.getProjectName();
        String processName = processMeta.getProcessDefinitionName();
        String taskName = taskDefinition.getName();
        return ConverterTypeUtils.getConverterType(convertType, projectName, processName, taskName, defaultConvertType);
    }
}
