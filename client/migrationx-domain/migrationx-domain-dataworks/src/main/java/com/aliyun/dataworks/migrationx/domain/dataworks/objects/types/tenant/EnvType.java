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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.tenant;

import com.aliyun.dataworks.common.spec.domain.dw.types.IntEnum;

/**
 * @author 聿剑
 * @date 2023/01/12
 */
public enum EnvType implements IntEnum<EnvType> {
    /**
     * DEV
     */
    DEV(0),
    /**
     * PRD
     */
    PRD(1);

    private final int value;

    private EnvType(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static EnvType from(int value) {
        for (EnvType envType : EnvType.values()) {
            if (value == envType.value) {
                return envType;
            }
        }

        return null;
    }
}
