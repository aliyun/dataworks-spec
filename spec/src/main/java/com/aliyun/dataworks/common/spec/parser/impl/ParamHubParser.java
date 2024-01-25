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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.annotation.SpecParser;
import com.aliyun.dataworks.common.spec.domain.noref.SpecParamHub;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;

/**
 * ParamHub解析器
 *
 * @author 聿剑
 * @date 2023/10/25
 * @see SpecParamHub
 */
@SpecParser
public class ParamHubParser implements Parser<SpecParamHub> {
    public static final String PARAM_HUB = "param-hub";
    private static final String KEY_TYPE = "type";
    private static final String KEY_SCOPE = "scope";
    private static final String KEY_NAME = "name";
    private static final String KEY_VARIABLES = "variables";

    @SuppressWarnings("unchecked")
    @Override
    public SpecParamHub parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        SpecParamHub paramHub = new SpecParamHub();

        List<Object> variables = (List<Object>)rawContext.get(KEY_VARIABLES);
        Optional.ofNullable(variables).orElseThrow(() -> new SpecException(SpecErrorCode.PARSE_ERROR,
            MessageFormat.format("{0} field of {1} is required", KEY_TYPE, PARAM_HUB)));

        List<SpecVariable> variableList = new ArrayList<>();
        for (Object variable : variables) {
            Object parsed = SpecDevUtil.getObjectByParser(SpecVariable.class, variable, specParserContext);
            variableList.add((SpecVariable)parsed);
        }

        paramHub.setVariables(variableList);
        return paramHub;
    }

    @Override
    public String getKeyType() {
        return PARAM_HUB;
    }
}