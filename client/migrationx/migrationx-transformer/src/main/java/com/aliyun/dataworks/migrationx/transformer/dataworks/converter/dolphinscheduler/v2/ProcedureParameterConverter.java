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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.enums.DbType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DolphinSchedulerV2Context;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.process.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.procedure.ProcedureParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.CollectionUtils;

public class ProcedureParameterConverter extends AbstractParameterConverter<ProcedureParameters> {
    public ProcedureParameterConverter(DagData processMeta, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo,
                    UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    public List<DwNode> convertParameter() {
        String sqlNodeMapStr = converterContext.getProperties().getProperty(
                Constants.CONVERTER_TARGET_SQL_NODE_TYPE_MAP, "{}");
        Map<String, DbType> sqlTypeNodeTypeMapping = GsonUtils.fromJsonString(sqlNodeMapStr,
                new TypeToken<Map<String, DbType>>() {}.getType());
        sqlTypeNodeTypeMapping = Optional.ofNullable(sqlTypeNodeTypeMapping).orElse(new HashMap<>(1));

        String defaultNodeTypeIfNotSupport = getConverterType();

        DwNode dwNode = newDwNode(taskDefinition);
        DbType codeProgramType = sqlTypeNodeTypeMapping.get(parameter.getType());
        dwNode.setType(Optional.ofNullable(codeProgramType).map(Enum::name)
                .orElse(defaultNodeTypeIfNotSupport));
        //add ref datasource
        List<DataSource> datasources = DolphinSchedulerV2Context.getContext().getDataSources();
        if (parameter.getDatasource() > 0) {
            CollectionUtils.emptyIfNull(datasources).stream()
                    .filter(s -> s.getId() == parameter.getDatasource())
                    .findFirst()
                    .ifPresent(s -> dwNode.setConnection(s.getName()));
        }
        dwNode.setCode(parameter.getMethod());
        return Arrays.asList(dwNode);
    }

    private String getConverterType() {
        String convertType = properties.getProperty(Constants.CONVERTER_TARGET_UNSUPPORTED_NODE_TYPE_AS);
        return getConverterType(convertType, CodeProgramType.VIRTUAL.name());
    }
}
