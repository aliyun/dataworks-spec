/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec;

import java.util.List;
import java.util.Objects;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils.LocationUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessTaskRelation;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320.DagDataSchedule;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.DolphinSchedulerConverter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.ProcessDefinitionConverter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.ProcessTaskRelationListConverter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.TaskDefinitionListConverter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Desc: convert whole spec as a dolphin scheduler v3 dag
 *
 * @author 莫泣
 * @date 2024-07-12
 */
@Slf4j
public class FlowSpecDolphinSchedulerV3Converter implements DolphinSchedulerConverter<DataWorksWorkflowSpec> {

    private final DataWorksWorkflowSpec spec;

    private final FlowSpecConverterContext context;

    private final DagDataSchedule dagDataSchedule;

    private static final int X_STEP = 350;

    private static final int Y_STEP = 150;

    public FlowSpecDolphinSchedulerV3Converter(DataWorksWorkflowSpec spec, FlowSpecConverterContext context) {
        this.spec = spec;
        this.context = context;
        this.dagDataSchedule = new DagDataSchedule();
    }

    /**
     * Convert the whole spec to dolphin scheduler dag
     *
     * @param from origin obj
     * @return dolphin scheduler obj
     */
    @Override
    public DagDataSchedule convert(DataWorksWorkflowSpec from) {
        if (Objects.isNull(from)) {
            log.error("spec is null");
            throw new BizException(ErrorCode.PARAMETER_NOT_SET, "spec");
        }

        ProcessDefinition processDefinition = new ProcessDefinitionConverter(from, null, this.context).convert();
        log.info("processDefinition: {}", processDefinition);
        List<TaskDefinition> taskDefinitionList = new TaskDefinitionListConverter(from, null, this.context).convert();
        log.info("taskDefinitionList: {}", taskDefinitionList);
        List<ProcessTaskRelation> processTaskRelationList = new ProcessTaskRelationListConverter(from, null, this.context).convert();
        log.info("processTaskRelationList: {}", processTaskRelationList);

        // add locations
        processDefinition.setLocations(LocationUtils.buildLocations(processTaskRelationList, X_STEP, Y_STEP));

        this.dagDataSchedule.setProcessDefinition(processDefinition);
        this.dagDataSchedule.setTaskDefinitionList(taskDefinitionList);
        this.dagDataSchedule.setProcessTaskRelationList(processTaskRelationList);

        return this.dagDataSchedule;
    }

    public DagDataSchedule convert() {
        return convert(spec);
    }

}
