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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.process.ResourceInfo;
import com.aliyun.migrationx.common.context.TransformerContext;

import org.apache.commons.collections4.ListUtils;

public class DolphinSchedulerV2Context {
    private static DolphinSchedulerV2Context context;

    private List<Project> projects;

    private List<DataSource> dataSources;
    private List<ResourceInfo> resources;
    private List<UdfFunc> udfFuncs;

    private List<DagData> dagDatas;
    private Map<Long, Project> projectCodeMap = new HashMap<>();
    private Map<Long, ProcessDefinition> processCodeMap = new HashMap<>();

    private Map<Long, List<TaskDefinition>> processCodeTaskRelationMap = new HashMap<>();
    private Map<Long, TaskDefinition> taskCodeMap = new HashMap<>();

    private Map<Long, List<String>> subProcessCodeOutMap = new HashMap<>();

    private DolphinSchedulerV2Context() {

    }

    public static void initContext(List<Project> projects, List<DagData> dagDatas, List<DataSource> dataSources,
            List<ResourceInfo> resources, List<UdfFunc> udfFuncs) {
        DolphinSchedulerV2Context context = new DolphinSchedulerV2Context();
        context.projects = projects;
        context.dagDatas = dagDatas;
        context.dataSources = dataSources;
        context.resources = resources;
        context.udfFuncs = udfFuncs;
        context.taskCodeMap = new HashMap<>();
        for (Project project : ListUtils.emptyIfNull(projects)) {
            context.projectCodeMap.put(Long.valueOf(project.getCode()), project);
        }
        int totalTasks = 0;
        for (DagData dagData : ListUtils.emptyIfNull(dagDatas)) {
            ProcessDefinition definition = dagData.getProcessDefinition();
            Project project = context.getProjectCodeMap().get(definition.getProjectCode());
            String projectName = null;
            if (project != null) {
                projectName = project.getName();
            }
            if (definition.getProjectName() == null) {
                definition.setProjectName(projectName);
            }
            context.processCodeMap.put(definition.getCode(), definition);
            for (TaskDefinition taskDefinition : ListUtils.emptyIfNull(dagData.getTaskDefinitionList())) {
                totalTasks++;
                if (taskDefinition.getProjectName() == null) {
                    taskDefinition.setProjectName(projectName);
                }
                context.taskCodeMap.put(taskDefinition.getCode(), taskDefinition);
            }
            for (ProcessTaskRelation relation : ListUtils.emptyIfNull(dagData.getProcessTaskRelationList())) {
                List<TaskDefinition> taskDefinitionList = context.processCodeTaskRelationMap.get(relation.getProcessDefinitionCode());
                if (taskDefinitionList == null) {
                    taskDefinitionList = new ArrayList<>();
                    context.processCodeTaskRelationMap.put(relation.getProcessDefinitionCode(), taskDefinitionList);
                }
                TaskDefinition taskDefinition = context.taskCodeMap.get(relation.getPostTaskCode());
                if (taskDefinition != null) {
                    taskDefinitionList.add(taskDefinition);
                } else {
                    taskDefinition = context.taskCodeMap.get(relation.getPreTaskCode());
                    if (taskDefinition != null) {
                        taskDefinitionList.add(taskDefinition);
                    }
                }
            }
        }
        TransformerContext.getCollector().setTotalTasks(totalTasks);
        DolphinSchedulerV2Context.context = context;
    }

    public static DolphinSchedulerV2Context getContext() {
        return DolphinSchedulerV2Context.context;
    }

    public Map<Long, Project> getProjectCodeMap() {
        return projectCodeMap;
    }

    public void setProjectCodeMap(Map<Long, Project> projectCodeMap) {
        this.projectCodeMap = projectCodeMap;
    }

    public Map<Long, ProcessDefinition> getProcessCodeMap() {
        return processCodeMap;
    }

    public void setProcessCodeMap(Map<Long, ProcessDefinition> processCodeMap) {
        this.processCodeMap = processCodeMap;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<DataSource> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    public List<ResourceInfo> getResources() {
        return resources;
    }

    public void setResources(List<ResourceInfo> resources) {
        this.resources = resources;
    }

    public List<UdfFunc> getUdfFuncs() {
        return udfFuncs;
    }

    public void setUdfFuncs(List<UdfFunc> udfFuncs) {
        this.udfFuncs = udfFuncs;
    }

    public List<DagData> getDagDatas() {
        return dagDatas;
    }

    public void setDagDatas(List<DagData> dagDatas) {
        this.dagDatas = dagDatas;
    }

    public Map<Long, TaskDefinition> getTaskCodeMap() {
        return taskCodeMap;
    }

    public void setTaskCodeMap(Map<Long, TaskDefinition> taskCodeMap) {
        this.taskCodeMap = taskCodeMap;
    }

    public Map<Long, List<TaskDefinition>> getProcessCodeTaskRelationMap() {
        return processCodeTaskRelationMap;
    }

    public void setProcessCodeTaskRelationMap(Map<Long, List<TaskDefinition>> processCodeTaskRelationMap) {
        this.processCodeTaskRelationMap = processCodeTaskRelationMap;
    }

    public void putSubProcessCodeOutMap(Long code, String out) {
        List<String> outs = this.subProcessCodeOutMap.get(code);
        if (outs == null) {
            outs = new ArrayList<>();
            this.subProcessCodeOutMap.put(code, outs);
        }
        outs.add(out);
    }

    public List<String> getSubProcessCodeMap(Long code) {
        return this.subProcessCodeOutMap.get(code);
    }
}
