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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.process.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.AbstractParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;
import com.aliyun.migrationx.common.utils.JSONUtils;

public class CustomParameterConverter extends AbstractParameterConverter<AbstractParameters> {
    public CustomParameterConverter(DagData dagData, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo, UdfFunc> converterContext) {
        super(dagData, taskDefinition, converterContext);
    }

    @Override
    public List<DwNode> convertParameter() throws IOException {
        DwNode dwNode = newDwNode(taskDefinition);
        dwNode.setType(CodeProgramType.CUSTOM.name());
        dwNode.setOrigin(JSONUtils.toJsonString(taskDefinition));

        return Arrays.asList(dwNode);
    }
}
