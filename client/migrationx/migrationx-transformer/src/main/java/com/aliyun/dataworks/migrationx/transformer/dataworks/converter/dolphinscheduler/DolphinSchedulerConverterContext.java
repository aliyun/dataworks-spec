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
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerPackage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Properties;

/**
 * @author 聿剑
 * @date 2022/10/25
 */
@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode
public class DolphinSchedulerConverterContext<Project, ProcessDefinitionType, DataSource, ResourceInfo, UdfFunction> {
    private com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project project;
    private DwWorkflow dwWorkflow;
    private DolphinSchedulerPackage<Project, ProcessDefinitionType, DataSource, ResourceInfo, UdfFunction>
        dolphinSchedulerPackage;
    private Properties properties;
}
