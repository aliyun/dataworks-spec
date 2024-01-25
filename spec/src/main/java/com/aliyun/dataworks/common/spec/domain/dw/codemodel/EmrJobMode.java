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
 * @author 聿剑
 * @date 2021/08/03
 */
public enum EmrJobMode {
    /**
     * Header/Gateway提交
     */
    LOCAL("LOCAL"),

    /**
     * Worker提交
     */
    YARN("YARN");

    private final String value;

    EmrJobMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EmrJobMode getByValue(String value) {
        for (EmrJobMode mode : values()) {
            if (mode.getValue().equalsIgnoreCase(value)) {
                return mode;
            }
        }

        throw new RuntimeException("unknown type: " + EmrJobMode.class.getSimpleName() + ", value: " + value);
    }
}
