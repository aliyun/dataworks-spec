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
import java.util.Collections;
import java.util.List;

import com.aliyun.dataworks.common.spec.utils.ReflectUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.DolphinSchedulerApiService;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.PaginateData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.PaginateResponse;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.Response;

import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author 聿剑
 * @date 2024/5/23
 */
public class DolphinSchedulerReaderTest {
    @Test
    public void testReader() throws Exception {
        String endpoint = "http://endpoint";
        String token = "token";
        String version = "1.3.9";
        List<String> projects = Collections.singletonList("project1");
        DolphinSchedulerApiService service = Mockito.mock(DolphinSchedulerApiService.class);
        Response<List<JsonObject>> projectResponse = new Response<>();
        JsonObject projJson = new JsonObject();
        projJson.addProperty("name", "project1");
        projJson.addProperty("code", "project1");
        projectResponse.setData(Collections.singletonList(projJson));
        Mockito.when(service.queryAllProjectList(Mockito.any())).thenReturn(projectResponse);
        Response<List<JsonObject>> resourceResponse = new Response<>();
        resourceResponse.setData(Collections.emptyList());
        Mockito.when(service.queryResourceList(Mockito.any())).thenReturn(resourceResponse);
        PaginateResponse<JsonObject> functionResponse = new PaginateResponse<>();
        PaginateData<JsonObject> funcData = new PaginateData<>();
        funcData.setTotalPage(0).setTotal(0).setTotalList(Collections.emptyList());
        functionResponse.setData(funcData);
        Mockito.when(service.queryUdfFuncListByPaging(Mockito.any())).thenReturn(functionResponse);
        File exportFile = new File(new File(DolphinSchedulerReaderTest.class.getClassLoader().getResource("").getFile()), "export.zip");
        DolphinSchedulerReader reader = new DolphinSchedulerReader(endpoint, token, version, projects, exportFile);
        ReflectUtils.setFieldValue(reader, "dolphinSchedulerApiService", service);
        reader.export();
        Assert.assertTrue(exportFile.exists());
    }
}
