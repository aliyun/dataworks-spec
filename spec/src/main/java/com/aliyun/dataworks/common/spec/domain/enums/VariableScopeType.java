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
 * @author yiwei.qyw
 * @date 2023/7/4
 */
public enum VariableScopeType implements LabelEnum {

    /**
     * Tenant level
     */
    TENANT("Tenant"),

    /**
     * Workspace level
     */
    WORKSPACE("Workspace"),

    /**
     * Workflow level
     */
    FLOW("Workflow"),

    /**
     * Node parameter level
     */
    NODE_PARAMETER("NodeParameter"),

    /**
     * Node context parameter level
     */
    NODE_CONTEXT("NodeContext");

    private final String label;

    VariableScopeType(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}