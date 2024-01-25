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

package com.aliyun.dataworks.migrationx.transformer.core.report;

/**
 * @author sam.liux
 * @date 2019/05/05
 */
public enum ReportItemType {
    /**
     * Oozie workflow convert to dataworks
     */
    OOZIE_TO_DATAWORKS("Oozie Task to DataWorks Node"),

    /**
     * Hive meta convert to MaxCompute meta
     */
    HIVE_META_TO_MC_META("Hive Meta to MaxCompute Meta"),

    /**
     * Sqoop to dataworks data integration
     */
    SQOOP_TO_DW_DI("Sqoop to Data Integration"),

    /**
     * Hive sql to MaxCompute
     */
    HIVE_SQL_TO_MC_SQL("Hive SQL to MaxCompute SQL"),

    /**
     * Hive sql mapping
     */
    HIVE_SQL_MAPPING("Hive SQL MappingConfig"),

    /**
     * Emr job to dataworks node
     */
    EMR_JOB_TO_DATAWORKS_NODE("Aliyun EMR Job to DataWorks Node"),

    /**
     * Bash to dataworks workflow
     */
    BASH_TO_DATAWORKS("Bash to DataWorks Workflow"),

    /**
     * Upload to dataworks
     */
    UPLOAD_TO_DATAWORKS("Upload to DataWorks"),

    /**
     * unsupported job type
     */
    UNSUPPORTED_JOB_TYPE("Unsupported Job Type"),

    /**
     * Dataworks datasource exists
     */
    DW_DATASOURCE_EXISTS("Datasource Already Exists"),

    /**
     * Dataworks datasource name invalid
     */
    DW_DATASOURCE_NAME_INVALID("Datasource Name Invalid"),

    /**
     * General migration task failure
     */
    TASK_FAILURE("Task Failure"),

    AIRFLOW_DUMP_FAILURE("Airflow Dump Failure"),

    /**
     * Ide file to node
     */
    IDE_EXPORT_FILE_TO_MODEL_NODE("Ide file to Dataworks Mode Node"),

    /**
     * Caiyunjian file to node failure
     */
    CAIYUNJIAN_FILE_TO_NODE_FAILURE("Caiyunjian file to Dataworks node Failure"),

    /**
     * DataGO file to node failure
     */
    DataGO_FILE_TO_NODE_FAILURE("DataGO file to Dataworks node Failure"),

    /**
     * 存在环
     */
    EXIST_CYCLE("Exist cycle"),

    /**
     *
     */
    RESOURCE_FILE_NOT_FOUND("Resource not found");

    private String name;

    ReportItemType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
