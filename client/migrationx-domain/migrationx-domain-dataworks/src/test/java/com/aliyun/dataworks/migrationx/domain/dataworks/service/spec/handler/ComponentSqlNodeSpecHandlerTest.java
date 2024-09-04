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

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntityAdapter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/6/19
 */
@Slf4j
public class ComponentSqlNodeSpecHandlerTest {
    @Test
    public void testComponentSpec() {
        ComponentSqlNodeSpecHandler handler = new ComponentSqlNodeSpecHandler();
        DwNode dwNode = new DwNode();
        dwNode.setNodeUseType(NodeUseType.SCHEDULED);
        dwNode.setType(CodeProgramType.COMPONENT_SQL.getName());
        String content
            = "{\"code\":\"select '@@{p1}', '@@{p2}', '@@{p3}' \\n\\n;\",\"config\":{\"input\":[{\"name\":\"p1\",\"type\":\"string\","
            + "\"value\":\"111111\"},{\"name\":\"p2\",\"type\":\"string\",\"value\":\"222222\"},{\"name\":\"p3\",\"type\":\"string\"}],"
            + "\"output\":[{\"name\":\"p2\",\"type\":\"string\",\"value\":\"222222\"}]},\"component\":{\"id\":30407036,\"name\":\"my_com\","
            + "\"version\":4}}";
        dwNode.setCode(content);
        Assert.assertTrue(handler.support(new DwNodeEntityAdapter(dwNode)));
        SpecNode spec = handler.handle(new DwNodeEntityAdapter(dwNode));
        Assert.assertNotNull(spec);
        Assert.assertNotNull(spec.getComponent());
        Assert.assertEquals("30407036", spec.getComponent().getId());
        Assert.assertEquals("my_com", spec.getComponent().getName());
        Assert.assertNotNull(spec.getComponent().getMetadata());
        Assert.assertEquals(4, spec.getComponent().getMetadata().get("version"));
        Assert.assertEquals("30407036", spec.getComponent().getMetadata().get("id"));
    }
}
