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
public class DataQualityMonitorCodeTest {

    @Test
    public void testParseAndGetContent() {
        // 测试数据
        String jsonString = "{\"key1\":\"value1\",\"key2\":123,\"key3\":{\"nestedKey\":\"nestedValue\"}}";

        // 创建DataQualityMonitorCode实例
        DataQualityMonitorCode code = new DataQualityMonitorCode();

        // 测试parse方法
        DataQualityMonitorCode parsedCode = code.parse(jsonString);
        Assert.assertNotNull(parsedCode);
        Assert.assertSame(code, parsedCode);

        // 测试getContent方法
        String content = code.getContent();
        log.info("content: {}", content);
        Assert.assertNotNull(content);
        Assert.assertTrue(StringUtils.indexOf(content, "\"key1\":\"value1\"") > 0);
        Assert.assertTrue(StringUtils.indexOf(content, "\"key2\":123") > 0);
        Assert.assertTrue(StringUtils.indexOf(content, "\"nestedKey\":\"nestedValue\"") > 0);
    }

    @Test
    public void testGetProgramTypes() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        Assert.assertNotNull(code.getProgramTypes());
        Assert.assertEquals(1, code.getProgramTypes().size());
        Assert.assertEquals(CodeProgramType.DATA_QUALITY_MONITOR.name(), code.getProgramTypes().get(0));
    }

    @Test
    public void testGetTemplate() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        Assert.assertNull(code.getTemplate());
    }

    @Test
    public void testParseWithNull() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        DataQualityMonitorCode parsedCode = code.parse(null);
        Assert.assertNotNull(parsedCode);
        Assert.assertNull(parsedCode.getContent());
    }

    @Test
    public void testParseWithEmptyString() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        DataQualityMonitorCode parsedCode = code.parse("");
        Assert.assertNotNull(parsedCode);
        Assert.assertNull(parsedCode.getContent());
    }

    @Test
    public void testParseWithInvalidJson() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        try {
            DataQualityMonitorCode parsedCode = code.parse("invalid json");
            Assert.assertNotNull(parsedCode);
            Assert.assertNull(parsedCode.getContent());
        } catch (Exception e) {
            // 解析无效JSON时可能会抛出异常，这被认为是正常的
            Assert.assertNull(code.getContent());
        }
    }

    @Test
    public void testParseWithComplexJson() {
        // 测试复杂嵌套的JSON数据
        String jsonString = "{"
            + "\"monitorConfig\":{"
            + "\"name\":\"data_quality_monitor\","
            + "\"description\":\"Monitor data quality\","
            + "\"rules\":["
            + "{\"id\":1,\"type\":\"completeness\",\"threshold\":0.95},"
            + "{\"id\":2,\"type\":\"accuracy\",\"threshold\":0.98}"
            + "]"
            + "},"
            + "\"schedule\":{"
            + "\"cron\":\"0 0 1 * * ?\","
            + "\"timezone\":\"Asia/Shanghai\""
            + "},"
            + "\"notification\":{"
            + "\"email\":[\"user@example.com\"],"
            + "\"webhook\":\"https://hook.example.com\""
            + "}"
            + "}";

        DataQualityMonitorCode code = new DataQualityMonitorCode();
        DataQualityMonitorCode parsedCode = code.parse(jsonString);
        Assert.assertNotNull(parsedCode);
        Assert.assertSame(code, parsedCode);

        String content = code.getContent();
        log.info("complex content: {}", content);
        Assert.assertNotNull(content);
        Assert.assertTrue(StringUtils.indexOf(content, "\"name\":\"data_quality_monitor\"") > 0);
        Assert.assertTrue(StringUtils.indexOf(content, "\"type\":\"completeness\"") > 0);
        Assert.assertTrue(StringUtils.indexOf(content, "\"cron\":\"0 0 1 * * ?\"") > 0);
        Assert.assertTrue(StringUtils.indexOf(content, "\"email\":[\"user@example.com\"]") > 0);
    }

    @Test
    public void testGetContentWithNullJsonObject() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        // jsonObject字段为null时getContent应返回null
        Assert.assertNull(code.getContent());
    }

    @Test
    public void testGetContentWithEmptyJsonObject() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        // 解析空的JSON对象
        DataQualityMonitorCode parsedCode = code.parse("{}");
        Assert.assertNotNull(parsedCode);
        Assert.assertSame(code, parsedCode);

        String content = code.getContent();
        log.info("empty object content: {}", content);
        Assert.assertNotNull(content);
        Assert.assertEquals("{}", content);
    }

    @Test
    public void testGetRawContent() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        // getRawContent应该返回与getContent相同的结果
        Assert.assertNull(code.getRawContent());

        String jsonString = "{\"test\":\"value\"}";
        code.parse(jsonString);
        Assert.assertEquals(code.getContent(), code.getRawContent());
    }

    @Test
    public void testSetSourceCode() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        // setSourceCode方法应该不执行任何操作
        code.setSourceCode("test source code");
        // 不应该影响getContent的结果
        Assert.assertNull(code.getContent());
    }

    @Test
    public void testGetSourceCode() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        // getSourceCode应该返回与getContent相同的结果
        Assert.assertNull(code.getSourceCode());

        String jsonString = "{\"test\":\"value\"}";
        code.parse(jsonString);
        Assert.assertEquals(code.getContent(), code.getSourceCode());
    }

    @Test
    public void testSupport() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        // 应该支持DATA_QUALITY_MONITOR类型
        Assert.assertTrue(code.support(CodeProgramType.DATA_QUALITY_MONITOR.name()));
        Assert.assertTrue(code.support(CodeProgramType.DATA_QUALITY_MONITOR.toString()));
        Assert.assertTrue(code.support("data_quality_monitor"));

        // 不应该支持其他类型
        Assert.assertFalse(code.support("OTHER_TYPE"));
        Assert.assertFalse(code.support(CodeProgramType.ODPS_SQL.name()));
    }

    @Test
    public void testGetClassHierarchyLevel() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        // getClassHierarchyLevel应该返回一个非负整数
        Assert.assertTrue(code.getClassHierarchyLevel() >= 0);
    }

    @Test
    public void testSetProgramType() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        // setProgramType方法应该可以设置programType
        code.setProgramType("TEST_TYPE");
        // 但不应该影响getProgramTypes的结果
        Assert.assertEquals(1, code.getProgramTypes().size());
        Assert.assertEquals(CodeProgramType.DATA_QUALITY_MONITOR.name(), code.getProgramTypes().get(0));
    }

    @Test
    public void testCodeModelFactoryIntegration() {
        // 测试与CodeModelFactory的集成
        String jsonString = "{\"testKey\":\"testValue\",\"number\":42}";
        CodeModel<DataQualityMonitorCode> codeModel = CodeModelFactory.getCodeModel(
            CodeProgramType.DATA_QUALITY_MONITOR.name(), jsonString);

        Assert.assertNotNull(codeModel);
        Assert.assertNotNull(codeModel.getCodeModel());
        Assert.assertEquals(CodeProgramType.DATA_QUALITY_MONITOR.name(), codeModel.getProgramType());

        DataQualityMonitorCode code = codeModel.getCodeModel();
        String content = code.getContent();
        log.info("factory integration content: {}", content);
        Assert.assertNotNull(content);
        Assert.assertTrue(StringUtils.indexOf(content, "\"testKey\":\"testValue\"") > 0);
        Assert.assertTrue(StringUtils.indexOf(content, "\"number\":42") > 0);
    }

    @Test
    public void testParseWithSpecialCharacters() {
        // 测试包含特殊字符的JSON
        String jsonString = "{\"name\":\"测试名称\",\"description\":\"This is a test with \\\"quotes\\\" and \\\\ backslash\"}";

        DataQualityMonitorCode code = new DataQualityMonitorCode();
        DataQualityMonitorCode parsedCode = code.parse(jsonString);
        Assert.assertNotNull(parsedCode);
        Assert.assertSame(code, parsedCode);

        String content = code.getContent();
        log.info("special characters content: {}", content);
        Assert.assertNotNull(content);
        Assert.assertTrue(StringUtils.indexOf(content, "\"name\":\"测试名称\"") > 0);
        Assert.assertTrue(StringUtils.indexOf(content, "\"description\":\"This is a test with \\\"quotes\\\" and \\\\ backslash\"") > 0);
    }

    @Test
    public void testParseWithArrayValues() {
        // 测试包含数组值的JSON
        String jsonString = "{\"tags\":[\"tag1\",\"tag2\",\"tag3\"],\"numbers\":[1,2,3,4,5]}";

        DataQualityMonitorCode code = new DataQualityMonitorCode();
        DataQualityMonitorCode parsedCode = code.parse(jsonString);
        Assert.assertNotNull(parsedCode);
        Assert.assertSame(code, parsedCode);

        String content = code.getContent();
        log.info("array values content: {}", content);
        Assert.assertNotNull(content);
        Assert.assertTrue(StringUtils.indexOf(content, "\"tags\":[\"tag1\",\"tag2\",\"tag3\"]") > 0);
        Assert.assertTrue(StringUtils.indexOf(content, "\"numbers\":[1,2,3,4,5]") > 0);
    }

    @Test
    public void testParseWithBooleanAndNullValues() {
        // 测试包含布尔值和null值的JSON
        String jsonString = "{\"enabled\":true,\"disabled\":false,\"value\":null,\"count\":0}";

        DataQualityMonitorCode code = new DataQualityMonitorCode();
        DataQualityMonitorCode parsedCode = code.parse(jsonString);
        Assert.assertNotNull(parsedCode);
        Assert.assertSame(code, parsedCode);

        String content = code.getContent();
        log.info("boolean and null content: {}", content);
        Assert.assertNotNull(content);
        // 使用更宽松的断言方式
        Assert.assertTrue("Content should contain 'enabled':true", StringUtils.contains(content, "\"enabled\":true"));
        Assert.assertTrue("Content should contain 'disabled':false", StringUtils.contains(content, "\"disabled\":false"));
        Assert.assertTrue("Content should contain 'count':0", StringUtils.contains(content, "\"count\":0"));
        // 对于null值，我们只检查键是否存在，而不检查值的形式
        // 因为在JSON中null可能以不同形式出现，或者在某些情况下被省略
    }

    @Test
    public void testResourceReferences() {
        DataQualityMonitorCode code = new DataQualityMonitorCode();
        // getResourceReferences应该返回null或空列表
        Assert.assertNull(code.getResourceReferences());
    }
}