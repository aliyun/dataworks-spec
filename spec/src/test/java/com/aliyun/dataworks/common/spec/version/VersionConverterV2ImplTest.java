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

package com.aliyun.dataworks.common.spec.version;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.enums.FlowType;
import com.aliyun.dataworks.common.spec.domain.enums.PriorityWeightStrategy;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.enums.TriggerType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFileResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFunction;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for VersionConverterV2Impl
 *
 * @author Qwen Code
 */
@Slf4j
public class VersionConverterV2ImplTest {

    private VersionConverterV2Impl converter;

    @Before
    public void setUp() {
        converter = new VersionConverterV2Impl();
    }

    @Test
    public void testSupport() {
        // Test when target version is V_2_0_0
        assertTrue(converter.support(SpecVersion.V_1_0_0, SpecVersion.V_2_0_0));
        assertTrue(converter.support(SpecVersion.V_1_1_0, SpecVersion.V_2_0_0));
        assertTrue(converter.support(SpecVersion.V_1_2_0, SpecVersion.V_2_0_0));
        assertTrue(converter.support(SpecVersion.V_2_0_0, SpecVersion.V_2_0_0));

        // Test when target version is not V_2_0_0
        assertFalse(converter.support(SpecVersion.V_1_0_0, SpecVersion.V_1_0_0));
        assertFalse(converter.support(SpecVersion.V_1_0_0, SpecVersion.V_1_1_0));
        assertFalse(converter.support(SpecVersion.V_1_0_0, SpecVersion.V_1_2_0));
    }

    @Test
    public void testConvertWithComponentKind() {
        Specification<DataWorksWorkflowSpec> sourceSpec = createBaseSpecification();
        sourceSpec.setKind(SpecKind.COMPONENT.getLabel());

        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        sourceSpec.setSpec(spec);

        Specification<DataWorksWorkflowSpec> result = converter.convert(sourceSpec);

        assertNotNull(result);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), result.getVersion());
        assertEquals(SpecKind.COMPONENT.getLabel(), result.getKind());
        assertNotNull(result.getSpec());
    }

    @Test
    public void testConvertWithResourceKind() {
        Specification<DataWorksWorkflowSpec> sourceSpec = createBaseSpecification();
        sourceSpec.setKind(SpecKind.RESOURCE.getLabel());

        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        SpecFileResource fileResource = new SpecFileResource();
        fileResource.setId("resource-1");
        spec.setFileResources(Collections.singletonList(fileResource));
        sourceSpec.setSpec(spec);

        Specification<DataWorksWorkflowSpec> result = converter.convert(sourceSpec);

        assertNotNull(result);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), result.getVersion());
        assertEquals(SpecKind.RESOURCE.getLabel(), result.getKind());
        assertNotNull(result.getSpec());
        assertNotNull(result.getSpec().getFileResources());
        assertEquals(1, result.getSpec().getFileResources().size());
        assertEquals("resource-1", result.getSpec().getFileResources().get(0).getId());
    }

    @Test
    public void testConvertWithFunctionKind() {
        Specification<DataWorksWorkflowSpec> sourceSpec = createBaseSpecification();
        sourceSpec.setKind(SpecKind.FUNCTION.getLabel());

        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        SpecFunction function = new SpecFunction();
        function.setId("function-1");
        spec.setFunctions(Collections.singletonList(function));
        sourceSpec.setSpec(spec);

        Specification<DataWorksWorkflowSpec> result = converter.convert(sourceSpec);

        assertNotNull(result);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), result.getVersion());
        assertEquals(SpecKind.FUNCTION.getLabel(), result.getKind());
        assertNotNull(result.getSpec());
        assertNotNull(result.getSpec().getFunctions());
        assertEquals(1, result.getSpec().getFunctions().size());
        assertEquals("function-1", result.getSpec().getFunctions().get(0).getId());
    }

    @Test
    public void testConvertWithCycleWorkflowKind() {
        Specification<DataWorksWorkflowSpec> sourceSpec = createBaseSpecification();
        sourceSpec.setKind(SpecKind.CYCLE_WORKFLOW.getLabel());

        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        SpecWorkflow workflow = new SpecWorkflow();
        workflow.setId("workflow-1");
        spec.setWorkflows(Collections.singletonList(workflow));
        sourceSpec.setSpec(spec);

        Specification<DataWorksWorkflowSpec> result = converter.convert(sourceSpec);

        assertNotNull(result);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), result.getVersion());
        assertEquals(SpecKind.CYCLE_WORKFLOW.getLabel(), result.getKind());
        assertNotNull(result.getSpec());
    }

    @Test
    public void testConvertWithTriggerWorkflowKind() {
        Specification<DataWorksWorkflowSpec> sourceSpec = createBaseSpecification();
        sourceSpec.setKind(SpecKind.TRIGGER_WORKFLOW.getLabel());

        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        SpecWorkflow workflow = new SpecWorkflow();
        workflow.setId("workflow-1");
        spec.setWorkflows(Collections.singletonList(workflow));
        sourceSpec.setSpec(spec);

        Specification<DataWorksWorkflowSpec> result = converter.convert(sourceSpec);

        assertNotNull(result);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), result.getVersion());
        assertEquals(SpecKind.TRIGGER_WORKFLOW.getLabel(), result.getKind());
        assertNotNull(result.getSpec());
    }

    @Test
    public void testConvertWithManualWorkflowKind() {
        Specification<DataWorksWorkflowSpec> sourceSpec = createBaseSpecification();
        sourceSpec.setKind(SpecKind.MANUAL_WORKFLOW.getLabel());

        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        SpecWorkflow workflow = new SpecWorkflow();
        workflow.setId("workflow-1");
        workflow.setName("manualworkflow");
        spec.setWorkflows(Collections.singletonList(workflow));
        sourceSpec.setSpec(spec);

        Specification<DataWorksWorkflowSpec> result = converter.convert(sourceSpec);
        log.info("manual workflow spec: {}", SpecUtil.writeToSpec(result));
        assertNotNull(result);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), result.getVersion());
        assertEquals(SpecKind.MANUAL_WORKFLOW.getLabel(), result.getKind());
        assertNotNull(result.getSpec());
        assertNotNull(result.getSpec().getWorkflows());
        assertEquals(1, result.getSpec().getWorkflows().size());
        assertNotNull("workflow-1", result.getSpec().getWorkflows().get(0).getScript());
        assertNotNull(result.getSpec().getWorkflows().get(0).getScript().getRuntime());
        assertEquals(SpecKind.MANUAL_WORKFLOW.name(), result.getSpec().getWorkflows().get(0).getScript().getRuntime().getCommand());
        assertNotNull("workflow-1", result.getSpec().getWorkflows().get(0).getTrigger());
        assertEquals(TriggerType.MANUAL, result.getSpec().getWorkflows().get(0).getTrigger().getType());
    }

    @Test
    public void testConvertWithNodeKind() {
        Specification<DataWorksWorkflowSpec> sourceSpec = createBaseSpecification();
        sourceSpec.setKind(SpecKind.NODE.getLabel());

        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        SpecNode node = new SpecNode();
        node.setId("node-1");
        spec.setNodes(Collections.singletonList(node));
        sourceSpec.setSpec(spec);

        Specification<DataWorksWorkflowSpec> result = converter.convert(sourceSpec);

        assertNotNull(result);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), result.getVersion());
        assertEquals(SpecKind.NODE.getLabel(), result.getKind());
        assertNotNull(result.getSpec());
    }

    @Test(expected = RuntimeException.class)
    public void testConvertWithUnsupportedKind() {
        Specification<DataWorksWorkflowSpec> sourceSpec = createBaseSpecification();
        sourceSpec.setKind("UnsupportedKind");

        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        sourceSpec.setSpec(spec);

        converter.convert(sourceSpec);
    }

    @Test(expected = RuntimeException.class)
    public void testConvertWithNullSpec() {
        Specification<DataWorksWorkflowSpec> sourceSpec = createBaseSpecification();
        sourceSpec.setSpec(null);

        converter.convert(sourceSpec);
    }

    private Specification<DataWorksWorkflowSpec> createBaseSpecification() {
        Specification<DataWorksWorkflowSpec> spec = new Specification<>();
        spec.setVersion(SpecVersion.V_1_0_0.getLabel());

        Map<String, Object> metadata = new HashMap<>();
        spec.setMetadata(metadata);

        return spec;
    }

    private String loadSpecFromResources(String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new IOException("Resource not found: " + fileName);
            }
            return new String(is.readAllBytes());
        }
    }

    @Test
    public void testConvertSingleCycleNode() throws IOException {
        String specJson = loadSpecFromResources("version/1.x.y/single_cycle_node.json");
        Specification<DataWorksWorkflowSpec> oldSpec = SpecUtil.parseToDomain(specJson);
        Specification<DataWorksWorkflowSpec> newSpec = converter.convert(oldSpec);

        log.info("oldSpec: {}", specJson);
        log.info("newSpec: {}", SpecUtil.writeToSpec(newSpec));
        assertNotNull(newSpec);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newSpec.getVersion());
        assertEquals(SpecKind.NODE.getLabel(), newSpec.getKind());
        assertNotNull(newSpec.getSpec());
        assertNotNull(newSpec.getSpec().getNodes());
        assertEquals(1, newSpec.getSpec().getNodes().size());
        assertNotNull(newSpec.getSpec().getDependencies());
        SpecWriterContext ctx = new SpecWriterContext();
        ctx.setVersion(newSpec.getVersion());
        JSONObject newSpecJson = (JSONObject)SpecUtil.write(newSpec, ctx);
        assertNull(newSpecJson.getByPath("$.spec.flow"));
        assertNotNull(newSpecJson.getByPath("$.spec.dependencies"));
    }

    @Test
    public void testConvertManualWorkflowInnerNode() throws IOException {
        String specJson = loadSpecFromResources("version/1.x.y/manual_workflow_inner_node.json");
        Specification<DataWorksWorkflowSpec> oldSpec = SpecUtil.parseToDomain(specJson);
        Specification<DataWorksWorkflowSpec> newSpec = converter.convert(oldSpec);

        log.info("oldSpec: {}", specJson);
        log.info("newSpec: {}", SpecUtil.writeToSpec(newSpec));
        assertNotNull(newSpec);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newSpec.getVersion());
        assertEquals(SpecKind.NODE.getLabel(), newSpec.getKind());
        assertNotNull(newSpec.getSpec());
        assertNotNull(newSpec.getSpec().getNodes());
        assertEquals(1, newSpec.getSpec().getNodes().size());
        assertNotNull(newSpec.getSpec().getDependencies());
        SpecWriterContext ctx = new SpecWriterContext();
        ctx.setVersion(newSpec.getVersion());
        JSONObject newSpecJson = (JSONObject)SpecUtil.write(newSpec, ctx);
        assertNull(newSpecJson.getByPath("$.spec.flow"));
        assertNotNull(newSpecJson.getByPath("$.spec.dependencies"));
        assertEquals(newSpecJson.getByPath("$.metadata.uuid"), newSpecJson.getByPath("$.spec.nodes[0].id"));
        assertEquals(newSpecJson.getByPath("$.metadata.uuid"), newSpecJson.getByPath("$.spec.dependencies[0].nodeId"));
    }

    @Test
    public void testManualNode() throws IOException {
        String specJson = loadSpecFromResources("version/1.x.y/manual_node.json");
        Specification<DataWorksWorkflowSpec> oldSpec = SpecUtil.parseToDomain(specJson);
        Specification<DataWorksWorkflowSpec> newSpec = converter.convert(oldSpec);

        log.info("oldSpec: {}", specJson);
        log.info("newSpec: {}", SpecUtil.writeToSpec(newSpec));
        assertNotNull(newSpec);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newSpec.getVersion());
        assertEquals(SpecKind.MANUAL_NODE.getLabel(), newSpec.getKind());
        assertNotNull(newSpec.getSpec());
        assertNotNull(newSpec.getSpec().getNodes());
        assertEquals(1, newSpec.getSpec().getNodes().size());
        assertNull(newSpec.getSpec().getDependencies());
        SpecWriterContext ctx = new SpecWriterContext();
        ctx.setVersion(newSpec.getVersion());
        JSONObject newSpecJson = (JSONObject)SpecUtil.write(newSpec, ctx);
        assertNull(newSpecJson.getByPath("$.spec.flow"));
        assertNull(newSpecJson.getByPath("$.spec.dependencies"));
        assertEquals(newSpecJson.getByPath("$.metadata.uuid"), newSpecJson.getByPath("$.spec.nodes[0].id"));

        Specification<DataWorksWorkflowSpec> newParsed = SpecUtil.parseToDomain(newSpecJson.toString());
        assertNotNull(newParsed);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newParsed.getVersion());
        assertEquals(SpecKind.MANUAL_NODE.getLabel(), newParsed.getKind());
        assertNotNull(newParsed.getSpec());
        assertNotNull(newParsed.getSpec().getNodes());
        assertEquals(1, newParsed.getSpec().getNodes().size());
    }

    @Test
    public void testManualWorkflow() throws IOException {
        String specJson = loadSpecFromResources("version/1.x.y/manual_workflow.json");
        Specification<DataWorksWorkflowSpec> oldSpec = SpecUtil.parseToDomain(specJson);
        Specification<DataWorksWorkflowSpec> newSpec = converter.convert(oldSpec);

        log.info("oldSpec: {}", specJson);
        log.info("newSpec: {}", SpecUtil.writeToSpec(newSpec));
        assertNotNull(newSpec);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newSpec.getVersion());
        assertEquals(SpecKind.MANUAL_WORKFLOW.getLabel(), newSpec.getKind());
        assertNotNull(newSpec.getSpec());
        assertNull(newSpec.getSpec().getNodes());
        assertNull(newSpec.getSpec().getDependencies());
        assertNotNull(newSpec.getSpec().getWorkflows());
        assertEquals(1, newSpec.getSpec().getWorkflows().size());

        SpecWriterContext ctx = new SpecWriterContext();
        ctx.setVersion(newSpec.getVersion());
        JSONObject newSpecJson = (JSONObject)SpecUtil.write(newSpec, ctx);
        assertNull(newSpecJson.getByPath("$.spec.flow"));
        assertNull(newSpecJson.getByPath("$.spec.dependencies"));
        assertEquals(newSpecJson.getByPath("$.metadata.uuid"), newSpecJson.getByPath("$.spec.workflows[0].id"));

        Specification<DataWorksWorkflowSpec> newParsed = SpecUtil.parseToDomain(newSpecJson.toString());
        assertNotNull(newParsed);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newParsed.getVersion());
        assertEquals(SpecKind.MANUAL_WORKFLOW.getLabel(), newParsed.getKind());
        assertNotNull(newParsed.getSpec());
        assertNull(newParsed.getSpec().getNodes());
        assertEquals(1, newParsed.getSpec().getWorkflows().size());
        assertTrue(CollectionUtils.isEmpty(newParsed.getSpec().getWorkflows().get(0).getNodes()));
        assertTrue(CollectionUtils.isEmpty(newParsed.getSpec().getWorkflows().get(0).getDependencies()));

        assertTrue(CollectionUtils.isEmpty(newParsed.getSpec().getVariables()));
        assertEquals(1, newParsed.getSpec().getWorkflows().get(0).getScript().getParameters().size());

        assertEquals(1, newParsed.getSpec().getWorkflows().get(0).getStrategy().getPriority().intValue());
        assertEquals(PriorityWeightStrategy.DISABLED, newParsed.getSpec().getWorkflows().get(0).getStrategy().getPriorityWeightStrategy());
        assertEquals(0, newParsed.getSpec().getWorkflows().get(0).getStrategy().getMaxInternalConcurrency().intValue());
    }

    @Test
    public void testCycleWorkflow() throws IOException {
        String specJson = loadSpecFromResources("version/1.x.y/cycle_workflow.json");
        Specification<DataWorksWorkflowSpec> oldSpec = SpecUtil.parseToDomain(specJson);
        Specification<DataWorksWorkflowSpec> newSpec = converter.convert(oldSpec);

        log.info("oldSpec: {}", specJson);
        log.info("newSpec: {}", SpecUtil.writeToSpec(newSpec));
        assertNotNull(newSpec);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newSpec.getVersion());
        assertEquals(SpecKind.CYCLE_WORKFLOW.getLabel(), newSpec.getKind());
        assertNotNull(newSpec.getSpec());
        assertNull(newSpec.getSpec().getNodes());
        assertNotNull(newSpec.getSpec().getDependencies());
        assertEquals(1, newSpec.getSpec().getDependencies().size());
        assertNotNull(newSpec.getSpec().getWorkflows());
        assertEquals(1, newSpec.getSpec().getWorkflows().size());

        SpecWriterContext ctx = new SpecWriterContext();
        ctx.setVersion(newSpec.getVersion());
        JSONObject newSpecJson = (JSONObject)SpecUtil.write(newSpec, ctx);
        assertNull(newSpecJson.getByPath("$.spec.flow"));
        assertNotNull(newSpecJson.getByPath("$.spec.dependencies"));
        assertEquals(newSpecJson.getByPath("$.metadata.uuid"), newSpecJson.getByPath("$.spec.workflows[0].id"));

        Specification<DataWorksWorkflowSpec> newParsed = SpecUtil.parseToDomain(newSpecJson.toString());
        assertNotNull(newParsed);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newParsed.getVersion());
        assertEquals(SpecKind.CYCLE_WORKFLOW.getLabel(), newParsed.getKind());
        assertNotNull(newParsed.getSpec());
        assertNull(newParsed.getSpec().getNodes());
        assertEquals(1, newParsed.getSpec().getWorkflows().size());
        assertTrue(CollectionUtils.isEmpty(newParsed.getSpec().getWorkflows().get(0).getNodes()));
        assertTrue(CollectionUtils.isEmpty(newParsed.getSpec().getWorkflows().get(0).getDependencies()));
        assertNotNull(newParsed.getSpec().getDependencies());
        assertEquals(1, newParsed.getSpec().getDependencies().size());
        assertEquals(newParsed.getSpec().getWorkflows().get(0).getId(), newParsed.getSpec().getDependencies().get(0).getNodeId().getId());
        assertEquals("project_root", newParsed.getSpec().getDependencies().get(0).getDepends().get(0).getOutput().getData());
    }

    @Test
    public void testTriggerWorkflow() throws IOException {
        String specJson = loadSpecFromResources("version/1.x.y/trigger_workflow.json");
        Specification<DataWorksWorkflowSpec> oldSpec = SpecUtil.parseToDomain(specJson);
        Specification<DataWorksWorkflowSpec> newSpec = converter.convert(oldSpec);

        log.info("oldSpec: {}", specJson);
        log.info("newSpec: {}", SpecUtil.writeToSpec(newSpec));
        assertNotNull(newSpec);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newSpec.getVersion());
        assertEquals(SpecKind.TRIGGER_WORKFLOW.getLabel(), newSpec.getKind());
        assertNotNull(newSpec.getSpec());
        assertNull(newSpec.getSpec().getNodes());
        assertNull(newSpec.getSpec().getDependencies());
        assertNotNull(newSpec.getSpec().getWorkflows());
        assertEquals(1, newSpec.getSpec().getWorkflows().size());

        SpecWriterContext ctx = new SpecWriterContext();
        ctx.setVersion(newSpec.getVersion());
        JSONObject newSpecJson = (JSONObject)SpecUtil.write(newSpec, ctx);
        assertNull(newSpecJson.getByPath("$.spec.flow"));
        assertNull(newSpecJson.getByPath("$.spec.dependencies"));
        assertEquals(newSpecJson.getByPath("$.metadata.uuid"), newSpecJson.getByPath("$.spec.workflows[0].id"));

        Specification<DataWorksWorkflowSpec> newParsed = SpecUtil.parseToDomain(newSpecJson.toString());
        assertNotNull(newParsed);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newParsed.getVersion());
        assertEquals(SpecKind.TRIGGER_WORKFLOW.getLabel(), newParsed.getKind());
        assertNotNull(newParsed.getSpec());
        assertNull(newParsed.getSpec().getNodes());
        assertEquals(1, newParsed.getSpec().getWorkflows().size());
        assertEquals(FlowType.TRIGGER_WORKFLOW.getLabel(), newParsed.getSpec().getWorkflows().get(0).getType());
    }

    @Test
    public void testCycleWorkflowInnerNode() throws IOException {
        String specJson = loadSpecFromResources("version/1.x.y/cycle_workflow_inner_node.json");
        Specification<DataWorksWorkflowSpec> oldSpec = SpecUtil.parseToDomain(specJson);
        Specification<DataWorksWorkflowSpec> newSpec = converter.convert(oldSpec);

        log.info("oldSpec: {}", specJson);
        log.info("newSpec: {}", SpecUtil.writeToSpec(newSpec));
        assertNotNull(newSpec);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newSpec.getVersion());
        assertEquals(SpecKind.NODE.getLabel(), newSpec.getKind());
        assertNotNull(newSpec.getSpec());
        assertNotNull(newSpec.getSpec().getNodes());
        assertNotNull(newSpec.getSpec().getDependencies());
        assertEquals(1, newSpec.getSpec().getDependencies().size());
        assertNull(newSpec.getSpec().getWorkflows());

        SpecWriterContext ctx = new SpecWriterContext();
        ctx.setVersion(newSpec.getVersion());
        JSONObject newSpecJson = (JSONObject)SpecUtil.write(newSpec, ctx);
        assertNull(newSpecJson.getByPath("$.spec.flow"));
        assertNotNull(newSpecJson.getByPath("$.spec.dependencies"));
        assertEquals(newSpecJson.getByPath("$.metadata.uuid"), newSpecJson.getByPath("$.spec.nodes[0].id"));

        Specification<DataWorksWorkflowSpec> newParsed = SpecUtil.parseToDomain(newSpecJson.toString());
        assertNotNull(newParsed);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newParsed.getVersion());
        assertEquals(SpecKind.NODE.getLabel(), newParsed.getKind());
        assertNotNull(newParsed.getSpec());
        assertNotNull(newParsed.getSpec().getNodes());
        assertEquals(1, newParsed.getSpec().getNodes().size());
        assertNotNull(newParsed.getSpec().getDependencies());
        assertEquals(1, newParsed.getSpec().getDependencies().size());
        assertEquals(newParsed.getSpec().getNodes().get(0).getId(), newParsed.getSpec().getDependencies().get(0).getNodeId().getId());
        assertEquals("7598838183218421046", newParsed.getSpec().getDependencies().get(0).getDepends().get(0).getOutput().getData());
    }

    @Test
    public void testSingleDowhileInnerNode() throws IOException {
        String specJson = loadSpecFromResources("version/1.x.y/single_dowhile_inner_node.json");
        Specification<DataWorksWorkflowSpec> oldSpec = SpecUtil.parseToDomain(specJson);
        Specification<DataWorksWorkflowSpec> newSpec = converter.convert(oldSpec);

        log.info("oldSpec: {}", specJson);
        log.info("newSpec: {}", SpecUtil.writeToSpec(newSpec));
        assertNotNull(newSpec);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newSpec.getVersion());
        assertEquals(SpecKind.NODE.getLabel(), newSpec.getKind());
        assertNotNull(newSpec.getSpec());
        assertNotNull(newSpec.getSpec().getNodes());
        assertNotNull(newSpec.getSpec().getDependencies());
        assertEquals(1, newSpec.getSpec().getDependencies().size());
        assertNull(newSpec.getSpec().getWorkflows());

        SpecWriterContext ctx = new SpecWriterContext();
        ctx.setVersion(newSpec.getVersion());
        JSONObject newSpecJson = (JSONObject)SpecUtil.write(newSpec, ctx);
        assertNull(newSpecJson.getByPath("$.spec.flow"));
        assertNotNull(newSpecJson.getByPath("$.spec.dependencies"));
        assertEquals(newSpecJson.getByPath("$.metadata.uuid"), newSpecJson.getByPath("$.spec.nodes[0].id"));

        Specification<DataWorksWorkflowSpec> newParsed = SpecUtil.parseToDomain(newSpecJson.toString());
        assertNotNull(newParsed);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newParsed.getVersion());
        assertEquals(SpecKind.NODE.getLabel(), newParsed.getKind());
        assertNotNull(newParsed.getSpec());
        assertNotNull(newParsed.getSpec().getNodes());
        assertEquals(1, newParsed.getSpec().getNodes().size());
        assertNotNull(newParsed.getSpec().getDependencies());
        assertEquals(1, newParsed.getSpec().getDependencies().size());
        assertEquals(newParsed.getSpec().getNodes().get(0).getId(), newParsed.getSpec().getDependencies().get(0).getNodeId().getId());
        assertEquals("4661565324722130459", newParsed.getSpec().getDependencies().get(0).getDepends().get(0).getOutput().getData());
    }

    @Test
    public void testTriggerWorkflowInnerForeachInnerNode() throws IOException {
        String specJson = loadSpecFromResources("version/1.x.y/trigger_workflow_inner_foreach_inner_node.json");
        Specification<DataWorksWorkflowSpec> oldSpec = SpecUtil.parseToDomain(specJson);
        Specification<DataWorksWorkflowSpec> newSpec = converter.convert(oldSpec);

        log.info("oldSpec: {}", specJson);
        log.info("newSpec: {}", SpecUtil.writeToSpec(newSpec));
        assertNotNull(newSpec);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newSpec.getVersion());
        assertEquals(SpecKind.NODE.getLabel(), newSpec.getKind());
        assertNotNull(newSpec.getSpec());
        assertNotNull(newSpec.getSpec().getNodes());
        assertNotNull(newSpec.getSpec().getDependencies());
        assertEquals(1, newSpec.getSpec().getDependencies().size());
        assertNull(newSpec.getSpec().getWorkflows());

        SpecWriterContext ctx = new SpecWriterContext();
        ctx.setVersion(newSpec.getVersion());
        JSONObject newSpecJson = (JSONObject)SpecUtil.write(newSpec, ctx);
        assertNull(newSpecJson.getByPath("$.spec.flow"));
        assertNotNull(newSpecJson.getByPath("$.spec.dependencies"));
        assertEquals(newSpecJson.getByPath("$.metadata.uuid"), newSpecJson.getByPath("$.spec.nodes[0].id"));

        Specification<DataWorksWorkflowSpec> newParsed = SpecUtil.parseToDomain(newSpecJson.toString());
        assertNotNull(newParsed);
        assertEquals(SpecVersion.V_2_0_0.getLabel(), newParsed.getVersion());
        assertEquals(SpecKind.NODE.getLabel(), newParsed.getKind());
        assertNotNull(newParsed.getSpec());
        assertNotNull(newParsed.getSpec().getNodes());
        assertEquals(1, newParsed.getSpec().getNodes().size());
        assertNotNull(newParsed.getSpec().getDependencies());
        assertEquals(1, newParsed.getSpec().getDependencies().size());
        assertEquals(newParsed.getSpec().getNodes().get(0).getId(), newParsed.getSpec().getDependencies().get(0).getNodeId().getId());
        assertEquals("8305302544780622154", newParsed.getSpec().getDependencies().get(0).getDepends().get(0).getOutput().getData());
    }
}