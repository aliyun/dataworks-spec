/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.core.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.aliyun.dataworks.common.spec.domain.enums.SpecFileResourceType;
import org.apache.commons.lang3.StringUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-25
 */
public class SpecFileResourceTypeUtils {

    private static final String PYTHON_SUFFIX = ".py";

    private static final String JAR_SUFFIX = ".jar";

    private static final String ARCHIVE_SUFFIX_ZIP = ".zip";

    private static final String ARCHIVE_SUFFIX_TAR = ".tar";

    private static final String ARCHIVE_SUFFIX_TGZ = ".tgz";

    private static final String ARCHIVE_SUFFIX_TAR_GZ = ".tar.gz";

    private static final String ARCHIVE_SUFFIX_NAR = ".nar";

    private static final Set<String> ARCHIVE_SUFFIX = new HashSet<>(Arrays.asList(ARCHIVE_SUFFIX_ZIP,
        ARCHIVE_SUFFIX_TAR, ARCHIVE_SUFFIX_TGZ, ARCHIVE_SUFFIX_TAR_GZ, ARCHIVE_SUFFIX_NAR));

    public static SpecFileResourceType getResourceTypeBySuffix(String fileName) {
        int dotIndex = StringUtils.defaultString(fileName).lastIndexOf('.');
        if (dotIndex == -1) {
            return SpecFileResourceType.FILE;
        }
        String suffix = fileName.substring(dotIndex).toLowerCase();
        if (ARCHIVE_SUFFIX.contains(suffix) || StringUtils.defaultString(fileName).endsWith(ARCHIVE_SUFFIX_TAR_GZ)) {
            return SpecFileResourceType.ARCHIVE;
        }
        switch (suffix) {
            case PYTHON_SUFFIX:
                return SpecFileResourceType.PYTHON;
            case JAR_SUFFIX:
                return SpecFileResourceType.JAR;
            default:
                return SpecFileResourceType.FILE;
        }
    }
}