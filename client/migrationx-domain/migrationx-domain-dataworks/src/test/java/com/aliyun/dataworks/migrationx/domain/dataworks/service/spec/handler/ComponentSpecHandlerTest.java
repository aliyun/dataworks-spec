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
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
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
public class ComponentSpecHandlerTest {
    @Test
    public void testComponentSpec() {
        ComponentSpecHandler handler = new ComponentSpecHandler();
        DwNode dwNode = new DwNode();
        dwNode.setNodeUseType(NodeUseType.COMPONENT);
        dwNode.setType(CodeProgramType.SQL_COMPONENT.getName());
        String content
            = "{\"code\":\"select '@@{p1}', '@@{p2}', '@@{p3}' \\n\\n;\",\"config\":{\"input\":[{\"name\":\"p1\",\"type\":\"string\"},"
            + "{\"name\":\"p2\",\"type\":\"string\"},{\"name\":\"p3\",\"type\":\"string\"}],\"output\":[{\"name\":\"p2\",\"type\":\"string\"}],"
            + "\"name\":\"my_com\",\"owner\":\"聿剑\"}}";
        dwNode.setCode(content);
        Assert.assertTrue(handler.support(new DwNodeEntityAdapter(dwNode)));
        SpecComponent spec = handler.handle(new DwNodeEntityAdapter(dwNode));
        Assert.assertNotNull(spec);
        Assert.assertNotNull(spec);
        Assert.assertNotNull(spec.getScript());
        Assert.assertNotNull(spec.getScript().getRuntime().getCommand());
        Assert.assertEquals(CodeProgramType.SQL_COMPONENT.name(), spec.getScript().getRuntime().getCommand());
        Assert.assertNotNull(spec.getInputs());
        Assert.assertEquals(3, spec.getInputs().size());
        Assert.assertNotNull(spec.getOutputs());
        Assert.assertEquals(1, spec.getOutputs().size());
    }
}
