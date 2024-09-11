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

package com.aliyun.dataworks.common.spec.domain.enums;

import com.aliyun.dataworks.common.spec.domain.interfaces.LabelEnum;
import com.google.common.base.Joiner;
import lombok.Getter;

/**
 * @author 聿剑
 * @date 2023/11/16
 */
@Getter
public enum SpecVersion implements LabelEnum {
    /**
     * v1.0.0
     */
    V_1_0_0("1", "0", "0"),
    /**
     * v1.1.0
     */
    V_1_1_0("1", "1", "0"),
    /**
     * v1.2.0
     */
    V_1_2_0("1", "2", "0");
    private final String major;
    private final String minor;
    private final String patch;

    SpecVersion(String major, String minor, String patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    @Override
    public String toString() {
        return Joiner.on(".").join(major, minor, patch);
    }

    @Override
    public String getLabel() {
        return toString();
    }
}
