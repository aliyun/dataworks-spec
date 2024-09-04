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

package com.aliyun.dataworks.common.spec.domain.dw.codemodel;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/6/6
 */
@Slf4j
public class SqlComponentCodeTest {
    @Test
    public void test() {
        String content
            = "{\"code\":\"select '@@{p1}', '@@{p2}', '@@{p3}' \\n\\n;\",\"config\":{\"input\":[{\"name\":\"p1\",\"type\":\"string\"},"
            + "{\"name\":\"p2\",\"type\":\"string\"},{\"name\":\"p3\",\"type\":\"string\"}],\"output\":[{\"name\":\"p2\",\"type\":\"string\"}],"
            + "\"name\":\"my_com\",\"owner\":\"聿剑\"}}";
        CodeModel<Code> cm
            = CodeModelFactory.getCodeModel(CodeProgramType.SQL_COMPONENT.getName(), content);
        log.info("content: {}", cm.getCodeModel().getContent());
        log.info("source code: {}", cm.getCodeModel().getSourceCode());
        Assert.assertNotNull(cm);
        Assert.assertNotNull(cm.getCodeModel());
        Assert.assertEquals(CodeProgramType.SQL_COMPONENT.name(), cm.getProgramType());
        Assert.assertTrue(StringUtils.startsWith(cm.getSourceCode(), "select '@@{p1}', '@@{p2}', '@@{p3}'"));

        SqlComponentCode sqlCom = (SqlComponentCode)cm.getCodeModel();
        log.info("com: {}", sqlCom);
        Assert.assertNotNull(sqlCom.getConfig());
        Assert.assertEquals(3, sqlCom.getConfig().getInputs().size());
        Assert.assertEquals(1, sqlCom.getConfig().getOutputs().size());
        Assert.assertEquals("聿剑", sqlCom.getConfig().getOwner());
        Assert.assertEquals("my_com", sqlCom.getConfig().getName());
    }
}
