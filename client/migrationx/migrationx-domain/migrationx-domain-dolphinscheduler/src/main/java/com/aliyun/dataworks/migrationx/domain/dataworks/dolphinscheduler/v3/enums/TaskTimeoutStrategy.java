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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums;

/**
 * task timeout strategy
 */
public enum TaskTimeoutStrategy {
    /**
     * 0 warn
     * 1 failed
     * 2 warn+failed
     */
    WARN(0, "warn"),
    FAILED(1, "failed"),
    WARNFAILED(2, "warnfailed");

    TaskTimeoutStrategy(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    private final int code;
    private final String descp;

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }

    public static TaskTimeoutStrategy of(int status) {
        for (TaskTimeoutStrategy es : values()) {
            if (es.getCode() == status) {
                return es;
            }
        }
        throw new IllegalArgumentException("invalid status : " + status);
    }
}
