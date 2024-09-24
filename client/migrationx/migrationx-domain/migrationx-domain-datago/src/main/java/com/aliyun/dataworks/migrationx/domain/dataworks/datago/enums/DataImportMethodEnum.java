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

public enum DataImportMethodEnum {
    /**
     * 普通全量同步
     */
    FULL("FULL"),

    /**
     * 根据数据库字段增全量同步
     */
    DB_TIMESTANMP("DB_TIMESTANMP");

    private String code;

    DataImportMethodEnum(String code) {
        this.code = code;
    }

    public static DataImportMethodEnum fromCode(String code) {
        for (DataImportMethodEnum t : values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

}