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

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Datasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.ProcessMeta;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.TaskNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.AbstractParameters;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;

import java.util.Optional;

/**
 * @author 聿剑
 * @date 2022/10/19
 */
public class DefaultParameterConverter extends AbstractParameterConverter<AbstractParameters> {
    public DefaultParameterConverter(ProcessMeta processMeta,
        TaskNode taskDefinition,
        DolphinSchedulerConverterContext<Project, ProcessMeta, Datasource, ResourceInfo,
            UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    protected void convertParameter() {
        DwNode dwNode = newDwNode(processMeta, taskDefinition);
        dwNode.setCode(taskDefinition.getParams());
        dwNode.setType(Optional.ofNullable(properties)
            .map(props -> props.getProperty(Constants.CONVERTER_TARGET_UNSUPPORTED_NODE_TYPE_AS))
            .orElse(CodeProgramType.VIRTUAL.name()));
    }
}
