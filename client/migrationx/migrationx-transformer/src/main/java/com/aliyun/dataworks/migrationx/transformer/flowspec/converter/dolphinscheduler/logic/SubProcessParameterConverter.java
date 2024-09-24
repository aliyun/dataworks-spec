/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.logic;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.subprocess.SubProcessParameters;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.AbstractParameterConverter;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-26
 */
public class SubProcessParameterConverter extends AbstractParameterConverter<SubProcessParameters> {
    public SubProcessParameterConverter(DataWorksWorkflowSpec spec, SpecWorkflow specWorkflow, TaskDefinition taskDefinition,
        DolphinSchedulerV3ConverterContext context) {
        super(spec, specWorkflow, taskDefinition, context);
    }

    /**
     * Each node translates the specific logic of the parameters
     */
    @Override
    protected void convertParameter(SpecNode specNode) {
    }
}
