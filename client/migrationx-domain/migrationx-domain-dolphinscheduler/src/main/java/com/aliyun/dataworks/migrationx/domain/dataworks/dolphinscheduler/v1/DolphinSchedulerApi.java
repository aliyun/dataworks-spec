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

import java.io.File;
import java.util.List;

import com.google.gson.JsonObject;

/**
 * @author 聿剑
 * @date 2022/10/20
 */
public interface DolphinSchedulerApi {
    /**
     * query process definition by paginate request
     *
     * @param request QueryProcessDefinitionByPaginateRequest
     * @return PaginateResponse<JsonObject>
     */
    PaginateResponse<JsonObject> queryProcessDefinitionByPaging(QueryProcessDefinitionByPaginateRequest request)
        throws Exception;

    /**
     * batch export process definition by ids, will get a plan text response of json array
     *
     * @param request BatchExportProcessDefinitionByIdsRequest
     * @return String
     */
    String batchExportProcessDefinitionByIds(BatchExportProcessDefinitionByIdsRequest request) throws Exception;

    /**
     * query resource list
     *
     * @param request QueryResourceListRequest
     * @return Response<List < JsonObject>>
     * @throws Exception ex
     */
    Response<List<JsonObject>> queryResourceList(QueryResourceListRequest request) throws Exception;

    /**
     * download resource to local file
     *
     * @param request DownloadResourceRequest
     * @return java.io.File
     * @throws Exception ex
     */
    File downloadResource(DownloadResourceRequest request) throws Exception;

    /**
     * query udf function list by paging
     *
     * @param request QueryUdfFuncListByPaginateRequest
     * @return PaginateResponse<JsonObject>
     * @throws Exception ex
     */
    PaginateResponse<JsonObject> queryUdfFuncListByPaging(QueryUdfFuncListByPaginateRequest request)
        throws Exception;

    /**
     * query datasource list by paging
     *
     * @param request QueryDataSourceLisByPaginateRequest
     * @return PaginateResponse<JsonObject>
     * @throws Exception ex
     */
    PaginateResponse<JsonObject> queryDataSourceListByPaging(QueryDataSourceListByPaginateRequest request)
        throws Exception;

    /**
     * query project list by paging
     *
     * @param request DolphinSchedulerRequest
     * @return PaginateResponse<JsonObject>
     * @throws Exception ex
     */
    Response<List<JsonObject>> queryAllProjectList(DolphinSchedulerRequest request) throws Exception;
}
