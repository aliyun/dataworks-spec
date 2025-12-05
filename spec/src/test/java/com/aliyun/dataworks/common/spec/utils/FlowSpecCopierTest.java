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

package com.aliyun.dataworks.common.spec.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.Spec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author 聿剑
 * @date 2025/6/13
 */
public class FlowSpecCopierTest {
    @Test
    public void testManualWorkflowCopy() throws IOException {
        String spec = FileUtils.readFileToString(
            new File(FlowSpecCopierTest.class.getClassLoader().getResource("copier/manual_workflow_spec.json").getFile()),
            StandardCharsets.UTF_8);
        Specification<Spec> srcSpec = SpecUtil.parseToDomain(spec);
        FlowSpecCopier copier = new FlowSpecCopier();
        Specification<Spec> copiedSpec = copier.copy(srcSpec);

        JSONObject oldSpecA = (JSONObject)SpecUtil.write(srcSpec, new SpecWriterContext());
        JSONObject newSpecB = (JSONObject)SpecUtil.write(copiedSpec, new SpecWriterContext());

        assertNotNull(oldSpecA);
        JSONArray oldNodes = (JSONArray)oldSpecA.getByPath("$.spec.nodes");
        assertNotNull(oldNodes);
        assertEquals(7, oldNodes.size());

        assertNotNull(newSpecB);
        JSONArray newNodes = (JSONArray)newSpecB.getByPath("$.spec.workflows[0].nodes");
        assertNotNull(newSpecB);
        assertEquals(7, newNodes.size());

        assertEquals(SpecKind.MANUAL_WORKFLOW.getLabel(), newSpecB.getByPath("$.kind"));
        assertEquals(2, ((JSONArray)newSpecB.getByPath("$.spec.workflows[0].script.parameters")).size());

        assertEquals(newSpecB.getByPath("$.spec.workflows[0].nodes[0].id"),
            newSpecB.getByPath("$.spec.workflows[0].nodes[0].outputs.variables[0].node.output"));
        assertEquals(newSpecB.getByPath("$.spec.workflows[0].nodes[0].id"),
            newSpecB.getByPath("$.spec.workflows[0].nodes[1].inputs.variables[0].node.output"));
        assertEquals(newSpecB.getByPath("$.spec.workflows[0].nodes[0].id"),
            newSpecB.getByPath("$.spec.workflows[0].nodes[3].inputs.variables[0].node.output"));

        // assign1 --depends_on--> virtual_root1
        assertEquals(
            newSpecB.getByPath("$.spec.workflows[0].dependencies[0].nodeId"),
            newSpecB.getByPath("$.spec.workflows[0].nodes[0].id"));
        assertEquals(
            newSpecB.getByPath("$.spec.workflows[0].nodes[2].id"),
            newSpecB.getByPath("$.spec.workflows[0].dependencies[0].depends[0].output"));

        // python3 --depends_on--> assign1
        assertEquals(
            newSpecB.getByPath("$.spec.workflows[0].dependencies[1].nodeId"),
            newSpecB.getByPath("$.spec.workflows[0].nodes[1].id"));
        assertEquals(
            newSpecB.getByPath("$.spec.workflows[0].nodes[0].id"),
            newSpecB.getByPath("$.spec.workflows[0].dependencies[1].depends[0].output"));

        // paidlc1 --depends_on--> assign1
        assertEquals(
            newSpecB.getByPath("$.spec.workflows[0].dependencies[2].nodeId"),
            newSpecB.getByPath("$.spec.workflows[0].nodes[3].id"));
        assertEquals(
            newSpecB.getByPath("$.spec.workflows[0].nodes[0].id"),
            newSpecB.getByPath("$.spec.workflows[0].dependencies[2].depends[0].output"));

        // shell1 --depends_on--> assign1
        assertEquals(
            newSpecB.getByPath("$.spec.workflows[0].dependencies[3].nodeId"),
            newSpecB.getByPath("$.spec.workflows[0].nodes[4].id"));
        assertEquals(
            newSpecB.getByPath("$.spec.workflows[0].nodes[0].id"),
            newSpecB.getByPath("$.spec.workflows[0].dependencies[3].depends[0].output"));

        // param hub
        assertEquals(
            newSpecB.getByPath("$.spec.workflows[0].nodes[5].id") + ":outputs",
            newSpecB.getByPath("$.spec.workflows[0].nodes[6]['param-hub'].variables[2].value"));
        assertEquals(
            newSpecB.getByPath("$.spec.workflows[0].nodes[5].id"),
            newSpecB.getByPath("$.spec.workflows[0].nodes[6]['param-hub'].variables[2].referenceVariable.node.output"));
        assertEquals(
            newSpecB.getByPath("$.spec.workflows[0].nodes[5].id") + ":outputs",
            newSpecB.getByPath("$.spec.workflows[0].nodes[6].outputs.variables[1].value"));
    }

    @Test
    public void testSingleCycleNodeSpec() throws IOException {
        String spec = FileUtils.readFileToString(
            new File(FlowSpecCopierTest.class.getClassLoader().getResource("copier/single_cycle_node_spec.json").getFile()),
            StandardCharsets.UTF_8);
        Specification<Spec> srcSpec = SpecUtil.parseToDomain(spec);
        FlowSpecCopier copier = new FlowSpecCopier();
        Specification<Spec> copiedSpec = copier.copy(srcSpec);

        JSONObject old = (JSONObject)SpecUtil.write(srcSpec, new SpecWriterContext());
        JSONObject copied = (JSONObject)SpecUtil.write(copiedSpec, new SpecWriterContext());
        // id replaced in node
        assertNotEquals(old.getByPath("$.spec.nodes[0].id"), copied.getByPath("$.spec.nodes[0].id"));
        assertNotEquals(old.getByPath("$.spec.nodes[0].outputs.nodeOutputs[0].data"),
            copied.getByPath("$.spec.nodes[0].outputs.nodeOutputs[0].data"));
        assertNull(copied.getByPath("$.spec.nodes[0].script.id"));

        // id replace in flows[0].nodeId
        assertNotEquals(old.getByPath("$.spec.flow[0].nodeId"), copied.getByPath("$.spec.flow[0].nodeId"));

        // external dependencies keep the same
        assertEquals(old.getByPath("$.spec.flow[0].depends[0].output"), copied.getByPath("$.spec.flow[0].depends[0].output"));
        // context input variables depends keep the same node.output
        assertEquals(old.getByPath("$.spec.flow[0].depends[0].output"), copied.getByPath("$.spec.nodes[0].inputs.variables[0].node.output"));
    }

    @Test
    public void testRealCycleWorkflowSpec() throws IOException {
        String spec = FileUtils.readFileToString(
            new File(FlowSpecCopierTest.class.getClassLoader().getResource("copier/real_cycle_workflow_spec.json").getFile()),
            StandardCharsets.UTF_8);
        Specification<Spec> srcSpec = SpecUtil.parseToDomain(spec);
        FlowSpecCopier copier = new FlowSpecCopier();
        Specification<Spec> copiedSpec = copier.copy(srcSpec);

        JSONObject old = (JSONObject)SpecUtil.write(srcSpec, new SpecWriterContext());
        JSONObject copied = (JSONObject)SpecUtil.write(copiedSpec, new SpecWriterContext());
        assertEquals(10, ((JSONArray)copied.getByPath("$.spec.workflows[0].nodes")).size());
        assertEquals(8, ((JSONArray)copied.getByPath("$.spec.workflows[0].dependencies")).size());
    }

    @Test
    public void testSingleDowhileSpec() throws IOException {
        String spec = FileUtils.readFileToString(
            new File(FlowSpecCopierTest.class.getClassLoader().getResource("copier/single_dowhile_spec.json").getFile()),
            StandardCharsets.UTF_8);
        Specification<Spec> srcSpec = SpecUtil.parseToDomain(spec);
        FlowSpecCopier copier = new FlowSpecCopier();
        Specification<Spec> copiedSpec = copier.copy(srcSpec);

        JSONObject old = (JSONObject)SpecUtil.write(srcSpec, new SpecWriterContext());
        JSONObject copied = (JSONObject)SpecUtil.write(copiedSpec, new SpecWriterContext());
        assertNotNull(copied.getByPath("$.spec.nodes[0].script.content"));
        assertEquals(6, ((JSONArray)copied.getByPath("$.spec.nodes[0]['do-while'].nodes")).size());
        assertNotNull(copied.getByPath("$.spec.nodes[0]['do-while'].while"));
        assertEquals(6, ((JSONArray)copied.getByPath("$.spec.nodes[0]['do-while'].flow")).size());
    }

    @Test
    public void testCycleWorkflowCopy() throws IOException {
        String spec = FileUtils.readFileToString(
            new File(FlowSpecCopierTest.class.getClassLoader().getResource("copier/cycle_workflow_spec.json").getFile()),
            StandardCharsets.UTF_8);
        Specification<Spec> srcSpec = SpecUtil.parseToDomain(spec);
        FlowSpecCopier copier = new FlowSpecCopier();
        Specification<Spec> copiedSpec = copier.copy(srcSpec);

        JSONObject oldSpecA = (JSONObject)SpecUtil.write(srcSpec, new SpecWriterContext());
        JSONObject newSpecB = (JSONObject)SpecUtil.write(copiedSpec, new SpecWriterContext());

        assertNotNull(oldSpecA);
        assertNotNull(newSpecB);
        assertEquals("PassThrough", newSpecB.getByPath("$.spec.workflows[0].nodes[6].script.parameters[0].type"));
        assertEquals("PassThrough", newSpecB.getByPath("$.spec.workflows[0].nodes[6].script.parameters[0].referenceVariable.type"));
    }

    @Test
    public void testCycleWorkflowWithJoinCopy() throws IOException {
        String spec = FileUtils.readFileToString(
            new File(FlowSpecCopierTest.class.getClassLoader().getResource("copier/cycle_workflow_with_join_spec.json").getFile()),
            StandardCharsets.UTF_8);
        Specification<Spec> srcSpec = SpecUtil.parseToDomain(spec);
        FlowSpecCopier copier = new FlowSpecCopier();
        Specification<Spec> copiedSpec = copier.copy(srcSpec);

        JSONObject oldSpecA = (JSONObject)SpecUtil.write(srcSpec, new SpecWriterContext());
        JSONObject newSpecB = (JSONObject)SpecUtil.write(copiedSpec, new SpecWriterContext());

        assertNotNull(oldSpecA);
        assertNotNull(newSpecB);
        assertTrue(StringUtils.indexOf((String)newSpecB.getByPath("$.spec.workflows[0].nodes[0].script.content"), "6181565218970946826") < 0);
        assertTrue(StringUtils.indexOf((String)newSpecB.getByPath("$.spec.workflows[0].nodes[0].script.content"), "5075273744222348485") < 0);
    }

    @Test
    public void testTriggerWorkflowWithBranchCopy() throws IOException {
        String spec = FileUtils.readFileToString(
            new File(FlowSpecCopierTest.class.getClassLoader().getResource("copier/trigger_workflow_with_branch.json").getFile()),
            StandardCharsets.UTF_8);
        Specification<Spec> srcSpec = SpecUtil.parseToDomain(spec);
        FlowSpecCopier copier = new FlowSpecCopier();
        Specification<Spec> copiedSpec = copier.copy(srcSpec);

        JSONObject oldSpecA = (JSONObject)SpecUtil.write(srcSpec, new SpecWriterContext());
        JSONObject newSpecB = (JSONObject)SpecUtil.write(copiedSpec, new SpecWriterContext());

        assertNotNull(oldSpecA);
        assertNotNull(newSpecB);
        assertNotEquals("wl_test_0317_01.bbbb_true_1", newSpecB.getByPath("$.spec.workflows[0].nodes[2].branches[0].output.data"));
        assertNotEquals("wl_test_0317_01.bbb_false_1", newSpecB.getByPath("$.spec.workflows[0].nodes[2].outputs.nodeOutputs[1].data"));
        assertNotEquals("wl_test_0317_01.bbb_false_1", newSpecB.getByPath("$.spec.workflows[0].nodes[2].branches[1].output.data"));
        assertNotEquals("wl_test_0317_01.bbbb_true_1", newSpecB.getByPath("$.spec.workflows[0].nodes[2].outputs.nodeOutputs[2].data"));

        assertNotEquals("wl_test_0317_01.bbb_false_1", newSpecB.getByPath("$.spec.workflows[0].dependencies[0].depends[0].output"));
        assertNotEquals("wl_test_0317_01.bbbb_true_1", newSpecB.getByPath("$.spec.workflows[0].dependencies[0].depends[1].output"));
    }

    @Test
    public void testCopiedTriggerWorkflowWithBranchCopy() throws IOException {
        String spec = FileUtils.readFileToString(
            new File(FlowSpecCopierTest.class.getClassLoader().getResource("copier/copied_trigger_workflow_with_branch.json").getFile()),
            StandardCharsets.UTF_8);
        Specification<Spec> srcSpec = SpecUtil.parseToDomain(spec);
        FlowSpecCopier copier = new FlowSpecCopier();
        Specification<Spec> copiedSpec = copier.copy(srcSpec);

        JSONObject oldSpecA = (JSONObject)SpecUtil.write(srcSpec, new SpecWriterContext());
        JSONObject newSpecB = (JSONObject)SpecUtil.write(copiedSpec, new SpecWriterContext());

        assertNotNull(oldSpecA);
        assertNotNull(newSpecB);
        assertNotEquals("wl_test_0317_01.bbbb_true_1_CPSinrD30CPE", newSpecB.getByPath("$.spec.workflows[0].nodes[2].branches[0].output.data"));
        assertNotEquals("wl_test_0317_01.bbb_false_1_CPSsJ9gSDCPE", newSpecB.getByPath("$.spec.workflows[0].nodes[2].outputs.nodeOutputs[1].data"));
        assertNotEquals("wl_test_0317_01.bbb_false_1_CPSsJ9gSDCPE", newSpecB.getByPath("$.spec.workflows[0].nodes[2].branches[1].output.data"));
        assertNotEquals("wl_test_0317_01.bbbb_true_1_CPSinrD30CPE", newSpecB.getByPath("$.spec.workflows[0].nodes[2].outputs.nodeOutputs[2].data"));

        assertNotEquals("wl_test_0317_01.bbb_false_1_CPSsJ9gSDCPE", newSpecB.getByPath("$.spec.workflows[0].dependencies[0].depends[0].output"));
        assertNotEquals("wl_test_0317_01.bbbb_true_1_CPSinrD30CPE", newSpecB.getByPath("$.spec.workflows[0].dependencies[0].depends[1].output"));

        assertTrue(Pattern.compile("wl_test_0317_01.bbb_false_1_" + FlowSpecCopier.COPY_SUFFIX_PATTERN.pattern()).matcher(newSpecB.toJSONString())
            .find());
        assertTrue(Pattern.compile("wl_test_0317_01.bbbb_true_1_" + FlowSpecCopier.COPY_SUFFIX_PATTERN.pattern()).matcher(newSpecB.toJSONString())
            .find());
    }
}