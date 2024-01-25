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

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sam.liux
 * @date 2019/07/22
 */
public class ResourceUtils {
    public static final String PLACEHOLDER_RES = "res";
    public static final String PLACEHOLDER_FILE_NAME = "placeholder";
    public static final String FILE_EXT_JAR = "jar";
    public static final String FILE_EXT_PY = "py";
    public static final String FILE_EXT_ARCHIVE_ZIP = "zip";
    public static final String FILE_EXT_ARCHIVE_TAR = "tar";
    public static final String FILE_EXT_ARCHIVE_TGZ = "tgz";
    public static final String FILE_EXT_ARCHIVE_TAR_GZ = "tar.gz";
    public static final String FILE_EXT_ARCHIVE_NAR = "nar";
    public static final List<String> FILE_EXT_ARCHIVE = new ArrayList<>();

    static {
        FILE_EXT_ARCHIVE.add(FILE_EXT_ARCHIVE_NAR);
        FILE_EXT_ARCHIVE.add(FILE_EXT_ARCHIVE_TAR);
        FILE_EXT_ARCHIVE.add(FILE_EXT_ARCHIVE_TAR_GZ);
        FILE_EXT_ARCHIVE.add(FILE_EXT_ARCHIVE_TGZ);
        FILE_EXT_ARCHIVE.add(FILE_EXT_ARCHIVE_ZIP);
    }

    public static String getFileResourceType(String fileName) {
        return getFileResourceType(fileName, "odps");
    }

    public static String getFileResourceType(String fileName, String engineType) {
        if (fileName.endsWith("." + FILE_EXT_JAR)) {
            return DefaultNodeTypeUtils.getJarResourceType(engineType).name();
        }

        if (fileName.endsWith("." + FILE_EXT_PY)) {
            return CodeProgramType.ODPS_PYTHON.name();
        }

        if (FILE_EXT_ARCHIVE.stream().filter(ext -> fileName.endsWith("." + ext)).count() > 0) {
            return CodeProgramType.ODPS_ARCHIVE.name();
        }

        return DefaultNodeTypeUtils.getFileResourceType(engineType).name();
    }

    public static String getPlaceholderFile(String fileName) {
        if (fileName.endsWith("." + FILE_EXT_JAR)) {
            return PLACEHOLDER_RES + File.separator + PLACEHOLDER_FILE_NAME + "." + FILE_EXT_JAR;
        }

        if (fileName.endsWith("." + FILE_EXT_PY)) {
            return PLACEHOLDER_RES + File.separator + PLACEHOLDER_FILE_NAME + "." + FILE_EXT_PY;
        }

        if (FILE_EXT_ARCHIVE.stream().filter(ext -> fileName.endsWith("." + ext)).count() > 0) {
            return PLACEHOLDER_RES + File.separator + PLACEHOLDER_FILE_NAME + "." + FILE_EXT_ARCHIVE_ZIP;
        }

        return PLACEHOLDER_RES + File.separator + PLACEHOLDER_FILE_NAME;
    }

}
