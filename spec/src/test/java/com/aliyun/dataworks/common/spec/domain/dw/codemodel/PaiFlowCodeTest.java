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

import java.util.ArrayList;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/3/21
 */
@Slf4j
public class PaiFlowCodeTest {
    @Test
    public void testPaiFlow() {
        String code = "{\n"
            + "    \"appId\": 14255,\n"
            + "    \"computeResource\": {\n"
            + "        \"MaxCompute\": \"execution_maxcompute\"\n"
            + "    },\n"
            + "    \"flowUniqueCode\": \"62ce03e4-db31-4cb4-97f2-4e753fa47596\",\n"
            + "    \"inputs\": [\n"
            + "        {\n"
            + "            \"type\": \"MaxComputeTable\",\n"
            + "            \"value\": \"autotest._dw_user_info_all_d\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"type\": \"MaxComputeTable\",\n"
            + "            \"value\": \"autotest.t_d2_tt_test\"\n"
            + "        }\n"
            + "    ],\n"
            + "    \"name\": \"test_pai_designer_0\",\n"
            + "    \"outputs\": [\n"
            + "        {\n"
            + "            \"type\": \"MaxComputeTable\",\n"
            + "            \"value\": \"auotest.test_pai_designer_0_output_table_0\"\n"
            + "        }\n"
            + "    ],\n"
            + "    \"paiflowArguments\": \"---\\narguments:\\n  parameters:\\n  - name: \\\"execution_maxcompute\\\"\\n    value:\\n      endpoint:"
            + " \\\"http://service.odps.aliyun-inc.com/api\\\"\\n      odpsProject: \\\"autotest_dev\\\"\\n      spec:\\n        endpoint: "
            + "\\\"http://service.odps.aliyun-inc.com/api\\\"\\n        odpsProject: \\\"autotest_dev\\\"\\n      resourceType: "
            + "\\\"MaxCompute\\\"\\n\",\n"
            + "    \"paiflowParameters\": {\n"
            + "        \"global_var1\": \"${bizdate}\"\n"
            + "    },\n"
            + "    \"paiflowPipeline\": \"---\\napiVersion: \\\"core/v1\\\"\\nmetadata:\\n  provider: \\\"064152\\\"\\n  version: \\\"v1\\\"\\n  "
            + "identifier: \\\"job-root-pipeline-identifier\\\"\\n  annotations: {}\\nspec:\\n  inputs:\\n    artifacts: []\\n    parameters:\\n   "
            + " - name: \\\"execution_maxcompute\\\"\\n      type: \\\"Map\\\"\\n  arguments:\\n    artifacts: []\\n    parameters: []\\n  "
            + "dependencies: []\\n  initContainers: []\\n  sideCarContainers: []\\n  pipelines:\\n  - apiVersion: \\\"core/v1\\\"\\n    "
            + "metadata:\\n      provider: \\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: \\\"data_source\\\"\\n      name: "
            + "\\\"id-23839169-9567-4dc3-92cc\\\"\\n      displayName: \\\"读数据表-1\\\"\\n      annotations: {}\\n    spec:\\n      arguments:\\n    "
            + "    artifacts: []\\n        parameters:\\n        - name: \\\"inputTableName\\\"\\n          value: \\\"autotest.t_d2_tt_test\\\"\\n"
            + "        - name: \\\"hasPartition\\\"\\n          value: \\\"false\\\"\\n        - name: \\\"execution\\\"\\n          from: "
            + "\\\"{{inputs.parameters.execution_maxcompute}}\\\"\\n      dependencies: []\\n      initContainers: []\\n      sideCarContainers: "
            + "[]\\n      pipelines: []\\n      volumes: []\\n  - apiVersion: \\\"core/v1\\\"\\n    metadata:\\n      provider: \\\"pai\\\"\\n     "
            + " version: \\\"v1\\\"\\n      identifier: \\\"AppendColumns\\\"\\n      name: \\\"id-86b34bbb-b696-4557-9f48\\\"\\n      displayName:"
            + " \\\"合并列-1\\\"\\n      annotations: {}\\n    spec:\\n      arguments:\\n        artifacts:\\n        - name: \\\"leftTable\\\"\\n   "
            + "       from: \\\"{{pipelines.id-23839169-9567-4dc3-92cc.outputs.artifacts.outputTable}}\\\"\\n        - name: \\\"rightTable\\\"\\n "
            + "         from: \\\"{{pipelines.id-cd6fa98e-4fb1-44c2-a5d7.outputs.artifacts.outputTable}}\\\"\\n        parameters:\\n        - "
            + "name: \\\"leftColumns\\\"\\n          value: \\\"a:a:STRING\\\"\\n        - name: \\\"rightColumns\\\"\\n          value: "
            + "\\\"uid:uid:STRING,gender:gender:STRING,age_range:age_range:STRING\\\"\\n        - name: \\\"execution\\\"\\n          from: "
            + "\\\"{{inputs.parameters.execution_maxcompute}}\\\"\\n      dependencies:\\n      - \\\"id-23839169-9567-4dc3-92cc\\\"\\n      - "
            + "\\\"id-cd6fa98e-4fb1-44c2-a5d7\\\"\\n      initContainers: []\\n      sideCarContainers: []\\n      pipelines: []\\n      volumes: "
            + "[]\\n  - apiVersion: \\\"core/v1\\\"\\n    metadata:\\n      provider: \\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: "
            + "\\\"data_source\\\"\\n      name: \\\"id-cd6fa98e-4fb1-44c2-a5d7\\\"\\n      displayName: \\\"读数据表-2\\\"\\n      annotations: {}\\n "
            + "   spec:\\n      arguments:\\n        artifacts: []\\n        parameters:\\n        - name: \\\"inputTableName\\\"\\n          "
            + "value: \\\"autotest._dw_user_info_all_d\\\"\\n        - name: \\\"hasPartition\\\"\\n          value: \\\"false\\\"\\n        - "
            + "name: \\\"execution\\\"\\n          from: \\\"{{inputs.parameters.execution_maxcompute}}\\\"\\n      dependencies: []\\n      "
            + "initContainers: []\\n      sideCarContainers: []\\n      pipelines: []\\n      volumes: []\\n  - apiVersion: \\\"core/v1\\\"\\n    "
            + "metadata:\\n      provider: \\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: \\\"SqlWriteTable_1\\\"\\n      name: "
            + "\\\"id-afc64ca0-20d5-49fb-bbbd\\\"\\n      displayName: \\\"写数据表-1\\\"\\n      annotations: {}\\n    spec:\\n      arguments:\\n    "
            + "    artifacts:\\n        - name: \\\"inputTable\\\"\\n          from: \\\"{{pipelines.id-86b34bbb-b696-4557-9f48.outputs.artifacts"
            + ".outputTable}}\\\"\\n        parameters:\\n        - name: \\\"outputTableName\\\"\\n          value: \\\"auotest"
            + ".test_pai_designer_0_output_table_0\\\"\\n        - name: \\\"hasPartition\\\"\\n          value: \\\"true\\\"\\n        - name: "
            + "\\\"outputTablePartition\\\"\\n          value: \\\"dt=${global_var1}\\\"\\n        - name: \\\"execution\\\"\\n          from: "
            + "\\\"{{inputs.parameters.execution_maxcompute}}\\\"\\n      dependencies:\\n      - \\\"id-86b34bbb-b696-4557-9f48\\\"\\n      "
            + "initContainers: []\\n      sideCarContainers: []\\n      pipelines: []\\n      volumes: []\\n  volumes: []\\n\",\n"
            + "    \"paraValue\": \"--paiflow_endpoint=paiflowinner-share.aliyuncs.com --region=inner\",\n"
            + "    \"prgType\": \"1000138\",\n"
            + "    \"requestId\": \"F601815A-C2E4-50FD-96D9-FBF0625EBCE1\",\n"
            + "    \"taskRelations\": [\n"
            + "        {\n"
            + "            \"childTaskUniqueCode\": \"id-86b34bbb-b696-4557-9f48\",\n"
            + "            \"parentTaskUniqueCode\": \"id-23839169-9567-4dc3-92cc\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"childTaskUniqueCode\": \"id-86b34bbb-b696-4557-9f48\",\n"
            + "            \"parentTaskUniqueCode\": \"id-cd6fa98e-4fb1-44c2-a5d7\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"childTaskUniqueCode\": \"id-afc64ca0-20d5-49fb-bbbd\",\n"
            + "            \"parentTaskUniqueCode\": \"id-86b34bbb-b696-4557-9f48\"\n"
            + "        }\n"
            + "    ],\n"
            + "    \"tasks\": [\n"
            + "        {\n"
            + "            \"root\": true,\n"
            + "            \"taskName\": \"读数据表-1\",\n"
            + "            \"taskUniqueCode\": \"id-23839169-9567-4dc3-92cc\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"root\": false,\n"
            + "            \"taskName\": \"合并列-1\",\n"
            + "            \"taskUniqueCode\": \"id-86b34bbb-b696-4557-9f48\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"root\": true,\n"
            + "            \"taskName\": \"读数据表-2\",\n"
            + "            \"taskUniqueCode\": \"id-cd6fa98e-4fb1-44c2-a5d7\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"root\": false,\n"
            + "            \"taskName\": \"写数据表-1\",\n"
            + "            \"taskUniqueCode\": \"id-afc64ca0-20d5-49fb-bbbd\"\n"
            + "        }\n"
            + "    ],\n"
            + "    \"workspaceId\": \"14255\"\n"
            + "}";
        CodeModel<Code> m
            = CodeModelFactory.getCodeModel(CodeProgramType.PAI_STUDIO.name(), code);
        ((PaiFlowCode)m.getCodeModel()).setTasks(new ArrayList<>());
        log.info("code: " + m.getContent());
        Specification<DataWorksWorkflowSpec> spec = ((PaiFlowCode)m.getCodeModel()).getSpec();
        Object json = SpecUtil.write(spec, new SpecWriterContext());
        log.info("spec: {}", JSON.toJSONString(json, Feature.PrettyFormat));
    }

    @Test
    public void testPaiDesigner() {
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
        CodeModel<Code> code = CodeModelFactory.getCodeModel(CodeProgramType.PAI_STUDIO.name(), null);
        code.setSourceCode(content);
        Assert.assertNotNull(code);
        PaiFlowCode codeModel = (PaiFlowCode)code.getCodeModel();
        Assert.assertNotNull(codeModel);
        Assert.assertNotNull(codeModel.getExtraContent());
        log.info("content: {}", codeModel.getContent());
        log.info("extraContent: {}", codeModel.getExtraContent());
        PaiFlowCode paiFlowCode = JSON.parseObject(codeModel.getContent(), PaiFlowCode.class);
        Assert.assertNotNull(paiFlowCode);
        Assert.assertEquals(23620, (long)paiFlowCode.getAppId());
        Assert.assertNotNull(paiFlowCode.getComputeResource());
    }

    @Test
    public void testPaiDesignerCompatible() {
        String content
            = "{\"appId\":23620,\"computeResource\":{\"MaxCompute\":\"execution_maxcompute\"},"
            + "\"flowUniqueCode\":\"2e8838a9-3eaf-44c6-aca4-44629e98bcbf\",\"inputs\":[{\"type\":\"MaxComputeTable\",\"value\":\"pai_inner_project"
            + ".wumai_data\"}],\"name\":\"雾霾天气预测0000\",\"outputs\":[],\"paiflowArguments\":\"---\\narguments:\\n  parameters:\\n  - name: "
            + "\\\"execution_maxcompute\\\"\\n    value:\\n      endpoint: \\\"http://service.odps.aliyun-inc.com/api\\\"\\n      odpsProject: "
            + "\\\"dw_scheduler_pre_dev\\\"\\n      spec:\\n        endpoint: \\\"http://service.odps.aliyun-inc.com/api\\\"\\n        odpsProject:"
            + " \\\"dw_scheduler_pre_dev\\\"\\n      resourceType: \\\"MaxCompute\\\"\\n\",\"paiflowParameters\":{},"
            + "\"paiflowPipeline\":\"---\\napiVersion: \\\"core/v1\\\"\\nmetadata:\\n  provider: \\\"085941\\\"\\n  version: \\\"v1\\\"\\n  "
            + "identifier: \\\"job-root-pipeline-identifier\\\"\\n  annotations: {}\\nspec:\\n  inputs:\\n    artifacts: []\\n    parameters:\\n   "
            + " - name: \\\"execution_maxcompute\\\"\\n      type: \\\"Map\\\"\\n  arguments:\\n    artifacts: []\\n    parameters: []\\n  "
            + "dependencies: []\\n  initContainers: []\\n  sideCarContainers: []\\n  pipelines:\\n  - apiVersion: \\\"core/v1\\\"\\n    "
            + "metadata:\\n      provider: \\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: \\\"logisticregression_binary\\\"\\n      "
            + "name: \\\"id-9531-1608891073887-61143\\\"\\n      displayName: \\\"逻辑回归二分类\\\"\\n      annotations: {}\\n    spec:\\n      "
            + "arguments:\\n        artifacts:\\n        - name: \\\"inputTable\\\"\\n          from: \\\"{{pipelines.id-85ae-1608984392467-81946"
            + ".outputs.artifacts.output1Table}}\\\"\\n        parameters:\\n        - name: \\\"featureColNames\\\"\\n          value: \\\"pm10,"
            + "so2,co,no2\\\"\\n        - name: \\\"labelColName\\\"\\n          value: \\\"_c2\\\"\\n        - name: \\\"goodValue\\\"\\n         "
            + " value: \\\"1\\\"\\n        - name: \\\"enableSparse\\\"\\n          value: \\\"false\\\"\\n        - name: \\\"generatePmml\\\"\\n "
            + "         value: false\\n        - name: \\\"regularizedType\\\"\\n          value: \\\"None\\\"\\n        - name: \\\"maxIter\\\"\\n"
            + "          value: \\\"100\\\"\\n        - name: \\\"regularizedLevel\\\"\\n          value: \\\"1\\\"\\n        - name: "
            + "\\\"epsilon\\\"\\n          value: \\\"0.000001\\\"\\n        - name: \\\"execution\\\"\\n          from: \\\"{{inputs.parameters"
            + ".execution_maxcompute}}\\\"\\n      dependencies:\\n      - \\\"id-85ae-1608984392467-81946\\\"\\n      initContainers: []\\n      "
            + "sideCarContainers: []\\n      pipelines: []\\n      volumes: []\\n  - apiVersion: \\\"core/v1\\\"\\n    metadata:\\n      provider: "
            + "\\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: \\\"random_forests_1\\\"\\n      name: "
            + "\\\"id-3d25-1608980864737-77856\\\"\\n      displayName: \\\"随机森林\\\"\\n      annotations: {}\\n    spec:\\n      arguments:\\n     "
            + "   artifacts:\\n        - name: \\\"inputTable\\\"\\n          from: \\\"{{pipelines.id-85ae-1608984392467-81946.outputs.artifacts"
            + ".output1Table}}\\\"\\n        parameters:\\n        - name: \\\"featureColNames\\\"\\n          value: \\\"pm10,so2,co,no2\\\"\\n   "
            + "     - name: \\\"labelColName\\\"\\n          value: \\\"_c2\\\"\\n        - name: \\\"generatePmml\\\"\\n          value: false\\n "
            + "       - name: \\\"treeNum\\\"\\n          value: \\\"100\\\"\\n        - name: \\\"minNumObj\\\"\\n          value: \\\"2\\\"\\n   "
            + "     - name: \\\"minNumPer\\\"\\n          value: \\\"0\\\"\\n        - name: \\\"maxRecordSize\\\"\\n          value: "
            + "\\\"100000\\\"\\n        - name: \\\"execution\\\"\\n          from: \\\"{{inputs.parameters.execution_maxcompute}}\\\"\\n      "
            + "dependencies:\\n      - \\\"id-85ae-1608984392467-81946\\\"\\n      initContainers: []\\n      sideCarContainers: []\\n      "
            + "pipelines: []\\n      volumes: []\\n  - apiVersion: \\\"core/v1\\\"\\n    metadata:\\n      provider: \\\"pai\\\"\\n      version: "
            + "\\\"v1\\\"\\n      identifier: \\\"type_transform\\\"\\n      name: \\\"id-2d88-1608982098027-91558\\\"\\n      displayName: "
            + "\\\"类型转换\\\"\\n      annotations: {}\\n    spec:\\n      arguments:\\n        artifacts:\\n        - name: \\\"inputTable\\\"\\n    "
            + "      from: \\\"{{pipelines.id-9309-1608992715826-47326.outputs.artifacts.outputTable}}\\\"\\n        parameters:\\n        - name: "
            + "\\\"cols_to_double\\\"\\n          value: \\\"time,hour,pm2,pm10,so2,co,no2\\\"\\n        - name: \\\"default_int_value\\\"\\n      "
            + "    value: \\\"0\\\"\\n        - name: \\\"reserveOldFeat\\\"\\n          value: \\\"false\\\"\\n        - name: "
            + "\\\"execution\\\"\\n          from: \\\"{{inputs.parameters.execution_maxcompute}}\\\"\\n      dependencies:\\n      - "
            + "\\\"id-9309-1608992715826-47326\\\"\\n      initContainers: []\\n      sideCarContainers: []\\n      pipelines: []\\n      volumes: "
            + "[]\\n  - apiVersion: \\\"core/v1\\\"\\n    metadata:\\n      provider: \\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: "
            + "\\\"fe_meta_runner\\\"\\n      name: \\\"id-2317-1608984201281-74996\\\"\\n      displayName: \\\"数据视图\\\"\\n      annotations: "
            + "{}\\n    spec:\\n      arguments:\\n        artifacts:\\n        - name: \\\"inputTable\\\"\\n          from: \\\"{{pipelines"
            + ".id-73af-1608994414936-19117.outputs.artifacts.outputTable}}\\\"\\n        parameters:\\n        - name: \\\"selectedCols\\\"\\n    "
            + "      value: \\\"pm10,so2,co,no2\\\"\\n        - name: \\\"labelCol\\\"\\n          value: \\\"_c2\\\"\\n        - name: "
            + "\\\"isSparse\\\"\\n          value: \\\"false\\\"\\n        - name: \\\"maxBins\\\"\\n          value: \\\"100\\\"\\n        - name:"
            + " \\\"execution\\\"\\n          from: \\\"{{inputs.parameters.execution_maxcompute}}\\\"\\n      dependencies:\\n      - "
            + "\\\"id-73af-1608994414936-19117\\\"\\n      initContainers: []\\n      sideCarContainers: []\\n      pipelines: []\\n      volumes: "
            + "[]\\n  - apiVersion: \\\"core/v1\\\"\\n    metadata:\\n      provider: \\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: "
            + "\\\"split\\\"\\n      name: \\\"id-85ae-1608984392467-81946\\\"\\n      displayName: \\\"拆分\\\"\\n      annotations: {}\\n    "
            + "spec:\\n      arguments:\\n        artifacts:\\n        - name: \\\"inputTable\\\"\\n          from: \\\"{{pipelines"
            + ".id-9869-1608986331830-78910.outputs.artifacts.outputTable}}\\\"\\n        parameters:\\n        - name: \\\"_splitMethod\\\"\\n    "
            + "      value: \\\"_fraction\\\"\\n        - name: \\\"fraction\\\"\\n          value: \\\"0.8\\\"\\n        - name: "
            + "\\\"_advanced\\\"\\n          value: \\\"false\\\"\\n        - name: \\\"execution\\\"\\n          from: \\\"{{inputs.parameters"
            + ".execution_maxcompute}}\\\"\\n      dependencies:\\n      - \\\"id-9869-1608986331830-78910\\\"\\n      initContainers: []\\n      "
            + "sideCarContainers: []\\n      pipelines: []\\n      volumes: []\\n  - apiVersion: \\\"core/v1\\\"\\n    metadata:\\n      provider: "
            + "\\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: \\\"Prediction_1\\\"\\n      name: "
            + "\\\"id-4191-1608984751235-85036\\\"\\n      displayName: \\\"预测\\\"\\n      annotations: {}\\n    spec:\\n      arguments:\\n       "
            + " artifacts:\\n        - name: \\\"model\\\"\\n          from: \\\"{{pipelines.id-3d25-1608980864737-77856.outputs.artifacts"
            + ".model}}\\\"\\n        - name: \\\"inputTable\\\"\\n          from: \\\"{{pipelines.id-85ae-1608984392467-81946.outputs.artifacts"
            + ".output2Table}}\\\"\\n        parameters:\\n        - name: \\\"featureColNames\\\"\\n          value: \\\"pm10,so2,co,no2\\\"\\n   "
            + "     - name: \\\"appendColNames\\\"\\n          value: \\\"time,hour,pm10,so2,co,no2,_c2\\\"\\n        - name: "
            + "\\\"resultColName\\\"\\n          value: \\\"prediction_result\\\"\\n        - name: \\\"scoreColName\\\"\\n          value: "
            + "\\\"prediction_score\\\"\\n        - name: \\\"detailColName\\\"\\n          value: \\\"prediction_detail\\\"\\n        - name: "
            + "\\\"enableSparse\\\"\\n          value: \\\"false\\\"\\n        - name: \\\"execution\\\"\\n          from: \\\"{{inputs.parameters"
            + ".execution_maxcompute}}\\\"\\n      dependencies:\\n      - \\\"id-3d25-1608980864737-77856\\\"\\n      - "
            + "\\\"id-85ae-1608984392467-81946\\\"\\n      initContainers: []\\n      sideCarContainers: []\\n      pipelines: []\\n      volumes: "
            + "[]\\n  - apiVersion: \\\"core/v1\\\"\\n    metadata:\\n      provider: \\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: "
            + "\\\"Prediction_1\\\"\\n      name: \\\"id-6b26-1608984755520-07203\\\"\\n      displayName: \\\"预测\\\"\\n      annotations: {}\\n   "
            + " spec:\\n      arguments:\\n        artifacts:\\n        - name: \\\"inputTable\\\"\\n          from: \\\"{{pipelines"
            + ".id-85ae-1608984392467-81946.outputs.artifacts.output2Table}}\\\"\\n        - name: \\\"model\\\"\\n          from: \\\"{{pipelines"
            + ".id-9531-1608891073887-61143.outputs.artifacts.model}}\\\"\\n        parameters:\\n        - name: \\\"featureColNames\\\"\\n       "
            + "   value: \\\"pm10,so2,co,no2\\\"\\n        - name: \\\"appendColNames\\\"\\n          value: \\\"time,hour,pm10,so2,co,no2,"
            + "_c2\\\"\\n        - name: \\\"resultColName\\\"\\n          value: \\\"prediction_result\\\"\\n        - name: "
            + "\\\"scoreColName\\\"\\n          value: \\\"prediction_score\\\"\\n        - name: \\\"detailColName\\\"\\n          value: "
            + "\\\"prediction_detail\\\"\\n        - name: \\\"enableSparse\\\"\\n          value: \\\"false\\\"\\n        - name: "
            + "\\\"execution\\\"\\n          from: \\\"{{inputs.parameters.execution_maxcompute}}\\\"\\n      dependencies:\\n      - "
            + "\\\"id-85ae-1608984392467-81946\\\"\\n      - \\\"id-9531-1608891073887-61143\\\"\\n      initContainers: []\\n      "
            + "sideCarContainers: []\\n      pipelines: []\\n      volumes: []\\n  - apiVersion: \\\"core/v1\\\"\\n    metadata:\\n      provider: "
            + "\\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: \\\"evaluate_1\\\"\\n      name: \\\"id-5dd4-1608984770722-54098\\\"\\n "
            + "     displayName: \\\"二分类评估\\\"\\n      annotations: {}\\n    spec:\\n      arguments:\\n        artifacts:\\n        - name: "
            + "\\\"inputTable\\\"\\n          from: \\\"{{pipelines.id-4191-1608984751235-85036.outputs.artifacts.outputTable}}\\\"\\n        "
            + "parameters:\\n        - name: \\\"labelColName\\\"\\n          value: \\\"_c2\\\"\\n        - name: \\\"scoreColName\\\"\\n         "
            + " value: \\\"prediction_score\\\"\\n        - name: \\\"positiveLabel\\\"\\n          value: \\\"1\\\"\\n        - name: "
            + "\\\"binCount\\\"\\n          value: \\\"1000\\\"\\n        - name: \\\"_advanced\\\"\\n          value: \\\"false\\\"\\n        - "
            + "name: \\\"execution\\\"\\n          from: \\\"{{inputs.parameters.execution_maxcompute}}\\\"\\n      dependencies:\\n      - "
            + "\\\"id-4191-1608984751235-85036\\\"\\n      initContainers: []\\n      sideCarContainers: []\\n      pipelines: []\\n      volumes: "
            + "[]\\n  - apiVersion: \\\"core/v1\\\"\\n    metadata:\\n      provider: \\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: "
            + "\\\"evaluate_1\\\"\\n      name: \\\"id-41c6-1608984773170-26269\\\"\\n      displayName: \\\"二分类评估\\\"\\n      annotations: {}\\n  "
            + "  spec:\\n      arguments:\\n        artifacts:\\n        - name: \\\"inputTable\\\"\\n          from: \\\"{{pipelines"
            + ".id-6b26-1608984755520-07203.outputs.artifacts.outputTable}}\\\"\\n        parameters:\\n        - name: \\\"labelColName\\\"\\n    "
            + "      value: \\\"_c2\\\"\\n        - name: \\\"scoreColName\\\"\\n          value: \\\"prediction_score\\\"\\n        - name: "
            + "\\\"positiveLabel\\\"\\n          value: \\\"1\\\"\\n        - name: \\\"binCount\\\"\\n          value: \\\"1000\\\"\\n        - "
            + "name: \\\"_advanced\\\"\\n          value: \\\"false\\\"\\n        - name: \\\"execution\\\"\\n          from: \\\"{{inputs"
            + ".parameters.execution_maxcompute}}\\\"\\n      dependencies:\\n      - \\\"id-6b26-1608984755520-07203\\\"\\n      initContainers: "
            + "[]\\n      sideCarContainers: []\\n      pipelines: []\\n      volumes: []\\n  - apiVersion: \\\"core/v1\\\"\\n    metadata:\\n     "
            + " provider: \\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: \\\"normalize_1\\\"\\n      name: "
            + "\\\"id-9869-1608986331830-78910\\\"\\n      displayName: \\\"归一化\\\"\\n      annotations: {}\\n    spec:\\n      arguments:\\n      "
            + "  artifacts:\\n        - name: \\\"inputTable\\\"\\n          from: \\\"{{pipelines.id-73af-1608994414936-19117.outputs.artifacts"
            + ".outputTable}}\\\"\\n        parameters:\\n        - name: \\\"selectedColNames\\\"\\n          value: \\\"pm10,so2,co,no2\\\"\\n   "
            + "     - name: \\\"keepOriginal\\\"\\n          value: \\\"false\\\"\\n        - name: \\\"execution\\\"\\n          from: "
            + "\\\"{{inputs.parameters.execution_maxcompute}}\\\"\\n      dependencies:\\n      - \\\"id-73af-1608994414936-19117\\\"\\n      "
            + "initContainers: []\\n      sideCarContainers: []\\n      pipelines: []\\n      volumes: []\\n  - apiVersion: \\\"core/v1\\\"\\n    "
            + "metadata:\\n      provider: \\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: \\\"data_source\\\"\\n      name: "
            + "\\\"id-9309-1608992715826-47326\\\"\\n      displayName: \\\"读数据源\\\"\\n      annotations: {}\\n    spec:\\n      arguments:\\n     "
            + "   artifacts: []\\n        parameters:\\n        - name: \\\"inputTableName\\\"\\n          value: \\\"pai_inner_project"
            + ".wumai_data\\\"\\n        - name: \\\"execution\\\"\\n          from: \\\"{{inputs.parameters.execution_maxcompute}}\\\"\\n      "
            + "dependencies: []\\n      initContainers: []\\n      sideCarContainers: []\\n      pipelines: []\\n      volumes: []\\n  - "
            + "apiVersion: \\\"core/v1\\\"\\n    metadata:\\n      provider: \\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: "
            + "\\\"sql\\\"\\n      name: \\\"id-73af-1608994414936-19117\\\"\\n      displayName: \\\"SQL组件\\\"\\n      annotations: {}\\n    "
            + "spec:\\n      arguments:\\n        artifacts:\\n        - name: \\\"inputTable1\\\"\\n          from: \\\"{{pipelines"
            + ".id-2d88-1608982098027-91558.outputs.artifacts.outputTable}}\\\"\\n        parameters:\\n        - name: \\\"sql\\\"\\n          "
            + "value: \\\"select time,hour,(case when pm2>200 then 1 else 0 end),pm10,so2,co,no2\\\\\\n            \\\\ from ${t1}\\\"\\n        - "
            + "name: \\\"execution\\\"\\n          from: \\\"{{inputs.parameters.execution_maxcompute}}\\\"\\n      dependencies:\\n      - "
            + "\\\"id-2d88-1608982098027-91558\\\"\\n      initContainers: []\\n      sideCarContainers: []\\n      pipelines: []\\n      volumes: "
            + "[]\\n  - apiVersion: \\\"core/v1\\\"\\n    metadata:\\n      provider: \\\"pai\\\"\\n      version: \\\"v1\\\"\\n      identifier: "
            + "\\\"histograms\\\"\\n      name: \\\"id-3906-1608996028711-50772\\\"\\n      displayName: \\\"直方图\\\"\\n      annotations: {}\\n    "
            + "spec:\\n      arguments:\\n        artifacts:\\n        - name: \\\"inputTable\\\"\\n          from: \\\"{{pipelines"
            + ".id-2d88-1608982098027-91558.outputs.artifacts.outputTable}}\\\"\\n        parameters:\\n        - name: \\\"selectedColNames\\\"\\n"
            + "          value: \\\"hour,pm10,so2,co,no2,pm2\\\"\\n        - name: \\\"intervalNum\\\"\\n          value: \\\"100\\\"\\n        - "
            + "name: \\\"execution\\\"\\n          from: \\\"{{inputs.parameters.execution_maxcompute}}\\\"\\n      dependencies:\\n      - "
            + "\\\"id-2d88-1608982098027-91558\\\"\\n      initContainers: []\\n      sideCarContainers: []\\n      pipelines: []\\n      volumes: "
            + "[]\\n  volumes: []\\n\",\"paraValue\":\"--paiflow_endpoint=paiflowinner-share.aliyuncs.com --region=inner "
            + "--ai_workspace_endpoint=aiworkspaceinner-share.aliyuncs.com\",\"prgType\":\"1000138\","
            + "\"requestId\":\"EEC0A335-B741-180F-B26C-85297EDC6D9D\",\"taskRelations\":[{\"childTaskUniqueCode\":\"id-3d25-1608980864737-77856\","
            + "\"parentTaskUniqueCode\":\"id-85ae-1608984392467-81946\"},{\"childTaskUniqueCode\":\"id-9531-1608891073887-61143\","
            + "\"parentTaskUniqueCode\":\"id-85ae-1608984392467-81946\"},{\"childTaskUniqueCode\":\"id-4191-1608984751235-85036\","
            + "\"parentTaskUniqueCode\":\"id-3d25-1608980864737-77856\"},{\"childTaskUniqueCode\":\"id-4191-1608984751235-85036\","
            + "\"parentTaskUniqueCode\":\"id-85ae-1608984392467-81946\"},{\"childTaskUniqueCode\":\"id-6b26-1608984755520-07203\","
            + "\"parentTaskUniqueCode\":\"id-85ae-1608984392467-81946\"},{\"childTaskUniqueCode\":\"id-6b26-1608984755520-07203\","
            + "\"parentTaskUniqueCode\":\"id-9531-1608891073887-61143\"},{\"childTaskUniqueCode\":\"id-5dd4-1608984770722-54098\","
            + "\"parentTaskUniqueCode\":\"id-4191-1608984751235-85036\"},{\"childTaskUniqueCode\":\"id-41c6-1608984773170-26269\","
            + "\"parentTaskUniqueCode\":\"id-6b26-1608984755520-07203\"},{\"childTaskUniqueCode\":\"id-85ae-1608984392467-81946\","
            + "\"parentTaskUniqueCode\":\"id-9869-1608986331830-78910\"},{\"childTaskUniqueCode\":\"id-2d88-1608982098027-91558\","
            + "\"parentTaskUniqueCode\":\"id-9309-1608992715826-47326\"},{\"childTaskUniqueCode\":\"id-73af-1608994414936-19117\","
            + "\"parentTaskUniqueCode\":\"id-2d88-1608982098027-91558\"},{\"childTaskUniqueCode\":\"id-2317-1608984201281-74996\","
            + "\"parentTaskUniqueCode\":\"id-73af-1608994414936-19117\"},{\"childTaskUniqueCode\":\"id-9869-1608986331830-78910\","
            + "\"parentTaskUniqueCode\":\"id-73af-1608994414936-19117\"},{\"childTaskUniqueCode\":\"id-3906-1608996028711-50772\","
            + "\"parentTaskUniqueCode\":\"id-2d88-1608982098027-91558\"}],\"tasks\":[{\"root\":false,\"taskName\":\"逻辑回归二分类\","
            + "\"taskUniqueCode\":\"id-9531-1608891073887-61143\"},{\"root\":false,\"taskName\":\"随机森林\","
            + "\"taskUniqueCode\":\"id-3d25-1608980864737-77856\"},{\"root\":false,\"taskName\":\"类型转换\","
            + "\"taskUniqueCode\":\"id-2d88-1608982098027-91558\"},{\"root\":false,\"taskName\":\"数据视图\","
            + "\"taskUniqueCode\":\"id-2317-1608984201281-74996\"},{\"root\":false,\"taskName\":\"拆分\","
            + "\"taskUniqueCode\":\"id-85ae-1608984392467-81946\"},{\"root\":false,\"taskName\":\"预测\","
            + "\"taskUniqueCode\":\"id-4191-1608984751235-85036\"},{\"root\":false,\"taskName\":\"预测\","
            + "\"taskUniqueCode\":\"id-6b26-1608984755520-07203\"},{\"root\":false,\"taskName\":\"二分类评估\","
            + "\"taskUniqueCode\":\"id-5dd4-1608984770722-54098\"},{\"root\":false,\"taskName\":\"二分类评估\","
            + "\"taskUniqueCode\":\"id-41c6-1608984773170-26269\"},{\"root\":false,\"taskName\":\"归一化\","
            + "\"taskUniqueCode\":\"id-9869-1608986331830-78910\"},{\"root\":true,\"taskName\":\"读数据源\","
            + "\"taskUniqueCode\":\"id-9309-1608992715826-47326\"},{\"root\":false,\"taskName\":\"SQL组件\","
            + "\"taskUniqueCode\":\"id-73af-1608994414936-19117\"},{\"root\":false,\"taskName\":\"直方图\","
            + "\"taskUniqueCode\":\"id-3906-1608996028711-50772\"}],\"workspaceId\":\"23620\"}";
        CodeModel<Code> code = CodeModelFactory.getCodeModel(CodeProgramType.PAI_STUDIO.name(), null);
        code.setSourceCode(content);
        Assert.assertNotNull(code);
        PaiFlowCode codeModel = (PaiFlowCode)code.getCodeModel();
        Assert.assertNotNull(codeModel);
        Assert.assertNotNull(codeModel.getExtraContent());
        log.info("content: {}", codeModel.getContent());
        log.info("extraContent: {}", codeModel.getExtraContent());
        PaiFlowCode paiFlowCode = JSON.parseObject(codeModel.getContent(), PaiFlowCode.class);
        Assert.assertNotNull(paiFlowCode);
        Assert.assertEquals(23620, (long)paiFlowCode.getAppId());
        Assert.assertNotNull(paiFlowCode.getComputeResource());
        Specification<DataWorksWorkflowSpec> spec = paiFlowCode.getSpec();
        log.info("spec: {}", SpecUtil.writeToSpec(spec));
        Assert.assertNotNull(spec);
        Assert.assertNotNull(spec.getSpec());
        Assert.assertNotNull(spec.getSpec().getNodes());
        Assert.assertTrue(CollectionUtils.isNotEmpty(spec.getSpec().getNodes()));
        Assert.assertNotNull(spec.getSpec().getFlow());
        Assert.assertTrue(CollectionUtils.isNotEmpty(spec.getSpec().getFlow()));
    }
}
