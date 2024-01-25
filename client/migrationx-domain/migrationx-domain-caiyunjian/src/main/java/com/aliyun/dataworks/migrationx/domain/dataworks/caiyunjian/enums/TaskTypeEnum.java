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
public enum TaskTypeEnum {
    /**
     * 刷新任务
     */
    ODPS(2, "ODPS", "刷新任务"),
    /**
     * 同步任务
     */
    CDP(5, "CDP", "同步任务"),
    /**
     * 虚任务
     */
    VIRTUAL(6, "VIRTUAL", "虚任务"),
    /**
     * 检测任务
     */
    DETECT(8, "DETECT", "检测任务"),
    /**
     * 通知任务
     */
    NOTIFY(9, "NOTIFY", "通知任务"),
    /**
     * PAI任务
     */
    PAI(12, "PAI", "PAI任务");

    private final String type;
    private final Integer code;

    public static TaskTypeEnum fromType(String type) {
        return ALL_VALUES.get(type);
    }

    TaskTypeEnum(Integer code, String type, String description) {
        this.code = code;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Integer getCode() {
        return code;
    }

    private static final Map<String, TaskTypeEnum> ALL_VALUES = Maps.newHashMap();

    static {
        for (TaskTypeEnum type : TaskTypeEnum.values()) {
            ALL_VALUES.put(type.getType(), type);
        }
    }
}
