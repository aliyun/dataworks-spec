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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter.Feature;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Spec;
import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.Code;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.nodemodel.DataWorksNodeAdapter;
import com.aliyun.dataworks.common.spec.domain.dw.nodemodel.DataWorksNodeAdapter.Context;
import com.aliyun.dataworks.common.spec.domain.dw.nodemodel.DataWorksNodeCodeAdapter;
import com.aliyun.dataworks.common.spec.domain.enums.DependencyType;
import com.aliyun.dataworks.common.spec.domain.enums.FailureStrategy;
import com.aliyun.dataworks.common.spec.domain.enums.FunctionType;
import com.aliyun.dataworks.common.spec.domain.enums.SourceType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.noref.SpecBranches;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDoWhile;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecForEach;
import com.aliyun.dataworks.common.spec.domain.noref.SpecJoinBranch;
import com.aliyun.dataworks.common.spec.domain.noref.SpecLogic;
import com.aliyun.dataworks.common.spec.domain.noref.SpecParamHub;
import com.aliyun.dataworks.common.spec.domain.noref.SpecSubFlow;
import com.aliyun.dataworks.common.spec.domain.ref.SpecArtifact;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDataIntegrationJob;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDataset;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDatasource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDqcRule;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFunction;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecRuntimeResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScheduleStrategy;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.common.spec.domain.ref.calcengine.SpecCalcEngine;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import com.aliyun.dataworks.common.spec.writer.Writer;
import com.aliyun.dataworks.common.spec.writer.WriterFactory;
import com.aliyun.dataworks.common.spec.writer.impl.SpecificationWriter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author yiwei.qyw
 * @date 2023/7/4
 */
@Slf4j
public class SpecUtilTest {

    @Test
    public void testExample() {
        String spec = readJson("example.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);
        assertNotNull(specObj.getVersion());
        assertNotNull(specObj.getKind());
        assertNotNull(specObj.getMetadata());
        assertNotNull(specification.getVariables());
        assertNotNull(specification.getScripts());
        assertNotNull(specification.getRuntimeResources());
        assertNotNull(specification.getTriggers());
        assertNotNull(specification.getArtifacts());
        assertNotNull(specification.getNodes());
        assertNotNull(specification.getFlow());

        String label = specObj.getKind();
        assertEquals(label.toLowerCase(), "CycleWorkflow".toLowerCase());

        // variable的node是否一致
        SpecVariable variable_ctx_output_1 = specification.getVariables().stream().filter(
            v -> v.getId().equals("ctx_output_1")).findFirst().get();
        SpecNode specNode_node_existed_xx = specification.getNodes().stream().filter(
            n -> n.getId().equals("node_existed_xx")).findFirst().get();
        // Assert.assertSame(variable_ctx_output_1.getNode(), specNode_node_existed_xx);

        // node的Script是否一致
        SpecNode specNode_node_1 = specification.getNodes().stream().filter(n -> n.getId().equals("node_1")).findFirst()
            .get();

        List<Input> inputs = specNode_node_1.getInputs();
        for (Input input : inputs) {
            assertTrue(input instanceof SpecArtifact || input instanceof SpecVariable);
        }

        SpecScript scriptFile1 = specification.getScripts().stream().filter(s -> s.getId().equals("script_file1"))
            .findFirst().get();
        Assert.assertSame(scriptFile1, specNode_node_1.getScript());

        // 变量的类型是否正确
        assertNotNull(specNode_node_1.getPriority());

        assertTrue(specNode_node_1.getPriority().equals(7));

        // Script的parameters和variable是否一致
        SpecVariable variable_biz = specification.getVariables().stream().filter(v -> v.getId().equals("bizdate"))
            .findFirst().get();
        SpecVariable specVariable1 = scriptFile1.getParameters().stream().filter(s -> s.getId().equals("bizdate"))
            .findFirst().get();
        Assert.assertSame(variable_biz, specVariable1);

        // Node的input中的artifacts是否一致
        SpecArtifact node_specArtifact_table1 = specNode_node_1.getInputs().stream().filter(
            input -> input instanceof SpecArtifact).map(input -> (SpecArtifact)input).filter(
            specArtifact -> specArtifact.getId().equals("table1")).findFirst().get();

        SpecArtifact specArtifact_table1 = specification.getArtifacts().stream().filter(
            specArtifact -> specArtifact.getId().equals("table1")).findFirst().get();

        Assert.assertSame(node_specArtifact_table1, specArtifact_table1);

        // Node的input中的variables是否一致
        SpecVariable node_specArtifact_var = specNode_node_1.getInputs().stream().filter(
            input -> input instanceof SpecVariable).map(input -> (SpecVariable)input).filter(
            v -> v.getId().equals("ctx_output_1")).findFirst().get();
        Assert.assertSame(variable_ctx_output_1, node_specArtifact_var);

        // Node的output中的artifacts是否一致
        SpecArtifact node_specArtifact_artifact2 = specNode_node_1.getOutputs().stream().filter(
            output -> output instanceof SpecArtifact).map(output -> (SpecArtifact)output).filter(
            a -> a.getId().equals("table3")).findFirst().get();

        SpecArtifact specArtifact_table3 = specification.getArtifacts().stream().filter(
            specArtifact -> specArtifact.getId().equals("table3")).findFirst().get();
        Assert.assertSame(node_specArtifact_artifact2, specArtifact_table3);

        // Node的output中的variables是否一致
        SpecVariable node_specVariable_var1 = specNode_node_1.getOutputs().stream().filter(
            output -> output instanceof SpecVariable).map(output -> (SpecVariable)output).filter(
            v -> v.getId().equals("region")).findFirst().get();

        SpecVariable specVariable_var1 = specification.getVariables().stream().filter(
            variable -> variable.getId().equals("region")).findFirst().get();

        Assert.assertSame(node_specVariable_var1, specVariable_var1);

        // Node的trigger是否一致
        SpecTrigger trigger = specNode_node_1.getTrigger();
        SpecTrigger specTrigger = specification.getTriggers().stream().filter(t -> t.getId().equals("daily"))
            .findFirst().get();
        Assert.assertSame(trigger, specTrigger);

        // Node的runtimeResource是否一致
        SpecRuntimeResource runtimeResource = specNode_node_1.getRuntimeResource();
        SpecRuntimeResource resgroup1 = specification.getRuntimeResources().stream().filter(
            r -> r.getId().equals("resgroup_1")).findFirst().get();
        Assert.assertSame(resgroup1, runtimeResource);

        // Flow的nodeId是否一致
        SpecFlowDepend specFlow_Depend_node = specification.getFlow().stream().filter(
            f -> f.getNodeId().getId().equals("node_1")).findFirst().get();
        Assert.assertSame(specFlow_Depend_node.getNodeId(), specNode_node_1);
        // Flow中的Depends中的nodeID是否一致
        SpecDepend nodeExistedXx = specFlow_Depend_node.getDepends().stream().filter(
            d -> d.getNodeId().getId().equals("node_existed_xx")).findFirst().get();
        Assert.assertSame(specNode_node_existed_xx, nodeExistedXx.getNodeId());

        assertNotNull(specNode_node_1.getDatasource());
        assertNotNull(specNode_node_1.getDatasource().getName());

        assertTrue(specification.getNodes().stream().anyMatch(n -> n.getOwner() != null));
        assertTrue(specification.getNodes().stream().anyMatch(n -> n.getRerunTimes() != null));
        assertTrue(specification.getNodes().stream().anyMatch(n -> n.getRerunInterval() != null));
        List<SpecDataset> datasets = specNode_node_1.getDatasets();
        assertNotNull(datasets);
        assertEquals(datasets.size(), 2);
        SpecDataset specDataset1 = datasets.get(0);
        assertNotNull(specDataset1);
        assertEquals("dataset_1", specDataset1.getIdentifier());

        assertTrue(specification.getNodes().stream().anyMatch(n -> n.getOwner() != null));
        assertTrue(specification.getNodes().stream().anyMatch(n -> n.getRerunTimes() != null));
        assertTrue(specification.getNodes().stream().anyMatch(n -> n.getRerunInterval() != null));

        System.out.println(SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testBranch() {
        String spec = readJson("branch.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);
        assertNotNull(specification.getArtifacts());
        assertNotNull(specification.getScripts());
        assertNotNull(specification.getNodes());
        assertNotNull(specification.getFlow());

        SpecNode specNode_branch = specification.getNodes().stream().filter(n -> n.getId().equals("branch")).findFirst()
            .get();

        // node branch的output是否一致
        SpecBranches specBranches = specNode_branch.getBranch().getBranches().stream().filter(b -> b.getWhen().equals("a == 1"))
            .findFirst().get();

        SpecArtifact artifact = specification.getArtifacts().stream().filter(a -> a.getId().equals("branch_1"))
            .findFirst().get();
        Assert.assertSame(artifact, specBranches.getOutput());

        // flow的output是否一致
        SpecFlowDepend specFlowDepend = specification.getFlow().stream().filter(
            f -> f.getNodeId().getId().equals("branch_1")).findFirst().get();
        SpecDepend specDepend = specFlowDepend.getDepends().stream().findFirst().get();
        Assert.assertSame(specDepend.getOutput(), artifact);

        log.info("spec branch: {}", SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testExpanded() {
        String spec = readJson("expanded.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);
        // node
        assertNotNull(specification.getNodes());
        List<SpecNode> nodes = specification.getNodes();
        assertEquals(nodes.size(), 4);

        SpecNode specNode1 = nodes.get(0);
        // reference
        assertNotNull(specNode1.getReference());
        // script
        SpecNode specNode2 = nodes.get(1);
        assertNotNull(specNode2.getScript());
        // script runtime
        assertNotNull(specNode2.getScript().getRuntime());
        // parameters
        assertEquals(4, specNode2.getScript().getParameters().size());
        // input
        assertEquals(1, specNode2.getInputs().size());
        // output
        assertEquals(1, specNode2.getOutputs().size());
        // trigger
        assertNotNull(specNode2.getTrigger());
        // runtimeResource
        assertNotNull(specNode2.getRuntimeResource());

        // flow
        assertNotNull(specification.getFlow());

        SpecWriterContext context = new SpecWriterContext();
        SpecificationWriter writer = new SpecificationWriter(context);
        System.out.println(writer.write(specObj, context).toJSONString(Feature.PrettyFormat));
    }

    @Test
    public void testInnerFlow() {
        String spec = readJson("innerflow.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        DataWorksWorkflowSpec specification = specObj.getSpec();

        assertNotNull(specification);

        List<SpecNode> nodes = specification.getNodes();
        // do-while node
        assertEquals(4, nodes.size());
        SpecNode doWhile1 = nodes.stream().filter(n -> n.getId().equals("do_while_1")).findFirst().get();
        // do-while not null
        assertNotNull(doWhile1.getDoWhile());
        SpecDoWhile doWhile = doWhile1.getDoWhile();
        // nodes not null
        assertNotNull(doWhile.getNodes());
        // script
        SpecScript script_sql1 = specification.getScripts().stream().filter(s -> s.getId().equals("sql1")).findFirst()
            .get();
        SpecNode specNode1 = doWhile.getNodes().stream().filter(specNode -> specNode.getId().equals("sql1")).findFirst()
            .get();

        Assert.assertSame(specNode1.getScript(), script_sql1);

        // while
        assertNotNull(doWhile.getSpecWhile());
        SpecScript script_end = specification.getScripts().stream().filter(s -> s.getId().equals("end")).findFirst()
            .get();
        Assert.assertSame(doWhile.getSpecWhile().getScript(), script_end);

        // flow
        assertNotNull(doWhile.getFlow());
        assertNotNull(doWhile.getFlow().get(0).getNodeId());

        // for-each node
        SpecNode node_foreach_1 = nodes.stream().filter(n -> n.getId().equals("foreach_1")).findFirst().get();

        assertNotNull(node_foreach_1.getForeach());
        SpecForEach foreach = node_foreach_1.getForeach();

        assertNotNull(foreach.getArray());
        assertNotNull(foreach.getNodes());
        assertNotNull(foreach.getFlow());

        // variable 是否是同一对象
        SpecVariable specVariable = specification.getVariables().stream().filter(v -> v.getId().equals("var_arr"))
            .findFirst().get();
        Assert.assertSame(foreach.getArray(), specVariable);

        // nodes中的script是否统一对象
        SpecNode sqlNode = foreach.getNodes().stream().filter(n -> n.getId().equals("foreach_sql1")).findFirst().get();
        Assert.assertSame(script_sql1, sqlNode.getScript());

        Writer writer = WriterFactory.getWriter(SpecForEach.class, new SpecWriterContext());
        log.info("foreach: {}", JSON.toJSONString(writer.write(node_foreach_1.getForeach(), new SpecWriterContext()), Feature.PrettyFormat));
    }

    @Test
    public void testJoin() {
        String spec = readJson("join.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        DataWorksWorkflowSpec specification = specObj.getSpec();

        assertNotNull(specification);

        // join node
        SpecNode join_1 = specification.getNodes().stream().filter(n -> n.getId().equals("join_1")).findFirst().get();

        assertNotNull(join_1.getJoin().getLogic());
        SpecLogic logic = join_1.getJoin().getLogic();

        assertNotNull(logic.getExpression());

        // and nodeId 是否是同一对象
        SpecJoinBranch specAnd = join_1.getJoin().getBranches().stream().filter(a -> a.getNodeId().getId().equals("join_branch_1")).findFirst().get();
        SpecNode branch_1 = specification.getNodes().stream().filter(n -> n.getId().equals("join_branch_1")).findFirst().get();
        Assert.assertSame(specAnd.getNodeId(), branch_1);

        // or nodeId 是否是同一对象
        SpecJoinBranch specOr = join_1.getJoin().getBranches().stream().filter(a -> a.getNodeId().getId().equals("join_branch_3")).findFirst().get();

        SpecNode branch_3 = specification.getNodes().stream().filter(n -> n.getId().equals("join_branch_3")).findFirst().get();

        Assert.assertSame(specOr.getNodeId(), branch_3);

        // or的 status是否存在
        assertNotNull(specOr.getAssertion());

        System.out.println("join spec: " + SpecUtil.writeToSpec(specObj));
        assertTrue(join_1.getJoin().getBranches().stream().allMatch(b -> StringUtils.isNotBlank(b.getName())));
    }

    @Test
    public void testManual() {
        String spec = readJson("manual_flow.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);
        assertNotNull(specObj.getSpec().getId());
        assertEquals(specObj.getKind(), SpecKind.MANUAL_WORKFLOW.getLabel());
    }

    @Test
    public void testParameter_node() {
        String spec = readJson("parameter_node.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        DataWorksWorkflowSpec specification = specObj.getSpec();

        assertNotNull(specification);
        SpecVariable ctx_var_2 = specification.getVariables().stream().filter(n -> n.getId().equals("ctx_var_2"))
            .findFirst().get();

        Assert.assertSame(ctx_var_2.getScope(), VariableScopeType.NODE_PARAMETER);
    }

    @Test
    public void testDefaultParser() {
        String spec = readJson("newSimple.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);
    }

    @Test
    public void testSimpleDemo() {
        String spec = readJson("simpleDemo.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();

        assertNotNull(specification);

        System.out.println(SpecUtil.writeToSpec(specObj));
    }

    public String readJson(String fileName) {
        String fileContent = "";
        try {
            URL resource = getClass().getResource("/" + fileName);
            String path = resource.getPath();
            File file = new File(path);
            Scanner scanner = new Scanner(file);

            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine());
            }

            fileContent = stringBuilder.toString();
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return fileContent;
    }

    @Test
    public void testParse() {
        String spec = "{\n"
            + "    \"version\": \"1.0.0\",\n"
            + "    \"kind\": \"CycleWorkflow\",\n"
            + "    \"nodes\": [\n"
            + "      {\n"
            + "        \"id\": \"c05cc423ac8046a7b18ccc9dd88ef27e\",\n"
            + "        \"recurrence\": \"Normal\",\n"
            + "        \"timeout\": 3,\n"
            + "        \"instanceMode\": \"T+1\",\n"
            + "        \"rerunMode\": \"Allowed\",\n"
            + "        \"rerunTimes\": 3,\n"
            + "        \"rerunInterval\": 180000,\n"
            + "        \"script\": {\n"
            + "          \"language\": \"odps\",\n"
            + "          \"runtime\": {\n"
            + "            \"engine\": \"MaxCompute\",\n"
            + "            \"command\": \"ODPS_SQL\"\n"
            + "          },\n"
            + "          \"parameters\": [\n"
            + "            {\n"
            + "              \"name\": \"bizdate\",\n"
            + "              \"scope\": \"NodeParameter\",\n"
            + "              \"type\": \"System\",\n"
            + "              \"value\": \"$[yyyymmdd]\"\n"
            + "            }\n"
            + "          ]\n"
            + "        },\n"
            + "        \"trigger\": {\n"
            + "          \"id\": \"ddb2d936a16a4a45bc34b68c30d05f84\",\n"
            + "          \"type\": \"Scheduler\",\n"
            + "          \"cron\": \"00 00 00 * * ?\",\n"
            + "          \"startTime\": \"1970-01-01 00:00:00\",\n"
            + "          \"endTime\": \"9999-01-01 00:00:00\",\n"
            + "          \"timezone\": \"Asia/Shanghai\"\n"
            + "        },\n"
            + "        \"runtimeResource\": {\n"
            + "          \"resourceGroup\": \"dataphin_scheduler_pre\"\n"
            + "        },\n"
            + "        \"name\": \"p_param_2\",\n"
            + "        \"owner\": \"064152\",\n"
            + "        \"inputs\": {},\n"
            + "        \"outputs\": {\n"
            + "          \"outputs\": [\n"
            + "            {\n"
            + "              \"type\": \"Output\",\n"
            + "              \"data\": \"c05cc423ac8046a7b18ccc9dd88ef27e\",\n"
            + "              \"refTableName\": \"p_param_2\"\n"
            + "            }\n"
            + "          ]\n"
            + "        },\n"
            + "        \"functions\": [],\n"
            + "        \"fileResources\": []\n"
            + "      }\n"
            + "    ],\n"
            + "    \"flow\": [\n"
            + "      {\n"
            + "        \"nodeId\": \"c05cc423ac8046a7b18ccc9dd88ef27e\",\n"
            + "        \"depends\": [\n"
            + "          {\n"
            + "            \"type\": \"Normal\",\n"
            + "            \"output\": \"dw_scheduler_pre.test_sql002\"\n"
            + "          }\n"
            + "        ]\n"
            + "      }\n"
            + "    ]\n"
            + "  }";

        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        String str = SpecUtil.writeToSpec(specObj);
        System.out.println(str);

        specification.getFlow().forEach(flow -> System.out.println(JSON.toJSONString(flow.getDepends().get(0).getOutput(), Feature.PrettyFormat)));
    }

    @Test
    public void testScriptRuntimeTemplate() {
        String spec = readJson("script_runtime_template.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();

        SpecScript script = specification.getScripts().get(0);

        System.out.println(script.getRuntime().getTemplate());

        assertNotNull(script.getRuntime().getTemplate());

        CodeModel<Code> codeModel
            = CodeModelFactory.getCodeModel(script.getRuntime().getCommand(), JSON.toJSONString(script.getRuntime().getTemplate()));

        EmrCode emrCode = (EmrCode)codeModel.getCodeModel();

        emrCode.setName("test emr name");
        emrCode.getProperties().getEnvs().put("test_v1", "v1");
        emrCode.setSourceCode("select 1");

        System.out.println("code model: " + emrCode.getContent());
    }

    @Test
    public void testSpecSampleJsonFiles() {
        String path = "spec/examples/json";
        File dir = new File(SpecUtilTest.class.getClassLoader().getResource(path).getFile());
        Arrays.stream(dir.listFiles((dir1, name) -> name.endsWith(".json"))).forEach(js -> {
            Specification specification = null;
            try {
                String json = readJson(path + File.separator + js.getName());
                specification = SpecUtil.parseToDomain(json);
            } catch (Exception ex) {
                System.err.println("json: " + js + " parse error: " + ex);
                ex.printStackTrace();
            }
            assertNotNull(specification);
        });
    }

    @Test
    public void testCombinedNode() {
        String spec = readJson("combined_node.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);
        assertNotNull(specification.getNodes());
        assertEquals(1, specification.getNodes().size());

        SpecNode combined = specification.getNodes().get(0);
        assertNotNull(combined.getCombined());

        assertNotNull(combined.getCombined().getNodes());
        Assert.assertFalse(combined.getCombined().getNodes().isEmpty());

        System.out.println(SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testParamHubParser() {
        String spec = "{\n"
            + "        \"variables\": [\n"
            + "          {\n"
            + "            \"name\": \"my_const\",\n"
            + "            \"type\": \"Constant\",\n"
            + "            \"scope\": \"NodeContext\",\n"
            + "            \"value\": \"cn-shanghai\"\n"
            + "            \"description\": \"cn-shanghai\"\n"
            + "          },\n"
            + "          {\n"
            + "            \"name\": \"my_var\",\n"
            + "            \"type\": \"System\",\n"
            + "            \"scope\": \"NodeContext\",\n"
            + "            \"value\": \"${yyyymmdd}\"\n"
            + "          },\n"
            + "          {\n"
            + "            \"name\": \"outputs\",\n"
            + "            \"type\": \"PassThrough\",\n"
            + "            \"scope\": \"NodeContext\",\n"
            + "            \"referenceVariable\": {\n"
            + "              \"name\": \"outputs\",\n"
            + "              \"type\": \"NodeOutput\",\n"
            + "              \"scope\": \"NodeContext\",\n"
            + "              \"value\": \"${outputs}\",\n"
            + "              \"node\": {\n"
            + "                \"output\": \"autotest.28517448_out\"\n"
            + "              }\n"
            + "            }\n"
            + "          },\n"
            + "          {\n"
            + "            \"name\": \"shell_const_1\",\n"
            + "            \"type\": \"PassThrough\",\n"
            + "            \"scope\": \"NodeContext\",\n"
            + "            \"referenceVariable\": {\n"
            + "              \"name\": \"shell_const_1\",\n"
            + "              \"type\": \"NodeOutput\",\n"
            + "              \"scope\": \"NodeContext\",\n"
            + "              \"node\": {\n"
            + "                \"output\": \"autotest.28517347_out\"\n"
            + "              }\n"
            + "            }\n"
            + "          },\n"
            + "          {\n"
            + "            \"name\": \"shell_var_1\",\n"
            + "            \"type\": \"PassThrough\",\n"
            + "            \"scope\": \"NodeContext\",\n"
            + "            \"referenceVariable\": {\n"
            + "              \"name\": \"shell_var_1\",\n"
            + "              \"type\": \"NodeOutput\",\n"
            + "              \"scope\": \"NodeContext\",\n"
            + "              \"node\": {\n"
            + "                \"output\": \"autotest.28517347_out\"\n"
            + "              }\n"
            + "            }\n"
            + "          }\n"
            + "        ]\n"
            + "      }";
        SpecParserContext ctx = new SpecParserContext();
        ctx.setVersion(SpecVersion.V_1_1_0.getLabel());
        SpecParamHub paramHub = (SpecParamHub)SpecDevUtil.getObjectByParser(SpecParamHub.class, JSON.parseObject(spec), ctx);
        log.info("para hub: {}", GsonUtils.toJsonString(paramHub));
        assertNotNull(paramHub);
        assertNotNull(paramHub.getVariables());
        assertNotNull(paramHub.getVariables().stream().filter(v -> v.getName().equals("outputs")).findFirst()
            .map(SpecVariable::getReferenceVariable).map(SpecVariable::getNode).map(SpecDepend::getOutput).map(SpecNodeOutput::getData)
            .orElse(null));
    }

    @Test
    public void testParamHub() {
        String spec = readJson("param_hub.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();

        assertNotNull(specification);

        System.out.println(SpecUtil.writeToSpec(specObj));

        specObj = SpecUtil.parseToDomain(SpecUtil.writeToSpec(specObj));
        specification = specObj.getSpec();

        assertNotNull(specification);
        assertNotNull(specification.getNodes());
        assertEquals(1, specification.getNodes().size());

        SpecNode node = specification.getNodes().get(0);
        assertNotNull(node);
        assertNotNull(node.getParamHub());
        assertNotNull(node.getParamHub().getVariables());
        assertEquals(5, node.getParamHub().getVariables().size());

        SpecVariable shellVar1 = node.getParamHub().getVariables().stream()
            .filter(v -> v.getName().equalsIgnoreCase("shell_var_1")).findFirst().orElse(null);
        assertNotNull(shellVar1);
        assertNotNull(shellVar1.getReferenceVariable());
        assertNotNull(shellVar1.getReferenceVariable().getNode());
    }

    @Test
    public void testAssignment() {
        String spec = readJson("assign.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();

        assertNotNull(specification);

        assertNotNull(specification.getNodes());
        assertEquals(1, specification.getNodes().size());

        SpecNode node = specification.getNodes().get(0);
        assertNotNull(node);

        assertNotNull(node.getOutputs());
        assertEquals(2, node.getOutputs().size());

        System.out.println(SpecUtil.writeToSpec(specObj));

        Specification<DataWorksWorkflowSpec> reparseSpec = SpecUtil.parseToDomain(SpecUtil.writeToSpec(specObj));
        assertNotNull(reparseSpec);
        assertNotNull(reparseSpec.getSpec());
        assertNotNull(reparseSpec.getSpec().getNodes());
        assertEquals(1, reparseSpec.getSpec().getNodes().size());

        node = reparseSpec.getSpec().getNodes().get(0);
        assertNotNull(node);

        assertNotNull(node.getOutputs());
        assertEquals(2, node.getOutputs().size());
    }

    @Test
    public void testDoWhile() {
        String spec = readJson("spec/examples/json/dowhile.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);
        assertNotNull(specification.getNodes());
        assertEquals(1, specification.getNodes().size());
        //Assert.assertNotNull(specification.getNodes().get(0).getDoWhile());
        //
        //Assert.assertNotNull(specification.getNodes().get(0).getDoWhile().getNodes());
        //Assert.assertEquals(3, specification.getNodes().get(0).getDoWhile().getNodes().size());

        System.out.println(SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testDiJob() {
        String spec = readJson("spec/examples/json/di_job.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);
        assertEquals(SpecKind.DATA_INTEGRATION_JOB.getLabel(), specObj.getKind());
        assertEquals("8745734768222459575", specObj.getMetadata().get("uuid"));
        List<SpecDataIntegrationJob> diJobs = specObj.getSpec().getDataIntegrationJobs();
        SpecDataIntegrationJob diJob = diJobs.get(0);
        assertEquals("di_job_01", diJob.getName());
        assertEquals("8745734768222459575", diJob.getId());
        assertEquals("DATA_INTEGRATION_JOB", diJob.getScript().getRuntime().getCommand());
        assertEquals("test/DI_test_biz/DI/job1", diJob.getScript().getPath());
        assertEquals("select 1", diJob.getScript().getContent());
        assertEquals("123124124", diJob.getOwner());
        assertEquals("description of diJob1", diJob.getDescription());

    }

    @Test
    public void testPaiflow() {
        String spec = readJson("spec/examples/json/paiflow.json");

        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);
        assertNotNull(specification.getNodes());
        assertEquals(1, specification.getNodes().size());
        assertNotNull(specification.getNodes().get(0).getPaiflow());
        assertNotNull(specification.getNodes().get(0).getPaiflow().getNodes());
        assertEquals(3, specification.getNodes().get(0).getPaiflow().getNodes().size());

        System.out.println(SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testFileResource() {
        String spec = readJson("spec/examples/json/file_resource.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);
        assertNotNull(specification.getFiles());
        assertEquals(2, specification.getFiles().size());

        assertNotNull(specification.getFileResources());
        log.info("file resources: {}", specification.getFileResources());
        assertEquals(2, specification.getFileResources().size());

        log.info("spec: {}", SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testFunction() {
        String spec = readJson("spec/examples/json/function.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);

        assertEquals(1, specification.getFunctions().size());
        SpecFunction func = specification.getFunctions().get(0);
        assertNotNull(func);
        log.info("spec: {}", SpecUtil.writeToSpec(specObj));
        assertEquals(FunctionType.MATH, func.getType());
        assertNotNull(func.getFileResources());
        assertNotNull(func.getClassName());
        assertNotNull(func.getName());
        assertNotNull(func.getDatasource());
        assertNotNull(func.getRuntimeResource());
    }

    @Test
    public void testTable() {
        String spec = readJson("spec/examples/json/table.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);

        assertEquals(1, specification.getArtifacts().size());
        SpecArtifact table = specification.getArtifacts().get(0);
        assertNotNull(table);

        assertTrue(table instanceof SpecTable);

        assertNotNull(((SpecTable)table).getDdl());
        assertNotNull(((SpecTable)table).getCalcEngine());
        assertNotNull(((SpecTable)table).getName());

        log.info("spec: {}", SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testEmr() {
        String spec = readJson("spec/examples/json/emr.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);

        List<SpecNode> nodes = specification.getNodes();
        assertNotNull(nodes);

        SpecNode emrNode = nodes.get(0);
        assertNotNull(emrNode);
        assertNotNull(emrNode.getScript());

        SpecScript script = emrNode.getScript();
        assertNotNull(script);
        assertNotNull(script.getRuntime());

        log.info("spec: {}", SpecUtil.writeToSpec(specObj));

        DataWorksNodeCodeAdapter adapter = new DataWorksNodeCodeAdapter(emrNode);
        log.info("code: {}", adapter.getCode());
        assertNotNull(adapter.getCode());
        assertTrue(StringUtils.indexOf(adapter.getCode(), "spark.executor.memory") > 0);
    }

    @Test
    public void testCdh() {
        String spec = readJson("spec/examples/json/cdh.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);

        List<SpecNode> nodes = specification.getNodes();
        assertNotNull(nodes);

        SpecNode cdhNode = nodes.get(0);
        assertNotNull(cdhNode);
        assertNotNull(cdhNode.getScript());

        SpecScript script = cdhNode.getScript();
        assertNotNull(script);
        assertNotNull(script.getRuntime());

        log.info("spec: {}", SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testDatasource() {
        String spec = readJson("spec/examples/json/datasource.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);

        List<SpecDatasource> datasources = specification.getDatasources();
        assertNotNull(datasources);

        SpecDatasource datasource = datasources.get(0);
        assertNotNull(datasource);
        assertNotNull(datasource.getName());
        assertNotNull(datasource.getType());
        assertNotNull(datasource.getConfig());

        log.info("spec: {}", SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testDqcRule() {
        String spec = readJson("spec/examples/json/dqc.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        assertNotNull(specification);

        List<SpecDqcRule> specDqcRules = specification.getDqcRules();
        assertNotNull(specDqcRules);

        SpecDqcRule dqcRule = specDqcRules.get(0);
        assertNotNull(dqcRule);
        assertNotNull(dqcRule.getName());
        assertNotNull(dqcRule.getTable());
        assertNotNull(dqcRule.getCalcEngine());
        assertNotNull(dqcRule.getRuleConfig());
        assertNotNull(dqcRule.getSchedulerNode());

        log.info("spec: {}", SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testNodeIdMissing() {
        String s = "{\n"
            + "\t\"version\":\"1.1.0\",\n"
            + "\t\"kind\":\"CycleWorkflow\",\n"
            + "\t\"spec\":{\n"
            + "\t\t\"nodes\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"recurrence\":\"Normal\",\n"
            + "\t\t\t\t\"id\":\"11195215\",\n"
            + "\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\"rerunTimes\":0,\n"
            + "\t\t\t\t\"rerunInterval\":0,\n"
            + "\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\"path\":\"业务流程/预发回归case_请不要加东西/控制\",\n"
            + "\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\"command\":\"CONTROLLER_TRAVERSE\"\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\"type\":\"Scheduler\",\n"
            + "\t\t\t\t\t\"cron\":\"00 25 00 * * ?\",\n"
            + "\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"endTime\":\"9999-01-01 00:00:00\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"runtimeResource\":{\n"
            + "\t\t\t\t\t\"resourceGroup\":\"group_2\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"name\":\"foreach_regr\",\n"
            + "\t\t\t\t\"owner\":\"068198\",\n"
            + "\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"dw_scheduler_pre.11195181_out\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"refTableName\":\"dw_scheduler_pre.11195181_out\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"dw_scheduler_pre.11195215_out\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"dw_scheduler_pre.foreach_regr\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"for-each\":{\n"
            + "\t\t\t\t\t\"nodes\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"recurrence\":\"Normal\",\n"
            + "\t\t\t\t\t\t\t\"id\":\"11195231\",\n"
            + "\t\t\t\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\t\t\t\"rerunTimes\":0,\n"
            + "\t\t\t\t\t\t\t\"rerunInterval\":0,\n"
            + "\t\t\t\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\t\t\t\"command\":\"DIDE_SHELL\"\n"
            + "\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\t\t\t\"type\":\"Scheduler\",\n"
            + "\t\t\t\t\t\t\t\t\"cron\":\"00,00\",\n"
            + "\t\t\t\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\t\t\t\"endTime\":\"9999-01-01 16:03:47\"\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"runtimeResource\":{\n"
            + "\t\t\t\t\t\t\t\t\"resourceGroup\":\"group_2\"\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"name\":\"echo_data\",\n"
            + "\t\t\t\t\t\t\t\"owner\":\"068198\",\n"
            + "\t\t\t\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\t\"data\":\"dw_scheduler_pre.11195216_out\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"refTableName\":\"dw_scheduler_pre.11195216_out\"\n"
            + "\t\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\t\"data\":\"dw_scheduler_pre.11195231_out\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"recurrence\":\"Normal\",\n"
            + "\t\t\t\t\t\t\t\"id\":\"11195217\",\n"
            + "\t\t\t\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\t\t\t\"rerunTimes\":0,\n"
            + "\t\t\t\t\t\t\t\"rerunInterval\":0,\n"
            + "\t\t\t\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\t\t\t\"command\":\"CONTROLLER_TRAVERSE_END\"\n"
            + "\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\t\t\t\"type\":\"Manual\",\n"
            + "\t\t\t\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\t\t\t\"endTime\":\"9999-01-01 16:02:31\"\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"name\":\"end\",\n"
            + "\t\t\t\t\t\t\t\"owner\":\"068198\",\n"
            + "\t\t\t\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\t\"data\":\"dw_scheduler_pre.11195231_out\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"refTableName\":\"dw_scheduler_pre.11195231_out\"\n"
            + "\t\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\t\"data\":\"dw_scheduler_pre.11195217_out\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"recurrence\":\"Normal\",\n"
            + "\t\t\t\t\t\t\t\"id\":\"11195216\",\n"
            + "\t\t\t\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\t\t\t\"rerunTimes\":0,\n"
            + "\t\t\t\t\t\t\t\"rerunInterval\":0,\n"
            + "\t\t\t\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\t\t\t\"command\":\"CONTROLLER_TRAVERSE_START\"\n"
            + "\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\t\t\t\"type\":\"Manual\",\n"
            + "\t\t\t\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\t\t\t\"endTime\":\"9999-01-01 16:02:31\"\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"name\":\"start\",\n"
            + "\t\t\t\t\t\t\t\"owner\":\"068198\",\n"
            + "\t\t\t\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\t\t\t\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\t\"data\":\"dw_scheduler_pre.11195216_out\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\"\n"
            + "\t\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t],\n"
            + "\t\t\t\t\t\"flow\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"nodeId\":\"11195231\",\n"
            + "\t\t\t\t\t\t\t\"depends\":[\n"
            + "\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\"type\":\"Normal\",\n"
            + "\t\t\t\t\t\t\t\t\t\"output\":\"dw_scheduler_pre.11195216_out\"\n"
            + "\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"nodeId\":\"11195217\",\n"
            + "\t\t\t\t\t\t\t\"depends\":[\n"
            + "\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\"type\":\"Normal\",\n"
            + "\t\t\t\t\t\t\t\t\t\"output\":\"dw_scheduler_pre.11195231_out\"\n"
            + "\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"nodeId\":\"11195216\",\n"
            + "\t\t\t\t\t\t\t\"depends\":[\n"
            + "\t\t\t\t\t\t\t\t\n"
            + "\t\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t],\n"
            + "\t\t\t\t\t\"array\":{\n"
            + "\t\t\t\t\t\t\"name\":\"loopDataArray\",\n"
            + "\t\t\t\t\t\t\"artifactType\":\"Variable\",\n"
            + "\t\t\t\t\t\t\"scope\":\"NodeContext\",\n"
            + "\t\t\t\t\t\t\"type\":\"Constant\",\n"
            + "\t\t\t\t\t\t\"node\":{\n"
            + "\t\t\t\t\t\t\t\"nodeId\":\"11195215\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"referenceVariable\":{\n"
            + "\t\t\t\t\t\t\t\"name\":\"outputs\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"Variable\",\n"
            + "\t\t\t\t\t\t\t\"scope\":\"NodeContext\",\n"
            + "\t\t\t\t\t\t\t\"type\":\"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"node\":{\n"
            + "\t\t\t\t\t\t\t\t\"nodeId\":\"11195181\",\n"
            + "\t\t\t\t\t\t\t\t\"output\":\"dw_scheduler_pre.11195181_out\"\n"
            + "\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t}\n"
            + "\t\t\t}\n"
            + "\t\t],\n"
            + "\t\t\"flow\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"nodeId\":\"11195215\",\n"
            + "\t\t\t\t\"depends\":[\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"type\":\"Normal\",\n"
            + "\t\t\t\t\t\t\"output\":\"dw_scheduler_pre.11195181_out\"\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t]\n"
            + "\t\t\t}\n"
            + "\t\t]\n"
            + "\t},\n"
            + "\t\"metadata\":{\n"
            + "\t\t\"owner\":\"068198\"\n"
            + "\t}\n"
            + "}";
        Specification<Spec> spec = SpecUtil.parseToDomain(s);

        log.info("{}", SpecUtil.writeToSpec(spec));
        assertNotNull(spec);
        assertNotNull(spec.getContext());
        SpecWriterContext context = new SpecWriterContext();
        context.setVersion(spec.getVersion());
        log.info("{}",
            JSON.toJSONString(SpecUtil.write(((DataWorksWorkflowSpec)spec.getSpec()).getNodes().get(0), context),
                Feature.PrettyFormat));
    }

    @Test
    public void testParseForeach() {
        String json = "{\n"
            + "          \"nodes\": [\n"
            + "            {\n"
            + "              \"id\": \"d0e36d269ed0414d9acd08149f360129\",\n"
            + "              \"recurrence\": \"Normal\",\n"
            + "              \"timeout\": 12,\n"
            + "              \"instanceMode\": \"T+1\",\n"
            + "              \"rerunMode\": \"Allowed\",\n"
            + "              \"rerunTimes\": 3,\n"
            + "              \"rerunInterval\": 18000,\n"
            + "              \"script\": {\n"
            + "                \"path\": \"/遍历节点0/traverse_start\",\n"
            + "                \"runtime\": {\n"
            + "                  \"engine\": \"GENERAL\",\n"
            + "                  \"command\": \"CONTROLLER_TRAVERSE_START\"\n"
            + "                },\n"
            + "                \"parameters\": []\n"
            + "              },\n"
            + "              \"trigger\": {\n"
            + "                \"type\": \"Scheduler\",\n"
            + "                \"cron\": \"00 00 00 * * ?\",\n"
            + "                \"startTime\": \"1970-01-01 00:00:00\",\n"
            + "                \"endTime\": \"9999-01-01 00:00:00\",\n"
            + "                \"timezone\": \"Asia/Shanghai\"\n"
            + "              },\n"
            + "              \"runtimeResource\": {\n"
            + "                \"resourceGroup\": \"res_group_1\"\n"
            + "              },\n"
            + "              \"name\": \"traverse_start\",\n"
            + "              \"owner\": \"WORKER_1482465063962\",\n"
            + "              \"inputs\": {},\n"
            + "              \"outputs\": {\n"
            + "                \"nodeOutputs\": [\n"
            + "                  {\n"
            + "                    \"artifactType\": \"NodeOutput\",\n"
            + "                    \"data\": \"d0e36d269ed0414d9acd08149f360129\",\n"
            + "                    \"refTableName\": \"traverse_start\"\n"
            + "                  }\n"
            + "                ]\n"
            + "              },\n"
            + "              \"functions\": [],\n"
            + "              \"fileResources\": []\n"
            + "            },\n"
            + "            {\n"
            + "              \"id\": \"8401efef76224eacbf28cc284b11a788\",\n"
            + "              \"recurrence\": \"Normal\",\n"
            + "              \"timeout\": 12,\n"
            + "              \"instanceMode\": \"T+1\",\n"
            + "              \"rerunMode\": \"Allowed\",\n"
            + "              \"rerunTimes\": 3,\n"
            + "              \"rerunInterval\": 18000,\n"
            + "              \"script\": {\n"
            + "                \"path\": \"/遍历节点0/shell\",\n"
            + "                \"runtime\": {\n"
            + "                  \"engine\": \"GENERAL\",\n"
            + "                  \"command\": \"DIDE_SHELL\"\n"
            + "                },\n"
            + "                \"parameters\": []\n"
            + "              },\n"
            + "              \"trigger\": {\n"
            + "                \"type\": \"Scheduler\",\n"
            + "                \"cron\": \"00 00 00 * * ?\",\n"
            + "                \"startTime\": \"1970-01-01 00:00:00\",\n"
            + "                \"endTime\": \"9999-01-01 00:00:00\",\n"
            + "                \"timezone\": \"Asia/Shanghai\"\n"
            + "              },\n"
            + "              \"runtimeResource\": {\n"
            + "                \"resourceGroup\": \"res_group_1\"\n"
            + "              },\n"
            + "              \"name\": \"shell\",\n"
            + "              \"owner\": \"WORKER_1482465063962\",\n"
            + "              \"inputs\": {},\n"
            + "              \"outputs\": {\n"
            + "                \"nodeOutputs\": [\n"
            + "                  {\n"
            + "                    \"artifactType\": \"NodeOutput\",\n"
            + "                    \"data\": \"8401efef76224eacbf28cc284b11a788\",\n"
            + "                    \"refTableName\": \"shell\"\n"
            + "                  }\n"
            + "                ]\n"
            + "              },\n"
            + "              \"functions\": [],\n"
            + "              \"fileResources\": []\n"
            + "            },\n"
            + "            {\n"
            + "              \"id\": \"227b06c3ab0549e3b77731b0c828dcec\",\n"
            + "              \"recurrence\": \"Normal\",\n"
            + "              \"timeout\": 12,\n"
            + "              \"instanceMode\": \"T+1\",\n"
            + "              \"rerunMode\": \"Allowed\",\n"
            + "              \"rerunTimes\": 3,\n"
            + "              \"rerunInterval\": 18000,\n"
            + "              \"script\": {\n"
            + "                \"path\": \"/遍历节点0/traverse_end\",\n"
            + "                \"runtime\": {\n"
            + "                  \"engine\": \"GENERAL\",\n"
            + "                  \"command\": \"CONTROLLER_TRAVERSE_END\"\n"
            + "                },\n"
            + "                \"parameters\": []\n"
            + "              },\n"
            + "              \"trigger\": {\n"
            + "                \"type\": \"Scheduler\",\n"
            + "                \"cron\": \"00 00 00 * * ?\",\n"
            + "                \"startTime\": \"1970-01-01 00:00:00\",\n"
            + "                \"endTime\": \"9999-01-01 00:00:00\",\n"
            + "                \"timezone\": \"Asia/Shanghai\"\n"
            + "              },\n"
            + "              \"runtimeResource\": {\n"
            + "                \"resourceGroup\": \"res_group_1\"\n"
            + "              },\n"
            + "              \"name\": \"traverse_end\",\n"
            + "              \"owner\": \"WORKER_1482465063962\",\n"
            + "              \"inputs\": {},\n"
            + "              \"outputs\": {\n"
            + "                \"nodeOutputs\": [\n"
            + "                  {\n"
            + "                    \"artifactType\": \"NodeOutput\",\n"
            + "                    \"data\": \"227b06c3ab0549e3b77731b0c828dcec\",\n"
            + "                    \"refTableName\": \"traverse_end\"\n"
            + "                  }\n"
            + "                ]\n"
            + "              },\n"
            + "              \"functions\": [],\n"
            + "              \"fileResources\": []\n"
            + "            }\n"
            + "          ],\n"
            + "          \"flow\": [\n"
            + "            {\n"
            + "              \"nodeId\": \"8401efef76224eacbf28cc284b11a788\",\n"
            + "              \"depends\": [\n"
            + "                {\n"
            + "                  \"nodeId\": \"d0e36d269ed0414d9acd08149f360129\",\n"
            + "                  \"type\": \"Normal\"\n"
            + "                }\n"
            + "              ]\n"
            + "            },\n"
            + "            {\n"
            + "              \"nodeId\": \"227b06c3ab0549e3b77731b0c828dcec\",\n"
            + "              \"depends\": [\n"
            + "                {\n"
            + "                  \"nodeId\": \"8401efef76224eacbf28cc284b11a788\",\n"
            + "                  \"type\": \"Normal\"\n"
            + "                }\n"
            + "              ]\n"
            + "            }\n"
            + "          ]\n"
            + "        }";
        SpecForEach foreach = SpecUtil.parse(json, SpecForEach.class, new SpecParserContext());
        log.info("before: {}", json);
    }

    @Test
    public void testx() {
        String spec = "{\n"
            + "\t\"version\":\"1.1.0\",\n"
            + "\t\"kind\":\"TemporaryWorkflow\",\n"
            + "\t\"spec\":{\n"
            + "\t\t\"nodes\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"recurrence\":\"Normal\",\n"
            + "\t\t\t\t\"id\":\"5143110377713406119\",\n"
            + "\t\t\t\t\"timeout\":0,\n"
            + "\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\"rerunTimes\":3,\n"
            + "\t\t\t\t\"rerunInterval\":180000,\n"
            + "\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\"path\":\"聿剑/flow/flow6/f_ge_shell1\",\n"
            + "\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\"command\":\"DIDE_SHELL\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"content\":\"#!/bin/bash\\n#********************************************************************#\\n##author:聿剑\\n"
            + "##create time:2024-04-09 16:05:37\\n#********************************************************************#\\necho $1\",\n"
            + "\t\t\t\t\t\"id\":\"6138281211054878711\",\n"
            + "\t\t\t\t\t\"parameters\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"name\":\"flow_bizdate\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"Variable\",\n"
            + "\t\t\t\t\t\t\t\"scope\":\"NodeParameter\",\n"
            + "\t\t\t\t\t\t\t\"type\":\"System\",\n"
            + "\t\t\t\t\t\t\t\"value\":\"$[yyyymmdd-1]\",\n"
            + "\t\t\t\t\t\t\t\"id\":\"6584333807719816392\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\"type\":\"Scheduler\",\n"
            + "\t\t\t\t\t\"id\":\"4752762997864777554\",\n"
            + "\t\t\t\t\t\"cron\":\"00 00 00 * * ?\",\n"
            + "\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"endTime\":\"9999-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"timezone\":\"Asia/Shanghai\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"runtimeResource\":{\n"
            + "\t\t\t\t\t\"resourceGroup\":\"group_2\",\n"
            + "\t\t\t\t\t\"id\":\"5623679673296125496\",\n"
            + "\t\t\t\t\t\"resourceGroupId\":\"2\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"name\":\"f_ge_shell1\",\n"
            + "\t\t\t\t\"owner\":\"064152\",\n"
            + "\t\t\t\t\"metadata\":{\n"
            + "\t\t\t\t\t\"owner\":{\n"
            + "\t\t\t\t\t\t\"userId\":\"064152\",\n"
            + "\t\t\t\t\t\t\"userName\":\"聿剑\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"containerUuid\":\"8522335580915008505\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"5143110377713406119\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"refTableName\":\"f_ge_shell1\",\n"
            + "\t\t\t\t\t\t\t\"isDefault\":true\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t}\n"
            + "\t\t\t},\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"recurrence\":\"Normal\",\n"
            + "\t\t\t\t\"id\":\"7495526614688319692\",\n"
            + "\t\t\t\t\"timeout\":0,\n"
            + "\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\"rerunTimes\":3,\n"
            + "\t\t\t\t\"rerunInterval\":180000,\n"
            + "\t\t\t\t\"datasource\":{\n"
            + "\t\t\t\t\t\"name\":\"odps_first\",\n"
            + "\t\t\t\t\t\"type\":\"odps\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\"path\":\"聿剑/flow/flow6/f_mc_sql1\",\n"
            + "\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\"command\":\"ODPS_SQL\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"content\":\"--MaxCompute SQL\\n--********************************************************************--\\n--author: "
            + "聿剑\\n--create time: 2024-04-09 10:53:58\\n--********************************************************************--\\nSELECT "
            + "'${flow_bizdate}';\\n\",\n"
            + "\t\t\t\t\t\"id\":\"5724702094894912201\",\n"
            + "\t\t\t\t\t\"parameters\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"name\":\"flow_bizdate\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"Variable\",\n"
            + "\t\t\t\t\t\t\t\"scope\":\"NodeParameter\",\n"
            + "\t\t\t\t\t\t\t\"type\":\"System\",\n"
            + "\t\t\t\t\t\t\t\"value\":\"$[yyyymmdd-1]\",\n"
            + "\t\t\t\t\t\t\t\"id\":\"6584333807719816392\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\"type\":\"Scheduler\",\n"
            + "\t\t\t\t\t\"id\":\"8888865284073976707\",\n"
            + "\t\t\t\t\t\"cron\":\"00 00 00 * * ?\",\n"
            + "\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"endTime\":\"9999-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"timezone\":\"Asia/Shanghai\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"runtimeResource\":{\n"
            + "\t\t\t\t\t\"resourceGroup\":\"group_2\",\n"
            + "\t\t\t\t\t\"id\":\"5623679673296125496\",\n"
            + "\t\t\t\t\t\"resourceGroupId\":\"2\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"name\":\"f_mc_sql1\",\n"
            + "\t\t\t\t\"owner\":\"064152\",\n"
            + "\t\t\t\t\"metadata\":{\n"
            + "\t\t\t\t\t\"owner\":{\n"
            + "\t\t\t\t\t\t\"userId\":\"064152\",\n"
            + "\t\t\t\t\t\t\"userName\":\"聿剑\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"containerUuid\":\"8522335580915008505\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"7495526614688319692\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"refTableName\":\"f_mc_sql1\",\n"
            + "\t\t\t\t\t\t\t\"isDefault\":true\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t}\n"
            + "\t\t\t}\n"
            + "\t\t],\n"
            + "\t\t\"flow\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"nodeId\":\"5143110377713406119\",\n"
            + "\t\t\t\t\"depends\":[\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"type\":\"Normal\",\n"
            + "\t\t\t\t\t\t\"output\":\"7495526614688319692\"\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t]\n"
            + "\t\t\t}\n"
            + "\t\t],\n"
            + "\t\t\"variables\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"name\":\"flow_bizdate\",\n"
            + "\t\t\t\t\"artifactType\":\"Variable\",\n"
            + "\t\t\t\t\"scope\":\"NodeParameter\",\n"
            + "\t\t\t\t\"type\":\"System\",\n"
            + "\t\t\t\t\"value\":\"$[yyyymmdd-1]\"\n"
            + "\t\t\t}\n"
            + "\t\t]\n"
            + "\t},\n"
            + "\t\"metadata\":{\n"
            + "\t\t\"owner\":{\n"
            + "\t\t\t\"userId\":\"064152\",\n"
            + "\t\t\t\"userName\":\"聿剑\"\n"
            + "\t\t},\n"
            + "\t\t\"name\":\"fullflow2\",\n"
            + "\t\t\"tenantId\":\"1\",\n"
            + "\t\t\"type\":\"CycleWorkflow\",\n"
            + "\t\t\"uuid\":\"8522335580915008505\",\n"
            + "\t\t\"projectId\":\"23620\"\n"
            + "\t}\n"
            + "}";
        Specification<Spec> resp = SpecUtil.parseToDomain(spec);
        log.info("resp: {}", resp);
        assertNotNull(resp);
    }

    @Test
    public void testParseNodeWithComponent() {
        String spec = "{\n"
            + "    \"version\": \"1.1.0\",\n"
            + "    \"kind\": \"CycleWorkflow\",\n"
            + "    \"spec\": {\n"
            + "      \"nodes\": [\n"
            + "        {\n"
            + "          \"recurrence\": \"Normal\",\n"
            + "          \"id\": \"6289081068484952005\",\n"
            + "          \"timeout\": 0,\n"
            + "          \"instanceMode\": \"T+1\",\n"
            + "          \"rerunMode\": \"Allowed\",\n"
            + "          \"rerunTimes\": 3,\n"
            + "          \"rerunInterval\": 180000,\n"
            + "          \"datasource\": {\n"
            + "            \"name\": \"odps_first\",\n"
            + "            \"type\": \"odps\"\n"
            + "          },\n"
            + "          \"script\": {\n"
            + "            \"language\": \"odps-sql\",\n"
            + "            \"path\": \"昊祯/组件/c1\",\n"
            + "            \"runtime\": {\n"
            + "              \"command\": \"COMPONENT_SQL\",\n"
            + "              \"commandTypeId\": 1010\n"
            + "            },\n"
            + "            \"id\": \"6423534013528078585\"\n"
            + "          },\n"
            + "          \"trigger\": {\n"
            + "            \"type\": \"Scheduler\",\n"
            + "            \"id\": \"5065170306719262538\",\n"
            + "            \"cron\": \"00 00 00 * * ?\",\n"
            + "            \"startTime\": \"1970-01-01 00:00:00\",\n"
            + "            \"endTime\": \"9999-01-01 00:00:00\",\n"
            + "            \"timezone\": \"Asia/Shanghai\"\n"
            + "          },\n"
            + "          \"runtimeResource\": {\n"
            + "            \"resourceGroup\": \"wengzi_test\",\n"
            + "            \"id\": \"5700220827937093292\",\n"
            + "            \"resourceGroupId\": \"9527\"\n"
            + "          },\n"
            + "          \"name\": \"c1\",\n"
            + "          \"owner\": \"067848\",\n"
            + "          \"component\": {\n"
            + "            \"description\": \"11\",\n"
            + "            \"id\": \"6128718817130431653\",\n"
            + "            \"inputs\": [\n"
            + "              {\n"
            + "                \"name\": \"p1\"\n"
            + "              },\n"
            + "              {\n"
            + "                \"name\": \"p2\"\n"
            + "              }\n"
            + "            ],\n"
            + "            \"metadata\": {\n"
            + "              \"version\": \"3\"\n"
            + "            },\n"
            + "            \"name\": \"c1\",\n"
            + "            \"outputs\": [],\n"
            + "            \"owner\": \"067848\"\n"
            + "          },\n"
            + "          \"metadata\": {\n"
            + "            \"tenantId\": \"1\",\n"
            + "            \"projectId\": \"23620\"\n"
            + "          },\n"
            + "          \"inputs\": {\n"
            + "            \"nodeOutputs\": [\n"
            + "              {\n"
            + "                \"data\": \"dw_scheduler_pre_root\",\n"
            + "                \"artifactType\": \"NodeOutput\",\n"
            + "                \"isDefault\": false\n"
            + "              }\n"
            + "            ]\n"
            + "          },\n"
            + "          \"outputs\": {\n"
            + "            \"nodeOutputs\": [\n"
            + "              {\n"
            + "                \"data\": \"6289081068484952005\",\n"
            + "                \"artifactType\": \"NodeOutput\",\n"
            + "                \"refTableName\": \"c1\",\n"
            + "                \"isDefault\": true\n"
            + "              }\n"
            + "            ]\n"
            + "          }\n"
            + "        }\n"
            + "      ],\n"
            + "      \"flow\": [\n"
            + "        {\n"
            + "          \"nodeId\": \"6289081068484952005\",\n"
            + "          \"depends\": [\n"
            + "            {\n"
            + "              \"type\": \"Normal\",\n"
            + "              \"output\": \"dw_scheduler_pre_root\"\n"
            + "            }\n"
            + "          ]\n"
            + "        }\n"
            + "      ]\n"
            + "    },\n"
            + "    \"metadata\": {\n"
            + "      \"uuid\": \"6289081068484952005\"\n"
            + "    }\n"
            + "  }";

        Specification<DataWorksWorkflowSpec> specification = SpecUtil.parseToDomain(spec);
        Optional<SpecComponent> component = Optional.ofNullable(specification)
            .map(Specification::getSpec).map(DataWorksWorkflowSpec::getNodes)
            .flatMap(l -> l.stream().map(SpecNode::getComponent).filter(Objects::nonNull).findAny());

        assertTrue(component.isPresent());
        assertNotNull(component.get().getMetadata());
        assertNotNull(component.get().getId());
        assertNotNull(component.get().getInputs());
        assertNotNull(component.get().getOutputs());
    }

    @Test
    public void testWorkflow() {
        String spec = "{\n"
            + "    \"version\": \"1.1.0\",\n"
            + "    \"kind\": \"CycleWorkflow\",\n"
            + "    \"spec\": {\n"
            + "        \"workflows\": [\n"
            + "            {\n"
            + "                \"id\": \"flow_1\",\n"
            + "                \"name\": \"flow_1\",\n"
            + "                \"strategy\": {\n"
            + "                    \"failureStrategy\": \"Continue\"\n"
            + "                },\n"
            + "                \"script\": {\n"
            + "                    \"runtime\": {\n"
            + "                        \"command\": \"XxxWorkflow\",\n"
            + "                        \"commandTypeId\": 1111\n"
            + "                    },\n"
            + "                    \"parameters\": [\n"
            + "                        {\n"
            + "                            \"name\": \"p1\",\n"
            + "                            \"type\": \"System\",\n"
            + "                            \"scope\": \"Workflow\",\n"
            + "                            \"value\": \"$[yyyymmdd]\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"name\": \"p2\",\n"
            + "                            \"type\": \"Constant\",\n"
            + "                            \"scope\": \"Workflow\",\n"
            + "                            \"value\": \"ppp2\"\n"
            + "                        }\n"
            + "                    ]\n"
            + "                },\n"
            + "                \"runtimeResource\": {\n"
            + "                    \"resourceGroup\": \"group_xxx\"\n"
            + "                },\n"
            + "                \"trigger\": {\n"
            + "                    \"type\": \"Scheduler\",\n"
            + "                    \"cron\": \"00 00 00 * * ?\",\n"
            + "                    \"delaySeconds\": 10\n"
            + "                },\n"
            + "                \"inputs\": {},\n"
            + "                \"outputs\": {\n"
            + "                    \"nodeOutputs\": [\n"
            + "                        {\n"
            + "                            \"data\": \"autotest.workflow_1_xxx\"\n"
            + "                        }\n"
            + "                    ]\n"
            + "                },\n"
            + "                \"nodes\": [\n"
            + "                    {\n"
            + "                        \"id\": \"inner_node_1\",\n"
            + "                        \"name\": \"inner_node_1\",\n"
            + "                        \"script\": {\n"
            + "                            \"runtime\": {\n"
            + "                                \"command\": \"ODPS_SQL\"\n"
            + "                            }\n"
            + "                        },\n"
            + "                        \"trigger\": {\n"
            + "                            \"delay\": 10\n"
            + "                        }\n"
            + "                    }\n"
            + "                ],\n"
            + "                \"dependencies\": [\n"
            + "                    {\n"
            + "                        \"nodeId\": \"inner_node_1\",\n"
            + "                        \"depends\": [\n"
            + "                            {\n"
            + "                                \"type\": \"Normal\",\n"
            + "                                \"output\": \"autotest.inner_node_2_out\"\n"
            + "                            }\n"
            + "                        ]\n"
            + "                    }\n"
            + "                ]\n"
            + "            }\n"
            + "        ],\n"
            + "        \"flow\": [\n"
            + "            {\n"
            + "                \"nodeId\": \"flow_1\",\n"
            + "                \"depends\": [\n"
            + "                    {\n"
            + "                        \"type\": \"Normal\",\n"
            + "                        \"output\": \"autotest.node_1_xxx\"\n"
            + "                    }\n"
            + "                ]\n"
            + "            }\n"
            + "        ]\n"
            + "    }\n"
            + "}";
        log.info("spec: {}", spec);
        Specification<DataWorksWorkflowSpec> specification = SpecUtil.parseToDomain(spec);
        List<SpecWorkflow> workflows = specification.getSpec().getWorkflows();
        assertNotNull(workflows);
        assertEquals(1, workflows.size());

        log.info("workflows: {}", workflows);
        log.info("write spec: {}", SpecUtil.writeToSpec(specification));
        SpecWorkflow specWorkflow = workflows.get(0);
        assertNotNull(specWorkflow.getTrigger());
        assertNotNull(specWorkflow.getStrategy());
        assertNotNull(specWorkflow.getNodes());
        assertNotNull(specWorkflow.getDependencies());
        assertNotNull(specification.getSpec().getFlow());
        assertNotNull(specWorkflow.getStrategy().getFailureStrategy());
    }

    @Test
    public void testParseDepends() {
        String json = "[\n"
            + "  {\n"
            + "    \"nodeId\": \"123\"\n"
            + "  },\n"
            + "  {\n"
            + "    \"output\": \"autotest.1234_out\"\n"
            + "  }\n"
            + "]";
        List<SpecDepend> depends = ListUtils.emptyIfNull(JSONArray.parseArray(json)).stream().map(obj -> {
            SpecDepend dep = SpecUtil.parse(JSON.toJSONString(obj), SpecDepend.class, new SpecParserContext());
            log.info("dep: nodeId: {}, output: {}", Optional.ofNullable(dep.getNodeId()).map(SpecRefEntity::getId).orElse(null), dep.getOutput());
            return dep;
        }).collect(Collectors.toList());

        assertEquals(2, CollectionUtils.size(depends));
        assertEquals("123", depends.get(0).getNodeId().getId());
        assertEquals("autotest.1234_out", depends.get(1).getOutput().getData());
    }

    @Test
    public void testSpecWorkflow() {
        Specification<DataWorksWorkflowSpec> specification = new Specification<>();
        specification.setVersion(SpecVersion.V_1_1_0.getLabel());
        specification.setKind(SpecKind.CYCLE_WORKFLOW.getLabel());
        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        specification.setSpec(spec);
        SpecWorkflow specWorkflow = new SpecWorkflow();
        specWorkflow.setId("12");
        specWorkflow.setName("test");
        spec.setWorkflows(Collections.singletonList(specWorkflow));
        log.info("{}", SpecUtil.writeToSpec(specification));

        Specification<DataWorksWorkflowSpec> parsed = SpecUtil.parseToDomain(SpecUtil.writeToSpec(specification));
        assertNotNull(parsed);
        assertNotNull(parsed.getSpec());
        assertNotNull(parsed.getSpec().getWorkflows());
        assertTrue(CollectionUtils.isNotEmpty(parsed.getSpec().getWorkflows()));
        SpecWorkflow s = parsed.getSpec().getWorkflows().get(0);
        assertNotNull(s);
        assertNull(s.getNodes());
        assertNull(s.getDependencies());
    }

    @Test
    public void testSpecSchedulerStrategy() {
        SpecScheduleStrategy scheduleStrategy = new SpecScheduleStrategy();
        scheduleStrategy.setFailureStrategy(FailureStrategy.CONTINUE);
        JSONObject js = (JSONObject)SpecUtil.write(scheduleStrategy, new SpecWriterContext());
        log.info("js: {}", js.toJSONString());
        assertEquals("Continue", js.getString("failureStrategy"));
    }

    @Test
    public void testSingleNode() {
        String spec = "{\n"
            + "    \"version\":\"1.1.0\",\n"
            + "    \"kind\":\"Node\",\n"
            + "    \"spec\":{\n"
            + "      \"nodes\": [{\n"
            + "        \"id\": \"4744170535163410393\",\n"
            + "        \"recurrence\":\"Normal\",\n"
            + "        \"timeout\":0,\n"
            + "        \"instanceMode\":\"T+1\",\n"
            + "        \"rerunMode\":\"Allowed\",\n"
            + "        \"rerunTimes\":3,\n"
            + "        \"rerunInterval\":180000,\n"
            + "        \"owner\": \"064152\",\n"
            + "        \"script\":{\n"
            + "          \"path\":\"聿剑/dep1\",\n"
            + "          \"language\":\"odps-sql\",\n"
            + "          \"runtime\":{\n"
            + "            \"command\":\"ODPS_SQL\"\n"
            + "          }\n"
            + "        },\n"
            + "        \"trigger\":{\n"
            + "          \"type\":\"Scheduler\",\n"
            + "          \"cron\":\"00 00 00 * * ?\",\n"
            + "          \"startTime\":\"1970-01-01 00:00:00\",\n"
            + "          \"endTime\":\"9999-01-01 00:00:00\",\n"
            + "          \"timezone\":\"Asia/Shanghai\"\n"
            + "        },\n"
            + "        \"runtimeResource\":{\n"
            + "          \"resourceGroup\":\"group_2\",\n"
            + "          \"resourceGroupId\":\"2\"\n"
            + "        },\n"
            + "        \"name\":\"dep1\"\n"
            + "      }] ,    \n"
            + "      \"flow\":\n"
            + "        [{ \n"
            + "          \"nodeId\":\"1\",\n"
            + "          \"depends\":[\n"
            + "            {\n"
            + "              \"type\":\"Normal\",\n"
            + "              \"output\":\"4744170535163410393\",\n"
            + "              \"refTableName\":\"branch_2_pyodps\"\n"
            + "            },\n"
            + "            {\n"
            + "              \"type\":\"Normal\",\n"
            + "              \"output\":\"5910844902278897501\",\n"
            + "              \"refTableName\":\"branch_1_odpssql\"\n"
            + "            }\n"
            + "          ]\n"
            + "        }]\n"
            + "      \n"
            + "    }\n"
            + "  }";
        Specification<Spec> specObj = SpecUtil.parseToDomain(spec);
        SpecWriterContext context = new SpecWriterContext();
        context.setVersion("1.1.0");
        log.info("write spec: {}", SpecUtil.writeToSpec(specObj));
        assertNotNull(specObj);
        assertTrue(CollectionUtils.isNotEmpty(((DataWorksWorkflowSpec)specObj.getSpec()).getNodes()));
        assertTrue(CollectionUtils.isNotEmpty(((DataWorksWorkflowSpec)specObj.getSpec()).getFlow()));
    }

    @Test
    public void testWorkflows() {
        String spec = "{\n"
            + "\t\"version\":\"1.1.0\",\n"
            + "\t\"kind\":\"CycleWorkflow\",\n"
            + "\t\"spec\":{\n"
            + "\t\t\"name\":\"testflow0730_deploy_01\",\n"
            + "\t\t\"id\":\"8620630926993095479\",\n"
            + "\t\t\"type\":\"CycleWorkflow\",\n"
            + "\t\t\"owner\":\"1107550004253538\",\n"
            + "\t\t\"workflows\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\"path\":\"聿剑/testflow0730_deploy_01\",\n"
            + "\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\"command\":\"WORKFLOW\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"id\":\"5162322698918001755\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"id\":\"8620630926993095479\",\n"
            + "\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\"type\":\"Scheduler\",\n"
            + "\t\t\t\t\t\"id\":\"5971686020768809793\",\n"
            + "\t\t\t\t\t\"cron\":\"00 00 00 * * ?\",\n"
            + "\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"endTime\":\"9999-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"timezone\":\"Asia/Shanghai\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"strategy\":{\n"
            + "\t\t\t\t\t\"timeout\":0,\n"
            + "\t\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\t\"rerunTimes\":3,\n"
            + "\t\t\t\t\t\"rerunInterval\":180000,\n"
            + "\t\t\t\t\t\"failureStrategy\":\"Break\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"name\":\"testflow0730_deploy_01\",\n"
            + "\t\t\t\t\"owner\":\"1107550004253538\",\n"
            + "\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\":\"8620630926993095479\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"refTableName\":\"testflow0730_deploy_01\",\n"
            + "\t\t\t\t\t\t\t\"isDefault\":true\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"nodes\":[\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"recurrence\":\"Normal\",\n"
            + "\t\t\t\t\t\t\"id\":\"7751009343504221738\",\n"
            + "\t\t\t\t\t\t\"timeout\":0,\n"
            + "\t\t\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\t\t\"rerunTimes\":3,\n"
            + "\t\t\t\t\t\t\"rerunInterval\":180000,\n"
            + "\t\t\t\t\t\t\"datasource\":{\n"
            + "\t\t\t\t\t\t\t\"name\":\"odps_first\",\n"
            + "\t\t\t\t\t\t\t\"type\":\"\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\t\t\"path\":\"聿剑/testflow0730_deploy_01/mcsql_inner_02\",\n"
            + "\t\t\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\t\t\"command\":\"ODPS_SQL\",\n"
            + "\t\t\t\t\t\t\t\t\"commandTypeId\":10\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"id\":\"4646522489197098297\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\t\t\"type\":\"Scheduler\",\n"
            + "\t\t\t\t\t\t\t\"id\":\"6882497775385480901\",\n"
            + "\t\t\t\t\t\t\t\"cron\":\"00 00 00 * * ?\",\n"
            + "\t\t\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\t\t\"endTime\":\"9999-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\t\t\"timezone\":\"Asia/Shanghai\",\n"
            + "\t\t\t\t\t\t\t\"delaySeconds\":0\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"runtimeResource\":{\n"
            + "\t\t\t\t\t\t\t\"resourceGroup\":\"S_res_group_524257424564736_1681266742041\",\n"
            + "\t\t\t\t\t\t\t\"id\":\"8099134362653965803\",\n"
            + "\t\t\t\t\t\t\t\"resourceGroupId\":\"57214326\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"name\":\"mcsql_inner_02\",\n"
            + "\t\t\t\t\t\t\"owner\":\"1107550004253538\",\n"
            + "\t\t\t\t\t\t\"metadata\":{\n"
            + "\t\t\t\t\t\t\t\"container\":{\n"
            + "\t\t\t\t\t\t\t\t\"type\":\"Flow\",\n"
            + "\t\t\t\t\t\t\t\t\"uuid\":\"8620630926993095479\"\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"tenantId\":\"524257424564736\",\n"
            + "\t\t\t\t\t\t\t\"projectId\":\"295425\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\t\t\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\"data\":\"7751009343504221738\",\n"
            + "\t\t\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\t\t\"refTableName\":\"mcsql_inner_02\",\n"
            + "\t\t\t\t\t\t\t\t\t\"isDefault\":true\n"
            + "\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"recurrence\":\"Normal\",\n"
            + "\t\t\t\t\t\t\"id\":\"5641599010392971076\",\n"
            + "\t\t\t\t\t\t\"timeout\":0,\n"
            + "\t\t\t\t\t\t\"instanceMode\":\"T+1\",\n"
            + "\t\t\t\t\t\t\"rerunMode\":\"Allowed\",\n"
            + "\t\t\t\t\t\t\"rerunTimes\":3,\n"
            + "\t\t\t\t\t\t\"rerunInterval\":180000,\n"
            + "\t\t\t\t\t\t\"datasource\":{\n"
            + "\t\t\t\t\t\t\t\"name\":\"odps_first\",\n"
            + "\t\t\t\t\t\t\t\"type\":\"\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"script\":{\n"
            + "\t\t\t\t\t\t\t\"path\":\"聿剑/testflow0730_deploy_01/mcsql_inner_01\",\n"
            + "\t\t\t\t\t\t\t\"runtime\":{\n"
            + "\t\t\t\t\t\t\t\t\"command\":\"ODPS_SQL\",\n"
            + "\t\t\t\t\t\t\t\t\"commandTypeId\":10\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"id\":\"7359544718446803942\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"trigger\":{\n"
            + "\t\t\t\t\t\t\t\"type\":\"Scheduler\",\n"
            + "\t\t\t\t\t\t\t\"id\":\"5396511791028633183\",\n"
            + "\t\t\t\t\t\t\t\"cron\":\"00 00 00 * * ?\",\n"
            + "\t\t\t\t\t\t\t\"startTime\":\"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\t\t\"endTime\":\"9999-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\t\t\"timezone\":\"Asia/Shanghai\",\n"
            + "\t\t\t\t\t\t\t\"delaySeconds\":0\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"runtimeResource\":{\n"
            + "\t\t\t\t\t\t\t\"resourceGroup\":\"S_res_group_524257424564736_1681266742041\",\n"
            + "\t\t\t\t\t\t\t\"id\":\"8099134362653965803\",\n"
            + "\t\t\t\t\t\t\t\"resourceGroupId\":\"57214326\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"name\":\"mcsql_inner_01\",\n"
            + "\t\t\t\t\t\t\"owner\":\"1107550004253538\",\n"
            + "\t\t\t\t\t\t\"metadata\":{\n"
            + "\t\t\t\t\t\t\t\"container\":{\n"
            + "\t\t\t\t\t\t\t\t\"type\":\"Flow\",\n"
            + "\t\t\t\t\t\t\t\t\"uuid\":\"8620630926993095479\"\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"tenantId\":\"524257424564736\",\n"
            + "\t\t\t\t\t\t\t\"projectId\":\"295425\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"inputs\":{\n"
            + "\t\t\t\t\t\t\t\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"outputs\":{\n"
            + "\t\t\t\t\t\t\t\"nodeOutputs\":[\n"
            + "\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\"data\":\"5641599010392971076\",\n"
            + "\t\t\t\t\t\t\t\t\t\"artifactType\":\"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\t\t\"refTableName\":\"mcsql_inner_01\",\n"
            + "\t\t\t\t\t\t\t\t\t\"isDefault\":true\n"
            + "\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t],\n"
            + "\t\t\t\t\"dependencies\":[\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"nodeId\":\"7751009343504221738\",\n"
            + "\t\t\t\t\t\t\"depends\":[\n"
            + "\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\"type\":\"Normal\",\n"
            + "\t\t\t\t\t\t\t\t\"output\":\"5641599010392971076\",\n"
            + "\t\t\t\t\t\t\t\t\"refTableName\":\"mcsql_inner_01\"\n"
            + "\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"nodeId\":\"5641599010392971076\",\n"
            + "\t\t\t\t\t\t\"depends\":[\n"
            + "\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\"type\":\"Normal\",\n"
            + "\t\t\t\t\t\t\t\t\"output\":\"lwt_test_dd.504470094_out\"\n"
            + "\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t]\n"
            + "\t\t\t}\n"
            + "\t\t],\n"
            + "\t\t\"flow\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"nodeId\":\"8620630926993095479\",\n"
            + "\t\t\t\t\"depends\":[\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"type\":\"Normal\",\n"
            + "\t\t\t\t\t\t\"output\":\"lwt_test_dd_root\"\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t]\n"
            + "\t\t\t}\n"
            + "\t\t]\n"
            + "\t},\n"
            + "\t\"metadata\":{\n"
            + "\t\t\"innerVersion\":{\n"
            + "\t\t\t\"7751009343504221738\":1,\n"
            + "\t\t\t\"5641599010392971076\":4\n"
            + "\t\t},\n"
            + "\t\t\"gmtModified\":1722320416000,\n"
            + "\t\t\"tenantId\":\"524257424564736\",\n"
            + "\t\t\"projectId\":\"295425\",\n"
            + "\t\t\"uuid\":\"8620630926993095479\"\n"
            + "\t}\n"
            + "}";
        Specification<DataWorksWorkflowSpec> specification = SpecUtil.parseToDomain(spec);
        DataWorksNodeAdapter adapter = new DataWorksNodeAdapter(specification, specification.getSpec().getWorkflows().get(0));
        log.info("workflow inputs: {}", adapter.getInputs());
        assertTrue(CollectionUtils.isNotEmpty(adapter.getInputs()));

        List<SpecNode> nodes = specification.getSpec().getWorkflows().get(0).getNodes();
        for (SpecNode node : nodes) {
            DataWorksNodeAdapter nodeAdapter = new DataWorksNodeAdapter(specification, node);
            log.info("node: {}", nodeAdapter.getInputs());
            assertTrue(CollectionUtils.isNotEmpty(nodeAdapter.getInputs()));
        }
    }

    @Test
    public void testInnerNodes() {
        Specification<DataWorksWorkflowSpec> sp = new Specification<>();
        sp.setKind(SpecKind.CYCLE_WORKFLOW.getLabel());
        sp.setVersion(SpecVersion.V_1_2_0.getLabel());
        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        SpecNode node = new SpecNode();
        SpecSubFlow subflow = new SpecSubFlow();
        SpecNode subnode = new SpecNode();
        subnode.setId("subnode1");
        subnode.setName("subnode1");
        subflow.setNodes(Collections.singletonList(subnode));
        subflow.setId("12345");
        subflow.setCitable(true);
        subflow.setOutput("output");
        subflow.setName("subflow");
        node.setSubflow(subflow);
        spec.setNodes(Collections.singletonList(node));
        sp.setSpec(spec);
        JSONObject json = JSONObject.parseObject(SpecUtil.writeToSpec(sp));
        log.info("spec json: {}", json.toJSONString(Feature.PrettyFormat));
        assertNotNull(json);
        assertEquals(subnode.getId(), json.getByPath("$.spec.nodes[0].subflow.nodes[0].id"));
        assertEquals(subnode.getName(), json.getByPath("$.spec.nodes[0].subflow.nodes[0].name"));
        assertEquals(subflow.getId(), json.getByPath("$.spec.nodes[0].subflow.id"));
        assertEquals(subflow.getCitable(), json.getByPath("$.spec.nodes[0].subflow.citable"));
        assertEquals(subflow.getOutput(), json.getByPath("$.spec.nodes[0].subflow.output"));
        assertEquals(subflow.getName(), json.getByPath("$.spec.nodes[0].subflow.name"));

        Specification<DataWorksWorkflowSpec> parsed = SpecUtil.parseToDomain(json.toJSONString());
        assertNotNull(parsed);
        assertNotNull(parsed.getSpec());
        assertNotNull(parsed.getSpec().getNodes());
        assertNotNull(parsed.getSpec().getNodes().get(0).getSubflow());
        assertNotNull(parsed.getSpec().getNodes().get(0).getSubflow().getNodes());
        assertEquals(subnode.getId(), parsed.getSpec().getNodes().get(0).getSubflow().getNodes().get(0).getId());
        assertEquals(subnode.getName(), parsed.getSpec().getNodes().get(0).getSubflow().getNodes().get(0).getName());
    }

    @Test
    public void testFlowDependWithNoNodeId() {
        Specification<DataWorksWorkflowSpec> sp = new Specification<>();
        sp.setKind(SpecKind.CYCLE_WORKFLOW.getLabel());
        sp.setVersion(SpecVersion.V_1_2_0.getLabel());
        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        sp.setSpec(spec);
        SpecNode node = new SpecNode();
        spec.setNodes(Collections.singletonList(node));
        spec.setFlow(Collections.singletonList(new SpecFlowDepend()));
        JSONObject js = JSON.parseObject(SpecUtil.writeToSpec(sp));
        assertNotNull(js);
        assertTrue(js.containsKey("spec"));
        assertTrue(js.getJSONObject("spec").containsKey("flow"));
    }

    @Test
    public void testParserScriptParameterWithFromVariable() {
        String spec = "{\n"
            + "\t\"version\": \"1.1.0\",\n"
            + "\t\"kind\": \"CycleWorkflow\",\n"
            + "\t\"spec\": {\n"
            + "\t\t\"nodes\": [\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"recurrence\": \"Normal\",\n"
            + "\t\t\t\t\"id\": \"5087177253148138797\",\n"
            + "\t\t\t\t\"timeout\": 0,\n"
            + "\t\t\t\t\"instanceMode\": \"T+1\",\n"
            + "\t\t\t\t\"rerunMode\": \"Allowed\",\n"
            + "\t\t\t\t\"rerunTimes\": 3,\n"
            + "\t\t\t\t\"rerunInterval\": 120000,\n"
            + "\t\t\t\t\"script\": {\n"
            + "\t\t\t\t\t\"language\": \"odps-sql\",\n"
            + "\t\t\t\t\t\"path\": \"业务流程/Workflow/test11\",\n"
            + "\t\t\t\t\t\"runtime\": {\n"
            + "\t\t\t\t\t\t\"command\": \"ODPS_SQL\",\n"
            + "\t\t\t\t\t\t\"commandTypeId\": 10,\n"
            + "\t\t\t\t\t\t\"cu\": \"0.25\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"id\": \"6718099302539207849\",\n"
            + "\t\t\t\t\t\"parameters\": [\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"artifactType\": \"Variable\",\n"
            + "\t\t\t\t\t\t\t\"name\": \"aaaa\",\n"
            + "\t\t\t\t\t\t\t\"scope\": \"NodeContext\",\n"
            + "\t\t\t\t\t\t\t\"type\": \"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"value\": \"${outputs}\",\n"
            + "\t\t\t\t\t\t\t\"id\": \"6405143779358903358\",\n"
            + "\t\t\t\t\t\t\t\"from\": {\n"
            + "\t\t\t\t\t\t\t\t\"name\": \"outputs\",\n"
            + "\t\t\t\t\t\t\t\t\"scope\": \"NodeContext\",\n"
            + "\t\t\t\t\t\t\t\t\"type\": \"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\t\"value\": \"${outputs}\",\n"
            + "\t\t\t\t\t\t\t\t\"nodeUuid\": \"6649141278813321438\",\n"
            + "\t\t\t\t\t\t\t\t\"nodeOutput\": \"6649141278813321438\",\n"
            + "\t\t\t\t\t\t\t\t\"nodeName\": \"赋值11111\"\n"
            + "\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"trigger\": {\n"
            + "\t\t\t\t\t\"type\": \"Scheduler\",\n"
            + "\t\t\t\t\t\"id\": \"7632969190307402363\",\n"
            + "\t\t\t\t\t\"cron\": \"00 29 00 * * ?\",\n"
            + "\t\t\t\t\t\"startTime\": \"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"endTime\": \"9999-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"timezone\": \"GMT+8\",\n"
            + "\t\t\t\t\t\"delaySeconds\": 0\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"runtimeResource\": {\n"
            + "\t\t\t\t\t\"resourceGroup\": \"group_524257424564736\",\n"
            + "\t\t\t\t\t\"id\": \"8393194769092633224\",\n"
            + "\t\t\t\t\t\"resourceGroupId\": \"50414322\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"name\": \"test11\",\n"
            + "\t\t\t\t\"owner\": \"1107550004253538\",\n"
            + "\t\t\t\t\"metadata\": {\n"
            + "\t\t\t\t\t\"owner\": \"1107550004253538\",\n"
            + "\t\t\t\t\t\"ownerName\": \"dw_on_emr_qa3@test.aliyunid.com\",\n"
            + "\t\t\t\t\t\"createTime\": \"2025-03-26 16:17:58\",\n"
            + "\t\t\t\t\t\"tenantId\": \"524257424564736\",\n"
            + "\t\t\t\t\t\"project\": {\n"
            + "\t\t\t\t\t\t\"projectIdentifier\": \"test_upgrade_032502\",\n"
            + "\t\t\t\t\t\t\"projectName\": \"test_upgrade_032502\",\n"
            + "\t\t\t\t\t\t\"projectId\": \"723665\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"projectId\": \"723665\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"inputs\": {\n"
            + "\t\t\t\t\t\"nodeOutputs\": [\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\": \"test_upgrade_032502_root\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\": \"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"isDefault\": false\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t],\n"
            + "\t\t\t\t\t\"variables\": [\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"inputName\": \"aaaa\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\": \"Variable\",\n"
            + "\t\t\t\t\t\t\t\"name\": \"outputs\",\n"
            + "\t\t\t\t\t\t\t\"scope\": \"NodeContext\",\n"
            + "\t\t\t\t\t\t\t\"type\": \"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"value\": \"${outputs}\",\n"
            + "\t\t\t\t\t\t\t\"id\": \"6108609295212879780\",\n"
            + "\t\t\t\t\t\t\t\"node\": {\n"
            + "\t\t\t\t\t\t\t\t\"nodeId\": \"6649141278813321438\",\n"
            + "\t\t\t\t\t\t\t\t\"output\": \"6649141278813321438\",\n"
            + "\t\t\t\t\t\t\t\t\"refTableName\": \"赋值11111\"\n"
            + "\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"outputs\": {\n"
            + "\t\t\t\t\t\"nodeOutputs\": [\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\": \"5087177253148138797\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\": \"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"refTableName\": \"test11\",\n"
            + "\t\t\t\t\t\t\t\"isDefault\": true\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\": \"test_upgrade_032502.test11\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\": \"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"refTableName\": \"test11\",\n"
            + "\t\t\t\t\t\t\t\"isDefault\": false\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t}\n"
            + "\t\t\t}\n"
            + "\t\t],\n"
            + "\t\t\"flow\": [\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"nodeId\": \"5087177253148138797\",\n"
            + "\t\t\t\t\"depends\": [\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"type\": \"Normal\",\n"
            + "\t\t\t\t\t\t\"output\": \"6649141278813321438\",\n"
            + "\t\t\t\t\t\t\"refTableName\": \"赋值11111\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"type\": \"Normal\",\n"
            + "\t\t\t\t\t\t\"output\": \"test_upgrade_032502_root\"\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t]\n"
            + "\t\t\t}\n"
            + "\t\t]\n"
            + "\t}\n"
            + "}";
        Specification<DataWorksWorkflowSpec> specification = SpecUtil.parseToDomain(spec);
        assertNotNull(specification);
        assertNotNull(specification.getSpec());
        assertNotNull(specification.getSpec().getNodes());
        SpecNode node = specification.getSpec().getNodes().get(0);
        assertNotNull(node);
        log.info("node: {}", JSON.toJSONString(SpecUtil.write(node, new SpecWriterContext()), Feature.PrettyFormat));
    }

    @Test
    public void testWriteTableDomainToSpec() {
        SpecTable tableSpec = new SpecTable();
        tableSpec.setEngineType("ODPS");
        tableSpec.setEntityType("TABLE");
        tableSpec.setLogicTableUuid("logicTableUuid");
        tableSpec.setName("name");
        tableSpec.setGuid("tableGuid");
        tableSpec.setDdl("create table ...");
        tableSpec.setHasPartition(false);
        tableSpec.setIsVisible(false);
        tableSpec.setId("tableId");

        SpecCalcEngine specCalcEngine = new SpecCalcEngine();
        SpecDatasource specDatasource = new SpecDatasource();
        specDatasource.setId("datasourceId");
        specCalcEngine.setDatasource(specDatasource);
        tableSpec.setCalcEngine(specCalcEngine);

        Specification<DataWorksWorkflowSpec> specification = new Specification<DataWorksWorkflowSpec>();
        specification.setKind(SpecKind.TABLE.getLabel());
        specification.setVersion("1.0");
        DataWorksWorkflowSpec dataWorksWorkflowSpec = new DataWorksWorkflowSpec();
        dataWorksWorkflowSpec.setTables(Lists.newArrayList(tableSpec));
        specification.setSpec(dataWorksWorkflowSpec);

        String spec = SpecUtil.writeToSpec(specification);
        assertEquals("{\n"
            + "\t\"version\":\"1.0\",\n"
            + "\t\"kind\":\"Table\",\n"
            + "\t\"spec\":{\n"
            + "\t\t\"tables\":[\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"name\":\"name\",\n"
            + "\t\t\t\t\"artifactType\":\"Table\",\n"
            + "\t\t\t\t\"guid\":\"tableGuid\",\n"
            + "\t\t\t\t\"ddl\":\"create table ...\",\n"
            + "\t\t\t\t\"hasPartition\":false,\n"
            + "\t\t\t\t\"isVisible\":false,\n"
            + "\t\t\t\t\"calcEngine\":{\n"
            + "\t\t\t\t\t\"datasource\":{\n"
            + "\t\t\t\t\t\t\"id\":\"datasourceId\"\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"engineType\":\"ODPS\",\n"
            + "\t\t\t\t\"entityType\":\"TABLE\",\n"
            + "\t\t\t\t\"logicTableUuid\":\"logicTableUuid\",\n"
            + "\t\t\t\t\"id\":\"tableId\"\n"
            + "\t\t\t}\n"
            + "\t\t]\n"
            + "\t}\n"
            + "}", spec);
    }

    @Test
    public void testParseTableSpecToDomain() {
        String tableSpec = "{\n"
            + "  \"version\" : \"1.0\",\n"
            + "  \"kind\" : \"Table\",\n"
            + "  \"spec\" : {\n"
            + "    \"tables\" : [ {\n"
            + "      \"name\" : \"name\",\n"
            + "      \"artifactType\" : \"Table\",\n"
            + "      \"guid\" : \"tableGuid\",\n"
            + "      \"ddl\" : \"create table ...\",\n"
            + "      \"hasPartition\" : false,\n"
            + "      \"isVisible\" : false,\n"
            + "      \"calcEngine\" : {\n"
            + "        \"datasource\" : {\n"
            + "          \"id\" : \"datasourceId\"\n"
            + "        }\n"
            + "      },\n"
            + "      \"engineType\" : \"ODPS\",\n"
            + "      \"entityType\" : \"TABLE\",\n"
            + "      \"logicTableUuid\" : \"logicTableUuid\",\n"
            + "      \"id\" : \"tableId\"\n"
            + "    } ]\n"
            + "  }\n"
            + "}";
        Specification<DataWorksWorkflowSpec> specSpecification = SpecUtil.parseToDomain(tableSpec);
        assertEquals("1.0", specSpecification.getVersion());
        assertEquals(SpecKind.TABLE.getLabel(), specSpecification.getKind());
        assertNotNull(specSpecification.getSpec());

        DataWorksWorkflowSpec dataWorksWorkflowSpec = specSpecification.getSpec();
        SpecTable spec = dataWorksWorkflowSpec.getTables().get(0);
        assertEquals("ODPS", spec.getEngineType());
        assertEquals("TABLE", spec.getEntityType());
        assertEquals("logicTableUuid", spec.getLogicTableUuid());
        assertEquals("name", spec.getName());
        assertEquals("tableGuid", spec.getGuid());
        assertEquals("create table ...", spec.getDdl());
        Assert.assertFalse(spec.getHasPartition());
        Assert.assertFalse(spec.getIsVisible());
        assertEquals("tableId", spec.getId());
        assertNotNull(spec.getCalcEngine());
        assertNotNull(spec.getCalcEngine().getDatasource());
        assertEquals("datasourceId", spec.getCalcEngine().getDatasource().getId());
    }

    @Test
    public void testSourceType() {
        Specification<DataWorksWorkflowSpec> specObj = new Specification<>();
        specObj.setKind(SpecKind.CYCLE_WORKFLOW.getLabel());
        specObj.setVersion(SpecVersion.V_1_2_0.getLabel());
        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        specObj.setSpec(spec);
        SpecWriterContext context = new SpecWriterContext();
        SpecificationWriter writer = new SpecificationWriter(context);
        SpecNode node1 = new SpecNode();
        node1.setName("node1");
        node1.setId("node1");
        SpecNodeOutput input1 = new SpecNodeOutput();
        input1.setData("autotest.12345_out");
        input1.setSourceType(SourceType.CODE_PARSE);
        SpecVariable outVar1 = new SpecVariable();
        outVar1.setName("outputvar1");
        outVar1.setSourceType(SourceType.SYSTEM);
        node1.setInputs(List.of(input1));
        node1.setOutputs(List.of(outVar1));
        spec.setNodes(List.of(node1));
        SpecFlowDepend flow = new SpecFlowDepend();
        flow.setNodeId(node1);
        SpecDepend depend1 = new SpecDepend();
        depend1.setOutput(input1);
        depend1.setSourceType(input1.getSourceType());
        depend1.setType(DependencyType.NORMAL);
        flow.setDepends(List.of(depend1));
        spec.setFlow(List.of(flow));
        JSONObject outputSpec = writer.write(specObj, context);
        System.out.println(outputSpec.toJSONString(Feature.PrettyFormat));
        assertEquals(SourceType.CODE_PARSE.getLabel(), outputSpec.getByPath("spec.nodes[0].inputs.nodeOutputs[0].sourceType"));
        assertEquals(SourceType.SYSTEM.getLabel(), outputSpec.getByPath("spec.nodes[0].outputs.variables[0].sourceType"));
        assertEquals(SourceType.CODE_PARSE.getLabel(), outputSpec.getByPath("spec.flow[0].depends[0].sourceType"));
    }

    @Test
    public void testSourceTypeParse() throws IOException {
        String spec = FileUtils.readFileToString(
            new File(getClass().getClassLoader().getResource("spec_with_sourcetype.json").getFile()),
            StandardCharsets.UTF_8);
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        assertNotNull(specObj);
        assertNotNull(specObj.getSpec());
        assertNotNull(specObj.getSpec().getNodes());
        assertTrue(specObj.getSpec().getNodes().get(0).getOutputs().stream()
            .filter(o -> o instanceof SpecNodeOutput)
            .anyMatch(o -> ((SpecNodeOutput)o).getSourceType() != null));
        assertTrue(specObj.getSpec().getFlow().stream()
            .anyMatch(dep -> dep.getDepends().stream().anyMatch(d -> SourceType.CODE_PARSE.equals(d.getSourceType()))));
        assertTrue(specObj.getSpec().getFlow().stream()
            .anyMatch(dep -> dep.getDepends().stream().anyMatch(d -> SourceType.SYSTEM.equals(d.getSourceType()))));
        assertTrue(specObj.getSpec().getFlow().stream()
            .anyMatch(dep -> dep.getDepends().stream().anyMatch(d -> SourceType.MANUAL.equals(d.getSourceType()))));
    }

    @Test
    public void testFlowDependSourceType() {
        String spec = "{\n"
            + "\t\"kind\": \"CycleWorkflow\",\n"
            + "\t\"version\": \"1.2.0\",\n"
            + "\t\"spec\": {\n"
            + "\t\t\"nodes\": [\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"recurrence\": \"Normal\",\n"
            + "\t\t\t\t\"id\": \"6351452289056114903\",\n"
            + "\t\t\t\t\"timeout\": 0,\n"
            + "\t\t\t\t\"instanceMode\": \"T+1\",\n"
            + "\t\t\t\t\"rerunMode\": \"Allowed\",\n"
            + "\t\t\t\t\"rerunTimes\": 3,\n"
            + "\t\t\t\t\"rerunInterval\": 120000,\n"
            + "\t\t\t\t\"datasource\": {\n"
            + "\t\t\t\t\t\"name\": \"ram_mc_0512\",\n"
            + "\t\t\t\t\t\"type\": \"odps\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"script\": {\n"
            + "\t\t\t\t\t\"language\": \"odps-sql\",\n"
            + "\t\t\t\t\t\"path\": \"自动解析/odps02\",\n"
            + "\t\t\t\t\t\"runtime\": {\n"
            + "\t\t\t\t\t\t\"command\": \"ODPS_SQL\",\n"
            + "\t\t\t\t\t\t\"commandTypeId\": 10,\n"
            + "\t\t\t\t\t\t\"maxComputeConf\": {\n"
            + "\t\t\t\t\t\t\t\"quota\": {\n"
            + "\t\t\t\t\t\t\t\t\"id\": \"默认后付费Quota\",\n"
            + "\t\t\t\t\t\t\t\t\"name\": \"默认后付费Quota\",\n"
            + "\t\t\t\t\t\t\t\t\"key\": \"mcQuota\"\n"
            + "\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\"cu\": \"0.25\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"id\": \"9012310767637938371\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"trigger\": {\n"
            + "\t\t\t\t\t\"type\": \"Scheduler\",\n"
            + "\t\t\t\t\t\"cron\": \"00 27 00 * * ?\",\n"
            + "\t\t\t\t\t\"startTime\": \"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"endTime\": \"9999-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\"timezone\": \"GMT+8\",\n"
            + "\t\t\t\t\t\"delaySeconds\": 0\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"runtimeResource\": {\n"
            + "\t\t\t\t\t\"resourceGroup\": \"S_res_group_524257424564736_1698737937461\",\n"
            + "\t\t\t\t\t\"resourceGroupId\": \"64814320\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"name\": \"odps02\",\n"
            + "\t\t\t\t\"owner\": \"225199750534320298\",\n"
            + "\t\t\t\t\"metadata\": {\n"
            + "\t\t\t\t\t\"owner\": \"225199750534320298\",\n"
            + "\t\t\t\t\t\"ownerName\": \"ram_test2\",\n"
            + "\t\t\t\t\t\"createTime\": \"2025-06-24 15:13:36\",\n"
            + "\t\t\t\t\t\"tenantId\": \"524257424564736\",\n"
            + "\t\t\t\t\t\"project\": {\n"
            + "\t\t\t\t\t\t\"projectIdentifier\": \"update_test12\",\n"
            + "\t\t\t\t\t\t\"projectName\": \"update_test12\",\n"
            + "\t\t\t\t\t\t\"projectId\": \"624677\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t\"projectId\": \"624677\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"inputs\": {},\n"
            + "\t\t\t\t\"outputs\": {\n"
            + "\t\t\t\t\t\"nodeOutputs\": [\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\": \"6351452289056114903\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\": \"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"sourceType\": \"System\",\n"
            + "\t\t\t\t\t\t\t\"refTableName\": \"odps02\",\n"
            + "\t\t\t\t\t\t\t\"isDefault\": true\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\": \"ram_mc_0512.mctable01\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\": \"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"sourceType\": \"CodeParse\",\n"
            + "\t\t\t\t\t\t\t\"refTableName\": \"ram_mc_0512.mctable01\"\n"
            + "\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"data\": \"update_test12.odps02cp\",\n"
            + "\t\t\t\t\t\t\t\"artifactType\": \"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\"sourceType\": \"Manual\",\n"
            + "\t\t\t\t\t\t\t\"refTableName\": \"odps02\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t}\n"
            + "\t\t\t}\n"
            + "\t\t],\n"
            + "\t\t\"flow\": [\n"
            + "\t\t\t{\n"
            + "\t\t\t\t\"nodeId\": \"6351452289056114903\",\n"
            + "\t\t\t\t\"depends\": [\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"type\": \"Normal\",\n"
            + "\t\t\t\t\t\t\"output\": \"ram_mc_0512.mctable03\",\n"
            + "\t\t\t\t\t\t\"sourceType\": \"CodeParse\",\n"
            + "\t\t\t\t\t\t\"refTableName\": \"ram_mc_0512.mctable03\"\n"
            + "\t\t\t\t\t},\n"
            + "\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\"type\": \"Normal\",\n"
            + "\t\t\t\t\t\t\"output\": \"update_test12_root\"\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t]\n"
            + "\t\t\t}\n"
            + "\t\t]\n"
            + "\t}\n"
            + "}";
        Specification<DataWorksWorkflowSpec> sp = SpecUtil.parseToDomain(spec);
        DataWorksNodeAdapter adapter = new DataWorksNodeAdapter(sp, sp.getSpec().getNodes().get(0),
            Context.builder().deployToScheduler(true).build());
        log.info("inputs: {}", adapter.getInputs());
        assertNotNull(adapter.getInputs());
        assertTrue(adapter.getInputs().stream().map(i -> (SpecNodeOutput)i).anyMatch(i -> SourceType.CODE_PARSE.equals(i.getSourceType())));
    }

    @Test
    public void testMetadataSerializationAndFieldOrder() {
        Specification<DataWorksWorkflowSpec> sp1 = new Specification<>();
        sp1.setKind(SpecKind.CYCLE_WORKFLOW.getLabel());
        sp1.setVersion(SpecVersion.V_2_0_0.getLabel());
        sp1.setSpec(new DataWorksWorkflowSpec());

        Map<String, Object> meta1 = new HashMap<>();
        meta1.put("owner", "1223");
        meta1.put("ownerName", "2222");
        Map<String, Object> project1 = new HashMap<>();
        project1.put("projectId", "111");
        project1.put("projectName", "name");
        meta1.put("projectId", "111");
        meta1.put("project", project1);
        meta1.put("aaa", 1212);

        sp1.getSpec().setMetadata(meta1);
        sp1.getSpec().setId("1111");
        sp1.getSpec().setName("name");
        String s1 = SpecUtil.writeToSpec(sp1);
        log.info("s1: {}", s1);

        Specification<DataWorksWorkflowSpec> sp2 = new Specification<>();
        sp2.setKind(SpecKind.CYCLE_WORKFLOW.getLabel());
        sp2.setVersion(SpecVersion.V_2_0_0.getLabel());
        sp2.setSpec(new DataWorksWorkflowSpec());

        Map<String, Object> meta2 = new HashMap<>();
        meta2.put("owner", "1223");
        meta2.put("ownerName", "2222");

        Map<String, Object> project2 = new HashMap<>();
        project2.put("projectId", "111");
        project2.put("projectName", "name");
        meta2.put("projectId", "111");
        meta2.put("project", project2);

        meta2.put("ownerName", "2222");
        meta2.put("schedulerNodeId", "123123");
        meta2.put("qqqq", "111");
        meta2.put("rr", "11");
        sp2.getSpec().setMetadata(meta2);
        sp2.getSpec().setId("1111");
        sp2.getSpec().setName("name1");
        String s2 = SpecUtil.writeToSpec(sp2);
        log.info("s2: {}", s2);

        // assert field orders
        assertTrue(StringUtils.indexOf(s2, "\"projectId\":\"111\",\n"
            + "\t\t\t\"qqqq\":\"111\",\n"
            + "\t\t\t\"rr\":\"11\",\n"
            + "\t\t\t\"schedulerNodeId\":\"123123\"") > 0);
    }
}