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

import java.util.HashMap;
import java.util.Map;

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
public class CdhNodeSpecHandlerTest {
    @Test
    public void testHandle() {
        CdhNodeSpecHandler handler = new CdhNodeSpecHandler();
        handler.setContext(new SpecHandlerContext());
        DwNode dwNode = new DwNode();
        dwNode.setNodeUseType(NodeUseType.SCHEDULED);
        dwNode.setType(CodeProgramType.CDH_HIVE.getName());
        dwNode.setCode("select 1");
        Map<String, Object> advancedSetting = new HashMap<>();
        advancedSetting.put("memory", "2048");
        dwNode.setAdvanceSettings(JSON.toJSONString(advancedSetting));

        SpecNode specNode = handler.handle(new DwNodeEntityAdapter(dwNode));
        log.info("spec node: {}", JSON.toJSONString(SpecUtil.write(specNode, new SpecWriterContext()), Feature.PrettyFormat));

        Assert.assertNotNull(specNode);
        Assert.assertNotNull(specNode.getScript());
        Assert.assertNotNull(specNode.getScript().getRuntime());
        Assert.assertEquals(CodeProgramType.CDH_HIVE.name(), specNode.getScript().getRuntime().getCommand());
        Assert.assertNotNull(specNode.getScript().getRuntime().getCdhJobConfig());
        Assert.assertEquals("2048", specNode.getScript().getRuntime().getCdhJobConfig().get("memory"));
    }
}
