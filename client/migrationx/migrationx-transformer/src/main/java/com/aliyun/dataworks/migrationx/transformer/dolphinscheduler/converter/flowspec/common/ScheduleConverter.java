/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common;

import java.util.Date;
import java.util.Objects;

import com.aliyun.dataworks.common.spec.domain.enums.TriggerType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.FailureStrategy;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.WarningType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.Schedule;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.Priority;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v301.ReleaseState;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import com.aliyun.migrationx.common.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-03
 */
public class ScheduleConverter extends AbstractCommonConverter<Schedule> {

    private final SpecTrigger specTrigger;

    private final ProcessDefinition processDefinition;

    public ScheduleConverter(Schedule schedule, SpecTrigger specTrigger, ProcessDefinition processDefinition, FlowSpecConverterContext context) {
        super(Objects.nonNull(schedule) ? schedule : new Schedule(), context);
        this.processDefinition = processDefinition;
        this.specTrigger = specTrigger;
    }

    public ScheduleConverter(SpecTrigger specTrigger, ProcessDefinition processDefinition, FlowSpecConverterContext context) {
        this(null, specTrigger, processDefinition, context);
    }

    public Schedule convert() {
        if (Objects.isNull(specTrigger) || Objects.isNull(processDefinition) || !TriggerType.SCHEDULER.equals(specTrigger.getType())) {
            return null;
        }
        // all field
        result.setId(generateId(specTrigger.getId()));
        result.setProcessDefinitionCode(processDefinition.getCode());
        result.setProcessDefinitionName(processDefinition.getName());
        result.setProjectName(processDefinition.getProjectName());
        result.setDefinitionDescription(processDefinition.getDescription());
        result.setStartTime(DateUtils.convertStringToDate(specTrigger.getStartTime()));
        result.setEndTime(DateUtils.convertStringToDate(specTrigger.getEndTime()));
        result.setTimezoneId(specTrigger.getTimezone());
        result.setCrontab(specCron2DolphinCron(specTrigger.getCron()));
        result.setFailureStrategy(FailureStrategy.END);
        result.setWarningType(WarningType.NONE);
        result.setCreateTime(new Date());
        result.setUpdateTime(new Date());
        result.setUserId(context.getUserId());
        result.setUserName(null);

        result.setReleaseState(context.isOnlineSchedule() ? ReleaseState.ONLINE : ReleaseState.OFFLINE);
        processDefinition.setScheduleReleaseState(result.getReleaseState());

        result.setWarningGroupId(0);
        result.setProcessInstancePriority(Priority.MEDIUM);
        result.setWorkerGroup(StringUtils.defaultString(context.getWorkerGroup(), "default"));
        result.setTenantCode(StringUtils.defaultString(context.getTenantCode(), "default"));
        result.setEnvironmentCode(context.getEnvironmentCode());
        result.setEnvironmentName(null);
        return result;
    }

    private String specCron2DolphinCron(String specCron) {
        return specCron;
    }
}
