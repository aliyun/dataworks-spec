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
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * @author 聿剑
 * @date 2023/02/10
 */
@Slf4j
public abstract class AbstractPackageFileService<T extends Package> implements PackageFileService<T> {
    protected Locale locale;

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    protected abstract boolean isProjectRoot(File file);

    protected File getPackageRoot(File unzippedDir) {
        if (unzippedDir.listFiles() == null) {
            return null;
        }

        Optional<File> root = Arrays.stream(Objects.requireNonNull(unzippedDir.listFiles())).filter(this::isProjectRoot)
            .findAny();
        if (root.isPresent()) {
            File rootDir = root.get();
            boolean hasSibling = Arrays.stream(Objects.requireNonNull(rootDir.getParentFile().listFiles()))
                .filter(sibling -> !sibling.equals(rootDir) && sibling.isDirectory())
                .filter(sibling -> sibling.listFiles() != null)
                .anyMatch(sibling -> Arrays.stream(Objects.requireNonNull(sibling.listFiles()))
                    .anyMatch(this::isProjectRoot));
            return hasSibling ? rootDir.getParentFile() : unzippedDir;
        }

        for (File subdir : Objects.requireNonNull(unzippedDir.listFiles(File::isDirectory))) {
            File dirRoot = getPackageRoot(subdir);
            if (dirRoot != null) {
                return dirRoot;
            }
        }
        return null;
    }
}
