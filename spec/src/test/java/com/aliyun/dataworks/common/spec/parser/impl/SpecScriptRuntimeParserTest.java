package com.aliyun.dataworks.common.spec.parser.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;

import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.container.SpecContainer;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.container.SpecContainerEnvVar;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 戒迷
 * @date 2024/6/6
 */
public class SpecScriptRuntimeParserTest {

    SpecScriptRuntimeParser specScriptRuntimeParser = new SpecScriptRuntimeParser();

    @Test
    public void testParse() throws Exception {
        SpecScriptRuntime param = new SpecScriptRuntime();
        SpecContainer specContainer = new SpecContainer();
        List<SpecContainerEnvVar> env = Lists.newArrayList();
        SpecContainerEnvVar env1 = new SpecContainerEnvVar();
        env1.setName("k1");
        env1.setValue("v1");
        env.add(env1);
        SpecContainerEnvVar env2 = new SpecContainerEnvVar();
        env2.setName("k2");
        env2.setValue("v2");
        env.add(env2);

        specContainer.setEnv(env);
        specContainer.setArgs(Lists.newArrayList("3600"));
        specContainer.setCommand(Lists.newArrayList("sleep"));
        specContainer.setImage("image_name");
        specContainer.setImageId("image_id");
        param.setContainer(specContainer);
        param.setCommand("PYTHON");
        param.setCu("0.5");

        Map<String, Object> map = JSON.parseObject(JSON.toJSONString(param), Map.class);

        SpecScriptRuntime result = specScriptRuntimeParser.parse(map, new SpecParserContext());
        Assert.assertEquals(param, result);
    }

}

// Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme