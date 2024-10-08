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

import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.enums.SpecFileResourceType;
import com.aliyun.dataworks.common.spec.domain.ref.file.SpecObjectStorageFile;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author yiwei.qyw
 * @date 2023/7/4
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SpecFileResource extends SpecRefEntity implements ScriptWired {
    /**
     * Resource name
     */
    private String name;
    /**
     * Resource config script
     */
    @EqualsAndHashCode.Include
    private SpecScript script;
    /**
     * Runtime resource for file resource register to calculation engine
     */
    @EqualsAndHashCode.Include
    private SpecRuntimeResource runtimeResource;
    /**
     * Resource type
     */
    private SpecFileResourceType type;
    /**
     * Resource file storage
     */
    @EqualsAndHashCode.Include
    private SpecObjectStorageFile file;
    /**
     * Resource calculate engine datasource
     */
    @EqualsAndHashCode.Include
    private SpecDatasource datasource;
}