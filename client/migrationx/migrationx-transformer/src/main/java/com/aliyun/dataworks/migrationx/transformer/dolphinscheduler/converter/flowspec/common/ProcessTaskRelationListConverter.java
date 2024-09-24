/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.interfaces.Output;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Constants;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.ConditionType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessTaskRelation;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-04
 */
public class ProcessTaskRelationListConverter extends AbstractCommonConverter<List<ProcessTaskRelation>> {

    private final DataWorksWorkflowSpec spec;

    private final SpecWorkflow workflow;

    public ProcessTaskRelationListConverter(DataWorksWorkflowSpec spec, SpecWorkflow workflow, List<ProcessTaskRelation> result,
        FlowSpecConverterContext context) {
        super(Objects.nonNull(result) ? result : new ArrayList<>(), context);
        this.spec = spec;
        this.workflow = workflow;
    }

    public ProcessTaskRelationListConverter(DataWorksWorkflowSpec spec, SpecWorkflow workflow, FlowSpecConverterContext context) {
        this(spec, workflow, null, context);
    }

    @Override
    public List<ProcessTaskRelation> convert() {
        if (Objects.isNull(spec)) {
            return result;
        }
        Long processDefinitionCode = context.getIdCodeMap().get(spec.getId());
        List<SpecNode> nodeList = spec.getNodes();
        List<SpecFlowDepend> dependList = spec.getFlow();

        // Determine whether to convert the contents of workflow or spec based on whether workflow is null
        if (Objects.nonNull(workflow)) {
            processDefinitionCode = context.getIdCodeMap().get(workflow.getId());
            nodeList = workflow.getNodes();
            dependList = workflow.getDependencies();
        }

        prepareFlow(nodeList, dependList);

        // zero in-degree node set
        Set<Long> nonHeadNodeCodeSet = new HashSet<>();

        // build processTaskRelationList
        Long finalProcessDefinitionCode = processDefinitionCode;
        ListUtils.emptyIfNull(dependList).forEach(flow -> {
            String postNodeId = Optional.ofNullable(flow.getNodeId()).map(SpecNode::getId).orElseThrow(
                () -> new BizException(ErrorCode.PARAMETER_NOT_SET, "nodeId"));
            Long postNodeCode = context.getIdCodeMap().get(postNodeId);
            ListUtils.emptyIfNull(flow.getDepends()).forEach(depend -> {
                String preNodeId = Optional.ofNullable(depend.getNodeId()).map(SpecNode::getId).orElse(null);
                Long preNodeCode = context.getIdCodeMap().get(preNodeId);
                if (Objects.nonNull(preNodeCode) && Objects.nonNull(postNodeCode)) {
                    ProcessTaskRelation processTaskRelation = buildProcessTaskRelation(finalProcessDefinitionCode, preNodeCode, postNodeCode);
                    result.add(processTaskRelation);
                    nonHeadNodeCodeSet.add(postNodeCode);
                }
            });
        });

        // For nodes that do not have in-degrees, we need to add a default relation
        ListUtils.emptyIfNull(nodeList).stream()
            .map(SpecNode::getId)
            .filter(Objects::nonNull)
            .map(id -> context.getIdCodeMap().get(id))
            .filter(code -> Objects.nonNull(code) && !nonHeadNodeCodeSet.contains(code))
            .map(code -> buildProcessTaskRelation(finalProcessDefinitionCode, 0L, code))
            .forEach(result::add);

        return result;
    }

    private ProcessTaskRelation buildProcessTaskRelation(Long processDefinitionCode, Long preNodeCode, Long postNodeCode) {
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setId(generateId(null));
        processTaskRelation.setName("");
        processTaskRelation.setProcessDefinitionVersion(Constants.VERSION_FIRST);
        processTaskRelation.setProjectCode(context.getProjectCode());
        processTaskRelation.setProcessDefinitionCode(processDefinitionCode);
        processTaskRelation.setPreTaskCode(preNodeCode);
        if (preNodeCode == 0L) {
            processTaskRelation.setPreTaskVersion(0);
        } else {
            processTaskRelation.setPreTaskVersion(Constants.VERSION_FIRST);
        }
        processTaskRelation.setPostTaskCode(postNodeCode);
        processTaskRelation.setPostTaskVersion(Constants.VERSION_FIRST);
        processTaskRelation.setConditionType(ConditionType.NONE);
        processTaskRelation.setConditionParams("{}");
        processTaskRelation.setCreateTime(new Date());
        processTaskRelation.setUpdateTime(new Date());
        return processTaskRelation;
    }

    /**
     * prepare flow ,fill the missing nodeId in depend
     *
     * @param nodeList           node list
     * @param specFlowDependList flow depend list
     */
    private void prepareFlow(List<SpecNode> nodeList, List<SpecFlowDepend> specFlowDependList) {
        Map<String, SpecNode> outputDataNodeMap = new HashMap<>();
        ListUtils.emptyIfNull(nodeList).forEach(node -> {
            List<Output> outputs = node.getOutputs();
            ListUtils.emptyIfNull(outputs).stream()
                .filter(output -> Objects.nonNull(output) && output instanceof SpecNodeOutput)
                .map(SpecNodeOutput.class::cast)
                .filter(output -> Objects.nonNull(output.getData()))
                .forEach(output -> outputDataNodeMap.put(output.getData(), node));
        });

        ListUtils.emptyIfNull(specFlowDependList).stream()
            .filter(flow -> Objects.nonNull(flow.getNodeId()) && CollectionUtils.isNotEmpty(flow.getDepends()))
            .forEach(flow -> {
                List<SpecDepend> depends = flow.getDepends();
                depends.stream()
                    .filter(depend -> Objects.isNull(depend.getNodeId()))
                    .forEach(
                        depend -> {
                            String outputData = Optional.ofNullable(depend.getOutput()).map(SpecNodeOutput::getData).orElse(null);
                            depend.setNodeId(outputDataNodeMap.get(outputData));
                        }
                    );
            });
    }
}
