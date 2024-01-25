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

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v301.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v301.ReleaseState;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v301.TaskDefinition;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.AbstractBaseConverter;
import com.aliyun.dataworks.migrationx.transformer.core.loader.ProjectAssetLoader;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * for dolphinscheduler v2.0.1 and later convert dolphinscheduler process to dataworks model
 *
 * @author 聿剑
 * @date 2022/10/12
 */
@Slf4j
public class DolphinSchedulerV2Converter extends AbstractBaseConverter {
    private Project project;
    private Properties properties;

    private List<DagData> dagDataList = new ArrayList<>();
    private List<DwWorkflow> dwWorkflowList = new ArrayList<>();

    public DolphinSchedulerV2Converter(AssetType assetType, String name) {
        super(assetType, name);
    }

    public DolphinSchedulerV2Converter(AssetType assetType, String name,
        ProjectAssetLoader projectAssetLoader) {
        super(assetType, name, projectAssetLoader);
    }

    public DolphinSchedulerV2Converter setProject(Project project) {
        this.project = project;
        return this;
    }

    public DolphinSchedulerV2Converter setProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public List<DwWorkflow> convert(Asset asset) throws Exception {
        File rootDir = asset.getPath();
        dagDataList = readDagDataList(rootDir);
        log.info("dagDataList size: {}", CollectionUtils.size(dagDataList));

        dwWorkflowList = convertDagDataListToDwWorkflowList(dagDataList);
        return dwWorkflowList;
    }

    private List<DwWorkflow> convertDagDataListToDwWorkflowList(List<DagData> dagDataList) {
        return ListUtils.emptyIfNull(dagDataList).stream()
            .map(dagData -> convertDagDataToDwWorkflow(dagData, dagDataList))
            .collect(Collectors.toList());
    }

    private DwWorkflow convertDagDataToDwWorkflow(DagData dagData, List<DagData> dagDataList) {
        DwWorkflow dwWorkflow = new DwWorkflow();
        dwWorkflow.setName(dagData.getProcessDefinition().getName());
        dwWorkflow.setType(ReleaseState.ONLINE.equals(dagData.getProcessDefinition().getScheduleReleaseState()) ?
            WorkflowType.BUSINESS : WorkflowType.MANUAL_BUSINESS);
        dwWorkflow.setScheduled(ReleaseState.ONLINE.equals(dagData.getProcessDefinition().getScheduleReleaseState()));
        dwWorkflow.setParameters(GsonUtils.toJsonString(dagData.getProcessDefinition().getGlobalParamMap()));
        dwWorkflow.setNodes(convertDagDataTasks(dwWorkflow, dagData, dagDataList));
        return dwWorkflow;
    }

    private List<Node> convertDagDataTasks(DwWorkflow dwWorkflow, DagData dagData, List<DagData> dagDataList) {
        return ListUtils.emptyIfNull(dagData.getTaskDefinitionList()).stream()
            .map(taskDefinition -> convertDagDataTaskDefinitionToDwNode(dwWorkflow, taskDefinition, dagData,
                dagDataList))
            .collect(Collectors.toList());
    }

    private DwNode convertDagDataTaskDefinitionToDwNode(DwWorkflow dwWorkflow, TaskDefinition taskDefinition,
        DagData dagData, List<DagData> dagDataList) {
        DwNode dwNode = new DwNode();
        dwNode.setName(taskDefinition.getName());
        dwNode.setWorkflowRef(dwWorkflow);
        dwNode.setType(convertDagDataTaskDefinitionType(taskDefinition, dagData));
        return dwNode;
    }

    private String convertDagDataTaskDefinitionType(TaskDefinition taskDefinition, DagData dagData) {
        taskDefinition.getTaskParams();
        return null;
    }

    private List<DagData> readDagDataList(File rootDir) {
        if (!rootDir.exists()) {
            log.info("root directory not exits: {}", rootDir);
            return ListUtils.emptyIfNull(null);
        }

        return Optional.ofNullable(rootDir.listFiles(f -> f.isFile() && f.getName().endsWith(".json")))
            .map(Arrays::asList)
            .map(files -> files.stream().map(this::readDagDataJson).collect(Collectors.toList()))
            .orElse(ListUtils.emptyIfNull(null));
    }

    private DagData readDagDataJson(File jsonFile) {
        try {
            String json = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
            return GsonUtils.fromJsonString(json, new TypeToken<DagData>() {}.getType());
        } catch (IOException e) {
            log.error("read json file: {} error: ", jsonFile, e);
            throw new RuntimeException(e);
        }
    }
}
