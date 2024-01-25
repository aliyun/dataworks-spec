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

package com.aliyun.dataworks.migrationx.domain.dataworks.constants;

import java.util.regex.Pattern;

/**
 * @author 聿剑
 * @date 2023/01/12
 */
public class DataWorksConstants {
    public static final String OLD_VERSION_WORKFLOW_NAME = "OLD_VERSION_WORKFLOW";
    public static final String OLD_VERSION_WORKFLOW_FOLDER = "workflowroot";

    public static Pattern PROJECT_PREFIX_CODE_MATCH_PATTERN = Pattern.compile(
        "(?<prefixWhiteSpace>\\s+)(?<projectIdentifier>(?!odps)[a-zA-Z][0-9A-Za-z\\-_]{3,})(?<dot>\\.)"
            + "(?<tableName>[a-zA-Z]+.*\\s*)",
        Pattern.CASE_INSENSITIVE);
}
