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

package com.aliyun.dataworks.common.spec.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sam.liux
 * @date 2019/05/04
 */
public class GsonUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(GsonUtils.class);

    public static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static Gson gson;
    public static Gson gsonNoPrettyPrint;
    public static Gson defaultGson;
    public static Gson longDateJson;

    static {
        initGson();
    }

    synchronized public static void initGson() {
        LOGGER.info("initialize GsonUtils ...");
        gson = new GsonBuilder().setDateFormat(DATE_FORMAT).setPrettyPrinting().create();
        gsonNoPrettyPrint = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
        defaultGson = new GsonBuilder().setDateFormat(DATE_FORMAT).disableHtmlEscaping().create();
        longDateJson = new GsonBuilder().registerTypeAdapter(Date.class, new DateLongAdapter()).create();
        LOGGER.info("initialize GsonUtils done.");
    }

    public static class DateLongAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            if (json != null && json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
                return convertLongToDate(json.getAsLong());
            }
            return context.deserialize(json, typeOfT);
        }

        public Date convertLongToDate(long timestamp) {
            GregorianCalendar gc = (GregorianCalendar)GregorianCalendar.getInstance();
            gc.setTimeInMillis(timestamp);
            return gc.getTime();
        }

        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                context.serialize(src.getTime(), new TypeToken<Long>() {}.getType());
            }
            return context.serialize(src, typeOfSrc);
        }
    }

    public static class EnumKeyMapDeserializerAdapter<E extends Enum<E>, V>
        implements JsonDeserializer<Map<E, V>>, JsonSerializer<Map<E, V>> {
        /**
         * 反序列化时，Map的Key如果遇到未知的枚举类型，则跳过，不写入Map
         *
         * @param json
         * @param typeOfT
         * @param context
         * @return
         * @throws JsonParseException
         */
        @Override
        public Map<E, V> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            if (!json.isJsonObject()) {
                return context.deserialize(json, typeOfT);
            }

            Type[] argTypes = ((ParameterizedType)typeOfT).getActualTypeArguments();
            Map<E, V> resultMap = new HashMap<>();
            ((JsonObject)json).entrySet().forEach(ent -> {
                E k = EnumUtils.getEnum((Class<E>)argTypes[0], ent.getKey());
                if (k != null) {
                    resultMap.put(k, context.deserialize(ent.getValue(), argTypes[1]));
                } else {
                    LOGGER.warn("get unknown enum value by name: {}, value: {}", ent.getKey(), ent.getValue());
                }
            });
            return resultMap;
        }

        @Override
        public JsonElement serialize(Map<E, V> src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src, typeOfSrc);
        }
    }

    public static String toJsonString(Object object) {
        if (object != null) {
            return gson.toJson(object);
        }
        return null;
    }

    public static <T> T fromJsonString(String jsonString, Type type) {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }

        try {
            return gson.fromJson(jsonString, type);
        } catch (Throwable e) {
            LOGGER.error("parse json type: {}, string: {}, exception: ", type.getTypeName(), jsonString, e);
            throw e;
        }
    }

    public static String toEnumString(Enum enumObj) {
        if (enumObj != null) {
            return enumObj.name();
        }

        return null;
    }
}
