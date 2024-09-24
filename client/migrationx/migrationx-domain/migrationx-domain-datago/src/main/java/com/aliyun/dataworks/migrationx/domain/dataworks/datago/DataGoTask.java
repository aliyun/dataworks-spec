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

package com.aliyun.dataworks.migrationx.domain.dataworks.datago;

import java.io.File;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.DITask;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.DateParser;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.DgDatasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.constant.DataXConstants;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.enums.CDPTypeEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.enums.DependentTypeEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.enums.FrequencyTypeEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.enums.TaskCodeEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.NewDataX2DI;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.OldDataX2DI;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.CodeModeType;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import static com.aliyun.dataworks.migrationx.domain.dataworks.datago.DataGoConstants.NODE_IO_SUFFIX_LOWERCASE;
import static com.aliyun.dataworks.migrationx.domain.dataworks.datago.DataGoConstants.NODE_IO_SUFFIX_MIXED;
import static com.aliyun.dataworks.migrationx.domain.dataworks.datago.DataGoConstants.NODE_IO_SUFFIX_UPPERCASE;
import static com.aliyun.dataworks.migrationx.domain.dataworks.utils.StringUtils.isAllAlphabetLowerCase;
import static com.aliyun.dataworks.migrationx.domain.dataworks.utils.StringUtils.isAllAlphabetUpperCase;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description DataGO任务解析
 * @createTime 2020-04-09
 */
@Data
@Slf4j
public class DataGoTask {
    private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static String INPUT = "INPUT";
    private final static String OUTPUT = "OUTPUT";
    private final static String DEFAULT_START_NODE = "datago_start";
    private final static String NORMAL_EXPR = "0:ALIGN";

    private TaskInstModel taskInstModel;
    private String treePathOfName;
    private Boolean success = Boolean.FALSE;
    private Properties properties;

    /**
     * 任务实例
     */
    @Data
    public static class TaskInstModel {
        /**
         * 任务类型->TaskCodeEnum
         */
        private String taskCode;
        private String name;
        private String userId;
        private TaskSnapshot taskSnapshot;

        /**
         * 任务版本
         */
        @Data
        public static class TaskSnapshot {
            private Task task;
            private List<IoTag> ioTags;

            /**
             * 任务模型
             */
            @Data
            private static class Task {
                /**
                 * 任务类型->TaskCodeEnum
                 */
                private String code;
                private String name;
                private String description;
                private String userId;
                /**
                 * 任务主内容 注意:国泰是老版任务,该字段为空
                 */
                private String content;

                /**
                 * 老板自定义变量
                 */
                private String udv;

                private JsonObject getSerializeUdv() {
                    JsonParser parser = new JsonParser();
                    return parser.parse(this.udv).getAsJsonObject();
                }

                /**
                 * 变量 User Defined Variables
                 */
                private List<UserDefineValueModel> userDefineValueModels;
                /**
                 * 调度周期
                 */
                private ScheduleConfig scheduleConfig;
                /**
                 * 老版本的reader，writer放到这里;
                 */
                private OldDataX2DI params;
                private List<DetectConfig> depDetectList;

                @Override
                public String toString() {
                    return GsonUtils.defaultGson.toJson(this);
                }

            }
        }

        /**
         * 检测任务配置
         */
        @Data
        private static class DetectConfig {
            private String type;
            private Date gmtModified;
            /**
             * interval in seconds
             */
            private Integer interval;
            private Integer duration;
            private String stopStrategy;
            private String stopTime;
            private DetectTarget target;
        }

        @Data
        private static class DetectTarget {
            private String fileType;
            private String directory;
            private String file;
            private Long datasourceId;
            private String type;
        }

        /**
         * 任务变量
         */
        @Data
        private static class UserDefineValueModel {
            private final String EQUAL_SIGN = "=";
            private String key;
            private String value;
            private Boolean encryption;

            public String toKeyValue() {
                return key + EQUAL_SIGN + value;
            }
        }

        /**
         * 任务调度属性
         */
        @Data
        private static class ScheduleConfig {
            /**
             * 调度时间 ->FrequencyEnum
             */
            private String frequency;
            private String scheduleExpression;
            private Integer priority;
            private String startEffectDate;
            private String endEffectDate;
        }

        /**
         * 依赖表达式
         */
        @Data
        private static class DepExpr {
            private String expr;
        }

        /**
         * 输入输出标签
         */
        @Data
        private static class IoTag {
            private String type;
            private String name;
            private String source;
            private Boolean enabled;
            private DepExpr depExpr;
        }

    }

    /**
     * 转换成Dataworks的Node
     */
    private DwNode toNode(String workflow) throws ParseException {
        TaskInstModel.TaskSnapshot.Task task = this.taskInstModel.taskSnapshot.task;
        DwNode dwNode = new DwNode();
        // type
        getNodeType(dwNode);
        // info
        convertBasicInfo(dwNode);
        // cron
        dwNode.setCycleType(FrequencyTypeEnum.toCycleType(task.scheduleConfig.frequency).getCode());
        dwNode.setCronExpress(CronUtil.parseCron(task.scheduleConfig.scheduleExpression));
        // dependent
        convertDependent(dwNode);
        return dwNode;
    }

    public List<DwNode> toNodes(String workflow, Map<String, DgDatasource> datasourceMap) throws Exception {
        // 节点转换
        DwNode originalNode = toNode(workflow);
        processDepDetect(originalNode, datasourceMap);
        // 特殊任务处理
        TaskInstModel.TaskSnapshot.Task task = this.taskInstModel.taskSnapshot.task;
        OldDataX2DI oldDataX2DI = task.getParams();
        List<DwNode> dwNodes = oldDataX2DI.convertMergeNode(originalNode, workflow);
        if (CollectionUtils.isNotEmpty(dwNodes)) {
            this.success = Boolean.TRUE;
        }
        return dwNodes;
    }

    private void processDepDetect(DwNode originalNode, Map<String, DgDatasource> datasourceMap) throws Exception {
        if (CollectionUtils.isEmpty(this.taskInstModel.taskSnapshot.task.depDetectList) || MapUtils.isEmpty(
            datasourceMap)) {
            return;
        }

        List<TaskInstModel.DetectConfig> depDetectList = this.taskInstModel.taskSnapshot.task.depDetectList;
        if (depDetectList.size() > 1) {
            log.error("depDetectList: {}", GsonUtils.gson.toJson(depDetectList));
            throw new RuntimeException("got more than one detect config: " + this.taskInstModel.taskSnapshot.task.name);
        }

        TaskInstModel.DetectConfig detectConfig = depDetectList.get(0);
        if (detectConfig.getTarget() == null) {
            return;
        }

        TaskInstModel.DetectTarget target = detectConfig.getTarget();
        datasourceMap.values().stream().filter(ds -> ds.getId().equals(target.getDatasourceId())).findFirst().ifPresent(
            ds -> {
                try {
                    String path = DateParser.parse(Joiner.on(File.separator).join(
                        StringUtils.defaultIfBlank(ds.getRootPath(), "root"),
                        StringUtils.defaultIfBlank(target.getDirectory(), "directory"),
                        StringUtils.defaultIfBlank(target.getFile(), "")));
                    Integer interval = detectConfig.getInterval() == null ? 3 : detectConfig.getInterval();
                    String stopAt = Optional.ofNullable(this.properties)
                        .map(prop -> prop.getProperty(DataGoConstants.PROPERTIES_CONVERTER_DETECT_TASK_STOP_AT))
                        .orElse("2350");

                    // StringUtils.defaultIfBlank(detectConfig.getStopTime(), "23:59:59");

                    String params = Joiner.on(" ").join(
                        StringUtils.defaultIfBlank(ds.getName(), "datasourceName"),
                        path, interval, stopAt);
                    String code = IOUtils.toString(
                        new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("res/ftpcheck.sh")));
                    originalNode.setParameter(params);
                    originalNode.setCode(code);
                    originalNode.setType(CodeProgramType.DIDE_SHELL.name());
                } catch (Exception e) {
                    log.error("convert detect config error: ", e);
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * 转换成Dataworks的Node
     */
    public List<DwNode> toNodes(String workflow) throws Exception {
        return toNodes(workflow, null);
    }

    /**
     * 获取节点类型以及code
     */
    private void getNodeType(DwNode dwNode) {
        TaskInstModel.TaskSnapshot.Task task = this.taskInstModel.taskSnapshot.task;
        CodeProgramType nodeType = TaskCodeEnum.toDefaultNodeType(task.code);
        if (null == nodeType) {
            log.error("[DataGO] node Type not support, task: {}", taskInstModel.name);
            dwNode.setType(CodeProgramType.DIDE_SHELL.name());
            dwNode.setCode(task.content);
        } else {
            dwNode.setType(nodeType.name());
            if (CodeProgramType.DI.name().equals(nodeType.getName())) {
                DITask diTask = toDITask(task);
                diTask.getSteps().stream().forEach(step -> rewriteParameters(step));
                if (diTask.getSetting() != null) {
                    DITask.Setting setting = diTask.getSetting();
                    if (setting.getSpeed() != null) {
                        DITask.Setting.Speed speed = setting.getSpeed();
                        if (speed != null && speed.getConcurrent() != null) {
                            speed.setDmu(speed.getConcurrent());
                        }
                    }
                    if (setting.getErrorLimit() == null) {
                        DITask.Setting.ErrorLimit errorLimit = new DITask.Setting.ErrorLimit();
                        errorLimit.setRecord(0);
                        setting.setErrorLimit(errorLimit);
                    } else if (setting.getErrorLimit().getRecord() == null) {
                        setting.getErrorLimit().setRecord(0);
                    }
                } else {
                    DITask.Setting setting = new DITask.Setting();
                    DITask.Setting.ErrorLimit errorLimit = new DITask.Setting.ErrorLimit();
                    errorLimit.setRecord(0);
                    setting.setErrorLimit(errorLimit);
                    DITask.Setting.Speed speed = new DITask.Setting.Speed();
                    setting.setSpeed(speed);
                    diTask.setSetting(setting);
                }
                dwNode.setCode(diTask.toString());
                dwNode.setCodeMode(CodeModeType.WIZARD.getValue());
            } else if (!CodeProgramType.VIRTUAL.name().equals(nodeType.name())) {
                dwNode.setCode(task.content);
            }
        }
    }

    private void rewriteParameters(DITask.Step step) {
        if ("sftp".equalsIgnoreCase(step.getStepType())) {
            step.setStepType("ftp");
        }

        JsonObject parameter = step.getParameter();
        if (parameter != null) {
            if (!parameter.has(DataXConstants.DI_FIELD_DELIMITER_ORIGIN) ||
                StringUtils.isBlank(parameter.get(DataXConstants.DI_FIELD_DELIMITER_ORIGIN).getAsString())) {
                String fieldDelimiter = parameter.has(DataXConstants.DI_FIELD_DELIMITER) ?
                    parameter.get(DataXConstants.DI_FIELD_DELIMITER).getAsString() : null;
                if (StringUtils.isNotBlank(fieldDelimiter)) {
                    parameter.addProperty(DataXConstants.DI_FIELD_DELIMITER_ORIGIN, StringEscapeUtils.escapeJava(fieldDelimiter));
                }
            }

            if (!parameter.has(DataXConstants.DI_PATH)) {
                String filePath = parameter.has(DataXConstants.DI_FILE_PATH) ? parameter.get(DataXConstants.DI_FILE_PATH).getAsString() : "";
                if (StringUtils.isNotBlank(filePath)) {
                    JsonArray path = new JsonArray();
                    path.add(filePath);
                    parameter.add(DataXConstants.DI_PATH, path);
                }
            }

            if (!parameter.has(DataXConstants.DI_FILE_FORMAT)) {
                parameter.addProperty(DataXConstants.DI_FILE_FORMAT, "text");
            }

            if (parameter.has(DataXConstants.DI_COMPRESS)) {
                String compress = parameter.get(DataXConstants.DI_COMPRESS).getAsString();
                if ("NONE".equalsIgnoreCase(compress)) {
                    parameter.remove(DataXConstants.DI_COMPRESS);
                }
            }

            /**
             * odps reader 的partition必须为字符串数组
             * "partition": ["pt=xxxx"]
             */
            if ("odps".equalsIgnoreCase(step.getStepType())
                && "reader".equalsIgnoreCase(step.getCategory())
                && parameter.has(DataXConstants.DI_PARTITION)
            ) {
                JsonElement partition = parameter.get(DataXConstants.DI_PARTITION);
                if (partition.isJsonPrimitive()) {
                    String partitionStr = partition.getAsString();
                    JsonArray partitionArray = new JsonArray();
                    partitionArray.add(partitionStr);
                    parameter.add(DataXConstants.DI_PARTITION, partitionArray);
                }
            }

            /**
             * oceanbase_new -> apsaradb_for_oceanbase
             */
            if (CDPTypeEnum.OCEAN_BASE_NEW.getCDPDataXType().equalsIgnoreCase(step.getStepType())) {
                step.setStepType(CDPTypeEnum.OCEAN_BASE_NEW.getD2DataXType());
            }
        }
    }

    /**
     * 同步脚本转换成DI
     */
    public static DITask toDITask(TaskInstModel.TaskSnapshot.Task task) {
        DITask diTask = new DITask();
        if (TaskCodeEnum.DATA_IMPORT.getCode().equals(task.code) || TaskCodeEnum.DATA_EXPORT.getCode().equals(
            task.code)) {
            OldDataX2DI oldDataX2DI = task.getParams();
            diTask.getSteps().add(oldDataX2DI.toReader());
            diTask.getSteps().add(oldDataX2DI.toWriter());
            diTask.setSetting(oldDataX2DI.getDataSyncConfig().getSerializeSetting());
        } else {
            NewDataX2DI dataXContent = GsonUtils.defaultGson.fromJson(task.content,
                new TypeToken<NewDataX2DI>() {}.getType());
            diTask.getSteps().add(dataXContent.toReader());
            diTask.getSteps().add(dataXContent.toWriter());
            diTask.setSetting(dataXContent.getContent().getDataxSetting());
        }
        return diTask;
    }

    /**
     * 基本信息
     */
    private void convertBasicInfo(DwNode dwNode) throws ParseException {
        TaskInstModel.TaskSnapshot.Task task = this.taskInstModel.taskSnapshot.task;
        dwNode.setName(task.name.replaceAll(" +", ""));
        dwNode.setPriority(task.scheduleConfig.priority);
        dwNode.setIsAutoParse(0);
        if (StringUtils.isNotEmpty(task.scheduleConfig.startEffectDate)) {
            dwNode.setStartEffectDate(SDF.parse(task.scheduleConfig.startEffectDate));
        }
        if (StringUtils.isNotEmpty(task.scheduleConfig.endEffectDate)) {
            dwNode.setEndEffectDate(SDF.parse(task.scheduleConfig.endEffectDate));
        }
        List<TaskInstModel.UserDefineValueModel> userDefineValueModels = task.userDefineValueModels;
        if (CollectionUtils.isNotEmpty(userDefineValueModels)) {
            List<String> paramList = userDefineValueModels.stream()
                .map(TaskInstModel.UserDefineValueModel::toKeyValue)
                .collect(Collectors.toList());
            String param = Joiner.on(" ").skipNulls().join(paramList);
            dwNode.setParameter(DateParser.parse(param));
        }
        if (StringUtils.isNotBlank(task.getUdv())) {
            JsonObject uv = task.getSerializeUdv();
            StringBuilder builder = new StringBuilder();
            uv.entrySet().forEach(stringJsonElementEntry -> {
                builder.append(stringJsonElementEntry.getKey())
                    .append("=")
                    .append(stringJsonElementEntry.getValue().getAsString())
                    .append(" ");
            });
            dwNode.setParameter(DateParser.parse(builder.toString()));
        }
        if (task.getParams() != null && task.getParams() != null && task.getParams().getInputParam() != null) {
            dwNode.setParameter(DateParser.parse(task.getParams().getInputParam().getValue()));
        }
    }

    /**
     * 依赖关系
     */
    private void convertDependent(DwNode dwNode) {
        List<TaskInstModel.IoTag> ioTags = this.taskInstModel.taskSnapshot.ioTags;
        if (CollectionUtils.isNotEmpty(ioTags)) {
            Optional<TaskInstModel.IoTag> crossDependentIO = ioTags.stream()
                .filter(ioTag -> INPUT.equals(ioTag.type) && !NORMAL_EXPR.equals(ioTag.depExpr.expr))
                .findFirst();
            if (crossDependentIO.isPresent()) {
                dwNode.setDependentType(DependentTypeEnum.SELF.getType());
            } else {
                dwNode.setDependentType(DependentTypeEnum.NONE.getType());
            }
            List<NodeIo> inputs = ioTags.stream()
                .filter(ioTag -> INPUT.equals(ioTag.type) && NORMAL_EXPR.equals(ioTag.depExpr.expr) && ioTag.enabled)
                .map(ioTag -> {
                    NodeIo in = convertNodeIo(ioTag.name);
                    in.setParseType(1);
                    return in;
                }).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(inputs)) {
                dwNode.getInputs().addAll(inputs);
            }
            List<NodeIo> outputs = ioTags.stream()
                .filter(ioTag -> OUTPUT.equals(ioTag.type) && ioTag.enabled)
                .map(ioTag -> convertNodeIo(ioTag.name)).collect(Collectors.toList());
            dwNode.getOutputs().addAll(outputs);

        } else {
            dwNode.getOutputs().add(convertNodeIo(dwNode.getName()));
        }

        // 去掉依赖自己的output
        dwNode.setInputs(dwNode.getInputs().stream()
            .filter(in -> !dwNode.getOutputs().stream().anyMatch(out -> out.getData().equalsIgnoreCase(in.getData())))
            .collect(Collectors.toList()));
    }

    private String normalizeNodeIo(String data) {
        if (StringUtils.isBlank(data)) {
            return data;
        }

        if (isAllAlphabetLowerCase(data)) {
            data = data + NODE_IO_SUFFIX_LOWERCASE;
            return data;
        }

        if (isAllAlphabetUpperCase(data)) {
            data = data + NODE_IO_SUFFIX_UPPERCASE;
            return data;
        }

        data = data + NODE_IO_SUFFIX_MIXED;
        return data;
    }

    private NodeIo convertNodeIo(String name) {
        NodeIo output = new DwNodeIo();
        output.setParseType(1);
        output.setData(normalizeNodeIo(name));
        ((DwNodeIo)output).setIsDifferentApp(true);
        return output;
    }
}
