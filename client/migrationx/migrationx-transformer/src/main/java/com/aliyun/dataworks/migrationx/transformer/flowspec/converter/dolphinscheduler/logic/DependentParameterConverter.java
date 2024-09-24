/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.enums.DependencyType;
import com.aliyun.dataworks.common.spec.domain.interfaces.Output;
import com.aliyun.dataworks.common.spec.domain.noref.SpecAssertIn;
import com.aliyun.dataworks.common.spec.domain.noref.SpecAssertion;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecJoin;
import com.aliyun.dataworks.common.spec.domain.noref.SpecJoinBranch;
import com.aliyun.dataworks.common.spec.domain.noref.SpecLogic;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.DependentRelation;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.DependentItem;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.DependentTaskModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320.task.dependent.DependentParameters;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.AbstractParameterConverter;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;
import com.aliyun.migrationx.common.utils.BeanUtils;
import org.apache.commons.collections4.ListUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-26
 */
public class DependentParameterConverter extends AbstractParameterConverter<DependentParameters> {

    private static final String ASSERTION_FIELD = "status";

    private static final String SPEC_ASSERT_IN_VALUE_SUCCESS = "1";

    private static final SpecAssertion SPEC_ASSERTION = new SpecAssertion();

    private static final SpecScriptRuntime RUNTIME = new SpecScriptRuntime();

    static {
        SPEC_ASSERTION.setField(ASSERTION_FIELD);
        SPEC_ASSERTION.setIn(new SpecAssertIn());
        SPEC_ASSERTION.getIn().setValue(new ArrayList<>());
        SPEC_ASSERTION.getIn().getValue().add(SPEC_ASSERT_IN_VALUE_SUCCESS);

        RUNTIME.setEngine(CodeProgramType.CONTROLLER_JOIN.getCalcEngineType().getLabel());
        RUNTIME.setCommand(CodeProgramType.CONTROLLER_JOIN.getName());
    }

    public DependentParameterConverter(DataWorksWorkflowSpec spec, SpecWorkflow specWorkflow, TaskDefinition taskDefinition,
        DolphinSchedulerV3ConverterContext context) {
        super(spec, specWorkflow, taskDefinition, context);
    }

    /**
     * Each node translates the specific logic of the parameters
     */
    @Override
    protected void convertParameter(SpecNode finalJoinNode) {
        SpecScript script = new SpecScript();
        script.setId(generateUuid());
        script.setRuntime(RUNTIME);
        script.setPath(getScriptPath(finalJoinNode));
        finalJoinNode.setScript(script);

        // build main content
        Map<String, SpecNode> nodeIdMap = buildNodeIdMap();
        Map<String, SpecWorkflow> workflowIdMap = buildWorkFlowIdMap();
        Optional.ofNullable(parameter).map(DependentParameters::getDependence).ifPresent(dependence -> {
            List<SpecNode> nodeList = new ArrayList<>();
            for (int i = 0; i < ListUtils.emptyIfNull(dependence.getDependTaskList()).size(); i++) {
                DependentTaskModel dependentTaskModel = dependence.getDependTaskList().get(i);
                List<SpecNode> subNodeList = ListUtils.emptyIfNull(dependentTaskModel.getDependItemList()).stream()
                    .map(dependItem -> findSubNode(nodeIdMap, dependItem))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                // if spec node depend on the whole workflow, need depend on workflow output
                List<SpecNodeOutput> subNodeOutputList = ListUtils.emptyIfNull(dependentTaskModel.getDependItemList()).stream()
                    .map(dependItem -> findSubNodeOutput(workflowIdMap, dependItem))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                SpecJoin specJoin = newSpecJoin(subNodeList, subNodeOutputList, dependentTaskModel.getRelation());
                SpecNode specNode = copySpecNode(finalJoinNode, specJoin, "-join-" + i);
                addRelation(specNode, subNodeList, subNodeOutputList);
                nodeList.add(specNode);
            }
            SpecJoin specJoin = newSpecJoin(nodeList, null, dependence.getRelation());
            finalJoinNode.setJoin(specJoin);
            addRelation(finalJoinNode, nodeList);
        });
        tailList.add(finalJoinNode);
    }

    /**
     * This method mainly deals with cases that depend on the whole workflow
     *
     * @param nodeIdMap     nodeIdMap
     * @param dependentItem dependentItem
     * @return SpecNode, may be a join node
     */
    private SpecNode findSubNode(Map<String, SpecNode> nodeIdMap, DependentItem dependentItem) {
        if (dependentItem.getDepTaskCode() == 0L) {
            // depend on the whole workflow, don't deal this time
            return null;
        }

        String id = context.getCodeUuidMap().get(dependentItem.getDepTaskCode());
        SpecNode dependentNode = nodeIdMap.get(id);
        if (Objects.isNull(dependentNode)) {
            dependentNode = new SpecNode();
            dependentNode.setId(id);
            dependentNode.getOutputs().add(buildDefaultNodeOutput(dependentNode));
        }
        return dependentNode;
    }

    private SpecNodeOutput findSubNodeOutput(Map<String, SpecWorkflow> workflowIdMap, DependentItem dependentItem) {
        if (dependentItem.getDepTaskCode() == 0L) {
            // depend on the whole workflow, find the workflow output
            String id = context.getCodeUuidMap().get(dependentItem.getDefinitionCode());
            SpecWorkflow specWorkflow = workflowIdMap.get(id);
            return getDefaultOutput(specWorkflow, false);
        }
        return null;
    }

    /**
     * add relation before join node. if the node depend on a whole workflow, need depend on workflow output
     *
     * @param postNode          post join node
     * @param preNodeList       pre node list
     * @param preNodeOutputList pre workflow output list
     */
    private void addRelation(SpecNode postNode, List<SpecNode> preNodeList, List<SpecNodeOutput> preNodeOutputList) {
        SpecFlowDepend specFlowDepend = newSpecFlowDepend();
        specFlowDepend.setNodeId(postNode);
        ListUtils.emptyIfNull(preNodeList).forEach(preNode -> {
            SpecNodeOutput preNodeOutput = getDefaultOutput(preNode);
            postNode.getInputs().add(preNodeOutput);
            postNode.getInputs().addAll(getContextOutputs(preNode));
            specFlowDepend.getDepends().add(new SpecDepend(preNode, DependencyType.NORMAL, preNodeOutput));
        });

        ListUtils.emptyIfNull(preNodeOutputList).forEach(preNodeOutput -> {
            postNode.getInputs().add(preNodeOutput);
            SpecDepend specDepend = new SpecDepend();
            specDepend.setType(DependencyType.NORMAL);
            specDepend.setOutput(preNodeOutput);
            specFlowDepend.getDepends().add(specDepend);
        });
        getWorkflowDependencyList().add(specFlowDepend);
    }

    private void addRelation(SpecNode postNode, List<SpecNode> preNodeList) {
        addRelation(postNode, preNodeList, null);
    }

    private SpecJoin newSpecJoin(List<SpecNode> specNodeList, List<SpecNodeOutput> specNodeOutputList, DependentRelation relation) {
        SpecJoin specJoin = new SpecJoin();
        specJoin.setBranches(new ArrayList<>());
        List<String> branchNameList = new ArrayList<>();
        ListUtils.emptyIfNull(specNodeList).forEach(specNode -> {
            SpecJoinBranch specJoinBranch = buildSpecJoinBranch(specNode);
            specJoin.getBranches().add(specJoinBranch);
            branchNameList.add(specJoinBranch.getName());
        });
        ListUtils.emptyIfNull(specNodeOutputList).forEach(specNodeOutput -> {
            SpecJoinBranch specJoinBranch = buildSpecJoinBranch(specNodeOutput);
            specJoin.getBranches().add(specJoinBranch);
            branchNameList.add(specJoinBranch.getName());
        });
        SpecLogic specLogic = new SpecLogic();
        specLogic.setExpression(String.join(" " + relation.name() + " ", branchNameList));
        specJoin.setLogic(specLogic);
        specJoin.setResultStatus(SPEC_ASSERT_IN_VALUE_SUCCESS);
        return specJoin;
    }

    private SpecJoinBranch buildSpecJoinBranch(SpecNode specNode) {
        SpecJoinBranch specJoinBranch = new SpecJoinBranch();
        specJoinBranch.setNodeId(specNode);
        specJoinBranch.setName("branch-" + specNode.getId());
        specJoinBranch.setOutput(getDefaultOutput(specNode, false));
        specJoinBranch.setAssertion(SPEC_ASSERTION);
        return specJoinBranch;
    }

    private SpecJoinBranch buildSpecJoinBranch(SpecNodeOutput specNodeOutput) {
        SpecJoinBranch specJoinBranch = new SpecJoinBranch();
        specJoinBranch.setName("branch-" + specNodeOutput.getId());
        specJoinBranch.setOutput(specNodeOutput);
        specJoinBranch.setAssertion(SPEC_ASSERTION);
        return specJoinBranch;
    }

    /**
     * use current workflow and others in context to build node id map
     *
     * @return node id map
     */
    private Map<String, SpecNode> buildNodeIdMap() {
        List<SpecNode> nodes = new ArrayList<>(ListUtils.emptyIfNull(getWorkflowNodeList()));
        ListUtils.emptyIfNull(context.getDependSpecification()).stream()
            .map(Specification::getSpec)
            .filter(Objects::nonNull)
            .map(DataWorksWorkflowSpec::getWorkflows)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .map(SpecWorkflow::getNodes)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .forEach(nodes::add);

        return nodes.stream().collect(Collectors.toMap(SpecNode::getId, Function.identity(), (v1, v2) -> v1));
    }

    private Map<String, SpecWorkflow> buildWorkFlowIdMap() {
        List<SpecWorkflow> nodes = new ArrayList<>(Collections.singleton(getWorkFlow()));
        ListUtils.emptyIfNull(context.getDependSpecification()).stream()
            .map(Specification::getSpec)
            .filter(Objects::nonNull)
            .map(DataWorksWorkflowSpec::getWorkflows)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .forEach(nodes::add);

        return nodes.stream().filter(Objects::nonNull).collect(Collectors.toMap(SpecWorkflow::getId, Function.identity(), (v1, v2) -> v1));
    }

    /**
     * copy node, only used in dependent join.
     *
     * @param specNode origin node
     * @param suffix   new suffix
     * @return copied node
     */
    private SpecNode copySpecNode(SpecNode specNode, SpecJoin specJoin, String suffix) {
        SpecNode specNodeCopy = BeanUtils.deepCopy(specNode, SpecNode.class);
        specNodeCopy.setId(generateUuid());
        specNodeCopy.setName(specNodeCopy.getName() + suffix);
        for (Output output : specNodeCopy.getOutputs()) {
            if (output instanceof SpecNodeOutput && Boolean.TRUE.equals(((SpecNodeOutput)output).getIsDefault())) {
                ((SpecNodeOutput)output).setId(generateUuid());
                ((SpecNodeOutput)output).setData(specNodeCopy.getId());
                ((SpecNodeOutput)output).setRefTableName(specNodeCopy.getName());
            } else if (output instanceof SpecRefEntity) {
                ((SpecRefEntity)output).setId(generateUuid());
            }
        }
        getWorkflowNodeList().add(specNodeCopy);

        SpecScript scriptCopy = BeanUtils.deepCopy(specNodeCopy.getScript(), SpecScript.class);
        scriptCopy.setId(generateUuid());
        scriptCopy.setPath(scriptCopy.getPath() + suffix);

        specNodeCopy.setScript(scriptCopy);

        specNodeCopy.setJoin(specJoin);
        return specNodeCopy;
    }
}
