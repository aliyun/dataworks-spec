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

package com.aliyun.dataworks.migrationx.reader.dolphinscheduler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.BatchExportProcessDefinitionByIdsRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.DolphinSchedulerApi;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.DolphinSchedulerApiService;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.DolphinSchedulerRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.DownloadResourceRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.PaginateData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.PaginateResponse;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.QueryDataSourceListByPaginateRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.QueryProcessDefinitionByPaginateRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.QueryResourceListRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.QueryUdfFuncListByPaginateRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.Response;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DolphinSchedulerApiV2Service;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.ResourceComponent;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DolphinschedulerApiV3Service;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.aliyun.migrationx.common.utils.JSONUtils;
import com.aliyun.migrationx.common.utils.PaginateUtils;
import com.aliyun.migrationx.common.utils.ZipUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 聿剑
 * @date 2022/10/19
 */
@Slf4j
public class DolphinSchedulerReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DolphinSchedulerReader.class);
    private static final String PACKAGE_INFO_JSON = "package_info.json";
    private static final String PROCESS_DEFINITION = "processDefinition";
    private static final String RESOURCE = "resource";
    private static final String UDF_FUNCTION = "udfFunction";
    private static final String DATASOURCE = "datasource";

    private static final String PROJECTS = "projects";
    private static final String PROJECTS_JSON = "projects.json";

    private final String version;
    private final List<String> projects;
    private List<Project> projectInfoList = new ArrayList<>();
    private Map<String, Long> projectNameToCodeMap = new HashMap<>();
    private final File exportFile;
    private Boolean skipResources = true;
    private final DolphinSchedulerApi dolphinSchedulerApiService;

    public DolphinSchedulerReader(String endpoint, String token, String version, List<String> projects,
            File exportFile) {
        this.version = version;
        this.projects = projects;
        this.exportFile = exportFile;
        if (isVersion1()) {
            this.dolphinSchedulerApiService = new DolphinSchedulerApiService(endpoint, token);
        } else if (isVersion2()) {
            this.dolphinSchedulerApiService = new DolphinSchedulerApiV2Service(endpoint, token);
        } else if (isVersion3()) {
            this.dolphinSchedulerApiService = new DolphinschedulerApiV3Service(endpoint, token);
        } else {
            throw new RuntimeException("unsupported dolphinscheduler version: " + version);
        }
    }

    public File export() throws Exception {
        File parent = new File(exportFile.getParentFile(), StringUtils.split(exportFile.getName(), ".")[0]);

        if (!parent.exists() && !parent.mkdirs()) {
            LOGGER.error("failed create file directory for: {}", exportFile);
            return null;
        }

        LOGGER.info("workspace directory: {}", parent);

        File tmpDir = new File(parent, ".tmp");
        if (tmpDir.exists()) {
            FileUtils.deleteDirectory(tmpDir);
        }

        doExport(tmpDir);
        if (exportFile.getName().endsWith("zip")) {
            return doPackage(tmpDir, exportFile);
        } else {
            return tmpDir;
        }
    }

    private File doPackage(File tmpDir, File exportFile) throws IOException {
        return ZipUtils.zipDir(tmpDir, exportFile);
    }

    private void doExport(File tmpDir) throws Exception {
        writePackageInfoJson(tmpDir);

        exportProjects(tmpDir);

        exportResourceFiles(tmpDir);

        exportUdfFunctions(tmpDir);

        exportDataSources(tmpDir);

        ListUtils.emptyIfNull(projects).forEach(project -> {
            try {
                File projects = new File(tmpDir, PROJECTS);
                exportProcessDefinition(new File(projects, project), project);
            } catch (Exception e) {
                LOGGER.error("export project: {} process definition failed: ", project);
                throw new RuntimeException(e);
            }
        });
    }

    private void exportProjects(File tmpDir) throws Exception {
        Response<List<JsonObject>> response = dolphinSchedulerApiService.queryAllProjectList(
                new DolphinSchedulerRequest());

        List<JsonObject> projectsList = response.getData();
        if (CollectionUtils.isNotEmpty(this.projects)) {
            projectsList = ListUtils.emptyIfNull(projectsList).stream()
                    .filter(proj -> this.projects.stream()
                            .anyMatch(p -> {
                                if (StringUtils.equalsIgnoreCase(p, proj.get("name").getAsString())) {
                                    if (isVersion2() || isVersion3()) {
                                        Long code = proj.get("code").getAsLong();
                                        projectNameToCodeMap.put(p, code);
                                    }
                                    return true;
                                }
                                return false;
                            })
                    )
                    .collect(Collectors.toList());
        }

        this.projectInfoList = ListUtils.emptyIfNull(projectsList).stream()
                .map(proj -> GsonUtils.fromJsonString(GsonUtils.toJsonString(proj), new TypeToken<Project>() {}.getType()))
                .map(proj -> (Project) proj)
                .collect(Collectors.toList());

        File projectFile = new File(tmpDir, PROJECTS_JSON);
        FileUtils.writeStringToFile(projectFile, GsonUtils.toJsonString(projectsList), StandardCharsets.UTF_8);
    }

    private void exportDataSources(File tmpDir) throws InterruptedException {
        File datasourceDir = new File(tmpDir, DATASOURCE);
        if (!datasourceDir.exists() && !datasourceDir.mkdirs()) {
            LOGGER.error("error make datasource directory: {}", datasourceDir);
            return;
        }

        QueryDataSourceListByPaginateRequest request = new QueryDataSourceListByPaginateRequest();
        PaginateUtils.Paginator paginator = new PaginateUtils.Paginator();
        paginator.setPageNum(1);
        paginator.setPageSize(20);
        PaginateUtils.doPaginate(paginator, p -> {
            try {
                request.setPageNo(p.getPageNum());
                request.setPageSize(p.getPageSize());
                PaginateResponse<JsonObject> response = dolphinSchedulerApiService.queryDataSourceListByPaging(request);
                log.info("response: {}", response);
                FileUtils.writeStringToFile(
                        new File(datasourceDir, "datasource_page_" + p.getPageNum() + ".json"),
                        GsonUtils.toJsonString(Optional.ofNullable(response).map(Response::getData).map(PaginateData::getTotalList).orElse(null)),
                        StandardCharsets.UTF_8);
                PaginateUtils.PaginateResult<JsonObject> paginateResult = new PaginateUtils.PaginateResult<>();
                paginateResult.setPageNum(p.getPageNum());
                paginateResult.setPageSize(p.getPageSize());
                paginateResult.setData(Optional.ofNullable(response).map(Response::getData).map(PaginateData::getTotalList).orElse(null));
                paginateResult.setTotalCount(Optional.ofNullable(response).map(Response::getData).map(PaginateData::getTotal).orElse(0));
                return paginateResult;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void exportUdfFunctions(File tmpDir) throws Exception {
        File udfFunctionDir = new File(tmpDir, UDF_FUNCTION);
        if (!udfFunctionDir.exists() && !udfFunctionDir.mkdirs()) {
            LOGGER.error("error make udf function directory: {}", udfFunctionDir);
            return;
        }

        QueryUdfFuncListByPaginateRequest request = new QueryUdfFuncListByPaginateRequest();
        PaginateUtils.Paginator paginator = new PaginateUtils.Paginator();
        paginator.setPageNum(1);
        paginator.setPageSize(20);
        PaginateUtils.doPaginate(paginator, p -> {
            try {
                request.setPageNo(p.getPageNum());
                request.setPageSize(p.getPageSize());
                PaginateResponse<JsonObject> response = dolphinSchedulerApiService.queryUdfFuncListByPaging(request);
                FileUtils.writeStringToFile(
                        new File(udfFunctionDir, "udf_function_page_" + p.getPageNum() + ".json"),
                        GsonUtils.toJsonString(response.getData().getTotalList()),
                        StandardCharsets.UTF_8);
                PaginateUtils.PaginateResult<JsonObject> paginateResult = new PaginateUtils.PaginateResult<>();
                paginateResult.setPageNum(p.getPageNum());
                paginateResult.setPageSize(p.getPageSize());
                paginateResult.setData(response.getData().getTotalList());
                paginateResult.setTotalCount(response.getData().getTotal());
                return paginateResult;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void exportResourceFiles(File tmpDir) {
        File resourceDir = new File(tmpDir, RESOURCE);
        if (!resourceDir.exists() && !resourceDir.mkdirs()) {
            LOGGER.error("error make resource directory: {}", resourceDir);
            return;
        }

        List<JsonObject> resources = new ArrayList<>();
        Arrays.asList("FILE", "UDF").forEach(type -> {
            try {
                QueryResourceListRequest queryResourceListRequest = new QueryResourceListRequest();
                queryResourceListRequest.setType(type);
                Response<List<JsonObject>> response = dolphinSchedulerApiService.queryResourceList(
                        queryResourceListRequest);
                if (response.getData() == null) {
                    log.error("queryResourceList response {}", JSONUtils.toJsonString(response));
                    return;
                }
                resources.addAll(response.getData());
                if (!BooleanUtils.isTrue(skipResources)) {
                    List<ResourceComponent> resourceComponents = GsonUtils.fromJsonString(GsonUtils.toJsonString(response.getData()),
                            new com.google.common.reflect.TypeToken<List<ResourceComponent>>() {}.getType());
                    visitAndDownloadResource(resourceComponents, resourceDir.getAbsolutePath());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        try {
            FileUtils.writeStringToFile(
                    new File(resourceDir, "resources.json"),
                    GsonUtils.toJsonString(resources),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void visitAndDownloadResource(List<ResourceComponent> components, String parentDir) {
        for (ResourceComponent resource : components) {
            String currentDir = parentDir;
            if (resource.isDirctory()) {
                //mkdir
                if (resource.getName() != null) {
                    currentDir = currentDir + File.separator + resource.getName();
                }
                File file = new File(currentDir);
                if (!file.exists()) {
                    file.mkdirs();
                }
                //visit children
                visitAndDownloadResource(resource.getChildren(), currentDir);
            } else {
                //download file
                DownloadResourceRequest downloadResourceRequest = new DownloadResourceRequest();
                downloadResourceRequest.setId(resource.getId());
                downloadResourceRequest.setDir(currentDir);
                try {
                    dolphinSchedulerApiService.downloadResource(downloadResourceRequest);
                } catch (Exception e) {
                    LOGGER.error("download resource error: {}", e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void exportProcessDefinition(File projectDir, String project) throws Exception {
        int count = queryProcessDefinitionCount(project);
        if (count == 0) {
            LOGGER.warn("total process definition count: {}", count);
            return;
        } else {
            LOGGER.info("total process definition count: {}", count);
        }

        LOGGER.info("exporting process definition by page");
        File processDefinitionDir = new File(projectDir, PROCESS_DEFINITION);
        if (!processDefinitionDir.mkdirs()) {
            LOGGER.error("error create process definition directory: {}", processDefinitionDir);
            return;
        }

        PaginateUtils.Paginator paginator = new PaginateUtils.Paginator();
        paginator.setPageSize(5);
        paginator.setPageNum(1);
        PaginateUtils.doPaginate(paginator, p -> {
            try {
                List<JsonObject> processDefinitions = queryProcessDefinitionByPage(p, project);
                List<String> idList = ListUtils.emptyIfNull(processDefinitions).stream()
                        .map(js -> {
                            if (isVersion1()) {
                                return js.getAsJsonObject().get("id").getAsString();
                            } else {
                                return js.getAsJsonObject().get("code").getAsString();
                            }
                        })
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(idList)) {
                    String response = batchExportProcessDefinitionByIds(idList, project);
                    if (StringUtils.isEmpty(response)) {
                        throw new RuntimeException("get response by export process empty, "
                                + "project " + project + "id " + String.join(",", idList));
                    }
                    JsonNode jsonNode = JSONUtils.parseObject(response);
                    //error code
                    if (jsonNode.has("code") && jsonNode.get("code").asInt() > 0) {
                        throw new RuntimeException(response);
                    }
                    FileUtils.writeStringToFile(
                            new File(processDefinitionDir, "process_definitions_page_" + p.getPageNum() + ".json"),
                            JSONUtils.toPrettyString(jsonNode),
                            StandardCharsets.UTF_8);
                }

                PaginateUtils.PaginateResult<JsonObject> paginateResult = new PaginateUtils.PaginateResult<>();
                paginateResult.setPageNum(p.getPageNum());
                paginateResult.setPageSize(p.getPageSize());
                paginateResult.setData(processDefinitions);
                paginateResult.setTotalCount(count);
                return paginateResult;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private int queryProcessDefinitionCount(String project) throws Exception {
        QueryProcessDefinitionByPaginateRequest request = new QueryProcessDefinitionByPaginateRequest();
        request.setPageSize(1);
        request.setPageNo(1);
        //for dolphin 1.x
        request.setProjectName(project);
        //for dolphin 2.x 3.x
        Long code = getCodeByName(project);
        request.setProjectCode(code);
        PaginateResponse<JsonObject> response = dolphinSchedulerApiService.queryProcessDefinitionByPaging(request);
        return Optional.ofNullable(response)
                .map(Response::getData)
                .map(PaginateData::getTotal)
                .orElse(0);
    }

    private List<JsonObject> queryProcessDefinitionByPage(PaginateUtils.Paginator p, String project) throws Exception {
        QueryProcessDefinitionByPaginateRequest request = new QueryProcessDefinitionByPaginateRequest();
        request.setPageNo(p.getPageNum());
        request.setPageSize(p.getPageSize());
        Long code = getCodeByName(project);
        request.setProjectCode(code);
        request.setProjectName(project);
        PaginateResponse<JsonObject> response = dolphinSchedulerApiService.queryProcessDefinitionByPaging(request);
        return Optional.ofNullable(response)
                .map(Response::getData)
                .map(PaginateData::getTotalList)
                .orElse(new ArrayList<>(1));
    }

    private String batchExportProcessDefinitionByIds(List<String> ids, String project) throws Exception {
        BatchExportProcessDefinitionByIdsRequest request = new BatchExportProcessDefinitionByIdsRequest();
        request.setIds(ids);
        Long code = getCodeByName(project);
        request.setProjectCode(code);
        request.setProjectName(project);
        return dolphinSchedulerApiService.batchExportProcessDefinitionByIds(request);
    }

    private void writePackageInfoJson(File tmpDir) throws IOException {
        File packageInfoJson = new File(tmpDir, PACKAGE_INFO_JSON);
        LOGGER.info("writing {}", packageInfoJson);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("version", version);
        FileUtils.writeStringToFile(packageInfoJson, GsonUtils.toJsonString(jsonObject), StandardCharsets.UTF_8);
        LOGGER.info("writing {} done", packageInfoJson);
    }

    public DolphinSchedulerReader setSkipResources(Boolean skipResources) {
        this.skipResources = skipResources;
        return this;
    }

    private boolean isVersion1() {
        return StringUtils.startsWith(version, "1.");
    }

    private boolean isVersion2() {
        return StringUtils.startsWith(version, "2.");
    }

    private boolean isVersion3() {
        return StringUtils.startsWith(version, "3.");
    }

    private Long getCodeByName(String projectName) {
        Long code = projectNameToCodeMap.get(projectName);
        if (isVersion2() || isVersion3()) {
            if (code == null) {
                new RuntimeException("project code not found by name: " + projectName);
            }
        }
        return code;
    }
}
