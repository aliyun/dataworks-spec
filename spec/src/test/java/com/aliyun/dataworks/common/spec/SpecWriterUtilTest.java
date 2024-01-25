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

package com.aliyun.dataworks.common.spec;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter.Feature;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.Code;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrJobType;
import com.aliyun.dataworks.common.spec.domain.enums.ArtifactType;
import com.aliyun.dataworks.common.spec.domain.enums.DependencyType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeInstanceModeType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRecurrenceType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRerunModeType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDoWhile;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecForEach;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecRuntimeResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import com.aliyun.dataworks.common.spec.writer.WriterFactory;
import com.aliyun.dataworks.common.spec.writer.impl.SpecificationWriter;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;

/**
 * writer tests
 *
 * @author 聿剑
 * @date 2023/8/27
 */
public class SpecWriterUtilTest {
    @Test
    public void testWriterFactory() {
        SpecWriterContext context = new SpecWriterContext();
        SpecificationWriter writer = (SpecificationWriter)WriterFactory.getWriter(Specification.class, context);
        Specification<DataWorksWorkflowSpec> spec = new Specification<>();
        spec.setVersion(SpecVersion.V_1_0_0);
        spec.setKind(SpecKind.CYCLE_WORKFLOW);
        spec.setMetadata(new HashMap<>());
        spec.setSpec(new DataWorksWorkflowSpec());

        SpecNode node1 = new SpecNode();

        node1.setId("uuid-node-1");
        node1.setName("node1");
        node1.setTimeout(1);
        node1.setRerunInterval(10000);
        node1.setRecurrence(NodeRecurrenceType.SKIP);
        node1.setInstanceMode(NodeInstanceModeType.IMMEDIATELY);
        node1.setRerunMode(NodeRerunModeType.ALL_DENIED);

        SpecScript script1 = new SpecScript();
        script1.setContent("show tables");
        script1.setId("uuid-script-1");
        script1.setLanguage("sql");
        script1.setPath("/to/path/q");
        SpecScriptRuntime runtime = new SpecScriptRuntime();
        runtime.setEngine("MaxCompute");
        runtime.setCommand("ODPS_SQL");

        CodeModel<Code> hive = CodeModelFactory.getCodeModel("EMR_HIVE", "{}");
        EmrCode emrCode = (EmrCode)hive.getCodeModel();
        emrCode.setName("");
        emrCode.setType(EmrJobType.HIVE_SQL);
        emrCode.setSourceCode("select 1");
        runtime.setTemplate(GsonUtils.fromJsonString(emrCode.getContent(), new TypeToken<Map<String, Object>>() {}.getType()));
        script1.setRuntime(runtime);
        SpecVariable p1 = new SpecVariable();
        p1.setValue("${yyyymmdd}");
        p1.setName("bizdate");
        p1.setType(VariableType.SYSTEM);
        p1.setScope(VariableScopeType.NODE_PARAMETER);

        SpecVariable p2 = new SpecVariable();
        p2.setName("regionId");
        p2.setScope(VariableScopeType.NODE_PARAMETER);
        p2.setType(VariableType.CONSTANT);
        p2.setValue("cn-shanghai");
        script1.setParameters(Arrays.asList(p1, p2));
        node1.setScript(script1);

        SpecNode node2 = new SpecNode();
        node2.setName("node2");
        SpecRuntimeResource runtimeResource = new SpecRuntimeResource();
        runtimeResource.setResourceGroup("S_res_group_x");
        node2.setRuntimeResource(runtimeResource);

        SpecNodeOutput in1 = new SpecNodeOutput();
        in1.setData("autotest.123132_out");
        in1.setArtifactType(ArtifactType.NODE_OUTPUT);

        SpecVariable in2 = new SpecVariable();
        in2.setName("bizdate");
        in2.setType(VariableType.SYSTEM);
        in2.setScope(VariableScopeType.NODE_PARAMETER);
        in2.setValue("${yyyymmdd}");
        node2.setInputs(Arrays.asList(in1, in2));
        spec.getSpec().setNodes(Arrays.asList(node1, node2));

        SpecFlowDepend flow1 = new SpecFlowDepend();
        flow1.setNodeId(node1);
        SpecDepend dep1 = new SpecDepend();
        dep1.setType(DependencyType.CROSS_CYCLE_SELF);
        flow1.setDepends(Collections.singletonList(dep1));
        spec.getSpec().setFlow(Collections.singletonList(flow1));

        JSONObject json = writer.write(spec, context);
        System.out.println(json.toJSONString(Feature.PrettyFormat));
        Assert.assertNotNull(json);
    }

    @Test
    public void testDoWhileForeachWriter() {
        Specification<DataWorksWorkflowSpec> spec = new Specification<>();
        spec.setVersion(SpecVersion.V_1_1_0);
        spec.setKind(SpecKind.CYCLE_WORKFLOW);
        DataWorksWorkflowSpec dwSpec = new DataWorksWorkflowSpec();
        spec.setSpec(dwSpec);

        SpecNode dowhile = new SpecNode();
        SpecDoWhile dow = new SpecDoWhile();
        dowhile.setDoWhile(dow);

        SpecNode foreach = new SpecNode();
        SpecForEach fore = new SpecForEach();
        foreach.setForeach(fore);

        dwSpec.setNodes(Arrays.asList(foreach, dowhile));

        System.out.println(SpecUtil.writeToSpec(spec));
    }
}
