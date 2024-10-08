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

import java.util.List;
import java.util.Map;

import com.aliyun.dataworks.common.spec.domain.Spec;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2023/11/16
 */
@com.aliyun.dataworks.common.spec.annotation.SpecParser
public class SpecParser<T extends Spec> extends DefaultSpecParser<T> {
    public boolean support(String kind) {
        T t = instantiateSpecObject();
        return matchKinds(t.getKinds(), kind);
    }

    protected boolean matchKinds(List<SpecKind> kinds, String kind) {
        return ListUtils.emptyIfNull(kinds).stream().anyMatch(k -> StringUtils.equalsIgnoreCase(k.getLabel(), kind));
    }

    @Override
    public T parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        T specObj = instantiateSpecObject();
        parseSpecObjectFields(specObj, specParserContext.getContextMap(), specParserContext);
        return specObj;
    }
}
