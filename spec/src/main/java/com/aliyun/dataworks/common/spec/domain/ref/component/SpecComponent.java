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

package com.aliyun.dataworks.common.spec.domain.ref.component;

import java.util.List;

import com.alibaba.fastjson2.annotation.JSONField;

import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Component
 *
 * @author 聿剑
 * @date 2024/6/7
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class SpecComponent extends SpecRefEntity {
    private String name;
    private String owner;
    private String description;
    private SpecScript script;
    @JSONField(name = "inputs", alternateNames = {"input"})
    @com.alibaba.fastjson.annotation.JSONField(name = "inputs", alternateNames = {"input"})
    @SerializedName(value = "inputs", alternate = "input")
    private List<SpecComponentParameter> inputs;
    @JSONField(name = "outputs", alternateNames = {"output"})
    @com.alibaba.fastjson.annotation.JSONField(name = "outputs", alternateNames = {"output"})
    @SerializedName(value = "outputs", alternate = "output")
    private List<SpecComponentParameter> outputs;
}
