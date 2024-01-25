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

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Datasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.ProcessMeta;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.TaskNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.dependent.DependentParameters;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.google.common.base.Joiner;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 聿剑
 * @date 2022/10/24
 */
@Slf4j
public class DependentParameterConverter extends AbstractParameterConverter<DependentParameters> {
    public DependentParameterConverter(ProcessMeta processMeta, TaskNode taskDefinition,
        DolphinSchedulerConverterContext<Project, ProcessMeta, Datasource,
            ResourceInfo, UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    protected void convertParameter() {
        DwNode dwNode = newDwNode(processMeta, taskDefinition);
        dwNode.setType(CodeProgramType.VIRTUAL.name());
        ListUtils.emptyIfNull(taskDefinition.getDependence().getDependTaskList()).forEach(dependModel -> {
            ListUtils.emptyIfNull(dependModel.getDependItemList()).forEach(depItem -> {
                List<DwNodeIo> dwNodeIos = Optional.ofNullable(converterContext)
                    .map(DolphinSchedulerConverterContext::getDolphinSchedulerPackage)
                    .map(DolphinSchedulerPackage::getProjects)
                    .map(projects -> (List<Project>)GsonUtils.fromJsonString(GsonUtils.toJsonString(projects),
                        new TypeToken<List<Project>>() {}.getType()))
                    .orElse(ListUtils.emptyIfNull(null)).stream()
                    .filter(proj -> Objects.equals(depItem.getProjectId(), proj.getId()))
                    .map(depProject ->
                        Optional.ofNullable(converterContext)
                            .map(DolphinSchedulerConverterContext::getDolphinSchedulerPackage)
                            .map(DolphinSchedulerPackage::getProcessDefinitions)
                            .map(map -> map.get(depProject.getName()))
                            .orElse(ListUtils.emptyIfNull(null)).stream()
                            .filter(pro -> Objects.equals(depItem.getDefinitionId(), pro.getProcessDefinitionId()))
                            .map(proMeta -> ListUtils.emptyIfNull(proMeta.getProcessDefinitionJson().getTasks())
                                .stream()
                                .filter(depTask -> StringUtils.equalsIgnoreCase("all", depItem.getDepTasks()) ||
                                    StringUtils.equals(depTask.getName(), depItem.getDepTasks()))
                                .map(depTask -> {
                                    DwNodeIo crossProjectDepend = new DwNodeIo();
                                    crossProjectDepend.setParseType(1);
                                    crossProjectDepend.setData(Joiner.on(".").join(
                                        converterContext.getProject().getName(),
                                        depProject.getName(),
                                        proMeta.getProcessDefinitionName(),
                                        depTask.getName()));
                                    return crossProjectDepend;
                                }).collect(Collectors.toList()))
                            .flatMap(List::stream)
                            .collect(Collectors.toList()))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
                if (dwNode.getInputs() == null) {
                    dwNode.setInputs(new ArrayList<>());
                }
                dwNode.getInputs().addAll(dwNodeIos);
            });
        });
    }
}
