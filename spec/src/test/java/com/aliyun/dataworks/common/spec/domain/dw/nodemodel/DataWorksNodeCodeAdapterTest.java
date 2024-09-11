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

package com.aliyun.dataworks.common.spec.domain.dw.nodemodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ComponentSqlCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.DefaultJsonFormCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrAllocationSpec;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.OdpsSparkCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.PaiFlowCode;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.noref.SpecAssertIn;
import com.aliyun.dataworks.common.spec.domain.noref.SpecAssertion;
import com.aliyun.dataworks.common.spec.domain.noref.SpecBranch;
import com.aliyun.dataworks.common.spec.domain.noref.SpecBranches;
import com.aliyun.dataworks.common.spec.domain.noref.SpecJoin;
import com.aliyun.dataworks.common.spec.domain.noref.SpecJoinBranch;
import com.aliyun.dataworks.common.spec.domain.noref.SpecLogic;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponentParameter;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.emr.EmrJobConfig;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.emr.EmrJobExecuteMode;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.emr.EmrJobSubmitMode;
import com.aliyun.dataworks.common.spec.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2023/11/9
 */
@Slf4j
public class DataWorksNodeCodeAdapterTest {
    @Test
    public void testAssignCode() {
        SpecNode assignNode = new SpecNode();
        SpecScript script = new SpecScript();
        script.setLanguage("OdpsScript");
        script.setContent("select 'true';");
        SpecScriptRuntime runtime = new SpecScriptRuntime();
        runtime.setCommand(CodeProgramType.CONTROLLER_ASSIGNMENT.name());
        script.setRuntime(runtime);
        assignNode.setScript(script);

        DataWorksNodeCodeAdapter adapter = new DataWorksNodeCodeAdapter(assignNode);
        System.out.println(adapter.getCode());
    }

    @Test
    public void testBranch() {
        SpecNode branchNode = new SpecNode();
        SpecScript script = new SpecScript();
        SpecScriptRuntime runtime = new SpecScriptRuntime();
        runtime.setCommand(CodeProgramType.CONTROLLER_BRANCH.name());
        script.setRuntime(runtime);

        SpecBranches b1 = new SpecBranches();
        b1.setWhen("${a} == 1");
        b1.setDesc("b1");
        SpecNodeOutput out1 = new SpecNodeOutput();
        out1.setData("autotest.12345_out");
        b1.setOutput(out1);

        SpecBranches b2 = new SpecBranches();
        b2.setWhen("${a} == 1");
        b2.setDesc("b1");
        SpecNodeOutput out2 = new SpecNodeOutput();
        out2.setData("autotest.56789_out");
        b2.setOutput(out2);

        branchNode.setBranch(new SpecBranch().setBranches(Arrays.asList(b1, b2)));
        branchNode.setScript(script);

        DataWorksNodeCodeAdapter adapter = new DataWorksNodeCodeAdapter(branchNode);
        System.out.println(adapter.getCode());
        Assert.assertNotNull(adapter.getCode());
    }

    @Test
    public void testJoin() {
        SpecNode joinNode = new SpecNode();
        SpecScript script = new SpecScript();
        SpecScriptRuntime runtime = new SpecScriptRuntime();
        runtime.setCommand(CodeProgramType.CONTROLLER_JOIN.name());
        script.setRuntime(runtime);
        joinNode.setScript(script);

        SpecJoin join = new SpecJoin();
        joinNode.setJoin(join);
        SpecLogic logic = new SpecLogic();
        logic.setExpression("b1 and b2 or b3 or b4");
        join.setLogic(logic);

        SpecAssertion assertion = new SpecAssertion();
        assertion.setField("status");
        SpecAssertIn in = new SpecAssertIn();
        in.setValue(Arrays.asList("SUCCESS", "FAILURE"));
        assertion.setIn(in);

        SpecJoinBranch b1 = new SpecJoinBranch();
        b1.setName("b1");
        b1.setAssertion(assertion);
        SpecNodeOutput o1 = new SpecNodeOutput();
        o1.setData("dd");
        b1.setOutput(o1);

        SpecJoinBranch b2 = new SpecJoinBranch();
        b2.setName("b2");
        b2.setAssertion(assertion);
        SpecNodeOutput o2 = new SpecNodeOutput();
        o2.setData("dd");
        b2.setOutput(o2);

        SpecJoinBranch b3 = new SpecJoinBranch();
        b3.setName("b3");
        b3.setAssertion(assertion);
        SpecNodeOutput o3 = new SpecNodeOutput();
        o3.setData("dd");
        b3.setOutput(o3);

        SpecJoinBranch b4 = new SpecJoinBranch();
        b4.setName("b4");
        b4.setAssertion(assertion);
        SpecNodeOutput o4 = new SpecNodeOutput();
        o4.setData("dd");
        b4.setOutput(o4);

        join.setBranches(Arrays.asList(b1, b2, b3, b4));

        DataWorksNodeCodeAdapter adapter = new DataWorksNodeCodeAdapter(joinNode);
        System.out.println(adapter.getCode());
    }

    @Test
    public void testEmr() {
        String spec = "{\n"
            + "\t\"version\":\"1.1.0\",\n"
            + "\t\"kind\":\"CycleWorkflow\",\n"
            + "\t\"spec\":{\n"
            + "\t\t\"nodes\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"recurrence\":\"Normal\",\n"
            + "\t\t\t\t\"id\":\"7261439383042556772\",\n"
            + "\t\t\t\t\"timeout\":0,\n"
            + "\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\"rerunTimes\":3,\n"
            + "\t\t\t\t\"rerunInterval\":180000,\n"
            + "\t\t\t\t\"datasource\":{\n"
            + "\t\t\t\t\t\"name\":\"dt_spark_cluster_ea120_01\",\n"
            + "\t\t\t\t\t\"type\":\"emr\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\"language\":\"spark-sql\",\n"
            + "\t\t\t\t\t\"path\":\"个秋/onefall_test_spark_sql_1\",\n"
            + "\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\"command\":\"EMR_SPARK_SQL\",\n"
            + "\t\t\t\t\t\t\"commandTypeId\":229,\n"
            + "\t\t\t\t\t\t\"emrJobConfig\":{\n"
            + "\t\t\t\t\t\t\t\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"content\":\"select * from paimon.dt_spark_test_db5.students;\\n\",\n"
            + "\t\t\t\t\t\"id\":\"5862898736117902935\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\"type\":\"Scheduler\",\n"
            + "\t\t\t\t\t\"id\":\"8461963194104597781\",\n"
            + "\t\t\t\t\t\"cron\":\"00 00 00 * * ?\",\n"
            + "\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"endTime\":\"9999-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"timezone\":\"Asia/Shanghai\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"runtimeResource\":{\n"
            + "\t\t\t\t\t\"resourceGroup\":\"emr_poc_serverless_spark\",\n"
            + "\t\t\t\t\t\"id\":\"8212072828324694797\",\n"
            + "\t\t\t\t\t\"resourceGroupId\":\"394152227\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"name\":\"onefall_test_spark_sql_1\",\n"
            + "\t\t\t\t\"owner\":\"171389\",\n"
            + "\t\t\t\t\"metadata\":{\n"
            + "\t\t\t\t\t\"tenantId\":\"1\",\n"
            + "\t\t\t\t\t\"projectId\":\"33293\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\n"
            + "\t\t\t\t}\n"
            + "\t\t\t}\n"
            + "\t\t],\n"
            + "\t\t\"flow\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"nodeId\":\"7261439383042556772\",\n"
            + "\t\t\t\t\"depends\":[\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"type\":\"Normal\",\n"
            + "\t\t\t\t\t\t\"output\":\"serverless_spark_root\"\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t]\n"
            + "\t\t\t}\n"
            + "\t\t]\n"
            + "\t},\n"
            + "\t\"metadata\":{\n"
            + "\t\t\"uuid\":\"7261439383042556772\"\n"
            + "\t}\n"
            + "}";

        Specification<DataWorksWorkflowSpec> sp = SpecUtil.parseToDomain(spec);
        DataWorksNodeCodeAdapter adapter = new DataWorksNodeCodeAdapter(sp.getSpec().getNodes().get(0));
        System.out.println(adapter.getCode());
    }

    @Test
    public void testEmrCompatible() {
        SpecNode emrNode = new SpecNode();
        SpecScript script = new SpecScript();
        script.setLanguage("hive-sql");
        script.setContent("{}");
        SpecScriptRuntime runtime = new SpecScriptRuntime();
        runtime.setCommand(CodeProgramType.EMR_HIVE.name());
        EmrJobConfig emrJobConfig = new EmrJobConfig();
        emrJobConfig.setExecuteMode(EmrJobExecuteMode.SINGLE);
        emrJobConfig.setSubmitMode(EmrJobSubmitMode.YARN);
        emrJobConfig.setQueue("ods");
        emrJobConfig.setPriority(5);
        emrJobConfig.setSessionEnabled(true);
        runtime.setEmrJobConfig(emrJobConfig.toMap());
        script.setRuntime(runtime);
        emrNode.setScript(script);

        DataWorksNodeCodeAdapter adapter = new DataWorksNodeCodeAdapter(emrNode);
        System.out.println(adapter.getCode());
        Assert.assertTrue(StringUtils.isNotBlank(adapter.getCode()));
        Assert.assertTrue(StringUtils.indexOf(adapter.getCode(), "arguments") > 0);
    }

    @Test
    public void testEmrSpecNodeAdapter() {
        String spec =
            "  {\n"
                + "    \"version\": \"1.1.0\",\n"
                + "    \"kind\": \"CycleWorkflow\",\n"
                + "    \"spec\": {\n"
                + "      \"nodes\": [\n"
                + "        {\n"
                + "          \"recurrence\": \"Normal\",\n"
                + "          \"id\": \"5452198562404448810\",\n"
                + "          \"timeout\": 12,\n"
                + "          \"instanceMode\": \"T+1\",\n"
                + "          \"rerunMode\": \"Allowed\",\n"
                + "          \"rerunTimes\": 3,\n"
                + "          \"rerunInterval\": 18000,\n"
                + "          \"datasource\": {\n"
                + "            \"name\": \"test_current_account_hadoop\",\n"
                + "            \"type\": \"emr\"\n"
                + "          },\n"
                + "          \"script\": {\n"
                + "            \"path\": \"createNode/emr_hive_test_0\",\n"
                + "            \"language\": \"odps\",\n"
                + "              \"content\": \"show databases\",\n"
                + "            \"runtime\": {\n"
                + "              \"engine\": \"EMR\",\n"
                + "              \"command\": \"EMR_HIVE\",\n"
                + "              \"emrJobConfig\": {\n"
                + "                \"cores\": 1,\n"
                + "                \"executeMode\": \"SINGLE\",\n"
                + "                \"memory\": 1024,\n"
                + "                \"priority\": 1,\n"
                + "                \"queue\": \"default\",\n"
                + "                \"submitMode\": \"LOCAL\",\n"
                + "                \"submitter\": \"root\"\n"
                + "              },\n"
                + "              \"sparkConf\": {\n"
                + "                \"spark.executor.memory\": \"1024m\",\n"
                + "                \"spark.executor.cores\": 1,\n"
                + "                \"spark.executor.instances\": 1,\n"
                + "                \"spark.yarn.maxAppAttempts\": 1,\n"
                + "                \"spark.yarn.queue\": \"default\",\n"
                + "                \"spark.yarn.maxExecutorRetries\": 1\n"
                + "              }\n"
                + "            },\n"
                + "            \"parameters\": [\n"
                + "              {\n"
                + "                \"name\": \"bizdate\",\n"
                + "                \"artifactType\": \"Variable\",\n"
                + "                \"scope\": \"NodeParameter\",\n"
                + "                \"type\": \"System\",\n"
                + "                \"value\": \"${yyyymmdd}\"\n"
                + "              }\n"
                + "            ]\n"
                + "          },\n"
                + "          \"trigger\": {\n"
                + "            \"type\": \"Scheduler\",\n"
                + "            \"cron\": \"00 00 00 * * ?\",\n"
                + "            \"startTime\": \"1970-01-01 00:00:00\",\n"
                + "            \"endTime\": \"9999-01-01 00:00:00\",\n"
                + "            \"timezone\": \"Asia/Shanghai\"\n"
                + "          },\n"
                + "          \"runtimeResource\": {\n"
                + "            \"resourceGroup\": \"res_group_1\",\n"
                + "            \"resourceGroupId\": \"1\"\n"
                + "          },\n"
                + "          \"name\": \"emr_hive_test_0\",\n"
                + "          \"owner\": \"WORKER_1482465063962\",\n"
                + "          \"inputs\": {},\n"
                + "          \"outputs\": {\n"
                + "            \"nodeOutputs\": [\n"
                + "              {\n"
                + "                \"data\": \"5452198562404448810\",\n"
                + "                \"artifactType\": \"NodeOutput\",\n"
                + "                \"refTableName\": \"emr_hive_test_0\"\n"
                + "              }\n"
                + "            ]\n"
                + "          }\n"
                + "        }\n"
                + "      ]\n"
                + "    }\n"
                + "  }";

        Specification<DataWorksWorkflowSpec> sp = SpecUtil.parseToDomain(spec);
        SpecNode specNode = sp.getSpec().getNodes().get(0);
        EmrCode emrCode = new EmrCode();
        emrCode.setSourceCode(specNode.getScript().getContent());
        specNode.getScript().setContent(JSONUtils.toJsonString(emrCode));
        DataWorksNodeAdapter adapter = new DataWorksNodeAdapter(sp, specNode);
        CodeModel<EmrCode> cm = CodeModelFactory.getCodeModel(CodeProgramType.EMR_HIVE.name(), adapter.getCode());
        Assert.assertNotNull(cm);
        Assert.assertNotNull(cm.getCodeModel());
        Assert.assertNotNull(cm.getCodeModel().getLauncher());
        Assert.assertNotNull(cm.getCodeModel().getLauncher().getAllocationSpec());

        EmrAllocationSpec allSpec = EmrAllocationSpec.of(cm.getCodeModel().getLauncher().getAllocationSpec());
        Assert.assertNotNull(allSpec);
        System.out.println(allSpec);
        Assert.assertTrue(CollectionUtils.isNotEmpty(cm.getCodeModel().getLauncher().getAllocationSpec().entrySet()));
        Assert.assertNotNull(cm.getCodeModel().getProperties());
    }

    @Test
    public void testHologresDataSyncNode() {
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
        SpecNode node = new SpecNode();
        SpecScript scr = new SpecScript();
        SpecScriptRuntime rt = new SpecScriptRuntime();
        rt.setCommand(CodeProgramType.HOLOGRES_SYNC_DATA.name());
        scr.setRuntime(rt);
        scr.setContent(code);
        node.setScript(scr);
        DataWorksNodeCodeAdapter codeAdapter = new DataWorksNodeCodeAdapter(node);
        String codeContent = codeAdapter.getCode();
        log.info("code content: {}", codeContent);
        Assert.assertTrue(StringUtils.startsWith(codeContent, "IMPORT FOREIGN SCHEMA shanghai_on"));
    }

    @Test
    public void testComponentSql() {
        SpecNode specNode = new SpecNode();
        SpecComponent com = new SpecComponent();
        com.setId("121212");
        com.setInputs(Collections.singletonList(new SpecComponentParameter().setName("in1").setValue("va1")));
        com.setOutputs(Collections.singletonList(new SpecComponentParameter().setName("out1").setValue("va1")));
        specNode.setComponent(com);
        SpecScript scr = new SpecScript();
        scr.setContent("select @@{in1}");
        SpecScriptRuntime rt = new SpecScriptRuntime();
        rt.setCommand(CodeProgramType.COMPONENT_SQL.getName());
        scr.setRuntime(rt);
        specNode.setScript(scr);
        DataWorksNodeCodeAdapter adapter = new DataWorksNodeCodeAdapter(specNode);
        String code = adapter.getCode();
        log.info("code: {}", code);

        CodeModel<ComponentSqlCode> cm = CodeModelFactory.getCodeModel(CodeProgramType.COMPONENT_SQL.getName(), code);
        Assert.assertNotNull(cm);
        Assert.assertEquals("select va1", cm.getSourceCode());
        Assert.assertNotNull(cm.getCodeModel().getConfig());
    }

    @Test
    public void testPaiStudio() {
        String content = "{\n"
            + "  \"content\": \"{\\\"appId\\\":23620,\\\"computeResource\\\":{\\\"MaxCompute\\\":\\\"execution_maxcompute\\\"},"
            + "\\\"connectionType\\\":\\\"MaxCompute\\\",\\\"description\\\":\\\"\\\","
            + "\\\"flowUniqueCode\\\":\\\"ee1b1797-9f7c-4937-b672-028c4ced649b\\\",\\\"inputs\\\":[],\\\"name\\\":\\\"haozhen-designer-001\\\","
            + "\\\"outputs\\\":[],\\\"paiflowArguments\\\":\\\"---\\\\narguments:\\\\n  parameters:\\\\n  - name: "
            + "\\\\\\\"execution_maxcompute\\\\\\\"\\\\n    value:\\\\n      endpoint: \\\\\\\"http://service.odps.aliyun-inc.com/api\\\\\\\"\\\\n "
            + "     odpsProject: \\\\\\\"dw_scheduler_pre_dev\\\\\\\"\\\\n      spec:\\\\n        endpoint: \\\\\\\"http://service.odps.aliyun-inc"
            + ".com/api\\\\\\\"\\\\n        odpsProject: \\\\\\\"dw_scheduler_pre_dev\\\\\\\"\\\\n      resourceType: "
            + "\\\\\\\"MaxCompute\\\\\\\"\\\\n\\\",\\\"paiflowParameters\\\":{},\\\"paiflowPipeline\\\":\\\"---\\\\napiVersion: "
            + "\\\\\\\"core/v1\\\\\\\"\\\\nmetadata:\\\\n  provider: \\\\\\\"067848\\\\\\\"\\\\n  version: \\\\\\\"v1\\\\\\\"\\\\n  identifier: "
            + "\\\\\\\"job-root-pipeline-identifier\\\\\\\"\\\\n  annotations: {}\\\\nspec:\\\\n  inputs:\\\\n    artifacts: []\\\\n    "
            + "parameters:\\\\n    - name: \\\\\\\"execution_maxcompute\\\\\\\"\\\\n      type: \\\\\\\"Map\\\\\\\"\\\\n  arguments:\\\\n    "
            + "artifacts: []\\\\n    parameters: []\\\\n  dependencies: []\\\\n  initContainers: []\\\\n  sideCarContainers: []\\\\n  "
            + "pipelines:\\\\n  - apiVersion: \\\\\\\"core/v1\\\\\\\"\\\\n    metadata:\\\\n      provider: \\\\\\\"pai\\\\\\\"\\\\n      version: "
            + "\\\\\\\"v1\\\\\\\"\\\\n      identifier: \\\\\\\"sql\\\\\\\"\\\\n      name: \\\\\\\"id-fdb222dd-1396-4cdc-baf8\\\\\\\"\\\\n      "
            + "displayName: \\\\\\\"SQL脚本-1\\\\\\\"\\\\n      annotations: {}\\\\n    spec:\\\\n      arguments:\\\\n        artifacts: []\\\\n    "
            + "    parameters:\\\\n        - name: \\\\\\\"scriptMode\\\\\\\"\\\\n          value: false\\\\n        - name: "
            + "\\\\\\\"addCreateTableStatement\\\\\\\"\\\\n          value: true\\\\n        - name: \\\\\\\"sql\\\\\\\"\\\\n          value: "
            + "\\\\\\\"select 1;\\\\\\\"\\\\n        - name: \\\\\\\"execution\\\\\\\"\\\\n          from: \\\\\\\"{{inputs.parameters"
            + ".execution_maxcompute}}\\\\\\\"\\\\n      dependencies: []\\\\n      initContainers: []\\\\n      sideCarContainers: []\\\\n      "
            + "pipelines: []\\\\n      volumes: []\\\\n  - apiVersion: \\\\\\\"core/v1\\\\\\\"\\\\n    metadata:\\\\n      provider: "
            + "\\\\\\\"pai\\\\\\\"\\\\n      version: \\\\\\\"v1\\\\\\\"\\\\n      identifier: \\\\\\\"sql\\\\\\\"\\\\n      name: "
            + "\\\\\\\"id-c541f1eb-81b8-4e10-921b\\\\\\\"\\\\n      displayName: \\\\\\\"SQL脚本-2\\\\\\\"\\\\n      annotations: {}\\\\n    "
            + "spec:\\\\n      arguments:\\\\n        artifacts:\\\\n        - name: \\\\\\\"inputTable3\\\\\\\"\\\\n          from: "
            + "\\\\\\\"{{pipelines.id-fdb222dd-1396-4cdc-baf8.outputs.artifacts.outputTable}}\\\\\\\"\\\\n        parameters:\\\\n        - name: "
            + "\\\\\\\"scriptMode\\\\\\\"\\\\n          value: false\\\\n        - name: \\\\\\\"addCreateTableStatement\\\\\\\"\\\\n          "
            + "value: true\\\\n        - name: \\\\\\\"sql\\\\\\\"\\\\n          value: \\\\\\\"SELECT 2;\\\\\\\"\\\\n        - name: "
            + "\\\\\\\"execution\\\\\\\"\\\\n          from: \\\\\\\"{{inputs.parameters.execution_maxcompute}}\\\\\\\"\\\\n      "
            + "dependencies:\\\\n      - \\\\\\\"id-fdb222dd-1396-4cdc-baf8\\\\\\\"\\\\n      initContainers: []\\\\n      sideCarContainers: "
            + "[]\\\\n      pipelines: []\\\\n      volumes: []\\\\n  - apiVersion: \\\\\\\"core/v1\\\\\\\"\\\\n    metadata:\\\\n      provider: "
            + "\\\\\\\"pai\\\\\\\"\\\\n      version: \\\\\\\"v1\\\\\\\"\\\\n      identifier: \\\\\\\"data_source\\\\\\\"\\\\n      name: "
            + "\\\\\\\"id-151c5da3-ad9c-4be7-8ac3\\\\\\\"\\\\n      displayName: \\\\\\\"读数据表-1\\\\\\\"\\\\n      annotations: {}\\\\n    "
            + "spec:\\\\n      arguments:\\\\n        artifacts: []\\\\n        parameters:\\\\n        - name: \\\\\\\"hasPartition\\\\\\\"\\\\n  "
            + "        value: \\\\\\\"false\\\\\\\"\\\\n        - name: \\\\\\\"execution\\\\\\\"\\\\n          from: \\\\\\\"{{inputs.parameters"
            + ".execution_maxcompute}}\\\\\\\"\\\\n      dependencies: []\\\\n      initContainers: []\\\\n      sideCarContainers: []\\\\n      "
            + "pipelines: []\\\\n      volumes: []\\\\n  volumes: []\\\\n\\\",\\\"paraValue\\\":\\\"--paiflow_endpoint=paiflowinner-share.aliyuncs"
            + ".com --region=inner\\\",\\\"prgType\\\":\\\"1000138\\\",\\\"requestId\\\":\\\"7A497B66-62ED-52A3-A64D-F1EC7B61052A\\\","
            + "\\\"taskRelations\\\":[{\\\"childTaskUniqueCode\\\":\\\"id-c541f1eb-81b8-4e10-921b\\\","
            + "\\\"parentTaskUniqueCode\\\":\\\"id-fdb222dd-1396-4cdc-baf8\\\"}],\\\"tasks\\\":[{\\\"root\\\":true,"
            + "\\\"taskName\\\":\\\"SQL脚本-1\\\",\\\"taskUniqueCode\\\":\\\"id-fdb222dd-1396-4cdc-baf8\\\"},{\\\"root\\\":false,"
            + "\\\"taskName\\\":\\\"SQL脚本-2\\\",\\\"taskUniqueCode\\\":\\\"id-c541f1eb-81b8-4e10-921b\\\"},{\\\"root\\\":true,"
            + "\\\"taskName\\\":\\\"读数据表-1\\\",\\\"taskUniqueCode\\\":\\\"id-151c5da3-ad9c-4be7-8ac3\\\"}],\\\"workspaceId\\\":\\\"23620\\\"}\",\n"
            + "  \"extraContent\": \"{\\\"experimentId\\\":\\\"experiment-mwzpv9cbwjx0tc7gc3\\\",\\\"name\\\":\\\"haozhen-designer-001\\\","
            + "\\\"desc\\\":\\\"\\\"}\"\n"
            + "}";
        SpecNode node = new SpecNode();
        SpecScript scr = new SpecScript();
        SpecScriptRuntime rt = new SpecScriptRuntime();
        rt.setCommand(CodeProgramType.PAI_STUDIO.name());
        scr.setRuntime(rt);
        scr.setContent(content);
        node.setScript(scr);
        DataWorksNodeCodeAdapter codeAdapter = new DataWorksNodeCodeAdapter(node);
        String paiCode = codeAdapter.getCode();
        PaiFlowCode paiFlowCode = new PaiFlowCode().parse(paiCode);
        Assert.assertNotNull(paiFlowCode);
        Assert.assertNotNull(paiFlowCode.getAppId());
        Assert.assertNotNull(paiFlowCode.getComputeResource());
    }

    @Test
    public void testHologresDdl() {
        String content = "select 1";
        SpecNode node = new SpecNode();
        SpecScript scr = new SpecScript();
        SpecScriptRuntime rt = new SpecScriptRuntime();
        rt.setCommand(CodeProgramType.HOLOGRES_SYNC_DDL.name());
        scr.setRuntime(rt);
        scr.setContent(content);
        node.setScript(scr);
        DataWorksNodeCodeAdapter codeAdapter = new DataWorksNodeCodeAdapter(node);
        String code = codeAdapter.getCode();
        log.info("code: {}", code);
        Assert.assertEquals("select 1", code);

        DefaultJsonFormCode jsonFormCode = new DefaultJsonFormCode();
        jsonFormCode.setSourceCode(code);

        log.info("content: {}", jsonFormCode.getContent());
        Assert.assertNotNull(jsonFormCode.getContent());
        Assert.assertNotNull(jsonFormCode.getExtraContent());
    }

    @Test
    public void testOdpsSpark() {
        OdpsSparkCode odpsSparkCode = new OdpsSparkCode();
        odpsSparkCode.setResourceReferences(new ArrayList<>());
        odpsSparkCode.setSparkJson(new OdpsSparkCode.CodeJson());

        odpsSparkCode.getResourceReferences().add("xxxx.jar");
        odpsSparkCode.getResourceReferences().add("yyyy.zip");
        odpsSparkCode.getResourceReferences().add("zzzz.tar.gz");

        odpsSparkCode.getSparkJson().setMainJar("xxxx.jar");
        odpsSparkCode.getSparkJson().setMainClass("org.apache.spark.examples.JavaSparkPi");
        odpsSparkCode.getSparkJson().setVersion("2.x");
        odpsSparkCode.getSparkJson().setLanguage("java");
        odpsSparkCode.getSparkJson().setArgs("test_res_01");
        List<String> confs = new ArrayList<>();
        confs.add("spark.hadoop.odps.task.major.version=cupid_v2");
        confs.add("xxxxx=yyyy");
        odpsSparkCode.getSparkJson().setConfigs(confs);
        odpsSparkCode.getSparkJson().setAssistJars(Collections.singletonList("test_res_01"));
        odpsSparkCode.getSparkJson().setAssistFiles(Collections.singletonList("test.zip"));
        odpsSparkCode.getSparkJson().setAssistArchives(Collections.singletonList("test.zip"));
        odpsSparkCode.getSparkJson().setArchivesName(Collections.singletonList("test.zip"));

        String content = odpsSparkCode.getContent();
        SpecNode node = new SpecNode();
        SpecScript scr = new SpecScript();
        SpecScriptRuntime rt = new SpecScriptRuntime();
        rt.setCommand(CodeProgramType.ODPS_SPARK.name());
        scr.setRuntime(rt);
        scr.setContent(content);
        node.setScript(scr);
        DataWorksNodeCodeAdapter codeAdapter = new DataWorksNodeCodeAdapter(node);
        String code = codeAdapter.getCode();
        log.info("content: {}", code);
        Assert.assertTrue(StringUtils.indexOf(code, "##@resource_reference{\"yyyy.zip\"}") >= 0);
        Assert.assertTrue(StringUtils.indexOf(code, "##@resource_reference{\"zzzz.tar.gz\"}") >= 0);

        CodeModel<OdpsSparkCode> parsed = CodeModelFactory.getCodeModel(CodeProgramType.ODPS_SPARK.name(), code);
        OdpsSparkCode sparkCode = parsed.getCodeModel();
        Assert.assertNotNull(sparkCode);
        Assert.assertNotNull(sparkCode.getResourceReferences());
        Assert.assertEquals(3, CollectionUtils.size(sparkCode.getResourceReferences()));
        Assert.assertEquals("xxxx.jar", sparkCode.getSparkJson().getMainJar());
        Assert.assertEquals("org.apache.spark.examples.JavaSparkPi", sparkCode.getSparkJson().getMainClass());
        Assert.assertEquals("2.x", sparkCode.getSparkJson().getVersion());
        Assert.assertEquals("java", sparkCode.getSparkJson().getLanguage());
        Assert.assertEquals("test_res_01", sparkCode.getSparkJson().getArgs());
        Assert.assertEquals(2, CollectionUtils.size(sparkCode.getSparkJson().getConfigs()));
        Assert.assertEquals(1, CollectionUtils.size(sparkCode.getSparkJson().getAssistJars()));
        Assert.assertEquals(1, CollectionUtils.size(sparkCode.getSparkJson().getAssistFiles()));
        Assert.assertEquals(1, CollectionUtils.size(sparkCode.getSparkJson().getAssistArchives()));
        Assert.assertEquals(1, CollectionUtils.size(sparkCode.getSparkJson().getArchivesName()));
        Assert.assertEquals("test.zip", sparkCode.getSparkJson().getArchivesName().get(0));
        Assert.assertEquals("test.zip", sparkCode.getSparkJson().getAssistArchives().get(0));
        Assert.assertEquals("test.zip", sparkCode.getSparkJson().getAssistFiles().get(0));
        Assert.assertEquals("test_res_01", sparkCode.getSparkJson().getAssistJars().get(0));
        Assert.assertEquals("test_res_01", sparkCode.getSparkJson().getArgs());
        Assert.assertEquals("spark.hadoop.odps.task.major.version=cupid_v2", sparkCode.getSparkJson().getConfigs().get(0));
        Assert.assertEquals("xxxxx=yyyy", sparkCode.getSparkJson().getConfigs().get(1));
    }
}
