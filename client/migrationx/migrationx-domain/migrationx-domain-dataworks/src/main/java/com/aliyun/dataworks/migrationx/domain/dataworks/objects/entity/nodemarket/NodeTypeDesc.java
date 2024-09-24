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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.nodemarket;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author 聿剑
 * @date 2024/6/17
 */
@Data
@ToString
@EqualsAndHashCode
public class NodeTypeDesc {
    private Integer id;
    private String name;
    private String shortName;
    private Integer level;
    private String appName;
    private List<String> moduleName;
    private String categoryName;
    private String icon;
    private String permissionCode;
    private String label;
    private List<Integer> resGroupTypes;
    private String codeTemplate;
    private String developTips;
    private boolean enableAdvance;
    private String advanceSettings;
    private boolean supportAssignParam;
    private boolean supportSession;
    private boolean display = true;
    private Boolean supportConfigCU;
    private BigDecimal defaultCU;
    private BigDecimal cuGradient;
    private Integer measureType;
    private Boolean supportCustomImage;
}