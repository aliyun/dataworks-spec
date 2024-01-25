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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2;

/**
 * 文件夹的枚举类型
 *
 * @author luojian.lj
 * @since 2017-08-30
 */
public enum IdeFolderSubType {

    NORMAL(0, "normal"),
    INNER(1, "inner"),

    ;

    private Integer code;
    private String alias;

    private IdeFolderSubType(Integer code, String alias) {
        this.code = code;
        this.alias = alias;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getAlias() {
        return this.alias;
    }

    /**
     * 通过code来获取枚举对象
     *
     * @param code
     * @return
     */
    public static IdeFolderSubType getByCode(Integer code) {
        for (IdeFolderSubType type : IdeFolderSubType.values()) {
            if (code.equals(type.getCode())) {
                return type;
            }
        }
        return null;
    }

    public static IdeFolderSubType getByAlias(String alias) {
        for (IdeFolderSubType type : IdeFolderSubType.values()) {
            if (alias.equals(type.getAlias())) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return code.toString();
    }
}
