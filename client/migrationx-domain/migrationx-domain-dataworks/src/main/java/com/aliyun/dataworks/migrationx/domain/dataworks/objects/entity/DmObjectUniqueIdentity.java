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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 聿剑
 * @date 2021/12/01
 */
@Data
@ToString
@Accessors(chain = true)
public class DmObjectUniqueIdentity {
    private Map<String, String> fields = new HashMap<>();

    public String getField(String fieldName) {
        if (MapUtils.isEmpty(fields)) {
            return null;
        }

        return fields.get(fieldName);
    }

    public DmObjectUniqueIdentity setField(String fieldName, String fieldValue) {
        if (fields == null) {
            fields = new HashMap<>();
        }

        fields.put(fieldName, fieldValue);
        return this;
    }

    public static DmObjectUniqueIdentity ofLong(String fieldName, Long value) {
        return new DmObjectUniqueIdentity().setField(fieldName, String.valueOf(value));
    }
}
