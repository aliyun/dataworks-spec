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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.annotation.SpecParser;
import com.aliyun.dataworks.common.spec.domain.enums.ArtifactType;
import com.aliyun.dataworks.common.spec.domain.interfaces.NodeIO;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecArtifact;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.MapKeyMatchUtils;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

/**
 * @author yiwei.qyw
 * @date 2023/7/6
 */
@SpecParser
public class NodeParser implements Parser<SpecNode> {
    @SuppressWarnings("unchecked")
    @Override
    public SpecNode parse(Map<String, Object> ctxMap, SpecParserContext specParserContext) {
        SpecNode specNode = new SpecNode();

        SpecDevUtil.setSameKeyField(ctxMap, specNode, specParserContext);
        SpecDevUtil.setSpecObject(specNode, "doWhile", ctxMap.get(DoWhileParser.DO_WHILE), specParserContext);
        SpecDevUtil.setSpecObject(specNode, "foreach", ctxMap.get(SpecForEachParser.FOREACH), specParserContext);
        SpecDevUtil.setSpecObject(specNode, "paramHub", ctxMap.get(ParamHubParser.PARAM_HUB), specParserContext);

        specNode.setInputs(parseInputOutputs(specParserContext, (Map<String, Object>)ctxMap.get("inputs")));
        specNode.setOutputs(parseInputOutputs(specParserContext, (Map<String, Object>)ctxMap.get("outputs")));
        ListUtils.emptyIfNull(specNode.getOutputs()).stream()
            .filter(out -> out instanceof SpecVariable)
            .map(out -> (SpecVariable)out)
            .forEach(out -> {
                SpecDepend node = new SpecDepend();
                node.setNodeId(specNode);
                SpecNodeOutput specOut = new SpecNodeOutput();
                specOut.setData(node.getNodeId().getId());
                node.setOutput(specOut);
                out.setNode(node);
            });
        return specNode;
    }

    @Override
    public String getKeyType() {return "node";}

    @SuppressWarnings("unchecked")
    private static <T extends NodeIO> ArrayList<T> parseInputOutputs(SpecParserContext contextMeta, Map<String, Object> ioCtxMap) {
        ArrayList<T> ioList = new ArrayList<>();
        Reflections reflections = new Reflections(SpecArtifact.class.getPackage().getName());
        Set<Class<? extends SpecArtifact>> artifactClzTypes = reflections.getSubTypesOf(SpecArtifact.class);
        SetUtils.emptyIfNull(artifactClzTypes).forEach(artifactClzType -> {
            try {
                SpecArtifact ins = artifactClzType.getDeclaredConstructor().newInstance();
                List<Object> artifacts = getArtifacts(ioCtxMap, ins.getArtifactType());
                for (Object artifact : artifacts) {
                    // artifact is Map
                    if (artifact instanceof Map) {
                        T parse = (T)SpecDevUtil.getObjectByParser(artifactClzType, artifact, contextMeta);
                        ioList.add(parse);
                        continue;
                    }
                    // artifact is String
                    SpecDevUtil.setRefContext(ioList, SpecDevUtil.parseRefId((String)artifact).getId(), contextMeta,
                        null, artifactClzType.getSimpleName());
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
        return ioList;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<Object> getArtifacts(Map<String, Object> map, ArtifactType... typeFilters) {
        List<ArtifactType> types = Arrays.stream(ArtifactType.values())
            .filter(type -> typeFilters == null || Arrays.asList(typeFilters).contains(type))
            .collect(Collectors.toList());

        return ListUtils.emptyIfNull(types).stream()
            .map(type -> MapUtils.emptyIfNull(map).entrySet().stream()
                .filter(ent -> matchArtifactType(type, ent.getKey()))
                .map(Map.Entry::getValue)
                .map(v -> (List<Object>)v)
                .flatMap(List::stream)
                .peek(obj -> {
                    if (obj instanceof HashMap) {
                        if (!((HashMap)obj).containsKey("type")) {
                            ((HashMap)obj).put("type", type);
                        }
                    }
                })
                .collect(Collectors.toList()))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private static boolean matchArtifactType(ArtifactType type, String key) {
        if (type == null) {
            return false;
        }

        if (StringUtils.isBlank(key)) {
            return false;
        }

        if (type == ArtifactType.NODE_OUTPUT) {
            return MapKeyMatchUtils.matchIgnoreSinglePluralForm(key, type.getLabel()) ||
                MapKeyMatchUtils.matchIgnoreSinglePluralForm(key, "output");
        }
        return MapKeyMatchUtils.matchIgnoreSinglePluralForm(key, type.getLabel());
    }
}