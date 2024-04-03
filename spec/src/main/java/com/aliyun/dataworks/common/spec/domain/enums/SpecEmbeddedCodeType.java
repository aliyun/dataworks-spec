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

package com.aliyun.dataworks.common.spec.domain.enums;

import com.aliyun.dataworks.common.spec.domain.interfaces.LabelEnum;

/**
 * 嵌入式函数代码类型
 *
 * @author 聿剑
 * @date 2024/3/16
 */
public enum SpecEmbeddedCodeType implements LabelEnum {
    /**
     * python2
     */
    PYTHON2("python2"),

    /**
     * python3
     */
    PYTHON3("python3"),

    /**
     * java8
     */
    JAVA8("java8"),

    /**
     * java11
     */
    JAVA11("java11"),

    /**
     * java17
     */
    JAVA17("java17");

    private final String label;

    SpecEmbeddedCodeType(String label) {this.label = label;}

    @Override
    public String getLabel() {
        return label;
    }
}
