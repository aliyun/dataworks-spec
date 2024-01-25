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

package com.aliyun.dataworks.common.spec.domain.dw.codemodel;

/**
 * @author sam.liux
 * @date 2020/12/11
 */
public enum EmrJobType {
    /**
     * Hive cmd
     */
    HIVE("HIVE"),
    /**
     * Hive Sql
     */
    HIVE_SQL("HIVE_SQL"),
    /**
     * Spark sql
     */
    SPARK_SQL("SPARK_SQL"),
    /**
     * spark shell
     */
    SPARK_SHELL("SPARK_SHELL"),
    /**
     * spark streaming
     */
    SPARK_STREAMING("SPARK_STREAMING"),
    /**
     * spark submit
     */
    SPARK("SPARK"),
    /**
     * impala sql
     */
    IMPALA_SQL("IMPALA_SQL"),
    /**
     * presto sql
     */
    PRESTO_SQL("PRESTO_SQL"),
    /**
     * MR
     */
    MR("MR"),
    /**
     * sqoop
     */
    SQOOP("SQOOP"),
    /**
     * flink
     */
    FLINK("FLINK"),
    /**
     * pig
     */
    PIG("PIG"),
    /**
     * streaming sql
     */
    STREAMING_SQL("STREAMING_SQL"),
    /**
     * shell
     */
    SHELL("SHELL");

    EmrJobType(String text) {
        this.text = text;
    }

    private String text;

    public String getText() {
        return text;
    }

    public static EmrJobType getJobType(String type) {
        for (EmrJobType jobType : values()) {
            if (jobType.getText().equalsIgnoreCase(type)) {
                return jobType;
            }
        }

        throw new RuntimeException("unknown type: " + type);
    }
}
