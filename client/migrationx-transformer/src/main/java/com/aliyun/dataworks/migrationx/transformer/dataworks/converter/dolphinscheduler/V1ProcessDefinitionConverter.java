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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.WorkflowParameter;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowVersion;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Datasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.ProcessMeta;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.TaskType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.AbstractParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.subprocess.SubProcessParameters;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.google.common.base.Joiner;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 聿剑
 * @date 2022/10/19
 */
@Slf4j
public class V1ProcessDefinitionConverter
    extends ProcessDefinitionConverter<Project, ProcessMeta, Datasource, ResourceInfo, UdfFunc> {
    private List<DwWorkflow> dwWorkflowList = new ArrayList<>();

    public V1ProcessDefinitionConverter(
        DolphinSchedulerConverterContext<Project, ProcessMeta, Datasource, ResourceInfo, UdfFunc> converterContext,
        ProcessMeta processDefinition) {
        super(converterContext, processDefinition);
    }

    public static String toWorkflowName(ProcessMeta processMeta) {
        return com.aliyun.dataworks.migrationx.domain.dataworks.utils.StringUtils.toValidName(Joiner.on("_").join(
            processMeta.getProjectName(), processMeta.getProcessDefinitionName()));
    }

    @Override
    public void convert() {
        DwWorkflow dwWorkflow = new DwWorkflow();
        dwWorkflow.setName(toWorkflowName(processDefinition));
        dwWorkflow.setType(WorkflowType.BUSINESS);
        dwWorkflow.setScheduled(true);
        dwWorkflow.setVersion(WorkflowVersion.V3);

        converterContext.setDwWorkflow(dwWorkflow);
        dwWorkflowList = Optional.ofNullable(processDefinition.getProcessDefinitionJson()).map(pd -> {
            dwWorkflow.setParameters(GsonUtils.toJsonString(pd.getGlobalParams().stream()
                .map(prop -> new WorkflowParameter().setKey(prop.getProp()).setValue(prop.getValue()))
                .collect(Collectors.toList())));
            return ListUtils.emptyIfNull(pd.getTasks()).stream().map(taskNode -> {
                AbstractParameterConverter<AbstractParameters> converter;
                try {
                    converter = TaskConverterFactory.create(processDefinition, taskNode, converterContext);
                    converter.convert();
                } catch (Throwable e) {
                    log.error("task converter error: ", e);
                    throw new RuntimeException(e);
                }
                return converter.getWorkflowList();
            }).collect(Collectors.toList());
        }).orElse(ListUtils.emptyIfNull(null)).stream().flatMap(List::stream).distinct().collect(Collectors.toList());

        ListUtils.emptyIfNull(dwWorkflow.getNodes()).forEach(node ->
            node.setCronExpress(convertCrontab(processDefinition.getScheduleCrontab())));

        processSubProcessDefinitionDepends();
    }

    /**
     * SubProcess dependents handling logic
     */
    private void processSubProcessDefinitionDepends() {
        // for SubProcess Type
        ListUtils.emptyIfNull(dwWorkflowList).forEach(workflow -> ListUtils.emptyIfNull(workflow.getNodes()).stream()
            .filter(n -> StringUtils.equalsIgnoreCase(TaskType.SUB_PROCESS.name(), ((DwNode)n).getRawNodeType()))
            .forEach(subProcessNode -> {
                SubProcessParameters subProcessParameter =
                    GsonUtils.fromJsonString(subProcessNode.getCode(),
                        new TypeToken<SubProcessParameters>() {}.getType());
                converterContext.getDolphinSchedulerPackage().getProcessDefinitions().values().stream().map(defList ->
                        ListUtils.emptyIfNull(defList).stream()
                            .filter(df -> subProcessParameter != null)
                            .filter(df -> Objects.equals(df.getProcessDefinitionId(),
                                subProcessParameter.getProcessDefinitionId()))
                            .findFirst()
                            .flatMap(proDef -> dwWorkflowList.stream()
                                .filter(wf -> StringUtils.equals(toWorkflowName(proDef), wf.getName()))
                                .findFirst()
                                .map(wf -> addStartEndNodeToDependedWorkflow(subProcessNode, proDef, wf)))
                            .orElse(new DwNodeIo()))
                    .filter(io -> StringUtils.isNotBlank(io.getData()))
                    .findFirst()
                    .ifPresent(endNodeOut -> ListUtils.emptyIfNull(workflow.getNodes()).stream()
                        // set children of sub process node depends on end node of depend workflow
                        .filter(n -> ListUtils.emptyIfNull(n.getInputs()).stream().anyMatch(in ->
                            ListUtils.emptyIfNull(subProcessNode.getOutputs()).stream().anyMatch(out ->
                                StringUtils.equalsIgnoreCase(out.getData(), in.getData()))))
                        .forEach(child -> ListUtils.emptyIfNull(child.getInputs()).stream()
                            .filter(in -> ListUtils.emptyIfNull(subProcessNode.getOutputs()).stream().anyMatch(depOut ->
                                StringUtils.equalsIgnoreCase(in.getData(), depOut.getData())))
                            .forEach(in -> in.setData(endNodeOut.getData()))));
            }));
    }

    /**
     * add start, end node for sub process workflow - set start as parent of all nodes that has no parents - set end as
     * child of all nodes that has no children - set start as child of sub process node
     *
     * @param subProcessNode
     * @param proDef
     * @param wf
     * @return output of end node
     */
    private DwNodeIo addStartEndNodeToDependedWorkflow(Node subProcessNode, ProcessMeta proDef, DwWorkflow wf) {
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

    @Override
    public List<DwWorkflow> getWorkflowList() {
        return dwWorkflowList;
    }
}
