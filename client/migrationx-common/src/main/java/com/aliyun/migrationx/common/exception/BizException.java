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

import com.aliyun.migrationx.common.utils.IntlUtils;
import com.google.common.base.Joiner;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * BizException is known Exception, no need retry
 *
 * @author sam.liux
 */
@Accessors(chain = true)
@Slf4j
public class BizException extends BaseException {
    public BizException(ErrorCode errCode, Object ... errMessages) {
        super(errCode, errCode.getMessage());
        setMessage(formatMessage(errCode, errMessages));
    }

    public BizException(ErrorCode errorCode, Throwable e, Object ...errMessages) {
        super(errorCode, errorCode.getMessage(), e);
        setMessage(formatMessage(errorCode, errMessages));
    }

    private String formatMessage(ErrorCode error, Object... infos) {
        String errorMessage = error.getMessage();
        try {
            errorMessage = error.getMessage();
            Map<String, String> params = new HashMap<>();
            int index = 0;
            for (Object info : infos) {
                params.put(String.valueOf(index++), String.valueOf(info));
            }
            return IntlUtils.get(error.name(), params)
                .d(MessageFormat.format(errorMessage, Arrays.stream(infos).map(String::valueOf).toArray()));
        } catch (Exception ex) {
            log.warn("format message code: {}, name: {}, exception: ", error.name(), errorMessage, ex);
        }

        return Joiner.on("\n").join(Stream.of(infos)
            .filter(Objects::nonNull).map(String::valueOf).collect(Collectors.toList()));
    }

    public static BizException of(ErrorCode errorCode) {
        return new BizException(errorCode);
    }

    public static BizException of(ErrorCode errorCode, Throwable e) {
        return new BizException(errorCode, e);
    }

    public BizException with(Object ...info) {
        setMessage(this.formatMessage(this.getErrCode(), info));
        return this;
    }
}