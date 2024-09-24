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

package com.aliyun.dataworks.migrationx.domain.dataworks.standard.service;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 聿剑
 * @date 2023/01/12
 */
@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode
public class PaginDTO {
    private Map<String, Object> conditionsMap = new HashMap<>();
    private Integer pageSize = 18;
    private Integer toPage = 1;
    private String order;
    private String orderType;

    public int getFromRow() {
        return this.getToPage() * this.getPageSize() - this.getPageSize();
    }

    public void addCondition(String key, Object value) {
        this.conditionsMap.put(key, value);
    }
}
