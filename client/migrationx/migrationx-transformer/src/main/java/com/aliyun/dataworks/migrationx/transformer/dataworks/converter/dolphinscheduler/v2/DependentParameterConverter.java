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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DolphinSchedulerV2Context;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.ProcessDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.model.DependentItem;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.model.DependentTaskModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.process.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.dependent.DependentParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.common.base.Joiner;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

/**
 * @author 聿剑
 * @date 2022/10/24
 */
@Slf4j
public class DependentParameterConverter extends AbstractParameterConverter<DependentParameters> {
    public DependentParameterConverter(DagData processMeta, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<Project, DagData, DataSource,
                    ResourceInfo, UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    public List<DwNode> convertParameter() {
        log.info("params : {}", taskDefinition.getTaskParams());
        DwNode dwNode = newDwNode(taskDefinition);
        dwNode.setType(CodeProgramType.VIRTUAL.name());

        JsonObject param = GsonUtils.fromJsonString(taskDefinition.getTaskParams(), JsonObject.class);
        DependentParameters dependentParameters = null;
        if (param.get("dependence") != null) {
            dependentParameters = GsonUtils.fromJson(param.getAsJsonObject("dependence"), DependentParameters.class);
        }
        if (dependentParameters == null
                || dependentParameters.getDependTaskList() == null
                || dependentParameters.getDependTaskList().isEmpty()) {
            log.warn("no dependence param {}", taskDefinition.getTaskParams());
            return Arrays.asList(dwNode);
        }

        // 本节点的条件依赖
        List<DependentTaskModel> dependencies = dependentParameters.getDependTaskList();

        List<DwNodeIo> dwNodeIos = depTaskToDwNode(dependencies);
        if (dwNode.getInputs() == null) {
            dwNode.setInputs(new ArrayList<>());
        }
        dwNode.getInputs().addAll(dwNodeIos);

        return Arrays.asList(dwNode);
    }

    /**
     * if depends on other project,need to put other project info to this converter
     */
    private List<DwNodeIo> depTaskToDwNode(List<DependentTaskModel> dependencies) {
        List<DwNodeIo> dwNodeIos = new ArrayList<>();
        final DolphinSchedulerV2Context context = DolphinSchedulerV2Context.getContext();
        ListUtils.emptyIfNull(dependencies).forEach(dependModel ->
                ListUtils.emptyIfNull(dependModel.getDependItemList()).forEach(depItem -> {
                    //find dependent task then to node
                    ProcessDefinition depProcessDefinition = context.getProcessCodeMap().get(depItem.getDefinitionCode());
                    //process from other project, maybe not find from this converter
                    if (depProcessDefinition != null) {
                        //check if the same project
                        if (depItem.getProjectCode() > 0L && depProcessDefinition.getProjectCode() == depItem.getProjectCode()) {
                            if (depItem.getDepTaskCode() == 0L) {
                                //all leaf task of process definition
                                //1. get all relation of process
                                // preTaskCode -> postTaskCode
                                Map<Long, List<Long>> relations = findRelations(depItem);

                                List<TaskDefinition> taskDefinitions = context.getProcessCodeTaskRelationMap().get(depItem.getDefinitionCode());
                                for (TaskDefinition task : CollectionUtils.emptyIfNull(taskDefinitions)) {
                                    //2. find all taskCode not in preTaskCode (not as a pre dependent, leaf task)
                                    if (!relations.containsKey(task.getCode())) {
                                        DwNodeIo dwNodeIo = taskToNode(task, depProcessDefinition);
                                        dwNodeIos.add(dwNodeIo);
                                    }
                                }
                            } else {
                                TaskDefinition depTask = context.getTaskCodeMap().get(depItem.getDepTaskCode());
                                if (depTask != null) {
                                    DwNodeIo dwNodeIo = taskToNode(depTask, depProcessDefinition);
                                    dwNodeIos.add(dwNodeIo);
                                }
                            }
                        }
                    }
                })
        );
        return dwNodeIos;
    }

    private Map<Long, List<Long>> findRelations(DependentItem depItem) {
        Map<Long, List<Long>> relations = new HashMap<>();
        DolphinSchedulerV2Context.getContext().getDagDatas().stream()
                .filter(dag -> dag.getProcessDefinition().getCode() == depItem.getDefinitionCode())
                .map(dag -> dag.getProcessTaskRelationList())
                .flatMap(List::stream)
                .forEach(s -> {
                    List<Long> postCodes = relations.get(s.getPreTaskCode());
                    if (postCodes == null) {
                        relations.put(s.getPreTaskCode(), new ArrayList<>());
                    }
                    relations.get(s.getPreTaskCode()).add(s.getPostTaskCode());
                });
        return relations;
    }

    private DwNodeIo taskToNode(TaskDefinition depTask, ProcessDefinition processDefinition) {
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
}
