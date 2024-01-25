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
import com.aliyun.dataworks.common.spec.domain.SpecConstants;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import com.google.common.base.Preconditions;

/**
 * Specification writer
 *
 * @author 聿剑
 * @date 2023/8/27
 */
@SuppressWarnings("rawtypes")
@SpecWriter
public class SpecificationWriter extends DefaultJsonObjectWriter<Specification> {
    public SpecificationWriter(SpecWriterContext context) {
        super(context);
    }

    @Override
    public JSONObject write(Specification specObj, SpecWriterContext context) {
        if (specObj == null || context == null) {
            return null;
        }

        if (specObj.getVersion() != null) {
            context.setVersion(specObj.getVersion());
        }

        Preconditions.checkNotNull(context.getVersion(), "version is null");
        Preconditions.checkNotNull(specObj.getVersion(), "version is null");

        JSONObject jsonObject = writeJsonObject(specObj, true);
        Optional.ofNullable(specObj.getVersion()).orElseThrow(() -> new SpecException(SpecErrorCode.PARSE_ERROR, "version is null"));

        switch (specObj.getVersion()) {
            case V_1_0_0: {
                jsonObject.remove(SpecConstants.SPEC_KEY_SPEC);
                JSONObject spec = (JSONObject)writeByWriter(specObj.getSpec());
                if (spec != null) {
                    for (String key : spec.keySet()) {
                        jsonObject.put(key, spec.get(key));
                    }
                }
                break;
            }
            case V_1_1_0: {
                jsonObject.put(SpecConstants.SPEC_KEY_SPEC, writeByWriter(specObj.getSpec()));
                break;
            }
        }
        return jsonObject;
    }
}