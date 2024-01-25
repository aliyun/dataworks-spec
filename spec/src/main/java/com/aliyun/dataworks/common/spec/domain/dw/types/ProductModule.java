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

import lombok.Getter;

/**
 * Product module enum type
 *
 * @author 聿剑
 * @date 2023/12/7
 */
@Getter
public enum ProductModule {
    /**
     * Data Studio
     */
    DATA_STUDIO("DataStudio"),

    /**
     * Data Quality
     */
    DATA_QUALITY("DataQuality"),

    /**
     * Data Service
     */
    DATA_SERVICE("DataService"),

    /**
     * Data Catalog
     */
    DATA_CATALOG("DataCatalog"),

    /**
     * Data Source
     */
    DATA_SOURCE("DataSource");

    private final String name;

    ProductModule(String name) {
        this.name = name;
    }
}
