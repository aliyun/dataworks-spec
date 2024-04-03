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

import java.util.Arrays;
import java.util.Map;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.SpecConstants;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.parser.SpecParserFactory;

/**
 * @author 聿剑
 * @date 2023/11/16
 */
public class DataWorksWorkflowSpecParser extends SpecParser<DataWorksWorkflowSpec> {
    @Override
    public boolean support(String kind) {
        return Arrays.stream(SpecKind.values()).map(SpecKind::getLabel).anyMatch(k -> k.equalsIgnoreCase(kind));
    }

    @Override
    public DataWorksWorkflowSpec parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        DataWorksWorkflowSpec specObj = instantiateSpecObject();
        specParserContext.setIgnoreMissingFields(true);
        parseSpecObjectFields(specObj, rawContext, specParserContext);
        return specObj;
    }

    @Override
    protected Parser<?> getCustomParser(String jsonKey) {
        if (SpecConstants.SPEC_KEY_ARTIFACTS.equals(jsonKey)) {
            return SpecParserFactory.getParser(ArtifactListParser.KEY_TYPE);
        }

        if (SpecConstants.SPEC_KEY_FILES.equals(jsonKey)) {
            return SpecParserFactory.getParser(FileListParser.KEY_TYPE);
        }

        return super.getCustomParser(jsonKey);
    }
}
