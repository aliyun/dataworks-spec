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

package com.aliyun.dataworks.common.spec.domain.ref.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/6/7
 */
@Slf4j
public class SpecComponentTest {
    @Test
    public void test() {
        SpecComponent com = new SpecComponent();
        com.setId("com1");
        com.setName("com1");
        SpecScript script = new SpecScript();
        SpecScriptRuntime rt = new SpecScriptRuntime();
        rt.setCommand(CodeProgramType.SQL_COMPONENT.name());
        script.setLanguage("odps-script");
        script.setRuntime(rt);
        script.setContent("select @@{in1}, @@{in2}, @@{out1}, @@{out2}");
        script.setPath("test/com1");
        com.setScript(script);
        com.setDescription("com1");
        com.setOwner("123123");
        List<SpecComponentParameter> input = new ArrayList<>();
        input.add(new SpecComponentParameter().setName("in1").setType("string").setValue("11"));
        input.add(new SpecComponentParameter().setName("in2").setType("string").setValue("22"));
        List<SpecComponentParameter> output = new ArrayList<>();
        output.add(new SpecComponentParameter().setName("out1").setType("string").setValue("out11"));
        output.add(new SpecComponentParameter().setName("out2").setType("string").setValue("out22"));
        com.setInputs(input);
        com.setOutputs(output);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", "12344556");
        metadata.put("version", "11");
        com.setMetadata(metadata);

        Specification<DataWorksWorkflowSpec> spec = new Specification<>();
        spec.setVersion(SpecVersion.V_1_1_0.getLabel());
        spec.setKind(SpecKind.COMPONENT.getLabel());
        DataWorksWorkflowSpec flowSpec = new DataWorksWorkflowSpec();
        flowSpec.setComponents(Collections.singletonList(com));
        spec.setSpec(flowSpec);
        String specStr = SpecUtil.writeToSpec(spec);
        log.info("spec string: {}", specStr);

        Specification<DataWorksWorkflowSpec> parsedSpec = SpecUtil.parseToDomain(specStr);
        Assert.assertNotNull(parsedSpec);
        Assert.assertNotNull(parsedSpec.getSpec());
        Assert.assertNotNull(parsedSpec.getSpec().getComponents());
        Assert.assertEquals(1, parsedSpec.getSpec().getComponents().size());

        SpecComponent parseCom = parsedSpec.getSpec().getComponents().get(0);
        Assert.assertEquals(com.getId(), parseCom.getId());
        Assert.assertEquals(com.getName(), parseCom.getName());
        Assert.assertEquals(com.getDescription(), parseCom.getDescription());
        Assert.assertEquals(com.getOwner(), parseCom.getOwner());
        Assert.assertEquals(com.getScript().getLanguage(), parseCom.getScript().getLanguage());
        Assert.assertEquals(com.getScript().getContent(), parseCom.getScript().getContent());
        Assert.assertEquals(com.getScript().getPath(), parseCom.getScript().getPath());
        Assert.assertEquals(com.getScript().getRuntime().getCommand(), parseCom.getScript().getRuntime().getCommand());
        Assert.assertEquals(com.getInputs().size(), parseCom.getInputs().size());
        for (int i = 0; i < com.getInputs().size(); i++) {
            SpecComponentParameter inputParam = com.getInputs().get(i);
            SpecComponentParameter parseInputParam = parseCom.getInputs().get(i);
            Assert.assertEquals(inputParam.getName(), parseInputParam.getName());
            Assert.assertEquals(inputParam.getType(), parseInputParam.getType());
            Assert.assertEquals(inputParam.getValue(), parseInputParam.getValue());
        }
        Assert.assertEquals(com.getOutputs().size(), parseCom.getOutputs().size());
        for (int i = 0; i < com.getOutputs().size(); i++) {
            SpecComponentParameter outputParam = com.getOutputs().get(i);
            SpecComponentParameter parseOutputParam = parseCom.getOutputs().get(i);
            Assert.assertEquals(outputParam.getName(), parseOutputParam.getName());
            Assert.assertEquals(outputParam.getType(), parseOutputParam.getType());
            Assert.assertEquals(outputParam.getValue(), parseOutputParam.getValue());
        }
        Assert.assertEquals(com.getMetadata().get("id"), parseCom.getMetadata().get("id"));
        Assert.assertEquals(com.getMetadata().get("version"), parseCom.getMetadata().get("version"));
    }
}
