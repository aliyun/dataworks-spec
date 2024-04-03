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

import java.util.List;

import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/3/27
 */
public class VariableUtilsTest {
    @Test
    public void testNoKvPairParaValue() {
        String paraValue = " -p\"-Dbizdate=$bizdate -Denv_path=$env_path -Dhour=$hour -Dendtime=$[yyyymmdd hh24] -Dbegintime=$[yyyymmdd hh24 - 1/24] "
            + "-Dgmtdate=$gmtdate\"";
        Assert.assertTrue(VariableUtils.isNoKvPairParaValue(paraValue));

        List<SpecVariable> specVars = VariableUtils.getVariables(paraValue);
        Assert.assertNotNull(specVars);
        Assert.assertEquals(1, CollectionUtils.size(specVars));
        Assert.assertEquals(VariableType.NO_KV_PAIR_EXPRESSION, specVars.get(0).getType());
    }
}
