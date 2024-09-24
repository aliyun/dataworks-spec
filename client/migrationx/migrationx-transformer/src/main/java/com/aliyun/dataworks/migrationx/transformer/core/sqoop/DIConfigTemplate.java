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

package com.aliyun.dataworks.migrationx.transformer.core.sqoop;

public class DIConfigTemplate {
    public static String DI_CODE_TEMPLATE = "{\n" +
        "    \"order\": {\n" +
        "        \"hops\": [\n" +
        "            {\n" +
        "                \"from\": \"Reader\",\n" +
        "                \"to\": \"Writer\"\n" +
        "            }\n" +
        "        ]\n" +
        "    },\n" +
        "    \"setting\": {\n" +
        "        \"errorLimit\": {\n" +
        "            \"record\": \"0\"\n" +
        "        },\n" +
        "        \"speed\": {\n" +
        "            \"concurrent\": 1,\n" +
        "            \"dmu\": 1,\n" +
        "            \"throttle\": true\n" +
        "        }\n" +
        "    },\n" +
        "    \"steps\": [\n" +
        "        {\n" +
        "            \"category\": \"reader\",\n" +
        "            \"name\": \"Reader\",\n" +
        "            \"parameter\": {},\n" +
        "            \"stepType\": \"\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"category\": \"writer\",\n" +
        "            \"name\": \"Writer\",\n" +
        "            \"parameter\": {},\n" +
        "            \"stepType\": \"\"\n" +
        "        }\n" +
        "    ],\n" +
        "    \"type\": \"job\",\n" +
        "    \"version\": \"2.0\"\n" +
        "}";
}
