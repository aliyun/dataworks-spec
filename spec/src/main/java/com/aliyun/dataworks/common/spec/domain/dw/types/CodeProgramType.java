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

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * DataWorks common used code program types
 *
 * @author 聿剑
 * @date 2022/12/28
 */
@Getter
public enum CodeProgramType {
    SHELL(2, "SHELL", CalcEngineType.GENERAL, null, ".sh"),
    DIDE_SHELL(6, "DIDE_SHELL", CalcEngineType.GENERAL, null, ".sh"),
    PERL(31, "PERL", CalcEngineType.GENERAL, null, ".pl"),
    VIRTUAL_WORKFLOW(97, "VIRTUAL_WORKFLOW", CalcEngineType.GENERAL, null, null),
    COMBINED_NODE(98, "COMBINED_NODE", CalcEngineType.GENERAL, null, null),
    VIRTUAL(99, "VIRTUAL", CalcEngineType.GENERAL, null, ".vi"),
    CONTROLLER_ASSIGNMENT(1100, "CONTROLLER_ASSIGNMENT", CalcEngineType.GENERAL, null, ".assign.json"),
    CONTROLLER_BRANCH(1101, "CONTROLLER_BRANCH", CalcEngineType.GENERAL, null, ".branch.json"),
    CONTROLLER_JOIN(1102, "CONTROLLER_JOIN", CalcEngineType.GENERAL, null, ".join.json"),
    CONTROLLER_CYCLE(1103, "CONTROLLER_CYCLE", CalcEngineType.GENERAL, null, ".do-while.json"),
    CONTROLLER_CYCLE_START(1104, "CONTROLLER_CYCLE_START", CalcEngineType.GENERAL, null, ".do-while-start"),
    CONTROLLER_CYCLE_END(1105, "CONTROLLER_CYCLE_END", CalcEngineType.GENERAL, null, ".do-while-end"),
    CONTROLLER_TRAVERSE(1106, "CONTROLLER_TRAVERSE", CalcEngineType.GENERAL, null, ".for-each.json"),
    CONTROLLER_TRAVERSE_START(1107, "CONTROLLER_TRAVERSE_START", CalcEngineType.GENERAL, null, ".for-each-start"),
    CONTROLLER_TRAVERSE_END(1108, "CONTROLLER_TRAVERSE_END", CalcEngineType.GENERAL, null, ".for-each-end"),
    CONTROLLER_WAIT(1109, "CONTROLLER_WAIT", CalcEngineType.GENERAL, null, ".wait.json"),
    SCHEDULER_TRIGGER(1114, "SCHEDULER_TRIGGER", CalcEngineType.GENERAL, null, ".json"),
    PARAM_HUB(1115, "PARAM_HUB", CalcEngineType.GENERAL, null, ".param-hub.json"),
    FTP_CHECK(1320, "FTP_CHECK", CalcEngineType.GENERAL, null, ".json"),
    CHECK(19, "CHECK", CalcEngineType.GENERAL, null, ".json"),
    OSS_INSPECT(239, "OSS_INSPECT", CalcEngineType.GENERAL, null, ".json"),
    CROSS_TENANTS(1089, "CROSS_TENANTS", CalcEngineType.GENERAL, null, ".json"),

    HIVE(3, "HIVE", CalcEngineType.ODPS, LabelType.DATA_PROCESS, ".sql"),
    ODPS_PERL(9, "odps_pl", CalcEngineType.ODPS, LabelType.DATA_PROCESS, ".pl"),
    ODPS_SQL(10, "ODPS_SQL", CalcEngineType.ODPS, LabelType.DATA_PROCESS, ".sql"),
    ODPS_SPARK_SQL(226, "ODPS_SPARK_SQL", CalcEngineType.ODPS, LabelType.DATA_PROCESS, ".sql"),
    ODPS_MR(11, "ODPS_MR", CalcEngineType.ODPS, LabelType.DATA_PROCESS, null),
    PYODPS3(1221, "PYODPS3", CalcEngineType.ODPS, LabelType.DATA_PROCESS, ".py"),
    ODPS_SCRIPT(24, "ODPS_SQL_SCRIPT", CalcEngineType.ODPS, LabelType.DATA_PROCESS, ".ms"),
    PYODPS(221, "PY_ODPS", CalcEngineType.ODPS, LabelType.DATA_PROCESS, ".py"),
    ODPS_SPARK(225, "ODPS_SPARK", CalcEngineType.ODPS, LabelType.DATA_PROCESS, null),
    COMPONENT_SQL(1010, "COMPONENT_SQL", CalcEngineType.ODPS, LabelType.DATA_PROCESS, ".sql"),
    SQL_COMPONENT(3010, "SQL_COMPONENT", CalcEngineType.ODPS, LabelType.DATA_PROCESS, ".sql"),
    ODPS_PYTHON(12, "ODPS_PYTHON", CalcEngineType.ODPS, LabelType.RESOURCE, ".py"),
    ODPS_JAR(13, "ODPS_JAR", CalcEngineType.ODPS, LabelType.RESOURCE, null),
    ODPS_ARCHIVE(14, "ODPS_ARCHIVE", CalcEngineType.ODPS, LabelType.RESOURCE, null),
    ODPS_FILE(15, "ODPS_FILE", CalcEngineType.ODPS, LabelType.RESOURCE, null),
    ODPS_DDL(18, "ODPS_DDL", CalcEngineType.ODPS, LabelType.RESOURCE, ".sql"),
    ODPS_TABLE(16, "ODPS_TABLE", CalcEngineType.ODPS, LabelType.TABLE, null),
    ODPS_FUNCTION(17, "ODPS_FUNCTION", CalcEngineType.ODPS, LabelType.FUNCTION, null),

    DATAX(4, "DATAX", CalcEngineType.DI, null, ".json"),
    DATAX2(20, "DATAX2", CalcEngineType.DI, null, ".json"),
    CDP(22, "CDP", CalcEngineType.DI, null, ".json"),
    DI(23, "DI", CalcEngineType.DI, null, ".json"),
    RI(900, "RI", CalcEngineType.DI, null, ".json"),
    DD_MERGE(222, "DD_MERGE", CalcEngineType.DI, null, ".json"),
    TT_MERGE(200, "TT_MERGE", CalcEngineType.DI, null, ".json"),

    EMR_HIVE(227, "EMR_HIVE", CalcEngineType.EMR, LabelType.DATA_PROCESS, ".sql"),
    EMR_SPARK(228, "EMR_SPARK", CalcEngineType.EMR, LabelType.DATA_PROCESS, ".sh"),
    EMR_SPARK_SQL(229, "EMR_SPARK_SQL", CalcEngineType.EMR, LabelType.DATA_PROCESS, ".sql"),
    EMR_MR(230, "EMR_MR", CalcEngineType.EMR, LabelType.DATA_PROCESS, ".sh"),
    EMR_SHELL(257, "EMR_SHELL", CalcEngineType.EMR, LabelType.DATA_PROCESS, ".sh"),
    EMR_SPARK_SHELL(258, "EMR_SPARK_SHELL", CalcEngineType.EMR, LabelType.DATA_PROCESS, ".sh"),
    EMR_PRESTO(259, "EMR_PRESTO", CalcEngineType.EMR, LabelType.DATA_PROCESS, ".sql"),
    EMR_IMPALA(260, "EMR_IMPALA", CalcEngineType.EMR, LabelType.DATA_PROCESS, ".sql"),
    EMR_SCOOP(263, "EMR_SCOOP", CalcEngineType.EMR, LabelType.DATA_PROCESS, null),
    EMR_SPARK_STREAMING(264, "EMR_SPARK_STREAMING", CalcEngineType.EMR, LabelType.DATA_PROCESS, ".sh"),
    EMR_HIVE_CLI(265, "EMR_HIVE_CLI", CalcEngineType.EMR, LabelType.DATA_PROCESS, null),
    EMR_STREAMING_SQL(266, "EMR_STREAMING_SQL", CalcEngineType.EMR, LabelType.DATA_PROCESS, ".sql"),
    EMR_TRINO(267, "EMR_TRINO", CalcEngineType.EMR, LabelType.DATA_PROCESS, ".sql"),
    EMR_JAR(231, "EMR_JAR", CalcEngineType.EMR, LabelType.RESOURCE, null),
    EMR_FILE(232, "EMR_FILE", CalcEngineType.EMR, LabelType.RESOURCE, null),
    EMR_TABLE(261, "EMR_TABLE", CalcEngineType.EMR, LabelType.TABLE, null),
    EMR_FUNCTION(262, "EMR_FUNCTION", CalcEngineType.EMR, LabelType.FUNCTION, null),

    CDH_HIVE(270, "CDH_HIVE", CalcEngineType.HADOOP_CDH, LabelType.DATA_PROCESS, ".sql"),
    CDH_SPARK(271, "CDH_SPARK", CalcEngineType.HADOOP_CDH, LabelType.DATA_PROCESS, ".sh"),
    CDH_SPARK_SQL(272, "CDH_SPARK_SQL", CalcEngineType.HADOOP_CDH, LabelType.DATA_PROCESS, ".sql"),
    CDH_MR(273, "CDH_MR", CalcEngineType.HADOOP_CDH, LabelType.DATA_PROCESS, null),
    CDH_SHELL(276, "CDH_SHELL", CalcEngineType.HADOOP_CDH, LabelType.DATA_PROCESS, ".sh"),
    CDH_SPARK_SHELL(277, "CDH_SPARK_SHELL", CalcEngineType.HADOOP_CDH, LabelType.DATA_PROCESS, ".sh"),
    CDH_PRESTO(278, "CDH_PRESTO", CalcEngineType.HADOOP_CDH, LabelType.DATA_PROCESS, ".sql"),
    CDH_IMPALA(279, "CDH_IMPALA", CalcEngineType.HADOOP_CDH, LabelType.DATA_PROCESS, ".sql"),

    CDH_JAR(274, "CDH_JAR", CalcEngineType.HADOOP_CDH, LabelType.RESOURCE, null),
    CDH_FILE(275, "CDH_FILE", CalcEngineType.HADOOP_CDH, LabelType.RESOURCE, null),
    CDH_TABLE(280, "CDH_TABLE", CalcEngineType.HADOOP_CDH, LabelType.TABLE, null),
    CDH_FUNCTION(281, "CDH_FUNCTION", CalcEngineType.HADOOP_CDH, LabelType.FUNCTION, null),

    PAI(1002, "pai", CalcEngineType.ALGORITHM, null, null),
    PAI_STUDIO(1117, "pai_studio", CalcEngineType.ALGORITHM, null, null),

    HOLOGRES_DEVELOP(1091, "HOLOGRES_DEVELOP", CalcEngineType.HOLO, LabelType.DATA_PROCESS, ".sql"),
    HOLOGRES_SYNC(1092, "HOLOGRES_DEVELOP", CalcEngineType.HOLO, LabelType.DATA_PROCESS, ".json"),
    HOLOGRES_SQL(1093, "HOLOGRES_SQL", CalcEngineType.HOLO, LabelType.DATA_PROCESS, ".sql"),
    HOLOGRES_SYNC_DDL(1094, "HOLOGRES_SYNC_DDL", CalcEngineType.HOLO, LabelType.DATA_PROCESS, ".json"),
    HOLOGRES_SYNC_DATA(1095, "HOLOGRES_SYNC_DATA", CalcEngineType.HOLO, LabelType.DATA_PROCESS, ".json"),

    BLINK_BATCH_SQL(2020, "BLINK_BATCH_SQL", CalcEngineType.FLINK, LabelType.DATA_PROCESS, null),
    BLINK_DATASTREAM(2019, "BLINK_DATASTREAM", CalcEngineType.FLINK, LabelType.DATA_PROCESS, null),

    CLICK_SQL(1301, "CLICK_SQL", CalcEngineType.CLICKHOUSE, LabelType.DATA_PROCESS, ".sql"),
    ;

    private final int code;
    private final String name;
    private final CalcEngineType calcEngineType;
    private final LabelType labelType;
    private final String extension;

    CodeProgramType(int code, String name, CalcEngineType calcEngineType, LabelType labelType, String extension) {
        this.code = code;
        this.name = name;
        this.calcEngineType = calcEngineType;
        this.labelType = labelType;
        this.extension = extension;
    }

    public static boolean isODPSResource(Integer value) {
        return value >= ODPS_PYTHON.getCode() && value <= ODPS_DDL.getCode();
    }

    public static boolean isODPSFunction(Integer value) {
        return value.equals(ODPS_FUNCTION.getCode());
    }

    public static CodeProgramType getNodeTypeByCode(int code) {
        return Arrays.stream(values()).filter(t -> t.getCode() == code).findFirst().orElse(null);
    }

    public static String getNodeTypeNameByCode(int code) {
        return Optional.ofNullable(getNodeTypeByCode(code)).map(Enum::name).orElse(null);
    }

    public static CodeProgramType getNodeTypeByName(String name) {
        return Arrays.stream(values()).filter(t -> StringUtils.equalsIgnoreCase(t.name(), name)).findAny().orElse(null);
    }

    public static boolean matchEngine(String codeProgramType, CalcEngineType calcEngineType) {
        return Arrays.stream(CodeProgramType.values())
            .filter(t -> StringUtils.equalsIgnoreCase(t.name(), codeProgramType))
            .anyMatch(t -> Objects.equals(calcEngineType, t.getCalcEngineType()));
    }
}
