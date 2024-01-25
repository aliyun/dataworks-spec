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

/**
 * @author sam.liux
 * @date 2019/04/23
 */
public enum AssetType {
    ///**
    // * hive meta
    // */
    //HIVE_META("hive_meta"),

    /**
     * oozie workflow
     */
    OOZIE("oozie"),

    /**
     * airflow dags
     */
    AIRFLOW("airflow"),

    /**
     * aliyun emr workflow
     */
    EMR("emr"),

    /**
     * dataworks export project
     */
    DW_EXPORT("dw_export"),

    /**
     * V1 Migration Export
     */
    V1_EXPORT("v1_export"),

    /**
     * 采云间
     */
    CAIYUNJIAN("caiyunjian"),

    /**
     * DataGO
     */
    DATAGO("datago"),

    /**
     * Azkaban
     */
    AZKABAN("azkaban"),

    /**
     * dolphinscheduler
     */
    DOLPHINSCHEDULER("dolphinscheduler");

    private String type;

    AssetType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static AssetType getAssetType(String type) throws Exception {
        for (AssetType assetType : values()) {
            if (assetType.getType().equalsIgnoreCase(type)) {
                return assetType;
            }
        }
        throw new Exception("unknown asset type: {}" + type);
    }
}
