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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DolphinSchedulerV2Context;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.DbType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.process.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.sql.SqlParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.aliyun.migrationx.common.utils.JSONUtils;

import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author 聿剑
 * @date 2022/10/18
 */
@Slf4j
public class SqlParameterConverter extends AbstractParameterConverter<SqlParameters> {
    public SqlParameterConverter(DagData processMeta, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo,
                    UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    public List<DwNode> convertParameter() {
        String sqlNodeMapStr = converterContext.getProperties().getProperty(
                Constants.CONVERTER_TARGET_SQL_NODE_TYPE_MAP, "{}");
        Map<String, String> sqlTypeNodeTypeMapping = GsonUtils.fromJsonString(sqlNodeMapStr,
                new TypeToken<Map<String, String>>() {}.getType());

        DwNode dwNode = newDwNode(taskDefinition);
        String codeProgramType = Optional.ofNullable(sqlTypeNodeTypeMapping)
                .map(s -> s.get(parameter.getType()))
                .orElseGet(() -> {
                    if (DbType.HIVE.name().equalsIgnoreCase(parameter.getType())) {
                        return CodeProgramType.EMR_HIVE.name();
                    } else if (DbType.SPARK.name().equalsIgnoreCase(parameter.getType())) {
                        return CodeProgramType.EMR_SPARK.name();
                    } else if (DbType.ofType(parameter.getType()) != null) {
                        return parameter.getType();
                    } else {
                        String defaultNodeTypeIfNotSupport = getSQLConverterType();
                        log.warn("using default node Type {} for node {}", defaultNodeTypeIfNotSupport, dwNode.getName());
                        return defaultNodeTypeIfNotSupport;
                    }
                });

        dwNode.setType(codeProgramType);
        dwNode.setConnection(getConnectionName(codeProgramType));
        dwNode.setCode(parameter.getSql());

        if (CodeProgramType.EMR_HIVE.name().equals(codeProgramType) || CodeProgramType.EMR_SPARK.name().equals(codeProgramType)) {
            dwNode.setCode(EmrCodeUtils.toEmrCode(dwNode));
        }

        return Arrays.asList(dwNode);
    }

    private String getConnectionName(String codeProgramType) {
        String mappingJson = converterContext.getProperties().getProperty(Constants.WORKFLOW_CONVERTER_CONNECTION_MAPPING);
        if (StringUtils.isNotEmpty(mappingJson)) {
            Map<String, String> connectionMapping = JSONUtils.parseObject(mappingJson, Map.class);
            if (connectionMapping == null) {
                log.error("parse connection mapping with {} error", mappingJson);
            } else {
                String connectionName = connectionMapping.get(codeProgramType);
                log.info("Got connectionName {} by {}", connectionName, codeProgramType);
                return connectionName;
            }
        }

        if (!CodeProgramType.EMR_HIVE.name().equals(codeProgramType) && !CodeProgramType.EMR_SPARK.name().equals(codeProgramType)) {
            //add ref datasource
            List<DataSource> datasources = DolphinSchedulerV2Context.getContext().getDataSources();
            if (parameter.getDatasource() > 0) {
                return CollectionUtils.emptyIfNull(datasources).stream()
                        .filter(s -> s.getId() == parameter.getDatasource())
                        .findFirst()
                        .map(s -> s.getName())
                        .orElseGet(null);
            }
        }
        return null;
    }

    private String getSQLConverterType() {
        String convertType = properties.getProperty(Constants.CONVERTER_TARGET_COMMAND_SQL_TYPE_AS);
        return getConverterType(convertType, CodeProgramType.SQL_COMPONENT.name());
    }
}
