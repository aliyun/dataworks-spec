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
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerVersion;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.PackageInfo;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2022/10/24
 */
@Slf4j
public abstract class DolphinSchedulerPackageLoader<Project, ProcessDefinitionType, DataSource, ResourceInfo,
        UdfFunction> {
    protected static final String PACKAGE_INFO_JSON = "package_info.json";
    protected static final String PROJECTS = "projects";
    protected static final String PROCESS_DEFINITION = "processDefinition";
    protected static final String DATASOURCE = "datasource";
    protected static final String RESOURCE = "resource";
    protected static final String UDF_FUNCTION = "udfFunction";
    private static final String PROJECTS_JSON = "projects.json";

    protected final File packageRoot;

    protected DolphinSchedulerPackageLoader(File packageRoot) {this.packageRoot = packageRoot;}

    /**
     * get DolphinSchedulerPackage info
     *
     * @return DolphinSchedulerPackage
     */
    public abstract DolphinSchedulerPackage<Project, ProcessDefinitionType, DataSource, ResourceInfo, UdfFunction> getDolphinSchedulerPackage();

    /**
     * supported version
     *
     * @param version DolphinSchedulerVersion
     * @return true / false
     */
    public abstract boolean support(DolphinSchedulerVersion version);

    /**
     * load package directory
     */
    public abstract void loadPackage() throws IOException;

    protected List<Project> readProjects(File packageRoot) throws IOException {
        File projectsJson = new File(packageRoot, PROJECTS_JSON);
        String projects = FileUtils.readFileToString(projectsJson, StandardCharsets.UTF_8);
        List<Project> projectList = GsonUtils.fromJsonString(projects, new TypeToken<List<Project>>() {}.getType());
        return Optional.ofNullable(projectList).orElse(ListUtils.emptyIfNull(null));
    }

    protected List<Project> readProjects(File packageRoot, Type type) throws IOException {
        File projectsJson = new File(packageRoot, PROJECTS_JSON);
        String projects = FileUtils.readFileToString(projectsJson, StandardCharsets.UTF_8);
        List<Project> projectList = GsonUtils.fromJsonString(projects, type);
        return Optional.ofNullable(projectList).orElse(ListUtils.emptyIfNull(null));
    }

    protected static PackageInfo readPackageInfo(File rootDir) throws IOException {
        File packageInfoJson = new File(rootDir, PACKAGE_INFO_JSON);
        String jsonContent = FileUtils.readFileToString(packageInfoJson, StandardCharsets.UTF_8);
        PackageInfo info = GsonUtils.fromJsonString(jsonContent, new TypeToken<PackageInfo>() {}.getType());
        if (info == null) {
            throw new RuntimeException("error read pacakge_info.json file");
        }

        return info;
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> readJsonFiles(File dir, TypeToken<List<T>> typeToken) {
        if (!dir.exists()) {
            return ListUtils.emptyIfNull(null);
        }

        File[] subJsons = Optional.ofNullable(dir.listFiles(f ->
                StringUtils.endsWithIgnoreCase(f.getName(), ".json"))).orElse(new File[]{});
        return Arrays.stream(subJsons)
                .map(jsonFile -> {
                    try {
                        return FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(json -> (List<T>) GsonUtils.fromJsonString(json, typeToken.getType()))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <Project, ProcessDefinitionType, DataSource, ResourceInfo, UdfFunction> DolphinSchedulerPackageLoader<Project,
            ProcessDefinitionType, DataSource, ResourceInfo, UdfFunction> create(
            File packageDir) throws IOException {
        PackageInfo pacakgeInfo = readPackageInfo(packageDir);
        DolphinSchedulerVersion version = pacakgeInfo.getDolphinSchedulerVersion();
        switch (version) {
            case V1:
                return (DolphinSchedulerPackageLoader<Project, ProcessDefinitionType, DataSource, ResourceInfo,
                        UdfFunction>) new DolphinSchedulerV1PackageLoader(
                        packageDir);
            case V2:
                return (DolphinSchedulerPackageLoader<Project, ProcessDefinitionType, DataSource, ResourceInfo,
                        UdfFunction>) new DolphinSchedulerV2PackageLoader(
                        packageDir);
            case V3:
                return (DolphinSchedulerPackageLoader<Project, ProcessDefinitionType, DataSource, ResourceInfo,
                        UdfFunction>) new DolphinSchedulerV3PackageLoader(
                        packageDir);
            default:
                throw new RuntimeException("unsupported dolphinscheduler version: " + version);
        }
    }
}
