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

package com.aliyun.dataworks.common.spec.domain.ref.file;

import java.util.Objects;
import java.util.Set;

import com.aliyun.dataworks.common.spec.domain.enums.SpecStorageType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFile;
import com.aliyun.dataworks.common.spec.domain.ref.storage.SpecStorage;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.SetUtils;
import org.reflections.Reflections;

/**
 * Object storage file
 *
 * @author 聿剑
 * @date 2023/11/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SpecObjectStorageFile extends SpecFile {
    private static final Set<Class<? extends SpecObjectStorageFile>> subClz;

    static {
        Reflections reflections = new Reflections(SpecObjectStorageFile.class.getPackage().getName());
        subClz = reflections.getSubTypesOf(SpecObjectStorageFile.class);
    }

    private SpecStorage storage;

    public static SpecObjectStorageFile newInstanceOf(SpecStorageType storageType) {
        if (storageType == null) {
            return null;
        }

        return SetUtils.emptyIfNull(subClz).stream().map(clz -> {
                try {
                    return clz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            })
            .filter(clz -> Objects.equals(storageType, clz.getStorage().getType()))
            .findFirst().orElseThrow(() -> new SpecException(SpecErrorCode.PARSE_ERROR, "storage type not supported: " + storageType));
    }
}
