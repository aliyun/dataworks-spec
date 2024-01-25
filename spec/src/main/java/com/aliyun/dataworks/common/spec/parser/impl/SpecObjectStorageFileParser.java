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

import java.util.Map;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.annotation.SpecParser;
import com.aliyun.dataworks.common.spec.domain.SpecConstants;
import com.aliyun.dataworks.common.spec.domain.ref.file.SpecObjectStorageFile;
import com.aliyun.dataworks.common.spec.domain.ref.storage.SpecStorage;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

/**
 * SpecFileResource parser
 *
 * @author 聿剑
 * @date 2023/11/30
 */
@Slf4j
@SpecParser
public class SpecObjectStorageFileParser implements Parser<SpecObjectStorageFile> {
    @Override
    public SpecObjectStorageFile parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        Map<?, ?> storageTypeStr = MapUtils.getMap(rawContext, SpecConstants.SPEC_KEY_STORAGE);
        SpecStorage storage = (SpecStorage)SpecDevUtil.getObjectByParser(SpecStorage.class, storageTypeStr, specParserContext);
        return Optional.ofNullable(storage)
            .map(st -> {
                SpecObjectStorageFile file = SpecObjectStorageFile.newInstanceOf(st.getType());
                file.setStorage(st);
                SpecDevUtil.setSimpleField(rawContext, file);
                SpecDevUtil.setEntityToCtx(file, specParserContext);
                return file;
            }).orElse(null);
    }
}
