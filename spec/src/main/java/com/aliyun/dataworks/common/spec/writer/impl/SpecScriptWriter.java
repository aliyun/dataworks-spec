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

import java.util.List;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.annotation.SpecWriter;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

/**
 * @author 聿剑
 * @date 2023/8/27
 */
@SpecWriter
public class SpecScriptWriter extends DefaultJsonObjectWriter<SpecScript> {
    public SpecScriptWriter(SpecWriterContext context) {
        super(context);
    }

    @Override
    public JSONObject write(SpecScript specObj, SpecWriterContext context) {
        JSONObject json = writeJsonObject(specObj, true);
        json.put("parameters", writeParameters(specObj.getParameters()));
        json.put("runtime", writeByWriter(specObj.getRuntime()));
        return json;
    }

    private JSONArray writeParameters(List<SpecVariable> parameters) {
        if (CollectionUtils.isEmpty(parameters)) {
            return null;
        }

        JSONArray jsonArray = new JSONArray();
        ListUtils.emptyIfNull(parameters).stream().map(this::writeByWriter).forEach(jsonArray::add);
        return jsonArray;
    }
}
