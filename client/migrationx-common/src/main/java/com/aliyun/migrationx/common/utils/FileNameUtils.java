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

import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2023/01/11
 */
public class FileNameUtils {
    public static String normalizedFileName(String name) {
        if (StringUtils.isBlank(name)) {
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
}
