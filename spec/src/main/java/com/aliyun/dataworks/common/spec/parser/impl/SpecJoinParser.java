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
import java.util.Optional;

import com.aliyun.dataworks.common.spec.annotation.SpecParser;
import com.aliyun.dataworks.common.spec.domain.noref.SpecAssertIn;
import com.aliyun.dataworks.common.spec.domain.noref.SpecAssertion;
import com.aliyun.dataworks.common.spec.domain.noref.SpecJoin;
import com.aliyun.dataworks.common.spec.domain.noref.SpecJoinBranch;
import com.aliyun.dataworks.common.spec.domain.noref.SpecLogic;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import org.apache.commons.collections4.MapUtils;

/**
 * @author 聿剑
 * @date 2023/12/16
 */
@SpecParser
public class SpecJoinParser extends DefaultSpecParser<SpecJoin> {
    public static final String KEY_JOIN = "join";
    public static final String KEY_LOGIC = "logic";
    public static final String KEY_BRANCHES = "branches";
    public static final String KEY_BRANCH_NAME = "name";
    public static final String KEY_LOGIC_EXPRESSION = "expression";
    public static final String KEY_BRANCH_ASSERTION = "assertion";
    public static final String KEY_BRANCH_ASSERTION_IN = "in";

    @SuppressWarnings("unchecked")
    @Override
    public SpecJoin parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        SpecJoin specJoin = new SpecJoin();
        // parse logic
        SpecLogic specLogic = new SpecLogic();
        specJoin.setLogic(specLogic);
        specLogic.setExpression((String)MapUtils.emptyIfNull((Map<String, Object>)rawContext.get(KEY_LOGIC))
            .getOrDefault(KEY_LOGIC_EXPRESSION, ""));

        // parse branches
        List<Object> ctxMapBranches = (List<Object>)Optional.ofNullable(rawContext.get(KEY_BRANCHES)).orElse(new ArrayList<>());
        ArrayList<SpecJoinBranch> specJoinBranches = new ArrayList<>();
        for (Object o : ctxMapBranches) {
            Map<String, Object> ctxMapBranch = (Map<String, Object>)o;
            SpecJoinBranch specJoinBranch = new SpecJoinBranch();
            if (!ctxMapBranch.containsKey(KEY_BRANCH_NAME)) {
                throw new SpecException(SpecErrorCode.PARSE_ERROR, "'" + KEY_BRANCH_NAME + "' field of branch is required");
            }

            // parse
            SpecDevUtil.setSimpleField(ctxMapBranch, specJoinBranch);
            SpecDevUtil.setSpecObject(specJoinBranch, "nodeId", ctxMapBranch.get("nodeId"), specParserContext);
            SpecDevUtil.setSpecObject(specJoinBranch, "output", ctxMapBranch.get("output"), specParserContext);
            SpecDevUtil.setSimpleListField(ctxMapBranch, specJoinBranch);

            if (!ctxMapBranch.containsKey(KEY_BRANCH_ASSERTION)) {
                throw new SpecException(SpecErrorCode.PARSE_ERROR, "'" + KEY_BRANCH_ASSERTION + "' field of branch is required");
            }

            Map<String, Object> assertion = (Map<String, Object>)ctxMapBranch.get(KEY_BRANCH_ASSERTION);

            SpecAssertion specAssertion = new SpecAssertion();
            SpecDevUtil.setSimpleField(assertion, specAssertion);
            if (assertion.containsKey(KEY_BRANCH_ASSERTION_IN)) {
                SpecAssertIn in = new SpecAssertIn();
                in.setValue((List<Object>)assertion.get(KEY_BRANCH_ASSERTION_IN));
                specAssertion.setIn(in);
            }

            specJoinBranch.setAssertion(specAssertion);
            specJoinBranches.add(specJoinBranch);
        }
        specJoin.setBranches(specJoinBranches);
        return specJoin;
    }

    @Override
    public String getKeyType() {
        return KEY_JOIN;
    }
}
