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
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Spec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.Code;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.nodemodel.DataWorksNodeCodeAdapter;
import com.aliyun.dataworks.common.spec.domain.enums.FunctionType;
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
import com.aliyun.dataworks.common.spec.domain.ref.SpecArtifact;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDatasource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDqcRule;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFunction;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecRuntimeResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import com.aliyun.dataworks.common.spec.writer.Writer;
import com.aliyun.dataworks.common.spec.writer.WriterFactory;
import com.aliyun.dataworks.common.spec.writer.impl.SpecificationWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertNotNull(specification);
        Assert.assertNotNull(specObj.getVersion());
        Assert.assertNotNull(specObj.getKind());
        Assert.assertNotNull(specObj.getMetadata());
        Assert.assertNotNull(specification.getVariables());
        Assert.assertNotNull(specification.getScripts());
        Assert.assertNotNull(specification.getRuntimeResources());
        Assert.assertNotNull(specification.getTriggers());
        Assert.assertNotNull(specification.getArtifacts());
        Assert.assertNotNull(specification.getNodes());
        Assert.assertNotNull(specification.getFlow());

        String label = specObj.getKind();
        Assert.assertEquals(label.toLowerCase(), "CycleWorkflow".toLowerCase());

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
            Assert.assertTrue(input instanceof SpecArtifact || input instanceof SpecVariable);
        }

        SpecScript scriptFile1 = specification.getScripts().stream().filter(s -> s.getId().equals("script_file1"))
            .findFirst().get();
        Assert.assertSame(scriptFile1, specNode_node_1.getScript());

        // 变量的类型是否正确
        Assert.assertNotNull(specNode_node_1.getPriority());

        Assert.assertTrue(specNode_node_1.getPriority().equals(7));

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

        Assert.assertNotNull(specNode_node_1.getDatasource());
        Assert.assertNotNull(specNode_node_1.getDatasource().getName());

        Assert.assertTrue(specification.getNodes().stream().anyMatch(n -> n.getOwner() != null));
        Assert.assertTrue(specification.getNodes().stream().anyMatch(n -> n.getRerunTimes() != null));
        Assert.assertTrue(specification.getNodes().stream().anyMatch(n -> n.getRerunInterval() != null));

        System.out.println(SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testBranch() {
        String spec = readJson("branch.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        DataWorksWorkflowSpec specification = specObj.getSpec();
        Assert.assertNotNull(specification);
        Assert.assertNotNull(specification.getArtifacts());
        Assert.assertNotNull(specification.getScripts());
        Assert.assertNotNull(specification.getNodes());
        Assert.assertNotNull(specification.getFlow());

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
        Assert.assertNotNull(specification);
        // node
        Assert.assertNotNull(specification.getNodes());
        List<SpecNode> nodes = specification.getNodes();
        Assert.assertEquals(nodes.size(), 4);

        SpecNode specNode1 = nodes.get(0);
        // reference
        Assert.assertNotNull(specNode1.getReference());
        // script
        SpecNode specNode2 = nodes.get(1);
        Assert.assertNotNull(specNode2.getScript());
        // script runtime
        Assert.assertNotNull(specNode2.getScript().getRuntime());
        // parameters
        Assert.assertEquals(4, specNode2.getScript().getParameters().size());
        // input
        Assert.assertEquals(1, specNode2.getInputs().size());
        // output
        Assert.assertEquals(1, specNode2.getOutputs().size());
        // trigger
        Assert.assertNotNull(specNode2.getTrigger());
        // runtimeResource
        Assert.assertNotNull(specNode2.getRuntimeResource());

        // flow
        Assert.assertNotNull(specification.getFlow());

        SpecWriterContext context = new SpecWriterContext();
        SpecificationWriter writer = new SpecificationWriter(context);
        System.out.println(writer.write(specObj, context).toJSONString(Feature.PrettyFormat));
    }

    @Test
    public void testInnerFlow() {
        String spec = readJson("innerflow.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        DataWorksWorkflowSpec specification = specObj.getSpec();

        Assert.assertNotNull(specification);

        List<SpecNode> nodes = specification.getNodes();
        // do-while node
        Assert.assertEquals(4, nodes.size());
        SpecNode doWhile1 = nodes.stream().filter(n -> n.getId().equals("do_while_1")).findFirst().get();
        // do-while not null
        Assert.assertNotNull(doWhile1.getDoWhile());
        SpecDoWhile doWhile = doWhile1.getDoWhile();
        // nodes not null
        Assert.assertNotNull(doWhile.getNodes());
        // script
        SpecScript script_sql1 = specification.getScripts().stream().filter(s -> s.getId().equals("sql1")).findFirst()
            .get();
        SpecNode specNode1 = doWhile.getNodes().stream().filter(specNode -> specNode.getId().equals("sql1")).findFirst()
            .get();

        Assert.assertSame(specNode1.getScript(), script_sql1);

        // while
        Assert.assertNotNull(doWhile.getSpecWhile());
        SpecScript script_end = specification.getScripts().stream().filter(s -> s.getId().equals("end")).findFirst()
            .get();
        Assert.assertSame(doWhile.getSpecWhile().getScript(), script_end);

        // flow
        Assert.assertNotNull(doWhile.getFlow());
        Assert.assertNotNull(doWhile.getFlow().get(0).getNodeId());

        // for-each node
        SpecNode node_foreach_1 = nodes.stream().filter(n -> n.getId().equals("foreach_1")).findFirst().get();

        Assert.assertNotNull(node_foreach_1.getForeach());
        SpecForEach foreach = node_foreach_1.getForeach();

        Assert.assertNotNull(foreach.getArray());
        Assert.assertNotNull(foreach.getNodes());
        Assert.assertNotNull(foreach.getFlow());

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

        Assert.assertNotNull(specification);

        // join node
        SpecNode join_1 = specification.getNodes().stream().filter(n -> n.getId().equals("join_1")).findFirst().get();

        Assert.assertNotNull(join_1.getJoin().getLogic());
        SpecLogic logic = join_1.getJoin().getLogic();

        Assert.assertNotNull(logic.getExpression());

        // and nodeId 是否是同一对象
        SpecJoinBranch specAnd = join_1.getJoin().getBranches().stream().filter(a -> a.getNodeId().getId().equals("join_branch_1")).findFirst().get();
        SpecNode branch_1 = specification.getNodes().stream().filter(n -> n.getId().equals("join_branch_1")).findFirst().get();
        Assert.assertSame(specAnd.getNodeId(), branch_1);

        // or nodeId 是否是同一对象
        SpecJoinBranch specOr = join_1.getJoin().getBranches().stream().filter(a -> a.getNodeId().getId().equals("join_branch_3")).findFirst().get();

        SpecNode branch_3 = specification.getNodes().stream().filter(n -> n.getId().equals("join_branch_3")).findFirst().get();

        Assert.assertSame(specOr.getNodeId(), branch_3);

        // or的 status是否存在
        Assert.assertNotNull(specOr.getAssertion());

        System.out.println("join spec: " + SpecUtil.writeToSpec(specObj));
        Assert.assertTrue(join_1.getJoin().getBranches().stream().allMatch(b -> StringUtils.isNotBlank(b.getName())));
    }

    @Test
    public void testManual() {
        String spec = readJson("manual_flow.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);
        Assert.assertNotNull(specObj.getSpec().getId());
        Assert.assertEquals(specObj.getKind(), SpecKind.MANUAL_WORKFLOW.getLabel());
    }

    @Test
    public void testParameter_node() {
        String spec = readJson("parameter_node.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        DataWorksWorkflowSpec specification = specObj.getSpec();

        Assert.assertNotNull(specification);
        SpecVariable ctx_var_2 = specification.getVariables().stream().filter(n -> n.getId().equals("ctx_var_2"))
            .findFirst().get();

        Assert.assertSame(ctx_var_2.getScope(), VariableScopeType.NODE_PARAMETER);

    }

    @Test
    public void testDefaultParser() {
        String spec = readJson("newSimple.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        Assert.assertNotNull(specification);
    }

    @Test
    public void testSimpleDemo() {
        String spec = readJson("simpleDemo.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();

        Assert.assertNotNull(specification);

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
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        String str = SpecUtil.writeToSpec(specObj);
        System.out.println(str);

        specification.getFlow().forEach(flow -> System.out.println(JSON.toJSONString(flow.getDepends().get(0).getOutput(), Feature.PrettyFormat)));
    }

    @Test
    public void testScriptRuntimeTemplate() {
        String spec = readJson("script_runtime_template.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();

        SpecScript script = specification.getScripts().get(0);

        System.out.println(script.getRuntime().getTemplate());

        Assert.assertNotNull(script.getRuntime().getTemplate());

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
            Assert.assertNotNull(specification);

        });
    }

    @Test
    public void testCombinedNode() {
        String spec = readJson("combined_node.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        Assert.assertNotNull(specification);
        Assert.assertNotNull(specification.getNodes());
        Assert.assertEquals(1, specification.getNodes().size());

        SpecNode combined = specification.getNodes().get(0);
        Assert.assertNotNull(combined.getCombined());

        Assert.assertNotNull(combined.getCombined().getNodes());
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
        Assert.assertNotNull(paramHub);
        Assert.assertNotNull(paramHub.getVariables());
        Assert.assertNotNull(paramHub.getVariables().stream().filter(v -> v.getName().equals("outputs")).findFirst()
            .map(SpecVariable::getReferenceVariable).map(SpecVariable::getNode).map(SpecDepend::getOutput).map(SpecNodeOutput::getData)
            .orElse(null));
    }

    @Test
    public void testParamHub() {
        String spec = readJson("param_hub.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();

        Assert.assertNotNull(specification);

        System.out.println(SpecUtil.writeToSpec(specObj));

        specObj = SpecUtil.parseToDomain(SpecUtil.writeToSpec(specObj));
        specification = specObj.getSpec();

        Assert.assertNotNull(specification);
        Assert.assertNotNull(specification.getNodes());
        Assert.assertEquals(1, specification.getNodes().size());

        SpecNode node = specification.getNodes().get(0);
        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParamHub());
        Assert.assertNotNull(node.getParamHub().getVariables());
        Assert.assertEquals(5, node.getParamHub().getVariables().size());

        SpecVariable shellVar1 = node.getParamHub().getVariables().stream()
            .filter(v -> v.getName().equalsIgnoreCase("shell_var_1")).findFirst().orElse(null);
        Assert.assertNotNull(shellVar1);
        Assert.assertNotNull(shellVar1.getReferenceVariable());
        Assert.assertNotNull(shellVar1.getReferenceVariable().getNode());
    }

    @Test
    public void testAssignment() {
        String spec = readJson("assign.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();

        Assert.assertNotNull(specification);

        Assert.assertNotNull(specification.getNodes());
        Assert.assertEquals(1, specification.getNodes().size());

        SpecNode node = specification.getNodes().get(0);
        Assert.assertNotNull(node);

        Assert.assertNotNull(node.getOutputs());
        Assert.assertEquals(2, node.getOutputs().size());

        System.out.println(SpecUtil.writeToSpec(specObj));

        Specification<DataWorksWorkflowSpec> reparseSpec = SpecUtil.parseToDomain(SpecUtil.writeToSpec(specObj));
        Assert.assertNotNull(reparseSpec);
        Assert.assertNotNull(reparseSpec.getSpec());
        Assert.assertNotNull(reparseSpec.getSpec().getNodes());
        Assert.assertEquals(1, reparseSpec.getSpec().getNodes().size());

        node = reparseSpec.getSpec().getNodes().get(0);
        Assert.assertNotNull(node);

        Assert.assertNotNull(node.getOutputs());
        Assert.assertEquals(2, node.getOutputs().size());
    }

    @Test
    public void testDoWhile() {
        String spec = readJson("spec/examples/json/dowhile.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        Assert.assertNotNull(specification);
        Assert.assertNotNull(specification.getNodes());
        Assert.assertEquals(1, specification.getNodes().size());
        //Assert.assertNotNull(specification.getNodes().get(0).getDoWhile());
        //
        //Assert.assertNotNull(specification.getNodes().get(0).getDoWhile().getNodes());
        //Assert.assertEquals(3, specification.getNodes().get(0).getDoWhile().getNodes().size());

        System.out.println(SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testFileResource() {
        String spec = readJson("spec/examples/json/file_resource.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        Assert.assertNotNull(specification);
        Assert.assertNotNull(specification.getFiles());
        Assert.assertEquals(2, specification.getFiles().size());

        Assert.assertNotNull(specification.getFileResources());
        log.info("file resources: {}", specification.getFileResources());
        Assert.assertEquals(2, specification.getFileResources().size());

        log.info("spec: {}", SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testFunction() {
        String spec = readJson("spec/examples/json/function.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        Assert.assertNotNull(specification);

        Assert.assertEquals(1, specification.getFunctions().size());
        SpecFunction func = specification.getFunctions().get(0);
        Assert.assertNotNull(func);
        log.info("spec: {}", SpecUtil.writeToSpec(specObj));
        Assert.assertEquals(FunctionType.MATH, func.getType());
        Assert.assertNotNull(func.getFileResources());
        Assert.assertNotNull(func.getClassName());
        Assert.assertNotNull(func.getName());
        Assert.assertNotNull(func.getDatasource());
        Assert.assertNotNull(func.getRuntimeResource());
    }

    @Test
    public void testTable() {
        String spec = readJson("spec/examples/json/table.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        Assert.assertNotNull(specification);

        Assert.assertEquals(1, specification.getArtifacts().size());
        SpecArtifact table = specification.getArtifacts().get(0);
        Assert.assertNotNull(table);

        Assert.assertTrue(table instanceof SpecTable);

        Assert.assertNotNull(((SpecTable)table).getDdl());
        Assert.assertNotNull(((SpecTable)table).getCalcEngine());
        Assert.assertNotNull(((SpecTable)table).getName());

        log.info("spec: {}", SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testEmr() {
        String spec = readJson("spec/examples/json/emr.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        Assert.assertNotNull(specification);

        List<SpecNode> nodes = specification.getNodes();
        Assert.assertNotNull(nodes);

        SpecNode emrNode = nodes.get(0);
        Assert.assertNotNull(emrNode);
        Assert.assertNotNull(emrNode.getScript());

        SpecScript script = emrNode.getScript();
        Assert.assertNotNull(script);
        Assert.assertNotNull(script.getRuntime());

        log.info("spec: {}", SpecUtil.writeToSpec(specObj));

        DataWorksNodeCodeAdapter adapter = new DataWorksNodeCodeAdapter(emrNode);
        log.info("code: {}", adapter.getCode());
        Assert.assertNotNull(adapter.getCode());
        Assert.assertTrue(StringUtils.indexOf(adapter.getCode(), "spark.executor.memory") > 0);
    }

    @Test
    public void testCdh() {
        String spec = readJson("spec/examples/json/cdh.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        Assert.assertNotNull(specification);

        List<SpecNode> nodes = specification.getNodes();
        Assert.assertNotNull(nodes);

        SpecNode cdhNode = nodes.get(0);
        Assert.assertNotNull(cdhNode);
        Assert.assertNotNull(cdhNode.getScript());

        SpecScript script = cdhNode.getScript();
        Assert.assertNotNull(script);
        Assert.assertNotNull(script.getRuntime());

        log.info("spec: {}", SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testDatasource() {
        String spec = readJson("spec/examples/json/datasource.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        Assert.assertNotNull(specification);

        List<SpecDatasource> datasources = specification.getDatasources();
        Assert.assertNotNull(datasources);

        SpecDatasource datasource = datasources.get(0);
        Assert.assertNotNull(datasource);
        Assert.assertNotNull(datasource.getName());
        Assert.assertNotNull(datasource.getType());
        Assert.assertNotNull(datasource.getConfig());

        log.info("spec: {}", SpecUtil.writeToSpec(specObj));
    }

    @Test
    public void testDqcRule() {
        String spec = readJson("spec/examples/json/dqc.json");
        Specification<DataWorksWorkflowSpec> specObj = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(specObj);

        DataWorksWorkflowSpec specification = specObj.getSpec();
        Assert.assertNotNull(specification);

        List<SpecDqcRule> specDqcRules = specification.getDqcRules();
        Assert.assertNotNull(specDqcRules);

        SpecDqcRule dqcRule = specDqcRules.get(0);
        Assert.assertNotNull(dqcRule);
        Assert.assertNotNull(dqcRule.getName());
        Assert.assertNotNull(dqcRule.getTable());
        Assert.assertNotNull(dqcRule.getCalcEngine());
        Assert.assertNotNull(dqcRule.getRuleConfig());
        Assert.assertNotNull(dqcRule.getSchedulerNode());

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
        Assert.assertNotNull(spec);
        Assert.assertNotNull(spec.getContext());
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
        log.info("foreach: {}", JSON.toJSONString(SpecUtil.write(foreach, new SpecWriterContext()), Feature.PrettyFormat));
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
        Assert.assertNotNull(resp);
    }
}