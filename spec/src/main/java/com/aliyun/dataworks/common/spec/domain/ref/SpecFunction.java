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

package com.aliyun.dataworks.common.spec.domain.ref;

import java.util.List;

import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.enums.FunctionType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecEmbeddedCodeType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecEmbeddedResourceType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yiwei.qyw
 * @date 2023/7/4
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SpecFunction extends SpecRefEntity implements ScriptWired {
    private String name;
    @EqualsAndHashCode.Include
    private SpecScript script;
    private FunctionType type;
    private String className;
    @EqualsAndHashCode.Include
    private SpecDatasource datasource;
    @EqualsAndHashCode.Include
    private SpecRuntimeResource runtimeResource;
    @EqualsAndHashCode.Include
    private List<SpecFileResource> fileResources;
    private String armResource;
    private String usageDescription;
    private String argumentsDescription;
    private String returnValueDescription;
    private String usageExample;
    private SpecEmbeddedCodeType embeddedCodeType;
    private SpecEmbeddedResourceType resourceType;
    private String embeddedCode;
}