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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.v3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.utils.ReflectUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.TaskType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.conditions.ConditionResult;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DolphinSchedulerV3Context;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessTaskRelation;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.DbType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.conditions.ConditionsParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.datax.DataxParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.dependent.DependentParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.flink.FlinkParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.hivecli.HiveCliParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.http.HttpParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.mr.MapReduceParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.parameters.AbstractParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.procedure.ProcedureParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.python.PythonParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.shell.ShellParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.spark.SparkParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.sql.SqlParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.sqoop.SqoopParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.subprocess.SubProcessParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.switchs.SwitchParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.RerunMode;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.AbstractBaseConverter;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.utils.ConverterTypeUtils;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.v1.ConditionsParameterConverter;
import com.aliyun.migrationx.common.context.TransformerContext;
import com.aliyun.migrationx.common.metrics.DolphinMetrics;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.common.base.Joiner;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractParameterConverter<Parameter extends AbstractParameters> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBaseConverter.class);

    protected final DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo, UdfFunc>
            converterContext;
    protected final DwWorkflow dwWorkflow;
    protected final DagData dagData;
    protected final ProcessDefinition processMeta;
    protected final List<ProcessTaskRelation> processTaskRelationList;
    protected final TaskDefinition taskDefinition;
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
        taskTypeClassMap.put(TaskType.SWITCH, SwitchParameters.class);
        taskTypeClassMap.put(TaskType.HIVECLI, HiveCliParameters.class);
    }

    public AbstractParameterConverter(DagData dagData, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo, UdfFunc> converterContext) {
        this.converterContext = converterContext;
        this.dagData = dagData;
        this.processMeta = dagData.getProcessDefinition();
        this.processTaskRelationList = dagData.getProcessTaskRelationList();
        this.dwWorkflow = converterContext.getDwWorkflow();
        this.taskDefinition = taskDefinition;
        this.workflowList.add(dwWorkflow);
        this.properties = converterContext.getProperties();
    }

    public List<DwNode> convert() throws IOException {
        try {
            List<DwNode> dwNodes = doConvert();
            markSuccessProcess(dwNodes);
            return dwNodes;
        } catch (Throwable e) {
            markFailedProcess(e.getMessage());
            throw e;
        }
    }

    protected List<DwNode> doConvert() throws IOException {
        if (dwWorkflow.getNodes() == null) {
            dwWorkflow.setNodes(new ArrayList<>());
        }

        if (dwWorkflow.getResources() == null) {
            dwWorkflow.setResources(new ArrayList<>());
        }

        if (dwWorkflow.getFunctions() == null) {
            dwWorkflow.setFunctions(new ArrayList<>());
        }

        TaskType taskType = TaskType.of(taskDefinition.getTaskType());

        LOGGER.info("converting parameter of task: {}, type: {}", taskDefinition.getName(), taskType);
        if (!(this instanceof CustomParameterConverter)) {
            try {
                parameter = GsonUtils.fromJsonString(
                        taskDefinition.getTaskParams(), TypeToken.get(taskTypeClassMap.get(taskType)).getType());
            } catch (Exception ex) {
                LOGGER.error("parse task {}, {}, parameter {} error: ", taskType, taskTypeClassMap.get(taskType), ex);
            }
        }
        List<DwNode> nodes = convertParameter();
        LOGGER.info("convert task: {}, type: {} done.", taskDefinition.getName(), taskType);
        return nodes;
    }

    protected void markSuccessProcess(List<DwNode> nodes) {
        for (DwNode node : nodes) {
            DolphinMetrics metrics = DolphinMetrics.builder()
                    .projectName(taskDefinition.getProjectName())
                    .projectCode(taskDefinition.getProjectCode())
                    .processName(processMeta.getName())
                    .processCode(processMeta.getCode())
                    .taskName(taskDefinition.getName())
                    .taskCode(taskDefinition.getCode())
                    .taskType(taskDefinition.getTaskType())
                    .build();
            metrics.setWorkflowName(dwWorkflow.getName());
            metrics.setDwName(node.getName());
            metrics.setDwType(node.getType());
            TransformerContext.getCollector().markSuccessMiddleProcess(metrics);
        }
    }

    protected void markFailedProcess(String errorMsg) {
        DolphinMetrics metrics = DolphinMetrics.builder()
                .projectName(taskDefinition.getProjectName())
                .projectCode(taskDefinition.getProjectCode())
                .processName(processMeta.getName())
                .processCode(processMeta.getCode())
                .taskName(taskDefinition.getName())
                .taskCode(taskDefinition.getCode())
                .taskType(taskDefinition.getTaskType())
                .build();
        metrics.setWorkflowName(dwWorkflow.getName());
        metrics.setErrorMsg(errorMsg);
        TransformerContext.getCollector().markFailedMiddleProcess(metrics);
    }

    protected abstract List<DwNode> convertParameter() throws IOException;

    protected String getDefaultNodeOutput(ProcessDefinition processMeta, String taskName) {
        return Joiner.on(".").join(
                converterContext.getProject().getName(),
                processMeta.getProjectName(),
                processMeta.getName(),
                taskName);
    }

    protected DwNode newDwNode(TaskDefinition taskDefinition) {
        DwNode dwNode = new DwNode();
        dwNode.setWorkflowRef(dwWorkflow);
        dwWorkflow.getNodes().add(dwNode);
        dwNode.setName(com.aliyun.dataworks.migrationx.domain.dataworks.utils.StringUtils.toValidName(taskDefinition.getName()));
        dwNode.setDescription(taskDefinition.getDescription());
        if (parameter != null) {
            dwNode.setRawNodeType(Optional.ofNullable(ReflectUtils.getFieldValue(parameter, "type"))
                    .filter(type -> type instanceof DbType)
                    .map(type -> Joiner.on(".").join(taskDefinition.getTaskType(), type))
                    .orElse(taskDefinition.getTaskType()));
        } else {
            dwNode.setRawNodeType(taskDefinition.getTaskType());
        }

        dwNode.setDependentType(0);
        dwNode.setCycleType(0);
        dwNode.setNodeUseType(NodeUseType.SCHEDULED);
        if (taskDefinition.getFailRetryTimes() > 0) {
            dwNode.setRerunMode(RerunMode.ALL_ALLOWED);
        } else {
            dwNode.setRerunMode(RerunMode.FAILURE_ALLOWED);
        }
        dwNode.setTaskRerunTime(taskDefinition.getFailRetryTimes());
        dwNode.setTaskRerunInterval(taskDefinition.getFailRetryInterval() * 1000 * 60);

        // outputs
        setOutputs(dwNode);
        if (parameter != null) {
            dwNode.setParameter(Joiner.on(" ").join(ListUtils.emptyIfNull(parameter.getLocalParams()).stream()
                    .map(property -> property.getProp() + "=" + property.getValue())
                    .collect(Collectors.toList())));
        }
        //parameters

        Set<TaskDefinition> preTasks = listPreTasks();
        //inputs
        //pre tasks of current task
        setInputs(dwNode, preTasks);
        //if subprocess, set subprocess task input to virtual node
        setSubProcessInputs(dwNode);
        // 本节点的条件结果执行
        /**
         * 如果依赖Conditions节点，则增加依赖 节点名_join_success和节点名_join_failure个输入
         * @see ConditionsParameterConverter
         */
        SetUtils.emptyIfNull(preTasks).stream()
                .filter(preTask -> TaskType.CONDITIONS.name().equals(preTask.getTaskType()))

                .forEach(preTask -> {
                            ConditionResult conditionResult = GsonUtils.fromJsonString(taskDefinition.getTaskParams(), ConditionResult.class);

                            Optional.ofNullable(conditionResult.getSuccessNode())
                                    .filter(successNode -> ListUtils.emptyIfNull(successNode).stream()
                                            .anyMatch(n -> n == taskDefinition.getCode()))
                                    .ifPresent(successNode -> {
                                        String successInput = getDefaultNodeOutput(processMeta, Joiner.on("_").join(
                                                preTask.getName(), "join", "success"));
                                        ListUtils.emptyIfNull(dwNode.getInputs()).stream()
                                                .filter(in -> StringUtils.equals(
                                                        getDefaultNodeOutput(processMeta, preTask.getName()), in.getData()))
                                                .findFirst().ifPresent(in -> in.setData(successInput));
                                    });
                            Optional.ofNullable(conditionResult.getFailedNode())
                                    .filter(failureNode -> ListUtils.emptyIfNull(failureNode).stream()
                                            .anyMatch(n -> n == taskDefinition.getCode()))
                                    .ifPresent(failureNode -> {
                                        String failureInput = getDefaultNodeOutput(processMeta, Joiner.on("_").join(
                                                preTask.getName(), "join", "failure"));
                                        ListUtils.emptyIfNull(dwNode.getInputs()).stream()
                                                .filter(in -> StringUtils.equals(
                                                        getDefaultNodeOutput(processMeta, preTask.getName()), in.getData()))
                                                .findFirst().ifPresent(in -> in.setData(failureInput));
                                    });
                        }

                );

        dwNode.setResourceGroup(properties.getProperty(Constants.CONVERTER_TARGET_SCHEDULE_RES_GROUP_IDENTIFIER, null));
        return dwNode;
    }

    protected void setOutputs(DwNode dwNode) {
        DwNodeIo output = new DwNodeIo();
        output.setData(getDefaultNodeOutput(processMeta, taskDefinition.getName()));
        output.setParseType(1);
        output.setNodeRef(dwNode);
        dwNode.setOutputs(new ArrayList<>(Collections.singletonList(output)));
    }

    protected void setInputs(DwNode dwNode, Set<TaskDefinition> preTasks) {
        List<NodeIo> inputIo = SetUtils.emptyIfNull(preTasks).stream().map(upTask -> {
            DwNodeIo input = new DwNodeIo();
            input.setParseType(1);
            input.setNodeRef(dwNode);
            input.setData(Joiner.on(".").join(
                    converterContext.getProject().getName(),
                    processMeta.getProjectName(),
                    processMeta.getName(),
                    upTask.getName()));
            return input;
        }).collect(Collectors.toList());
        dwNode.getInputs().addAll(inputIo);
    }

    protected Set<TaskDefinition> listPreTasks() {
        List<ProcessTaskRelation> relations = dagData.getProcessTaskRelationList();
        Set<Long> preCodes = relations.stream().filter(r -> r.getPostTaskCode() == taskDefinition.getCode())
                .map(ProcessTaskRelation::getPreTaskCode).collect(Collectors.toSet());
        return dagData.getTaskDefinitionList().stream().filter(task -> preCodes.contains(task.getCode())).collect(Collectors.toSet());
    }

    private Set<TaskDefinition> listPostTasks() {
        List<ProcessTaskRelation> relations = dagData.getProcessTaskRelationList();
        Set<Long> postCodes = relations.stream().filter(r -> r.getPreTaskCode() == taskDefinition.getCode())
                .map(ProcessTaskRelation::getPostTaskCode).collect(Collectors.toSet());
        return dagData.getTaskDefinitionList().stream().filter(task -> postCodes.contains(task.getCode())).collect(Collectors.toSet());
    }

    /**
     * postTaskCode == task_code && preTaskCode == 0
     * 0: taskCode
     */
    private boolean isRootNode(long processCode, long taskCode) {
        return DolphinSchedulerV3Context.getContext().getDagDatas().stream()
                .filter(dag -> dag.getProcessDefinition().getCode() == processCode)
                .map(dag -> dag.getProcessTaskRelationList())
                .flatMap(List::stream)
                .filter(task -> task.getPostTaskCode() == taskCode)
                .filter(task -> task.getPreTaskCode() == 0L)
                .findAny()
                .isPresent();
    }

    private void setSubProcessInputs(DwNode dwNode) {
        if (!isRootNode(processMeta.getCode(), taskDefinition.getCode())) {
            return;
        }
        List<String> outs = DolphinSchedulerV3Context.getContext().getSubProcessCodeMap(processMeta.getCode());
        for (String virtualOut : CollectionUtils.emptyIfNull(outs)) {
            DwNodeIo input = new DwNodeIo();
            input.setData(virtualOut);
            input.setParseType(1);
            input.setNodeRef(dwNode);
            if (dwNode.getInputs() == null) {
                dwNode.setInputs(new ArrayList<>());
            }
            dwNode.getInputs().add(input);
        }
    }

    public List<DwWorkflow> getWorkflowList() {
        return workflowList;
    }

    protected String getConverterType(String convertType, String defaultConvertType) {
        String projectName = processMeta.getProjectName();
        String processName = processMeta.getName();
        String taskName = taskDefinition.getName();
        return ConverterTypeUtils.getConverterType(convertType, projectName, processName, taskName, defaultConvertType);
    }
}
