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

import java.util.Arrays;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2023/11/7
 */
public class MapKeyMatchUtils {
    public static String pluralizeWithS(String word) {
        if (word == null) {
            return null;
        }

        return word + "s";
    }

    public static String singularTrimS(String word) {
        if (word == null) {
            return null;
        }

        if (StringUtils.endsWithIgnoreCase(word, "s")) {
            return word.substring(0, word.length() - 1);
        }

        return word;
    }

    public static boolean matchIgnoreSinglePluralForm(String key, String toMatch) {
        return key.equalsIgnoreCase(pluralizeWithS(toMatch)) || key.equalsIgnoreCase(singularTrimS(toMatch));
    }

    public static <V> boolean containsIgnoreCase(java.util.Map<String, V> map, String... key) {
        if (map == null) {
            return false;
        }

        return map.keySet().stream().anyMatch(k -> Arrays.stream(key).anyMatch(k1 -> StringUtils.equalsIgnoreCase(k, k1)));
    }

    public static <V> V getIgnoreCaseSingleAndPluralForm(java.util.Map<String, V> map, String... key) {
        if (map == null || key == null || key.length == 0) {
            return null;
        }

        return map.keySet().stream()
            .filter(k -> Arrays.stream(key).anyMatch(k1 -> matchIgnoreSinglePluralForm(k, k1))).findAny().map(map::get)
            .orElse(null);
    }

    public static <V> V getValue(java.util.Map<String, V> map, BiPredicate<String, String> keyMatcher, String... key) {
        if (map == null || key == null || key.length == 0) {
            return null;
        }

        return map.keySet().stream()
            .filter(k -> Arrays.stream(key).anyMatch(k1 -> keyMatcher.test(k, k1))).findAny().map(map::get)
            .orElse(null);
    }

}
