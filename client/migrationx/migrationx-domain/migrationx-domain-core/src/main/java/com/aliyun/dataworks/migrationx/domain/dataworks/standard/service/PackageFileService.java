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

import java.io.File;
import java.util.Locale;

/**
 * @author 聿剑
 * @date 2023/02/10
 */
public interface PackageFileService<T extends Package> {
    /**
     * 设置语言
     * @param locale Locale
     */
    void setLocale(Locale locale);
    /**
     * 加载一个包
     * @param packageObj package
     */
    void load(T packageObj) throws Exception;

    /**
     * 获取加载后的领域对象
     * @return T package
     */
    T getPackage() throws Exception;

    /**
     * 将领域对象写入包文件
     * @param pacakgeModelObject T
     * @param targetPackageFile File
     */
    void write(T pacakgeModelObject, File targetPackageFile) throws Exception;
}
