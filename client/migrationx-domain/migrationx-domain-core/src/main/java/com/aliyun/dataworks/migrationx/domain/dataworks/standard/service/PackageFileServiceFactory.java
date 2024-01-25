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

package com.aliyun.dataworks.migrationx.domain.dataworks.standard.service;

import com.aliyun.dataworks.migrationx.domain.dataworks.standard.objects.Package;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;

/**
 * @author 聿剑
 * @date 2023/02/15
 */
public class PackageFileServiceFactory {
    /**
     * TODO: create package file service
     * @param thePackage
     * @return
     * @param <T>
     */
    public static <T extends Package> PackageFileService<T> create(T thePackage) {
        throw new BizException(ErrorCode.UNSUPPORTED_PACKAGE).with(thePackage.getClass().getSimpleName());
    }
}
