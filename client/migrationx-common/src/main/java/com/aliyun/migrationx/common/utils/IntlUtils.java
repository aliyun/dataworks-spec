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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author 聿剑
 * @date 2022/12/28
 */
public class IntlUtils {

    public static Map<String, Map<String, String>> intlResource = new HashMap<>();
    private static final String CLASS_PATH_RESOURCE = "i18n";

    public static IntlBo get(String key) {
        IntlBo intlBo = new IntlBo();
        Map<String, String> resource = getLanguageResource();
        if (null != resource && resource.containsKey(key)) {
            intlBo.setValue(resource.get(key));
        }

        return intlBo;
    }

    public static IntlBo get(String key, Map<String, String> params) {
        IntlBo intlBo = new IntlBo();
        Map<String, String> resource = getLanguageResource();
        if (null != resource && resource.containsKey(key)) {
            AtomicReference<String> value = new AtomicReference<>(resource.get(key));
            MapUtils.emptyIfNull(params).forEach((name, val) ->
                value.set(value.get().replaceAll("\\{" + name + "\\}", val)));
            intlBo.setValue(value.get());
        }

        return intlBo;
    }

    public static Map<String, String> getLanguageResource() {
        String lang = ThreadLocalUtils.getLocale().toString();
        if (null != intlResource && intlResource.containsKey(lang)) {
            return intlResource.get(lang);
        } else {
            try {
                String content = readFileToStr(lang + ".json");
                Map<String, String> result = GsonUtils.fromJsonString(content, new TypeToken<Map<String, String>>(){}.getType());
                intlResource.put(lang, result);
                return result;
            } catch (Exception var8) {
                return null;
            }
        }
    }

    private static String readFileToStr(String fileName) {
        Resource resource = new ClassPathResource(CLASS_PATH_RESOURCE + "/" + fileName);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            String line;

            while((line = br.readLine()) != null) {
                buffer.append(line);
            }

            resource.getInputStream().close();
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static class IntlBo {
        private String value;

        public String d(String content) {
            return null == this.value ? content : this.value;
        }

        public IntlBo() {
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof IntlBo)) {
                return false;
            } else {
                IntlBo other = (IntlBo)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$value = this.getValue();
                    Object other$value = other.getValue();
                    if (this$value == null) {
                        return other$value == null;
                    } else if (!this$value.equals(other$value)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof IntlBo;
        }

        public int hashCode() {
            int result = 1;
            String value = this.getValue();
            result = result * 59 + (value == null ? 43 : value.hashCode());
            return result;
        }

        public String toString() {
            return "IntlUtils.IntlBo(value=" + this.getValue() + ")";
        }
    }

    public enum IntlLang {
        /**
         * Chinese
         */
        zh_CN,
        /**
         * English
         */
        en_US,
        /**
         * Traditional Chinese
         */
        zh_TW;

        public static IntlLang of(Locale locale) {
            if (locale == null) {
                return null;
            }

            for (IntlLang lang : values()) {
                if (StringUtils.equals(lang.name(), locale.toString())) {
                    return lang;
                }
            }
            return null;
        }
    }

    @Data
    @ToString
    @Accessors
    public static class International {
        private final Map<IntlLang, String> intl = new HashMap<>();

        public String getIntl(IntlLang lang, String defaultText) {
            return MapUtils.emptyIfNull(intl).getOrDefault(lang, defaultText);
        }

        public void putIntl(IntlPair... intlPairs) {
            Optional.ofNullable(intlPairs).ifPresent(entries -> Arrays.asList(intlPairs)
                .forEach(ent -> this.intl.put(ent.getLang(), ent.getText())));
        }
    }

    @Data
    @ToString
    @Accessors(chain = true)
    public static class IntlPair {
        private IntlLang lang;
        private String text;

        public static IntlPair of(IntlLang lang, String text) {
            return new IntlPair().setLang(lang).setText(text);
        }
    }

    public static String getByEnum(Enum<?> tEnum) {
        if (tEnum == null) {
            return null;
        }

        return IntlUtils.get(getEnumKey(tEnum)).d(tEnum.name());
    }

    public static String getByEnum(Enum<?> tEnum, Object ... params) {
        if (tEnum == null) {
            return null;
        }

        if (params != null) {
            AtomicInteger index = new AtomicInteger(0);
            Map<String, String> paramMap = Arrays.stream(params).collect(Collectors.toMap(
                v -> String.valueOf(index.getAndIncrement()), String::valueOf));
            return get(getEnumKey(tEnum), paramMap).d(tEnum.name());
        }
        return getByEnum(tEnum);
    }

    private static String getEnumKey(Enum<?> tEnum) {
        return Joiner.on(".").join(tEnum.getClass().getSimpleName(), tEnum.name());
    }
}
