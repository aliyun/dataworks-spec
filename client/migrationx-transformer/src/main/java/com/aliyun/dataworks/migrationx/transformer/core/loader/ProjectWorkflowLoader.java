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

package com.aliyun.dataworks.migrationx.transformer.core.loader;

import com.aliyun.dataworks.migrationx.domain.dataworks.constants.DataWorksConstants;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwFunction;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwResource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Resource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowType;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DefaultNodeTypeUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.ResourceUtils;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.controller.Task;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sam.liux
 * @date 2019/07/03
 */
public class ProjectWorkflowLoader extends Task<List<DwWorkflow>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectWorkflowLoader.class);

    private String projectDir;
    private List<DwWorkflow> workflowList;
    private List<DwResource> resources;

    public ProjectWorkflowLoader(String projectDir) {
        super(ProjectWorkflowLoader.class.getSimpleName());
        this.projectDir = projectDir;
    }

    public ProjectWorkflowLoader(String projectDir, String name) {
        super(name);
        this.projectDir = projectDir;
    }

    @Override
    public List<DwWorkflow> call() throws Exception {
        File projectPath = new File(projectDir);
        File workflowPath = new File(projectPath.getAbsolutePath() + File.separator +
            Constants.WORKFLOWS_DIR_PRJ_RELATED);
        workflowList = new ArrayList<>();
        LOGGER.info("workflow path: {}", workflowPath);
        if (workflowPath == null || workflowPath.listFiles() == null || !workflowPath.exists()) {
            return workflowList;
        }

        getResources();

        boolean newWorkflowDirectory = isNewWorkflowDirectory(workflowPath);
        if (!newWorkflowDirectory) {
            processWorkflowType(workflowPath, null);
        } else if (workflowPath.listFiles() != null) {
            Arrays.stream(workflowPath.listFiles()).forEach(typeDir -> {
                try {
                    processWorkflowType(
                        typeDir,
                        WorkflowType.valueOf(org.apache.commons.lang3.StringUtils.upperCase(typeDir.getName())));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return workflowList;
    }

    private void processWorkflowType(File workflowPath, WorkflowType type) throws IOException {
        for (File flowDir : workflowPath.listFiles()) {
            if (flowDir.isFile()) {
                LOGGER.info("{} is not directory, skip it", flowDir.getName());
                continue;
            }

            LOGGER.info("found workflow: {}, path: {}", flowDir.getName(), flowDir.getAbsoluteFile());
            DwWorkflow workflow = loadWorkflowDir(flowDir);
            if (type == null) {
                if (BooleanUtils.isTrue(workflow.getScheduled())) {
                    workflow.setType(WorkflowType.BUSINESS);
                } else {
                    workflow.setType(WorkflowType.MANUAL_BUSINESS);
                }
                if (workflow.getName().equalsIgnoreCase(DataWorksConstants.OLD_VERSION_WORKFLOW_NAME)) {
                    workflow.setType(WorkflowType.OLD_WORKFLOW);
                }
            } else {
                workflow.setType(type);
            }
            workflow.setLocalPath(flowDir);
            workflowList.add(workflow);
        }
    }

    /**
     * 判断是不是新的workflow组织形式 - src - workflows - BUSINESS - test_business_workflow01 - workflow.xml - xxx - workflow.xml -
     * MANUAL_BUSINESS - yyy - workflow.xml - OLD_WORKFLOW - workflow.xml
     *
     * @param workflowPath
     * @return
     */
    private boolean isNewWorkflowDirectory(File workflowPath) {
        File[] subDirs = workflowPath.listFiles();
        if (subDirs == null) {
            return false;
        }

        return Arrays.stream(subDirs).allMatch(dir ->
            Arrays.stream(WorkflowType.values()).anyMatch(t -> t.name().equalsIgnoreCase(dir.getName()))
        );
    }

    @SuppressWarnings("unchecked")
    private void getResources() {
        resources = dependencies.stream()
            .filter(task -> task.getResult() != null)
            .filter(task -> task.getResult() instanceof List)
            .filter(task -> !CollectionUtils.isEmpty((List)task.getResult()))
            .filter(task -> ((List)task.getResult()).get(0) instanceof DwResource)
            .map(task -> (List<DwResource>)task.getResult())
            .flatMap(List::stream).collect(Collectors.toList());
    }

    private DwWorkflow loadWorkflowDir(File flowDir) throws IOException {
        String workflowXmlFile = flowDir.getAbsolutePath() + File.separator + Constants.WORKFLOW_XML;
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DwWorkflow workflow = xmlMapper.readValue(new File(workflowXmlFile), DwWorkflow.class);
        workflow.setNodes(ListUtils.emptyIfNull(workflow.getNodes()).stream().map(node -> {
            DwNode dwNode = new DwNode();
            BeanUtils.copyProperties(node, dwNode);

            ListUtils.emptyIfNull(dwNode.getInputs()).stream().filter(in -> in.getParseType() == null).forEach(
                in -> in.setParseType(1));
            ListUtils.emptyIfNull(dwNode.getOutputs()).stream().filter(in -> in.getParseType() == null).forEach(
                in -> in.setParseType(1));
            if (workflow.getScheduled()) {
                dwNode.setNodeUseType(Optional.ofNullable(node.getNodeUseType()).orElse(NodeUseType.SCHEDULED));
            } else {
                dwNode.setNodeUseType(Optional.ofNullable(node.getNodeUseType()).orElse(NodeUseType.MANUAL));
            }

            if (DefaultNodeTypeUtils.isDiNode(dwNode.getType())) {
                setDiResourceGroupInfo(dwNode);
            }
            return dwNode;
        }).collect(Collectors.toList()));

        if (!CollectionUtils.isEmpty(workflow.getResources())) {
            Map<String, DwResource> map = resources.stream().collect(
                Collectors.toMap(res -> res.getName(), res -> res));

            Map<String, Resource> resMap = workflow.getResources()
                .stream().collect(Collectors.toMap(res -> res.getName(), res -> res));

            List<Resource> newResList = new ArrayList<>();
            resMap.keySet().stream().forEach(name -> {
                Resource res = resMap.get(name);
                DwResource dwRes = new DwResource();
                BeanUtils.copyProperties(res, dwRes);
                if (map.containsKey(name)) {
                    dwRes.setLocalPath(map.get(name).getLocalPath());
                }
                if (res.getType() == null) {
                    dwRes.setType(ResourceUtils.getFileResourceType(name));
                }
                dwRes.setWorkflowRef(workflow);
                newResList.add(dwRes);
            });
            workflow.setResources(newResList);
        }

        if (!CollectionUtils.isEmpty(workflow.getFunctions())) {
            workflow.setFunctions(workflow.getFunctions().stream().map(fun -> {
                DwFunction dwFunction = new DwFunction();
                BeanUtils.copyProperties(fun, dwFunction);
                dwFunction.setWorkflowRef(workflow);
                return dwFunction;
            }).collect(Collectors.toList()));
        }
        return workflow;
    }

    private void setDiResourceGroupInfo(DwNode dwNode) {
        if (StringUtils.isNotBlank(dwNode.getDiResourceGroup())) {
            return;
        }

        String code = dwNode.getCode();
        if (StringUtils.isBlank(code)) {
            return;
        }

        try {
            JsonObject jsonObject = GsonUtils.gson.fromJson(code, JsonObject.class);
            if (jsonObject == null) {
                return;
            }

            if (!jsonObject.has("extend")) {
                return;
            }

            JsonObject extendJson = jsonObject.get("extend").getAsJsonObject();
            if (!extendJson.has("resourceGroup")) {
                return;
            }

            String resourceGroup = extendJson.get("resourceGroup").getAsString();
            dwNode.setDiResourceGroup(resourceGroup);
            dwNode.setDiResourceGroupName(resourceGroup);
        } catch (Exception e) {
        }
    }
}
