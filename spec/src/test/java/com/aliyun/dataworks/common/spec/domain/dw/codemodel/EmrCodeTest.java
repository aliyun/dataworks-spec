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

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/7/19
 */
public class EmrCodeTest {
    @Test
    public void testEmrCode() {
        CodeModel<EmrCode> emrCodeModel = CodeModelFactory.getCodeModel("EMR_HIVE", null);
        EmrCode emrCode = emrCodeModel.getCodeModel();
        emrCode.setProperties(new EmrProperty().setArguments(Collections.singletonList("hive -e")));
        System.out.println("emr code: " + emrCode.getContent());
        Assert.assertTrue(StringUtils.indexOf(emrCode.getContent(), "hive -e") >= 0);
        Assert.assertEquals(EmrJobType.HIVE_SQL, emrCode.getType());

        // parse content to EmrCode and verify
        CodeModel<EmrCode> parsedCode = CodeModelFactory.getCodeModel("EMR_HIVE", emrCode.getContent());
        Assert.assertNotNull(parsedCode.getCodeModel().getProperties());
        Assert.assertNotNull(parsedCode.getCodeModel().getProperties().getArguments());
        Assert.assertEquals("hive -e", parsedCode.getCodeModel().getProperties().getArguments().get(0));
        Assert.assertEquals(EmrJobType.HIVE_SQL, parsedCode.getCodeModel().getType());
    }

    @Test
    public void testEmrCodeParseByContent() {
        String originalCode = "{\n"
            + "  \"type\": \"TRINO_SQL\",\n"
            + "  \"launcher\": {\n"
            + "    \"allocationSpec\": {}\n"
            + "  },\n"
            + "  \"properties\": {\n"
            + "    \"envs\": {},\n"
            + "    \"arguments\": [\n"
            + "      \"hive -e\"\n"
            + "    ],\n"
            + "    \"tags\": []\n"
            + "  },\n"
            + "  \"programType\": \"EMR_HIVE\"\n"
            + "}";

        // if emr job type is set in code json, it will be used by ignoring the CodeProgramType
        CodeModel<EmrCode> emr = CodeModelFactory.getCodeModel("EMR_HIVE", originalCode);
        Assert.assertNotNull(emr.getCodeModel());
        Assert.assertEquals(EmrJobType.TRINO_SQL, emr.getCodeModel().getType());
    }

    @Test
    public void testEmrCodeParse() {
        String originalCode = "{\n"
            + "  \"type\": \"TRINO_SQL\",\n"
            + "  \"launcher\": {\n"
            + "    \"allocationSpec\": {}\n"
            + "  },\n"
            + "  \"properties\": {\n"
            + "    \"envs\": {},\n"
            + "    \"arguments\": [\n"
            + "      \"hive -e\"\n"
            + "    ],\n"
            + "    \"tags\": []\n"
            + "  },\n"
            + "  \"programType\": \"EMR_HIVE\"\n"
            + "}";
        CodeModel<EmrCode> emr = CodeModelFactory.getCodeModel("EMR_HIVE", null);
        System.out.println(emr.getContent());
        Assert.assertTrue(StringUtils.indexOf(emr.getContent(), "hive -e") < 0);

        emr.parse(originalCode);
        System.out.println(emr.getContent());
        Assert.assertTrue(StringUtils.indexOf(emr.getContent(), "hive -e") > 0);
    }

    @Test
    public void testEmrCodeParseException() {
        CodeModel<EmrCode> emr = CodeModelFactory.getCodeModel("EMR_HIVE", "select 1");
        Assert.assertNotNull(emr);
        Assert.assertNotNull(emr.getCodeModel());
        Assert.assertEquals(EmrJobType.HIVE_SQL, emr.getCodeModel().getType());
    }

    @Test
    public void testEmrCodeContentMustWithNoHtmlEscape() {
        String originalCode = "{\n"
            + "  \"type\": \"TRINO_SQL\",\n"
            + "  \"launcher\": {\n"
            + "    \"allocationSpec\": {}\n"
            + "  },\n"
            + "  \"properties\": {\n"
            + "    \"envs\": {},\n"
            + "    \"arguments\": [\n"
            + "      \"select * from a='http://xx.yy.com?aaa=vvv'\"\n"
            + "    ],\n"
            + "    \"tags\": []\n"
            + "  },\n"
            + "  \"programType\": \"EMR_HIVE\"\n"
            + "}";
        CodeModel<EmrCode> emr = CodeModelFactory.getCodeModel("EMR_HIVE", originalCode);
        System.out.println(emr.getContent());
        Assert.assertTrue(StringUtils.indexOf(emr.getContent(), "http://xx.yy.com?aaa=vvv") > 0);
    }
}
