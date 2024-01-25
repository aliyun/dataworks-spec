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

package com.aliyun.dataworks.common.spec.parser.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aliyun.dataworks.common.spec.annotation.SpecParser;
import com.aliyun.dataworks.common.spec.domain.SpecConstants;
import com.aliyun.dataworks.common.spec.domain.enums.SpecStorageType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFile;
import com.aliyun.dataworks.common.spec.domain.ref.file.SpecHdfsFile;
import com.aliyun.dataworks.common.spec.domain.ref.file.SpecObjectStorageFile;
import com.aliyun.dataworks.common.spec.domain.ref.file.SpecOssFile;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.MapKeyMatchUtils;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yiwei
 */
@SpecParser
public class FileListParser implements Parser<List<SpecFile>> {
    public static final String KEY_TYPE = "files";

    @SuppressWarnings("unchecked")
    @Override
    public List<SpecFile> parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        ArrayList<SpecFile> specFiles = new ArrayList<>();
        List<Map<String, Object>> ossFiles = (List<Map<String, Object>>)MapKeyMatchUtils.getValue(rawContext,
            StringUtils::equalsIgnoreCase, SpecStorageType.OSS.getLabel());
        if (CollectionUtils.isNotEmpty(ossFiles)) {
            for (Map<String, Object> file : ossFiles) {
                Map<String, Object> storage;
                if (!file.containsKey(SpecConstants.SPEC_KEY_STORAGE)) {
                    storage = new LinkedHashMap<>();
                } else {
                    storage = (Map<String, Object>)file.get(SpecConstants.SPEC_KEY_STORAGE);
                }
                storage.put(SpecConstants.SPEC_KEY_TYPE, SpecStorageType.OSS.getLabel());
                file.put(SpecConstants.SPEC_KEY_STORAGE, storage);
                SpecOssFile specOssFile = (SpecOssFile)SpecDevUtil.getObjectByParser(SpecObjectStorageFile.class, file, specParserContext);
                specFiles.add(specOssFile);
            }
        }

        List<Map<String, Object>> hdfsFiles = (List<Map<String, Object>>)MapKeyMatchUtils.getValue(rawContext,
            StringUtils::equalsIgnoreCase, SpecStorageType.HDFS.getLabel());
        if (CollectionUtils.isNotEmpty(hdfsFiles)) {
            for (Map<String, Object> output : hdfsFiles) {
                Map<String, Object> storage;
                if (!output.containsKey(SpecConstants.SPEC_KEY_STORAGE)) {
                    storage = new LinkedHashMap<>();
                } else {
                    storage = (Map<String, Object>)output.get(SpecConstants.SPEC_KEY_STORAGE);
                }
                storage.put(SpecConstants.SPEC_KEY_TYPE, SpecStorageType.HDFS.getLabel());
                output.put(SpecConstants.SPEC_KEY_STORAGE, storage);
                SpecHdfsFile hdfsFile = (SpecHdfsFile)SpecDevUtil.getObjectByParser(SpecObjectStorageFile.class, output, specParserContext);
                specFiles.add(hdfsFile);
            }
        }
        return specFiles;
    }

    @Override
    public String getKeyType() {
        return KEY_TYPE;
    }
}
