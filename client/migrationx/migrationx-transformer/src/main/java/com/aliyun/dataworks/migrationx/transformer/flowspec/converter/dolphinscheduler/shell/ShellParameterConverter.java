/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.shell;

import java.util.List;
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
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.shell.ShellParameters;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.AbstractParameterConverter;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;
import org.apache.commons.collections4.ListUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-26
 */
public class ShellParameterConverter extends AbstractParameterConverter<ShellParameters> {

    private static final SpecScriptRuntime RUNTIME = new SpecScriptRuntime();

    private static final String RESOURCE_REFERENCE_PREFIX = "##";

    static {
        RUNTIME.setEngine(CodeProgramType.DIDE_SHELL.getCalcEngineType().getLabel());
        RUNTIME.setCommand(CodeProgramType.DIDE_SHELL.getName());
    }

    public ShellParameterConverter(DataWorksWorkflowSpec spec, SpecWorkflow specWorkflow, TaskDefinition taskDefinition,
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
        script.setLanguage(LanguageEnum.SHELL_SCRIPT.getIdentifier());
        script.setRuntime(RUNTIME);
        script.setPath(getScriptPath(specNode));
        String resourceReference = buildFileResourceReference(specNode, RESOURCE_REFERENCE_PREFIX);
        script.setContent(resourceReference + parameter.getRawScript());
        script.setParameters(ListUtils.emptyIfNull(specVariableList).stream().filter(v -> !VariableType.NODE_OUTPUT.equals(v.getType()))
            .collect(Collectors.toList()));
        specNode.setScript(script);
    }
}
