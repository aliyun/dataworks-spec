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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.Code;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.PaiFlowCode;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import lombok.extern.slf4j.Slf4j;
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
}
