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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.conditions;

import java.util.List;

public class ConditionResult {
    private List<Long> successNode;
    private List<Long> failedNode;

    public List<Long> getSuccessNode() {
        return successNode;
    }

    public void setSuccessNode(List<Long> successNode) {
        this.successNode = successNode;
    }

    public List<Long> getFailedNode() {
        return failedNode;
    }

    public void setFailedNode(List<Long> failedNode) {
        this.failedNode = failedNode;
    }
}
