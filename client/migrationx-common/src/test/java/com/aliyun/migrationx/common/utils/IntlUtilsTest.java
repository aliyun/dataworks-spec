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

package com.aliyun.migrationx.common.utils;

import com.aliyun.migrationx.common.exception.ErrorCode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

/**
 * @author 聿剑
 * @date 2022/12/28
 */
public class IntlUtilsTest {
    @Test
    public void testI18nResource() {
        ThreadLocalUtils.setLocale(Locale.SIMPLIFIED_CHINESE);
        Assert.assertEquals("未知的枚举类型: ErrorCode, 取值: 1",
            IntlUtils.getByEnum(ErrorCode.UNKNOWN_ENUM_TYPE, ErrorCode.class.getSimpleName(), 1));
    }
}
