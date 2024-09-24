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

package com.aliyun.dataworks.migrationx.domain.dataworks.aliyunemr;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2024/5/12
 */
@Slf4j
public class ParamUtil {
    private static final String DEFAULT_PARAMETER_CONVERT_PATTERN
        = "\\$\\{(?<expr>[y{4}|M{2}|d{2}|H{2}|h{2}|m{2}|s{2}|:|a-z|A-Z||_| ]+)\\s*((?<opr>[-|+])\\s*(?<count>\\d+)(?<unit>[y|m|d|h]))?}";

    public static String convertParameterExpression(String value) {
        Pattern p = Pattern.compile(DEFAULT_PARAMETER_CONVERT_PATTERN);
        Matcher m = p.matcher(value);
        boolean matched = false;
        String newValue = value;
        while (m.find()) {
            matched = true;
            String dateExpr = m.group("expr");
            String opr = m.group("opr");
            String count = m.group("count");
            String unit = m.group("unit");
            String newExpr = dateExpr
                .replace("mm", "mi")
                .replace("MM", "mm")
                .replace("HH", "hh24");
            String oprSuffix = null;
            if ("h".equalsIgnoreCase(unit)) {
                oprSuffix = "/24";
            }
            String newOpr = Optional.ofNullable(opr).orElse("")
                + Optional.ofNullable(count).orElse("") +
                Optional.ofNullable(oprSuffix).orElse("");
            if ("y".equalsIgnoreCase(unit)) {
                newExpr = "yyyy";
            }
            // 小时粒度
            newValue = "$[" + newExpr.trim() + newOpr + "]";
            if (StringUtils.indexOfIgnoreCase(newExpr, "dd") < 0) {
                // >天粒度，为月，年粒度
                newValue = "${" + newExpr.trim() + newOpr + "}";
            }
        }
        if (matched) {
            // log.info("convert emr job parameter expr: {} to dataworks expr: {}, by pattern: {}", value, newValue,
            // DEFAULT_PARAMETER_CONVERT_PATTERN);
            return newValue;
        }

        return value;
    }
}
