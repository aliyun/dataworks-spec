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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.v2;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.TaskType;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;
import com.aliyun.migrationx.common.utils.Config;

public class TaskConverterFactoryV2 {
    public static AbstractParameterConverter create(
            DagData processMeta, TaskDefinition taskDefinition, DolphinSchedulerConverterContext converterContext) throws Throwable {
        if (Config.INSTANCE.getTempTaskTypes().contains(taskDefinition.getTaskType())) {
            return new CustomParameterConverter(processMeta, taskDefinition, converterContext);
        }
        TaskType taskType = TaskType.of(taskDefinition.getTaskType());

        switch (taskType) {
            case PROCEDURE:
                return new ProcedureParameterConverter(processMeta, taskDefinition, converterContext);
            case SQL:
                return new SqlParameterConverter(processMeta, taskDefinition, converterContext);
            case PYTHON:
                return new PythonParameterConverter(processMeta, taskDefinition, converterContext);
            case CONDITIONS:
                return new ConditionsParameterConverter(processMeta, taskDefinition, converterContext);
            case SUB_PROCESS:
                return new SubProcessParameterConverter(processMeta, taskDefinition, converterContext);
            case DEPENDENT:
                return new DependentParameterConverter(processMeta, taskDefinition, converterContext);
            case SHELL:
                return new ShellParameterConverter(processMeta, taskDefinition, converterContext);
            case SWITCH:
                return new SwitchParameterConverter(processMeta, taskDefinition, converterContext);
            case HTTP:
                return new HttpParameterConverter(processMeta, taskDefinition, converterContext);
            case SPARK:
                return new SparkParameterConverter(processMeta, taskDefinition, converterContext);
            case MR:
                return new MrParameterConverter(processMeta, taskDefinition, converterContext);
            case DATAX:
                return new DataxParameterConverter(processMeta, taskDefinition, converterContext);
            case SQOOP:
                return new SqoopParameterConverter(processMeta, taskDefinition, converterContext);
            default:
                throw new RuntimeException("unsupported converter task type: " + taskType);
        }
    }
}
