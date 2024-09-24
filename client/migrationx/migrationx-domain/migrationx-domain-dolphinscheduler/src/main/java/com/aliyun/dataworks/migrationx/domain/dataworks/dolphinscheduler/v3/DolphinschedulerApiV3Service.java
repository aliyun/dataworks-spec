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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.BatchExportProcessDefinitionByIdsRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.DolphinSchedulerApi;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.DolphinSchedulerRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.DownloadResourceRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.PaginateResponse;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.QueryDataSourceListByPaginateRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.QueryProcessDefinitionByPaginateRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.QueryResourceListRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.QueryUdfFuncListByPaginateRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.Response;
import com.aliyun.migrationx.common.http.HttpClientUtil;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

/**
 * V3 Dolphinscheduler Api Implementations
 *
 * @author 聿剑
 * @date 2024/4/20
 */
@Slf4j
public class DolphinschedulerApiV3Service implements DolphinSchedulerApi {
    private static final String HEADER_TOKEN = "token";
    private final String endpoint;
    private final String token;

    public DolphinschedulerApiV3Service(String endpoint, String token) {
        this.endpoint = endpoint;
        this.token = token;
    }

    private HttpGet newHttpGet(String url) throws URISyntaxException {
        HttpGet httpGet = new HttpGet();
        httpGet.setHeader(HEADER_TOKEN, token);
        String finalUrl = MessageFormat.format("{0}/dolphinscheduler/{1}", endpoint, url);
        httpGet.setURI(new URI(finalUrl));
        return httpGet;
    }

    private HttpPost newHttpPost(String url) throws URISyntaxException {
        HttpPost httpPost = new HttpPost();
        httpPost.setHeader(HEADER_TOKEN, token);
        String finalUrl = MessageFormat.format("{0}/dolphinscheduler/{1}", endpoint, url);
        httpPost.setURI(new URI(finalUrl));
        return httpPost;
    }

    @Override
    public PaginateResponse<JsonObject> queryProcessDefinitionByPaging(QueryProcessDefinitionByPaginateRequest request) throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        String url = String.format("projects/%s/process-definition?pageNo=%s&pageSize=%s",
                request.getProjectCode(), request.getPageNo(), request.getPageSize());
        String responseStr = client.executeAndGet(newHttpGet(url));
        return GsonUtils.fromJsonString(responseStr, new TypeToken<PaginateResponse<JsonObject>>() {}.getType());
    }

    @Override
    public String batchExportProcessDefinitionByIds(BatchExportProcessDefinitionByIdsRequest request) throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        String url = String.format("projects/%s/process-definition/batch-export?codes=%s",
                request.getProjectCode(),
                Joiner.on(",").join(ListUtils.emptyIfNull(request.getIds()).stream().distinct().collect(Collectors.toList())));
        return client.executeAndGet(newHttpPost(url));
    }

    @Override
    public Response<List<JsonObject>> queryResourceList(QueryResourceListRequest request) throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        String url = String.format("resources/query-by-type?type=%s", request.getType());
        HttpGet httpGet = newHttpGet(url);
        String responseStr = client.executeAndGet(httpGet);
        return GsonUtils.fromJsonString(responseStr, new TypeToken<Response<List<JsonObject>>>() {}.getType());
    }

    public String getSuggestedFileName(Header contentDispositionHeader) {
        String value = contentDispositionHeader.getValue();
        return Arrays.stream(StringUtils.split(value, ";"))
                .map(StringUtils::trim)
                .filter(token -> StringUtils.startsWithIgnoreCase(token, "filename="))
                .findFirst()
                .map(fileNamePart -> StringUtils.replace(fileNamePart, "filename=", ""))
                .map(fileName -> RegExUtils.replaceAll(fileName, "^\"", ""))
                .map(fileName -> RegExUtils.replaceAll(fileName, "\"$", ""))
                .orElse(null);
    }

    @Override
    public File downloadResource(DownloadResourceRequest request) throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        String url = String.format("resources/download?fullName=%s", request.getFullName());
        HttpGet httpGet = newHttpGet(url);
        HttpResponse resp = client.executeAndGetHttpResponse(httpGet);
        if (HttpStatus.SC_OK != resp.getStatusLine().getStatusCode()) {
            throw new RuntimeException("download file " + request.getFullName() + " error with status " + resp.getStatusLine());
        }
        InputStream inputStream = resp.getEntity().getContent();
        String fileName = Stream.of(resp.getAllHeaders())
                .filter(header -> StringUtils.equalsIgnoreCase(header.getName(), "Content-Disposition"))
                .findFirst()
                .map(this::getSuggestedFileName)
                .orElse(null);

        if (StringUtils.isBlank(fileName)) {
            String content = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
            Response<Object> response = GsonUtils.fromJsonString(content, new TypeToken<Response<Object>>() {}.getType());
            log.warn("download resource url: {} failed: {}",
                    url, Optional.ofNullable(response).map(Response::getMsg).orElse(content));
            return null;
        }

        File tmpFile = new File(FileUtils.getTempDirectory(), fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
        IOUtils.copy(inputStream, fileOutputStream);
        return tmpFile;
    }

    @Override
    public PaginateResponse<JsonObject> queryUdfFuncListByPaging(QueryUdfFuncListByPaginateRequest request) throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        String url = String.format("resources/udf-func?pageNo=%s&pageSize=%s", request.getPageNo(), request.getPageSize());
        HttpGet httpGet = newHttpGet(url);
        String responseStr = client.executeAndGet(httpGet);
        return GsonUtils.fromJsonString(responseStr, new TypeToken<PaginateResponse<JsonObject>>() {}.getType());
    }

    @Override
    public PaginateResponse<JsonObject> queryDataSourceListByPaging(QueryDataSourceListByPaginateRequest request) throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        String url = String.format("datasources?pageNo=%s&pageSize=%s", request.getPageNo(), request.getPageSize());
        HttpGet httpGet = newHttpGet(url);
        String responseStr = client.executeAndGet(httpGet);
        return GsonUtils.fromJsonString(responseStr, new TypeToken<PaginateResponse<JsonObject>>() {}.getType());
    }

    @Override
    public Response<List<JsonObject>> queryAllProjectList(DolphinSchedulerRequest request) throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        String url = "projects/list";
        HttpGet httpGet = newHttpGet(url);
        String responseStr = client.executeAndGet(httpGet);
        return GsonUtils.fromJsonString(responseStr, new TypeToken<Response<List<JsonObject>>>() {}.getType());
    }
}
