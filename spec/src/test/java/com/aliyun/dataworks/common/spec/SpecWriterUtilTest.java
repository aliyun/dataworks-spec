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
import com.aliyun.dataworks.common.spec.domain.enums.SpecFileResourceType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.SpecStorageType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDoWhile;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecForEach;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDatasource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFileResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecRuntimeResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.file.SpecObjectStorageFile;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.container.SpecContainer;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.container.SpecContainerEnvVar;
import com.aliyun.dataworks.common.spec.domain.ref.storage.SpecStorage;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import com.aliyun.dataworks.common.spec.writer.WriterFactory;
import com.aliyun.dataworks.common.spec.writer.impl.SpecificationWriter;
import com.google.common.collect.Lists;
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
        spec.setVersion(SpecVersion.V_1_0_0.getLabel());
        spec.setKind(SpecKind.CYCLE_WORKFLOW.getLabel());
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

        SpecContainer container = new SpecContainer();
        container.setArgs(Lists.newArrayList("3600"));
        container.setImage("registry.cn-hangzhou.aliyuncs.com/aliyun-dataworks/dataworks-base:1.0.0-20231227110000-aliyun-inc-stable");
        container.setImageId("imageId-1");
        container.setCommand(Lists.newArrayList("sleep"));

        SpecContainerEnvVar envVar1 = new SpecContainerEnvVar();
        envVar1.setName("key1");
        envVar1.setValue("value1");

        SpecContainerEnvVar envVar2 = new SpecContainerEnvVar();
        envVar2.setName("key2");
        envVar2.setValue("value2");

        container.setEnv(Lists.newArrayList(envVar1, envVar2));
        runtime.setContainer(container);

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
        spec.setVersion(SpecVersion.V_1_1_0.getLabel());
        spec.setKind(SpecKind.CYCLE_WORKFLOW.getLabel());
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

    @Test
    public void testWriteFileResource() {
        Specification<DataWorksWorkflowSpec> spec = new Specification<>();
        DataWorksWorkflowSpec dataWorksWorkflowSpec = new DataWorksWorkflowSpec();
        spec.setVersion(SpecVersion.V_1_1_0.getLabel());
        spec.setKind(SpecKind.CYCLE_WORKFLOW.getLabel());
        spec.setSpec(dataWorksWorkflowSpec);
        SpecFileResource res = new SpecFileResource();
        res.setName("test_res");
        res.setType(SpecFileResourceType.FILE);
        SpecRuntimeResource rt = new SpecRuntimeResource();
        rt.setResourceGroup("S_resgroup_xx");
        res.setRuntimeResource(rt);
        SpecScript script = new SpecScript();
        script.setPath("/tmp/test.txt");
        script.setContent("OSS_KEY_xxx");
        res.setScript(script);
        SpecDatasource datasource = new SpecDatasource();
        datasource.setName("emr_0");
        datasource.setType("emr");
        res.setDatasource(datasource);
        SpecObjectStorageFile file = new SpecObjectStorageFile();
        SpecStorage storage = new SpecStorage();
        storage.setType(SpecStorageType.OSS);
        file.setStorage(storage);
        file.setPath("/tmp/test.txt");
        res.setFile(file);
        dataWorksWorkflowSpec.setFileResources(Collections.singletonList(res));
        System.out.println(SpecUtil.writeToSpec(spec));
    }
}
