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

public enum CycleType {
    DAY(0),
    NOT_DAY(1),
    NOT_DAY_SEQ(2);

    private int code;

    private CycleType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static CycleType getCycleTypeByCode(int code) {
        CycleType[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            CycleType type = var1[var3];
            if (type.getCode() == code) {
                return type;
            }
        }

        throw new IllegalArgumentException(String.format("unknown code %d for CycleType", code));
    }
}
