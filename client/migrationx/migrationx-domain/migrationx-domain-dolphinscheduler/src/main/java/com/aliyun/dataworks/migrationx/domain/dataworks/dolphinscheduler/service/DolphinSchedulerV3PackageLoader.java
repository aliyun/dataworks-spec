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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerVersion;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.PackageInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DolphinSchedulerV3Context;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.ResourceComponent;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.UdfFunc;
import com.aliyun.migrationx.common.utils.JSONUtils;

import com.fasterxml.jackson.core.type.TypeReference;
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
public class DolphinSchedulerV3PackageLoader extends DolphinSchedulerPackageLoader<Project, DagData, DataSource, ResourceComponent, UdfFunc> {
    private final DolphinSchedulerPackage<Project, DagData, DataSource, ResourceComponent, UdfFunc>
            dolphinSchedulerPackage = new DolphinSchedulerPackage<>();

    public DolphinSchedulerV3PackageLoader(File packageRoot) {super(packageRoot);}

    @Override
    public DolphinSchedulerPackage<Project, DagData, DataSource, ResourceComponent, UdfFunc> getDolphinSchedulerPackage() {
        return dolphinSchedulerPackage;
    }

    @Override
    public boolean support(DolphinSchedulerVersion version) {
        return DolphinSchedulerVersion.V2.equals(version);
    }

    @Override
    public void loadPackage() throws IOException {
        dolphinSchedulerPackage.setPackageRoot(packageRoot);
        PackageInfo packageInfo = readPackageInfo(packageRoot);
        dolphinSchedulerPackage.setPackageInfo(packageInfo);
        List<Project> projects = readProjects(packageRoot, new TypeToken<List<Project>>() {}.getType());
        dolphinSchedulerPackage.setProjects(projects);
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
                readJsonFiles(new File(packageRoot, RESOURCE), new TypeToken<List<ResourceComponent>>() {}));
        dolphinSchedulerPackage.setUdfFuncs(
                readJsonFiles(new File(packageRoot, UDF_FUNCTION), new TypeToken<List<UdfFunc>>() {}));

        DolphinSchedulerV3Context.initContext(dolphinSchedulerPackage.getProjects(),
                dolphinSchedulerPackage.getProcessDefinitions()
                        .values().stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList()),
                dolphinSchedulerPackage.getDatasources(),
                dolphinSchedulerPackage.getResources(),
                dolphinSchedulerPackage.getUdfFuncs());
    }

    private List<DagData> readProcessMetaList(File rootDir) {
        if (!rootDir.exists()) {
            log.warn("root directory not exits: {}", rootDir);
            return Collections.emptyList();
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

    private List<DagData> readProcessMetaJson(File jsonFile) {
        try {
            String json = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
            return JSONUtils.parseObject(json, new TypeReference<List<DagData>>() {});
        } catch (IOException e) {
            log.error("read json file: {} error: ", jsonFile, e);
            throw new RuntimeException(e);
        }
    }
}
