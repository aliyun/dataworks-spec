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

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.BatchExportProcessDefinitionByIdsRequest;
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
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.aliyun.migrationx.common.utils.PaginateUtils;
import com.aliyun.migrationx.common.utils.ZipUtils;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 聿剑
 * @date 2022/10/19
 */
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
    private List<String> projects;
    private final File exportFile;
    private Boolean skipResources = false;
    private final DolphinSchedulerApiService dolphinSchedulerApiService;

    public DolphinSchedulerReader(String endpoint, String token, String version, List<String> projects,
        File exportFile) {
        this.version = version;
        this.projects = projects;
        this.exportFile = exportFile;
        this.dolphinSchedulerApiService = new DolphinSchedulerApiService(endpoint, token);
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

        return doPackage(tmpDir, exportFile);
    }

    private File doPackage(File tmpDir, File exportFile) throws IOException {
        return ZipUtils.zipDir(tmpDir, exportFile);
    }

    private int queryProcessDefinitionCount(String project) throws Exception {
        QueryProcessDefinitionByPaginateRequest request = new QueryProcessDefinitionByPaginateRequest();
        request.setPageSize(1);
        request.setPageNo(1);
        request.setProjectName(project);
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
        request.setProjectName(project);
        PaginateResponse<JsonObject> response = dolphinSchedulerApiService.queryProcessDefinitionByPaging(request);
        return Optional.ofNullable(response)
            .map(Response::getData)
            .map(PaginateData::getTotalList)
            .orElse(new ArrayList<>(1));
    }

    private String batchExportProcessDefinitionByIds(List<Integer> ids, String project) throws Exception {
        BatchExportProcessDefinitionByIdsRequest request = new BatchExportProcessDefinitionByIdsRequest();
        request.setIds(ids);
        request.setProjectName(project);
        return dolphinSchedulerApiService.batchExportProcessDefinitionByIds(request);
    }

    private void doExport(File tmpDir) throws Exception {
        writePackageInfoJson(tmpDir);

        exportProjects(tmpDir);

        exportResourceFiles(tmpDir);

        exportUdfFunctions(tmpDir);

        exportDatasources(tmpDir);

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
                .filter(proj -> this.projects.stream().anyMatch(prjName ->
                    StringUtils.equalsIgnoreCase(prjName, proj.get("name").getAsString())))
                .collect(Collectors.toList());
        } else {
            this.projects = ListUtils.emptyIfNull(projectsList).stream()
                .map(proj -> proj.get("name").getAsString())
                .collect(Collectors.toList());
        }

        File projectFile = new File(tmpDir, PROJECTS_JSON);
        FileUtils.writeStringToFile(projectFile, GsonUtils.toJsonString(projectsList), StandardCharsets.UTF_8);
    }

    private void exportDatasources(File tmpDir) throws InterruptedException {
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
                FileUtils.writeStringToFile(
                    new File(datasourceDir, "datasource_page_" + p.getPageNum() + ".json"),
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
                resources.addAll(response.getData());
                if (!BooleanUtils.isTrue(skipResources)) {
                    ListUtils.emptyIfNull(response.getData()).forEach(resource -> {
                        DownloadResourceRequest downloadResourceRequest = new DownloadResourceRequest();
                        downloadResourceRequest.setId(resource.get("id").getAsInt());
                        try {
                            File file = dolphinSchedulerApiService.downloadResource(downloadResourceRequest);
                            Optional.ofNullable(file).ifPresent(file1 -> {
                                try {
                                    LOGGER.info("downloaded resource file: {}", file);
                                    FileUtils.copyFile(file, new File(resourceDir, file.getName()));
                                } catch (IOException e) {
                                    LOGGER.error("copy file error: ", e);
                                    throw new RuntimeException(e);
                                }
                            });
                        } catch (Exception e) {
                            LOGGER.error("download resource error: {}", e.getMessage());
                            throw new RuntimeException(e);
                        }
                    });
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

    private void exportProcessDefinition(File projectDir, String project) throws Exception {
        int count = queryProcessDefinitionCount(project);
        LOGGER.info("total process definition count: {}", count);

        LOGGER.info("exporting process definition by page");
        File processDefinitionDir = new File(projectDir, PROCESS_DEFINITION);
        if (!processDefinitionDir.mkdirs()) {
            LOGGER.error("error create process definition directory: {}", processDefinitionDir);
            return;
        }

        PaginateUtils.Paginator paginator = new PaginateUtils.Paginator();
        paginator.setPageSize(20);
        paginator.setPageNum(1);
        PaginateUtils.doPaginate(paginator, p -> {
            try {
                List<JsonObject> processDefinitions = queryProcessDefinitionByPage(p, project);
                Map<String, Integer> nameIdMap = ListUtils.emptyIfNull(processDefinitions).stream()
                    .collect(Collectors.toMap(
                        js -> js.get("name").getAsString(),
                        js -> js.getAsJsonObject().get("id").getAsInt()));

                if (MapUtils.isNotEmpty(nameIdMap)) {
                    String response = batchExportProcessDefinitionByIds(new ArrayList<>(nameIdMap.values()), project);
                    List<JsonObject> dsResponse = GsonUtils.fromJsonString(response,
                        new TypeToken<List<JsonObject>>() {}.getType());
                    ListUtils.emptyIfNull(dsResponse).forEach(jsonObject -> {
                        String processDefinitionName = jsonObject.has("processDefinitionName") ?
                            jsonObject.get("processDefinitionName").getAsString() : null;
                        Optional.ofNullable(nameIdMap.get(processDefinitionName)).ifPresent(id ->
                            jsonObject.addProperty("processDefinitionId", id));
                    });

                    FileUtils.writeStringToFile(
                        new File(processDefinitionDir, "process_definitions_page_" + p.getPageNum() + ".json"),
                        GsonUtils.toJsonString(dsResponse),
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
}
