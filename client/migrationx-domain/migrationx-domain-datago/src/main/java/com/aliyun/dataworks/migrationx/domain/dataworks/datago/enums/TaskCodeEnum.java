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

package com.aliyun.dataworks.migrationx.domain.dataworks.datago.enums;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description
 * @createTime 2020-04-07
 */
public enum TaskCodeEnum {
    /**
     * 新版的同步任务-引入任务
     */
    DATA_SYNC_IMPORT(
        "DATA_SYNC_IMPORT", "新版本原子引入任务", "新版本原子引入任务"
    ),
    /**
     * 新版的同步任务-导出任务
     */
    DATA_SYNC_EXPORT(
        "DATA_SYNC_EXPORT", "原子导出任务", "原子导出任务"
    ),
    /**
     * 老版的同步任务-引入任务
     */
    DATA_IMPORT(
        "DATA_IMPORT", "老版本引入任务", "老版本引入入任务"
    ),
    /**
     * 老版的同步任务-导出任务
     */
    DATA_EXPORT(
        "DATA_EXPORT", "导出", "导出任务"
    ),
    /**
     * 虚任务
     */
    VIRTUAL(
        "VIRTUAL", "虚任务", "虚任务"
    ),
    /**
     * ODPS PYTHON 任务
     */
    ODPS_PYTHON(
        "ODPS_PYTHON", "ODPS PYTHON任务", "ODPS PYTHON任务"
    ),
    /**
     * ODPS SHELL 任务
     */
    ODPS_SHELL(
        "ODPS_SHELL", "ODPS SHELL任务", "ODPS SHELL任务"
    ),
    /**
     * ODPS SQL任务
     */
    ODPS_BATCH_SQL(
        "ODPS_BATCH_SQL", "ODPS离线", "ODPS离线SQL任务"
    ),
    ODPS_PERL(
        "ODPS_PERL", "ODPS Perl任务", "ODPS PERL任务"
    ),
    SHELL("SHELL", "Shell任务", "Shell任务");

    private final String code;

    public static TaskCodeEnum fromCode(String code) {
        return ALL_VALUES.get(code);
    }

    public static CodeProgramType toDefaultNodeType(String code) {
        if (DATA_SYNC_IMPORT.code.equals(code)
            || DATA_SYNC_EXPORT.code.equals(code)
            || DATA_IMPORT.code.equals(code)
            || DATA_EXPORT.code.equals(code)
        ) {
            return CodeProgramType.DI;
        } else if (ODPS_BATCH_SQL.code.equalsIgnoreCase(code)) {
            return CodeProgramType.ODPS_SQL;
        } else if (ODPS_PYTHON.code.equalsIgnoreCase(code)) {
            return CodeProgramType.PYODPS;
        } else if (VIRTUAL.code.equalsIgnoreCase(code)) {
            return CodeProgramType.VIRTUAL;
        } else if (ODPS_SHELL.code.equalsIgnoreCase(code)) {
            return CodeProgramType.DIDE_SHELL;
        } else if (ODPS_PERL.code.equalsIgnoreCase(code)) {
            return CodeProgramType.ODPS_PERL;
        } else if (SHELL.code.equalsIgnoreCase(code)) {
            return CodeProgramType.DIDE_SHELL;
        }

        return null;
    }

    TaskCodeEnum(String code, String type, String description) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    private static final Map<String, TaskCodeEnum> ALL_VALUES = Maps.newHashMap();

    static {
        for (TaskCodeEnum type : TaskCodeEnum.values()) {
            ALL_VALUES.put(type.getCode(), type);
        }
    }
}
