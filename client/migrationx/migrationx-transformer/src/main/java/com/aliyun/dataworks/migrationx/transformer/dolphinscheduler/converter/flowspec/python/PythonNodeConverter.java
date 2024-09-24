/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.python;

import java.util.ArrayList;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.TaskType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.python.PythonParameters;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.AbstractNodeConverter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import org.apache.commons.lang3.StringUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-07
 */
public class PythonNodeConverter extends AbstractNodeConverter<PythonParameters> {

    private static final String RESOURCE_REFERENCE_PREFIX = "##";

    public PythonNodeConverter(SpecNode specNode, FlowSpecConverterContext context) {
        super(specNode, context);
    }

    /**
     * convert spec node to dolphin scheduler task parameters
     *
     * @return dolphin scheduler task parameters
     */
    @Override
    protected PythonParameters convertParameter() {
        PythonParameters pythonParameters = new PythonParameters();
        pythonParameters.setResourceList(new ArrayList<>());

        String content = Optional.ofNullable(specNode.getScript()).map(SpecScript::getContent).orElse(StringUtils.EMPTY);
        pythonParameters.setRawScript(content);

        convertResourceList(RESOURCE_REFERENCE_PREFIX).forEach(pythonParameters.getResourceList()::add);
        return pythonParameters;
    }

    /**
     * judge task type from spec node and set in taskDefinition
     */
    @Override
    protected void setTaskType() {
        result.setTaskType(TaskType.PYTHON.name());
    }
}
