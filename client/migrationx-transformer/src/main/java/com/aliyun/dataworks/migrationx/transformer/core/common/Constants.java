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

package com.aliyun.dataworks.migrationx.transformer.core.common;

import java.io.File;

/**
 * @author sam.liux
 * @date 2019/04/23
 */
public class Constants {
    public static final String WORKFLOW_XML = "workflow.xml";
    public static final String WORKFLOWS_DIR = "workflows";
    public static final String ASSETS_DIR = "assets";
    public static final String TABLES_DIR = "tables";
    public static final String RESOURCES_DIR = "resources";
    public static final String SRC_DIR_PRJ_RELATED = "." + File.separator + "src";
    public static final String WORKFLOWS_DIR_PRJ_RELATED = SRC_DIR_PRJ_RELATED + File.separator + WORKFLOWS_DIR;
    public static final String ASSETS_DIR_PRJ_RELATED = SRC_DIR_PRJ_RELATED + File.separator + ASSETS_DIR;
    public static final String TABLES_DIR_PRJ_RELATED = SRC_DIR_PRJ_RELATED + File.separator + "tables";
    public static final String RESOURCES_DIR_PRJ_RELATED = SRC_DIR_PRJ_RELATED + File.separator + "resources";
    public static final Object PROJECT_XML_PRG_RELATED = File.separator + "project.xml";
    public static final String NODES_DIR = "nodes";
    public static final String VAR_DIR = "var";

    public static final String IDE_URL_PROPERTY_KEY = "ide.url";
    public static final String TENANT_URL_PROPERTY_KEY = "tenant.url";
    public static final String META_SERVICE_URL_PROPERTY_KEY = "metaservice.url";
    public static final String BASE_KEY_PROPERTY_KEY = "base.key";
    public static final String BASE_TOKEN_PROPERTY_KEY = "base.token";
    public static final String UPLOAD_FORCE_UPDATE = "upload.forceUpdate";
    public static final String UPLOAD_CONCURRENT = "upload.concurrent";
    public static final String BASH_WORKFLOW_PREFIX = "bash_";

    public static final String TRANSLATOR_HIVE_SQL_REWRITE_ENABLE = "translator.hive.sql.rewrite.enable";
    public static final String TRANSLATOR_HIVE_SQL_REWRITE_CONFIG_JSON = "mappings.config.json";
    public static final String TRANSLATOR_HIVE_SQL_TO_MC_ENABLE = "hiveSql.convert.enable";
    public static final String TRANSLATOR_SQOOP_TO_DATAX_ENABLE = "translator.sqoop.to.datax.enable";
    public static final String TRANSLATOR_SPARK_SUBMIT_ENABLE = "translator.spark.submit.enable";
    public static final String IDE_EXPORT_TASK = "ide_export_task";
    public static final String IDE_EXPORT_TASK_DOWNLOAD = "exported.zip";

    public static final String PROPERTIES_CONVERTER_PERL2SHELL_ENABLE = "workflow.converter.perl2shell.enable";
    public static final String PROPERTIES_CONVERTER_DETECT2SHELL_ENABLE = "workflow.converter.detect2shell.enable";
    public static final String PROPERTIES_CONVERTER_DETECT_TASK_STOP_AT = "workflow.converter.detectTask.stopAt";
    public static final String PROPERTIES_CONVERTER_PERL2SHELL_PERL_BIN = "workflow.converter.perl2shell.perlBin";
    public static final String PROPERTIES_CONVERTER_PERL2SHELL_PERL_INCLUDE_PATHS
        = "workflow.converter.perl2shell.perlIncludePaths";

    public static final String CONVERTER_TARGET_SQL_NODE_TYPE_AS = "workflow.converter.sqlNodeType";
    public static final String CONVERTER_TARGET_MR_NODE_TYPE_AS = "workflow.converter.mrNodeType";
    public static final String CONVERTER_TARGET_SPARK_SUBMIT_TYPE_AS = "workflow.converter.sparkSubmitAs";
    public static final String CONVERTER_TARGET_UNSUPPORTED_NODE_TYPE_AS
        = "workflow.converter.target.unknownNodeTypeAs";
    public static final String CONVERTER_TARGET_ENGINE_TYPE = "workflow.converter.target.engine.type";
    public static final String CONVERTER_TARGET_ENGINE_DATASOURCE_NAME
        = "workflow.converter.target.engine.datasource.name";
    public static final String CONVERTER_TARGET_ENGINE_DATASOURCE_TYPE
        = "workflow.converter.target.engine.datasource.type";
    public static final String CONVERTER_TARGET_SHELL_NODE_TYPE_AS = "workflow.converter.shellNodeType";
    public static final String UNDEFINED_VARIABLE_VALUE = "UNDEFINED_VARIABLE_VALUE";
    public static final String OBJECT_CONFLICT_RESOLVE_STRATEGY = "object.conflict.resolve.strategy";
    public static final String WORKFLOW_PARAMETER_CONFLICT_RESOLVE_STRATEGY
        = "workflow.parameter.conflict.resolve.strategy";
    public static final String CONVERTER_ALIYUN_EMR_TO_DATAWORKS_NODE_TYPE_MAP
        = "workflow.converter.aliyunEmr.nodeTypeMapping";
    public static final String CONVERTER_ALIYUN_EMR_TO_DATAWORKS_JOB_SUBMIT_MODE
        = "workflow.converter.aliyunEmr.jobSubmitMode";
    public static final String CONVERTER_ALIYUN_EMR_TO_DATAWORKS_REUSE_SESSION
        = "workflow.converter.aliyunEmr.reuseSession";
    public static final String CONVERTER_ALIYUN_EMR_TO_DATAWORKS_START_RIGHT_NOW
        = "workflow.converter.aliyunEmr.startRightNow";
    public static final String CONVERTER_ALIYUN_DATAGO_TO_DATAWORKS_NODE_TYPE_MAP
        = "workflow.converter.datago.nodeTypeMapping";
    public static final String CONVERTER_ALIYUN_CAIYUNJIAN_TO_DATAWORKS_NODE_TYPE_MAP
        = "workflow.converter.caiyunjian.nodeTypeMapping";

    // Airflow
    public static final String CONVERTER_SPARK_SUBMIT_TYPE_AS = "workflow.converter.sparkSubmitAs";

    // 设置通过命令行跑的sql任务转为什么类型的节点
    public static final String CONVERTER_TARGET_COMMAND_SQL_TYPE_AS = "workflow.converter.commandSqlAs";
    public static final String CONVERTER_TARGET_SCHEDULE_RES_GROUP_IDENTIFIER
        = "workflow.converter.target.schedule.resGroupIdentifier";

    public static final String WORKFLOW_DATASTUDIO_UPLOADER_AUTO_COMMIT = "workflow.datastudio.uploader.autoCommit";
    public static final String WORKFLOW_DATASTUDIO_UPLOADER_AUTO_DEPLOY = "workflow.datastudio.uploader.autoDeploy";
    public static final String EXPORTER_ALIYUN_EMR_FLOW_LIST = "exporter.aliyun.emr.projectFlowList";
    public static final String CUSTOM_EXPORT_EXCEL_FILE_OSS_KEY = "custom.export.excel.file.oss.key";
    public static final String CUSTOM_IMPORT_MAPPING_EXCEL_FILE_OSS_KEY = "custom.import.excel.file.oss.key";
    public static final String CUSTOM_IMPORT_MAPPING_ITEMS_TXT_FILE_OSS_KEY = "custom.import.items.txt.file.oss.key";

    public static final String EXPORTER_ALIYUN_EMR_PROJECT_NAMES = "exporter.aliyun.emr.projectNames";

    public static final String CONVERTER_DOLPHINSCHEDULER_TO_DATAWORKS_NODE_TYPE_MAP
        = "workflow.converter.dolphinscheduler.nodeTypeMapping";
    public static final String CONVERTER_TARGET_SQL_NODE_TYPE_MAP
        = "workflow.converter.dolphinscheduler.sqlNodeTypeMapping";
}