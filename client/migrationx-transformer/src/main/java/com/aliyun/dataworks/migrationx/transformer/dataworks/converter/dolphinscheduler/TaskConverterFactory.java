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

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.ProcessMeta;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.TaskNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.TaskType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.AbstractParameters;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author 聿剑
 * @date 2022/10/18
 */
public class TaskConverterFactory {
    @SuppressWarnings("unchecked")
    public static <P extends AbstractParameters> AbstractParameterConverter<P> create(
        ProcessMeta processMeta, TaskNode taskDefinition, DolphinSchedulerConverterContext converterContext)
        throws Throwable {
        TaskType taskType = taskDefinition.getType();
        return (AbstractParameterConverter<P>)Optional.ofNullable(taskType).map(type -> {
            switch (taskType) {
                case SQL:
                    return new SqlParameterConverter(processMeta, taskDefinition, converterContext);
                case PYTHON:
                    return new PythonParameterConverter(processMeta, taskDefinition, converterContext);
                case SPARK:
                    return new SparkParameterConverter(processMeta, taskDefinition, converterContext);
                case CONDITIONS:
                    return new ConditionsParameterConverter(processMeta, taskDefinition, converterContext);
                case SUB_PROCESS:
                    return new SubProcessParameterConverter(processMeta, taskDefinition, converterContext);
                case DEPENDENT:
                    return new DependentParameterConverter(processMeta, taskDefinition, converterContext);
                case SHELL:
                    return new ShellParameterConverter(processMeta, taskDefinition, converterContext);
                case MR:
                    return new MrParameterConverter(processMeta, taskDefinition, converterContext);
                case SQOOP:
                    return new SqoopParameterConverter(processMeta, taskDefinition, converterContext);
                case HTTP:
                    // TODO:
                case DATAX:
                    // TODO:
                default:
                    return new DefaultParameterConverter(processMeta, taskDefinition, converterContext);
            }
        }).orElseThrow((Supplier<Throwable>)() -> new RuntimeException("unsupported converter task type: " + taskType));
    }
}
