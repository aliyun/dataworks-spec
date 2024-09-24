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

package com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.enums;

import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description
 * @createTime 2020-04-07
 */
public enum CDPTypeEnum {
    /**
     * odps
     */
    ODPS("odps", "odps"),
    /**
     * mysql
     */
    MYSQL("mysql", "mysql"),

    /**
     * oracle
     */
    ORACLE("oracle", "oracle"),
    /**
     * ots
     */
    OTS("ots", "ots"),
    /**
     * oss
     */
    OSS("oss", "oss"),
    /**
     * oss
     */
    SFTP("ftp", "sftp"),
    /**
     * oceanbase
     */
    OCEAN_BASE_NEW("apsaradb_for_oceanbase", "oceanbase_new"),

    /**
     * db2
     */
    DB2("db2", "db2"),

    /**
     * sqlserver
     */
    SQL_SERVER("sqlserver", "sqlserver"),

    /**
     * ads
     */
    ADS("ads", "ads"),

    /**
     * postgresql
     */
    POSTGRESQL("postgresql", "postgresql"),

    /**
     * hdfs
     */
    HDFS("hdfs", "hdfs"),

    /**
     * hbase
     */
    HBASE("hbase", "hbase");

    private final String D2DataXType;
    private final String CDPDataXType;

    public static CDPTypeEnum fromCyjType(String type) {
        if (!ALL_VALUES.containsKey(type)) {
            throw BizException.of(ErrorCode.UNKNOWN_ENUM_TYPE).with(CDPTypeEnum.class.getSimpleName(), type);
        }

        return ALL_VALUES.get(type);
    }

    CDPTypeEnum(String d2DataXType, String CDPDataXType) {
        this.D2DataXType = d2DataXType;
        this.CDPDataXType = CDPDataXType;
    }

    public String getCDPDataXType() {
        return CDPDataXType;
    }

    public String getD2DataXType() {
        return D2DataXType;
    }

    private static final Map<String, CDPTypeEnum> ALL_VALUES = Maps.newHashMap();

    static {
        for (CDPTypeEnum type : CDPTypeEnum.values()) {
            ALL_VALUES.put(type.getCDPDataXType(), type);
        }
    }
}
