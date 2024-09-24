/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.parameters.AbstractParameters;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import org.apache.commons.collections4.ListUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-04
 */
public class TaskDefinitionListConverter extends AbstractCommonConverter<List<TaskDefinition>> {

    private final DataWorksWorkflowSpec spec;

    private final SpecWorkflow workflow;

    public TaskDefinitionListConverter(DataWorksWorkflowSpec spec, SpecWorkflow workflow, List<TaskDefinition> result,
        FlowSpecConverterContext context) {
        super(Objects.nonNull(result) ? result : new ArrayList<>(), context);
        this.spec = spec;
        this.workflow = workflow;
    }

    public TaskDefinitionListConverter(DataWorksWorkflowSpec spec, SpecWorkflow workflow, FlowSpecConverterContext context) {
        this(spec, workflow, null, context);
    }

    @Override
    public List<TaskDefinition> convert() {
        if (Objects.isNull(spec)) {
            return result;
        }
        // Determine whether to convert the contents of workflow or spec based on whether workflow is null
        if (Objects.isNull(workflow)) {
            return convertNodeList(spec.getNodes());
        } else {
            return convertNodeList(workflow.getNodes());
        }
    }

    private List<TaskDefinition> convertNodeList(List<SpecNode> specNodeList) {
        return ListUtils.emptyIfNull(specNodeList).stream()
            .filter(Objects::nonNull)
            .map(specNode -> {
                AbstractNodeConverter<? extends AbstractParameters> nodeConverter = NodeConverterFactory.create(specNode, context);
                return nodeConverter.convert();
            }).collect(Collectors.toList());
    }
}
