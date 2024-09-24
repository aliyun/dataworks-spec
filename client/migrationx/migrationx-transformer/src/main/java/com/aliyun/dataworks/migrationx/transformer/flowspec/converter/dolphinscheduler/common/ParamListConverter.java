/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.Direct;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.Property;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;
import org.apache.commons.collections4.ListUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-04
 */

public class ParamListConverter extends AbstractCommonConverter<List<SpecVariable>> {

    public static final String SYSTEM_VARIABLE_TAG = "$";

    private final List<Property> paramList;

    private final TaskDefinition taskDefinition;

    public ParamListConverter(List<Property> paramList, DolphinSchedulerV3ConverterContext context) {
        this(paramList, null, context);
    }

    public ParamListConverter(List<Property> paramList, TaskDefinition taskDefinition, DolphinSchedulerV3ConverterContext context) {
        super(context);
        this.paramList = paramList;
        this.taskDefinition = taskDefinition;
    }

    @Override
    public List<SpecVariable> convert() {
        return ListUtils.emptyIfNull(paramList).stream().map(p -> {
                // don't convert global out param
                if (Objects.isNull(taskDefinition) && Direct.OUT.equals(p.getDirect())) {
                    return null;
                }

                SpecVariable specVariable = new SpecVariable();
                specVariable.setId(generateUuid());
                specVariable.setName(p.getProp());
                specVariable.setValue(p.getValue());
                specVariable.setDescription(p.getType().name());
                if (Direct.IN.equals(p.getDirect())) {
                    if (specVariable.getValue().startsWith(SYSTEM_VARIABLE_TAG)) {
                        specVariable.setType(VariableType.SYSTEM);
                    } else {
                        specVariable.setType(VariableType.CONSTANT);
                    }
                    specVariable.setScope(Objects.isNull(taskDefinition) ? VariableScopeType.FLOW : VariableScopeType.NODE_PARAMETER);
                } else {
                    specVariable.setType(VariableType.NODE_OUTPUT);
                    specVariable.setScope(VariableScopeType.NODE_CONTEXT);
                }
                return specVariable;
            }
        ).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
