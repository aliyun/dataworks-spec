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

import java.util.Collections;
import java.util.List;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.enums.DependencyType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDoWhile;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/8/22
 */
@Slf4j
public class DataWorksNodeInputOutputAdapterTest {
    @Test
    public void testGetInputsOfInnerNodeOfWorkflowNode() {
        Specification<DataWorksWorkflowSpec> spec = new Specification<>();
        spec.setVersion(SpecVersion.V_1_2_0.getLabel());
        spec.setKind(SpecKind.CYCLE_WORKFLOW.getLabel());
        DataWorksWorkflowSpec dataworksWorkflowSpec = new DataWorksWorkflowSpec();
        SpecWorkflow workflow = new SpecWorkflow();
        workflow.setId("workflow-id-1");

        SpecNode dowhile = new SpecNode();
        dowhile.setId("workflow-dowhile-id-1");
        SpecDoWhile specDowhile = new SpecDoWhile();
        SpecNode specNodeWhile = new SpecNode();
        specNodeWhile.setId("workflow-dowhile-while-id-1");
        specDowhile.setSpecWhile(specNodeWhile);
        SpecFlowDepend whileDep = new SpecFlowDepend();
        whileDep.setNodeId(specNodeWhile);
        SpecDepend dep = new SpecDepend();
        SpecNodeOutput output = new SpecNodeOutput();
        output.setData("autotest.while_dep_1");
        dep.setOutput(output);
        dep.setType(DependencyType.NORMAL);
        whileDep.setDepends(Collections.singletonList(dep));
        specDowhile.setFlow(Collections.singletonList(whileDep));
        dowhile.setDoWhile(specDowhile);

        workflow.setNodes(Collections.singletonList(dowhile));
        dataworksWorkflowSpec.setWorkflows(Collections.singletonList(workflow));
        spec.setSpec(dataworksWorkflowSpec);
        DataWorksNodeInputOutputAdapter adapter = new DataWorksNodeInputOutputAdapter(spec, specNodeWhile);
        List<Input> inputs = adapter.getInputs();
        log.info("inputs: {}", inputs);
        Assert.assertNotNull(inputs);
        Assert.assertEquals(1, CollectionUtils.size(inputs));
        Assert.assertEquals(output.getData(), ((SpecNodeOutput)inputs.get(0)).getData());
    }

    @Test
    public void testForeachInputContext() {
        String specJson = "{\n"
            + "\t\t\t\t\"metadata\": {\n"
            + "\t\t\t\t\t\"gmtModified\": 1724643085000,\n"
            + "\t\t\t\t\t\"uuid\": \"5055902565472511966\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"kind\": \"CycleWorkflow\",\n"
            + "\t\t\t\t\"version\": \"1.1.0\",\n"
            + "\t\t\t\t\"spec\": {\n"
            + "\t\t\t\t\t\"owner\": \"1107550004253538\",\n"
            + "\t\t\t\t\t\"name\": \"工作流挂载跨周期一级子节点依赖之后再挂载自依赖失败\",\n"
            + "\t\t\t\t\t\"id\": \"6611006631241735927\",\n"
            + "\t\t\t\t\t\"workflows\": [\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"owner\": \"1107550004253538\",\n"
            + "\t\t\t\t\t\t\t\"outputs\": {\n"
            + "\t\t\t\t\t\t\t\t\"nodeOutputs\": [\n"
            + "\t\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\t\"artifactType\": \"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"isDefault\": true,\n"
            + "\t\t\t\t\t\t\t\t\t\t\"data\": \"6611006631241735927\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"refTableName\": \"工作流挂载跨周期一级子节点依赖之后再挂载自依赖失败\"\n"
            + "\t\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"metadata\": {\n"
            + "\t\t\t\t\t\t\t\t\"owner\": \"1107550004253538\",\n"
            + "\t\t\t\t\t\t\t\t\"ownerName\": \"dw_on_emr_qa3@test.aliyunid.com\",\n"
            + "\t\t\t\t\t\t\t\t\"schedulerNodeId\": 700006666312,\n"
            + "\t\t\t\t\t\t\t\t\"tenantId\": \"524257424564736\",\n"
            + "\t\t\t\t\t\t\t\t\"project\": {\n"
            + "\t\t\t\t\t\t\t\t\t\"mode\": \"SIMPLE\",\n"
            + "\t\t\t\t\t\t\t\t\t\"projectOwnerId\": \"1107550004253538\",\n"
            + "\t\t\t\t\t\t\t\t\t\"tenantId\": \"524257424564736\",\n"
            + "\t\t\t\t\t\t\t\t\t\"simple\": true,\n"
            + "\t\t\t\t\t\t\t\t\t\"projectIdentifier\": \"lwt_test_newIde\",\n"
            + "\t\t\t\t\t\t\t\t\t\"projectName\": \"李文涛测试新版ide\",\n"
            + "\t\t\t\t\t\t\t\t\t\"projectId\": \"528891\"\n"
            + "\t\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\t\"projectId\": \"528891\"\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"nodes\": [\n"
            + "\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\"owner\": \"1107550004253538\",\n"
            + "\t\t\t\t\t\t\t\t\t\"outputs\": {\n"
            + "\t\t\t\t\t\t\t\t\t\t\"nodeOutputs\": [\n"
            + "\t\t\t\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\t\"artifactType\": \"NodeOutput\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\t\"isDefault\": true,\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\t\"data\": \"5055902565472511966\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\t\"refTableName\": \"ggg\"\n"
            + "\t\t\t\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t\t\t\t]\n"
            + "\t\t\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\t\t\"metadata\": {\n"
            + "\t\t\t\t\t\t\t\t\t\t\"owner\": \"1107550004253538\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"container\": {\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\"type\": \"Flow\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"6611006631241735927\"\n"
            + "\t\t\t\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\t\t\t\"ownerName\": \"dw_on_emr_qa3@test.aliyunid.com\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"schedulerNodeId\": 700006666313,\n"
            + "\t\t\t\t\t\t\t\t\t\t\"tenantId\": \"524257424564736\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"project\": {\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\"mode\": \"SIMPLE\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\"projectOwnerId\": \"1107550004253538\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\"tenantId\": \"524257424564736\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\"simple\": true,\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\"projectIdentifier\": \"lwt_test_newIde\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\"projectName\": \"李文涛测试新版ide\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\"projectId\": \"528891\"\n"
            + "\t\t\t\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\t\t\t\"projectId\": \"528891\"\n"
            + "\t\t\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\t\t\"rerunInterval\": 180000,\n"
            + "\t\t\t\t\t\t\t\t\t\"inputs\": {},\n"
            + "\t\t\t\t\t\t\t\t\t\"rerunMode\": \"Allowed\",\n"
            + "\t\t\t\t\t\t\t\t\t\"trigger\": {\n"
            + "\t\t\t\t\t\t\t\t\t\t\"cron\": \"00 02 00 * * ?\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"delaySeconds\": 0,\n"
            + "\t\t\t\t\t\t\t\t\t\t\"timezone\": \"Asia/Shanghai\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"startTime\": \"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"id\": \"9087147597187371929\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"endTime\": \"9999-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"type\": \"Scheduler\"\n"
            + "\t\t\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\t\t\"timeout\": 0,\n"
            + "\t\t\t\t\t\t\t\t\t\"script\": {\n"
            + "\t\t\t\t\t\t\t\t\t\t\"path\": \"李文涛测试工作流/缺陷验证/工作流挂载跨周期一级子节点依赖之后再挂载自依赖失败/ggg\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"runtime\": {\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\"commandTypeId\": 6,\n"
            + "\t\t\t\t\t\t\t\t\t\t\t\"command\": \"DIDE_SHELL\"\n"
            + "\t\t\t\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\t\t\t\"id\": \"8209655082050446613\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"content\": \"#!/bin/bash\\n#********************************************************************#\\n##author"
            + ":dw_on_emr_qa3@test.aliyunid.com\\n##create time:2024-08-26 "
            + "11:31:26\\n#********************************************************************#\"\n"
            + "\t\t\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\t\t\"recurrence\": \"Normal\",\n"
            + "\t\t\t\t\t\t\t\t\t\"runtimeResource\": {\n"
            + "\t\t\t\t\t\t\t\t\t\t\"resourceGroup\": \"group_524257424564736\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"resourceGroupId\": \"50414322\",\n"
            + "\t\t\t\t\t\t\t\t\t\t\"id\": \"4780972265711308253\"\n"
            + "\t\t\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\t\t\"rerunTimes\": 3,\n"
            + "\t\t\t\t\t\t\t\t\t\"name\": \"ggg\",\n"
            + "\t\t\t\t\t\t\t\t\t\"id\": \"5055902565472511966\",\n"
            + "\t\t\t\t\t\t\t\t\t\"instanceMode\": \"T+1\"\n"
            + "\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t],\n"
            + "\t\t\t\t\t\t\t\"inputs\": {},\n"
            + "\t\t\t\t\t\t\t\"name\": \"工作流挂载跨周期一级子节点依赖之后再挂载自依赖失败\",\n"
            + "\t\t\t\t\t\t\t\"id\": \"6611006631241735927\",\n"
            + "\t\t\t\t\t\t\t\"trigger\": {\n"
            + "\t\t\t\t\t\t\t\t\"cron\": \"00 01 00 * * ?\",\n"
            + "\t\t\t\t\t\t\t\t\"delaySeconds\": 0,\n"
            + "\t\t\t\t\t\t\t\t\"timezone\": \"Asia/Shanghai\",\n"
            + "\t\t\t\t\t\t\t\t\"startTime\": \"1970-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\t\t\t\"id\": \"6461813501878498656\",\n"
            + "\t\t\t\t\t\t\t\t\"endTime\": \"9999-01-01 00:00:00\",\n"
            + "\t\t\t\t\t\t\t\t\"type\": \"Scheduler\"\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"strategy\": {\n"
            + "\t\t\t\t\t\t\t\t\"rerunInterval\": 180000,\n"
            + "\t\t\t\t\t\t\t\t\"failureStrategy\": \"Break\",\n"
            + "\t\t\t\t\t\t\t\t\"rerunTimes\": 3,\n"
            + "\t\t\t\t\t\t\t\t\"rerunMode\": \"Allowed\",\n"
            + "\t\t\t\t\t\t\t\t\"instanceMode\": \"T+1\",\n"
            + "\t\t\t\t\t\t\t\t\"timeout\": 0\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"script\": {\n"
            + "\t\t\t\t\t\t\t\t\"path\": \"李文涛测试工作流/缺陷验证/工作流挂载跨周期一级子节点依赖之后再挂载自依赖失败\",\n"
            + "\t\t\t\t\t\t\t\t\"runtime\": {\n"
            + "\t\t\t\t\t\t\t\t\t\"command\": \"WORKFLOW\"\n"
            + "\t\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\t\"id\": \"7170058299331901324\"\n"
            + "\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\"dependencies\": []\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t],\n"
            + "\t\t\t\t\t\"type\": \"CycleWorkflow\",\n"
            + "\t\t\t\t\t\"flow\": [\n"
            + "\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\"depends\": [\n"
            + "\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\"output\": \"6611006631241735927\",\n"
            + "\t\t\t\t\t\t\t\t\t\"refTableName\": \"工作流挂载跨周期一级子节点依赖之后再挂载自依赖失败\",\n"
            + "\t\t\t\t\t\t\t\t\t\"type\": \"CrossCycleDependsOnSelf\"\n"
            + "\t\t\t\t\t\t\t\t},\n"
            + "\t\t\t\t\t\t\t\t{\n"
            + "\t\t\t\t\t\t\t\t\t\"output\": \"6611006631241735927\",\n"
            + "\t\t\t\t\t\t\t\t\t\"refTableName\": \"工作流挂载跨周期一级子节点依赖之后再挂载自依赖失败\",\n"
            + "\t\t\t\t\t\t\t\t\t\"type\": \"CrossCycleDependsOnChildren\"\n"
            + "\t\t\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t\t\t],\n"
            + "\t\t\t\t\t\t\t\"nodeId\": \"6611006631241735927\"\n"
            + "\t\t\t\t\t\t}\n"
            + "\t\t\t\t\t]\n"
            + "\t\t\t\t}\n"
            + "\t\t\t}";

        Specification<DataWorksWorkflowSpec> spec = SpecUtil.parseToDomain(specJson);
        DataWorksNodeAdapter adapter = new DataWorksNodeAdapter(spec, spec.getSpec().getWorkflows().get(0));
        DwNodeDependentTypeInfo dep = adapter.getDependentType(n -> null);
        log.info("inputs: {}", dep);
        Assert.assertNotNull(dep);
        Assert.assertEquals(DwNodeDependentTypeInfo.CHILD_AND_SELF, dep.getDependentType());
    }
}
