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

import com.aliyun.dataworks.common.spec.annotation.SpecParser;
import com.aliyun.dataworks.common.spec.domain.SpecConstants;
import com.aliyun.dataworks.common.spec.domain.enums.SpecStorageType;
import com.aliyun.dataworks.common.spec.domain.interfaces.LabelEnum;
import com.aliyun.dataworks.common.spec.domain.ref.storage.SpecStorage;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SpecStorageParser
 *
 * @author 聿剑
 * @date 2023/11/30
 * @see com.aliyun.dataworks.common.spec.domain.ref.storage.SpecOssStorage
 * @see com.aliyun.dataworks.common.spec.domain.ref.storage.SpecHdfsStorage
 * @see SpecStorage
 */
@SpecParser
public class SpecStorageParser implements Parser<SpecStorage> {
    private static final Logger log = LoggerFactory.getLogger(SpecStorageParser.class);

    @Override
    public SpecStorage parse(Map<String, Object> rawContext, SpecParserContext specParserContext) {
        String storageTypeStr = MapUtils.getString(rawContext, SpecConstants.SPEC_KEY_TYPE);

        SpecStorageType storageType = LabelEnum.getByLabel(SpecStorageType.class, storageTypeStr);
        if (storageType == null) {
            SpecException ex = new SpecException(SpecErrorCode.PARSE_ERROR, "storage type " + storageTypeStr + " is not supported");
            log.warn("ignore parse storage error: {}", ex.getMessage());
            return null;
        }

        SpecStorage storage = SpecStorage.of(storageType);
        SpecDevUtil.setSimpleField(rawContext, storage);
        SpecDevUtil.setEntityToCtx(storage, specParserContext);
        return storage;
    }
}
