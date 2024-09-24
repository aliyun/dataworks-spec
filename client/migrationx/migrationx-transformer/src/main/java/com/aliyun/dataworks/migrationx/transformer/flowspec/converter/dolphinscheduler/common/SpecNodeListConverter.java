/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.TaskType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.parameters.AbstractParameters;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;
import org.apache.commons.collections4.ListUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-10
 */
public class SpecNodeListConverter extends AbstractCommonConverter<List<SpecNode>> {

    private final List<TaskDefinition> taskDefinitionList;

    private static final List<TaskType> TASK_TYPE_ORDER;

    private final SpecWorkflow specWorkflow;

    private final DataWorksWorkflowSpec spec;

    static {
        List<TaskType> taskTypeOrderList = new ArrayList<>();
        taskTypeOrderList.add(TaskType.SQL);
        taskTypeOrderList.add(TaskType.SHELL);
        taskTypeOrderList.add(TaskType.PYTHON);
        taskTypeOrderList.add(TaskType.SPARK);
        taskTypeOrderList.add(TaskType.MR);
        taskTypeOrderList.add(TaskType.SQOOP);
        taskTypeOrderList.add(TaskType.SUB_PROCESS);
        taskTypeOrderList.add(TaskType.DEPENDENT);
        taskTypeOrderList.add(TaskType.CONDITIONS);
        TASK_TYPE_ORDER = Collections.unmodifiableList(taskTypeOrderList);
    }

    public SpecNodeListConverter(DataWorksWorkflowSpec spec, SpecWorkflow specWorkflow, List<TaskDefinition> taskDefinitionList,
        DolphinSchedulerV3ConverterContext context) {
        super(context);
        this.spec = spec;
        this.taskDefinitionList = taskDefinitionList;
        this.specWorkflow = specWorkflow;
    }

    /**
     * convert to T type object
     *
     * @return T type object
     */
    @Override
    public List<SpecNode> convert() {
        Map<String, List<TaskDefinition>> taskTypeMap = ListUtils.emptyIfNull(taskDefinitionList).stream()
            .collect(Collectors.groupingBy(TaskDefinition::getTaskType));
        TASK_TYPE_ORDER.forEach(taskType -> ListUtils.emptyIfNull(taskTypeMap.get(taskType.name())).forEach(this::convertTaskDefinition));
        return Optional.ofNullable(specWorkflow).map(SpecWorkflow::getNodes).orElse(Optional.ofNullable(spec).map(DataWorksWorkflowSpec::getNodes)
            .orElse(Collections.emptyList()));
    }

    private void convertTaskDefinition(TaskDefinition taskDefinition) {
        AbstractParameterConverter<AbstractParameters> converter = ParameterConverterFactory.create(spec, specWorkflow, taskDefinition, context);
        converter.convert();
        /*
        A dolphin node may be converted into multiple spec nodes. You need to identify the beginning and end of the converted spec nodes of each
        dolphin node for subsequent node relationship analysis.
         */
        context.getNodeHeadMap().put(taskDefinition.getCode(), converter.getHeadList());
        context.getNodeTailMap().put(taskDefinition.getCode(), converter.getTailList());
    }
}
