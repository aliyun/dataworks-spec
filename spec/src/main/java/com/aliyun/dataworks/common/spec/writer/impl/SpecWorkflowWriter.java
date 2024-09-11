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

package com.aliyun.dataworks.common.spec.writer.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.annotation.SpecWriter;
import com.aliyun.dataworks.common.spec.domain.SpecContext;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.interfaces.NodeIO;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import org.apache.commons.collections4.ListUtils;

/**
 * Spec node writer
 *
 * @author 聿剑
 * @date 2023/8/27
 */
@SpecWriter
public class SpecWorkflowWriter extends DefaultJsonObjectWriter<SpecWorkflow> {
    public SpecWorkflowWriter(SpecWriterContext context) {
        super(context);
    }

    @Override
    public JSONObject write(SpecWorkflow specObj, SpecWriterContext context) {
        JSONObject json = writeJsonObject(specObj, true);

        JSONObject inputs = writeIo(specObj.getInputs());
        json.put("inputs", inputs);
        JSONObject outputs = writeIo(specObj.getOutputs());
        json.put("outputs", outputs);
        json.put("script", writeByWriter(specObj.getScript()));
        json.put("trigger", writeByWriter(specObj.getTrigger()));
        json.put("strategy", writeByWriter(specObj.getStrategy()));
        json.put("nodes", Optional.ofNullable(specObj.getNodes()).map(nodes -> writerListByWriter(new ArrayList<>(nodes))).orElse(new JSONArray()));
        json.put("dependencies", Optional.ofNullable(specObj.getDependencies()).map(dependencies -> writerListByWriter(new ArrayList<>(dependencies)))
            .orElse(new JSONArray()));
        return json;
    }

    private <T extends NodeIO> JSONObject writeIo(List<T> ioList) {
        if (ioList == null) {
            return null;
        }

        JSONObject ioJson = new JSONObject();
        Map<Class<?>, List<T>> ioGroup = ListUtils.emptyIfNull(ioList).stream().collect(Collectors.groupingBy(Object::getClass));

        ioGroup.keySet().stream().sorted(Comparator.comparing(Class::getSimpleName)).forEach(clz -> {
            List<T> ios = ioGroup.get(clz);
            String key;
            JSONArray arr;
            if (SpecTable.class.isAssignableFrom(clz)) {
                key = "tables";
            } else if (SpecNodeOutput.class.isAssignableFrom(clz)) {
                String contextVersion = Optional.ofNullable(context).map(SpecContext::getVersion).orElse(SpecVersion.V_1_1_0.getLabel());
                key = SpecVersion.V_1_0_0.getLabel().equalsIgnoreCase(contextVersion) ? "outputs" : "nodeOutputs";
            } else if (clz.equals(SpecVariable.class)) {
                key = "variables";
            } else {
                throw new RuntimeException("unsupported input type");
            }

            if (!ioJson.containsKey(key)) {
                ioJson.put(key, new JSONArray());
            }
            arr = ioJson.getJSONArray(key);
            ListUtils.emptyIfNull(ios).stream().map(this::writeByWriter).forEach(arr::add);
        });
        return ioJson;
    }
}
