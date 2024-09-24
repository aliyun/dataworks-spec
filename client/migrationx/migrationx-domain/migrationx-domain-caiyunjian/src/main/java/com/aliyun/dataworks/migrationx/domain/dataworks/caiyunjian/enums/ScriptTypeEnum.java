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
public enum ScriptTypeEnum {
    /**
     * HIVE脚本
     */
    HIVE(1, "HIVE", "HIVE脚本"),
    /**
     * ODPS SQL脚本
     */
    ODPS(2, "ODPS", "ODPS SQL脚本"),
    /**
     * SHELL脚本
     */
    SHELL(3, "SHELL", "shell脚本"),
    /**
     * 同步任务
     */
    CDP(5, "CDP", "CDP 同步任务"),
    /**
     * PERL脚本
     */
    PERL(9, "PERL", "PERL脚本"),
    /**
     * SQL脚本
     */
    SQL(10, "SQL", "SQL脚本"),
    /**
     * PYTHON脚本
     */
    PYTHON(11, "PYTHON", "python 脚本"),
    /**
     * PAI脚本
     */
    PAI(12, "PAI", "Pai脚本"),
    /**
     * 无脚本类型
     */
    NONE(-1, "NONE", "无脚本类型");

    private final String type;
    private final Integer code;

    public static ScriptTypeEnum fromType(String type) {
        return ALL_VALUES.get(type);
    }

    ScriptTypeEnum(Integer code, String type, String description) {
        this.code = code;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Integer getCode() {
        return code;
    }

    private static final Map<String, ScriptTypeEnum> ALL_VALUES = Maps.newHashMap();

    static {
        for (ScriptTypeEnum type : ScriptTypeEnum.values()) {
            ALL_VALUES.put(type.getType(), type);
        }
    }
}
