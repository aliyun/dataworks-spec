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

package com.aliyun.dataworks.common.spec.exception;

/**
 * @author yiwei.qyw
 * @date 2023/7/6
 */
public class SpecException extends RuntimeException {

    private String message;

    private SpecIErrorCode errorCode;

    public SpecException() {
        super();
    }

    public SpecIErrorCode getCode() {
        return errorCode;
    }

    public SpecException(SpecIErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    public SpecException(String message) {
        super(message);
        this.message = message;
    }

    public SpecException(Throwable cause, SpecIErrorCode errorCode, String message) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "\nerrorCode:" + errorCode.getCode() + "\n" + message;
    }

}