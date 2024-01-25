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
import com.aliyun.dataworks.common.spec.domain.noref.SpecDoWhile;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;

/**
 * @author yiwei.qyw
 * @date 2023/7/17
 */
@SpecParser
public class DoWhileParser implements Parser<SpecDoWhile> {
    public static final String DO_WHILE = "do-while";
    public static final String WHILE = "while";

    @Override
    public SpecDoWhile parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        SpecDoWhile specDoWhile = new SpecDoWhile();

        SpecDevUtil.setSameKeyField(rawContext, specDoWhile, specParserContext);
        SpecDevUtil.setSpecObject(specDoWhile, "specWhile", rawContext.get(WHILE), specParserContext);
        Optional.ofNullable(specDoWhile.getSpecWhile()).ifPresent(whileNode -> SpecDevUtil.setEntityToCtx(whileNode, specParserContext));
        return specDoWhile;
    }

    @Override
    public String getKeyType() {
        return DO_WHILE;
    }
}