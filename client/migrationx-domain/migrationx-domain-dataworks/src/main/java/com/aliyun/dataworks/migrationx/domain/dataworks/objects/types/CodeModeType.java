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

import lombok.Getter;

/**
 * @author sam.liux
 * @date 2020/01/09
 */
@Getter
public enum CodeModeType {
    /**
     * 向导模式
     */
    WIZARD("wizard"),

    /**
     * 脚本模式
     */
    CODE("code");

    private final String value;

    CodeModeType(String value) {
        this.value = value;
    }

    public static CodeModeType getCodeModeByValue(String value) {
        for (CodeModeType codeModeType : values()) {
            if (codeModeType.getValue().equalsIgnoreCase(value)) {
                return codeModeType;
            }
        }
        throw new RuntimeException("unknown code mode: " + value);
    }
}
