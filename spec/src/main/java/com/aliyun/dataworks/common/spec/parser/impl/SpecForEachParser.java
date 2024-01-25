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

package com.aliyun.dataworks.common.spec.parser.impl;

import java.util.Map;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.annotation.SpecParser;
import com.aliyun.dataworks.common.spec.domain.noref.SpecForEach;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;

/**
 * @author yiwei.qyw
 * @date 2023/7/17
 */
@SpecParser
public class SpecForEachParser extends DefaultSpecParser<SpecForEach> {
    public static final String FOREACH = "for-each";
    public static final String ARRAY = "array";

    @Override
    public SpecForEach parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        SpecForEach specForEach = new SpecForEach();

        SpecDevUtil.setSameKeyField(rawContext, specForEach, specParserContext);
        SpecDevUtil.setSpecObject(specForEach, ARRAY, rawContext.get(ARRAY), specParserContext);

        Optional.ofNullable(specForEach.getArray()).ifPresent(array -> SpecDevUtil.setEntityToCtx(array, specParserContext));
        return specForEach;
    }

    @Override
    public String getKeyType() {
        return FOREACH;
    }
}