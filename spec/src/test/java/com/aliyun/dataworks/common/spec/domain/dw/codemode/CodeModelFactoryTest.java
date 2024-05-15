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
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.DefaultJsonFormCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrJobType;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrProperty;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.MultiLanguageScriptingCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.OdpsSparkCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.PlainTextCode;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
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

    @Test
    public void testDefaultJsonFormCode() {
        String code = "{\n"
            + "            \"content\": \"IMPORT FOREIGN SCHEMA shanghai_onlineTest_simple LIMIT TO (wq_test_dataworks_pt_001) from SERVER "
            + "odps_server INTO public OPTIONS(prefix 'tmp_foreign_', suffix 'xozi4mmb', if_table_exist 'error',if_unsupported_type 'error');"
            + "\\nDROP TABLE IF EXISTS \\\"public\\\".tmp_holo_8gwvxopb_wqtest;\\nBEGIN;\\nCREATE TABLE IF NOT EXISTS \\\"public\\\""
            + ".tmp_holo_8gwvxopb_wqtest (\\n \\\"f1\\\" text NOT NULL,\\n \\\"f2\\\" text NOT NULL,\\n \\\"f4\\\" text NOT NULL,\\n \\\"f5\\\" "
            + "text NOT NULL,\\n \\\"f3\\\" text NOT NULL,\\n \\\"f6\\\" text NOT NULL,\\n \\\"f7\\\" text NOT NULL,\\n \\\"f10\\\" text NOT NULL,"
            + "\\n \\\"ds\\\" bigint NOT NULL,\\n \\\"pt\\\" text NOT NULL\\n);\\nCALL SET_TABLE_PROPERTY('\\\"public\\\""
            + ".tmp_holo_8gwvxopb_wqtest', 'orientation', 'column');\\ncomment on column \\\"public\\\".tmp_holo_8gwvxopb_wqtest.pt is '分区字段';"
            + "\\nCOMMIT;\\nINSERT INTO \\\"public\\\".tmp_holo_8gwvxopb_wqtest\\nSELECT \\n    CAST(\\\"f1\\\" as text),\\n    CAST(\\\"f2\\\" as "
            + "text),\\n    CAST(\\\"f4\\\" as text),\\n    CAST(\\\"f5\\\" as text),\\n    CAST(\\\"f3\\\" as text),\\n    CAST(\\\"f6\\\" as "
            + "text),\\n    CAST(\\\"f7\\\" as text),\\n    CAST(\\\"f10\\\" as text),\\n    CAST(\\\"ds\\\" as bigint),\\n    CAST(\\\"pt\\\" as "
            + "text)\\nFROM \\\"public\\\".tmp_foreign_wq_test_dataworks_pt_001xozi4mmb\\nWHERE pt='${bizdate}';\\nDROP FOREIGN TABLE IF EXISTS "
            + "\\\"public\\\".tmp_foreign_wq_test_dataworks_pt_001xozi4mmb;BEGIN;\\nDROP TABLE IF EXISTS \\\"public\\\".wqtest;\\nALTER TABLE "
            + "\\\"public\\\".tmp_holo_8gwvxopb_wqtest RENAME TO wqtest;\\nCOMMIT;\\n\",\n"
            + "            \"extraContent\": \"{\\\"connId\\\":\\\"yongxunqa_holo_shanghai\\\",\\\"dbName\\\":\\\"yongxunqa_hologres_db\\\","
            + "\\\"syncType\\\":1,\\\"extendProjectName\\\":\\\"shanghai_onlineTest_simple\\\",\\\"schemaName\\\":\\\"public\\\","
            + "\\\"tableName\\\":\\\"wqtest\\\",\\\"partitionColumn\\\":\\\"\\\",\\\"orientation\\\":\\\"column\\\","
            + "\\\"columns\\\":[{\\\"name\\\":\\\"f1\\\",\\\"comment\\\":\\\"\\\",\\\"type\\\":\\\"STRING\\\",\\\"allowNull\\\":false,"
            + "\\\"holoName\\\":\\\"f1\\\",\\\"holoType\\\":\\\"text\\\"},{\\\"name\\\":\\\"f2\\\",\\\"comment\\\":\\\"\\\","
            + "\\\"type\\\":\\\"STRING\\\",\\\"allowNull\\\":false,\\\"holoName\\\":\\\"f2\\\",\\\"holoType\\\":\\\"text\\\"},"
            + "{\\\"name\\\":\\\"f4\\\",\\\"comment\\\":\\\"\\\",\\\"type\\\":\\\"STRING\\\",\\\"allowNull\\\":false,\\\"holoName\\\":\\\"f4\\\","
            + "\\\"holoType\\\":\\\"text\\\"},{\\\"name\\\":\\\"f5\\\",\\\"comment\\\":\\\"\\\",\\\"type\\\":\\\"STRING\\\","
            + "\\\"allowNull\\\":false,\\\"holoName\\\":\\\"f5\\\",\\\"holoType\\\":\\\"text\\\"},{\\\"name\\\":\\\"f3\\\","
            + "\\\"comment\\\":\\\"\\\",\\\"type\\\":\\\"STRING\\\",\\\"allowNull\\\":false,\\\"holoName\\\":\\\"f3\\\","
            + "\\\"holoType\\\":\\\"text\\\"},{\\\"name\\\":\\\"f6\\\",\\\"comment\\\":\\\"\\\",\\\"type\\\":\\\"STRING\\\","
            + "\\\"allowNull\\\":false,\\\"holoName\\\":\\\"f6\\\",\\\"holoType\\\":\\\"text\\\"},{\\\"name\\\":\\\"f7\\\","
            + "\\\"comment\\\":\\\"\\\",\\\"type\\\":\\\"STRING\\\",\\\"allowNull\\\":false,\\\"holoName\\\":\\\"f7\\\","
            + "\\\"holoType\\\":\\\"text\\\"},{\\\"name\\\":\\\"f10\\\",\\\"comment\\\":\\\"\\\",\\\"type\\\":\\\"STRING\\\","
            + "\\\"allowNull\\\":false,\\\"holoName\\\":\\\"f10\\\",\\\"holoType\\\":\\\"text\\\"},{\\\"name\\\":\\\"ds\\\","
            + "\\\"comment\\\":\\\"\\\",\\\"type\\\":\\\"BIGINT\\\",\\\"allowNull\\\":false,\\\"holoName\\\":\\\"ds\\\","
            + "\\\"holoType\\\":\\\"bigint\\\"},{\\\"name\\\":\\\"pt\\\",\\\"comment\\\":\\\"分区字段\\\",\\\"type\\\":\\\"STRING\\\","
            + "\\\"allowNull\\\":false,\\\"holoName\\\":\\\"pt\\\",\\\"holoType\\\":\\\"text\\\"}],\\\"serverName\\\":\\\"odps_server\\\","
            + "\\\"extendTableName\\\":\\\"wq_test_dataworks_pt_001\\\",\\\"foreignSchemaName\\\":\\\"public\\\",\\\"foreignTableName\\\":\\\"\\\","
            + "\\\"instanceId\\\":\\\"yongxunqa_holo_shanghai\\\",\\\"engineType\\\":\\\"Hologres\\\",\\\"clusteringKey\\\":[],"
            + "\\\"bitmapIndexKey\\\":[],\\\"segmentKey\\\":[],\\\"dictionaryEncoding\\\":[]}\"\n"
            + "        }";
        CodeModel<DefaultJsonFormCode> codeModel = CodeModelFactory.getCodeModel(CodeProgramType.HOLOGRES_SYNC_DATA.name(), code);
        log.info("content: {}", codeModel.getCodeModel().getContent());
        log.info("extraContent: {}", codeModel.getCodeModel().getExtraContent());
        Assert.assertTrue(StringUtils.startsWith(codeModel.getCodeModel().getContent(), "IMPORT FOREIGN SCHEMA shanghai_on"));

        CodeModel<DefaultJsonFormCode> newCode = CodeModelFactory.getCodeModel(CodeProgramType.HOLOGRES_SYNC_DATA.name(), "{}");
        newCode.getCodeModel().setExtraContent("xx").setContent("select 1");
        log.info("content: {}", newCode.getContent());
        log.info("rawContent: {}", newCode.getRawContent());
        Assert.assertEquals("xx", newCode.getCodeModel().getExtraContent());
        Assert.assertEquals("select 1", newCode.getContent());
        Assert.assertEquals("{\"extraContent\":\"xx\",\"content\":\"select 1\"}", newCode.getRawContent());
    }
}
