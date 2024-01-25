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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1;

import com.aliyun.migrationx.common.http.HttpClientUtil;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.google.common.base.Joiner;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 聿剑
 * @date 2022/10/20
 */
@Slf4j
public class DolphinSchedulerApiService implements DolphinSchedulerApi {
    private final String endpoint;
    private final String token;

    public DolphinSchedulerApiService(String endpoint, String token) {
        this.endpoint = endpoint;
        this.token = token;
    }

    @Override
    public PaginateResponse<JsonObject> queryProcessDefinitionByPaging(QueryProcessDefinitionByPaginateRequest request)
        throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        HttpGet httpGet = new HttpGet();
        httpGet.setHeader("token", token);
        String url = MessageFormat.format("{0}/dolphinscheduler/projects/{1}/process/list-paging?pageNo={2}&pageSize={3}",
            endpoint, request.getProjectName(), request.getPageNo(), request.getPageSize());
        httpGet.setURI(new URI(url));
        String responseStr = client.executeAndGet(httpGet);
        return GsonUtils.fromJsonString(responseStr, new TypeToken<PaginateResponse<JsonObject>>() {}.getType());
    }

    @Override
    public String batchExportProcessDefinitionByIds(BatchExportProcessDefinitionByIdsRequest request) throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        HttpGet httpGet = new HttpGet();
        httpGet.setHeader("token", token);
        String url = MessageFormat.format("{0}/dolphinscheduler/projects/{1}/process/export?processDefinitionIds={2}",
            endpoint, request.getProjectName(),
            Joiner.on(",").join(ListUtils.emptyIfNull(request.getIds()).stream().distinct().collect(Collectors.toList())));
        httpGet.setURI(new URI(url));
        return client.executeAndGet(httpGet);
    }

    @Override
    public Response<List<JsonObject>> queryResourceList(QueryResourceListRequest request) throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        HttpGet httpGet = new HttpGet();
        httpGet.setHeader("token", token);
        String url = MessageFormat.format("{0}/dolphinscheduler/resources/list?type={1}",
            endpoint, request.getType());
        httpGet.setURI(new URI(url));
        String responseStr = client.executeAndGet(httpGet);
        return GsonUtils.fromJsonString(responseStr, new TypeToken<Response<List<JsonObject>>>() {}.getType());
    }

    @Override
    public File downloadResource(DownloadResourceRequest request) throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        HttpGet httpGet = new HttpGet();
        httpGet.setHeader("token", token);
        String url = MessageFormat.format("{0}/dolphinscheduler/resources/download?id={1}",
            endpoint, request.getId());
        httpGet.setURI(new URI(url));
        HttpResponse resp = client.executeAndGetHttpResponse(httpGet);
        InputStream inputStream = resp.getEntity().getContent();
        String fileName = Stream.of(resp.getAllHeaders())
            .filter(header -> StringUtils.equalsIgnoreCase(header.getName(), "Content-Disposition"))
            .findFirst()
            .map(this::getSuggestedFileName)
            .orElse(null);

        if (StringUtils.isBlank(fileName)) {
            String content = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
            Response<Object> response = GsonUtils.fromJsonString(content, new TypeToken<Response<Object>>() {}.getType());
            log.warn("download resource id: {} failed: {}",
                request.getId(), Optional.ofNullable(response).map(Response::getMsg).orElse(content));
            return null;
        }

        File tmpFile = new File(FileUtils.getTempDirectory(), fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
        IOUtils.copy(inputStream, fileOutputStream);
        return tmpFile;
    }

    @Override
    public PaginateResponse<JsonObject> queryUdfFuncListByPaging(QueryUdfFuncListByPaginateRequest request)
        throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        HttpGet httpGet = new HttpGet();
        httpGet.setHeader("token", token);
        String url = MessageFormat.format("{0}/dolphinscheduler/resources/udf-func/list-paging?pageNo={1}&pageSize={2}",
            endpoint, request.getPageNo(), request.getPageSize());
        httpGet.setURI(new URI(url));
        String responseStr = client.executeAndGet(httpGet);
        return GsonUtils.fromJsonString(responseStr, new TypeToken<PaginateResponse<JsonObject>>() {}.getType());
    }

    @Override
    public PaginateResponse<JsonObject> queryDataSourceListByPaging(QueryDataSourceListByPaginateRequest request)
        throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        HttpGet httpGet = new HttpGet();
        httpGet.setHeader("token", token);
        String url = MessageFormat.format("{0}/dolphinscheduler/datasources/list-paging?pageNo={1}&pageSize={2}",
            endpoint, request.getPageNo(), request.getPageSize());
        httpGet.setURI(new URI(url));
        String responseStr = client.executeAndGet(httpGet);
        return GsonUtils.fromJsonString(responseStr, new TypeToken<PaginateResponse<JsonObject>>() {}.getType());
    }

    @Override
    public Response<List<JsonObject>> queryAllProjectList(DolphinSchedulerRequest request) throws Exception {
        HttpClientUtil client = new HttpClientUtil();
        HttpGet httpGet = new HttpGet();
        httpGet.setHeader("token", token);
        String url = MessageFormat.format("{0}/dolphinscheduler/projects/query-project-list",
            endpoint, request.getPageNo(), request.getPageSize());
        httpGet.setURI(new URI(url));
        String responseStr = client.executeAndGet(httpGet);
        return GsonUtils.fromJsonString(responseStr, new TypeToken<Response<List<JsonObject>>>() {}.getType());
    }

    private String getSuggestedFileName(Header contentDispositionHeader) {
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
}
