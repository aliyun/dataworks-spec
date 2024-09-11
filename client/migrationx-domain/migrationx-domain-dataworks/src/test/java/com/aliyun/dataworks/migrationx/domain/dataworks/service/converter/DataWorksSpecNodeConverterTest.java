package com.aliyun.dataworks.migrationx.domain.dataworks.service.converter;

import java.util.Collections;

import com.alibaba.fastjson2.JSON;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDoWhile;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.client.FileDetail;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v5.DataSnapshot;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v5.DataSnapshot.DataSnapshotContent;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 戒迷
 * @date 2024/4/16
 */
@Slf4j
public class DataWorksSpecNodeConverterTest {

    @Test
    public void testHandleNodeSpec() throws Exception {
        String specStr = "{\n"
            + "\t\"version\":\"1.1.0\",\n"
            + "\t\"kind\":\"CycleWorkflow\",\n"
            + "\t\"spec\":{\n"
            + "\t\t\"nodes\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"recurrence\":\"Normal\",\n"
            + "\t\t\t\t\"id\":\"7031136461380012389\",\n"
            + "\t\t\t\t\"timeout\":0,\n"
            + "\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\"rerunTimes\":3,\n"
            + "\t\t\t\t\"rerunInterval\":180000,\n"
            + "\t\t\t\t\"datasource\":{\n"
            + "\t\t\t\t\t\"name\":\"test_current_account_hadoop\",\n"
            + "\t\t\t\t\t\"type\":\"emr\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\"language\":\"hive-sql\",\n"
            + "\t\t\t\t\t\"path\":\"212112/hive01\",\n"
            + "\t\t\t\t\t\"content\":\"--EMR Hive SQL\n"
            + "--********************************************************************--\n"
            + "--author: dw_on_emr_qa3@test.aliyunid.com\n"
            + "--create time: 2024-09-10 11:07:44\n"
            + "--EMR任务只能运行在独享资源组上\n"
            + "--********************************************************************--\n"
            + "select 2;\",\n"
            + "\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\"command\":\"EMR_HIVE\",\n"
            + "\t\t\t\t\t\t\"commandTypeId\":227,\n"
            + "\t\t\t\t\t\t\"emrJobConfig\":{\n"
            + "\t\t\t\t\t\t\t\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"id\":\"7064766281846260070\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\"type\":\"Scheduler\",\n"
            + "\t\t\t\t\t\"id\":\"8439753760239652464\",\n"
            + "\t\t\t\t\t\"cron\":\"00 24 00 * * ?\",\n"
            + "\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"endTime\":\"9999-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"timezone\":\"Asia/Shanghai\",\n"
            + "\t\t\t\t\t\"delaySeconds\":0\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"runtimeResource\":{\n"
            + "\t\t\t\t\t\"resourceGroup\":\"S_res_group_524257424564736_1710147121495\",\n"
            + "\t\t\t\t\t\"id\":\"8717954356554596115\",\n"
            + "\t\t\t\t\t\"resourceGroupId\":\"67614320\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"name\":\"hive01\",\n"
            + "\t\t\t\t\"owner\":\"1107550004253538\",\n"
            + "\t\t\t\t\"metadata\":{\n"
            + "\t\t\t\t\t\"owner\":\"1107550004253538\",\n"
            + "\t\t\t\t\t\"ownerName\":\"dw_on_emr_qa3@test.aliyunid.com\",\n"
            + "\t\t\t\t\t\"createTime\":\"2024-09-10 11:07:44\",\n"
            + "\t\t\t\t\t\"tenantId\":\"524257424564736\",\n"
            + "\t\t\t\t\t\"project\":{\n"
            + "\t\t\t\t\t\t\"mode\":\"SIMPLE\",\n"
            + "\t\t\t\t\t\t\"projectId\":\"289221\",\n"
            + "\t\t\t\t\t\t\"projectIdentifier\":\"emr_meta_test\",\n"
            + "\t\t\t\t\t\t\"projectName\":\"EMR 元数据测试\",\n"
            + "\t\t\t\t\t\t\"projectOwnerId\":\"1107550004253538\",\n"
            + "\t\t\t\t\t\t\"simple\":true,\n"
            + "\t\t\t\t\t\t\"tenantId\":\"524257424564736\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"projectId\":\"289221\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"emr_meta_test_root\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"7031136461380012389\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"refTableName\":\"hive01\",\n"
            + "\t\t\t\t\t\t\t\"isDefault\":true\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t}\n"
            + "\t\t\t}\n"
            + "\t\t],\n"
            + "\t\t\"flow\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"nodeId\":\"7031136461380012389\",\n"
            + "\t\t\t\t\"depends\":[\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"type\":\"Normal\",\n"
            + "\t\t\t\t\t\t\"output\":\"emr_meta_test_root\"\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t]\n"
            + "\t\t\t}\n"
            + "\t\t]\n"
            + "\t},\n"
            + "\t\"metadata\":{\n"
            + "\t\t\"gmtModified\":1725937675000,\n"
            + "\t\t\"uuid\":\"7031136461380012389\"\n"
            + "\t}\n"
            + "}";
        Specification<DataWorksWorkflowSpec> spec = SpecUtil.parseToDomain(specStr);
        FileDetail result = DataWorksSpecNodeConverter.nodeSpecToFileDetail(spec);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getFile());
        Assert.assertNotNull(result.getNodeCfg());
        Assert.assertEquals(NodeUseType.SCHEDULED.getValue(), (int)result.getFile().getUseType());
    }

    @Test
    public void testHandleNodeSpecManualNode() throws Exception {
        String specStr = "{\n"
            + "\t\"version\":\"1.1.0\",\n"
            + "\t\"kind\":\"ManualNode\",\n"
            + "\t\"spec\":{\n"
            + "\t\t\"nodes\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"recurrence\":\"Normal\",\n"
            + "\t\t\t\t\"id\":\"26248077\",\n"
            + "\t\t\t\t\"timeout\":0,\n"
            + "\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\"rerunTimes\":0,\n"
            + "\t\t\t\t\"rerunInterval\":120000,\n"
            + "\t\t\t\t\"datasource\":{\n"
            + "\t\t\t\t\t\"name\":\"odps_first\",\n"
            + "\t\t\t\t\t\"type\":\"odps\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\"path\":\"业务流程/建模引擎/MaxCompute/数据开发/config_driver数据同步/model_table\",\n"
            + "\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\"command\":\"ODPS_SQL\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"parameters\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"name\":\"bizdate\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"Variable\",\n"
            + "\t\t\t\t\t\t\t\"scope\":\"NodeParameter\",\n"
            + "\t\t\t\t\t\t\t\"type\":\"System\",\n"
            + "\t\t\t\t\t\t\t\"value\":\"$[yyyymmdd-1]\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\"type\":\"Scheduler\",\n"
            + "\t\t\t\t\t\"cron\":\"00 29 00 * * ?\",\n"
            + "\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"endTime\":\"9999-01-01 15:12:51\",\n"
            + "\t\t\t\t\t\"timezone\":\"Asia/Shanghai\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"runtimeResource\":{\n"
            + "\t\t\t\t\t\"resourceGroup\":\"group_20051853\",\n"
            + "\t\t\t\t\t\"resourceGroupId\":\"20051853\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"name\":\"model_table\",\n"
            + "\t\t\t\t\"owner\":\"370260\",\n"
            + "\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"dataworks_meta.dwd_base_config_driver_data_jsondata_df\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"dataworks_analyze.26248077_out\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"dataworks_analyze.model_table_config_driver\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t}\n"
            + "\t\t\t}\n"
            + "\t\t],\n"
            + "\t\t\"flow\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"nodeId\":\"26248077\",\n"
            + "\t\t\t\t\"depends\":[\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"type\":\"Normal\",\n"
            + "\t\t\t\t\t\t\"output\":\"dataworks_meta.dwd_base_config_driver_data_jsondata_df\"\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t]\n"
            + "\t\t\t}\n"
            + "\t\t]\n"
            + "\t}\n"
            + "}";
        Specification<DataWorksWorkflowSpec> spec = SpecUtil.parseToDomain(specStr);
        FileDetail result = DataWorksSpecNodeConverter.nodeSpecToFileDetail(spec);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getFile());
        Assert.assertNotNull(result.getNodeCfg());
        Assert.assertEquals(NodeUseType.MANUAL.getValue(), (int)result.getFile().getUseType());
    }

    @Test
    public void testHandleNodeSpecManualWorkflow() throws Exception {
        String specStr = "{\n"
            + "\t\"version\":\"1.1.0\",\n"
            + "\t\"kind\":\"ManualWorkflow\",\n"
            + "\t\"spec\":{\n"
            + "\t\t\"nodes\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"recurrence\":\"Normal\",\n"
            + "\t\t\t\t\"id\":\"26248077\",\n"
            + "\t\t\t\t\"timeout\":0,\n"
            + "\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\"rerunTimes\":0,\n"
            + "\t\t\t\t\"rerunInterval\":120000,\n"
            + "\t\t\t\t\"datasource\":{\n"
            + "\t\t\t\t\t\"name\":\"odps_first\",\n"
            + "\t\t\t\t\t\"type\":\"odps\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\"path\":\"业务流程/建模引擎/MaxCompute/数据开发/config_driver数据同步/model_table\",\n"
            + "\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\"command\":\"ODPS_SQL\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"parameters\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"name\":\"bizdate\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"Variable\",\n"
            + "\t\t\t\t\t\t\t\"scope\":\"NodeParameter\",\n"
            + "\t\t\t\t\t\t\t\"type\":\"System\",\n"
            + "\t\t\t\t\t\t\t\"value\":\"$[yyyymmdd-1]\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\"type\":\"Scheduler\",\n"
            + "\t\t\t\t\t\"cron\":\"00 29 00 * * ?\",\n"
            + "\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"endTime\":\"9999-01-01 15:12:51\",\n"
            + "\t\t\t\t\t\"timezone\":\"Asia/Shanghai\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"runtimeResource\":{\n"
            + "\t\t\t\t\t\"resourceGroup\":\"group_20051853\",\n"
            + "\t\t\t\t\t\"resourceGroupId\":\"20051853\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"name\":\"model_table\",\n"
            + "\t\t\t\t\"owner\":\"370260\",\n"
            + "\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"dataworks_meta.dwd_base_config_driver_data_jsondata_df\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"dataworks_analyze.26248077_out\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"dataworks_analyze.model_table_config_driver\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t}\n"
            + "\t\t\t}\n"
            + "\t\t],\n"
            + "\t\t\"flow\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"nodeId\":\"26248077\",\n"
            + "\t\t\t\t\"depends\":[\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"type\":\"Normal\",\n"
            + "\t\t\t\t\t\t\"output\":\"dataworks_meta.dwd_base_config_driver_data_jsondata_df\"\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t]\n"
            + "\t\t\t}\n"
            + "\t\t]\n"
            + "\t}\n"
            + "}";
        Specification<DataWorksWorkflowSpec> spec = SpecUtil.parseToDomain(specStr);
        FileDetail result = DataWorksSpecNodeConverter.nodeSpecToFileDetail(spec);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getFile());
        Assert.assertNotNull(result.getNodeCfg());
        Assert.assertEquals(NodeUseType.MANUAL_WORKFLOW.getValue(), (int)result.getFile().getUseType());
    }

    @Test
    public void testHandleResourceSpec() throws Exception {
        String specStr = "{\n"
            + "  \"version\":\"1.1.0\",\n"
            + "  \"kind\":\"CycleWorkflow\",\n"
            + "  \"spec\":{\n"
            + "    \"fileResources\":[\n"
            + "      {\n"
            + "        \"name\":\"mc.py\",\n"
            + "        \"id\":\"6300484767235409791\",\n"
            + "        \"script\":{\n"
            + "          \"path\":\"戒迷/资源/mc.py\",\n"
            + "          \"runtime\":{\n"
            + "            \"command\":\"ODPS_PYTHON\"\n"
            + "          }\n"
            + "        },\n"
            + "        \"runtimeResource\":{\n"
            + "          \"id\":\"5623679673296125496\",\n"
            + "          \"resourceGroup\":\"group_2\",\n"
            + "          \"resourceGroupId\":\"2\"\n"
            + "        },\n"
            + "        \"type\":\"python\",\n"
            + "        \"file\":{\n"
            + "          \"storage\":{\n"
            + "             \"type\": \"oss\"\n"
            + "          }\n"
            + "        },\n"
            + "        \"datasource\":{\n"
            + "          \"name\":\"odps_first\",\n"
            + "          \"type\":\"odps\"\n"
            + "        },\n"
            + "        \"metadata\":{\n"
            + "          \"owner\":\"370260\"\n"
            + "        }\n"
            + "      }\n"
            + "    ]\n"
            + "  }\n"
            + "}";
        Specification<DataWorksWorkflowSpec> spec = SpecUtil.parseToDomain(specStr);
        FileDetail result = DataWorksSpecNodeConverter.resourceSpecToFileDetail(spec);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getFile());
        Assert.assertNotNull(result.getNodeCfg());
    }

    @Test
    public void testHandleFunction() throws Exception {
        String specStr = "{\n"
            + "  \"version\":\"1.1.0\",\n"
            + "  \"kind\":\"CycleWorkflow\",\n"
            + "  \"spec\":{\n"
            + "    \"functions\":[\n"
            + "      {\n"
            + "        \"name\":\"odps_function\",\n"
            + "        \"id\":\"6615080895197716196\",\n"
            + "        \"script\":{\n"
            + "          \"path\":\"戒迷/函数/odps_function\",\n"
            + "          \"runtime\":{\n"
            + "            \"command\":\"ODPS_FUNCTION\"\n"
            + "          }\n"
            + "        },\n"
            + "        \"type\":\"other\",\n"
            + "        \"className\":\"main\",\n"
            + "        \"datasource\":{\n"
            + "          \"name\":\"odps_first\",\n"
            + "          \"type\":\"odps\"\n"
            + "        },\n"
            + "        \"runtimeResource\":{\n"
            + "          \"resourceGroup\":\"group_2\",\n"
            + "          \"id\":\"5623679673296125496\",\n"
            + "          \"resourceGroupId\":\"2\"\n"
            + "        },\n"
            + "        \"resourceType\":\"file\",\n"
            + "        \"metadata\":{\n"
            + "          \"owner\":\"370260\"\n"
            + "        },\n"
            + "        \"fileResources\":[\n"
            + "          {\n"
            + "            \"name\":\"mc.py\"\n"
            + "          }\n"
            + "        ]\n"
            + "      }\n"
            + "    ]\n"
            + "  }\n"
            + "}";
        Specification<DataWorksWorkflowSpec> spec = SpecUtil.parseToDomain(specStr);
        FileDetail result = DataWorksSpecNodeConverter.functionSpecToFileDetail(spec);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getFile());
        Assert.assertNotNull(result.getNodeCfg());
    }

    @Test
    public void testDataSnapshotToFileDetail() {
        String specStr = "{\n"
            + "\t\"version\":\"1.1.0\",\n"
            + "\t\"kind\":\"ManualWorkflow\",\n"
            + "\t\"spec\":{\n"
            + "\t\t\"nodes\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"recurrence\":\"Normal\",\n"
            + "\t\t\t\t\"id\":\"26248077\",\n"
            + "\t\t\t\t\"timeout\":0,\n"
            + "\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\"rerunTimes\":0,\n"
            + "\t\t\t\t\"rerunInterval\":120000,\n"
            + "\t\t\t\t\"datasource\":{\n"
            + "\t\t\t\t\t\"name\":\"odps_first\",\n"
            + "\t\t\t\t\t\"type\":\"odps\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\"path\":\"业务流程/建模引擎/MaxCompute/数据开发/config_driver数据同步/model_table\",\n"
            + "\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\"command\":\"ODPS_SQL\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"parameters\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"name\":\"bizdate\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"Variable\",\n"
            + "\t\t\t\t\t\t\t\"scope\":\"NodeParameter\",\n"
            + "\t\t\t\t\t\t\t\"type\":\"System\",\n"
            + "\t\t\t\t\t\t\t\"value\":\"$[yyyymmdd-1]\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\"type\":\"Scheduler\",\n"
            + "\t\t\t\t\t\"cron\":\"00 29 00 * * ?\",\n"
            + "\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"endTime\":\"9999-01-01 15:12:51\",\n"
            + "\t\t\t\t\t\"timezone\":\"Asia/Shanghai\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"runtimeResource\":{\n"
            + "\t\t\t\t\t\"resourceGroup\":\"group_20051853\",\n"
            + "\t\t\t\t\t\"resourceGroupId\":\"20051853\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"name\":\"model_table\",\n"
            + "\t\t\t\t\"owner\":\"370260\",\n"
            + "\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"dataworks_meta.dwd_base_config_driver_data_jsondata_df\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"dataworks_analyze.26248077_out\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"dataworks_analyze.model_table_config_driver\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t}\n"
            + "\t\t\t}\n"
            + "\t\t],\n"
            + "\t\t\"flow\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"nodeId\":\"26248077\",\n"
            + "\t\t\t\t\"depends\":[\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"type\":\"Normal\",\n"
            + "\t\t\t\t\t\t\"output\":\"dataworks_meta.dwd_base_config_driver_data_jsondata_df\"\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t]\n"
            + "\t\t\t}\n"
            + "\t\t]\n"
            + "\t}\n"
            + "}";
        DataSnapshotContent content = DataSnapshotContent.builder()
            .content("select 1")
            .spec(specStr)
            .build();
        DataSnapshot snapshot = DataSnapshot.builder()
            .content(JSON.toJSONString(content))
            .build();
        FileDetail fileDetail = DataWorksSpecNodeConverter.snapshotContentToFileDetail(snapshot);
        Assert.assertNotNull(fileDetail);
        Assert.assertNotNull(fileDetail.getFile());
        Assert.assertEquals(10, (int)fileDetail.getFile().getFileType());
        Assert.assertEquals(content.getContent(), fileDetail.getFile().getContent());
        Assert.assertNotNull(fileDetail.getNodeCfg());
    }

    @Test
    public void testDataSnapshotComponentToFileDetail() {
        String specStr = "{\n"
            + "\t\"version\":\"1.1.0\",\n"
            + "\t\"kind\":\"Component\",\n"
            + "\t\"spec\":{\n"
            + "\t\t\"components\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"name\":\"test_sql_component\",\n"
            + "\t\t\t\t\"id\":\"5640313746029937468\",\n"
            + "\t\t\t\t\"owner\":\"453125\",\n"
            + "\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\"id\":\"5206893221480063330\",\n"
            + "\t\t\t\t\t\"language\":\"odps-sql\",\n"
            + "\t\t\t\t\t\"path\":\"test_sql_component\",\n"
            + "\t\t\t\t\t\"content\":\"select 1;\",\n"
            + "\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\"command\":\"SQL_COMPONENT\",\n"
            + "\t\t\t\t\t\t\"commandTypeId\":3010\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t}\n"
            + "\t\t\t}\n"
            + "\t\t]\n"
            + "\t}\n"
            + "}";

        Specification<DataWorksWorkflowSpec> spec = SpecUtil.parseToDomain(specStr);
        FileDetail fileDetail = DataWorksSpecNodeConverter.componentSpecToFileDetail(spec);
        Assert.assertNotNull(fileDetail);
        Assert.assertNotNull(fileDetail.getFile());
        log.info("file: {}", JSON.toJSONString(fileDetail.getFile()));
        Assert.assertEquals(30, (int)fileDetail.getFile().getUseType());
        Assert.assertEquals(3010, (int)fileDetail.getFile().getFileType());
    }

    @Test
    public void testWorkflowInnerNodeMatchCase() {
        Specification<DataWorksWorkflowSpec> specification = new Specification<>();
        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        SpecWorkflow wf = new SpecWorkflow();
        wf.setId("wf1");
        SpecNode dowhile = new SpecNode();
        dowhile.setId("dowhile1");
        SpecDoWhile dowhileDef = new SpecDoWhile();
        SpecNode dowhileInner1 = new SpecNode();
        dowhileInner1.setId("dowhileInner1");
        dowhileDef.setNodes(Collections.singletonList(dowhileInner1));
        dowhile.setDoWhile(dowhileDef);
        wf.setNodes(Collections.singletonList(dowhile));
        spec.setWorkflows(Collections.singletonList(wf));
        specification.setSpec(spec);

        Assert.assertNotNull(DataWorksSpecNodeConverter.getMatchSpecNode(specification.getSpec(), "dowhileInner1"));
        Assert.assertEquals(dowhileInner1.getId(), DataWorksSpecNodeConverter.getMatchSpecNode(specification.getSpec(), "dowhileInner1").getId());
        Assert.assertEquals(dowhile.getId(), DataWorksSpecNodeConverter.getMatchSpecNode(specification.getSpec(), "dowhile1").getId());
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme