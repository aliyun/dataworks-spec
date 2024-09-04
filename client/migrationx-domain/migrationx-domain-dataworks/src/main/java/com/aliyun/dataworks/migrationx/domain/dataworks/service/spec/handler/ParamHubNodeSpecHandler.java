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

import java.util.Optional;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.noref.SpecParamHub;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntity;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2024/6/18
 */
@Slf4j
public class ParamHubNodeSpecHandler extends BasicNodeSpecHandler {
    @Override
    public boolean support(DwNodeEntity nodeEntity) {
        return matchNodeType(nodeEntity, CodeProgramType.PARAM_HUB);
    }

    @Override
    public SpecNode handle(DwNodeEntity orcNode) {
        Preconditions.checkArgument(orcNode != null);
        SpecNode specNode = super.handle(orcNode);
        specNode.setParamHub(buildParamHub(orcNode));
        return specNode;
    }

    private SpecParamHub buildParamHub(DwNodeEntity orcNode) {
        SpecParserContext ctx = new SpecParserContext();
        ctx.setVersion(SpecVersion.V_1_1_0.getLabel());
        return Optional.ofNullable(orcNode.getCode()).filter(StringUtils::isNotBlank)
            .map(code -> Optional.ofNullable(JSON.parseObject(code)).orElse(new JSONObject()))
            .map(code -> (SpecParamHub)SpecDevUtil.getObjectByParser(SpecParamHub.class, code, ctx))
            .map(paramHub -> {
                ListUtils.emptyIfNull(paramHub.getVariables()).stream()
                    .filter(var -> VariableType.PASS_THROUGH.equals(var.getType()))
                    .forEach(var -> var.setValue(Optional.ofNullable(var.getReferenceVariable())
                        .filter(refVar -> refVar.getNode() != null && refVar.getNode().getOutput() != null
                            && StringUtils.isNotBlank(refVar.getNode().getOutput().getData())
                            && StringUtils.isNotBlank(refVar.getName()))
                        .map(refVar -> Joiner.on(":").join(refVar.getNode().getOutput().getData(), refVar.getName()))
                        .orElse(var.getValue())));
                return paramHub;
            }).orElse(new SpecParamHub());
    }
}
