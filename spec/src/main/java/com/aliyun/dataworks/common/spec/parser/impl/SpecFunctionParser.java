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
import com.aliyun.dataworks.common.spec.domain.SpecConstants;
import com.aliyun.dataworks.common.spec.domain.enums.FunctionType;
import com.aliyun.dataworks.common.spec.domain.interfaces.LabelEnum;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFunction;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import org.apache.commons.collections4.MapUtils;

/**
 * SpecFunctionParser
 *
 * @author 聿剑
 * @date 2023/11/30
 * @see SpecFunctionParser
 */
@SpecParser
public class SpecFunctionParser extends DefaultSpecParser<SpecFunction> {
    public SpecFunctionParser() {
        super(SpecFunction.class);
    }

    @Override
    public SpecFunction parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        SpecFunction func = super.parse(rawContext, specParserContext);
        String typeStr = MapUtils.getString(rawContext, SpecConstants.SPEC_KEY_TYPE);
        func.setType(LabelEnum.getByLabel(FunctionType.class, typeStr));
        return func;
    }
}
