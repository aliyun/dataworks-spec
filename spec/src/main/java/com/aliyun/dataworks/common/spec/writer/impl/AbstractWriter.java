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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import com.aliyun.dataworks.common.spec.writer.Writer;
import com.aliyun.dataworks.common.spec.writer.WriterFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

/**
 * Specification writer
 *
 * @author 聿剑
 * @date 2023/8/27
 */
@Slf4j
public abstract class AbstractWriter<T, V> implements Writer<T, V> {
    protected SpecWriterContext context;

    private final ParameterizedType parameterizedType = (ParameterizedType)this.getClass().getGenericSuperclass();

    public AbstractWriter(SpecWriterContext context) {
        this.context = context;
    }

    public boolean matchType(Class<T> t) {
        Type tType = parameterizedType.getActualTypeArguments()[0];
        boolean equals = Objects.equals(t, tType);
        if (equals) {
            return true;
        }

        if (tType instanceof Class<?>) {
            return ((Class<?>)tType).isAssignableFrom(t);
        }
        return false;
    }

    protected JSONObject writeJsonObject(Object specObj, boolean withoutCollectionFields) {
        return SpecDevUtil.writeJsonObject(specObj, withoutCollectionFields);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Object writeByWriter(Object specObj) {
        if (specObj == null) {
            return null;
        }

        if (specObj instanceof List) {
            return writerListByWriter((List)specObj);
        }

        Writer writer = WriterFactory.getWriter(specObj.getClass(), context);
        if (writer != null) {
            return writer.write(specObj, context);
        }

        return writeJsonObject(specObj, false);
    }

    protected JSONArray writerListByWriter(List<Object> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return null;
        }

        JSONArray arr = new JSONArray();
        ListUtils.emptyIfNull(objects).stream().map(this::writeByWriter).forEach(arr::add);
        return arr;
    }
}
