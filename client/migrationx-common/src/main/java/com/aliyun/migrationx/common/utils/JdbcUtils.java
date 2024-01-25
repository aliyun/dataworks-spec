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

/**
 * @author sam.liux
 * @date 2020/08/13
 */
public class JdbcUtils {
    public static String getDbName(String jdbcUrl) {
        String url = jdbcUrl;
        int qIndex = jdbcUrl.indexOf("?");
        if (qIndex != -1) {
            url = jdbcUrl.substring(0, qIndex);
        }

        String[] tokens = url.split("/");
        return tokens[tokens.length - 1];
    }
}
