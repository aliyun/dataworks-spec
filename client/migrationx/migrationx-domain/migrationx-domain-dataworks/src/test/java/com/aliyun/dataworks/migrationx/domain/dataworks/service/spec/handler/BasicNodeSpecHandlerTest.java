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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.adapter.SpecHandlerContext;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.LanguageEnum;
import com.aliyun.dataworks.common.spec.domain.enums.TriggerType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeContext;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.RerunMode;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntityAdapter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/6/18
 */
@Slf4j
public class BasicNodeSpecHandlerTest {
    @Test
    public void test() {
        BasicNodeSpecHandler handler = new BasicNodeSpecHandler();
        handler.setContext(new SpecHandlerContext());
        DwNode dwNode = new DwNode();
        dwNode.setNodeUseType(NodeUseType.SCHEDULED);
        dwNode.setType(CodeProgramType.ODPS_SQL.getName());
        dwNode.setCode("select 1;");
        dwNode.setCronExpress("day");
        dwNode.setResourceGroup("S_resgroup_xxx");
        dwNode.setRerunMode(RerunMode.ALL_ALLOWED);
        dwNode.setConnection("emr1");
        dwNode.setParameter("bizdate=${yyyymmdd}");
        NodeIo input = new DwNodeIo();
        input.setData("autotest.input1");
        dwNode.setInputs(Collections.singletonList(input));
        NodeIo output = new DwNodeIo();
        output.setData("autotest.output1");
        dwNode.setOutputs(Collections.singletonList(output));
        NodeContext inCtx = new NodeContext();
        inCtx.setParamName("var1");
        inCtx.setType(0);
        inCtx.setParamNodeId(11L);
        inCtx.setParamValue("autotest.xx1:var1");
        inCtx.setParamType(2);
        dwNode.setInputContexts(Collections.singletonList(inCtx));
        NodeContext outCtx = new NodeContext();
        outCtx.setParamType(2);
        outCtx.setParamName("outputs");
        outCtx.setParamValue("autotest.output1:outputs");
        outCtx.setParamNodeId(222L);
        dwNode.setOutputContexts(Collections.singletonList(outCtx));

        SpecNode specNode = handler.handle(new DwNodeEntityAdapter(dwNode));
        log.info("spec node: {}", JSON.toJSONString(SpecUtil.write(specNode, new SpecWriterContext()), Feature.PrettyFormat));

        Assert.assertNotNull(specNode);
        Assert.assertNotNull(specNode.getScript());
        Assert.assertEquals(CodeProgramType.ODPS_SQL.name(), specNode.getScript().getRuntime().getCommand());
        Assert.assertEquals(LanguageEnum.ODPS_SQL.getIdentifier(), specNode.getScript().getLanguage());

        Assert.assertNotNull(specNode.getTrigger());
        Assert.assertEquals(TriggerType.SCHEDULER, specNode.getTrigger().getType());
        Assert.assertEquals(dwNode.getCronExpress(), specNode.getTrigger().getCron());

        Assert.assertNotNull(specNode.getDatasource());
        Assert.assertEquals(dwNode.getConnection(), specNode.getDatasource().getName());
    }
}
