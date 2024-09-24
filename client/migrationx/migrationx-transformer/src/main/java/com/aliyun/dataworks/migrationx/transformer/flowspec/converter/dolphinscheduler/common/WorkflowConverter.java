/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.enums.FailureStrategy;
import com.aliyun.dataworks.common.spec.domain.enums.NodeInstanceModeType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRerunModeType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScheduleStrategy;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessTaskRelation;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.Schedule;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320.DagDataSchedule;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-10
 */
@Slf4j
public class WorkflowConverter extends AbstractCommonConverter<SpecWorkflow> {

    private final SpecWorkflow workflow;

    private final DagDataSchedule dagDataSchedule;

    private static final SpecScriptRuntime WORKFLOW_RUNTIME = new SpecScriptRuntime();

    private static final SpecScriptRuntime MANUAL_WORKFLOW_RUNTIME = new SpecScriptRuntime();

    static {
        WORKFLOW_RUNTIME.setEngine(CodeProgramType.VIRTUAL_WORKFLOW.getCalcEngineType().getLabel());
        WORKFLOW_RUNTIME.setCommand("WORKFLOW");

        MANUAL_WORKFLOW_RUNTIME.setEngine(CodeProgramType.VIRTUAL_WORKFLOW.getCalcEngineType().getLabel());
        MANUAL_WORKFLOW_RUNTIME.setCommand("MANUAL_WORKFLOW");
    }

    public WorkflowConverter(SpecWorkflow workflow, DagDataSchedule dagDataSchedule, DolphinSchedulerV3ConverterContext context) {
        super(context);
        this.workflow = Optional.ofNullable(workflow).orElseGet(this::initWorkflow);
        this.dagDataSchedule = dagDataSchedule;
    }

    public WorkflowConverter(DagDataSchedule dagDataSchedule, DolphinSchedulerV3ConverterContext context) {
        this(null, dagDataSchedule, context);
    }

    private SpecWorkflow initWorkflow() {
        SpecWorkflow specWorkflow = new SpecWorkflow();
        specWorkflow.setDependencies(new ArrayList<>());
        specWorkflow.setNodes(new ArrayList<>());
        specWorkflow.setInputs(new ArrayList<>());
        specWorkflow.setOutputs(new ArrayList<>());
        return specWorkflow;
    }

    /**
     * convert to T type object
     *
     * @return T type object
     */
    @Override
    public SpecWorkflow convert() {
        if (Objects.isNull(dagDataSchedule) || Objects.isNull(dagDataSchedule.getProcessDefinition())) {
            return workflow;
        }
        ProcessDefinition processDefinition = dagDataSchedule.getProcessDefinition();
        convertProcess(processDefinition, workflow, context);

        convertTrigger(dagDataSchedule.getSchedule(), workflow, processDefinition, workflow.getScript(), context);

        convertTaskDefinitions(dagDataSchedule.getTaskDefinitionList(), workflow, context);

        convertTaskRelations(dagDataSchedule.getProcessTaskRelationList(), workflow, context);
        return workflow;
    }

    protected void convertProcess(ProcessDefinition processDefinition, SpecWorkflow specWorkflow, DolphinSchedulerV3ConverterContext context) {
        log.info("convert workflow,processDefinition: {}", dagDataSchedule.getProcessDefinition());
        specWorkflow.setId(generateUuid(processDefinition.getCode()));
        specWorkflow.setName(processDefinition.getName());
        specWorkflow.setDescription(processDefinition.getDescription());

        List<SpecVariable> specVariableList = new ParamListConverter(processDefinition.getGlobalParamList(), context).convert();
        log.info("convert workflow,global params: {}", specVariableList);

        SpecScript script = new SpecScript();
        script.setParameters(specVariableList);
        // The default value is manual workflow. If there is schedule in the dagDataSchedule, it will be changed to workflow after parsing schedule
        script.setRuntime(MANUAL_WORKFLOW_RUNTIME);
        script.setPath(FilenameUtils.concat(StringUtils.defaultString(context.getDefaultScriptPath()), specWorkflow.getName()));

        specWorkflow.setScript(script);
        specWorkflow.getOutputs().add(buildDefaultOutput(specWorkflow));
    }

    protected void convertTrigger(Schedule schedule, SpecWorkflow specWorkflow, ProcessDefinition processDefinition, SpecScript script,
        DolphinSchedulerV3ConverterContext context) {
        if (Objects.nonNull(dagDataSchedule.getSchedule())) {
            SpecTrigger trigger = new TriggerConverter(schedule, context).convert();
            specWorkflow.setTrigger(trigger);
            specWorkflow.setStrategy(buildSpecScheduleStrategy(processDefinition, schedule));
            script.setRuntime(WORKFLOW_RUNTIME);
            log.info("convert workflow,schedule: {}", schedule);
        }
    }

    protected void convertTaskDefinitions(List<TaskDefinition> taskDefinitions, SpecWorkflow specWorkflow,
        DolphinSchedulerV3ConverterContext context) {
        log.info("convert workflow,taskDefinitionList: {}", taskDefinitions);
        new SpecNodeListConverter(null, specWorkflow, taskDefinitions, context).convert();
    }

    protected void convertTaskRelations(List<ProcessTaskRelation> processTaskRelationList, SpecWorkflow specWorkflow,
        DolphinSchedulerV3ConverterContext context) {
        log.info("convert workflow,processTaskRelationList: {}", processTaskRelationList);
        new SpecFlowDependConverter(null, specWorkflow, processTaskRelationList, context).convert();
    }

    private SpecNodeOutput buildDefaultOutput(SpecWorkflow specWorkflow) {
        SpecNodeOutput specNodeOutput = new SpecNodeOutput();
        specNodeOutput.setIsDefault(true);
        specNodeOutput.setId(generateUuid());
        specNodeOutput.setData(specWorkflow.getId());
        specNodeOutput.setRefTableName(specWorkflow.getName());
        return specNodeOutput;
    }

    private SpecScheduleStrategy buildSpecScheduleStrategy(ProcessDefinition processDefinition, Schedule schedule) {
        SpecScheduleStrategy strategy = new SpecScheduleStrategy();
        strategy.setPriority(convertPriority(schedule.getProcessInstancePriority()));
        strategy.setTimeout(processDefinition.getTimeout());
        strategy.setInstanceMode(NodeInstanceModeType.T_PLUS_1);
        strategy.setRerunMode(NodeRerunModeType.ALL_ALLOWED);
        strategy.setRerunTimes(0);
        strategy.setRerunInterval(0);
        strategy.setIgnoreBranchConditionSkip(false);
        strategy.setFailureStrategy(
            com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.FailureStrategy.CONTINUE.equals(schedule.getFailureStrategy())
                ? FailureStrategy.CONTINUE
                : FailureStrategy.BREAK);
        return strategy;
    }
}
