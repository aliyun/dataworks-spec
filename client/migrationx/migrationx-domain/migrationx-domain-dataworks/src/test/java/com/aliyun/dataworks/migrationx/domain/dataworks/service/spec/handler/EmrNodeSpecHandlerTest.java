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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.adapter.SpecHandlerContext;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrJobType;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrLauncher;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrProperty;
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
public class EmrNodeSpecHandlerTest {
    @Test
    public void testHandle() {
        EmrNodeSpecHandler handler = new EmrNodeSpecHandler();
        handler.setContext(new SpecHandlerContext());
        DwNode dwNode = new DwNode();
        dwNode.setNodeUseType(NodeUseType.SCHEDULED);
        dwNode.setType(CodeProgramType.EMR_HIVE.getName());
        EmrCode emrCode = new EmrCode();
        emrCode.setType(EmrJobType.HIVE_SQL);
        EmrProperty prop = new EmrProperty();
        prop.setArguments(Collections.singletonList("select 1;"));
        emrCode.setProperties(prop);
        emrCode.setDescription("test");
        EmrLauncher launcher = new EmrLauncher();
        Map<String, Object> allocateSpec = new HashMap<>();
        allocateSpec.put("memory", "2048");
        launcher.setAllocationSpec(allocateSpec);
        emrCode.setLauncher(launcher);
        dwNode.setCode(emrCode.getContent());

        SpecNode specNode = handler.handle(new DwNodeEntityAdapter(dwNode));
        log.info("spec node: {}", JSON.toJSONString(SpecUtil.write(specNode, new SpecWriterContext()), Feature.PrettyFormat));

        Assert.assertNotNull(specNode);
        Assert.assertNotNull(specNode.getScript());
        Assert.assertNotNull(specNode.getScript().getRuntime());
        Assert.assertEquals(CodeProgramType.EMR_HIVE.name(), specNode.getScript().getRuntime().getCommand());
        Assert.assertNotNull(specNode.getScript().getRuntime().getEmrJobConfig());
        Assert.assertEquals("2048", specNode.getScript().getRuntime().getEmrJobConfig().get("memory"));
    }
}
