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
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;

/**
 * @author 聿剑
 * @date 2024/3/5
 */
@SpecParser
public class SpecDependParser extends DefaultSpecParser<SpecDepend> {
    @Override
    public SpecDepend parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        SpecDepend specDepend = super.parse(rawContext, specParserContext);
        Optional.ofNullable(rawContext.get("nodeId")).map(id -> (String)id).ifPresent(nodeId -> {
            SpecNode specNode = new SpecNode();
            specNode.setId((String)nodeId);
            specDepend.setNodeId(specNode);
        });
        Optional.ofNullable(rawContext.get("output")).map(o -> (String)o).ifPresent(output -> {
            SpecNodeOutput out = new SpecNodeOutput();
            out.setData(output);
            specDepend.setOutput(out);
        });
        return specDepend;
    }
}
