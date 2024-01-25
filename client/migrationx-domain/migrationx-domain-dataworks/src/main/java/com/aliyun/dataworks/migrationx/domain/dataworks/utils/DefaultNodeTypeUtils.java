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

package com.aliyun.dataworks.migrationx.domain.dataworks.utils;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author sam.liux
 * @date 2020/06/19
 */
public class DefaultNodeTypeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNodeTypeUtils.class);
    private static final Set<CodeProgramType> resourceTypes = new HashSet<>();
    private static final Set<CodeProgramType> functionTypes = new HashSet<>();
    private static final Set<CodeProgramType> diNodeTypes = new HashSet<>();
    private static final Set<CodeProgramType> sqlNodeTypes = new HashSet<>();
    private static final Set<CodeProgramType> mapReduceNodeTypes = new HashSet<>();
    private static final Set<CodeProgramType> shellNodeTypes = new HashSet<>();
    private static final Set<CodeProgramType> normalShellTypes = new HashSet<>();
    private static final Set<CodeProgramType> sparkSubmitTypes = new HashSet<>();

    static {
        mapReduceNodeTypes.add(CodeProgramType.ODPS_MR);
        mapReduceNodeTypes.add(CodeProgramType.EMR_MR);
        mapReduceNodeTypes.add(CodeProgramType.CDH_MR);

        resourceTypes.addAll(Arrays.asList(CodeProgramType.values()).stream()
            .filter(t -> CodeProgramType.isODPSResource(t.getCode()))
            .filter(t -> !CodeProgramType.isODPSFunction(t.getCode()))
            .collect(Collectors.toList()));
        resourceTypes.add(CodeProgramType.EMR_JAR);
        resourceTypes.add(CodeProgramType.EMR_FILE);
        resourceTypes.add(CodeProgramType.CDH_FILE);
        resourceTypes.add(CodeProgramType.CDH_JAR);

        functionTypes.addAll(Arrays.asList(CodeProgramType.values()).stream()
            .filter(t -> CodeProgramType.isODPSFunction(t.getCode()))
            .collect(Collectors.toList()));
        functionTypes.add(CodeProgramType.EMR_FUNCTION);
        functionTypes.add(CodeProgramType.CDH_FUNCTION);

        diNodeTypes.addAll(Arrays.asList(
            CodeProgramType.DI, CodeProgramType.DATAX, CodeProgramType.DATAX2, CodeProgramType.RI));

        sqlNodeTypes.addAll(Arrays.asList(
            // ODPS SQL
            CodeProgramType.ODPS_SQL,
            // EMR SQL
            CodeProgramType.EMR_HIVE, CodeProgramType.EMR_SPARK_SQL, CodeProgramType.EMR_SPARK_SQL,
            CodeProgramType.EMR_IMPALA, CodeProgramType.EMR_PRESTO,
            // CDH SQL
            CodeProgramType.CDH_HIVE, CodeProgramType.CDH_PRESTO, CodeProgramType.CDH_SPARK_SQL,
            CodeProgramType.CDH_IMPALA)
        );

        shellNodeTypes.addAll(Arrays.asList(
            CodeProgramType.DIDE_SHELL, CodeProgramType.EMR_SHELL, CodeProgramType.EMR_SPARK_SHELL,
            CodeProgramType.CDH_SHELL, CodeProgramType.CDH_SPARK_SHELL
        ));

        normalShellTypes.add(CodeProgramType.DIDE_SHELL);

        sparkSubmitTypes.addAll(Arrays.asList(
            CodeProgramType.CDH_SPARK, CodeProgramType.ODPS_SPARK, CodeProgramType.EMR_SPARK));
    }

    public static CodeProgramType getTypeByName(String name, CodeProgramType CodeProgramType) {
        try {
            return com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType.valueOf(StringUtils.upperCase(name));
        } catch (Exception e) {
            LOGGER.warn("{}", e.getMessage());
            return CodeProgramType;
        }
    }

    public static boolean isDiNode(String type) {
        return diNodeTypes.stream().anyMatch(t -> t.name().equalsIgnoreCase(type));
    }

    public static boolean isDiNode(Integer fileType) {
        return diNodeTypes.stream().anyMatch(t -> Integer.valueOf(t.getCode()).equals(fileType));
    }

    public static boolean isResource(Integer fileType) {
        if (CodeProgramType.isODPSResource(fileType) && !CodeProgramType.isODPSFunction(fileType)) {
            return true;
        }

        return resourceTypes.stream().anyMatch(t -> Integer.valueOf(t.getCode()).equals(fileType));
    }

    public static boolean isFunction(Integer fileType) {
        if (CodeProgramType.isODPSFunction(fileType)) {
            return true;
        }

        return functionTypes.stream().anyMatch(t -> Integer.valueOf(t.getCode()).equals(fileType));
    }

    public static Set<CodeProgramType> getSqlNodeTypes() {
        return sqlNodeTypes;
    }

    public static Set<CodeProgramType> getMapReduceNodeTypes() {
        return mapReduceNodeTypes;
    }

    public static Set<CodeProgramType> getShellNodeTypes() {
        return shellNodeTypes;
    }

    public static String convertToNodeMarketModelEngineTypes(CalcEngineType engineType) {
        switch (engineType) {
            case ODPS:
                return "odps";
            case EMR:
                return "emr";
            case HOLO:
                return "holodb";
            case BLINK:
                return "flink";
            case MaxGraph:
                return "maxgraph";
            case HYBRIDDB_FOR_POSTGRESQL:
                return "adb";
            case HADOOP_CDH:
                return "cdh";
            case GENERAL:
            default:
                return engineType.name();
        }
    }

    public static String getDatasourceTypeByEngineType(CalcEngineType engineType) {
        switch (engineType) {
            case ODPS:
                return "odps";
            case EMR:
            case HADOOP_CDH:
                return "hive";
            case HOLO:
                return "holo";
            case BLINK:
                return "flink";
            case MaxGraph:
                return "maxgraph";
            case HYBRIDDB_FOR_POSTGRESQL:
                return "adb";
            case GENERAL:
            default:
                return engineType.name();
        }
    }

    public static Set<CodeProgramType> getSparkSubmitTypes() {
        return sparkSubmitTypes;
    }

    public static boolean isComplexNode(CodeProgramType nodeType) {
        if (nodeType == null) {
            return false;
        }

        switch (nodeType) {
            case COMBINED_NODE:
            case CONTROLLER_CYCLE:
            case CONTROLLER_TRAVERSE:
                return true;
            default:
        }

        return false;
    }

    public static boolean isSqlNode(String type) {
        return getSqlNodeTypes().stream().anyMatch(t -> t.name().equals(type));
    }

    public static boolean isShellNode(String type) {
        return shellNodeTypes.stream().anyMatch(t -> t.getName().equalsIgnoreCase(type));
    }

    public static boolean isSqlComponent(Integer fileType) {
        if (fileType == null) {
            return false;
        }

        return CodeProgramType.SQL_COMPONENT.getCode() == fileType.intValue();
    }

    public static boolean isComponentSql(Integer fileType) {
        if (fileType == null) {
            return false;
        }

        return CodeProgramType.COMPONENT_SQL.getCode() == fileType.intValue();
    }

    public static boolean isNoCalcEngineShell(String type) {
        return normalShellTypes.stream().anyMatch(t -> t.getName().equalsIgnoreCase(type));
    }

    public static CodeProgramType getJarResourceType(String nodeMarketEngineType) {
        return Arrays.stream(CodeProgramType.values())
            .filter(t -> isResource(t.getCode()))
            .filter(t -> t.getName().endsWith("_JAR") && t.getCalcEngineType().name().equalsIgnoreCase(nodeMarketEngineType))
            .findFirst().orElseThrow(() -> BizException.of(ErrorCode.UNKNOWN_ENUM_TYPE)
                .with(CodeProgramType.class, "no jar resource type support by calc engine: " + nodeMarketEngineType));
    }

    public static CodeProgramType getFileResourceType(String nodeMarketEngineType) {
        return Arrays.stream(CodeProgramType.values())
            .filter(t -> isResource(t.getCode()))
            .filter(t -> t.getName().endsWith("_FILE") && t.getCalcEngineType().name().equalsIgnoreCase(nodeMarketEngineType))
            .findFirst().orElseThrow(() -> BizException.of(ErrorCode.UNKNOWN_ENUM_TYPE)
                .with(CodeProgramType.class, "no jar resource type support by calc engine: " + nodeMarketEngineType));
    }
}
