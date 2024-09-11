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
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

/**
 * @author 聿剑
 * @date 2024/7/9
 */
@SpecParser
public class SpecWorkflowParser implements Parser<SpecWorkflow> {
    @Override
    public SpecWorkflow parse(Map<String, Object> ctxMap, SpecParserContext specParserContext) {
        SpecWorkflow specWorkflow = new SpecWorkflow();

        SpecDevUtil.setSameKeyField(ctxMap, specWorkflow, specParserContext);
        if (CollectionUtils.isEmpty(specWorkflow.getDependencies())) {
            SpecDevUtil.setSpecObject(specWorkflow, "dependencies", ctxMap.get("flow"), specParserContext);
        }

        //noinspection unchecked
        specWorkflow.setInputs(SpecNodeParser.parseInputOutputs(specParserContext, (Map<String, Object>)ctxMap.get("inputs")));
        //noinspection unchecked
        specWorkflow.setOutputs(SpecNodeParser.parseInputOutputs(specParserContext, (Map<String, Object>)ctxMap.get("outputs")));
        ListUtils.emptyIfNull(specWorkflow.getOutputs()).stream()
            .filter(out -> out instanceof SpecVariable)
            .map(out -> (SpecVariable)out)
            .forEach(out -> {
                SpecDepend node = new SpecDepend();
                SpecNode nodeId = new SpecNode();
                nodeId.setId(specWorkflow.getId());
                node.setNodeId(nodeId);
                SpecNodeOutput specOut = new SpecNodeOutput();
                specOut.setData(node.getNodeId().getId());
                node.setOutput(specOut);
                out.setNode(node);
            });
        return specWorkflow;
    }
}