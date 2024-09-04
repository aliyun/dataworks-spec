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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums;

/**
 * task node type
 */
public enum TaskType {
    /**
     * 0 SHELL
     * 1 SQL
     * 2 SUB_PROCESS
     * 3 PROCEDURE
     * 4 MR
     * 5 SPARK
     * 6 PYTHON
     * 7 DEPENDENT
     * 8 FLINK
     * 9 HTTP
     * 10 DATAX
     * 11 CONDITIONS
     * 12 SQOOP
     * 13 WATERDROP
     * 15 PIGEON
     */
    SHELL(0, "SHELL"),
    SQL(1, "SQL"),
    SUB_PROCESS(2, "SUB_PROCESS"),
    PROCEDURE(3, "PROCEDURE"),
    MR(4, "MR"),
    SPARK(5, "SPARK"),
    PYTHON(6, "PYTHON"),
    DEPENDENT(7, "DEPENDENT"),
    FLINK(8, "FLINK"),
    HTTP(9, "HTTP"),
    DATAX(10, "DATAX"),
    CONDITIONS(11, "CONDITIONS"),
    SQOOP(12, "SQOOP"),
    WATERDROP(13, "WATERDROP"),
    SWITCH(14, "SWITCH"),
    PIGEON(15, "PIGEON"),
    HIVECLI(16, "HIVECLI");

    TaskType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final int code;
    private final String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static TaskType of(String type) {
        for (TaskType taskType : TaskType.values()) {
            if (taskType.name().equalsIgnoreCase(type)) {
                return taskType;
            }
        }
        throw new RuntimeException("unSupport type " + type);
    }
}
