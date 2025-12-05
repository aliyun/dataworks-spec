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

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.enums.DependencyType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRecurrenceType;
import com.aliyun.dataworks.common.spec.domain.enums.TriggerType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.interfaces.Output;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDoWhile;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecForEach;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScheduleStrategy;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 聿剑
 * @date 2023/11/9
 */
public class DataWorksNodeAdapter implements DataWorksNode, DataWorksNodeAdapterContextAware {
    public static final String TIMEOUT = "alisaTaskKillTimeout";
    public static final String TIMEOUT_UNIT = "killTimeoutUnit";
    public static final String IGNORE_BRANCH_CONDITION_SKIP = "ignoreBranchConditionSkip";
    public static final String LOOP_COUNT = "loopCount";
    public static final String PARALLELISM = "parallelism";
    public static final String STREAM_LAUNCH_MODE = "streamLaunchMode";
    public static final String SETTINGS_JSON_LINKED_ROLE_ARN = "linkedRoleArn";
    public static final String SETTINGS_JSON_DATASET_INFO = "datasetInfo";
    public static final Integer NODE_TYPE_NORMAL = 0;
    public static final Integer NODE_TYPE_MANUAL = 1;
    public static final Integer NODE_TYPE_PAUSE = 2;
    public static final Integer NODE_TYPE_SKIP = 3;
    public static final Integer NODE_TYPE_NONE_AUTO = 4;

    private static final Logger logger = LoggerFactory.getLogger(DataWorksNodeAdapter.class);
    private static final String DELAY_SECONDS = "delaySeconds";
    private static final List<String> METADATA_KEYS = Lists.newArrayList("sourceType", "sourceResourceUuid");

    /**
     * @author 聿剑
     * @date 2024/6/19
     */
    @Data
    @ToString
    @Builder
    public static class Context {
        private boolean deployToScheduler;
    }

    protected final DataWorksWorkflowSpec specification;
    protected final Specification<DataWorksWorkflowSpec> spec;
    protected final SpecEntityDelegate<? extends SpecRefEntity> delegate;
    protected Context context;

    public DataWorksNodeAdapter(Specification<DataWorksWorkflowSpec> specification, SpecRefEntity specEntity) {
        this.spec = specification;
        this.specification = this.spec.getSpec();
        this.delegate = new SpecEntityDelegate<>(specEntity);
        this.context = Context.builder().build();
    }

    public DataWorksNodeAdapter(Specification<DataWorksWorkflowSpec> specification, SpecRefEntity specEntity, Context context) {
        this.spec = specification;
        this.specification = this.spec.getSpec();
        this.delegate = new SpecEntityDelegate<>(specEntity);
        this.context = context;
    }

    @Override
    public DwNodeDependentTypeInfo getDependentType(Function<List<SpecNodeOutput>, List<Long>> getNodeIdsByOutputs) {
        List<SpecFlowDepend> flows = ListUtils.emptyIfNull(specification.getFlow());
        // if the current node is inner node of a workflow, use the workflow's dependency list to get dependency type
        SpecWorkflow outerWorkflow = Optional.ofNullable(spec)
            .map(Specification::getSpec)
            .map(DataWorksWorkflowSpec::getWorkflows).flatMap(wfs ->
                wfs.stream().filter(wf ->
                    ListUtils.emptyIfNull(wf.getNodes()).stream().anyMatch(n ->
                        StringUtils.equalsIgnoreCase(n.getId(), delegate.getId()))).findFirst())
            .orElse(null);
        if (outerWorkflow != null) {
            flows = outerWorkflow.getDependencies();
        }

        SpecFlowDepend specNodeFlowDepend = ListUtils.emptyIfNull(flows).stream()
            .filter(fd -> StringUtils.equalsIgnoreCase(delegate.getId(), fd.getNodeId().getId()))
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
        DataWorksNodeCodeAdapter codeAdapter = new DataWorksNodeCodeAdapter(delegate.getObject());
        codeAdapter.setContext(context);
        return codeAdapter.getCode();
    }

    @Override
    public List<Input> getInputs() {
        return new DataWorksNodeInputOutputAdapter(this.spec, delegate.getObject()).getInputs();
    }

    @Override
    public List<Output> getOutputs() {
        return new DataWorksNodeInputOutputAdapter(this.spec, delegate.getObject()).getOutputs();
    }

    @Override
    public List<InputContext> getInputContexts() {
        return new DataWorksNodeInputOutputAdapter(this.spec, delegate.getObject()).getInputContexts();
    }

    @Override
    public List<OutputContext> getOutputContexts() {
        return new DataWorksNodeInputOutputAdapter(this.spec, delegate.getObject()).getOutputContexts();
    }

    @Override
    public String getParaValue() {
        return Optional.ofNullable(delegate.getScript()).map(SpecScript::getRuntime)
            .map(SpecScriptRuntime::getCommand)
            .map(cmd -> {
                if (StringUtils.equalsIgnoreCase(CodeProgramType.DIDE_SHELL.name(), cmd)
                    || StringUtils.equalsIgnoreCase(CodeProgramType.PYTHON.name(), cmd)) {
                    return getShellParaValue();
                }

                if (ListUtils.emptyIfNull(delegate.getScript().getParameters()).stream()
                    .anyMatch(v -> VariableType.NO_KV_PAIR_EXPRESSION.equals(v.getType()))) {
                    return ListUtils.emptyIfNull(delegate.getScript().getParameters()).stream()
                        .filter(v -> VariableType.NO_KV_PAIR_EXPRESSION.equals(v.getType()))
                        .findAny()
                        .map(SpecVariable::getValue).orElse(null);
                }

                return getKvParaValue();
            })
            .or(() -> Optional.ofNullable(getKvParaValue()))
            .orElse("");
    }

    private String getKvParaValue() {
        return Optional.ofNullable(delegate.getScript()).map(SpecScript::getParameters)
            .map(parameters -> parameters.stream()
                .filter(v -> v.getReferenceVariable() == null)
                .map(p -> p.getName() + "=" + p.getValue()).collect(Collectors.joining(" ")))
            .orElse(null);
    }

    private String getShellParaValue() {
        return ListUtils.emptyIfNull(delegate.getScript().getParameters()).stream()
            .sorted(Comparator.comparing(SpecVariable::getName))
            .filter(v -> v.getReferenceVariable() == null && v.getValue() != null)
            .map(SpecVariable::getValue).collect(Collectors.joining(" "));
    }

    @Override
    public Map<String, Object> getExtConfig() {
        final Map<String, Object> extConfig = new HashMap<>();
        SpecNode specNode = (SpecNode)delegate.getObject();
        Optional.ofNullable(specNode.getTimeout()).filter(timeout -> timeout > 0).ifPresent(timeout ->
            extConfig.put(TIMEOUT, specNode.getTimeout()));

        Optional.ofNullable(specNode.getTimeoutUnit()).ifPresent(timeoutUnit ->
            extConfig.put(TIMEOUT_UNIT, timeoutUnit));

        Optional.ofNullable(specNode.getIgnoreBranchConditionSkip()).ifPresent(ignoreBranchConditionSkip ->
            extConfig.put(IGNORE_BRANCH_CONDITION_SKIP, BooleanUtils.isTrue(ignoreBranchConditionSkip)));

        Optional.ofNullable(specNode.getDoWhile()).map(SpecDoWhile::getMaxIterations).ifPresent(maxIterations ->
            extConfig.put(LOOP_COUNT, maxIterations));

        Optional.ofNullable(specNode.getDoWhile()).map(SpecDoWhile::getParallelism).ifPresent(parallelism ->
            extConfig.put(PARALLELISM, parallelism));

        Optional.ofNullable(specNode.getForeach()).map(SpecForEach::getMaxIterations).ifPresent(maxIterations ->
            extConfig.put(LOOP_COUNT, maxIterations));

        Optional.ofNullable(specNode.getForeach()).map(SpecForEach::getParallelism).ifPresent(parallelism ->
            extConfig.put(PARALLELISM, parallelism));

        Optional.ofNullable(specNode.getTrigger()).map(SpecTrigger::getDelaySeconds).ifPresent(delaySeconds ->
            extConfig.put(DELAY_SECONDS, delaySeconds));

        Optional.ofNullable(specNode.getScript()).map(SpecScript::getRuntime)
            .map(SpecScriptRuntime::getStreamJobConfig)
            .map(emrJobConfig -> emrJobConfig.get(STREAM_LAUNCH_MODE))
            .map(String::valueOf).filter(StringUtils::isNumeric)
            .map(Integer::valueOf)
            .ifPresent(i -> extConfig.put(STREAM_LAUNCH_MODE, i));

        Optional.ofNullable(specNode.getMetadata()).ifPresent(metadata ->
            METADATA_KEYS.stream().filter(metadata::containsKey).forEach(key -> extConfig.put(key, metadata.get(key))));

        return extConfig;
    }

    @Override
    public Integer getNodeType() {
        if (delegate.getObject() instanceof SpecWorkflow) {
            // not scheduler type trigger workflow, means TriggerWorkflow
            if (((SpecWorkflow)delegate.getObject()).getTrigger() != null
                && TriggerType.CUSTOM.equals(((SpecWorkflow)delegate.getObject()).getTrigger().getType())) {
                return NODE_TYPE_MANUAL;
            }

            return Optional.ofNullable(((SpecWorkflow)delegate.getObject()).getStrategy())
                .map(SpecScheduleStrategy::getRecurrenceType)
                .map(DataWorksNodeAdapter::convertRecurrenceType)
                .orElse(null);
        }

        SpecNode specNode = (SpecNode)delegate.getObject();
        // not scheduler type trigger node, means node in TriggerWorkflow
        if (((SpecNode)delegate.getObject()).getTrigger() != null
            && TriggerType.CUSTOM.equals(((SpecNode)delegate.getObject()).getTrigger().getType())) {
            return NODE_TYPE_MANUAL;
        }

        if (Optional.ofNullable(specNode.getTrigger()).map(SpecTrigger::getType).map(TriggerType.MANUAL::equals).orElse(false)) {
            return NODE_TYPE_MANUAL;
        }

        return Optional.ofNullable(specNode.getRecurrence())
            .map(DataWorksNodeAdapter::convertRecurrenceType)
            .orElseThrow(() -> new RuntimeException("not support node type: " + specNode));
    }

    private static Integer convertRecurrenceType(NodeRecurrenceType nodeRecurrenceType) {
        switch (nodeRecurrenceType) {
            case PAUSE:
                return NODE_TYPE_PAUSE;
            case SKIP:
                return NODE_TYPE_SKIP;
            case NORMAL:
                return NODE_TYPE_NORMAL;
            case NONE_AUTO:
                return NODE_TYPE_NONE_AUTO;
        }
        return null;
    }

    @Override
    public Integer getPrgType(Function<String, Integer> getNodeTypeByName) {
        SpecScriptRuntime runtime = Optional.ofNullable(delegate.getScript()).map(SpecScript::getRuntime)
            .orElseThrow(() -> new SpecException("node runtime info not found: " + delegate.getScript()));

        return Optional.ofNullable(runtime.getCommandTypeId())
            .orElseGet(() -> Optional.ofNullable(runtime.getCommand())
                .map(getNodeTypeByName)
                .orElseThrow(() -> new SpecException("unknown node command runtime: " + runtime)));
    }

    @Override
    public String getAdvanceSettings() {
        Map<String, Object> settings = new HashMap<>();
        Optional.ofNullable(delegate.getScript()).map(SpecScript::getRuntime).map(SpecScriptRuntime::getEmrJobConfig)
            .filter(MapUtils::isNotEmpty)
            .ifPresent(settings::putAll);
        Optional.ofNullable(delegate.getScript()).map(SpecScript::getRuntime).map(SpecScriptRuntime::getCdhJobConfig)
            .filter(MapUtils::isNotEmpty)
            .ifPresent(settings::putAll);
        Optional.ofNullable(delegate.getScript()).map(SpecScript::getRuntime).map(SpecScriptRuntime::getAdbJobConfig)
            .filter(MapUtils::isNotEmpty)
            .ifPresent(settings::putAll);
        Optional.ofNullable(delegate.getScript()).map(SpecScript::getRuntime).map(SpecScriptRuntime::getLindormJobConfig)
            .filter(MapUtils::isNotEmpty)
            .ifPresent(settings::putAll);
        Optional.ofNullable(delegate.getScript()).map(SpecScript::getRuntime).map(SpecScriptRuntime::getSparkConf)
            .filter(MapUtils::isNotEmpty)
            .ifPresent(settings::putAll);
        return MapUtils.isEmpty(settings) ? null : JSON.toJSONString(settings);
    }

    @Override
    public String getSettingsJson() {
        Map<String, Object> settings = new HashMap<>();
        Optional.ofNullable(delegate.getScript()).map(SpecScript::getRuntime).map(SpecScriptRuntime::getLinkedRoleArn)
            .filter(StringUtils::isNotBlank)
            .ifPresent(x -> settings.put(SETTINGS_JSON_LINKED_ROLE_ARN, x));

        Optional.ofNullable(delegate.getObject())
            .filter(x -> x instanceof SpecNode)
            .map(x -> (SpecNode)x)
            .map(SpecNode::getDatasets)
            .ifPresent(x -> settings.put(SETTINGS_JSON_DATASET_INFO, JSON.toJSONString(x)));

        return MapUtils.isEmpty(settings) ? null : JSON.toJSONString(settings);
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    @Override
    public String getQuota() {
        return Optional.ofNullable(delegate.getScript())
            .map(SpecScript::getRuntime)
            .map(SpecScriptRuntime::getMaxComputeConf)
            .map(conf -> conf.get("quota"))
            .map(String::valueOf)
            .orElse(null);
    }
}