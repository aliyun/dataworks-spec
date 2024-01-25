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

package com.aliyun.dataworks.migrationx.domain.dataworks.utils;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.hive.ast.HiveInsertStatement;
import com.alibaba.druid.sql.dialect.hive.ast.HiveMultiInsertStatement;
import com.alibaba.druid.sql.dialect.odps.visitor.OdpsASTVisitorAdapter;
import com.alibaba.druid.sql.dialect.odps.visitor.OdpsSchemaStatVisitor;
import com.alibaba.druid.sql.repository.Schema;
import com.alibaba.druid.sql.visitor.SQLEvalVisitorImpl;
import com.aliyun.migrationx.common.utils.ReflectUtils;
import com.google.common.base.Joiner;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author sam.liux
 * @date 2021/04/14
 */
public class DruidSqlUtils {
    public enum SCHEMA_OF_TYPE {
        TABLE,
        FUNCTION
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DruidSqlUtils.class);

    public static class OdpsFunctionVisitor extends SQLEvalVisitorImpl {
        @Data
        @Accessors(chain = true)
        @ToString
        public static class OdpsSqlMethod {
            private String schema;
            private String methodName;
        }

        private final List<OdpsSqlMethod> methods = new ArrayList<>();

        @Override
        public boolean visit(SQLMethodInvokeExpr x) {
            LOGGER.info("method: {}, arguments: {}", x.getMethodName(), x.getArguments());
            ListUtils.emptyIfNull(x.getArguments()).stream()
                .filter(arg -> arg instanceof SQLMethodInvokeExpr)
                .forEach(exp -> super.visit((SQLMethodInvokeExpr)exp));

            OdpsSqlMethod method = new OdpsSqlMethod().setMethodName(x.getMethodName());
            if (x.getOwner() != null && x.getOwner() instanceof SQLIdentifierExpr) {
                String owner = visitOwner((SQLIdentifierExpr)x.getOwner());
                method.setSchema(owner);
            }

            if (ListUtils.emptyIfNull(methods).stream().noneMatch(m ->
                StringUtils.equals(m.getMethodName(), method.getMethodName()))) {
                methods.add(method);
            }

            return super.visit(x);
        }

        private String visitOwner(SQLIdentifierExpr owner) {
            if (owner == null) {
                return null;
            }

            return owner.getName();
        }

        public List<OdpsSqlMethod> getMethods() {
            return methods;
        }
    }

    static class OdpsSchemaMappingVisitor extends OdpsASTVisitorAdapter {
        private Map<String, String> schemaMapping;
        private Map<String, String> tableMapping = new HashMap<>();

        public OdpsSchemaMappingVisitor setSchemaMapping(Map<String, String> schemaMapping) {
            this.schemaMapping = MapUtils.emptyIfNull(schemaMapping).entrySet().stream()
                .collect(Collectors.toMap(ent -> StringUtils.lowerCase(ent.getKey()), Map.Entry::getValue));
            return this;
        }

        public Map<String, String> getTableMapping() {
            return tableMapping;
        }

        @Override
        public boolean visit(SQLExprTableSource x) {
            String normalizedSchema = StringUtils.lowerCase(StringUtils.defaultIfBlank(x.getSchema(), getOwner(x)));
            LOGGER.info("table: {}, {}, source line: {}, source number: {}", normalizedSchema, x.getTableName(),
                x.getSourceLine(), x.getSourceColumn());
            if (MapUtils.emptyIfNull(schemaMapping).containsKey(normalizedSchema)) {
                String newSchema = MapUtils.emptyIfNull(schemaMapping).get(normalizedSchema);
                String oldTable = Joiner.on(".").join(
                    "(\\s?)" + com.aliyun.dataworks.migrationx.domain.dataworks.utils.StringUtils.escapeRegexChars(
                        normalizedSchema) + "(\\s*)",
                    "(\\s*)" + com.aliyun.dataworks.migrationx.domain.dataworks.utils.StringUtils.escapeRegexChars(
                        x.getTableName()) + "([\\s,;]?)");
                String newTable = Joiner.on(".").join("$1" + newSchema + "$2",
                    "$3" + com.aliyun.dataworks.migrationx.domain.dataworks.utils.StringUtils.escapeRegexChars(x.getTableName())
                        + "$4");
                x.setSchema(newSchema);
                tableMapping.put(oldTable, newTable);
            }
            return super.visit(x);
        }

        private String getOwner(SQLExprTableSource x) {
            return (String)Optional.ofNullable(x).map(SQLExprTableSource::getExpr)
                .filter(ex -> SQLPropertyExpr.class.isAssignableFrom(ex.getClass()))
                .map(ex -> (SQLPropertyExpr)ex)
                .map(SQLPropertyExpr::getOwner)
                .map(o -> ReflectUtils.getObjectField(o, "name")).orElse(null);
        }

        private void visitWith(SQLWithSubqueryClause clause) {
            if (clause != null) {
                clause.accept(this);
            }
        }

        @Override
        public boolean visit(SQLInSubQueryExpr x) {
            return super.visit(x);
        }

        @Override
        public boolean visit(HiveInsertStatement x) {
            visitWith(x.getWith());
            return super.visit(x);
        }

        @Override
        public boolean visit(SQLInsertStatement x) {
            visitWith(x.getWith());
            return super.visit(x);
        }

        @Override
        public boolean visit(HiveMultiInsertStatement x) {
            visitWith(x.getWith());
            return super.visit(x);
        }

        @Override
        public boolean visit(SQLUpdateStatement x) {
            visitWith(x.getWith());
            return super.visit(x);
        }

        @Override
        public boolean visit(SQLSelect x) {
            visitWith(x.getWithSubQuery());
            return super.visit(x);
        }
    }

    public static List<String> parseOdpsSchemas(String sql) {
        try {
            List<SQLStatement> res = SQLUtils.parseStatements(sql, DbType.odps);
            OdpsSchemaStatVisitor schemaStatVisitor = new OdpsSchemaStatVisitor();
            ListUtils.emptyIfNull(res).forEach(st -> st.accept(schemaStatVisitor));
            return getSchemas(schemaStatVisitor);
        } catch (Exception e) {
            LOGGER.warn("parse odps sql schema failed, sql: {}, exception: ", sql, e);
        }
        return ListUtils.emptyIfNull(null);
    }

    private static List<String> getSchemas(OdpsSchemaStatVisitor visitor) {
        List<String> schemas = visitor.getRepository().getSchemas().stream()
            .map(Schema::getName)
            .distinct()
            .collect(Collectors.toList());

        List<String> owners = ListUtils.emptyIfNull(visitor.getOriginalTables()).stream()
            .filter(t -> SQLPropertyExpr.class.isAssignableFrom(t.getClass()))
            .map(t -> (SQLPropertyExpr)t)
            .map(SQLPropertyExpr::getOwner)
            .filter(Objects::nonNull)
            .map(o -> (String)ReflectUtils.getObjectField(o, "name"))
            .filter(Objects::nonNull)
            .distinct().collect(Collectors.toList());
        schemas.addAll(owners);
        return schemas.stream().distinct().collect(Collectors.toList());
    }

    public static Map<SCHEMA_OF_TYPE, List<String>> parseOdpsSchemaNew(String sql) {
        try {
            List<SQLStatement> res = SQLUtils.parseStatements(sql, DbType.odps);
            OdpsSchemaStatVisitor schemaStatVisitor = new OdpsSchemaStatVisitor();
            OdpsFunctionVisitor sqlEvalVisitor = new OdpsFunctionVisitor();
            ListUtils.emptyIfNull(res).forEach(st -> {
                st.accept(schemaStatVisitor);
                st.accept(sqlEvalVisitor);
            });

            List<String> schemas = getSchemas(schemaStatVisitor);
            Map<SCHEMA_OF_TYPE, List<String>> result = new HashMap<>();
            result.put(SCHEMA_OF_TYPE.TABLE, schemas);

            result.put(SCHEMA_OF_TYPE.FUNCTION, ListUtils.emptyIfNull(sqlEvalVisitor.getMethods()).stream()
                .filter(m -> StringUtils.isNotBlank(m.getSchema()))
                .map(OdpsFunctionVisitor.OdpsSqlMethod::getSchema)
                .distinct()
                .collect(Collectors.toList()));
            return result;
        } catch (Exception e) {
            LOGGER.warn("parse odps sql schema failed, sql: {}, exception: ", sql, e);
        }
        return MapUtils.emptyIfNull(null);
    }

    public static String replaceOdpsSchemas(String sql, Map<String, String> mappings) {
        return replaceOdpsSchemas(sql, mappings, true);
    }

    public static String replaceOdpsSchemas(String sql, Map<String, String> mappings, boolean withoutFormat) {
        if (StringUtils.isBlank(sql) || MapUtils.isEmpty(mappings)) {
            return sql;
        }

        if (BooleanUtils.isTrue(withoutFormat)) {
            return replaceOdpsSchemasWithoutFormat(sql, mappings);
        }
        return replaceOdpsSchemasWithFormat(sql, mappings);
    }

    private static String replaceOdpsSchemasWithFormat(String sql, Map<String, String> mappings) {
        if (StringUtils.isBlank(sql) || MapUtils.isEmpty(mappings)) {
            return sql;
        }

        try {
            List<SQLStatement> res = SQLUtils.parseStatements(sql, DbType.odps, true);
            OdpsSchemaMappingVisitor odpsSchemaMappingVisitor = new OdpsSchemaMappingVisitor();
            odpsSchemaMappingVisitor.setSchemaMapping(mappings);
            ListUtils.emptyIfNull(res).forEach(st -> st.accept(odpsSchemaMappingVisitor));
            SQLUtils.FormatOption options = SQLUtils.DEFAULT_LCASE_FORMAT_OPTION;
            return fixExcludeIoComments(SQLUtils.toSQLString(res, DbType.odps, options));
        } catch (Exception e) {
            LOGGER.warn("replace odps sql schema failed, sql: {}, exception: ", sql, e);
        }
        return sql;
    }

    private static String replaceOdpsSchemasWithoutFormat(String sql, Map<String, String> mappings) {
        if (StringUtils.isBlank(sql) || MapUtils.isEmpty(mappings)) {
            return sql;
        }

        try {
            List<SQLStatement> res = SQLUtils.parseStatements(sql, DbType.odps, true);
            OdpsSchemaMappingVisitor odpsSchemaMappingVisitor = new OdpsSchemaMappingVisitor();
            odpsSchemaMappingVisitor.setSchemaMapping(mappings);
            OdpsFunctionVisitor odpsFunctionVisitor = new OdpsFunctionVisitor();
            ListUtils.emptyIfNull(res).forEach(st -> {
                st.accept(odpsSchemaMappingVisitor);
                st.accept(odpsFunctionVisitor);
            });

            Map<String, String> tableMapping = odpsSchemaMappingVisitor.getTableMapping();
            LOGGER.info("tableMapping: {}", tableMapping);

            Map<String, String> functionMapping = ListUtils.emptyIfNull(odpsFunctionVisitor.getMethods()).stream()
                .filter(m ->
                    StringUtils.isNotBlank(m.getSchema()) && mappings.containsKey(m.getSchema())).collect(
                    Collectors.toMap(
                        method -> Joiner.on(".").join(
                            "(\\s?)" + com.aliyun.dataworks.migrationx.domain.dataworks.utils.StringUtils.escapeRegexChars(
                                method.getSchema()) + "(\\s*)",
                            "(\\s*)" + com.aliyun.dataworks.migrationx.domain.dataworks.utils.StringUtils.escapeRegexChars(
                                method.getMethodName()) + "([\\s]*\\()"),
                        method -> Joiner.on(".").join(
                            "$1" + mappings.get(method.getSchema()) + "$2",
                            "$3" + method.getMethodName() + "$4")
                    ));
            LOGGER.info("functionMapping: {}", functionMapping);

            AtomicReference<String> newSql = new AtomicReference<>(sql);
            MapUtils.emptyIfNull(tableMapping).forEach((oldTable, newTable) ->
                JavaFunctionUtils.withResultAndExceptionIgnored(_t -> {
                    Pattern pattern = Pattern.compile(oldTable, Pattern.CASE_INSENSITIVE);
                    newSql.set(RegExUtils.replaceAll(newSql.get(), pattern, newTable));
                }).accept(null));

            MapUtils.emptyIfNull(functionMapping).forEach((oldFunc, newFunc) ->
                JavaFunctionUtils.withResultAndExceptionIgnored(_t -> {
                    Pattern pattern = Pattern.compile(oldFunc, Pattern.CASE_INSENSITIVE);
                    newSql.set(RegExUtils.replaceAll(newSql.get(), pattern, newFunc));
                }).accept(null));
            return fixExcludeIoComments(newSql.get());
        } catch (Exception e) {
            LOGGER.warn("replace odps sql schema failed, sql: {}, exception: ", sql, e);
        }
        return sql;
    }

    private static String fixExcludeIoComments(String code) {
        if (StringUtils.indexOf(code, "-- @exclude_input=") >= 0) {
            code = StringUtils.replaceIgnoreCase(code, "-- @exclude_input=", "--@exclude_input=");
        }

        if (StringUtils.indexOf(code, "-- @exclude_output=") >= 0) {
            code = StringUtils.replaceIgnoreCase(code, "-- @exclude_output=", "--@exclude_output=");
        }

        return code;
    }
}
