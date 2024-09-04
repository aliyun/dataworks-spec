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

package com.aliyun.dataworks.migrationx.transformer.dataworks.transformer;

import java.util.Locale;
import java.util.Map;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwProject;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author 聿剑
 * @date 2023/03/13
 */
@Data
@ToString
@Accessors(chain = true)
public class DataWorksTransformerConfig {
    private static final ThreadLocal<DataWorksTransformerConfig> threadLocal = new ThreadLocal<>();

    private DwProject project;
    private Map<String, Object> settings;
    private DataWorksPackageFormat format;
    private Locale locale;

    public static void setConfig(DataWorksTransformerConfig config) {
        threadLocal.set(config);
    }

    public static DataWorksTransformerConfig getConfig() {
        return threadLocal.get();
    }
}
