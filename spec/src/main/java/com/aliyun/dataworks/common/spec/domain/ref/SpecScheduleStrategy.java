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

import com.aliyun.dataworks.common.spec.domain.enums.FailureStrategy;
import com.aliyun.dataworks.common.spec.domain.enums.NodeInstanceModeType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRerunModeType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Schedule strategy
 *
 * @author 聿剑
 * @date 2024/7/8
 */
@Data
@EqualsAndHashCode
public class SpecScheduleStrategy {
    private Integer priority;

    private Integer timeout;

    private NodeInstanceModeType instanceMode;

    private NodeRerunModeType rerunMode;

    private Integer rerunTimes;

    private Integer rerunInterval;

    /**
     * 是否忽略分支条件跳过
     */
    private Boolean ignoreBranchConditionSkip;

    /**
     * 失败策略
     */
    private FailureStrategy failureStrategy;
}
