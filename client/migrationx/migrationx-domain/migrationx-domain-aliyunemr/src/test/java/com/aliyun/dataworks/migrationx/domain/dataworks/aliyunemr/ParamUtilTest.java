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

package com.aliyun.dataworks.migrationx.domain.dataworks.aliyunemr;

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO 概要描述
 * <p>TODO 详细描述
 *
 * @author 聿剑
 * @date 2024/5/12
 */
public class ParamUtilTest {
    @Test
    public void test() {
        Assert.assertEquals("$[yyyymmddhh24-7/24]", ParamUtil.convertParameterExpression("${yyyyMMddHH-7h}"));
        Assert.assertEquals("${yyyymm-7}", ParamUtil.convertParameterExpression("${yyyyMM-7m}"));
        Assert.assertEquals("${yyyy-7}", ParamUtil.convertParameterExpression("${yyyy-7y}"));
        Assert.assertEquals("$[yyyymmdd-26]", ParamUtil.convertParameterExpression("${yyyyMMdd-26d}"));
        Assert.assertEquals("$[ddmmyyyy-26]", ParamUtil.convertParameterExpression("${ddMMyyyy-26d}"));
        Assert.assertEquals("$[ddmmyyyy-26]", ParamUtil.convertParameterExpression("${ddMMyyyy - 26d}"));
        Assert.assertEquals("$[yyyymmdd hh24:mi:ss]",
            ParamUtil.convertParameterExpression("${yyyyMMdd HH:mm:ss}"));
        Assert.assertEquals("$[yyyymmdd-4]", ParamUtil.convertParameterExpression("${yyyyMMdd-4d}"));
    }
}
