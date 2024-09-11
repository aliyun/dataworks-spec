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
public enum SpecKind implements LabelEnum {
    /**
     * Cycle scheduling workflow
     */
    CYCLE_WORKFLOW("CycleWorkflow"),

    /**
     * Manual workflow
     */
    MANUAL_WORKFLOW("ManualWorkflow"),
    /**
     * Single Manual Node
     */
    MANUAL_NODE("ManualNode"),

    /**
     * TemporaryWorkflow
     */
    TEMPORARY_WORKFLOW("TemporaryWorkflow"),
    /**
     * PaiFlow
     */
    PAIFLOW("PaiFlow"),
    /**
     * BatchDeployment
     */
    BATCH_DEPLOYMENT("BatchDeployment"),
    /**
     * DataSource
     */
    DATASOURCE("DataSource"),
    /**
     * DataQuality
     */
    DATA_QUALITY("DataQuality"),
    /**
     * DataService
     */
    DATA_SERVICE("DataService"),
    /**
     * DataCatalog
     */
    DATA_CATALOG("DataCatalog"),
    /**
     * Table
     */
    TABLE("Table"),
    /**
     * spec kind for single Node
     */
    NODE("Node"),
    /**
     * Component
     */
    COMPONENT("Component"),
    /**
     * Resource
     */
    RESOURCE("Resource"),
    /**
     * Function
     */
    FUNCTION("Function"),
    /**
     * Workflow，新Workflow
     */
    WORKFLOW("Workflow");

    private final String label;

    SpecKind(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}