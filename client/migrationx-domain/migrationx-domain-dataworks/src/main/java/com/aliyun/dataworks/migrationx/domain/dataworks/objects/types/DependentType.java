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
 * DependentType
 *
 * @author 聿剑
 * @date 2024/6/17
 */
public enum DependentType {
    NONE(0),
    USER_DEFINE(1),
    CHILD(2),
    SELF(3),
    USER_DEFINE_AND_SELF(13),
    CHILD_AND_SELF(23);

    private final int value;

    DependentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static DependentType getDependentTypeByValue(int value) {
        DependentType[] var1 = values();
        int var2 = var1.length;

        for (DependentType dt : values()) {
            if (dt.getValue() == value) {
                return dt;
            }
        }

        throw new IllegalArgumentException(String.format("unknown code %d for DependentType", value));
    }
}