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
package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.enums;

/**
 * task node type
 */
public enum TaskType {
    /**
     * 0 SHELL 1 SQL 2 SUB_PROCESS 3 PROCEDURE 4 MR 5 SPARK 6 PYTHON 7 DEPENDENT 8 FLINK 9 HTTP 10 DATAX 11 CONDITIONS
     * 12 SQOOP
     */
    SHELL(0, "shell"),
    SQL(1, "sql"),
    SUB_PROCESS(2, "sub_process"),
    PROCEDURE(3, "procedure"),
    MR(4, "mr"),
    SPARK(5, "spark"),
    PYTHON(6, "python"),
    DEPENDENT(7, "dependent"),
    FLINK(8, "flink"),
    HTTP(9, "http"),
    DATAX(10, "datax"),
    CONDITIONS(11, "conditions"),
    SQOOP(12, "sqoop");

    TaskType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    private final int code;
    private final String descp;

    public static boolean typeIsNormalTask(String typeName) {
        TaskType taskType = TaskType.of(typeName);
        return !(taskType == TaskType.SUB_PROCESS || taskType == TaskType.DEPENDENT);
    }

    public static TaskType of(String typeName) {
        for (TaskType type : TaskType.values()) {
            if (type.name().equalsIgnoreCase(typeName)) {
                return type;
            }
        }
        throw new RuntimeException(String.format("no taskType with name %s", typeName));
    }

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }
}
