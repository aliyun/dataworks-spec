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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler;

import com.aliyun.dataworks.migrationx.domain.dataworks.standard.objects.Package;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author 聿剑
 * @date 2022/10/24
 */
@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DolphinSchedulerPackage<Project, ProcessDefinition, Datasource, ResourceInfo, UdfFunc> extends Package {
    private File packageRoot;
    private PackageInfo packageInfo;
    private List<Project> projects;
    private Map<String, List<ProcessDefinition>> processDefinitions;
    private List<Datasource> datasources;
    private List<ResourceInfo> resources;
    private List<UdfFunc> udfFuncs;
}
