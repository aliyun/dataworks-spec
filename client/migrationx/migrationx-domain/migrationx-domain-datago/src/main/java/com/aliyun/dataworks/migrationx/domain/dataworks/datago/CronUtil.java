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

package com.aliyun.dataworks.migrationx.domain.dataworks.datago;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description
 * @createTime 2020-04-28
 */
@Slf4j
public class CronUtil {

    public static String parseCron(String expression) {
        if (StringUtils.isBlank(expression)) {
            return expression;
        }
        String[] fields = expression.split(" ");
        fields[0] = convertField(fields[0]);
        fields[1] = convertField(fields[1]);
        fields[2] = convertField(fields[2]);
        return Joiner.on(" ").join(fields);
    }

    private static String convertField(String filed) {
        return convertField(filed, "/", 1);
    }

    private static String convertField(String field, String delimiter, int maxLength) {
        if (field.contains(delimiter)) {
            StringBuilder stringBuilder = new StringBuilder();
            String[] items = field.split(delimiter);
            for (int i = 0; i < items.length; i++) {
                String s = items[i];
                if (s.contains("-")) {
                    s = convertField(s, "-", 2);
                }
                if (StringUtils.isNumeric(s)) {
                    if (i < maxLength) {
                        s = String.format("%02d", Integer.parseInt(s));
                    }
                }
                stringBuilder.append(s);
                if (i < items.length - 1) {
                    stringBuilder.append(delimiter);
                }
            }
            field = stringBuilder.toString().trim();
        } else if (StringUtils.isNumeric(field)) {
            field = String.format("%02d", Integer.parseInt(field));
        }
        return field;
    }

}
