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

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 莫泣
 * @date 2025-10-27
 */
@Slf4j
public class DataSynchronizationQualityCheckCodeTest {

    @Test
    public void testParseAndGetContent() {
        // 测试数据
        String jsonString = "{\"syncKey1\":\"syncValue1\",\"syncKey2\":456,\"syncKey3\":{\"syncNestedKey\":\"syncNestedValue\"}}";

        // 创建DataSynchronizationQualityCheckCode实例
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();

        // 测试parse方法
        DataSynchronizationQualityCheckCode parsedCode = code.parse(jsonString);
        Assert.assertNotNull(parsedCode);
        Assert.assertSame(code, parsedCode);

        // 测试getContent方法
        String content = code.getContent();
        log.info("content: {}", content);
        Assert.assertNotNull(content);
        Assert.assertTrue(StringUtils.indexOf(content, "\"syncKey1\":\"syncValue1\"") > 0);
        Assert.assertTrue(StringUtils.indexOf(content, "\"syncKey2\":456") > 0);
        Assert.assertTrue(StringUtils.indexOf(content, "\"syncNestedKey\":\"syncNestedValue\"") > 0);
    }

    @Test
    public void testGetProgramTypes() {
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        Assert.assertNotNull(code.getProgramTypes());
        Assert.assertEquals(1, code.getProgramTypes().size());
        Assert.assertEquals(CodeProgramType.DATA_SYNCHRONIZATION_QUALITY_CHECK.name(), code.getProgramTypes().get(0));
    }

    @Test
    public void testGetTemplate() {
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        Assert.assertNull(code.getTemplate());
    }

    @Test
    public void testParseWithNull() {
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        DataSynchronizationQualityCheckCode parsedCode = code.parse(null);
        Assert.assertNotNull(parsedCode);
        Assert.assertNull(parsedCode.getContent());
    }

    @Test
    public void testParseWithEmptyString() {
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        DataSynchronizationQualityCheckCode parsedCode = code.parse("");
        Assert.assertNotNull(parsedCode);
        Assert.assertNull(parsedCode.getContent());
    }

    @Test
    public void testCodeModelFactoryCreation() {
        String content = "{\"syncKey1\":\"syncValue1\",\"syncKey2\":456,\"syncKey3\":{\"syncNestedKey\":\"syncNestedValue\"}}";
        CodeModel<DataSynchronizationQualityCheckCode> codeModel = CodeModelFactory.getCodeModel(
            CodeProgramType.DATA_SYNCHRONIZATION_QUALITY_CHECK.name(), content);

        Assert.assertNotNull(codeModel);
        Assert.assertNotNull(codeModel.getCodeModel());
        Assert.assertEquals(CodeProgramType.DATA_SYNCHRONIZATION_QUALITY_CHECK.name(), codeModel.getProgramType());

        DataSynchronizationQualityCheckCode code = codeModel.getCodeModel();
        String resultContent = code.getContent();
        log.info("content from CodeModelFactory: {}", resultContent);
        Assert.assertNotNull(resultContent);
        Assert.assertTrue(StringUtils.indexOf(resultContent, "\"syncKey1\":\"syncValue1\"") > 0);
        Assert.assertTrue(StringUtils.indexOf(resultContent, "\"syncKey2\":456") > 0);
        Assert.assertTrue(StringUtils.indexOf(resultContent, "\"syncNestedKey\":\"syncNestedValue\"") > 0);
    }

    @Test
    public void testGetContentWithNullJsonObject() {
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        // jsonObject字段为null时getContent应该返回null
        Assert.assertNull(code.getContent());
    }

    @Test
    public void testParseWithInvalidJson() {
        String invalidJson = "{ invalid json content }";
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        try {
            DataSynchronizationQualityCheckCode parsedCode = code.parse(invalidJson);
            Assert.assertNotNull(parsedCode);
            // 解析无效JSON时应该返回null内容
            Assert.assertNull(parsedCode.getContent());
        } catch (Exception e) {
            // 解析无效JSON时可能抛出异常，这也是正常的
            Assert.assertTrue(e instanceof com.google.gson.JsonSyntaxException);
        }
    }

    @Test
    public void testParseWithComplexJson() {
        String complexJson = "{\n" +
            "  \"tables\": [\n" +
            "    {\n" +
            "      \"name\": \"table1\",\n" +
            "      \"columns\": [\n" +
            "        {\"name\": \"col1\", \"type\": \"string\"},\n" +
            "        {\"name\": \"col2\", \"type\": \"int\"}\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"rules\": {\n" +
            "    \"check_count\": true,\n" +
            "    \"check_null\": [\"col1\", \"col2\"]\n" +
            "  }\n" +
            "}";

        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        DataSynchronizationQualityCheckCode parsedCode = code.parse(complexJson);
        Assert.assertNotNull(parsedCode);
        Assert.assertSame(code, parsedCode);

        String content = code.getContent();
        log.info("complex json content: {}", content);
        Assert.assertNotNull(content);
        Assert.assertTrue(StringUtils.indexOf(content, "\"name\":\"table1\"") > 0);
        Assert.assertTrue(StringUtils.indexOf(content, "\"check_count\":true") > 0);
    }

    @Test
    public void testSupportMethod() {
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        Assert.assertTrue(code.support(CodeProgramType.DATA_SYNCHRONIZATION_QUALITY_CHECK.name()));
        Assert.assertFalse(code.support("OTHER_TYPE"));
    }

    @Test
    public void testGetRawContent() {
        String jsonString = "{\"syncKey1\":\"syncValue1\"}";
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        code.parse(jsonString);

        String rawContent = code.getRawContent();
        String content = code.getContent();
        Assert.assertEquals(content, rawContent);
    }

    @Test
    public void testSetSourceCode() {
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        // setSourceCode方法应该不改变对象状态
        code.setSourceCode("test code");
        Assert.assertNull(code.getContent());
    }

    @Test
    public void testGetSourceCode() {
        String jsonString = "{\"syncKey1\":\"syncValue1\"}";
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        code.parse(jsonString);

        String sourceCode = code.getSourceCode();
        String content = code.getContent();
        Assert.assertEquals(content, sourceCode);
    }

    @Test
    public void testGetResourceReferences() {
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        // resourceReferences字段默认为null
        Assert.assertNull(code.getResourceReferences());
    }

    @Test
    public void testSetProgramType() {
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        code.setProgramType(CodeProgramType.DATA_SYNCHRONIZATION_QUALITY_CHECK.name());
        // setProgramType方法应该设置programType字段，但不影响其他行为
        Assert.assertNull(code.getContent());
    }

    @Test
    public void testGetClassHierarchyLevel() {
        DataSynchronizationQualityCheckCode code = new DataSynchronizationQualityCheckCode();
        Assert.assertTrue(code.getClassHierarchyLevel() >= 0);
    }
}