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

package com.aliyun.dataworks.migrationx.dolphinscheduler.v1;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.DolphinSchedulerApiService;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.QueryProcessDefinitionByPaginateRequest;
import com.aliyun.migrationx.common.http.HttpClientUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author 聿剑
 * @date 2022/10/20
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DolphinSchedulerApiService.class, HttpClientUtil.class})
public class DolphinSchedulerApiServiceTest {
    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testQueryProcessDefinitionByPaginate() throws Exception {
        HttpClientUtil httpClient = PowerMockito.mock(HttpClientUtil.class);
        PowerMockito.whenNew(HttpClientUtil.class).withAnyArguments().thenReturn(httpClient);
        String response = "";
        Mockito.doReturn(response).when(httpClient).executeAndGet(Mockito.any());
        DolphinSchedulerApiService service = new DolphinSchedulerApiService("", "");
        QueryProcessDefinitionByPaginateRequest request = new QueryProcessDefinitionByPaginateRequest();
        service.queryProcessDefinitionByPaging(request);
    }
}
