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

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.CronExpressUtil;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.List;

/**
 * @author 聿剑
 * @date 2022/10/19
 */
@Slf4j
public abstract class ProcessDefinitionConverter<Project, ProcessDefinitionType, DataSource, ResourceInfo,
    UdfFunction> {
    protected final ProcessDefinitionType processDefinition;
    protected final DolphinSchedulerConverterContext<Project, ProcessDefinitionType, DataSource, ResourceInfo,
        UdfFunction>
        converterContext;

    public ProcessDefinitionConverter(
        DolphinSchedulerConverterContext<Project, ProcessDefinitionType, DataSource, ResourceInfo, UdfFunction> converterContext,
        ProcessDefinitionType processDefinition) {
        this.converterContext = converterContext;
        this.processDefinition = processDefinition;
    }

    public abstract void convert();

    public abstract List<DwWorkflow> getWorkflowList();

    protected String convertCrontab(String scheduleCrontab) {
        try {
            return CronExpressUtil.quartzCronExpressionToDwCronExpress(scheduleCrontab);
        } catch (ParseException e) {
            log.error("convert quartz cron expression error: ", e);
        }

        return "day";
    }
}
