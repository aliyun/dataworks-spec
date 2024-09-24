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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.enums.ArtifactType;
import com.aliyun.dataworks.common.spec.domain.enums.DependencyType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecArtifact;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessTaskRelation;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import com.aliyun.migrationx.common.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-10
 */
@Slf4j
public class SpecFlowDependConverter extends AbstractCommonConverter<List<SpecFlowDepend>> {

    private static final Map<ArtifactType, Class<? extends SpecArtifact>> ARTIFACT_TYPE_CLASS_MAP = new EnumMap<>(ArtifactType.class);

    private final DataWorksWorkflowSpec spec;

    private final SpecWorkflow specWorkflow;

    private final List<ProcessTaskRelation> processTaskRelationList;

    static {
        ARTIFACT_TYPE_CLASS_MAP.put(ArtifactType.TABLE, SpecTable.class);
        ARTIFACT_TYPE_CLASS_MAP.put(ArtifactType.VARIABLE, SpecVariable.class);
        ARTIFACT_TYPE_CLASS_MAP.put(ArtifactType.NODE_OUTPUT, SpecNodeOutput.class);
        ARTIFACT_TYPE_CLASS_MAP.put(ArtifactType.FILE, SpecArtifact.class);
    }

    public SpecFlowDependConverter(DataWorksWorkflowSpec spec, SpecWorkflow specWorkflow, List<ProcessTaskRelation> processTaskRelationList,
        DolphinSchedulerV3ConverterContext context) {
        super(context);
        this.spec = spec;
        this.specWorkflow = specWorkflow;
        this.processTaskRelationList = processTaskRelationList;
    }

    /**
     * convert to T type object
     *
     * @return T type object
     */
    @Override
    public List<SpecFlowDepend> convert() {
        if (Objects.nonNull(specWorkflow)) {
            specWorkflow.setDependencies(convertTaskRelationList(processTaskRelationList));
            return specWorkflow.getDependencies();
        }
        spec.setFlow(convertTaskRelationList(processTaskRelationList));
        return spec.getFlow();
    }

    private List<SpecFlowDepend> convertTaskRelationList(List<ProcessTaskRelation> taskRelationList) {
        List<SpecFlowDepend> flow = Optional.ofNullable(specWorkflow).map(SpecWorkflow::getDependencies).orElse(Optional.ofNullable(spec)
            .map(DataWorksWorkflowSpec::getFlow).orElse(new ArrayList<>()));
        Map<String, List<SpecDepend>> nodeIdDependMap = flow.stream().collect(
            Collectors.toMap(o -> o.getNodeId().getId(), SpecFlowDepend::getDepends));
        for (ProcessTaskRelation processTaskRelation : ListUtils.emptyIfNull(taskRelationList)) {
            long preTaskCode = processTaskRelation.getPreTaskCode();
            long postTaskCode = processTaskRelation.getPostTaskCode();
            // The preceding node code of the root node is 0
            if (preTaskCode == 0L) {
                continue;
            }

            List<SpecNode> preNodeList = context.getNodeTailMap().getOrDefault(preTaskCode, Collections.emptyList());
            List<SpecNode> postNodeList = context.getNodeHeadMap().getOrDefault(postTaskCode, Collections.emptyList());
            postNodeList.forEach(postNode -> dealSingleNodeDependency(postNode, nodeIdDependMap, flow, preNodeList));
        }
        return flow;
    }

    private void dealSingleNodeDependency(SpecNode postNode, Map<String, List<SpecDepend>> nodeIdDependMap, List<SpecFlowDepend> flow,
        List<SpecNode> preNodeList) {
        // The join node has already handled the dependencies before. There's no need to deal with it again
        if (Objects.nonNull(postNode.getJoin())) {
            return;
        }

        List<SpecDepend> specDependList = ListUtils.defaultIfNull(nodeIdDependMap.get(postNode.getId()), new ArrayList<>());
        assert specDependList != null;
        // If the node does not depend on other nodes before, need to create a new dependent node
        if (CollectionUtils.isEmpty(specDependList)) {
            SpecFlowDepend specFlowDepend = newSpecFlowDepend();
            specFlowDepend.setNodeId(postNode);
            specFlowDepend.getDepends().addAll(specDependList);
            flow.add(specFlowDepend);
            nodeIdDependMap.put(postNode.getId(), specDependList);
        }
        // the set is used to avoid duplicate dependencies
        Set<String> preNodeIdSet = specDependList.stream()
            .map(SpecDepend::getNodeId)
            .filter(Objects::nonNull)
            .map(SpecRefEntity::getId)
            .collect(Collectors.toSet());

        for (SpecNode preNode : preNodeList) {
            if (preNodeIdSet.contains(preNode.getId())) {
                continue;
            }
            preNodeIdSet.add(postNode.getId());

            SpecNodeOutput defaultOutput = getDefaultNodeOutput(preNode);
            specDependList.add(new SpecDepend(preNode, DependencyType.NORMAL, defaultOutput));
            postNode.getInputs().addAll(transformOutput2Input(preNode));
        }

        // refer context parameter
        List<SpecVariable> scriptParam = Optional.ofNullable(postNode.getScript()).map(SpecScript::getParameters).orElseGet(() -> {
            SpecScript script = postNode.getScript();
            if (script == null) {
                log.error("node.script not set, node id: {}", postNode.getId());
                return new ArrayList<>();
            } else {
                script.setParameters(new ArrayList<>());
                return script.getParameters();
            }
        });
        ListUtils.emptyIfNull(postNode.getInputs()).stream()
            .filter(input -> input instanceof SpecVariable)
            .map(input -> (SpecVariable)input)
            .filter(specVariable -> VariableScopeType.NODE_CONTEXT.equals(specVariable.getScope()))
            .forEach(specVariable -> {
                SpecVariable param = BeanUtils.deepCopy(specVariable, SpecVariable.class);
                param.setId(generateUuid());
                param.setNode(null);
                param.setReferenceVariable(specVariable);
                scriptParam.add(param);
            });
    }

    private List<? extends Input> transformOutput2Input(SpecNode specNode) {
        List<Input> res = new ArrayList<>();
        ListUtils.emptyIfNull(Optional.ofNullable(specNode).orElseThrow(() -> new BizException(ErrorCode.PARAMETER_NOT_SET, "spec node"))
            .getOutputs()).forEach(output -> {
                if (output instanceof SpecArtifact) {
                    SpecArtifact specArtifactOutput = (SpecArtifact)output;
                    Class<? extends SpecArtifact> clazz = ARTIFACT_TYPE_CLASS_MAP.get(specArtifactOutput.getArtifactType());
                    // input and output are not same entity, input need depend on output. so we need clone it
                    SpecArtifact specArtifactInput = BeanUtils.deepCopy(specArtifactOutput, clazz);

                    if (specArtifactInput instanceof SpecVariable) {
                        SpecVariable specVariableInput = (SpecVariable)specArtifactInput;
                        SpecVariable specVariableOutput = (SpecVariable)specArtifactOutput;
                        SpecNodeOutput defaultOutput = getDefaultNodeOutput(specNode);
                        specVariableOutput.setNode(new SpecDepend(specNode, DependencyType.NORMAL, defaultOutput));
                        specVariableInput.setReferenceVariable(specVariableOutput);
                    }
                    res.add(specArtifactInput);
                }
            }
        );
        return res;
    }

    private SpecNodeOutput getDefaultNodeOutput(SpecNode specNode) {
        return ListUtils.emptyIfNull(specNode.getOutputs()).stream()
            .filter(o -> o instanceof SpecNodeOutput && ((SpecNodeOutput)o).getIsDefault())
            .map(o -> (SpecNodeOutput)o)
            .findFirst().orElse(null);
    }
}
