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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.ProcessData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.ProcessMeta;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.TaskNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.datasource.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.entity.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.enums.TaskType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.AbstractParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.subprocess.SubProcessParameters;
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
import com.aliyun.migrationx.common.context.TransformerContext;
import com.aliyun.migrationx.common.exception.UnSupportedTypeException;
import com.aliyun.migrationx.common.metrics.DolphinMetrics;
import com.aliyun.migrationx.common.utils.Config;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.common.base.Joiner;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2022/10/19
 */
@Slf4j
public class V1ProcessDefinitionConverter
        extends ProcessDefinitionConverter<Project, ProcessMeta, DataSource, ResourceInfo, UdfFunc> {
    private List<DwWorkflow> dwWorkflowList = new ArrayList<>();

    private DolphinSchedulerConverterFilter filter;
    private CheckPoint checkPoint;

    public V1ProcessDefinitionConverter(
            DolphinSchedulerConverterContext<Project, ProcessMeta, DataSource, ResourceInfo, UdfFunc> converterContext,
            ProcessMeta processDefinition) {
        super(converterContext, processDefinition);
        this.filter = new DolphinSchedulerConverterFilter();
        checkPoint = CheckPoint.getInstance();
    }

    public static String toWorkflowName(ProcessMeta processMeta) {
        return com.aliyun.dataworks.migrationx.domain.dataworks.utils.StringUtils.toValidName(Joiner.on("_").join(
                processMeta.getProjectName(), processMeta.getProcessDefinitionName()));
    }

    @Override
    public List<DwWorkflow> convert() {
        DwWorkflow dwWorkflow = new DwWorkflow();
        dwWorkflow.setName(toWorkflowName(processDefinition));
        dwWorkflow.setType(WorkflowType.BUSINESS);
        dwWorkflow.setScheduled(true);
        dwWorkflow.setVersion(WorkflowVersion.V3);

        converterContext.setDwWorkflow(dwWorkflow);

        String projectName = processDefinition.getProjectName();
        String processName = processDefinition.getProcessDefinitionName();
        Map<String, List<DwWorkflow>> loadedTasks = checkPoint.loadFromCheckPoint(projectName, processName);

        final Function<StoreWriter, List<DwWorkflow>> processFunc = (StoreWriter writer) ->
                Optional.ofNullable(processDefinition.getProcessDefinitionJson())
                        .map(pd -> {
                            dwWorkflow.setParameters(GsonUtils.toJsonString(pd.getGlobalParams().stream()
                                    .map(prop -> new WorkflowParameter().setKey(prop.getProp()).setValue(prop.getValue()))
                                    .collect(Collectors.toList())));
                            return toWorkflowlist(pd, loadedTasks, writer);
                        }).orElse(Collections.emptyList());

        dwWorkflowList = checkPoint.doWithCheckpoint(processFunc, projectName);

        ListUtils.emptyIfNull(dwWorkflow.getNodes()).forEach(node ->
                node.setCronExpress(convertCrontab(processDefinition.getScheduleCrontab())));

        processSubProcessDefinitionDepends();
        return dwWorkflowList;
    }

    private List<DwWorkflow> toWorkflowlist(ProcessData pd, Map<String, List<DwWorkflow>> loadedTasks, StoreWriter writer) {
        return ListUtils.emptyIfNull(pd.getTasks())
                .stream()
                .filter(s -> {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new RuntimeException(new InterruptedException());
                    }
                    return true;
                })
                .filter(task -> filter.filter(processDefinition.getProjectName(),
                        processDefinition.getProcessDefinitionName(), task.getName()))
                .filter(taskNode -> !inSkippedList(taskNode))
                .map(taskNode -> {
                    List<DwWorkflow> dwWorkflows = convertTaskToWorkflowWithLoadedTask(taskNode, loadedTasks);
                    checkPoint.doCheckpoint(writer, dwWorkflows, processDefinition.getProcessDefinitionName(), taskNode.getName());
                    return dwWorkflows;
                })
                .collect(Collectors.toList())
                .stream().flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<DwWorkflow> convertTaskToWorkflowWithLoadedTask(TaskNode taskNode, Map<String, List<DwWorkflow>> loadedTasks) {
        List<DwWorkflow> workflows = loadedTasks.get(taskNode.getName());
        if (workflows != null) {
            markSuccessProcess(workflows, taskNode);
            log.info("loaded task {} from checkpoint", taskNode.getName());
            return workflows;
        }
        return converter(taskNode);
    }

    private List<DwWorkflow> converter(TaskNode taskNode) {
        AbstractParameterConverter<AbstractParameters> converter;
        try {
            converter = TaskConverterFactory.create(processDefinition, taskNode, converterContext);
            converter.convert();
        } catch (UnSupportedTypeException e) {
            markFailedProcess(taskNode, e.getMessage());
            if (Config.INSTANCE.isSkipUnSupportType()) {
                List<DwWorkflow> list = Collections.emptyList();
                return list;
            } else {
                throw e;
            }
        } catch (Throwable e) {
            log.error("task converter error: ", e);
            throw new RuntimeException(e);
        }
        return converter.getWorkflowList();
    }

    protected void markSuccessProcess(List<DwWorkflow> workflows, TaskNode taskNode) {
        for (DwWorkflow workflow : workflows) {
            for (Node node : workflow.getNodes()) {
                DolphinMetrics metrics = DolphinMetrics.builder()
                        .projectName(processDefinition.getProjectName())
                        .processName(processDefinition.getProcessDefinitionName())
                        .taskName(taskNode.getName())
                        .taskType(taskNode.getType().name())
                        .build();
                metrics.setWorkflowName(workflow.getName());
                metrics.setDwName(node.getName());
                metrics.setDwType(node.getType());
                TransformerContext.getCollector().markSuccessMiddleProcess(metrics);
            }
        }
    }

    private boolean inSkippedList(TaskNode taskNode) {
        if (Config.INSTANCE.getSkipTypes().contains(taskNode.getType())
                || Config.INSTANCE.getSkipTaskCodes().contains(taskNode.getName())) {
            log.warn("task name {}  in skipped list", taskNode.getName());
            markSkippedProcess(taskNode);
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
                                            .filter(df -> Objects.equals(df.getProcessDefinitionId(),
                                                    subProcessParameter.getProcessDefinitionId()))
                                            .findFirst()
                                            .flatMap(proDef -> dwWorkflowList.stream()
                                                    .filter(wf -> StringUtils.equals(toWorkflowName(proDef), wf.getName()))
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

    protected void markSkippedProcess(TaskNode taskNode) {
        DolphinMetrics metrics = DolphinMetrics.builder()
                .projectName(processDefinition.getProjectName())
                .processName(processDefinition.getProcessDefinitionName())
                .taskName(taskNode.getName())
                .taskType(taskNode.getType().name())
                .timestamp(System.currentTimeMillis())
                .build();
        TransformerContext.getCollector().markSkippedProcess(metrics);
    }

    protected void markFailedProcess(TaskNode taskNode, String errorMsg) {
        DolphinMetrics metrics = DolphinMetrics.builder()
                .projectName(processDefinition.getProjectName())
                .processName(processDefinition.getProcessDefinitionName())
                .taskName(taskNode.getName())
                .taskType(taskNode.getType().name())
                .timestamp(System.currentTimeMillis())
                .build();
        metrics.setErrorMsg(errorMsg);
        TransformerContext.getCollector().markSkippedProcess(metrics);
    }
}
