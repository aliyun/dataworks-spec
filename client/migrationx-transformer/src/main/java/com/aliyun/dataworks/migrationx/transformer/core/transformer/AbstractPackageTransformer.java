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

package com.aliyun.dataworks.migrationx.transformer.core.transformer;

import com.aliyun.dataworks.migrationx.domain.dataworks.standard.objects.Package;
import com.aliyun.dataworks.migrationx.domain.dataworks.standard.service.PackageFileService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * @author 聿剑
 * @date 2023/02/10
 */
@Slf4j
public abstract class AbstractPackageTransformer<SP extends Package, TP extends Package> implements Transformer {
    protected PackageFileService<SP> sourcePackageFileService;
    protected PackageFileService<TP> targetPackageFileService;
    protected File configFile;
    protected SP sourcePackage;
    protected TP targetPackage;

    public AbstractPackageTransformer(File configFile, SP sourcePackage, TP targetPackage) {
        this.configFile = configFile;
        this.sourcePackage = sourcePackage;
        this.targetPackage = targetPackage;
    }

    public TP getTargetPackage(){
        return this.targetPackage;
    }

    @Override
    public abstract void init() throws Exception;

    @Override
    public abstract void load() throws Exception;

    @Override
    public abstract void transform() throws Exception;

    @Override
    public abstract void write() throws Exception;
}
