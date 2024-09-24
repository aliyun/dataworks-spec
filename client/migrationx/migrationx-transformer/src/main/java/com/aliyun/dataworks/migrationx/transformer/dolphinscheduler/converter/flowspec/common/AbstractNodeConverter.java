/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.DataType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.Direct;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.Flag;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.Priority;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.Property;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.parameters.AbstractParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v301.Constants;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v301.TimeoutFlag;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320.TaskExecuteType;
import com.aliyun.dataworks.migrationx.transformer.core.utils.CodeGenerateUtils;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import com.aliyun.migrationx.common.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-04
 */
public abstract class AbstractNodeConverter<P extends AbstractParameters> extends AbstractCommonConverter<TaskDefinition> {

    private static final String RESOURCE_REFERENCE_REGEX_FORMAT = "%s@resource_reference\\{\"(.*?)\"\\}";

    private static final Set<String> COMMENT_SET = new HashSet<>();

    static {
        COMMENT_SET.add("##");
        COMMENT_SET.add("--");
    }

    protected SpecNode specNode;

    protected AbstractNodeConverter(SpecNode specNode, FlowSpecConverterContext context) {
        super(context);
        result = new TaskDefinition();
        this.specNode = specNode;
        this.context = context;
    }

    /**
     * main function to convert specNode to taskDefinition
     *
     * @return taskDefinition
     */
    public TaskDefinition convert() {
        if (Objects.isNull(specNode)) {
            return null;
        }
        result.setId(generateId(specNode.getId()));
        result.setCode(generateCode(specNode.getId()));
        result.setName(specNode.getName());
        result.setVersion(Constants.VERSION_FIRST);
        result.setDescription(StringUtils.defaultString(specNode.getDescription()));
        result.setProjectCode(context.getProjectCode());
        result.setUserId(context.getUserId());

        setTaskType();

        // Template method, special conversion logic for each node is here
        P parameter = convertParameter();
        List<Property> localParamList = convertLocalParam();
        if (CollectionUtils.isEmpty(parameter.getLocalParams())) {
            parameter.setLocalParams(localParamList);
        }

        result.setTaskParams(JSONUtils.toJsonString(parameter));
        result.setTaskParamList(localParamList);
        Map<String, String> taskParamMap = localParamList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        result.setTaskParamMap(taskParamMap);

        result.setFlag(Flag.YES);
        result.setIsCache(Flag.NO);
        result.setTaskPriority(convertPriority(specNode.getPriority()));
        result.setUserName(null);
        result.setProjectName(null);
        result.setWorkerGroup(StringUtils.defaultString(context.getWorkerGroup(), "default"));
        result.setEnvironmentCode(context.getEnvironmentCode());
        result.setFailRetryTimes(ObjectUtils.defaultIfNull(specNode.getRerunTimes(), 0));
        // unit conversion, from milliseconds to minutes
        result.setFailRetryInterval((int)Duration.ofMillis(ObjectUtils.defaultIfNull(specNode.getRerunInterval(), 0)).toMinutes());
        result.setTimeoutFlag(TimeoutFlag.CLOSE);
        result.setTimeoutNotifyStrategy(null);
        result.setTimeout(ObjectUtils.defaultIfNull(specNode.getTimeout(), 0));
        result.setDelayTime(Optional.ofNullable(specNode.getTrigger()).map(SpecTrigger::getDelaySeconds)
            .map(delaySeconds -> (delaySeconds + 59) / 60).orElse(0));
        result.setResourceIds(null);
        result.setCreateTime(new Date());
        result.setUpdateTime(new Date());
        result.setModifyBy(null);
        result.setTaskGroupId(0);
        result.setTaskGroupPriority(0);
        result.setTaskExecuteType(TaskExecuteType.BATCH);
        return result;
    }

    /**
     * convert spec node to dolphin scheduler task parameters
     *
     * @return dolphin scheduler task parameters
     */
    protected abstract P convertParameter();

    /**
     * judge task type from spec node and set in taskDefinition
     */
    protected abstract void setTaskType();

    /**
     * use other id generate dolphin style id
     *
     * @param id id
     * @return dolphin style id
     */
    protected int generateId(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return RandomUtils.nextInt(100000000, 999999999);
        }
    }

    /**
     * use other id generate dolphin style code
     *
     * @param id id
     * @return dolphin style code
     */
    protected long generateCode(String id) {
        if (Objects.isNull(id)) {
            throw new BizException(ErrorCode.PARAMETER_NOT_SET, "id");
        }
        Long code = context.getIdCodeMap().get(id);
        if (Objects.nonNull(code)) {
            return code;
        }

        long newCode;
        try {
            newCode = Long.parseLong(id);
        } catch (NumberFormatException e) {
            newCode = CodeGenerateUtils.getInstance().genCode();
        }
        context.getIdCodeMap().put(id, newCode);
        return newCode;
    }

    protected List<ResourceInfo> convertResourceList(String commentPrefix) {
        List<ResourceInfo> resourceInfoList = new ArrayList<>();
        Optional.of(specNode).map(SpecNode::getFileResources).ifPresent(specFileResources -> specFileResources.stream()
            .filter(Objects::nonNull)
            .forEach(fileResource -> {
                ResourceInfo resourceInfo = new ResourceInfo();
                String fileName = FilenameUtils.concat(StringUtils.defaultString(context.getDefaultFileResourcePath()), fileResource.getName());
                resourceInfo.setResourceName(fileName);
                resourceInfoList.add(resourceInfo);
            }));
        // spec may not provide fileResource information, try to parse from the script content
        if (COMMENT_SET.contains(commentPrefix) && CollectionUtils.isEmpty(resourceInfoList)) {
            Pattern pattern = Pattern.compile(String.format(RESOURCE_REFERENCE_REGEX_FORMAT, commentPrefix));
            Optional.of(specNode).map(SpecNode::getScript).map(SpecScript::getContent).ifPresent(content -> {
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    ResourceInfo resourceInfo = new ResourceInfo();
                    String fileName = FilenameUtils.concat(StringUtils.defaultString(context.getDefaultFileResourcePath()), matcher.group(1));
                    resourceInfo.setResourceName(fileName);
                    resourceInfoList.add(resourceInfo);
                }
            });
        }
        return resourceInfoList;
    }

    protected List<ResourceInfo> convertResourceList() {
        return convertResourceList(null);
    }

    private List<Property> convertLocalParam() {
        Set<String> dataTypeNameSet = Arrays.stream(DataType.values()).map(Enum::name).collect(Collectors.toSet());
        List<Property> localParamList = new ArrayList<>();
        Optional.of(specNode).map(SpecNode::getScript).map(SpecScript::getParameters).ifPresent(
            specVariableList -> specVariableList.stream()
                .filter(v -> VariableScopeType.NODE_PARAMETER.equals(v.getScope()))
                .forEach(v -> {
                    Property property = newProperty(v, dataTypeNameSet);
                    property.setDirect(Direct.IN);
                    localParamList.add(property);
                }));

        Optional.of(specNode).map(SpecNode::getOutputs).ifPresent(
            outputList -> outputList.stream()
                .filter(SpecVariable.class::isInstance)
                .map(SpecVariable.class::cast)
                .filter(v -> VariableScopeType.NODE_CONTEXT.equals(v.getScope()))
                .filter(v -> VariableType.NODE_OUTPUT.equals(v.getType()))
                .forEach(v -> {
                    Property property = newProperty(v, dataTypeNameSet);
                    property.setDirect(Direct.OUT);
                    localParamList.add(property);
                })
        );
        return localParamList;
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

    private Priority convertPriority(Integer specPriority) {
        if (Objects.isNull(specPriority)) {
            return Priority.MEDIUM;
        }
        switch (specPriority) {
            case 4:
                return Priority.HIGHEST;
            case 3:
                return Priority.HIGH;
            case 1:
                return Priority.LOW;
            case 0:
                return Priority.LOWEST;
            case 2:
            default:
                return Priority.MEDIUM;
        }
    }

}
