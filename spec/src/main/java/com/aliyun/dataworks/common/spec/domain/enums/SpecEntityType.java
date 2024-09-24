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
 * @author 聿剑
 * @date 2024/9/23
 */
public enum SpecEntityType implements LabelEnum {
    /**
     * @see com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow
     */
    WORKFLOW("Workflow"),

    /**
     * @see com.aliyun.dataworks.common.spec.domain.ref.SpecNode
     */
    NODE("Node"),

    /**
     * @see com.aliyun.dataworks.common.spec.domain.ref.SpecFileResource
     */
    RESOURCE("Resource"),

    /**
     * @see com.aliyun.dataworks.common.spec.domain.ref.SpecFunction
     */
    FUNCTION("Function"),

    /**
     * @see com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent
     */
    COMPONENT("Component");

    private final String label;

    SpecEntityType(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
