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

package com.aliyun.dataworks.common.spec.domain.noref;

import java.util.List;

import com.aliyun.dataworks.common.spec.domain.SpecNoRefEntity;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yiwei.qyw
 * @date 2023/7/17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpecForEach extends SpecNoRefEntity {
    private List<SpecNode> nodes;
    private SpecVariable array;
    private List<SpecFlowDepend> flow;
    /**
     * 最大迭代次数
     */
    private Integer maxIterations;
}