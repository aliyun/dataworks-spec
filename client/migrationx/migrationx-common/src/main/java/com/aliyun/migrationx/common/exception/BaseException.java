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

package com.aliyun.migrationx.common.exception;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Base Exception is the parent of all exceptions
 *
 * @author sam.liux
 */
@Data
@Accessors(chain = true)
public abstract class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private ErrorCode errCode;
    private String message;

    public BaseException(ErrorCode errCode, String errMessage) {
        super(errMessage);
        this.errCode = errCode;
        this.message = errMessage;
    }

    public BaseException(ErrorCode errCode, String errMessage, Throwable e) {
        super(errMessage, e);
        this.errCode = errCode;
        this.message = errMessage;
    }
}
