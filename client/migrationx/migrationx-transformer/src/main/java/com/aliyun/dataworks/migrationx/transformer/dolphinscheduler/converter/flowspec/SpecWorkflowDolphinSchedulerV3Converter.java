/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils.LocationUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessTaskRelation;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.Schedule;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320.DagDataSchedule;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.DolphinSchedulerConverter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.ProcessDefinitionConverter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.ProcessTaskRelationListConverter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.ScheduleConverter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.TaskDefinitionListConverter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Desc: convert a workflow in spec to dolphin scheduler dag
 *
 * @author 莫泣
 * @date 2024-07-03
 */
public class SpecWorkflowDolphinSchedulerV3Converter implements DolphinSchedulerConverter<SpecWorkflow> {

    private static final Logger log = LoggerFactory.getLogger(SpecWorkflowDolphinSchedulerV3Converter.class);
    private final DataWorksWorkflowSpec spec;

    private final SpecWorkflow workflow;

    private final FlowSpecConverterContext context;

    private final DagDataSchedule dagDataSchedule;

    private static final int X_STEP = 350;

    private static final int Y_STEP = 150;

    public SpecWorkflowDolphinSchedulerV3Converter(DataWorksWorkflowSpec spec, SpecWorkflow workflow, FlowSpecConverterContext context) {
        this.spec = spec;
        this.context = context;
        this.workflow = workflow;
        this.dagDataSchedule = new DagDataSchedule();
    }

    /**
     * Convert a work flow to dolphin scheduler obj
     *
     * @param from origin obj
     * @return dolphin scheduler obj
     */
    @Override
    public DagDataSchedule convert(SpecWorkflow from) {
        if (Objects.isNull(spec) || Objects.isNull(from)) {
            log.error("spec or workflow is null");
            throw new BizException(ErrorCode.PARAMETER_NOT_SET, "spec or workflow");
        }
        ProcessDefinition processDefinition = new ProcessDefinitionConverter(spec, from, this.context).convert();
        log.info("processDefinition: {}", processDefinition);
        Schedule schedule = new ScheduleConverter(findSpecTrigger(from), processDefinition, this.context).convert();
        log.info("schedule: {}", schedule);
        List<TaskDefinition> taskDefinitionList = new TaskDefinitionListConverter(spec, from, this.context).convert();
        log.info("taskDefinitionList: {}", taskDefinitionList);
        List<ProcessTaskRelation> processTaskRelationList = new ProcessTaskRelationListConverter(spec, from, this.context).convert();
        log.info("processTaskRelationList: {}", processTaskRelationList);

        // add locations
        processDefinition.setLocations(LocationUtils.buildLocations(processTaskRelationList, X_STEP, Y_STEP));

        this.dagDataSchedule.setProcessDefinition(processDefinition);
        this.dagDataSchedule.setSchedule(schedule);
        this.dagDataSchedule.setTaskDefinitionList(taskDefinitionList);
        this.dagDataSchedule.setProcessTaskRelationList(processTaskRelationList);

        return this.dagDataSchedule;
    }

    public DagDataSchedule convert() {
        return this.convert(this.workflow);
    }

    /**
     * find first valid spec trigger from spec and nodes
     *
     * @param workflow workflow
     * @return first valid spec trigger
     */
    private SpecTrigger findSpecTrigger(SpecWorkflow workflow) {
        Optional<SpecTrigger> specTriggerOpt = Optional.ofNullable(workflow).map(SpecWorkflow::getTrigger);
        return specTriggerOpt.orElseGet(() -> Optional.ofNullable(workflow).map(SpecWorkflow::getNodes).orElse(Collections.emptyList()).stream()
            .map(SpecNode::getTrigger).filter(Objects::nonNull).findFirst().orElse(null));
    }
}
