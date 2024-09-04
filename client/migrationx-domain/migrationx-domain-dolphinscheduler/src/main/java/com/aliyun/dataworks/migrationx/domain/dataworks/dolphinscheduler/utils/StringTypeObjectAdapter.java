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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.aliyun.migrationx.common.utils.GsonUtils;
import com.aliyun.migrationx.common.utils.JSONUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

/**
 * @author 聿剑
 * @date 2022/10/24
 */
public class StringTypeObjectAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
    private Class<T> clz;

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        if (!jsonElement.isJsonPrimitive()) {
            return jsonDeserializationContext.deserialize(jsonElement, type);
        }

        String str = jsonElement.getAsString();
        if (type instanceof ParameterizedType) {
            return GsonUtils.fromJsonString(str, TypeToken.getParameterized(((ParameterizedType) type).getRawType(),
                    ((ParameterizedType) type).getActualTypeArguments()).getType());
        }

        return JSONUtils.parseObject(str, (Class<T>) type);
    }

    @Override
    public JsonElement serialize(T t, Type type, JsonSerializationContext jsonSerializationContext) {
        return jsonSerializationContext.serialize(GsonUtils.toJsonString(t), String.class);
    }
}
