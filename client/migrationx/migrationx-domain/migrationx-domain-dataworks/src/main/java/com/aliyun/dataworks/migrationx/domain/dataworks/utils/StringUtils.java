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

package com.aliyun.dataworks.migrationx.domain.dataworks.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.CharacterIterator;
import java.text.MessageFormat;
import java.text.StringCharacterIterator;

/**
 * @author sam.liux
 * @date 2020/08/18
 */
public class StringUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);
    private static final String NVL_VALUE = "nvl";

    public static boolean isAllAlphabetUpperCase(String string) {
        if (org.apache.commons.lang3.StringUtils.isBlank(string)) {
            return false;
        }

        char[] chars = string.toCharArray();
        for (char c : chars) {
            if (Character.isLetter(c) && !Character.isUpperCase(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllAlphabetLowerCase(String string) {
        if (org.apache.commons.lang3.StringUtils.isBlank(string)) {
            return false;
        }

        char[] chars = string.toCharArray();
        for (char c : chars) {
            if (Character.isLetter(c) && !Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }

    public static String toHttps(String url) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(url)
            && org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(url, "http:")) {
            return org.apache.commons.lang3.StringUtils.replace(url, "http:", "https:");
        }

        return url;
    }

    public static String format(String template, Object... info) {
        try {
            return MessageFormat.format(template, info);
        } catch (Exception e) {
            LOGGER.warn("format string: {}, info: {}, error: {}", template, info, e.getMessage());
        }
        return template;
    }

    public static String escapeRegexChars(String str) {
        if (org.apache.commons.lang3.StringUtils.isBlank(str)) {
            return str;
        }

        return str
            .replace("\\", "\\\\")
            .replace("$", "\\$")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("|", "\\|")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace(",", "\\,")
            .replace("^", "\\^")
            ;
    }

    public static String toValidName(String name) {
        if (org.apache.commons.lang3.StringUtils.isBlank(name)) {
            return name;
        }

        return name
            .replaceAll(":", "_")
            .replaceAll("-", "_")
            .replaceAll("\\*", "_")
            .replaceAll("\\\\", "_")
            .replaceAll("\\+", "_")
            .replaceAll("%", "_")
            .replaceAll("/", "_")
            .replaceAll("=", "_")
            .replaceAll("&", "_")
            .replaceAll("#", "_")
            .replaceAll("@", "_")
            .replaceAll("!", "_")
            .replaceAll("~", "_")
            .replaceAll("`", "_")
            .replaceAll("\\$", "_")
            .replaceAll("\\^", "_")
            .replaceAll("\\[", "__").replaceAll("]", "__")
            .replaceAll("\\{", "__").replaceAll("}", "__")
            .replaceAll("\\(", "__").replaceAll("\\)", "__");
    }

    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    public static String replaceControlCharacterPrintable(String rawString) {
        if (org.apache.commons.lang3.StringUtils.isBlank(rawString)) {
            return rawString;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (char ch : rawString.toCharArray()) {
            if (Character.isISOControl(ch)) {
                stringBuilder.append(StringEscapeUtils.escapeJava(ch + ""));
            } else {
                stringBuilder.append(ch);
            }
        }
        return stringBuilder.toString();
    }

    public static boolean isNvlValue(String value) {
        return org.apache.commons.lang3.StringUtils.equalsIgnoreCase(value, NVL_VALUE);
    }
}
