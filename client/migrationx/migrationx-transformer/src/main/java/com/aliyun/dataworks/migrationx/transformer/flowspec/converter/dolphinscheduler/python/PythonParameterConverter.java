/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.python;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.LanguageEnum;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.python.PythonParameters;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.AbstractParameterConverter;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;
import org.apache.commons.collections4.ListUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-21
 */
public class PythonParameterConverter extends AbstractParameterConverter<PythonParameters> {

    private static final SpecScriptRuntime RUNTIME = new SpecScriptRuntime();

    private static final String RESOURCE_REFERENCE_PREFIX = "##";

    static {
        RUNTIME.setEngine(CodeProgramType.PYODPS3.getCalcEngineType().getLabel());
        RUNTIME.setCommand(CodeProgramType.PYODPS3.getName());
    }

    public PythonParameterConverter(DataWorksWorkflowSpec spec, SpecWorkflow specWorkflow, TaskDefinition taskDefinition,
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

        SpecScript script = new SpecScript();
        script.setId(generateUuid());
        script.setLanguage(LanguageEnum.PYTHON3.getIdentifier());
        script.setRuntime(RUNTIME);
        if (PythonVersion.PYTHON2.equals(context.getPythonVersion())) {
            script.getRuntime().setCommand(context.getPythonVersion().getCommand());
            script.setLanguage(LanguageEnum.PYTHON2.getIdentifier());
        }
        script.setPath(getScriptPath(specNode));

        String resourceReference = buildFileResourceReference(specNode, RESOURCE_REFERENCE_PREFIX);
        String pathImportCode = buildPathImportCode(specNode);
        script.setContent(resourceReference + pathImportCode + parameter.getRawScript());
        script.setParameters(ListUtils.emptyIfNull(specVariableList).stream().filter(v -> !VariableType.NODE_OUTPUT.equals(v.getType()))
            .collect(Collectors.toList()));
        specNode.setScript(script);
    }

    private String buildPathImportCode(SpecNode specNode) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("import os\n").append("import sys\n\n");
        Optional.ofNullable(specNode).map(SpecNode::getFileResources).ifPresent(fileResources ->
            fileResources.forEach(fileResource -> {
                String fileName = fileResource.getName();
                stringBuilder.append(String.format("sys.path.append(os.path.dirname(os.path.abspath('%s')))%n", fileName));
            }));
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }
}
