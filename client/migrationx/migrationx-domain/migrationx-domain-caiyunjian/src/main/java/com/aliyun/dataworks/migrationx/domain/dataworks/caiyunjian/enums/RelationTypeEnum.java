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
public enum RelationTypeEnum {
    /**
     * 正常依赖关系
     */
    NORMAL(0, "正常依赖关系"),
    /**
     * 跨周期依赖关系
     */
    STEP_VERSION_CYCLE(3, "跨周期依赖关系");

    private final Integer type;

    public static RelationTypeEnum fromType(Integer type) {
        return ALL_VALUES.get(type);
    }

    RelationTypeEnum(Integer type, String description) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    private static final Map<Integer, RelationTypeEnum> ALL_VALUES = Maps.newHashMap();

    static {
        for (RelationTypeEnum type : RelationTypeEnum.values()) {
            ALL_VALUES.put(type.getType(), type);
        }
    }
}
