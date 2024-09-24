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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.v3;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ControllerJoinCode;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DolphinSchedulerV3Context;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.DependentTaskModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.conditions.ConditionsParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.dependent.DependentParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.common.base.Joiner;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

/**
 * @author 聿剑
 * @date 2022/10/27
 */
@Slf4j
public class ConditionsParameterConverter extends AbstractParameterConverter<ConditionsParameters> {
    public ConditionsParameterConverter(DagData processMeta, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<
                    Project, DagData, DataSource, ResourceInfo, UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    /**
     * 用归并节点承接1.3.9 Conditions的判断上游成功失败状态的分支逻辑
     *
     * @throws IOException ex
     * @link <href a="https://yuque.antfin.com/dataworks/wohugx/bv4dom">转换逻辑说明</href>
     */
    @Override
    public List<DwNode> convertParameter() throws IOException {
        log.info("params : {}", taskDefinition.getTaskParams());

        JsonObject param = GsonUtils.fromJsonString(taskDefinition.getTaskParams(), JsonObject.class);
        DependentParameters dependentParameters = null;
        if (param.get("dependence") != null) {
            dependentParameters = GsonUtils.fromJson(param.getAsJsonObject("dependence"), DependentParameters.class);
        }
        if (dependentParameters == null || dependentParameters.getDependTaskList() == null || dependentParameters.getDependTaskList().isEmpty()) {
            log.warn("no dependence param {}", taskDefinition.getTaskParams());
            return Collections.emptyList();
        }
        // 本节点的条件依赖
        List<DependentTaskModel> dependencies = dependentParameters.getDependTaskList();

        final AtomicInteger outerRelationIndex = new AtomicInteger(0);
        List<DwNode> taskDepJoinNodes = ListUtils.emptyIfNull(dependencies)
                .stream()
                .map(dependentTaskModel -> conditionNodeToJoinNode(dependentTaskModel, outerRelationIndex.getAndIncrement()))
                .collect(Collectors.toList());

        final DependentParameters dependency = dependentParameters;
        DwNode joinSuccessNode = newDwNode(taskDefinition);
        joinSuccessNode.setType(CodeProgramType.CONTROLLER_JOIN.name());
        joinSuccessNode.setName(Joiner.on("_").join(joinSuccessNode.getName(), "join", "success"));
        ControllerJoinCode joinSuccessCode = new ControllerJoinCode();
        joinSuccessCode.setBranchList(ListUtils.emptyIfNull(taskDepJoinNodes).stream().map(Node::getOutputs)
                .flatMap(List::stream).map(NodeIo::getData).distinct().map(out -> {
                    ControllerJoinCode.Branch branch = new ControllerJoinCode.Branch();
                    switch (dependency.getRelation()) {
                        case OR:
                            branch.setLogic(1);
                            break;
                        case AND:
                            branch.setLogic(0);
                            break;
                    }
                    branch.setNode(out);
                    branch.setRunStatus(Collections.singletonList("1"));
                    return branch;
                }).collect(Collectors.toList()));
        joinSuccessCode.setResultStatus("1");
        joinSuccessNode.setCode(joinSuccessCode.getContent());
        ListUtils.emptyIfNull(joinSuccessNode.getOutputs()).stream().findFirst().ifPresent(out ->
                out.setData(getDefaultNodeOutput(processMeta, joinSuccessNode.getName())));
        joinSuccessNode.setInputs(ListUtils.emptyIfNull(taskDepJoinNodes).stream()
                .map(Node::getOutputs).flatMap(List::stream).collect(Collectors.toList()));

        DwNode joinFailureNode = newDwNode(taskDefinition);
        joinFailureNode.setType(CodeProgramType.CONTROLLER_JOIN.name());
        joinFailureNode.setName(Joiner.on("_").join(joinFailureNode.getName(), "join", "failure"));
        ControllerJoinCode joinFailureCode = new ControllerJoinCode();
        joinFailureCode.setBranchList(ListUtils.emptyIfNull(taskDepJoinNodes).stream().map(Node::getOutputs)
                .flatMap(List::stream).map(NodeIo::getData).distinct().map(out -> {
                    ControllerJoinCode.Branch branch = new ControllerJoinCode.Branch();
                    switch (dependency.getRelation()) {
                        case OR:
                            branch.setLogic(1);
                            break;
                        case AND:
                            branch.setLogic(0);
                            break;
                    }
                    branch.setNode(out);
                    branch.setRunStatus(Collections.singletonList("1"));
                    return branch;
                }).collect(Collectors.toList()));
        joinFailureCode.setResultStatus("0");
        joinFailureNode.setCode(joinFailureCode.getContent());
        ListUtils.emptyIfNull(joinFailureNode.getOutputs()).stream().findFirst().ifPresent(out ->
                out.setData(getDefaultNodeOutput(processMeta, joinFailureNode.getName())));
        joinFailureNode.setInputs(ListUtils.emptyIfNull(taskDepJoinNodes).stream()
                .map(Node::getOutputs).flatMap(List::stream).collect(Collectors.toList()));
        return Arrays.asList(joinSuccessNode, joinFailureNode);
    }

    private DwNode conditionNodeToJoinNode(DependentTaskModel dependentTaskModel, int index) {
        DwNode joinNode = newDwNode(taskDefinition);
        joinNode.setType(CodeProgramType.CONTROLLER_JOIN.name());
        joinNode.setName(Joiner.on("_").join(joinNode.getName(), "join", index));
        ListUtils.emptyIfNull(joinNode.getOutputs()).stream().findFirst().ifPresent(out ->
                out.setData(getDefaultNodeOutput(processMeta, joinNode.getName())));

        List<ControllerJoinCode.Branch> branchList = ListUtils.emptyIfNull(dependentTaskModel.getDependItemList())
                .stream()
                .filter(dependentItem -> dependentItem.getStatus() != null)
                .map(dependentItem -> {
                    ControllerJoinCode.Branch branch = new ControllerJoinCode.Branch();
                    switch (dependentTaskModel.getRelation()) {
                        case OR:
                            branch.setLogic(1);
                            break;
                        case AND:
                            branch.setLogic(0);
                            break;
                    }
                    TaskDefinition definition = DolphinSchedulerV3Context.getContext().getTaskCodeMap().get(dependentItem.getDepTaskCode());
                    if (definition == null) {
                        log.error("can not get task definition by code {}", dependentItem.getDepTaskCode());
                        return null;
                    }
                    branch.setNode(getDefaultNodeOutput(processMeta, definition.getName()));
                    switch (dependentItem.getStatus()) {
                        case FAILURE:
                            branch.setRunStatus(Collections.singletonList("0"));
                            break;
                        case SUCCESS:
                            branch.setRunStatus(Collections.singletonList("1"));
                            break;
                    }
                    return branch;
                }).collect(Collectors.toList());

        ControllerJoinCode joinCode = new ControllerJoinCode();
        joinCode.setBranchList(branchList);
        switch (dependentTaskModel.getRelation()) {
            case AND:
                joinCode.setResultStatus("0");
                break;
            case OR:
                joinCode.setResultStatus("1");
                break;
        }
        joinNode.setCode(joinCode.getContent());
        return joinNode;
    }
}
