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
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author 聿剑
 * @date 2023/01/12
 */
@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode
public class Result<T> {

    private String code;
    private Boolean success;
    private T data;
    private Integer errCode;
    private String errMsg;
    private String requestId;

    public static Result ofError(String msg, Integer errCode) {
        return of(msg, errCode, (Object)null, false);
    }

    public static Result ofError(String msg, Integer code, String sessionId) {
        return of(msg, code, (Object)null, false, sessionId);
    }

    public static Result ofErrorWithCode(String msg, String code) {
        return ofWithCode(msg, Code.ERROR.code, (Object)null, false, code);
    }

    public static Result ofError(String msg) {
        return of(msg, Code.ERROR.code, null, false);
    }

    public static Result ofError(String msg, String sessionId) {
        return of(msg, Code.ERROR.code, null, false, sessionId);
    }

    public static <T> Result<T> ofSuccess(T data) {
        return of(null, Code.SUCCESS.code, data, true);
    }

    public static <T> Result<T> ofSuccess(Integer errCode, T data) {
        return of(null, errCode, data, true);
    }

    public static <T> Result<T> of(String msg, Integer code, T data, Boolean success) {
        Result<T> result = new Result<>();
        result.setErrMsg(msg).setSuccess(success).setData(data).setErrCode(code);
        return result;
    }

    public static <T> Result<T> ofWithCode(String msg, Integer errCode, T data, Boolean success, String code) {
        Result<T> result = new Result<>();
        result.setErrMsg(msg).setSuccess(success).setData(data).setCode(code).setErrCode(errCode);
        return result;
    }

    public static <T> Result<T> of(String msg, Integer code, T data, Boolean success, String requestId) {
        Result<T> result = of(msg, code, data, success).setRequestId(requestId);
        return result;
    }
}
