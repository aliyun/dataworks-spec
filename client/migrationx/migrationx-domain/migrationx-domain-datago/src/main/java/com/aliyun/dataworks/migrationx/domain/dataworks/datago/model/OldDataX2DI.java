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

package com.aliyun.dataworks.migrationx.domain.dataworks.datago.model;

import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.DITask;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.enums.CDPTypeEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.MergeSqlUtil;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.enums.DataImportMethodEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.enums.DataSyncTypeEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.enums.TypeofEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.BaseModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.constant.DataXConstants;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.WriterModel.OdpsWriterModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description DataGO老版同步任务
 * @createTime 2020-04-15
 */
@Data
@Slf4j
public class OldDataX2DI {
    @SerializedName("reader_config")
    private Config readerConfig;
    @SerializedName("writer_config")
    private Config writerConfig;
    @SerializedName("data_sync_config")
    private DataSyncConfig dataSyncConfig;
    @SerializedName("data_import_config")
    private DataImportConfig dataImportConfig;
    @SerializedName("input_param")
    private Config inputParam;

    @Data
    public static class Config {
        private String name;
        private String value;

        private JsonObject getSerializeValue() {
            JsonParser parser = new JsonParser();
            return parser.parse(this.value).getAsJsonObject();
        }
    }

    @Data
    public static class DataSyncConfig {
        private String value;

        public DITask.Setting getSerializeSetting() {
            Setting setting = GsonUtils.defaultGson.fromJson(this.value, new TypeToken<Setting>() {
            }.getType());
            DITask.Setting diSetting = new DITask.Setting();
            diSetting.getErrorLimit().setRecord(setting.errorRecordLimit);
            diSetting.getSpeed().setConcurrent(setting.concurrency);
            if (null != setting.speed) {
                diSetting.getSpeed().setMbps(setting.speed);
            }
            return diSetting;
        }

        public DataSyncTypeEnum getDataSyncTypeEnum() {
            Setting setting = GsonUtils.defaultGson.fromJson(this.value, new TypeToken<Setting>() {
            }.getType());
            return setting.getDataSyncTypeEnum();
        }

        @Data
        private static class Setting {
            /**
             * 容错条数
             */
            private Integer errorRecordLimit;
            /**
             * 并发通道数 不能超过20
             */
            private Integer concurrency;
            /**
             * 同步的总速度
             */
            private Integer speed;

            /**
             * 引入类型特殊配置
             */
            private DataSyncTypeEnum dataSyncTypeEnum;
        }
    }

    @Data
    public static class DataImportConfig {
        private String value;

        public DataImportConfigValue getSerializeConfig() {
            return GsonUtils.defaultGson.fromJson(this.value, new TypeToken<DataImportConfigValue>() {
            }.getType());
        }

        @Data
        public static class DataImportConfigValue {
            /**
             * 数据引入方法
             */
            private DataImportMethodEnum dataImportMethodEnum;
            /**
             * 增量Key
             */
            private String incrColumn;
            /**
             * 合并主键
             */
            private List<String> joinKey;
        }
    }

    public DITask.Step toReader() {
        String readerConfig = this.readerConfig.value;
        JsonObject parameter = this.readerConfig.getSerializeValue();
        toDIParameter(parameter, readerConfig);
        if (null != parameter) {
            String type = parameter.get(DI_TYPE).getAsString();
            return new DITask.Step(type, parameter, type, DataXConstants.DI_READER);
        } else {
            return new DITask.Step();
        }
    }

    public DITask.Step toWriter() {
        String writerConfig = this.writerConfig.value;
        JsonObject parameter = this.writerConfig.getSerializeValue();
        toDIParameter(parameter, writerConfig);
        if (null != parameter) {
            String type = parameter.get(DI_TYPE).getAsString();
            return new DITask.Step(type, parameter, type, DataXConstants.DI_WRITER);
        } else {
            return new DITask.Step();
        }
    }

    /**
     * 特殊逻辑处理 天，增全量更新->新增odps_sql任务 小时，增量更新->新增odps_sql任务 实现方式：时间戳增量合并，需要新增where条件
     */
    public List<DwNode> convertMergeNode(DwNode originalNode, String workflow) throws Exception {
        if (null != this.dataImportConfig && StringUtils.isNotBlank(this.dataImportConfig.value)) {
            DataImportConfig.DataImportConfigValue dataImportConfigValue
                = this.dataImportConfig.getSerializeConfig();
            boolean isDaily = false;
            if (originalNode.getCycleType() == 0) {
                isDaily = true;
            }
            OdpsWriterModel odpsWriterModel = GsonUtils.defaultGson.fromJson(this.writerConfig.value,
                new TypeToken<OdpsWriterModel>() {}.getType());
            // 增加where
            DataImportMethodEnum dataImportMethodEnum = dataImportConfigValue.getDataImportMethodEnum();
            if (DataImportMethodEnum.DB_TIMESTANMP.equals(dataImportMethodEnum) && StringUtils.isNotBlank(
                dataImportConfigValue.getIncrColumn())) {
                DITask diTask = GsonUtils.defaultGson.fromJson(originalNode.getCode(), new TypeToken<DITask>() {
                }.getType());
                boolean finalIsDaily = isDaily;
                diTask.getSteps().forEach(step -> {
                    if (DataXConstants.DI_READER.equals(step.getCategory())) {
                        try {
                            step.getParameter().addProperty(DataXConstants.DI_WHERE,
                                MergeSqlUtil.generateWhere(finalIsDaily, odpsWriterModel,
                                    dataImportConfigValue.getIncrColumn()));
                        } catch (Exception e) {
                            log.error("[DataGO] generateWhere exception, nodeName:{}", originalNode.getName(), e);
                        }
                    }
                });
                originalNode.setCode(diTask.toString());
            }
            // 新增odpsSql
            DataSyncTypeEnum dataSyncTypeEnum = this.dataSyncConfig.getDataSyncTypeEnum();
            if ((originalNode.getCycleType() != 0 && DataSyncTypeEnum.INCREMENT.equals(dataSyncTypeEnum)) ||
                (originalNode.getCycleType() == 0 && DataSyncTypeEnum.INCREMENT_FULL.equals(dataSyncTypeEnum))) {
                List<DwNode> nodes = new ArrayList<>();
                // DI节点
                DwNode diNode = copyNode(originalNode);
                diNode.setName(diNode.getName() + "_DI");
                diNode.getOutputs().clear();
                diNode.getOutputs().add(convertNodeIo(diNode.getName(), workflow));
                nodes.add(diNode);
                // ODPS_SQL节点
                DwNode odpsNode = copyNode(originalNode);
                odpsNode.setName(odpsNode.getName() + "_ODPS_SQL");
                odpsNode.setType(CodeProgramType.ODPS_SQL.name());
                String code = MergeSqlUtil.generateMergeSql(isDaily, odpsWriterModel,
                    dataImportConfigValue.getJoinKey());
                odpsNode.setCode(code);
                odpsNode.getInputs().clear();
                odpsNode.getInputs().add(convertNodeIo(diNode.getName(), workflow));
                odpsNode.getOutputs().clear();
                odpsNode.getOutputs().add(convertNodeIo(odpsNode.getName(), workflow));
                nodes.add(odpsNode);
                // 虚节点
                DwNode virtualNode = copyNode(originalNode);
                virtualNode.setCode("");
                virtualNode.setType(CodeProgramType.VIRTUAL.name());
                virtualNode.getInputs().clear();
                virtualNode.getInputs().add(convertNodeIo(odpsNode.getName(), workflow));
                nodes.add(virtualNode);
                return nodes;
            }
        }
        return Collections.singletonList(originalNode);
    }

    private static final String SPLIT_PK = "splitPk";
    private static final String FILTER_SQL = "filterSql";
    private static final String COLUMNS = "columns";
    private static final String PT = "pt";
    private static final String DI_TYPE = "diType";
    private static final String INDEX = "index";
    private static final String TYPE = "type";
    private static final String VALUE = "value";
    private static final String FIELD_DELIMITER = "fieldDelimiter";
    private static final String MARK_DONE_FILE_NAME = "markDoneFileWithAbsolutePath";

    /**
     * DI的parameter转换
     */
    private void toDIParameter(JsonObject parameter, String config) {
        if (StringUtils.isNotBlank(config)) {
            BaseModel baseModel = GsonUtils.defaultGson.fromJson(config, new TypeToken<BaseModel>() {}.getType());
            CDPTypeEnum cdpTypeEnum = baseModel.getResourceType();
            parameter.addProperty(DI_TYPE, cdpTypeEnum.getD2DataXType());
            if (CollectionUtils.isNotEmpty(baseModel.getColumns())) {
                JsonArray columnArr = new JsonArray();
                if (TypeofEnum.OSS_READER.equals(baseModel.getTypedef()) || TypeofEnum.SFTP_READER.equals(
                    baseModel.getTypedef())) {
                    baseModel.getColumns().forEach(column -> {
                        JsonObject object = new JsonObject();
                        object.addProperty(INDEX, column.getIndex());
                        object.addProperty(TYPE, column.getType());
                        object.addProperty(DataXConstants.DI_COLUMN_NAME, column.getIndex());
                        columnArr.add(object);
                    });
                } else {
                    baseModel.getColumns().forEach(column -> columnArr.add(column.getName()));
                }
                parameter.add(DataXConstants.DI_COLUMN, columnArr);
                parameter.remove(COLUMNS);
            }
            parameter.addProperty(DataXConstants.DI_DATASOURCE, baseModel.getDatasourceName());

            if (parameter.has(DataXConstants.TABLE_NAME)) {
                String tableName = parameter.get(DataXConstants.TABLE_NAME).getAsString();
                String[] parts = StringUtils.split(tableName, ".");
                if (parts != null && parts.length > 1) {
                    tableName = parts[parts.length - 1];
                }

                parameter.addProperty(DataXConstants.DI_TABLE, tableName);
                parameter.remove(DataXConstants.TABLE_NAME);
            }
            if (parameter.has(FILTER_SQL)) {
                parameter.addProperty(DataXConstants.DI_WHERE, parameter.get(FILTER_SQL).getAsString());
                parameter.remove(FILTER_SQL);
            }
            if (parameter.has(SPLIT_PK)) {
                parameter.addProperty(DataXConstants.DI_SPLIT_PK, parameter.get(SPLIT_PK).getAsString());
                parameter.remove(SPLIT_PK);
            }
            if (TypeofEnum.MYSQL_READER.equals(baseModel.getTypedef()) && parameter.has(DataXConstants.TABLE_NAME)) {
                DITask.Connection connection = new DITask.Connection();
                connection.setDatasource(baseModel.getDatasourceName());
                connection.setTable(parameter.get(DataXConstants.TABLE_NAME).getAsString().split(","));
                List<DITask.Connection> connectionList = new ArrayList<>();
                connectionList.add(connection);
                parameter.add(DataXConstants.DI_CONNECTION, GsonUtils.defaultGson.toJsonTree(connectionList));
            }
            if (parameter.has(PT)) {
                String param = parameter.get(PT).getAsString();
                if (param.contains("hour") && !param.contains("hour=")) {
                    param = param.replace("hour", "hour=@@[HH-1h]");
                }
                if (TypeofEnum.ODPS_READER.equals(baseModel.getTypedef()) && StringUtils.isNotBlank(param)) {
                    JsonArray partition = new JsonArray();
                    if (StringUtils.isNotBlank(param)) {
                        Arrays.stream(param.split(",")).forEach(s -> {
                            if (!s.contains("=") && s.contains("hour")) {
                                partition.add("hour=@@[HH-1h]");
                            } else {
                                partition.add(s);
                            }
                        });
                    }
                    parameter.add(DataXConstants.DI_PARTITION, partition);
                } else {
                    parameter.addProperty(DataXConstants.DI_PARTITION, param);
                }
                parameter.remove(PT);
            }
            if (parameter.has(DataXConstants.WRITE_NODE)) {
                String writeMode = parameter.get(DataXConstants.WRITE_NODE).getAsString();
                if (DataXConstants.INSERT.equals(writeMode)) {
                    parameter.addProperty(DataXConstants.WRITE_NODE, DataXConstants.DI_INSERT);
                } else if (DataXConstants.UPDATE.equals(writeMode)) {
                    parameter.addProperty(DataXConstants.WRITE_NODE, DataXConstants.DI_UPDATE);
                } else if (DataXConstants.REPLACE.equals(writeMode)) {
                    parameter.addProperty(DataXConstants.WRITE_NODE, DataXConstants.DI_REPLACE);
                }
            }
            if (parameter.has(FIELD_DELIMITER)) {
                String fieldDelimiter = parameter.get(FIELD_DELIMITER).getAsString();
                parameter.addProperty(DataXConstants.DI_FIELD_DELIMITER_ORIGIN, StringEscapeUtils.escapeJava(fieldDelimiter));
                if (fieldDelimiter.length() > 1 && !parameter.has(DataXConstants.DI_FILE_FORMAT)) {
                    parameter.addProperty(DataXConstants.DI_FILE_FORMAT, "text");
                }
            }
            if (parameter.has(MARK_DONE_FILE_NAME)) {
                parameter.addProperty(DataXConstants.DI_MARK_DONE_FILE_NAME, parameter.get(MARK_DONE_FILE_NAME).getAsString());
            }
        }
    }

    private DwNode copyNode(DwNode originNode) {
        return GsonUtils.gson.fromJson(GsonUtils.gson.toJson(originNode), new TypeToken<DwNode>() {}.getType());
    }

    private NodeIo convertNodeIo(String name, String workflowName) {
        NodeIo output = new NodeIo();
        output.setParseType(1);
        if (name.contains(".")) {
            output.setData(name);
        } else {
            output.setData(Joiner.on(".").join(workflowName, name));
        }
        return output;
    }

}
