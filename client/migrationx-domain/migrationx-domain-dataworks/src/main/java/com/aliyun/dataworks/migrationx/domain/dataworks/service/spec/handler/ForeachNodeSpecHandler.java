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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.adapter.SpecHandlerContext;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.noref.SpecForEach;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntity;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 遍历节点Spec处理器
 *
 * @author 聿剑
 * @date 2023/11/22
 */
@Slf4j
public class ForeachNodeSpecHandler extends BasicNodeSpecHandler {
    public static final String TRAVERSE_INPUT_VARIABLE_NAME = "loopDataArray";

    @Override
    public boolean support(DwNodeEntity dwNode) {
        return matchNodeType(dwNode, CodeProgramType.CONTROLLER_TRAVERSE);
    }

    @Override
    public SpecNode handle(DwNodeEntity orcNode) {
        Preconditions.checkNotNull(orcNode, "node is null");
        SpecNode specNode = super.handle(orcNode);
        if (support(orcNode)) {
            specNode.setForeach(buildForEach(orcNode, specNode, context));
        }
        return specNode;
    }

    private SpecForEach buildForEach(DwNodeEntity orcNode, SpecNode specNode, SpecHandlerContext context) {
        List<DwNodeEntity> innerNodes = getInnerNodes(orcNode);
        SpecForEach specForEach = new SpecForEach();
        specForEach.setNodes(innerNodes.stream()
                .map(n -> getSpecAdapter().getHandler(n, context.getLocale()).handle(n))
                .collect(Collectors.toList()));

        specForEach.setFlow(innerNodes.stream().map(node -> getSpecAdapter().toFlow(this, node, context)).flatMap(List::stream)
                .collect(Collectors.toList()));
        ListUtils.emptyIfNull(Optional.ofNullable(specNode.getScript()).map(SpecScript::getParameters).orElse(null)).stream()
                .filter(Objects::nonNull)
                .filter(in -> StringUtils.equals(TRAVERSE_INPUT_VARIABLE_NAME, in.getName())).findFirst().ifPresent(in -> {
                    specForEach.setArray(in);
                    specNode.getInputs().remove(in);
                });
        return specForEach;
    }
}
