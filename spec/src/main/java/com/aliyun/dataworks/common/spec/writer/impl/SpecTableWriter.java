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

import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.annotation.SpecWriter;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTable;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;

/**
 * @author 聿剑
 * @date 2023/8/27
 */
@SpecWriter
public class SpecTableWriter extends DefaultJsonObjectWriter<SpecTable> {
    public SpecTableWriter(SpecWriterContext context) {
        super(context);
    }

    @Override
    public JSONObject write(SpecTable specObj, SpecWriterContext context) {
        JSONObject json = writeJsonObject(specObj, true);
        json.put("calcEngine", writeByWriter(specObj.getCalcEngine()));
        return json;
    }
}
