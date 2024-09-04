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
public class ComponentSqlCodeTest {
    @Test
    public void test() {
        String content
            = "{\"code\":\"select '@@{p1}', '@@{p2}', '@@{p3}' \\n\\n;\",\"config\":{\"input\":[{\"name\":\"p1\",\"type\":\"string\","
            + "\"value\":\"111111\"},{\"name\":\"p2\",\"type\":\"string\",\"value\":\"222222\"},{\"name\":\"p3\",\"type\":\"string\"}],"
            + "\"output\":[{\"name\":\"p2\",\"type\":\"string\",\"value\":\"222222\"}]},\"component\":{\"id\":30407036,\"name\":\"my_com\","
            + "\"version\":4}}";
        CodeModel<Code> cm
            = CodeModelFactory.getCodeModel(CodeProgramType.COMPONENT_SQL.getName(), content);
        log.info("content: {}", cm.getCodeModel().getContent());
        log.info("source code: {}", cm.getCodeModel().getSourceCode());
        Assert.assertNotNull(cm);
        Assert.assertNotNull(cm.getCodeModel());
        Assert.assertEquals(CodeProgramType.COMPONENT_SQL.name(), cm.getProgramType());
        Assert.assertTrue(StringUtils.startsWith(cm.getSourceCode(), "select '111111', '222222', '@@{p3}'"));
        Assert.assertTrue(StringUtils.indexOf(cm.getContent(), "\"input\":") > 0);
        Assert.assertTrue(StringUtils.indexOf(cm.getContent(), "\"output\":") > 0);
        /*
         * compatible case
         * @see com.aliyun.dataworks.common.spec.domain.dw.codemodel.SqlComponentCode.ComponentAdapter
         */
        Assert.assertTrue(StringUtils.indexOf(cm.getContent(), "\"inputs\":") < 0);
        Assert.assertTrue(StringUtils.indexOf(cm.getContent(), "\"outputs\":") < 0);

        ComponentSqlCode sqlCom = (ComponentSqlCode)cm.getCodeModel();
        log.info("com: {}", sqlCom);
        Assert.assertNotNull(sqlCom.getConfig());
        Assert.assertEquals(3, sqlCom.getConfig().getInputs().size());
        Assert.assertEquals("p1", sqlCom.getConfig().getInputs().get(0).getName());
        Assert.assertEquals("111111", sqlCom.getConfig().getInputs().get(0).getValue());
        Assert.assertEquals("string", sqlCom.getConfig().getInputs().get(0).getType());

        Assert.assertEquals(1, sqlCom.getConfig().getOutputs().size());
        Assert.assertNotNull(sqlCom.getComponent());
        Assert.assertEquals(30407036L, (long)sqlCom.getComponent().getId());
        Assert.assertEquals("my_com", sqlCom.getComponent().getName());
        Assert.assertEquals(4, (int)sqlCom.getComponent().getVersion());
    }
}
