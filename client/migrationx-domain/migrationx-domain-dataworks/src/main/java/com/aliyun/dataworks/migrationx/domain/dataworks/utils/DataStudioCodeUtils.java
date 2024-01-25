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

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeConstants;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.google.common.base.Joiner;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @author 聿剑
 * @date 2021/09/10
 */
public class DataStudioCodeUtils {
    public static String addResourceReference(CodeProgramType type, String code, List<String> resources) {
        if (type == null) {
            return code;
        }

        switch (type) {
            case SQL_COMPONENT:
            case ODPS_SQL:
            case EMR_SPARK_SQL:
            case CLICK_SQL:
            case EMR_HIVE:
            case EMR_IMPALA:
            case EMR_PRESTO:
            case CDH_SPARK_SQL:
            case CDH_IMPALA:
            case CDH_PRESTO:
                return addSqlResourceReference(code, resources);
            default:
                return addShellResourceReference(code, resources);
        }
    }

    private static String addSqlResourceReference(String code, List<String> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            return code;
        }

        String references = Joiner.on("\n").join(resources.stream()
            .map(str -> "--@resource_reference{\"" + str + "\"}")
            .collect(Collectors.toList()));

        return Joiner.on('\n').join(references, code);
    }

    private static String addShellResourceReference(String code, List<String> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            return code;
        }

        String references = Joiner.on("\n").join(resources.stream()
            .filter(Objects::nonNull)
            .map(str -> "##@resource_reference{\"" + str + "\"}")
            .collect(Collectors.toList()));

        return Joiner.on('\n').join(references, code);
    }

    public static List<String> parseResourceReference(String code) {
        if (StringUtils.isBlank(code)) {
            return ListUtils.emptyIfNull(null);
        }

        Matcher m = CodeConstants.RESOURCE_REFERENCE_PATTERN.matcher(code);

        List<String> resources = new ArrayList<>();
        while (m.find()) {
            resources.add(m.group("resourceName"));
        }
        return resources;
    }
}
