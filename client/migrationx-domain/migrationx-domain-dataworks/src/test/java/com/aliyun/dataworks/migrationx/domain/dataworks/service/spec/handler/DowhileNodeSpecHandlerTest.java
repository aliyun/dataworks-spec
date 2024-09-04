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

package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.handler;

import java.util.Arrays;

import com.aliyun.dataworks.common.spec.adapter.SpecHandlerContext;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.NodeSpecAdapter;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntityAdapter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/6/18
 */
public class DowhileNodeSpecHandlerTest {
    @Test
    public void testDowhile() {
        DwNode dwNode = new DwNode();
        dwNode.setName("test1");
        dwNode.setType(CodeProgramType.CONTROLLER_CYCLE.getName());
        dwNode.setNodeUseType(NodeUseType.SCHEDULED);

        // inner nodes
        DwNode start = new DwNode();
        start.setType(CodeProgramType.CONTROLLER_CYCLE_START.getName());
        start.setName("start");
        start.setNodeUseType(NodeUseType.SCHEDULED);

        DwNode sql = new DwNode();
        sql.setName("sql");
        sql.setType(CodeProgramType.ODPS_SQL.getName());
        sql.setNodeUseType(NodeUseType.SCHEDULED);

        DwNode end = new DwNode();
        end.setName("end");
        end.setType(CodeProgramType.CONTROLLER_CYCLE_END.getName());
        end.setNodeUseType(NodeUseType.SCHEDULED);
        dwNode.setInnerNodes(Arrays.asList(start, sql, end));

        // handler
        DowhileNodeSpecHandler handler = new DowhileNodeSpecHandler();
        SpecHandlerContext context = new SpecHandlerContext();
        NodeSpecAdapter adapter = new NodeSpecAdapter();
        adapter.setDefaultHandler(BasicNodeSpecHandler.class);
        context.setSpecAdapter(adapter);
        handler.setContext(context);
        SpecNode dowhile = handler.handle(new DwNodeEntityAdapter(dwNode));

        Assert.assertNotNull(dowhile);
        Assert.assertNotNull(dowhile.getDoWhile());
        Assert.assertNotNull(dowhile.getDoWhile().getNodes());
        Assert.assertEquals(2, dowhile.getDoWhile().getNodes().size());
        Assert.assertNotNull(dowhile.getDoWhile().getSpecWhile());
    }
}
