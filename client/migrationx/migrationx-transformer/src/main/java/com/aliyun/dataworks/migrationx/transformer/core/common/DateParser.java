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

package com.aliyun.dataworks.migrationx.transformer.core.common;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description
 * @createTime 2020-04-03
 */

public class DateParser {
    /**
     * 时间变量标识符 正则表达式
     */
    static Pattern patternVariable = Pattern.compile("@@(\\{[^\\}]+\\}|\\[[^\\]]+\\])");

    static Pattern patternOffset = Pattern.compile("[+-]\\s*\\d+(y|m|d|h|mi|s)?$");
    static Pattern patternFormat = Pattern.compile("(yy|yyyy|mm|dd|hh24|hh|mi|ss|-|:|_|\\s)+");

    /**
     * 变量标识符
     */
    public final static String VAR_BIZDATE_START = "{";
    public final static String VAR_BIZDATE_END = "}";
    public final static String VAR_DUE_TIME_START = "[";
    public final static String VAR_DUE_TIME_END = "]";
    public final static String VAR_D2_SYMBOL = "$";
    public final static String VAR_DATAGO_SYMBOL = "@@";

    public static Pattern getPattern() {
        return patternVariable;
    }

    public static String parse(String param) {
        Matcher m = getPattern().matcher(param);
        while (m.find()) {
            param = param.replace(m.group(), DateParser.parseExpr(m.group()));
        }
        return param;
    }

    /**
     * 解析 @@{}, @@[]
     *
     * @param expr
     * @return
     */
    public static String parseExpr(String expr) {
        // get {,[
        String symbolType = getSymbol(expr);
        if (StringUtils.isBlank(symbolType)) {
            return expr;
        }
        // 替换成d2格式 替换mm->mi MM->mm HH ->hh24
        expr = expr.replace("@@", "$").replace("mm", "mi").replace("MM", "mm").replace("HH", "hh24");
        // remove '$'
        String var = expr.substring(2, expr.length() - 1);

        String formatStr = null;
        String offsetStr = null;

        StringBuffer sb = new StringBuffer();

        Matcher m = patternOffset.matcher(var);
        // 检查 offset 部分, 同时替换掉 offset 部分
        while (m.find()) {
            offsetStr = m.group().trim();
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);

        // 只剩时间格式部分
        var = sb.toString();

        // 检查 日期格式 部分
        m = patternFormat.matcher(var);

        if (m.matches()) {
            formatStr = m.group().trim();
        } else {
            return expr;
        }

        if (offsetStr != null) {
            return toD2Format(symbolType, formatStr, offsetStr);
        } else {
            return expr;
        }
    }

    static String getSymbol(String expr) {
        String symbol = expr.substring(2, 3);
        if (expr.contains(VAR_DATAGO_SYMBOL) && (VAR_BIZDATE_START.equals(symbol) || VAR_DUE_TIME_START.equals(
            symbol))) {
            return symbol;
        }
        return "";
    }

    private static String toDateFormat(String symbolType, String formatStr, String offsetStr) {
        StringBuilder d2Format = new StringBuilder();
        int offset = 0;

        // 解析 offset
        // 符号: [+-]
        String symbol = offsetStr.substring(0, 1);
        // 时间: \d+(y|m|d|h|mi|s)
        String period = offsetStr.substring(1).trim();
        // 取 value 的数字部分, 得到 offset 的值
        offset = Integer.valueOf(period.replaceAll("\\D.*", ""));

        //
        if ("-".equals(symbol)) {
            offset = -offset;
        }

        // 根据offset替换成d2的格式
        if (VAR_DUE_TIME_START.equals(symbolType)) {
            if (period.endsWith("mi")) {
                period = period.replace("mi", "/24/60").trim();
            } else if (period.endsWith("h")) {
                period = period.replace("h", "/24").trim();
            } else if (period.endsWith("d")) {
                period = period.replace("d", "").trim();
            } else if (period.endsWith("y")) {
                return d2Format.append("add_months(").append(formatStr).append(",").append(12 * offset).append(")")
                    .toString();
            } else if (period.endsWith("m")) {
                return d2Format.append("add_months(").append(formatStr).append(",").append(offset).append(")")
                    .toString();
            }
        } else {
            period = period.replaceAll("(y|m|d|)", "").trim();
        }
        return d2Format.append(formatStr).append(symbol).append(period).toString();
    }

    private static String toD2Format(String symbolType, String formatStr, String offsetStr) {
        StringBuilder sb = new StringBuilder();
        String d2Format = toDateFormat(symbolType, formatStr, offsetStr);
        if (VAR_BIZDATE_START.equals(symbolType)) {
            sb.append(VAR_D2_SYMBOL).append(VAR_BIZDATE_START).append(d2Format).append(VAR_BIZDATE_END);
        } else {
            sb.append(VAR_D2_SYMBOL).append(VAR_DUE_TIME_START).append(d2Format).append(VAR_DUE_TIME_END);
        }
        return sb.toString();
    }

    public static String parsePartition(String pt) {
        if (pt.contains("=")) {
            pt = pt.split("=")[1];
        }
        return pt;
    }
}
