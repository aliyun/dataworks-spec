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

package com.aliyun.dataworks.common.spec.domain.ref;

import com.alibaba.fastjson2.annotation.JSONField;

import com.aliyun.dataworks.common.spec.domain.enums.ArtifactType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString.Exclude;

/**
 * @author yiwei.qyw
 * @date 2023/7/4
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpecVariable extends SpecArtifact {
    public SpecVariable() {
        setArtifactType(ArtifactType.VARIABLE);
    }

    private String name;
    private VariableScopeType scope;
    private VariableType type;
    private String value;
    private String description;
    @Exclude
    @JSONField(serialize = false)
    private SpecVariable referenceVariable;
    @Exclude
    @JSONField(serialize = false)
    private SpecDepend node;
}