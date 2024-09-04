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

package com.aliyun.dataworks.common.spec.parser.impl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.parser.SpecParserFactory;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;

/**
 * Auto set default parser for spec, which is used when json key is same as domain field name.
 *
 * @author yiwei.qyw
 * @date 2023/7/21
 */
@Slf4j
public class DefaultSpecParser<T> implements Parser<T> {

    private Class<?> type = null;

    public DefaultSpecParser() {}

    public DefaultSpecParser(Class<?> type) {
        this.type = type;
    }

    @Override
    public T parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        T specObj = instantiateSpecObject();

        SpecDevUtil.setSameKeyField(rawContext, specObj, specParserContext);
        return specObj;
    }

    @SuppressWarnings("unchecked")
    protected T instantiateSpecObject() {
        if (type == null) {
            type = getParameterizedObjectType(0);
        }

        if (type == null) {
            throw new SpecException(SpecErrorCode.PARSE_ERROR, "parser can not find type of " + this.getClass());
        }

        try {
            return (T)type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("instantiate spec object type: {} error: ", type, e);
            throw new SpecException(e, SpecErrorCode.PARSE_ERROR,
                "parser create spec object error. type: " + type.getName() + ", msg:" + e.getMessage() + ".");
        }
    }

    /**
     * get class's generic type
     *
     * @return this class's generic type
     */
    @SuppressWarnings("unchecked")
    public Class<T> getParameterizedObjectType() {
        return (Class<T>)getParameterizedObjectType(0);
    }

    /**
     * Through reflection, gets the type of the canonical parameter of the parent Class declared when class is defined.
     * Such as public BookManager extends GenricManager<Book>
     *
     * @param index the Index of the generic declaration,start from 0.
     */
    protected Class<?> getParameterizedObjectType(int index) throws IndexOutOfBoundsException {
        Type genType = this.getClass().getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType)genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class<?>)params[index];
    }

    @SuppressWarnings("unchecked")
    private Object parseFieldObject(String jsonKey, Class<?> fieldType, Object fieldObject, Class<?> actualTypeArgument,
        SpecParserContext specParserContext) {
        // get custom parser
        Parser<?> parser = getCustomParser(jsonKey);
        // default parser getter
        if (parser == null) {
            parser = SpecParserFactory.getParser(actualTypeArgument.getSimpleName());
            if (parser == null) {
                SpecException ex = new SpecException(SpecErrorCode.PARSER_NOT_FOUND, "cannot find parser for: " + jsonKey);
                log.warn("{}", ex.getMessage());
                if (fieldObject != null) {
                    log.debug("try parse object: {} by json deserialization", fieldObject);
                    if (isListField(fieldType, fieldObject)) {
                        TypeToken<?> typeToken = TypeToken.getParameterized(List.class, actualTypeArgument);
                        return GsonUtils.fromJsonString(GsonUtils.toJsonString(fieldObject), typeToken.getType());
                    }
                    return GsonUtils.fromJsonString(GsonUtils.toJsonString(fieldObject), actualTypeArgument);
                }
                return null;
            }
        }

        if (isListField(fieldType, fieldObject)) {
            // list type
            return getListObj(parser, (List<?>)fieldObject, specParserContext);
        }

        Object entity = parser.parse((Map<String, Object>)fieldObject, specParserContext);
        SpecDevUtil.setEntityToCtx(entity, specParserContext);
        return entity;
    }

    protected void parseSpecObjectFields(T specObject, Map<String, Object> contextMap, SpecParserContext specParserContext) {
        List<Field> fields = SpecDevUtil.getPropertyFields(specObject);
        for (String jsonKey : contextMap.keySet()) {
            Field declaredField;
            try {
                declaredField = ListUtils.emptyIfNull(fields).stream().filter(fd -> fd.getName().equals(jsonKey)).findAny()
                    .orElseThrow(() -> new NoSuchFieldException(jsonKey));
            } catch (Exception e) {
                if (!BooleanUtils.isTrue(specParserContext.getIgnoreMissingFields())) {
                    throw new SpecException(e, SpecErrorCode.FIELD_NOT_FOUND,
                        "Cannot find field in Specification. JSON key:" + jsonKey);
                } else {
                    log.info("ignore missing field: {} in spec object type: {}", jsonKey, specObject.getClass().getSimpleName());
                    continue;
                }
            }

            Class<?> fieldType = declaredField.getType();
            // skip field type is String Integer Enum
            if (SpecDevUtil.isSimpleType(fieldType)) {
                continue;
            }

            Class<?> actualTypeArgument = SpecDevUtil.getFieldClz(declaredField);

            // get parsed domain object
            Object entity = parseFieldObject(jsonKey, fieldType, contextMap.get(jsonKey), actualTypeArgument, specParserContext);
            SpecDevUtil.setValue(specObject, declaredField, entity);
        }
    }

    protected Parser<?> getCustomParser(String jsonKey) {
        return null;
    }

    protected boolean isListField(Class<?> fieldType, Object fieldObject) {
        return fieldObject instanceof List && fieldType == List.class;
    }

    @SuppressWarnings("unchecked")
    protected ArrayList<Object> getListObj(Parser<?> parser, List<?> fieldObject, SpecParserContext specParserContext) {
        ArrayList<Object> entityList = new ArrayList<>();
        for (Object obj : fieldObject) {
            Object parsed = parser.parse((Map<String, Object>)obj, specParserContext);
            SpecDevUtil.setEntityToCtx(parsed, specParserContext);
            entityList.add(parsed);
        }
        return entityList;
    }

}