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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.service;

import java.io.File;
import java.util.Optional;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.standard.service.AbstractPackageFileService;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import com.aliyun.migrationx.common.utils.ZipUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2023/02/10
 */
@SuppressWarnings("rawtypes")
@Slf4j
public class DolphinSchedulerPackageFileService extends AbstractPackageFileService<DolphinSchedulerPackage> {
    protected DolphinSchedulerPackage dolphinSchedulerPackage;

    @Override
    protected boolean isProjectRoot(File file) {
        return file.isFile() && (
                StringUtils.equals(file.getName(), "projects.json")
                        || StringUtils.equals(file.getName(), "package_info.json")
        );
    }

    @Override
    public void load(DolphinSchedulerPackage dolphinSchedulerPackage) throws Exception {
        File unzippedDir;
        if (dolphinSchedulerPackage.getPackageFile().isFile()) {
            unzippedDir = ZipUtils.decompress(dolphinSchedulerPackage.getPackageFile());
        } else {
            unzippedDir = dolphinSchedulerPackage.getPackageFile();
        }

        DolphinSchedulerPackageLoader loader = DolphinSchedulerPackageLoader.create(unzippedDir);
        loader.loadPackage();
        this.dolphinSchedulerPackage = loader.getDolphinSchedulerPackage();
    }

    @Override
    public DolphinSchedulerPackage getPackage() throws Exception {
        return Optional.ofNullable(dolphinSchedulerPackage)
                .orElseThrow(() -> new BizException(ErrorCode.PACKAGE_NOT_LOADED));
    }

    @Override
    public void write(DolphinSchedulerPackage pacakgeModelObject, File targetPackageFile) throws Exception {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
