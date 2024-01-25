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

package com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.constant;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description
 * @createTime 2020-04-10
 */
public class DataXConstants {
    /**
     * 采云间数据源信息
     */
    public final static String RESOURCE_INFO = "resourceInfo";
    /**
     * 采云间数据源名称
     */
    public final static String RESOURCE_NAME = "resourceName";
    /**
     * 采云间表信息
     */
    public final static String TABLE_INFO = "tableInfo";
    /**
     * 采云间表名称
     */
    public final static String TABLE_NAME = "tableName";
    /**
     * 采云间列详细信息
     */
    public final static String COLUMN_INFO_LIST = "columninfoList";
    /**
     * 采云间数据源信息-表信息
     */
    public final static String OBJECT_NAME = "objectName";
    /**
     * 采云间数据源信息-filePrefix
     */
    public final static String FILE_PREFIX = "filePrefix";
    /**
     * 写入模式
     */
    public final static String WRITE_NODE = "writeNode";
    /**
     * 采云间写入模式-replace
     */
    public final static String REPLACE = "replace";
    /**
     * 采云间写入模式-insert
     */
    public final static String INSERT = "insert";
    /**
     * 采云间写入模式-更新
     */
    public final static String UPDATE = "update";

    /**
     * 采云间reader
     */
    public final static String READER = "reader";

    public final static String WRITER = "writer";

    /**
     * d2 写入模式-insert
     */
    public final static String DI_INSERT = "insert into";
    /**
     * d2 写入模式-update
     */
    public final static String DI_UPDATE = "on duplicate key update";
    /**
     * d2 写入模式-replace
     */
    public final static String DI_REPLACE = "replace into";
    public final static String DI_DATASOURCE = "datasource";
    public final static String DI_TABLE = "table";
    public final static String DI_CONNECTION = "connection";
    public final static String DI_COLUMN = "column";
    public final static String DI_SPLIT_PK = "splitPK";
    public final static String DI_QUERY_SQL = "querySql";
    public final static String DI_WHERE = "where";
    public final static String DI_READER = "reader";
    public final static String DI_WRITER = "writer";
    public final static String DI_FIELD_DELIMITER_ORIGIN = "fieldDelimiterOrigin";
    public final static String DI_FIELD_DELIMITER = "fieldDelimiter";
    public final static String DI_MARK_DONE_FILE_NAME = "markDoneFileName";
    public final static String DI_FILE_FORMAT = "fileFormat";
    public final static String DI_FILE_PATH = "filePath";
    public final static String DI_PATH = "path";
    public static final String DI_COMPRESS = "compress";
    public static final String DI_HEADER = "header";
    public static final String DI_TRANSFORMER = "transformer";
    /**
     * d2 分区标示
     */
    public final static String DI_PARTITION = "partition";
    public static final String DI_COLUMN_VALUE = "value";
    public static final String DI_COLUMN_NAME = "name";
    public static final String DI_COLUMN_INDEX = "index";

    public final static String DI_OSS_OBJECT = "object";
    public static final String DI_FILE_NAME = "fileName";
}
