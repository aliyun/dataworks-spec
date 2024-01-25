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

import com.aliyun.dataworks.common.spec.domain.SpecEntity;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import org.apache.commons.collections4.MapUtils;

/**
 * default spec entity writer
 *
 * @author 聿剑
 * @date 2023/8/27
 */
public class DefaultJsonObjectWriter<T extends SpecEntity> extends AbstractWriter<T, JSONObject> {
    public DefaultJsonObjectWriter(SpecWriterContext context) {
        super(context);
    }

    @Override
    public JSONObject write(T specObj, SpecWriterContext context) {
        return writeJsonObject(specObj, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected JSONObject writeJsonObject(Object specObj, boolean withoutCollectionFields) {
        JSONObject json = super.writeJsonObject(specObj, withoutCollectionFields);
        if (specObj instanceof SpecEntity) {
            Optional.ofNullable(((T)specObj).getMetadata()).filter(MapUtils::isNotEmpty).ifPresent(metadata -> json.put("metadata", metadata));
        }
        return json;
    }
}
