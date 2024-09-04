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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.adapter.SpecHandlerContext;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntityAdapter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/6/17
 */
@Slf4j
public class BranchNodeSpecHandlerTest {
    @Test
    public void testHandle() {
        BranchNodeSpecHandler handler = new BranchNodeSpecHandler();
        handler.setContext(new SpecHandlerContext());
        DwNode dwNode = new DwNode();
        dwNode.setNodeUseType(NodeUseType.SCHEDULED);
        dwNode.setType(CodeProgramType.CONTROLLER_BRANCH.getName());

        dwNode.setCode("[\n"
            + "  {\n"
            + "    \"condition\": \"${out}\\u003d\\u003d1\",\n"
            + "    \"nodeoutput\": \"tmp_transform_project.proj1.swithc1.shell1\"\n"
            + "  },\n"
            + "  {\n"
            + "    \"condition\": \"${out}\\u003d\\u003d2\",\n"
            + "    \"nodeoutput\": \"tmp_transform_project.proj1.swithc1.shell2\"\n"
            + "  }\n"
            + "]");
        SpecNode specNode = handler.handle(new DwNodeEntityAdapter(dwNode));
        log.info("spec node: {}", JSON.toJSONString(SpecUtil.write(specNode, new SpecWriterContext()), Feature.PrettyFormat));

        Assert.assertNotNull(specNode);
        Assert.assertNotNull(specNode.getBranch());
        Assert.assertNotNull(specNode.getBranch().getBranches());
        Assert.assertEquals(2, specNode.getBranch().getBranches().size());
    }
}
