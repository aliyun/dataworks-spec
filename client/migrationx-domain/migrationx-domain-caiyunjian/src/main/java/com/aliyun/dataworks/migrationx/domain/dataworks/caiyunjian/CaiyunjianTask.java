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

package com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian;

import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.enums.CDPTypeEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.enums.ScheduleTypeEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.enums.ScriptTypeEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.enums.TaskTypeEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.constant.DataXConstants;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.CodeModeType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.aliyun.dataworks.migrationx.domain.dataworks.utils.StringUtils.replaceControlCharacterPrintable;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description 采云间任务解析
 * @createTime 2020-04-03
 */
@Data
@Slf4j
public class CaiyunjianTask {
    private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    private CdfTaskInfo cdfTaskInfo;
    private CdpInfoDO cdpInfoDO;
    private TaskDefineDO taskDefineDO;
    private TaskDetectInfoDo taskDetectInfoDO;
    private Boolean success = Boolean.FALSE;
    private List<String> dirPath;

    /**
     * 调度层配置
     */
    @Data
    public static class CdfTaskInfo {
        private String activityType;
        private String cronExpression;
        private Integer dependentType;
        private String description;
        private String endEffectDate;
        private String startEffectDate;
        private String param;
        private Integer priority;
        private Long resourceGroupId;
        private String fileContent;
        private List<ParentActivity> parentActivities;

        @Data
        public static class ParentActivity {
            private String name;
            private Integer relationType;
        }
    }

    /**
     * 检测任务配置
     */
    @Data
    public static class TaskDetectInfoDo {
        private String expiryAction; //  "fail"
        private String expiryTime; // "24",
        private String expiryTimeUnit; // "hour",
        private Long id; // 1912,
        private Boolean isDeleted; // false,
        private String setting;
        // "{\"datasourceId\":\"21l\",\"datasourceName\":\"bdp_ods_sftp_01\",\"datasourceType\":\"sftp\",
        // \"fileName\":\"dbank_@@{yyyyMMdd}_end\",\"path\":\"input_ftp/dbank\"}",
        private Long signMethodId; // 1,
        private Integer type; // 3

        @Data
        public static class Setting {
            private Long datasourceId;
            private String datasourceName;
            private String datasourceType; // "sftp"
            private String fileName; // "dbank_@@{yyyyMMdd}_end"
            private String path; // "input_ftp/dbank"
        }

        public Setting getSetting() {
            if (StringUtils.isBlank(setting)) {
                return new Setting();
            }

            Setting settingObj = GsonUtils.fromJsonString(setting, new TypeToken<Setting>() {}.getType());
            if (settingObj == null) {
                return new Setting();
            }

            return settingObj;
        }
    }

    /**
     * 同步任务配置
     */
    @Data
    public static class CdpInfoDO {
        private String readerContext;
        private String readerResourceType;
        private String writerContext;
        private String writerResourceType;
        private String setting;
        private Setting serializeSetting;
        private JsonObject serializeReaderContext;
        private JsonObject serializeWriterContext;
        private String transformer;

        public Setting getSerializeSetting() {
            return GsonUtils.defaultGson.fromJson(this.setting, new TypeToken<Setting>() {}.getType());
        }

        public JsonObject getSerializeReaderContext() {
            return new JsonParser().parse(this.readerContext).getAsJsonObject();
        }

        public JsonObject getSerializeWriterContext() {
            return new JsonParser().parse(this.writerContext).getAsJsonObject();
        }

        @Data
        public static class Setting {
            private Integer channelSpeed;
            private Double errorLimit;
            private Double speed;
        }

        @Data
        public static class TableInfo {
            private String resourceName;
            private String tableName;
        }
    }

    /**
     * 采云间JSS任务配置
     */
    @Data
    public static class TaskDefineDO {
        @SerializedName("activitytype")
        private String activityType;
        @SerializedName("dependenttype")
        private Integer dependentType;
        private String parameter;
        private Integer priority;
        private String scheduleType;
        @SerializedName("scheduleexp")
        private String scheduleExp;
        private Integer scriptType;
        private Integer taskType;
        private String taskName;
        private String owner;
    }

    /**
     * 转换成Dataworks的Node
     */
    public DwNode toNode(String projectName) throws ParseException {
        DwNode dwNode = new DwNode();
        // type
        getNodeType(dwNode);
        // info
        convertBasicInfo(dwNode);
        // cron
        dwNode.setCycleType(ScheduleTypeEnum.toCycleType(this.taskDefineDO.getScheduleType()).getCode());
        dwNode.setCronExpress(this.taskDefineDO.getScheduleExp());
        // dependent
        convertDependent(dwNode, projectName);
        this.success = Boolean.TRUE;
        return dwNode;
    }

    /**
     * 获取节点类型&code
     */
    private void getNodeType(DwNode dwNode) {
        String nodeType = "";
        if (TaskTypeEnum.CDP.getCode().equals(taskDefineDO.getTaskType())) {
            nodeType = CodeProgramType.DI.name();
            DITask diTask = toDITask(cdpInfoDO);
            JsonObject extend = new JsonObject();
            if (ListUtils.emptyIfNull(diTask.getSteps()).stream().anyMatch(
                st -> DataXConstants.DI_TRANSFORMER.equals(st.getCategory()))) {
                extend.addProperty("mode", CodeModeType.CODE.getValue());
            } else {
                extend.addProperty("mode", CodeModeType.WIZARD.getValue());
            }
            diTask.setExtend(extend);
            dwNode.setCode(diTask.toString());
            dwNode.setExtend(GsonUtils.toJsonString(extend));
        } else if (TaskTypeEnum.ODPS.getCode().equals(taskDefineDO.getTaskType())) {
            if (ScriptTypeEnum.HIVE.getCode().equals(taskDefineDO.getScriptType())) {
                nodeType = CodeProgramType.HIVE.name();
            } else if (ScriptTypeEnum.ODPS.getCode().equals(taskDefineDO.getScriptType())) {
                nodeType = CodeProgramType.ODPS_SQL.name();
            } else if (ScriptTypeEnum.SHELL.getCode().equals(taskDefineDO.getScriptType())) {
                nodeType = CodeProgramType.DIDE_SHELL.name();
            } else if (ScriptTypeEnum.PERL.getCode().equals(taskDefineDO.getScriptType())) {
                nodeType = CodeProgramType.PERL.name();
            } else if (ScriptTypeEnum.PYTHON.getCode().equals(taskDefineDO.getScriptType())) {
                nodeType = CodeProgramType.PYODPS.name();
            }
            dwNode.setCode(cdfTaskInfo.getFileContent());
        } else if (TaskTypeEnum.NOTIFY.getCode().equals(taskDefineDO.getTaskType())) {
            nodeType = CodeProgramType.DIDE_SHELL.name();
            dwNode.setCode(cdfTaskInfo.getFileContent());
        } else if (TaskTypeEnum.VIRTUAL.getCode().equals(taskDefineDO.getTaskType())) {
            nodeType = CodeProgramType.VIRTUAL.name();
        } else {
            nodeType = CodeProgramType.DIDE_SHELL.name();
            dwNode.setCode(cdfTaskInfo.getFileContent());
        }
        dwNode.setType(nodeType);
    }

    /**
     * 同步脚本转换成DI
     */
    public static DITask toDITask(CdpInfoDO cdpInfoDO) {
        DITask diTask = new DITask();
        String readerType = CDPTypeEnum.fromCyjType(cdpInfoDO.readerResourceType).getD2DataXType();
        DITask.Step readerStep = toDIParameter(readerType, cdpInfoDO.getSerializeReaderContext(), DataXConstants.DI_READER);
        if (readerStep != null) {
            diTask.getSteps().add(readerStep);
        }

        List<DITask.Step> transformerSteps = toTransformers(cdpInfoDO.transformer, readerStep);
        if (CollectionUtils.isNotEmpty(transformerSteps)) {
            diTask.getSteps().addAll(transformerSteps);
        }

        String writerType = CDPTypeEnum.fromCyjType(cdpInfoDO.writerResourceType).getD2DataXType();
        DITask.Step writerStep = toDIParameter(writerType, cdpInfoDO.getSerializeWriterContext(), DataXConstants.DI_WRITER);
        if (writerStep != null) {
            diTask.getSteps().add(writerStep);
        }

        if (cdpInfoDO.getSerializeSetting() != null) {
            if (cdpInfoDO.getSerializeSetting().getErrorLimit() != null) {
                Integer errorLimit = (int)Math.round(cdpInfoDO.getSerializeSetting().getErrorLimit());
                diTask.getSetting().getErrorLimit().setRecord(errorLimit);
            }

            if (cdpInfoDO.getSerializeSetting().getSpeed() != null &&
                cdpInfoDO.getSerializeSetting().getChannelSpeed() != null &&
                cdpInfoDO.getSerializeSetting().getChannelSpeed() > 0) {
                diTask.getSetting().getSpeed().setThrottle(Boolean.TRUE);
                int concurrent = Math.max(1,
                    (int)(cdpInfoDO.getSerializeSetting().getSpeed() / cdpInfoDO.getSerializeSetting()
                        .getChannelSpeed()));
                diTask.getSetting().getSpeed().setConcurrent(concurrent);
                diTask.getSetting().getSpeed().setMbps(cdpInfoDO.getSerializeSetting().getChannelSpeed());
            }
        }

        rewriteTaskOrder(diTask);
        return diTask;
    }

    private static void rewriteTaskOrder(DITask diTask) {
        if (CollectionUtils.isEmpty(diTask.getSteps())) {
            return;
        }

        if (diTask.getOrder() == null) {
            return;
        }

        List<DITask.Step> steps = diTask.getSteps();
        DITask.Order order = diTask.getOrder();
        List<DITask.Order.Hop> hops = new ArrayList<>();
        for (int i = 0; i < steps.size() - 1; i++) {
            DITask.Step stepFrom = steps.get(i);
            DITask.Step stepTo = steps.get(i + 1);
            DITask.Order.Hop hop = new DITask.Order.Hop();
            hop.setFrom(stepFrom.getName());
            hop.setTo(stepTo.getName());
            hops.add(hop);
        }

        if (CollectionUtils.isNotEmpty(hops)) {
            order.setHops(hops);
        }
    }

    private static List<DITask.Step> toTransformers(String transformer,
        DITask.Step readerStep) {
        if (StringUtils.isNotBlank(transformer)) {
            List<JsonObject> transformerJsonArray = GsonUtils.fromJsonString(transformer,
                new TypeToken<List<JsonObject>>() {}.getType());
            if (transformerJsonArray != null) {
                return transformerJsonArray.stream()
                    .map(tr -> convertTransformer(tr, readerStep))
                    .filter(Objects::nonNull).collect(Collectors.toList());
            }
        }
        return null;
    }

    private static DITask.Step convertTransformer(JsonObject t,
        DITask.Step readerStep) {
        if (!t.has("name")) {
            return null;
        }

        String name = t.get("name").getAsString();
        if ("dx_filter".equals(name)) {
            return convertDxFilterTransformer(t, readerStep);
        }
        return null;
    }

    private static DITask.Step convertDxFilterTransformer(JsonObject t, DITask.Step readerStep) {
        String name = t.get("name").getAsString();
        JsonArray parameter = t.get("parameter").getAsJsonArray();
        JsonObject stepParameter = new JsonObject();

        if (parameter.size() > 0) {
            String columnName = parameter.get(0).getAsString();
            if (readerStep.getParameter() != null) {
                JsonObject readerParameter = readerStep.getParameter();
                if (readerParameter.has(DataXConstants.DI_COLUMN)) {
                    JsonArray columnArr = readerParameter.get(DataXConstants.DI_COLUMN).getAsJsonArray();
                    Iterator<JsonElement> itr = columnArr.iterator();
                    int index = 0;
                    while (itr.hasNext()) {
                        String col = itr.next().getAsString();
                        if (StringUtils.equals(columnName, col)) {
                            stepParameter.addProperty("columnIndex", index);
                            parameter.remove(0);
                            stepParameter.add("paras", parameter);
                            return new DITask.Step(
                                DataXConstants.DI_TRANSFORMER,
                                stepParameter,
                                name,
                                DataXConstants.DI_TRANSFORMER);
                        }
                        index++;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 不同类型reader，writer解析
     */
    private static DITask.Step toDIParameter(String type, JsonObject parameter, String category) {
        if (parameter == null) {
            return null;
        }

        if (parameter.has(DataXConstants.RESOURCE_INFO)) {
            JsonObject resourceInfo = parameter.getAsJsonObject(DataXConstants.RESOURCE_INFO);
            if (null != resourceInfo && resourceInfo.has(DataXConstants.RESOURCE_NAME)) {
                parameter.add(DataXConstants.DI_DATASOURCE, resourceInfo.get(DataXConstants.RESOURCE_NAME));
            }
            if (null != resourceInfo && resourceInfo.has(DataXConstants.OBJECT_NAME)) {
                String tableName = resourceInfo.get(DataXConstants.OBJECT_NAME).getAsString();
                String[] parts = StringUtils.split(tableName, ".");
                if (parts != null && parts.length > 1) {
                    tableName = parts[parts.length - 1];
                }

                parameter.addProperty(DataXConstants.DI_TABLE, tableName);
            }
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
        if (DataXConstants.READER.equals(category) && CDPTypeEnum.ODPS.getD2DataXType().equals(type)) {
            if (parameter.has(DataXConstants.DI_PARTITION)) {
                String param = parameter.get(DataXConstants.DI_PARTITION).getAsString();
                JsonArray partition = new JsonArray();
                if (StringUtils.isNotBlank(param)) {
                    partition.add(param);
                }
                parameter.add(DataXConstants.DI_PARTITION, partition);
            }
        }
        if (DataXConstants.READER.equals(category) && parameter.has(DataXConstants.TABLE_INFO) && CDPTypeEnum.MYSQL.getD2DataXType().equalsIgnoreCase(
            type)) {
            List<CdpInfoDO.TableInfo> tableInfos = GsonUtils.defaultGson.fromJson(
                parameter.get(DataXConstants.TABLE_INFO), new TypeToken<List<CdpInfoDO.TableInfo>>() {}.getType());
            List<DITask.Connection> connections = tableInfos.stream()
                .map(tableInfo -> {
                    DITask.Connection connection = new DITask.Connection();
                    connection.setDatasource(tableInfo.resourceName);
                    connection.setTable(tableInfo.tableName.split(","));
                    return connection;
                }).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(connections)) {
                parameter.add(DataXConstants.DI_CONNECTION, GsonUtils.defaultGson.toJsonTree(connections));
                parameter.remove(DataXConstants.DI_TABLE);
                parameter.remove(DataXConstants.DI_DATASOURCE);
                parameter.remove(DataXConstants.TABLE_INFO);
            }
        }
        processGuidTable(parameter);
        processResourceReader(type, parameter, category);
        processResourceWriter(type, parameter, category);
        processColumnInfo(type, parameter, category);
        return new DITask.Step(type, parameter, type, category);
    }

    private static void processResourceReader(String type, JsonObject parameter, String category) {
        if (!DataXConstants.DI_READER.equalsIgnoreCase(category)) {
            return;
        }

        Set<CDPTypeEnum> resourceReaderTypes = new HashSet<>(Arrays.asList(CDPTypeEnum.OSS, CDPTypeEnum.SFTP));
        if (resourceReaderTypes.stream().noneMatch(t -> t.getD2DataXType().equalsIgnoreCase(type))) {
            return;
        }

        resourceReaderTypes.stream().filter(t -> t.getD2DataXType().equalsIgnoreCase(type)).findFirst().ifPresent(
            resType -> {
                switch (resType) {
                    case OSS: {
                        String objectName = parameter.has(DataXConstants.OBJECT_NAME) && parameter.get(DataXConstants.OBJECT_NAME) != null ?
                            parameter.get(DataXConstants.OBJECT_NAME).getAsString() : null;
                        String filePrefix = parameter.has(DataXConstants.FILE_PREFIX) && parameter.get(DataXConstants.FILE_PREFIX) != null ?
                            parameter.get(DataXConstants.FILE_PREFIX).getAsString() : null;

                        if (StringUtils.isNotBlank(objectName) && StringUtils.isNotBlank(filePrefix)) {
                            String ossObject = Joiner.on("/").join(objectName, filePrefix);
                            ossObject = Joiner.on("/").join(Arrays.stream(ossObject.split("/"))
                                .map(StringUtils::trim)
                                .filter(StringUtils::isNotBlank)
                                .collect(Collectors.toList()));
                            parameter.addProperty(DataXConstants.DI_OSS_OBJECT, ossObject);
                        }
                        break;
                    }
                    case SFTP: {
                        if (parameter.has(DataXConstants.DI_PATH) && StringUtils.isNotBlank(parameter.get(DataXConstants.DI_PATH).getAsString())) {
                            JsonArray arr = new JsonArray();
                            arr.add(parameter.get(DataXConstants.DI_PATH).getAsString());
                            parameter.add(DataXConstants.DI_PATH, arr);
                            break;
                        }
                    }
                    default:
                }
            });

        processFieldDelimiter(parameter);
    }

    private static void processColumnInfo(String type, JsonObject parameter, String category) {
        if (!parameter.has(DataXConstants.COLUMN_INFO_LIST)) {
            return;
        }

        if (DataXConstants.WRITER.equals(category)) {
            if (!Arrays.asList(CDPTypeEnum.OSS.getD2DataXType(), CDPTypeEnum.SFTP.getD2DataXType()).contains(type)) {
                return;
            }

            JsonArray columns = parameter.get(DataXConstants.COLUMN_INFO_LIST).getAsJsonArray();
            JsonArray d2Columns = new JsonArray();
            Iterator<JsonElement> itr = columns.iterator();
            while (itr.hasNext()) {
                JsonObject column = itr.next().getAsJsonObject();
                if (column.has("colIndex")) {
                    d2Columns.add(String.valueOf(column.get("colIndex").getAsString()));
                }
            }
            parameter.add(DataXConstants.DI_COLUMN, d2Columns);
        }

        if (DataXConstants.READER.equals(category)) {
            JsonArray columns = parameter.get(DataXConstants.COLUMN_INFO_LIST).getAsJsonArray();
            JsonArray d2Columns = new JsonArray();
            if (Arrays.asList(CDPTypeEnum.OSS.getD2DataXType(), CDPTypeEnum.SFTP.getD2DataXType()).contains(type)) {
                Integer preIndex = null;
                for (JsonElement jsonElement : columns) {
                    JsonObject column = jsonElement.getAsJsonObject();
                    JsonObject d2Column = new JsonObject();
                    Integer index = column.has("colIndex") ? column.get("colIndex").getAsInt() : null;
                    if (preIndex != null && index == null) {
                        index = preIndex + 1;
                    }

                    d2Column.addProperty("type", "string");
                    if (column.has("colIndex")) {
                        d2Column.addProperty("name", index);
                        d2Column.addProperty("index", index);
                    } else {
                        // missing colIndex with name like @@{yyyymmdd}
                        d2Column.addProperty("index", index);
                        d2Column.addProperty("value", column.get("name").getAsString());
                        d2Column.addProperty("name", column.get("name").getAsString());
                    }
                    preIndex = index;
                    d2Columns.add(d2Column);
                }
            } else {
                for (JsonElement jsonElement : columns) {
                    JsonObject column = jsonElement.getAsJsonObject();
                    d2Columns.add(String.valueOf(column.get("name").getAsString()));
                }
            }
            parameter.add(DataXConstants.DI_COLUMN, d2Columns);
        }

        parameter.remove(DataXConstants.COLUMN_INFO_LIST);
    }

    private static void processResourceWriter(String type, JsonObject parameter, String category) {
        if (!DataXConstants.DI_WRITER.equalsIgnoreCase(category)) {
            return;
        }

        Set<CDPTypeEnum> resourceWriteTypes = new HashSet<>(Arrays.asList(CDPTypeEnum.OSS, CDPTypeEnum.SFTP));
        if (resourceWriteTypes.stream().noneMatch(t -> t.getD2DataXType().equalsIgnoreCase(type))) {
            return;
        }

        String resourceName = parameter.has(DataXConstants.RESOURCE_NAME) && parameter.get(DataXConstants.RESOURCE_NAME) != null ?
            parameter.get(DataXConstants.RESOURCE_NAME).getAsString() : null;
        parameter.addProperty(DataXConstants.DI_DATASOURCE, resourceName);

        CDPTypeEnum resourceWriteType = resourceWriteTypes.stream()
            .filter(t -> t.getD2DataXType().equalsIgnoreCase(type)).findFirst().orElse(null);

        processFieldDelimiter(parameter);

        if (parameter.has(DataXConstants.DI_HEADER)) {
            String header = parameter.get(DataXConstants.DI_HEADER).getAsString();
            JsonArray headerArr = new JsonArray();
            if (StringUtils.isNotBlank(header)) {
                String[] headers = StringUtils.split(header, ",");
                for (String col : headers) {
                    headerArr.add(col);
                }
            }
            parameter.add(DataXConstants.DI_HEADER, headerArr);
        }
        switch (resourceWriteType) {
            case OSS: {
                String objectName = parameter.has(DataXConstants.OBJECT_NAME) && parameter.get(DataXConstants.OBJECT_NAME) != null ?
                    parameter.get(DataXConstants.OBJECT_NAME).getAsString() : null;
                String filePrefix = parameter.has(DataXConstants.FILE_PREFIX) && parameter.get(DataXConstants.FILE_PREFIX) != null ?
                    parameter.get(DataXConstants.FILE_PREFIX).getAsString() : null;

                if (StringUtils.isNotBlank(objectName) && StringUtils.isNotBlank(filePrefix)) {
                    String ossObject = Joiner.on("/").join(objectName, filePrefix);
                    ossObject = Joiner.on("/").join(Arrays.stream(ossObject.split("/"))
                        .map(StringUtils::trim)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList()));
                    parameter.addProperty(DataXConstants.DI_OSS_OBJECT, ossObject);
                }
                break;
            }
            case SFTP: {
                String objectName = parameter.has(DataXConstants.OBJECT_NAME) && parameter.get(DataXConstants.OBJECT_NAME) != null ?
                    parameter.get(DataXConstants.OBJECT_NAME).getAsString() : null;
                String filePrefix = parameter.has(DataXConstants.FILE_PREFIX) && parameter.get(DataXConstants.FILE_PREFIX) != null ?
                    parameter.get(DataXConstants.FILE_PREFIX).getAsString() : null;

                parameter.addProperty(DataXConstants.DI_FILE_NAME, filePrefix);
                parameter.addProperty(DataXConstants.DI_PATH, objectName);
                break;
            }
            default:
        }
    }

    private static void processFieldDelimiter(JsonObject parameter) {
        if (!parameter.has(DataXConstants.DI_FILE_FORMAT)) {
            parameter.addProperty(DataXConstants.DI_FILE_FORMAT, "text");
        }

        if (parameter.has(DataXConstants.DI_FIELD_DELIMITER)) {
            String fieldDelimiter = parameter.get(DataXConstants.DI_FIELD_DELIMITER).getAsString();
            String fieldDelimiterOriginal = replaceControlCharacterPrintable(fieldDelimiter);
            if (StringUtils.isNumeric(fieldDelimiter)) {
                char delimiter = (char)(Integer.parseInt(fieldDelimiter));
                parameter.addProperty(DataXConstants.DI_FIELD_DELIMITER, delimiter + "");
                fieldDelimiterOriginal = replaceControlCharacterPrintable(delimiter + "");
            }
            parameter.addProperty(DataXConstants.DI_FIELD_DELIMITER_ORIGIN, fieldDelimiterOriginal);
        }
    }

    private static void processGuidTable(JsonObject parameter) {
        if (parameter.has("guid")) {
            if (!parameter.has("table") || StringUtils.isBlank(
                parameter.get("table").getAsString())) {
                String guid = parameter.get("guid").getAsString();
                String[] tokens = StringUtils.split(guid, ".");
                if (tokens != null && tokens.length > 0) {
                    parameter.addProperty("table", tokens[tokens.length - 1]);
                }
            }
            parameter.remove("guid");
        }
    }

    /**
     * 基础信息
     */
    private void convertBasicInfo(DwNode dwNode) throws ParseException {
        dwNode.setNodeUseType(NodeUseType.SCHEDULED);
        dwNode.setName(this.taskDefineDO.getTaskName().replaceAll(" +", ""));
        dwNode.setPriority(this.taskDefineDO.getPriority());
        dwNode.setIsAutoParse(0);
        if (this.cdfTaskInfo.getStartEffectDate() != null) {
            dwNode.setStartEffectDate(SDF.parse(this.cdfTaskInfo.getStartEffectDate()));
        }
        if (this.cdfTaskInfo.getEndEffectDate() != null) {
            dwNode.setEndEffectDate(SDF.parse(this.cdfTaskInfo.getEndEffectDate()));
        }
        if (StringUtils.isNotBlank(this.taskDefineDO.parameter)) {
            dwNode.setParameter(DateParser.parse(this.taskDefineDO.parameter));
        }
    }

    /**
     * 依赖关系
     */
    private void convertDependent(DwNode dwNode, String workflowName) {
        List<NodeIo> inputs = this.cdfTaskInfo.getParentActivities().stream()
            .filter(d -> d.getRelationType() == 0)
            .map(d -> {
                DwNodeIo input = new DwNodeIo();
                input.setParseType(1);
                input.setData(d.name);
                input.setIsDifferentApp(true);
                return input;
            }).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(inputs)) {
            dwNode.getInputs().addAll(inputs);
            NodeIo output = new NodeIo();
            output.setParseType(1);
            output.setData(dwNode.getName());
            dwNode.getOutputs().add(output);
        }
        List<CdfTaskInfo.ParentActivity> parentActivities = this.cdfTaskInfo.getParentActivities().stream()
            .filter(d -> d.getRelationType() == 3).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(parentActivities)) {
            dwNode.setDependentType(this.cdfTaskInfo.getDependentType());
        }
    }
}
