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

package com.aliyun.dataworks.common.spec.domain.interfaces;

/**
 * Get enumeration values based on label values
 *
 * @author yiwei.qyw
 * @date 2023/7/7
 */
public interface LabelEnum {

    /**
     * get label value
     *
     * @return String type
     */
    String getLabel();

    /**
     * Get enumeration values based on label values and enumeration implementation classes
     *
     * @param enumClass The enumeration class that implements this interface
     * @param label     Label string value
     * @param <T>       Enum value
     * @return Enum value or null
     */
    @SuppressWarnings("unchecked")
    static <T extends Enum<T> & LabelEnum> T getByLabel(Class<? extends LabelEnum> enumClass, String label) {
        for (LabelEnum t : enumClass.getEnumConstants()) {
            if (t.getLabel().equalsIgnoreCase(label)) {
                return (T)t;
            }
        }
        return null;
    }
}
