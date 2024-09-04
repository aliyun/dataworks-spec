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

package com.aliyun.dataworks.common.spec.domain.dw.types;

import org.apache.commons.lang3.StringUtils;

/**
 * Language type defines
 *
 * @author 聿剑
 * @date 2024/6/17
 */
public enum LanguageEnum implements Language {
    CLICKHOUSE_SQL(IdentifierConstants.CLICKHOUSE_SQL, "ClickHouse SQL"),
    DORIS_SQL(IdentifierConstants.DORIS_SQL, "Doris SQL"),
    FLINK_SQL(IdentifierConstants.FLINK_SQL, "Flink SQL"),
    HIVE_SQL(IdentifierConstants.HIVE_SQL, "Hive SQL"),
    HOLOGRES_SQL(IdentifierConstants.HOLOGRES_SQL, "Hologres SQL"),
    IMPALA_SQL(IdentifierConstants.IMPALA_SQL, "Impala SQL"),
    JSON(IdentifierConstants.JSON, "Json"),
    JAVA(IdentifierConstants.JAVA, "Java"),
    MYSQL_SQL(IdentifierConstants.MYSQL_SQL, "Mysql SQL"),
    ADB_MYSQL_SQL(IdentifierConstants.ADB_MYSQL_SQL, "Adb Mysql SQL"),
    ODPS_SQL(IdentifierConstants.ODPS_SQL, "ODPS SQL"),
    ODPS_SCRIPT(IdentifierConstants.ODPS_SCRIPT, "ODPS Script"),
    OB_MYSQL_SQL(IdentifierConstants.OB_MYSQL_SQL, "OceanBase MySQL"),
    OB_ORACLE_SQL(IdentifierConstants.OB_ORACLE_SQL, "OceanBase Oracle"),
    TRANSACT_SQL(IdentifierConstants.TRANSACT_SQL, "T-SQL"),
    PLSQL(IdentifierConstants.PLSQL, "PL/SQL"),
    POSTGRESQL_SQL(IdentifierConstants.POSTGRESQL_SQL, "PostgreSQL SQL"),
    SHELL_SCRIPT(IdentifierConstants.SHELL_SCRIPT, "Shell Script"),
    SPARK_SQL(IdentifierConstants.SPARK_SQL, "Spark SQL"),
    SQL(IdentifierConstants.SQL, "SQL"),
    PRESTO_SQL(IdentifierConstants.PRESTO_SQL, "Presto SQL"),
    PYTHON2(IdentifierConstants.PYTHON2, "Python2"),
    PYTHON3(IdentifierConstants.PYTHON3, "Python3"),
    TRINO_SQL(IdentifierConstants.TRINO_SQL, "Trino SQL"),
    STARROCKS_SQL(IdentifierConstants.STARROCKS_SQL, "StarRocks SQL"),
    YAML(IdentifierConstants.YAML, "Yaml");

    private final String identifier;
    private final String displayName;

    LanguageEnum(String identifier, String displayName) {
        this.identifier = identifier;
        this.displayName = displayName;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static LanguageEnum getByIdentifier(String identifier) {
        LanguageEnum[] values = values();
        LanguageEnum[] var2 = values;
        int var3 = values.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            LanguageEnum languageEnum = var2[var4];
            if (StringUtils.equalsIgnoreCase(languageEnum.getIdentifier(), identifier)) {
                return languageEnum;
            }
        }

        throw new UnsupportedOperationException("unsupported identifier:" + identifier);
    }

    public static class IdentifierConstants {
        public static final String SQL = "sql";
        public static final String PYTHON2 = "python2";
        public static final String PYTHON3 = "python3";
        public static final String ODPS_SQL = "odps-sql";
        public static final String ODPS_SCRIPT = "odps-script";
        public static final String HIVE_SQL = "hive-sql";
        public static final String PRESTO_SQL = "presto-sql";
        public static final String TRINO_SQL = "trino-sql";
        public static final String SPARK_SQL = "spark-sql";
        public static final String HOLOGRES_SQL = "hologres-sql";
        public static final String POSTGRESQL_SQL = "postgresql-sql";
        public static final String TRANSACT_SQL = "t-sql";
        public static final String PLSQL = "plsql";
        public static final String MYSQL_SQL = "mysql-sql";
        public static final String CLICKHOUSE_SQL = "clickhouse-sql";
        public static final String FLINK_SQL = "flink-sql";
        public static final String ADB_MYSQL_SQL = "adbmysql-sql";
        public static final String STARROCKS_SQL = "starrocks-sql";
        public static final String SHELL_SCRIPT = "shell-script";
        public static final String IMPALA_SQL = "impala-sql";
        public static final String JSON = "json";
        public static final String JAVA = "java";
        public static final String YAML = "yaml";
        public static final String OB_MYSQL_SQL = "obmysql-sql";
        public static final String OB_ORACLE_SQL = "oboracle-sql";
        public static final String DORIS_SQL = "doris-sql";
    }
}
