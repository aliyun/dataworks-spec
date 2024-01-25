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

import java.text.MessageFormat;
import java.util.Map;

import com.aliyun.dataworks.common.spec.annotation.SpecParser;
import com.aliyun.dataworks.common.spec.domain.noref.SpecNodeRef;
import com.aliyun.dataworks.common.spec.domain.noref.SpecParamHub;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import org.apache.commons.lang3.StringUtils;

/**
 * ParamHub解析器
 *
 * @author 聿剑
 * @date 2023/10/25
 * @see SpecParamHub
 */
@SpecParser
public class SpecNodeRefParser implements Parser<SpecNodeRef> {
    @Override
    public SpecNodeRef parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        return parseSpecNodeRef(rawContext);
    }

    private SpecNodeRef parseSpecNodeRef(Map<String, Object> variableMap) {
        String output = (String)variableMap.get("output");
        if (StringUtils.isBlank(output)) {
            throw new SpecException(SpecErrorCode.PARSE_ERROR, MessageFormat.format("{0} field is required", "output"));
        }

        SpecNodeRef variable = new SpecNodeRef();
        variable.setOutput(output);
        return variable;
    }
}