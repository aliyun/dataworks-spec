/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.spark;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.OdpsSparkCode;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.LanguageEnum;
import com.aliyun.dataworks.common.spec.domain.enums.SpecFileResourceType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFileResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils.SparkArgsUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.spark.SparkParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.ResourceUtils;
import com.aliyun.dataworks.migrationx.transformer.core.spark.command.SparkSubmitCommandBuilder;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.AbstractParameterConverter;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import com.google.common.base.Joiner;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-24
 */
public class SparkParameterConverter extends AbstractParameterConverter<SparkParameters> {

    private static final String SQL_EXECUTION_TYPE_FILE = "FILE";

    private static final String RESOURCE_REFERENCE_SQL_PREFIX = "--";

    public SparkParameterConverter(DataWorksWorkflowSpec spec, SpecWorkflow specWorkflow, TaskDefinition taskDefinition,
        DolphinSchedulerV3ConverterContext context) {
        super(spec, specWorkflow, taskDefinition, context);
    }

    /**
     * Each node translates the specific logic of the parameters
     */
    @Override
    protected void convertParameter(SpecNode specNode) {
        List<SpecVariable> specVariableList = convertSpecNodeParam(specNode);

        convertFileResourceList(specNode);

        ProgramType programType = parameter.getProgramType();
        switch (programType) {
            case SQL:
                convertSparkSql(specNode, specVariableList);
                break;
            case JAVA:
            case PYTHON:
            case SCALA:
                convertSparkJar(specNode, specVariableList);
                break;
            default:
                throw new BizException(ErrorCode.UNKNOWN_ENUM_TYPE, "ProgramType", programType);
        }
    }

    /**
     * convert Spark-sql in raw script and in resource file
     *
     * @param specNode         node
     * @param specVariableList variables
     */
    private void convertSparkSql(SpecNode specNode, List<SpecVariable> specVariableList) {
        // todo: 完成读文件然后写入spec的操作，需要未来能够同时迁移文件后再做实现
        if (SQL_EXECUTION_TYPE_FILE.equals(parameter.getSqlExecutionType())) {
            return;
        }
        SpecScript script = new SpecScript();
        script.setId(generateUuid());
        script.setLanguage(LanguageEnum.SPARK_SQL.getIdentifier());

        SpecScriptRuntime specScriptRuntime = new SpecScriptRuntime();
        specScriptRuntime.setEngine(CodeProgramType.ODPS_SQL.getCalcEngineType().getLabel());
        specScriptRuntime.setCommand(CodeProgramType.ODPS_SQL.getName());
        script.setRuntime(specScriptRuntime);

        script.setParameters(ListUtils.emptyIfNull(specVariableList).stream().filter(v -> !VariableType.NODE_OUTPUT.equals(v.getType()))
            .collect(Collectors.toList()));

        script.setPath(getScriptPath(specNode));

        String resourceReference = buildFileResourceReference(specNode, RESOURCE_REFERENCE_SQL_PREFIX);
        script.setContent(resourceReference + parameter.getRawScript());

        specNode.setScript(script);
    }

    /**
     * not only convert spark jar. The method converts Spark jar, scala jar and python task
     *
     * @param specNode         node
     * @param specVariableList variables
     */
    private void convertSparkJar(SpecNode specNode, List<SpecVariable> specVariableList) {
        SpecScript script = new SpecScript();
        script.setId(generateUuid());
        script.setLanguage(LanguageEnum.JSON.getIdentifier());

        SpecScriptRuntime specScriptRuntime = new SpecScriptRuntime();
        specScriptRuntime.setEngine(CodeProgramType.ODPS_SPARK.getCalcEngineType().getLabel());
        specScriptRuntime.setCommand(CodeProgramType.ODPS_SPARK.getName());
        script.setRuntime(specScriptRuntime);

        script.setPath(getScriptPath(specNode));

        script.setParameters(ListUtils.emptyIfNull(specVariableList).stream().filter(v -> !VariableType.NODE_OUTPUT.equals(v.getType()))
            .collect(Collectors.toList()));

        List<String> args = dealArgs(SparkArgsUtils.buildArgs(parameter));
        script.setContent(parseOdpsSparkJson(specNode, args));

        specNode.setScript(script);
    }

    /**
     * The main purpose is to handle the option parameters in dolphins
     *
     * @param args origin args
     * @return dealt args
     */
    private List<String> dealArgs(List<String> args) {
        return ListUtils.emptyIfNull(args).stream().collect(ArrayList::new, (list, arg) -> list.addAll(Arrays.asList(arg.split(" "))),
            ArrayList::addAll);
    }

    /**
     * parse spark sql command args to odps spark json
     *
     * @param specNode node
     * @param args     args
     * @return odps spark json
     */
    private String parseOdpsSparkJson(SpecNode specNode, List<String> args) {
        SparkSubmitCommandBuilder sparkSubmitCommandBuilder = new SparkSubmitCommandBuilder(args);

        OdpsSparkCode odpsSparkCode = new OdpsSparkCode();
        odpsSparkCode.setProgramType(CodeProgramType.ODPS_SPARK.name());

        List<String> referenceResources = ListUtils.emptyIfNull(specNode.getFileResources()).stream().map(SpecFileResource::getName).collect(
            Collectors.toList());
        odpsSparkCode.setResourceReferences(referenceResources);
        OdpsSparkCode.CodeJson sparkJson = new OdpsSparkCode.CodeJson();

        String mainApp = new File(sparkSubmitCommandBuilder.getAppResource()).getName();
        if (mainApp.endsWith(ResourceUtils.FILE_EXT_JAR)) {
            sparkJson.setMainJar(mainApp);
            sparkJson.setLanguage(OdpsSparkCode.SPARK_LANGUAGE_JAVA);
        }

        if (mainApp.endsWith(ResourceUtils.FILE_EXT_PY)) {
            sparkJson.setMainPy(mainApp);
            sparkJson.setLanguage(OdpsSparkCode.SPARK_LANGUAGE_PY);
        }

        sparkJson.setArgs(Joiner.on(" ").join(sparkSubmitCommandBuilder.getAppArgs()));
        Map<SpecFileResourceType, List<SpecFileResource>> specFileResourceTypeListMap = ListUtils.emptyIfNull(specNode.getFileResources()).stream()
            .collect(Collectors.groupingBy(SpecFileResource::getType));
        sparkJson.setAssistFiles(getReferenceResourceNameList(specFileResourceTypeListMap.get(SpecFileResourceType.FILE), mainApp));
        sparkJson.setAssistJars(getReferenceResourceNameList(specFileResourceTypeListMap.get(SpecFileResourceType.JAR), mainApp));
        sparkJson.setAssistPys(getReferenceResourceNameList(specFileResourceTypeListMap.get(SpecFileResourceType.PYTHON), mainApp));
        List<String> archiveFileNameList = getReferenceResourceNameList(specFileResourceTypeListMap.get(SpecFileResourceType.ARCHIVE), mainApp);
        sparkJson.setAssistArchives(archiveFileNameList);
        sparkJson.setArchivesName(archiveFileNameList);
        sparkJson.setMainClass(sparkSubmitCommandBuilder.getMainClass());

        sparkSubmitCommandBuilder.getConf().put("spark.hadoop.odps.task.major.version", "cupid_v2");
        sparkJson.setConfigs(
            sparkSubmitCommandBuilder.getConf().entrySet()
                .stream()
                .map(ent -> Joiner.on("=").join(ent.getKey(), ent.getValue()))
                .collect(Collectors.toList())
        );
        odpsSparkCode.setSparkJson(sparkJson);
        return odpsSparkCode.getContent();
    }

    private List<String> getReferenceResourceNameList(List<SpecFileResource> specFileResources, String mainApp) {
        return ListUtils.emptyIfNull(specFileResources).stream()
            .map(SpecFileResource::getName)
            .filter(name -> StringUtils.isNotBlank(name) && !name.equals(mainApp))
            .collect(Collectors.toList());
    }
}
