/*
 * Copyright (c) 2024, Alibaba Cloud;
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aliyun.dataworks.common.spec.domain.dw.nodemodel;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.adapter.SpecNodeAdapter;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.enums.DependencyType;
import com.aliyun.dataworks.common.spec.domain.enums.TriggerType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.interfaces.Output;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 聿剑
 * @date 2023/11/9
 */
public class DataWorksNodeAdapter implements SpecNodeAdapter, DataWorksNode {
    public static final String TIMEOUT = "alisaTaskKillTimeout";
    public static final String IGNORE_BRANCH_CONDITION_SKIP = "ignoreBranchConditionSkip";
    public static final Integer NODE_TYPE_NORMAL = 0;
    public static final Integer NODE_TYPE_MANUAL = 1;
    public static final Integer NODE_TYPE_PAUSE = 2;
    public static final Integer NODE_TYPE_SKIP = 3;

    private static final Logger logger = LoggerFactory.getLogger(DataWorksNodeAdapter.class);

    protected final DataWorksWorkflowSpec specification;
    protected final Specification<DataWorksWorkflowSpec> spec;
    protected final SpecNode specNode;

    public DataWorksNodeAdapter(Specification<DataWorksWorkflowSpec> specification, SpecNode specNode) {
        this.spec = specification;
        this.specification = this.spec.getSpec();
        this.specNode = specNode;
    }

    @Override
    public SpecNode getSpecNode() {
        return specNode;
    }

    @Override
    public DwNodeDependentTypeInfo getDependentType(Function<List<SpecNodeOutput>, List<Long>> getNodeIdsByOutputs) {
        SpecFlowDepend specNodeFlowDepend = ListUtils.emptyIfNull(specification.getFlow()).stream()
            .filter(fd -> StringUtils.equalsIgnoreCase(specNode.getId(), fd.getNodeId().getId()))
            .peek(fd -> logger.info("node flow depends source nodeId: {}, depends: {}",
                JSON.toJSONString(fd.getNodeId()), JSON.toJSONString(fd.getDepends())))
            .findFirst().orElse(null);

        return getDependentType(specNodeFlowDepend, getNodeIdsByOutputs);
    }

    private DwNodeDependentTypeInfo getDependentType(SpecFlowDepend specNodeFlowDepend,
        Function<List<SpecNodeOutput>, List<Long>> getNodeIdsByOutputs) {
        Optional<SpecDepend> self = Optional.ofNullable(specNodeFlowDepend).map(SpecFlowDepend::getDepends)
            .orElse(ListUtils.emptyIfNull(null)).stream()
            .filter(dep -> DependencyType.CROSS_CYCLE_SELF.equals(dep.getType()))
            .findAny();

        Optional<SpecDepend> child = Optional.ofNullable(specNodeFlowDepend).map(SpecFlowDepend::getDepends)
            .orElse(ListUtils.emptyIfNull(null)).stream()
            .filter(dep -> DependencyType.CROSS_CYCLE_CHILDREN.equals(dep.getType()))
            .findAny();

        List<SpecDepend> other = Optional.ofNullable(specNodeFlowDepend).map(SpecFlowDepend::getDepends)
            .orElse(ListUtils.emptyIfNull(null)).stream()
            .filter(dep -> DependencyType.CROSS_CYCLE_OTHER_NODE.equals(dep.getType()))
            .collect(Collectors.toList());

        if (child.isPresent() && CollectionUtils.isNotEmpty(other)) {
            logger.error("invalid cross cycle depend, violation between cross cycle depends on children and custom other nodes, depends: {}",
                specNodeFlowDepend);
            throw new RuntimeException("invalid cross cycle depend, violation between cross cycle depends on children and custom other nodes");
        }

        if (self.isPresent() && child.isPresent()) {
            return DwNodeDependentTypeInfo.ofChildAndSelf();
        }

        if (self.isPresent() && CollectionUtils.isNotEmpty(other)) {
            List<SpecNodeOutput> outputs = ListUtils.emptyIfNull(other).stream()
                .map(SpecDepend::getOutput)
                .collect(Collectors.toList());
            return DwNodeDependentTypeInfo.ofUserDefineAndSelf(
                Optional.ofNullable(getNodeIdsByOutputs).map(f -> f.apply(outputs)).orElse(null),
                outputs.stream().map(SpecNodeOutput::getData).collect(Collectors.toList()));
        }

        if (CollectionUtils.isNotEmpty(other)) {
            List<SpecNodeOutput> outputs = ListUtils.emptyIfNull(other).stream()
                .map(SpecDepend::getOutput).collect(Collectors.toList());
            return DwNodeDependentTypeInfo.ofUserDefine(
                Optional.ofNullable(getNodeIdsByOutputs).map(f -> f.apply(outputs)).orElse(null),
                outputs.stream().map(SpecNodeOutput::getData).collect(Collectors.toList()));
        }

        if (child.isPresent()) {
            return DwNodeDependentTypeInfo.ofChild();
        }

        if (self.isPresent()) {
            return DwNodeDependentTypeInfo.ofSelf();
        }

        return DwNodeDependentTypeInfo.ofNode();
    }

    @Override
    public String getCode() {
        DataWorksNodeCodeAdapter codeAdapter = new DataWorksNodeCodeAdapter(specNode);
        return codeAdapter.getCode();
    }

    @Override
    public List<Input> getInputs() {
        return new DataWorksNodeInputOutputAdapter(this.spec, specNode).getInputs();
    }

    @Override
    public List<Output> getOutputs() {
        return new DataWorksNodeInputOutputAdapter(this.spec, specNode).getOutputs();
    }

    @Override
    public List<InputContext> getInputContexts() {
        return new DataWorksNodeInputOutputAdapter(this.spec, specNode).getInputContexts();
    }

    @Override
    public List<OutputContext> getOutputContexts() {
        return new DataWorksNodeInputOutputAdapter(this.spec, specNode).getOutputContexts();
    }

    @Override
    public String getParaValue() {
        return Optional.ofNullable(specNode).map(SpecNode::getScript).map(SpecScript::getRuntime)
            .map(SpecScriptRuntime::getCommand)
            .map(cmd -> {
                if (StringUtils.equalsIgnoreCase(CodeProgramType.DIDE_SHELL.name(), cmd)) {
                    return getShellParaValue();
                }

                if (ListUtils.emptyIfNull(specNode.getScript().getParameters()).stream()
                    .anyMatch(v -> VariableType.NO_KV_PAIR_EXPRESSION.equals(v.getType()))) {
                    return ListUtils.emptyIfNull(specNode.getScript().getParameters()).stream()
                        .filter(v -> VariableType.NO_KV_PAIR_EXPRESSION.equals(v.getType()))
                        .findAny()
                        .map(SpecVariable::getValue).orElse(null);
                }

                return getKvParaValue();
            }).orElse(getKvParaValue());
    }

    private String getKvParaValue() {
        return Optional.ofNullable(specNode).map(SpecNode::getScript).map(SpecScript::getParameters)
            .map(parameters -> parameters.stream()
                .filter(v -> v.getReferenceVariable() == null)
                .map(p -> p.getName() + "=" + p.getValue()).collect(Collectors.joining(" ")))
            .orElse(null);
    }

    private String getShellParaValue() {
        return ListUtils.emptyIfNull(specNode.getScript().getParameters()).stream()
            .sorted(Comparator.comparing(SpecVariable::getName))
            .filter(v -> v.getReferenceVariable() == null && v.getValue() != null)
            .map(SpecVariable::getValue).collect(Collectors.joining(" "));
    }

    @Override
    public String getExtConfig() {
        final Map<String, Object> extConfig = new HashMap<>();
        Optional.ofNullable(specNode.getTimeout()).filter(timeout -> timeout > 0).ifPresent(timeout ->
            extConfig.put(TIMEOUT, specNode.getTimeout()));

        Optional.ofNullable(specNode.getIgnoreBranchConditionSkip()).ifPresent(ignoreBranchConditionSkip ->
            extConfig.put(IGNORE_BRANCH_CONDITION_SKIP, BooleanUtils.isTrue(ignoreBranchConditionSkip)));

        return extConfig.isEmpty() ? null : JSONObject.toJSONString(extConfig);
    }

    @Override
    public Integer getNodeType() {
        if (Optional.ofNullable(specNode.getTrigger()).map(SpecTrigger::getType).map(TriggerType.MANUAL::equals).orElse(false)) {
            return NODE_TYPE_MANUAL;
        }

        return Optional.ofNullable(specNode.getRecurrence()).map(nodeRecurrenceType -> {
            switch (nodeRecurrenceType) {
                case PAUSE:
                    return NODE_TYPE_PAUSE;
                case SKIP:
                    return NODE_TYPE_SKIP;
                case NORMAL:
                    return NODE_TYPE_NORMAL;
            }
            return null;
        }).orElseThrow(() -> new RuntimeException("not support node type: " + specNode));
    }
}
