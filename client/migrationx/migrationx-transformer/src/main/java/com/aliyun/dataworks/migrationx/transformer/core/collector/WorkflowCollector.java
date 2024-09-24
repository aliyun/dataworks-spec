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

package com.aliyun.dataworks.migrationx.transformer.core.collector;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwResource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Workflow;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.controller.Task;
import com.aliyun.dataworks.migrationx.transformer.core.controller.TaskStage;
import com.aliyun.dataworks.migrationx.transformer.core.loader.ProjectResourceLoader;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author sam.liux
 * @date 2019/07/04
 */
public class WorkflowCollector extends Task<List<DwWorkflow>> {

    public WorkflowCollector() {
        super(WorkflowCollector.class.getSimpleName());
    }

    public WorkflowCollector(String name) {
        super(name);
    }

    @Override
    public List<DwWorkflow> call() {
        TaskStage[] stages = TaskStage.values();
        Arrays.sort(stages, Comparator.comparingInt(Enum::ordinal));
        Map<String, DwWorkflow> uuidWorkflowMap = new HashMap<>(100);

        // collect workflow list of all depend tasks, and reduce duplicated by workflow uuid
        for (TaskStage stage : stages) {
            dependencies.stream()
                    .filter(task -> task.getStage().equals(stage))
                    .filter(task -> task.getResult() instanceof List)
                    .filter(task -> !CollectionUtils.isEmpty((List) task.getResult()))
                    .filter(task -> ((List) task.getResult()).get(0) instanceof DwWorkflow)
                    .map(task -> (List<DwWorkflow>) task.getResult())
                    .flatMap(List::stream)
                    .forEach(dwWorkflow -> uuidWorkflowMap.put(dwWorkflow.getDmObjectUuid(), dwWorkflow));
        }

        List<DwWorkflow> workflows = uuidWorkflowMap.values().stream().collect(Collectors.toList());
        workflows.stream().forEach(workflow -> buildNodeCodePath(workflow));

        // 如果本地提供了对应名称的资源文件，则使用提供的资源文件，否则用placeholder
        dependencies.stream()
                .filter(task -> task instanceof ProjectResourceLoader)
                .findFirst().ifPresent(loader -> {
                            ProjectResourceLoader projectResourceLoader = (ProjectResourceLoader) loader;
                            List<DwResource> resources = projectResourceLoader.getResult();
                            workflows.stream()
                                    .map(Workflow::getResources)
                                    .flatMap(List::stream)
                                    .forEach(resource -> {
                                        resources.stream().forEach(res -> {
                                            if (res.getName().equals(resource.getName())) {
                                                ((DwResource) resource).setLocalPath(res.getLocalPath());
                                            }
                                        });
                                    });
                        }
                );

        return workflows;
    }

    private void buildNodeCodePath(DwWorkflow workflow) {
        for (Node node : workflow.getNodes()) {
            if (StringUtils.isEmpty(node.getRef())) {
                String path = Constants.WORKFLOWS_DIR_PRJ_RELATED +
                        File.separator + workflow.getName() +
                        File.separator + Constants.NODES_DIR +
                        File.separator + node.getName() +
                        File.separator + node.getName();
                node.setRef(path);
            }
        }
    }
}
