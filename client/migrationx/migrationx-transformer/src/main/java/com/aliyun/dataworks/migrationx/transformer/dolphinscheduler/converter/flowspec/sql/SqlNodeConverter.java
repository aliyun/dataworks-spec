/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.domain.ref.SpecDatasource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.enums.TaskType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.DbType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.SqlType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.sql.SqlParameters;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.AbstractNodeConverter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import org.apache.commons.lang3.StringUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-04
 */
public class SqlNodeConverter extends AbstractNodeConverter<SqlParameters> {

    public SqlNodeConverter(SpecNode specNode, FlowSpecConverterContext context) {
        super(specNode, context);
    }

    /**
     * convert spec node to dolphin scheduler task parameters
     *
     * @return dolphin scheduler task parameters
     */
    @Override
    protected SqlParameters convertParameter() {
        SqlParameters sqlParameters = new SqlParameters();
        SpecDatasource datasource = specNode.getDatasource();
        if (checkDatasource(datasource)) {
            sqlParameters.setType(datasource.getType());
            sqlParameters.setDatasource(Integer.parseInt(StringUtils.defaultString(datasource.getId(), "1")));
        } else {
            setDefaultDataSource(sqlParameters);
        }
        String content = Optional.ofNullable(specNode.getScript()).map(SpecScript::getContent).orElse(StringUtils.EMPTY);
        sqlParameters.setSql(content);
        sqlParameters.setSqlType(SqlType.QUERY.ordinal());

        sqlParameters.setPreStatements(new ArrayList<>());
        sqlParameters.setPostStatements(new ArrayList<>());
        sqlParameters.setDisplayRows(10);

        return sqlParameters;
    }

    private void setDefaultDataSource(SqlParameters sqlParameters) {
        sqlParameters.setType(context.getDefaultDatasourceType());
        sqlParameters.setDatasource(context.getDefaultDatasourceId());
    }

    private boolean checkDatasource(SpecDatasource datasource) {
        if (Objects.isNull(datasource)) {
            return false;
        }
        DbType[] values = DbType.values();
        return Arrays.stream(values).map(DbType::name).anyMatch(s -> s.equals(datasource.getType()));
    }

    @Override
    protected void setTaskType() {
        result.setTaskType(TaskType.SQL.name());
    }
}
