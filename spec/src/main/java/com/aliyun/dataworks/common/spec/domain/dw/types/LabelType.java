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

package com.aliyun.dataworks.common.spec.domain.dw.types;

import java.util.Locale;

import lombok.Getter;

/**
 * @author sam.liux
 * @date 2020/11/18
 */
@Getter
public enum LabelType implements LocaleAware {
    DATA_PROCESS("Data Analytics", "数据开发"),
    TABLE("Table", "表"),
    RESOURCE("Resource", "资源"),
    FUNCTION("Function", "函数");

    private final String label;
    private final String labelCn;

    LabelType(String label, String labelCn) {
        this.label = label;
        this.labelCn = labelCn;
    }

    @Override
    public String getDisplayName(Locale locale) {
        if (Locale.SIMPLIFIED_CHINESE.equals(locale)) {
            return labelCn;
        }

        return label;
    }
}
