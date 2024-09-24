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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.types;

/**
 * 可重跑的条件
 * @author zhaochen.zc
 */
public enum RerunMode {
    /**
     * 兼容脏数据
     */
    UNKNOWN(-1),

    /**
     * 所有实例均可重跑
     */
    ALL_ALLOWED(1),

    /**
     * 只有失败的情况下可以重跑（成功后便不可重跑）
     */
    FAILURE_ALLOWED(0),

    /**
     * 所有情况下均不可重跑
     */
    ALL_DENIED(2);

    private final Integer value;

    RerunMode(int v) {
        this.value = v;
    }

    public int getValue() {
        return value;
    }

    public static RerunMode getByValue(int value) {
        for (RerunMode mode : values()) {
            if (mode.getValue() == value) {
                return mode;
            }
        }

        throw new RuntimeException("unknown rerun mode value: " + value);
    }
}
