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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DolphinSchedulerV2Context;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.ProcessDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.process.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.subprocess.SubProcessParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;

import com.google.common.base.Joiner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * for parent subprocess Node, only find subprocess last node (multi node?) output name
 * for child subprocess Node, only find subprocess first node( multi Node?) input name
 *
 * @author 聿剑
 * @date 2022/10/24
 */
public class SubProcessParameterConverter extends AbstractParameterConverter<SubProcessParameters> {
    public SubProcessParameterConverter(
            DagData processMeta, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo, UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    /**
     * virtual node (start node)
     * /    \      \
     * node node    node
     * \     |      /
     * virtual node  (end node)
     */
    @Override
    public List<DwNode> convertParameter() {
        //end node
        DwNode end = newDwNode(taskDefinition);
        end.setType(CodeProgramType.VIRTUAL.name());
        if (end.getInputs() == null) {
            end.setInputs(new ArrayList<>());
        }
        end.getInputs().addAll(inputIoList());
        super.setOutputs(end);

        //begin node
        DwNode start = newDwNode(taskDefinition);
        start.setName(start.getName() + "_start");
        start.setType(CodeProgramType.VIRTUAL.name());
        if (start.getInputs() == null) {
            start.setInputs(new ArrayList<>());
        }
        super.setInputs(start, listPreTasks());

        //outputs
        String out = getDefaultNodeOutput(processMeta, taskDefinition.getName()) + ".virtual.start";
        setBeginNodeOutputs(start, out);
        //set task input to virtual.start when subprocess root task converting
        return Arrays.asList(start, end);
    }

    private void setBeginNodeOutputs(DwNode dwNode, String out) {
        DwNodeIo output = new DwNodeIo();
        output.setData(out);
        output.setParseType(1);
        output.setNodeRef(dwNode);
        dwNode.setOutputs(new ArrayList<>(Collections.singletonList(output)));
    }

    /**
     * override and set do nothing
     */
    @Override
    public void setOutputs(DwNode dwNode) {
    }

    /**
     * override and set do nothing
     */
    @Override
    public void setInputs(DwNode dwNode, Set<TaskDefinition> preTasks) {
    }

    public List<NodeIo> inputIoList() {
        final DolphinSchedulerV2Context context = DolphinSchedulerV2Context.getContext();
        List<NodeIo> dwNodeIos = new ArrayList<>();
        long relationProcessCode = parameter.getProcessDefinitionCode();
        ProcessDefinition depProcessDefinition = context.getProcessCodeMap().get(relationProcessCode);
        List<TaskDefinition> taskDefinitions = context.getProcessCodeTaskRelationMap().get(relationProcessCode);
        Set<Long> preTasks = findPreTaskNodes(relationProcessCode);
        for (TaskDefinition task : CollectionUtils.emptyIfNull(taskDefinitions)) {
            if (isLeafNode(preTasks, task.getCode())) {
                DwNodeIo dwNodeIo = taskToNodeIo(task, depProcessDefinition);
                dwNodeIos.add(dwNodeIo);
            }
        }
        return dwNodeIos;
    }

    /**
     * if taskCode not in preTask relation,that means this task is not a downstream dependent
     * aka a leaf node
     */
    private boolean isLeafNode(Set<Long> preTasks, Long code) {
        return !preTasks.contains(code);
    }

    private DwNodeIo taskToNodeIo(TaskDefinition depTask, ProcessDefinition processDefinition) {
        DwNodeIo crossProjectDepend = new DwNodeIo();
        crossProjectDepend.setParseType(1);
        crossProjectDepend.setData(Joiner.on(".").join(
                //current projectName
                converterContext.getProject().getName(),
                depTask.getProjectName(),
                processDefinition.getName(),
                depTask.getName()));
        return crossProjectDepend;
    }

    private Set<Long> findPreTaskNodes(Long code) {
        return DolphinSchedulerV2Context.getContext().getDagDatas().stream()
                .filter(dag -> dag.getProcessDefinition().getCode() == code)
                .map(dag -> dag.getProcessTaskRelationList())
                .flatMap(List::stream).map(s -> s.getPreTaskCode()).collect(Collectors.toSet());
    }

    /**
     * 1.建立一个虚拟节点
     * 2. 上游process依赖
     */

    //private void processSubProcessDefinitionDepends() {
    //    // for SubProcess Type
    //    ListUtils.emptyIfNull(dwWorkflowList).forEach(workflow -> ListUtils.emptyIfNull(workflow.getNodes()).stream()
    //            .filter(n -> StringUtils.equalsIgnoreCase(TaskType.SUB_PROCESS.name(), ((DwNode) n).getRawNodeType()))
    //            .forEach(subProcessNode -> {
    //                SubProcessParameters subProcessParameter =
    //                        GsonUtils.fromJsonString(subProcessNode.getCode(),
    //                                new TypeToken<SubProcessParameters>() {}.getType());
    //                converterContext.getDolphinSchedulerPackage().getProcessDefinitions().values().stream().map(defList ->
    //                                ListUtils.emptyIfNull(defList).stream()
    //                                        .filter(df -> subProcessParameter != null)
    //                                        .filter(df -> Objects.equals(df.getProcessDefinition().getCode(),
    //                                                subProcessParameter.getProcessDefinitionCode()))
    //                                        .findFirst()
    //                                        .flatMap(proDef -> dwWorkflowList.stream()
    //                                                .filter(wf -> StringUtils.equals(toWorkflowName(proDef.getProcessDefinition()), wf.getName()))
    //                                                .findFirst()
    //                                                .map(wf -> addStartEndNodeToDependedWorkflow(subProcessNode, proDef.getProcessDefinition(), wf)))
    //                                        .orElse(new DwNodeIo()))
    //                        .filter(io -> StringUtils.isNotBlank(io.getData()))
    //                        .findFirst()
    //                        .ifPresent(endNodeOut -> ListUtils.emptyIfNull(workflow.getNodes()).stream()
    //                                // set children of sub process node depends on end node of depend workflow
    //                                .filter(n -> ListUtils.emptyIfNull(n.getInputs()).stream().anyMatch(in ->
    //                                        ListUtils.emptyIfNull(subProcessNode.getOutputs()).stream().anyMatch(out ->
    //                                                StringUtils.equalsIgnoreCase(out.getData(), in.getData()))))
    //                                .forEach(child -> ListUtils.emptyIfNull(child.getInputs()).stream()
    //                                        .filter(in -> ListUtils.emptyIfNull(subProcessNode.getOutputs()).stream().anyMatch(depOut ->
    //                                                StringUtils.equalsIgnoreCase(in.getData(), depOut.getData())))
    //                                        .forEach(in -> in.setData(endNodeOut.getData()))));
    //            }));
    //}

    /**
     * add start, end node for sub process workflow - set start as parent of all nodes that has no parents - set end as
     * child of all nodes that has no children - set start as child of sub process node
     *
     * @param subProcessNode
     * @param proDef
     * @param wf
     * @return output of end node
     */
    private DwNodeIo addStartEndNodeToDependedWorkflow(Node subProcessNode, ProcessDefinition proDef, DwWorkflow wf) {
        DwNode startNode = new DwNode();
        startNode.setType(CodeProgramType.VIRTUAL.name());
        startNode.setWorkflowRef(wf);
        DwNodeIo startNodeOutput = new DwNodeIo();
        startNodeOutput.setData(Joiner.on(".").join(converterContext.getProject(), wf.getName(), "start"));
        startNodeOutput.setParseType(1);
        startNode.setOutputs(Collections.singletonList(startNodeOutput));
        startNode.setInputs(new ArrayList<>());
        ListUtils.emptyIfNull(subProcessNode.getOutputs()).stream().findFirst().ifPresent(
                depOut -> startNode.getInputs().add(depOut));

        DwNode endNode = new DwNode();
        endNode.setType(CodeProgramType.VIRTUAL.name());
        endNode.setWorkflowRef(wf);
        DwNodeIo endNodeOutput = new DwNodeIo();
        endNodeOutput.setData(Joiner.on(".").join(converterContext.getProject(), wf.getName(), "end"));
        endNodeOutput.setParseType(1);
        endNode.setOutputs(Collections.singletonList(endNodeOutput));
        endNode.setInputs(new ArrayList<>());

        ListUtils.emptyIfNull(wf.getNodes()).forEach(node -> {
            String prefix = Joiner.on(".").join(
                    converterContext.getProject().getName(), proDef.getProjectName());
            if (ListUtils.emptyIfNull(node.getInputs()).stream()
                    .noneMatch(in -> StringUtils.startsWithIgnoreCase(in.getData(), prefix))) {
                node.getInputs().add(startNodeOutput);
            }

            if (ListUtils.emptyIfNull(wf.getNodes()).stream()
                    .map(Node::getInputs)
                    .flatMap(List::stream)
                    .noneMatch(in -> ListUtils.emptyIfNull(node.getOutputs()).stream().anyMatch(out ->
                            StringUtils.equalsIgnoreCase(in.getData(), out.getData())))) {
                ListUtils.emptyIfNull(node.getOutputs()).stream().findFirst().ifPresent(
                        out -> endNode.getInputs().add(out));
            }
        });
        wf.getNodes().add(startNode);
        wf.getNodes().add(endNode);

        return endNodeOutput;
    }
}
