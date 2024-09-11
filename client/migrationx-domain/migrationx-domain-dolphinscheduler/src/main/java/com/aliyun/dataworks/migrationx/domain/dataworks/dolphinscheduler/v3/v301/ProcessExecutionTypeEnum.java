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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v301;

import java.util.HashMap;

/**
 * @author 聿剑
 * @date 2022/10/12
 */
public enum ProcessExecutionTypeEnum {

    PARALLEL(0, "parallel"),
    SERIAL_WAIT(1, "serial wait"),
    SERIAL_DISCARD(2, "serial discard"),
    SERIAL_PRIORITY(3, "serial priority");

    ProcessExecutionTypeEnum(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    private final int code;
    private final String descp;

    private static final HashMap<Integer, ProcessExecutionTypeEnum> EXECUTION_STATUS_MAP = new HashMap<>();

    static {
        for (ProcessExecutionTypeEnum executionType : ProcessExecutionTypeEnum.values()) {
            EXECUTION_STATUS_MAP.put(executionType.code, executionType);
        }
    }

    public boolean typeIsSerial() {
        return this != PARALLEL;
    }

    public boolean typeIsSerialWait() {
        return this == SERIAL_WAIT;
    }

    public boolean typeIsSerialDiscard() {
        return this == SERIAL_DISCARD;
    }

    public boolean typeIsSerialPriority() {
        return this == SERIAL_PRIORITY;
    }

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }

    public static ProcessExecutionTypeEnum of(int executionType) {
        if (EXECUTION_STATUS_MAP.containsKey(executionType)) {
            return EXECUTION_STATUS_MAP.get(executionType);
        }
        throw new IllegalArgumentException("invalid status : " + executionType);
    }
}
