/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Constants;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.DataType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.Direct;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.Flag;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.Property;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v301.ProcessExecutionTypeEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v301.ReleaseState;
import com.aliyun.dataworks.migrationx.transformer.core.utils.CodeGenerateUtils;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import com.aliyun.migrationx.common.utils.JSONUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-04
 */
public class ProcessDefinitionConverter extends AbstractCommonConverter<ProcessDefinition> {

    private final SpecWorkflow workflow;

    private final DataWorksWorkflowSpec spec;

    public ProcessDefinitionConverter(DataWorksWorkflowSpec spec, SpecWorkflow workflow, ProcessDefinition processDefinition,
        FlowSpecConverterContext context) {
        super(Objects.nonNull(processDefinition) ? processDefinition : new ProcessDefinition(), context);
        this.workflow = workflow;
        this.spec = spec;
    }

    public ProcessDefinitionConverter(DataWorksWorkflowSpec spec, SpecWorkflow workflow, FlowSpecConverterContext context) {
        this(spec, workflow, null, context);
    }

    public ProcessDefinition convert() {
        if (Objects.isNull(spec)) {
            throw new BizException(ErrorCode.PARAMETER_NOT_SET, "spec");
        }

        // Determine whether to convert the contents of workflow or spec based on whether workflow is null
        if (Objects.isNull(workflow)) {
            convertSpec();
        } else {
            convertWorkFlow();
        }

        // common fields
        result.setVersion(Constants.VERSION_FIRST);
        result.setReleaseState(context.isOnlineProcess() ? ReleaseState.ONLINE : ReleaseState.OFFLINE);
        result.setProjectCode(context.getProjectCode());
        result.setCreateTime(new Date());
        result.setUpdateTime(new Date());
        result.setFlag(Flag.YES);
        result.setUserId(context.getUserId());
        result.setUserName(null);
        result.setProjectName(null);

        // schedule release state is default offline unless schedule is converted
        result.setScheduleReleaseState(ReleaseState.OFFLINE);
        result.setTimeout(0);
        result.setModifyBy(null);
        result.setWarningGroupId(null);
        result.setExecutionType(ProcessExecutionTypeEnum.PARALLEL);

        return result;
    }

    private void convertWorkFlow() {
        // if id is null, fill it with a random number to avoid error
        if (Objects.isNull(workflow.getId())) {
            workflow.setId(String.valueOf(CodeGenerateUtils.getInstance().genCode()));
        }
        result.setId(generateId(workflow.getId()));
        result.setCode(generateCode(workflow.getId()));
        result.setName(generateProcessDefinitionName(workflow));
        result.setDescription(StringUtils.defaultString(workflow.getDescription()));

        List<Input> inputList = new ArrayList<>(ListUtils.emptyIfNull(workflow.getInputs()));
        inputList.addAll(ListUtils.emptyIfNull(spec.getVariables()));
        setGlobalParamListAndMap(inputList);
    }

    private void convertSpec() {
        // if id is null, fill it with a random number to avoid error
        if (Objects.isNull(spec.getId())) {
            spec.setId(String.valueOf(CodeGenerateUtils.getInstance().genCode()));
        }
        result.setId(generateId(spec.getId()));
        result.setCode(generateCode(spec.getId()));
        result.setName(generateProcessDefinitionName(spec));
        result.setDescription(StringUtils.defaultString(spec.getDescription()));

        List<Input> inputList = new ArrayList<>(ListUtils.emptyIfNull(spec.getVariables()));
        setGlobalParamListAndMap(inputList);
    }

    private void setGlobalParamListAndMap(List<Input> inputList) {
        List<Property> globalParamList = convertGlobalParam(inputList);
        result.setGlobalParams(JSONUtils.toJsonString(globalParamList));
        result.setGlobalParamList(globalParamList);
        Map<String, String> globalParamMap = globalParamList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        result.setGlobalParamMap(globalParamMap);
    }

    private String generateProcessDefinitionName(SpecWorkflow workflow) {
        if (StringUtils.isNotBlank(workflow.getName())) {
            return workflow.getName();
        }
        return ListUtils.emptyIfNull(workflow.getNodes()).stream()
            .map(SpecNode::getName)
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse("unnamed");
    }

    private String generateProcessDefinitionName(DataWorksWorkflowSpec spec) {
        if (StringUtils.isNotBlank(spec.getName())) {
            return spec.getName();
        }
        return ListUtils.emptyIfNull(spec.getNodes()).stream()
            .map(SpecNode::getName)
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse("unnamed");
    }

    private List<Property> convertGlobalParam(List<Input> inputList) {
        Set<String> dataTypeNameSet = Arrays.stream(DataType.values()).map(Enum::name).collect(Collectors.toSet());
        return ListUtils.emptyIfNull(inputList).stream()
            .filter(this::checkGlobalVariable)
            .map(v -> {
                Property property = newProperty((SpecVariable)v, dataTypeNameSet);
                property.setDirect(Direct.IN);
                return property;
            }).collect(Collectors.toList());
    }

    private boolean checkGlobalVariable(Input input) {
        if (Objects.nonNull(input) && input instanceof SpecVariable) {
            SpecVariable specVariable = (SpecVariable)input;
            return VariableScopeType.FLOW.equals(specVariable.getScope());
        }
        return false;

    }

    private Property newProperty(SpecVariable specVariable, Set<String> dataTypeNameSet) {
        Property property = new Property();
        property.setProp(specVariable.getName());
        property.setValue(specVariable.getValue());
        if (dataTypeNameSet.contains(specVariable.getDescription())) {
            property.setType(DataType.valueOf(specVariable.getDescription()));
        } else {
            property.setType(DataType.VARCHAR);
        }
        return property;
    }

}
