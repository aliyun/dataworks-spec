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

package com.aliyun.dataworks.common.spec.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2024/3/26
 */
public class VariableUtils {
    public static final Pattern NO_KV_PAIR_PARA_VALUE = Pattern.compile("[\\s^]?-p\\s*\"");

    public static boolean isNoKvPairParaValue(String paraValue) {
        if (StringUtils.isBlank(paraValue)) {
            return false;
        }

        return NO_KV_PAIR_PARA_VALUE.matcher(paraValue).find();
    }

    public static List<SpecVariable> getVariables(String paraValue) {
        if (isNoKvPairParaValue(paraValue)) {
            SpecVariable noKvPairVar = new SpecVariable();
            noKvPairVar.setName("-");
            noKvPairVar.setScope(VariableScopeType.NODE_PARAMETER);
            noKvPairVar.setType(VariableType.NO_KV_PAIR_EXPRESSION);
            noKvPairVar.setValue(paraValue);
            return Collections.singletonList(noKvPairVar);
        }

        AtomicInteger paraIndex = new AtomicInteger(1);
        return Arrays.stream(StringUtils.split(StringUtils.defaultString(paraValue, ""), " ")).map(kvStr -> {
            SpecVariable var = new SpecVariable();
            var.setType(VariableType.SYSTEM);
            var.setScope(VariableScopeType.NODE_PARAMETER);
            String[] kv = StringUtils.split(kvStr, "=");
            if (kv.length == 2) {
                var.setName(kv[0]);
                var.setValue(kv[1]);
                return var;
            } else {
                var.setValue(kvStr);
                var.setName(String.valueOf(paraIndex.getAndIncrement()));
            }
            return var;
        }).collect(Collectors.toList());
    }

}
