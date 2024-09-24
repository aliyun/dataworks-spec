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

package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.handler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.adapter.SpecHandlerContext;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDoWhile;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntity;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 循环节点Spec处理器
 *
 * @author 聿剑
 * @date 2023/11/22
 */
@Slf4j
public class DowhileNodeSpecHandler extends BasicNodeSpecHandler {
    @Override
    public boolean support(DwNodeEntity node) {
        return matchNodeType(node, CodeProgramType.CONTROLLER_CYCLE);
    }

    @Override
    public SpecNode handle(DwNodeEntity dmNodeBo) {
        Preconditions.checkNotNull(dmNodeBo, "node is null");
        SpecNode specNode = super.handle(dmNodeBo);

        if (support(dmNodeBo)) {
            specNode.setDoWhile(buildDoWhile(dmNodeBo, context));
        }
        return specNode;
    }

    private SpecDoWhile buildDoWhile(DwNodeEntity orcNode, SpecHandlerContext context) {
        List<DwNodeEntity> innerNodes = getInnerNodes(orcNode);
        SpecDoWhile specDoWhile = new SpecDoWhile();
        specDoWhile.setNodes(innerNodes.stream()
                .map(n -> getSpecAdapter().getHandler(n, context.getLocale()).handle(n))
                .filter(n -> !StringUtils.equalsIgnoreCase(
                        CodeProgramType.CONTROLLER_CYCLE_END.name(),
                        Optional.ofNullable(n).map(SpecNode::getScript).map(SpecScript::getRuntime).map(SpecScriptRuntime::getCommand).orElse(null)))
                .collect(Collectors.toList()));
        specDoWhile.setSpecWhile(innerNodes.stream()
                .map(n -> getSpecAdapter().getHandler(n, context.getLocale()).handle(n))
                .filter(n -> StringUtils.equalsIgnoreCase(
                        CodeProgramType.CONTROLLER_CYCLE_END.name(),
                        Optional.ofNullable(n).map(SpecNode::getScript).map(SpecScript::getRuntime).map(SpecScriptRuntime::getCommand).orElse(null)))
                .findFirst().orElse(null));
        specDoWhile.setFlow(innerNodes.stream()
                .map(node -> getSpecAdapter().toFlow(this, node, context)).flatMap(List::stream)
                .collect(Collectors.toList()));
        return specDoWhile;
    }
}