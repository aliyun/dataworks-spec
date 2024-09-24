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

package com.aliyun.migrationx.common.utils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
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

    static {
        initGson();
    }

    synchronized public static void initGson() {
        gson = new GsonBuilder().setDateFormat(DATE_FORMAT).setPrettyPrinting().create();
        gsonNoPrettyPrint = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
        defaultGson = new GsonBuilder().setDateFormat(DATE_FORMAT).disableHtmlEscaping().create();
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
            LOGGER.error("parse json string: {}, exception: ", jsonString, e);
            throw e;
        }
    }

    public static <T> T fromJson(JsonObject json, Type type) {
        if (json == null) {
            return null;
        }

        try {
            return gson.fromJson(json, type);
        } catch (Throwable e) {
            LOGGER.error("parse json string: {}, exception: ", json, e);
            throw e;
        }
    }

    public static String toEnumString(Enum enumObj) {
        if (enumObj != null) {
            return enumObj.name();
        }

        return null;
    }

    public static class LocaleAdapter implements JsonSerializer<Locale>, JsonDeserializer<Locale> {
        @Override
        public Locale deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {
            if (jsonElement.isJsonPrimitive()) {
                String locale = jsonElement.getAsString();
                return Arrays.stream(Locale.getAvailableLocales())
                        .filter(lc -> StringUtils.equalsIgnoreCase(locale, lc.toString()))
                        .findAny().orElse(null);
            }

            throw new RuntimeException("invalid locale type: " + type + ", json: {}" + jsonElement);
        }

        @Override
        public JsonElement serialize(Locale locale, Type type, JsonSerializationContext jsonSerializationContext) {
            return Optional.ofNullable(locale)
                    .map(Locale::toString)
                    .map(l -> jsonSerializationContext.serialize(l, String.class))
                    .orElse(jsonSerializationContext.serialize(null));
        }
    }
}
