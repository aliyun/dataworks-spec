/*
 * Copyright (c) 2025, Alibaba Cloud;
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

import java.util.List;
import java.util.Map;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author 莫泣
 * @date 2025-10-27
 */
@Slf4j
public class RealIntegrationCodeTest {

    @Test
    public void testParseAndGetContent() {
        // 测试数据
        String jsonString = "{\"riKey1\":\"riValue1\",\"riKey2\":789,\"riKey3\":{\"riNestedKey\":\"riNestedValue\"}}";

        // 创建RealIntegrationCode实例
        RealIntegrationCode code = new RealIntegrationCode();

        // 测试parse方法
        RealIntegrationCode parsedCode = code.parse(jsonString);
        assertNotNull(parsedCode);
        assertSame(code, parsedCode);

        // 测试getContent方法
        String content = code.getContent();
        log.info("content: {}", content);
        assertNotNull(content);
        assertTrue(StringUtils.indexOf(content, "\"riKey1\":\"riValue1\"") > 0);
        assertTrue(StringUtils.indexOf(content, "\"riKey2\":789") > 0);
        assertTrue(StringUtils.indexOf(content, "\"riNestedKey\":\"riNestedValue\"") > 0);
    }

    @Test
    public void testGetProgramTypes() {
        RealIntegrationCode code = new RealIntegrationCode();
        List<String> programTypes = code.getProgramTypes();
        assertNotNull(programTypes);
        assertEquals(1, programTypes.size());
        assertEquals(CodeProgramType.RI.name(), programTypes.get(0));
    }

    @Test
    public void testGetTemplate() {
        RealIntegrationCode code = new RealIntegrationCode();
        Map<String, Object> template = code.getTemplate();
        assertNull(template);
    }

    @Test
    public void testParseWithNull() {
        RealIntegrationCode code = new RealIntegrationCode();
        RealIntegrationCode parsedCode = code.parse(null);
        assertNotNull(parsedCode);
        assertNull(parsedCode.getContent());
    }

    @Test
    public void testParseWithEmptyString() {
        RealIntegrationCode code = new RealIntegrationCode();
        RealIntegrationCode parsedCode = code.parse("");
        assertNotNull(parsedCode);
        assertNull(parsedCode.getContent());
    }

    @Test
    public void testParseWithInvalidJson() {
        RealIntegrationCode code = new RealIntegrationCode();
        // 解析无效JSON时会抛出异常，但我们检查getContent的行为
        try {
            RealIntegrationCode parsedCode = code.parse("{invalid json}");
            assertNotNull(parsedCode);
        } catch (Exception e) {
            // 异常是预期的，我们检查getContent的返回值
        }
        // getContent应该返回null，因为jsonObject为null
        assertNull(code.getContent());
    }

    @Test
    public void testParseWithEmptyJsonObject() {
        RealIntegrationCode code = new RealIntegrationCode();
        RealIntegrationCode parsedCode = code.parse("{}");
        assertNotNull(parsedCode);
        assertEquals("{}", parsedCode.getContent());
    }

    @Test
    public void testParseWithComplexJson() {
        String jsonString = "{"
            + "\"stringField\":\"stringValue\","
            + "\"numberField\":123,"
            + "\"booleanField\":true,"
            + "\"arrayField\":[\"item1\",\"item2\"],"
            + "\"objectField\":{"
            + "\"nestedString\":\"nestedValue\","
            + "\"nestedNumber\":456"
            + "}"
            + "}";

        RealIntegrationCode code = new RealIntegrationCode();
        RealIntegrationCode parsedCode = code.parse(jsonString);
        assertNotNull(parsedCode);
        assertSame(code, parsedCode);

        String content = code.getContent();
        log.info("complex content: {}", content);
        assertNotNull(content);
        assertTrue(content.contains("\"stringField\":\"stringValue\""));
        assertTrue(content.contains("\"numberField\":123"));
        assertTrue(content.contains("\"booleanField\":true"));
        assertTrue(content.contains("\"arrayField\":[\"item1\",\"item2\"]"));
        assertTrue(content.contains("\"nestedString\":\"nestedValue\""));
        assertTrue(content.contains("\"nestedNumber\":456"));
    }

    @Test
    public void testGetContentWithNullJsonObject() {
        RealIntegrationCode code = new RealIntegrationCode();
        // jsonObject字段为null的情况
        assertNull(code.getContent());
    }

    @Test
    public void testSupportMethod() {
        RealIntegrationCode code = new RealIntegrationCode();
        assertTrue(code.support(CodeProgramType.RI.name()));
        assertTrue(code.support("ri"));
        assertTrue(code.support("RI"));
        assertFalse(code.support("OTHER"));
        assertFalse(code.support(""));
        assertFalse(code.support(null));
    }

    @Test
    public void testGetRawContent() {
        String jsonString = "{\"testKey\":\"testValue\"}";
        RealIntegrationCode code = new RealIntegrationCode();
        code.parse(jsonString);

        String rawContent = code.getRawContent();
        String content = code.getContent();
        assertEquals(content, rawContent);
    }

    @Test
    public void testSetSourceCode() {
        RealIntegrationCode code = new RealIntegrationCode();
        // setSourceCode方法在RealIntegrationCode中是空实现，测试确保不会抛出异常
        code.setSourceCode("test source code");
        // 不应该影响getContent的结果
        assertNull(code.getContent());
    }

    @Test
    public void testGetSourceCode() {
        String jsonString = "{\"testKey\":\"testValue\"}";
        RealIntegrationCode code = new RealIntegrationCode();
        code.parse(jsonString);

        String sourceCode = code.getSourceCode();
        String content = code.getContent();
        assertEquals(content, sourceCode);
    }

    @Test
    public void testSetProgramType() {
        RealIntegrationCode code = new RealIntegrationCode();
        code.setProgramType("TEST_TYPE");
        // 测试确保方法可以被调用，不会抛出异常
    }

    @Test
    public void testGetResourceReferences() {
        RealIntegrationCode code = new RealIntegrationCode();
        List<String> resourceReferences = code.getResourceReferences();
        // 默认实现应该返回null或空列表
        assertNull(resourceReferences);
    }

    @Test
    public void testFactoryCreation() {
        String jsonString = "{\"factoryKey\":\"factoryValue\"}";
        CodeModel<Code> codeModel = CodeModelFactory.getCodeModel(CodeProgramType.RI.name(), jsonString);

        assertNotNull(codeModel);
        assertNotNull(codeModel.getCodeModel());
        assertEquals(CodeProgramType.RI.name(), codeModel.getProgramType());

        Code code = codeModel.getCodeModel();
        assertTrue(code instanceof RealIntegrationCode);

        RealIntegrationCode realIntegrationCode = (RealIntegrationCode)code;
        String content = realIntegrationCode.getContent();
        assertNotNull(content);
        assertTrue(content.contains("\"factoryKey\":\"factoryValue\""));
    }

    @Test
    public void testFactoryCreationWithNullCode() {
        CodeModel<Code> codeModel = CodeModelFactory.getCodeModel(CodeProgramType.RI.name(), null);

        assertNotNull(codeModel);
        assertNotNull(codeModel.getCodeModel());
        assertEquals(CodeProgramType.RI.name(), codeModel.getProgramType());

        Code code = codeModel.getCodeModel();
        assertTrue(code instanceof RealIntegrationCode);

        RealIntegrationCode realIntegrationCode = (RealIntegrationCode)code;
        assertNull(realIntegrationCode.getContent());
    }

    @Test
    public void testToString() {
        String jsonString = "{\"toStringKey\":\"toStringValue\"}";
        RealIntegrationCode code = new RealIntegrationCode();
        code.parse(jsonString);

        String toStringResult = code.toString();
        log.info("toString result: {}", toStringResult);
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("RealIntegrationCode"));
        assertTrue(toStringResult.contains("jsonObject"));
    }
}