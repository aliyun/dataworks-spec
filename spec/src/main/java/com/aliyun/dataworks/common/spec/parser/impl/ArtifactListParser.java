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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aliyun.dataworks.common.spec.annotation.SpecParser;
import com.aliyun.dataworks.common.spec.domain.enums.ArtifactType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecArtifact;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.MapKeyMatchUtils;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author yiwei
 */
@SpecParser
public class ArtifactListParser implements Parser<List<SpecArtifact>> {
    public static final String KEY_TYPE = "artifactsList";

    @SuppressWarnings("unchecked")
    @Override
    public List<SpecArtifact> parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        ArrayList<SpecArtifact> specArtifacts = new ArrayList<>();
        List<Map<String, Object>> tables = (List<Map<String, Object>>)MapKeyMatchUtils.getIgnoreCaseSingleAndPluralForm(rawContext,
            ArtifactType.TABLE.getLabel());
        if (CollectionUtils.isNotEmpty(tables)) {
            for (Map<String, Object> table : tables) {
                table.put("type", ArtifactType.TABLE.getLabel());
                SpecTable specArtifact = (SpecTable)SpecDevUtil.getObjectByParser(SpecTable.class, table, specParserContext);
                SpecDevUtil.setEntityToCtx(specArtifact, specParserContext);
                specArtifacts.add(specArtifact);
            }
        }

        List<Map<String, Object>> outputs = (List<Map<String, Object>>)MapKeyMatchUtils.getIgnoreCaseSingleAndPluralForm(rawContext,
            "output", ArtifactType.NODE_OUTPUT.getLabel());
        if (CollectionUtils.isNotEmpty(outputs)) {
            for (Map<String, Object> output : outputs) {
                output.put("type", ArtifactType.NODE_OUTPUT.getLabel());
                SpecNodeOutput specArtifact = (SpecNodeOutput)SpecDevUtil.getObjectByParser(SpecNodeOutput.class, output, specParserContext);
                SpecDevUtil.setEntityToCtx(specArtifact, specParserContext);
                specArtifacts.add(specArtifact);
            }
        }

        List<Map<String, Object>> variables = (List<Map<String, Object>>)MapKeyMatchUtils.getIgnoreCaseSingleAndPluralForm(rawContext,
            ArtifactType.VARIABLE.getLabel());
        if (CollectionUtils.isNotEmpty(variables)) {
            for (Map<String, Object> output : variables) {
                output.put("type", ArtifactType.VARIABLE.getLabel());
                SpecVariable specArtifact = (SpecVariable)SpecDevUtil.getObjectByParser(SpecVariable.class, output, specParserContext);
                SpecDevUtil.setEntityToCtx(specArtifact, specParserContext);
                specArtifacts.add(specArtifact);
            }
        }
        return specArtifacts;
    }

    @Override
    public String getKeyType() {
        return KEY_TYPE;
    }
}
