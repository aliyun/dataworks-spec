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

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.CycleType;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description
 * @createTime 2020-04-07
 */
public enum ScheduleTypeEnum {
    /**
     * 默认
     */
    DEFAULT("0"),
    /**
     * 分
     */
    SECOND("1"),
    /**
     * 小时
     */
    HOUR("2"),
    /**
     * 天
     */
    DAY("3"),
    /**
     * 周
     */
    WEEK("4"),
    /**
     * 月
     */
    MONTH("5");

    private final String type;

    public static ScheduleTypeEnum fromType(String type) {
        return ALL_VALUES.get(type);
    }

    ScheduleTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static CycleType toCycleType(String type) {
        if (SECOND.type.equals(type) || HOUR.type.equals(type)) {
            return CycleType.NOT_DAY;
        } else {
            return CycleType.DAY;
        }
    }

    private static final Map<String, ScheduleTypeEnum> ALL_VALUES = Maps.newHashMap();

    static {
        for (ScheduleTypeEnum type : ScheduleTypeEnum.values()) {
            ALL_VALUES.put(type.getType(), type);
        }
    }
}
