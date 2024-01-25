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

import com.aliyun.dataworks.common.spec.annotation.SpecParser;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFileResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTable;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;

/**
 * SpecTableParser
 *
 * @author 聿剑
 * @date 2023/11/30
 * @see SpecTable
 */
@SpecParser
public class SpecTableParser extends DefaultSpecParser<SpecTable> {
    public SpecTableParser() {
        super(SpecFileResource.class);
    }

    @Override
    public SpecTable parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        SpecTable table = new SpecTable();
        SpecDevUtil.setSameKeyField(rawContext, table, specParserContext);
        return table;
    }
}
