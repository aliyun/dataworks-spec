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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerVersion;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.DolphinSchedulerV1Context;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.ProcessMeta;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.datasource.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.entity.ResourceInfo;
import com.aliyun.migrationx.common.context.TransformerContext;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author 聿剑
 * @date 2022/10/24
 */
@Slf4j
public class DolphinSchedulerV1PackageLoader extends DolphinSchedulerPackageLoader<
        Project, ProcessMeta, DataSource, ResourceInfo, UdfFunc> {
    private final DolphinSchedulerPackage<Project, ProcessMeta, DataSource, ResourceInfo, UdfFunc>
            dolphinSchedulerPackage = new DolphinSchedulerPackage<>();

    public DolphinSchedulerV1PackageLoader(File packageRoot) {
        super(packageRoot);
    }

    @Override
    public DolphinSchedulerPackage<Project, ProcessMeta, DataSource, ResourceInfo, UdfFunc> getDolphinSchedulerPackage() {
        return dolphinSchedulerPackage;
    }

    @Override
    public boolean support(DolphinSchedulerVersion version) {
        return DolphinSchedulerVersion.V1.equals(version);
    }

    @Override
    public void loadPackage() throws IOException {
        dolphinSchedulerPackage.setPackageRoot(packageRoot);
        dolphinSchedulerPackage.setPackageInfo(readPackageInfo(packageRoot));
        dolphinSchedulerPackage.setProjects(readProjects(packageRoot,new TypeToken<List<com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project>>() {}.getType()));
        File projectsDir = new File(packageRoot, PROJECTS);
        if (projectsDir.exists()) {
            File[] subDirs = Optional.ofNullable(projectsDir.listFiles(File::isDirectory)).orElse(new File[]{});
            dolphinSchedulerPackage.setProcessDefinitions(Arrays.stream(subDirs).collect(Collectors.toMap(
                    File::getName,
                    projectDir -> this.readProcessMetaList(new File(projectDir, PROCESS_DEFINITION)))));
        }
        dolphinSchedulerPackage.setDatasources(
                readJsonFiles(new File(packageRoot, DATASOURCE), new TypeToken<List<DataSource>>() {}));
        dolphinSchedulerPackage.setResources(
                readJsonFiles(new File(packageRoot, RESOURCE), new TypeToken<List<ResourceInfo>>() {}));
        dolphinSchedulerPackage.setUdfFuncs(
                readJsonFiles(new File(packageRoot, UDF_FUNCTION), new TypeToken<List<UdfFunc>>() {}));
        AtomicInteger counter = new AtomicInteger(0);
        dolphinSchedulerPackage.getProcessDefinitions()
                .values()
                .stream()
                .forEach(s -> s.forEach(ss ->
                        ss.getProcessDefinitionJson().getTasks()
                                .forEach(sss -> counter.incrementAndGet())));
        TransformerContext.getCollector().setTotalTasks(counter.get());
        DolphinSchedulerV1Context.initContext(dolphinSchedulerPackage.getProjects(),
                dolphinSchedulerPackage.getProcessDefinitions()
                        .values().stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList()),
                dolphinSchedulerPackage.getDatasources(),
                dolphinSchedulerPackage.getResources(),
                dolphinSchedulerPackage.getUdfFuncs());
    }

    private List<ProcessMeta> readProcessMetaList(File rootDir) {
        if (!rootDir.exists()) {
            log.info("root directory not exits: {}", rootDir);
            return ListUtils.emptyIfNull(null);
        }

        return Optional.ofNullable(rootDir.listFiles(f -> f.isFile() && f.getName().endsWith(".json")))
                .map(Arrays::asList)
                .map(files -> files.stream().map(this::readProcessMetaJson)
                        .filter(CollectionUtils::isNotEmpty)
                        .flatMap(List::stream)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .orElse(ListUtils.emptyIfNull(null));
    }

    private List<ProcessMeta> readProcessMetaJson(File jsonFile) {
        try {
            String json = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
            return GsonUtils.fromJsonString(json, new TypeToken<List<ProcessMeta>>() {}.getType());
        } catch (IOException e) {
            log.error("read json file: {} error: ", jsonFile, e);
            throw new RuntimeException(e);
        }
    }
}
