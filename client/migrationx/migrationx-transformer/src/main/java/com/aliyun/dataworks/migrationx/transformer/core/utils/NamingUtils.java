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

package com.aliyun.dataworks.migrationx.transformer.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sam.liux
 * @date 2019/08/22
 */
public class NamingUtils {
    private static final Pattern COMMON_IDENTIFIER_PATTERN = Pattern.compile("^[a-z|A-Z][a-z|A-Z|0-9|_]+");

    public static boolean checkIdentifier(String identifier) {
        Matcher m = COMMON_IDENTIFIER_PATTERN.matcher(identifier);
        return m.matches();
    }
}
