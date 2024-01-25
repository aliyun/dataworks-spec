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

package com.aliyun.dataworks.migrationx.domain.dataworks.standard.service;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author 聿剑
 * @date 2023/01/12
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode
public class ResultList<T> implements Serializable {
    private static final long serialVersionUID = 7154887528070131284L;
    private String code;
    private Boolean success;
    private List<T> data;
    private String errMsg;
    private Integer errCode;
    private String requestId;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalNum;
    private Integer totalPages;

    public static <T> ResultList<T> of(String msg, Integer errCode, List<T> data, Boolean success, String sessionId,
                                       Integer currentPage, Integer pageSize, Integer totalNum) {
        ResultList<T> result = new ResultList<>();
        result
            .setErrMsg(msg)
            .setSuccess(success)
            .setData(data)
            .setErrCode(errCode)
            .setRequestId(sessionId)
            .setTotalNum(totalNum)
            .setPageSize(pageSize)
            .setCurrentPage(currentPage)
            .setTotalPages((int)Math.ceil((float)totalNum / (float)pageSize));
        return result;
    }

    public static <T> ResultList<T> ofSuccess(List<T> data, Integer currentPage, Integer pageSize, Integer totalNum) {
        return of(null, Code.SUCCESS.code, data, true, "", currentPage, pageSize, totalNum);
    }

    public static <T> ResultList<T> ofSuccess(List<T> data) {
        return of(null, Code.SUCCESS.code, data, true, "", 1, data.size(), data.size());
    }

    public static <T> ResultList<T> ofError(String msg) {
        return of(msg, Code.ERROR.code, null, true, "", 0, 0, 0);
    }

    public static <T> ResultList<T> ofError(String msg, String sessionId) {
        return of(msg, Code.ERROR.code, null, true, sessionId, 0, 0, 0);
    }

    public static <T> ResultList<T> ofError(String msg, Integer code) {
        return of(msg, code, null, true, "", 0, 0, 0);
    }

    protected boolean canEqual(Object other) {
        return other instanceof ResultList;
    }

    public String toString() {
        return "ResultList(code=" + this.getCode() + ", success=" + this.getSuccess() + ", data=" + this.getData()
            + ", errMsg=" + this.getErrMsg() + ", errCode=" + this.getErrCode() + ", requestId=" + this.getRequestId()
            + ", currentPage=" + this.getCurrentPage() + ", pageSize=" + this.getPageSize() + ", totalNum="
            + this.getTotalNum() + ", totalPages=" + this.getTotalPages() + ")";
    }
}

