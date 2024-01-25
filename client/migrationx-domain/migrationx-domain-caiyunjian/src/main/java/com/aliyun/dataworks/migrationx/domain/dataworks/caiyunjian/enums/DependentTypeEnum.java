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

package com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.enums;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description
 * @createTime 2020-04-07
 */
public enum DependentTypeEnum {
    /**
     * 不跨版本
     */
    NONE(0, "不跨版本"),
    /**
     * 跨版本，自定义依赖
     */
    USER_DEFINE(1, "跨版本，自定义依赖"),
    /**
     * 跨版本，一级子节点依赖
     */
    CHILD(2, "跨版本，一级子节点依赖"),
    /**
     * 跨版本，自依赖
     */
    SELF(3, "跨版本，自依赖");

    private final Integer type;

    public static DependentTypeEnum fromType(Integer type) {
        return ALL_VALUES.get(type);
    }

    DependentTypeEnum(Integer type, String description) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    private static final Map<Integer, DependentTypeEnum> ALL_VALUES = Maps.newHashMap();

    static {
        for (DependentTypeEnum type : DependentTypeEnum.values()) {
            ALL_VALUES.put(type.getType(), type);
        }
    }
}
