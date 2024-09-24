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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.adapter.SpecHandlerContext;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ControllerJoinCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ControllerJoinCode.Branch;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.noref.SpecAssertIn;
import com.aliyun.dataworks.common.spec.domain.noref.SpecAssertion;
import com.aliyun.dataworks.common.spec.domain.noref.SpecJoin;
import com.aliyun.dataworks.common.spec.domain.noref.SpecJoinBranch;
import com.aliyun.dataworks.common.spec.domain.noref.SpecLogic;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntity;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

/**
 * 归并节点Spec处理器
 *
 * @author 云异
 * @date 2024/03/18
 */
@Slf4j
public class JoinNodeSpecHandler extends BasicNodeSpecHandler {
    public static final Integer LOGIC_OR = 0;
    public static final Integer LOGIC_AND = 1;
    public static final String SPEC_JOIN_BRANCH_NAME_PREFIX = "b_";
    public static final String SPEC_JOIN_LOGIC_OPERATOR_AND = "and";
    public static final String SPEC_JOIN_LOGIC_OPERATOR_OR = "or";

    @Override
    public boolean support(DwNodeEntity dwNode) {
        return matchNodeType(dwNode, CodeProgramType.CONTROLLER_JOIN);
    }

    @Override
    public SpecNode handle(DwNodeEntity dmNodeBo) {
        Preconditions.checkNotNull(dmNodeBo, "node is null");
        SpecNode specNode = super.handle(dmNodeBo);
        specNode.setJoin(buildJoin(dmNodeBo, context));
        return specNode;
    }

    private SpecJoin buildJoin(DwNodeEntity getNodeSpecBO, SpecHandlerContext dwContext) {
        SpecJoin specJoin = new SpecJoin();

        CodeModel<ControllerJoinCode> codeModel
                = CodeModelFactory.getCodeModel(CodeProgramType.CONTROLLER_JOIN.name(), getNodeSpecBO.getCode());
        List<Branch> branches = ListUtils.emptyIfNull(Optional.ofNullable(codeModel.getCodeModel())
                .map(ControllerJoinCode::getBranchList).orElse(null));

        AtomicInteger branchIndex = new AtomicInteger(0);
        AtomicReference<String> logicExpress = new AtomicReference<>("");
        specJoin.setBranches(ListUtils.emptyIfNull(branches).stream().map(branch -> {
            Integer index = branchIndex.incrementAndGet();
            SpecJoinBranch specJoinBranch = new SpecJoinBranch();
            specJoinBranch.setName(SPEC_JOIN_BRANCH_NAME_PREFIX + index);

            SpecNodeOutput output = new SpecNodeOutput();
            output.setData(branch.getNode());
            specJoinBranch.setOutput(output);

            SpecAssertion assertion = new SpecAssertion();
            assertion.setField("status");
            SpecAssertIn in = new SpecAssertIn();
            in.setValue(new ArrayList<>(ListUtils.emptyIfNull(branch.getRunStatus())));
            assertion.setIn(in);
            specJoinBranch.setAssertion(assertion);

            if (index.equals(1)) {
                logicExpress.set(specJoinBranch.getName());
            } else {
                String op = LOGIC_AND.equals(branch.getLogic()) ? SPEC_JOIN_LOGIC_OPERATOR_AND : SPEC_JOIN_LOGIC_OPERATOR_OR;
                logicExpress.set(Joiner.on(" ").join(logicExpress.get(), op, specJoinBranch.getName()));
            }
            return specJoinBranch;
        }).collect(Collectors.toList()));
        log.info("logic expression: {}", logicExpress.get());
        SpecLogic logic = new SpecLogic();
        logic.setExpression(logicExpress.get());
        specJoin.setLogic(logic);
        specJoin.setResultStatus(Optional.ofNullable(codeModel.getCodeModel()).map(ControllerJoinCode::getResultStatus).orElse(null));
        return specJoin;
    }
}
