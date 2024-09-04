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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.TaskType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.Schedule;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.parameters.AbstractParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.subprocess.SubProcessParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.WorkflowParameter;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowVersion;
import com.aliyun.dataworks.migrationx.transformer.core.checkpoint.CheckPoint;
import com.aliyun.dataworks.migrationx.transformer.core.checkpoint.StoreWriter;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.filters.DolphinSchedulerConverterFilter;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.v1.ProcessDefinitionConverter;
import com.aliyun.migrationx.common.context.TransformerContext;
import com.aliyun.migrationx.common.exception.UnSupportedTypeException;
import com.aliyun.migrationx.common.metrics.DolphinMetrics;
import com.aliyun.migrationx.common.utils.Config;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.common.base.Joiner;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2022/10/19
 */
@Slf4j
public class V3ProcessDefinitionConverter
        extends ProcessDefinitionConverter<Project, DagData, DataSource, ResourceInfo, UdfFunc> {
    private List<DwWorkflow> dwWorkflowList = new ArrayList<>();

    private ProcessDefinition processDefinition;
    private DagData dagData;

    private List<TaskDefinition> taskDefinitionList;

    private DolphinSchedulerConverterFilter filter;

    public V3ProcessDefinitionConverter(
            DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo, UdfFunc> converterContext,
            DagData dagData) {
        super(converterContext, dagData);
        this.dagData = dagData;
        this.processDefinition = dagData.getProcessDefinition();
        this.taskDefinitionList = dagData.getTaskDefinitionList();
        this.filter = new DolphinSchedulerConverterFilter();
    }

    public static String toWorkflowName(ProcessDefinition processDefinition) {
        return com.aliyun.dataworks.migrationx.domain.dataworks.utils.StringUtils.toValidName(Joiner.on("_").join(
                processDefinition.getProjectName(), processDefinition.getName()));
    }

    @Override
    public List<DwWorkflow> convert() {
        DwWorkflow dwWorkflow = new DwWorkflow();
        dwWorkflow.setName(toWorkflowName(processDefinition));
        dwWorkflow.setType(WorkflowType.BUSINESS);
        dwWorkflow.setScheduled(true);
        dwWorkflow.setVersion(WorkflowVersion.V3);

        converterContext.setDwWorkflow(dwWorkflow);
        dwWorkflow.setParameters(getGlobalParams());
        CheckPoint checkPoint = CheckPoint.getInstance();
        String projectName = processDefinition.getProjectName();
        String processName = processDefinition.getName();

        Map<String, List<DwWorkflow>> loadedTasks = checkPoint.loadFromCheckPoint(projectName, processName);

        final Function<StoreWriter, List<DwWorkflow>> processFunc = (StoreWriter writer) ->
                ListUtils.emptyIfNull(taskDefinitionList)
                        .stream()
                        .filter(s -> {
                            if (Thread.currentThread().isInterrupted()) {
                                throw new RuntimeException(new InterruptedException());
                            }
                            return true;
                        })
                        .filter(task -> filter.filter(projectName, processName, task.getName()))
                        .map(task -> {
                            List<DwWorkflow> dwWorkflows = convertTaskToWorkflowWithLoadedTask(task, loadedTasks);
                            checkPoint.doCheckpoint(writer, dwWorkflows, processName, task.getName());
                            return dwWorkflows;
                        })
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        dwWorkflowList = checkPoint.doWithCheckpoint(processFunc, projectName);

        if (dagData.getSchedule() != null) {
            setSchedule(dwWorkflow);
        }

        //processSubProcessDefinitionDepends();
        return dwWorkflowList;
    }

    private List<DwWorkflow> convertTaskToWorkflowWithLoadedTask(TaskDefinition taskDefinition, Map<String, List<DwWorkflow>> loadedTasks) {
        List<DwWorkflow> workflows = loadedTasks.get(taskDefinition.getName());
        if (workflows != null) {
            markSuccessProcess(workflows, taskDefinition);
            log.info("loaded task {} from checkpoint", taskDefinition.getName());
            return workflows;
        }
        return convertTaskToWorkFlow(taskDefinition);
    }

    protected void markSuccessProcess(List<DwWorkflow> workflows, TaskDefinition taskDefinition) {
        for (DwWorkflow workflow : workflows) {
            for (Node node : workflow.getNodes()) {
                DolphinMetrics metrics = DolphinMetrics.builder()
                        .projectName(taskDefinition.getProjectName())
                        .projectCode(taskDefinition.getProjectCode())
                        .processName(processDefinition.getName())
                        .processCode(processDefinition.getCode())
                        .taskName(taskDefinition.getName())
                        .taskCode(taskDefinition.getCode())
                        .taskType(taskDefinition.getTaskType())
                        .build();
                metrics.setWorkflowName(workflow.getName());
                metrics.setDwName(node.getName());
                metrics.setDwType(node.getType());
                TransformerContext.getCollector().markSuccessMiddleProcess(metrics);
            }
        }
    }

    private void setSchedule(DwWorkflow dwWorkflow) {
        Schedule schedule = dagData.getSchedule();
        ListUtils.emptyIfNull(dwWorkflow.getNodes()).forEach(node ->
        {
            if (schedule.getCrontab() != null) {
                node.setCronExpress(convertCrontab(schedule.getCrontab()));
            }
            if (schedule.getStartTime() != null) {
                node.setStartEffectDate(schedule.getStartTime());
            }
            if (schedule.getEndTime() != null) {
                node.setEndEffectDate(schedule.getEndTime());
            }
        });
    }

    private String getGlobalParams() {
        List<WorkflowParameter> parameters = MapUtils.emptyIfNull(processDefinition.getGlobalParamMap()).entrySet().stream().map(entry ->
                        new WorkflowParameter().setKey(entry.getKey()).setValue(entry.getValue()))
                .collect(Collectors.toList());
        return GsonUtils.toJsonString(parameters);
    }

    private List<DwWorkflow> convertTaskToWorkFlow(TaskDefinition taskDefinition) {
        if (inSkippedList(taskDefinition)) {
            return Collections.emptyList();
        }
        try {
            AbstractParameterConverter<AbstractParameters> taskConverter
                    = TaskConverterFactoryV3.create(dagData, taskDefinition, converterContext);
            taskConverter.convert();
            return taskConverter.getWorkflowList();
        } catch (UnSupportedTypeException e) {
            markFailedProcess(taskDefinition, e.getMessage());
            if (Config.INSTANCE.isSkipUnSupportType()) {
                return Collections.emptyList();
            } else {
                throw e;
            }
        } catch (Throwable e) {
            log.error("task converter error, taskName {} ", taskDefinition.getName(), e);
            if (Config.INSTANCE.isTransformContinueWithError()) {
                return Collections.emptyList();
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean inSkippedList(TaskDefinition taskDefinition) {
        if (Config.INSTANCE.getSkipTypes().contains(taskDefinition.getTaskType())
                || Config.INSTANCE.getSkipTaskCodes().contains(String.valueOf(taskDefinition.getCode()))) {
            log.warn("task name {} code {} in skipped list", taskDefinition.getName(), taskDefinition.getCode());
            markSkippedProcess(taskDefinition);
            return true;
        } else {
            return false;
        }
    }

    /**
     * SubProcess dependents handling logic
     */
    private void processSubProcessDefinitionDepends() {
        // for SubProcess Type
        ListUtils.emptyIfNull(dwWorkflowList).forEach(workflow -> ListUtils.emptyIfNull(workflow.getNodes()).stream()
                .filter(n -> StringUtils.equalsIgnoreCase(TaskType.SUB_PROCESS.name(), ((DwNode) n).getRawNodeType()))
                .forEach(subProcessNode -> {
                    SubProcessParameters subProcessParameter =
                            GsonUtils.fromJsonString(subProcessNode.getCode(),
                                    new TypeToken<SubProcessParameters>() {}.getType());
                    converterContext.getDolphinSchedulerPackage().getProcessDefinitions().values().stream().map(defList ->
                                    ListUtils.emptyIfNull(defList).stream()
                                            .filter(df -> subProcessParameter != null)
                                            .filter(df -> Objects.equals(df.getProcessDefinition().getCode(),
                                                    subProcessParameter.getProcessDefinitionCode()))
                                            .findFirst()
                                            .flatMap(proDef -> dwWorkflowList.stream()
                                                    .filter(wf -> StringUtils.equals(toWorkflowName(proDef.getProcessDefinition()), wf.getName()))
                                                    .findFirst()
                                                    .map(wf -> addStartEndNodeToDependedWorkflow(subProcessNode, proDef.getProcessDefinition(), wf)))
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
    private DwNodeIo addStartEndNodeToDependedWorkflow(Node subProcessNode, ProcessDefinition proDef, DwWorkflow wf) {
        DwNode startNode = new DwNode();
        startNode.setType(CodeProgramType.VIRTUAL.name());
        startNode.setWorkflowRef(wf);
        DwNodeIo startNodeOutput = new DwNodeIo();
        startNodeOutput.setData(Joiner.on(".").join(converterContext.getProject(), wf.getName(), "start"));
        startNodeOutput.setParseType(1);
        startNode.setOutputs(Collections.singletonList(startNodeOutput));
        startNode.setInputs(new ArrayList<>());
        ListUtils.emptyIfNull(subProcessNode.getOutputs()).stream().findFirst().ifPresent(
                depOut -> startNode.getInputs().add(depOut));

        DwNode endNode = new DwNode();
        endNode.setType(CodeProgramType.VIRTUAL.name());
        endNode.setWorkflowRef(wf);
        DwNodeIo endNodeOutput = new DwNodeIo();
        endNodeOutput.setData(Joiner.on(".").join(converterContext.getProject(), wf.getName(), "end"));
        endNodeOutput.setParseType(1);
        endNode.setOutputs(Collections.singletonList(endNodeOutput));
        endNode.setInputs(new ArrayList<>());

        ListUtils.emptyIfNull(wf.getNodes()).forEach(node -> {
            String prefix = Joiner.on(".").join(
                    converterContext.getProject().getName(), proDef.getProjectName());
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

    @Override
    public List<DwWorkflow> getWorkflowList() {
        return dwWorkflowList;
    }

    protected void markFailedProcess(TaskDefinition taskDefinition, String errorMsg) {
        DolphinMetrics metrics = DolphinMetrics.builder()
                .projectName(taskDefinition.getProjectName())
                .projectCode(taskDefinition.getProjectCode())
                .processName(processDefinition.getName())
                .processCode(processDefinition.getCode())
                .taskName(taskDefinition.getName())
                .taskCode(taskDefinition.getCode())
                .taskType(taskDefinition.getTaskType())
                .build();
        metrics.setErrorMsg(errorMsg);
        TransformerContext.getCollector().markFailedMiddleProcess(metrics);
    }

    protected void markSkippedProcess(TaskDefinition taskDefinition) {
        DolphinMetrics metrics = DolphinMetrics.builder()
                .projectName(taskDefinition.getProjectName())
                .projectCode(taskDefinition.getProjectCode())
                .processName(processDefinition.getName())
                .processCode(processDefinition.getCode())
                .taskName(taskDefinition.getName())
                .taskCode(taskDefinition.getCode())
                .taskType(taskDefinition.getTaskType())
                .build();
        TransformerContext.getCollector().markSkippedProcess(metrics);
    }
}
