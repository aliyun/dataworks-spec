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
import java.util.Optional;

import com.alibaba.fastjson2.JSON;

import com.aliyun.dataworks.common.spec.annotation.SpecParser;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.interfaces.LabelEnum;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Variable解析器
 *
 * @author 聿剑
 * @date 2023/10/25
 * @see SpecVariable
 */
@SpecParser
public class SpecVariableParser implements Parser<SpecVariable> {
    private static final String KEY_TYPE = "type";
    private static final String KEY_SCOPE = "scope";
    private static final String KEY_NAME = "name";
    private static final String KEY_NODE = "node";
    private static final String KEY_DESC = "description";
    private static final String KEY_REFERENCE_VARIABLE = "referenceVariable";
    private static final String KEY_VALUE = "value";
    private static final String KEY_ID = "id";

    @Override
    public SpecVariable parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        return parseVariable(specParserContext, rawContext);
    }

    private SpecVariable parseVariable(SpecParserContext contextMeta, Map<String, Object> variableMap) {
        String type = (String)variableMap.get(KEY_TYPE);
        if (StringUtils.isBlank(type)) {
            throw new SpecException(SpecErrorCode.PARSE_ERROR, MessageFormat.format("{0} field of variable is required, source: {1}", KEY_TYPE,
                JSON.toJSONString(variableMap)));
        }

        String id = (String)variableMap.get(KEY_ID);
        SpecVariable variable = new SpecVariable();
        variable.setId(id);
        parseName(variableMap, variable);
        parseScope(variableMap, variable);
        variable.setType(Optional.ofNullable((VariableType)LabelEnum.getByLabel(VariableType.class, type))
            .orElseThrow(() -> new SpecException(SpecErrorCode.PARSE_ERROR, MessageFormat.format("invalid variable type {0}", type))));
        variable.setValue((String)variableMap.get(KEY_VALUE));
        variable.setDescription((String)variableMap.get(KEY_DESC));
        SpecDevUtil.setSpecObject(variable, KEY_NODE, variableMap.get(KEY_NODE), contextMeta);
        SpecDevUtil.setSpecObject(variable, KEY_REFERENCE_VARIABLE, variableMap.get(KEY_REFERENCE_VARIABLE), contextMeta);
        return variable;
    }

    private static void parseName(Map<String, Object> variableMap, SpecVariable variable) {
        String name = StringUtils.defaultString((String)variableMap.get(KEY_NAME), "unnamed");
        variable.setName(name);
    }

    private static void parseScope(Map<String, Object> variableMap, SpecVariable variable) {
        String scope = (String)variableMap.get(KEY_SCOPE);
        if (StringUtils.isBlank(scope)) {
            throw new SpecException(SpecErrorCode.PARSE_ERROR, MessageFormat.format("{0} field of variable is required", KEY_SCOPE));
        }

        VariableScopeType scopeType = LabelEnum.getByLabel(VariableScopeType.class, scope);
        variable.setScope(Optional.ofNullable(scopeType).orElseThrow(() -> new SpecException(SpecErrorCode.PARSE_ERROR,
            MessageFormat.format("invalid value {0} for field {1} of variable", scope, KEY_SCOPE))));
    }
}