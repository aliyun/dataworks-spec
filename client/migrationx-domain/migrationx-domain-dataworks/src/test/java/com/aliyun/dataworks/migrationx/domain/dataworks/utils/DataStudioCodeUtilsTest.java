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

package com.aliyun.dataworks.migrationx.domain.dataworks.utils;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author 聿剑
 * @date 2022/12/28
 */
@Slf4j
public class DataStudioCodeUtilsTest {
    @Test
    public void testParseResourceReferences() {
        String code = "--@resource_reference{\"test_res.jar\"}"
            + "--@resource_reference{\"test_res.txt\"}";
        List<String> resList = DataStudioCodeUtils.parseResourceReference(code);
        Assert.assertNotNull(resList);
        Assert.assertTrue(resList.contains("test_res.jar"));
        Assert.assertTrue(resList.contains("test_res.txt"));

        Assert.assertTrue(CollectionUtils.isEmpty(DataStudioCodeUtils.parseResourceReference("")));
    }

    @Test
    public void testAddResourceReferences() {
        String code = "echo 123";

        String newCode = DataStudioCodeUtils.addResourceReference(
            CodeProgramType.CDH_SHELL, code, Arrays.asList("res1.jar", "res1.txt"));
        log.info("new code: \n{}", newCode);
        Assert.assertTrue(StringUtils.indexOf(newCode, "##@resource_reference{\"res1.jar\"}\n") >= 0);
        Assert.assertTrue(StringUtils.indexOf(newCode, "##@resource_reference{\"res1.txt\"}\n") >= 0);
        Assert.assertTrue(StringUtils.indexOf(newCode, code) >= 0);

        code = "select 1;";
        newCode = DataStudioCodeUtils.addResourceReference(
            CodeProgramType.ODPS_SQL, code, Arrays.asList("res1.jar", "res1.txt"));
        log.info("new code: \n{}", newCode);
        Assert.assertTrue(StringUtils.indexOf(newCode, "--@resource_reference{\"res1.jar\"}\n") >= 0);
        Assert.assertTrue(StringUtils.indexOf(newCode, "--@resource_reference{\"res1.txt\"}\n") >= 0);
        Assert.assertTrue(StringUtils.indexOf(newCode, code) >= 0);
    }
}
