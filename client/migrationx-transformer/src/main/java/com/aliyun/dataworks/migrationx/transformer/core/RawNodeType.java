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

package com.aliyun.dataworks.migrationx.transformer.core;

/**
 * @author sam.liux
 * @date 2019/07/01
 */
public enum RawNodeType {
    AZKABAN_COMMAND,
    AZKABAN_HIVE,
    SPARK_SQL,
    AZKABAN_NOOP,
    AZKABAN_FLOW,

    OOZIE_JOIN,
    OOZIE_FORK,
    OOZIE_END,
    OOZIE_START,
    OOZIE_KILL,
    OOZIE_OK,
    OOZIE_HIVE,
    OOZIE_EMAIL,
    OOZIE_ERROR,
    OOZIE_HIVE2,
    OOZIE_SHELL,
    OOZIE_SQOOP,

    HIVE_SQL,
    MR,
    SPARK,

    FLINK,
    SHELL,
    HIVE,
    SPARK_SHELL,
    SPARK_STREAMING,
    SQOOP,
    PIG,
    STREAMING_SQL,
    PRESTO_SQL,
    IMPALA_SQL
}
