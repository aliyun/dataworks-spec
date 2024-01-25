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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.types;

/**
 * 导出版本号
 * @author sam.liux
 * @date 2019/11/11
 */
public enum DmMajorVersion {
    /**
     * V1
     */
    DATAWORKS_V1(1),

    /**
     * V2
     */
    DATAWORKS_V2(2),

    /**
     * 公共云最新版本定为V3
     */
    DATAWORKS_V3(3);

    private int value;

    DmMajorVersion(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DmMajorVersion getMigrationVersionByValue(int value) {
        for (DmMajorVersion version : values()) {
            if (version.getValue() == value) {
                return version;
            }
        }
        throw new RuntimeException("unknown migration version value: " + value);
    }
}