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

package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec;

import java.util.Arrays;

import javax.validation.constraints.NotNull;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.adapter.SpecHandlerContext;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntityAdapter;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.handler.BasicNodeSpecHandler;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.handler.ForeachNodeSpecHandler;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/6/17
 */
@Slf4j
public class NodeSpecAdapterTest {
    @Test
    public void testGetHandler() {
        NodeSpecAdapter adapter = new NodeSpecAdapter();

        DwNode dwNode = new DwNode();
        dwNode.setType(CodeProgramType.CONTROLLER_TRAVERSE.getName());
        @NotNull
        BasicNodeSpecHandler handler = (BasicNodeSpecHandler) adapter.getHandler(new DwNodeEntityAdapter(dwNode), null);
        Assert.assertNotNull(handler);
        Assert.assertEquals(ForeachNodeSpecHandler.class, handler.getClass());
    }

    @Test
    public void testGetHandlerByDefault() {
        NodeSpecAdapter adapter = new NodeSpecAdapter();

        DwNode dwNode = new DwNode();
        dwNode.setType(CodeProgramType.ODPS_SQL.getName());
        @NotNull
        BasicNodeSpecHandler handler = (BasicNodeSpecHandler) adapter.getHandler(new DwNodeEntityAdapter(dwNode), null);
        Assert.assertNotNull(handler);
        Assert.assertEquals(BasicNodeSpecHandler.class, handler.getClass());
    }

    @Test
    public void testGetNodeSpec() {
        DwNode dwNode = new DwNode();
        dwNode.setName("test1");
        dwNode.setType(CodeProgramType.CONTROLLER_TRAVERSE.getName());
        dwNode.setNodeUseType(NodeUseType.SCHEDULED);
        DwNode start = new DwNode();
        start.setType(CodeProgramType.CONTROLLER_TRAVERSE_START.getName());
        start.setName("start");
        start.setNodeUseType(NodeUseType.SCHEDULED);

        DwNode sql = new DwNode();
        sql.setName("sql");
        sql.setType(CodeProgramType.ODPS_SQL.getName());
        sql.setNodeUseType(NodeUseType.SCHEDULED);

        DwNode end = new DwNode();
        end.setName("end");
        end.setType(CodeProgramType.CONTROLLER_TRAVERSE_END.getName());
        end.setNodeUseType(NodeUseType.SCHEDULED);
        dwNode.setInnerNodes(Arrays.asList(start, sql, end));
        NodeSpecAdapter adapter = new NodeSpecAdapter();
        SpecHandlerContext context = new SpecHandlerContext();
        String spec = adapter.getNodeSpec(new DwNodeEntityAdapter(dwNode), context);
        log.info("spec: {}", spec);

        Specification<DataWorksWorkflowSpec> sp = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(sp);
        Assert.assertNotNull(sp.getSpec());
        Assert.assertNotNull(sp.getSpec().getNodes());
        Assert.assertEquals(1, sp.getSpec().getNodes().size());
        SpecNode foreach = sp.getSpec().getNodes().get(0);
        Assert.assertNotNull(foreach.getForeach());
        Assert.assertNotNull(foreach.getForeach().getNodes());
        Assert.assertEquals(3, foreach.getForeach().getNodes().size());
    }

    @Test
    public void testComponent() {
        DwNode dwNode = new DwNode();
        dwNode.setName("test1");
        dwNode.setGlobalUuid("111");
        dwNode.setNodeUseType(NodeUseType.COMPONENT);
        dwNode.setType(CodeProgramType.SQL_COMPONENT.getName());
        String content
            = "{\"code\":\"select '@@{p1}', '@@{p2}', '@@{p3}' \\n\\n;\",\"config\":{\"input\":[{\"name\":\"p1\",\"type\":\"string\"},"
            + "{\"name\":\"p2\",\"type\":\"string\"},{\"name\":\"p3\",\"type\":\"string\"}],\"output\":[{\"name\":\"p2\",\"type\":\"string\"}],"
            + "\"name\":\"my_com\",\"owner\":\"聿剑\"}}";
        dwNode.setCode(content);

        NodeSpecAdapter adapter = new NodeSpecAdapter();
        SpecHandlerContext context = new SpecHandlerContext();
        String spec = adapter.getNodeSpec(new DwNodeEntityAdapter(dwNode), context);
        log.info("spec: {}", spec);
        Specification<DataWorksWorkflowSpec> sp = SpecUtil.parseToDomain(spec);
        Assert.assertNotNull(sp);
        Assert.assertNotNull(sp.getSpec());
        Assert.assertNotNull(sp.getSpec().getComponents());
        Assert.assertEquals(1, sp.getSpec().getComponents().size());
        SpecComponent com = sp.getSpec().getComponents().get(0);
        Assert.assertNotNull(com.getName());
        Assert.assertNotNull(com.getId());
        Assert.assertEquals(3, com.getInputs().size());
    }
}
