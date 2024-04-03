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

import java.util.Optional;

import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.annotation.SpecWriter;
import com.aliyun.dataworks.common.spec.domain.noref.SpecAssertion;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;

/**
 * @author 聿剑
 * @date 2024/3/9
 */
@SpecWriter
public class SpecAssertionWriter extends DefaultJsonObjectWriter<SpecAssertion> {
    public SpecAssertionWriter(SpecWriterContext context) {
        super(context);
    }

    @Override
    public JSONObject write(SpecAssertion specObj, SpecWriterContext context) {
        JSONObject json = super.write(specObj, context);
        Optional.ofNullable(specObj.getIn()).ifPresent(in -> json.put("in", in.getValue()));
        return json;
    }
}
