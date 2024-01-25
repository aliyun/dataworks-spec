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

package com.aliyun.dataworks.common.spec.domain.dw.codemode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.Code;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ControllerJoinCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrJobType;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrProperty;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.MultiLanguageScriptingCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.OdpsSparkCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.PlainTextCode;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2022/12/28
 */
@Slf4j
public class CodeModelFactoryTest {
    @Test
    public void testGetCodeModel() {
        CodeModel<OdpsSparkCode> odpsSparkCode = CodeModelFactory.getCodeModel("ODPS_SPARK", "{}");
        odpsSparkCode.getCodeModel().setResourceReferences(new ArrayList<>());
        odpsSparkCode.getCodeModel().getResourceReferences().add("test_res.jar");
        System.out.println("code content: " + odpsSparkCode.getCodeModel().getContent());
        Assert.assertTrue(StringUtils.indexOf(
            odpsSparkCode.getContent(), "##@resource_reference{\"test_res.jar\"}") >= 0);
        Assert.assertNotNull(odpsSparkCode.getCodeModel());
        Assert.assertNotNull(odpsSparkCode.getCodeModel().getSparkJson());

        CodeModel<EmrCode> emr = CodeModelFactory.getCodeModel("EMR_HIVE", "");
        emr.getCodeModel().setName("emr");
        emr.getCodeModel().setType(EmrJobType.HIVE);
        emr.getCodeModel().setProperties(new EmrProperty().setArguments(Collections.singletonList("hive -e")));
        System.out.println("emr code: " + emr.getCodeModel().getContent());
        Assert.assertTrue(StringUtils.indexOf(emr.getContent(), "hive -e") >= 0);

        CodeModel<ControllerJoinCode> joinCode = CodeModelFactory.getCodeModel("CONTROLLER_JOIN", null);
        joinCode.getCodeModel()
            .setBranchList(Collections.singletonList(
                new ControllerJoinCode.Branch()
                    .setLogic(0)
                    .setNode("branch_node")
                    .setRunStatus(Arrays.asList("1", "2"))))
            .setResultStatus("2");
        System.out.println("join code: " + joinCode.getCodeModel().getContent());
        Assert.assertNotNull(joinCode.getCodeModel());
        Assert.assertTrue(StringUtils.indexOf(joinCode.getContent(), "branch_node") >= 0);

        CodeModel<PlainTextCode> odpsSqlCode = CodeModelFactory.getCodeModel("ODPS_SQL", "select 1;");
        System.out.println("odps code: " + odpsSqlCode.getContent());
        Assert.assertNotNull(odpsSqlCode);
        Assert.assertEquals("select 1;", odpsSqlCode.getContent());

        Assert.assertNotNull(CodeModelFactory.getCodeModel(null, null));

        CodeModel<Code> emrHive = CodeModelFactory.getCodeModel("EMR_HIVE", null);
        System.out.println("emr hive code: " + emrHive.getContent());
        EmrCode code = (EmrCode)emrHive.getCodeModel();
        System.out.println("emr code mode: {}" + GsonUtils.toJsonString(code));
    }

    @Test
    public void testMultiLanguageCode() {
        CodeModel<MultiLanguageScriptingCode> m = CodeModelFactory.getCodeModel("CONTROLLER_ASSIGNMENT", "");
        m.getCodeModel().setSourceCode("select 1");
        m.getCodeModel().setLanguage("odps");

        CodeModel<MultiLanguageScriptingCode> m2 = CodeModelFactory.getCodeModel("CONTROLLER_CYCLE_END", "{\n"
            + "  \"language\": \"odps\",\n"
            + "  \"content\": \"select 1\"\n"
            + "}");
        System.out.println("assignment content: " + m.getContent());
        System.out.println("assignment source code: " + m.getSourceCode());
        Assert.assertEquals("select 1", m.getSourceCode());
        System.out.println("cycle end content: " + m2.getContent());
        System.out.println("cycle end source code: " + m2.getSourceCode());
        Assert.assertEquals("select 1", m2.getSourceCode());

        System.out.println("template: " + m2.getTemplate());

        CodeModel<EmrCode> c = CodeModelFactory.getCodeModel("EMR_HIVE", "");
        c.setSourceCode("select 1");
        System.out.println("emr hive template: " + GsonUtils.toJsonString(c.getTemplate()));
    }
}
