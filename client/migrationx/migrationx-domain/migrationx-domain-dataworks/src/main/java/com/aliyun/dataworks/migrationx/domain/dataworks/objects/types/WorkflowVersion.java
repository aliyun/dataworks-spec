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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.types;

/**
 * @author sam.liux
 * @date 2020/01/02
 */
public enum  WorkflowVersion {
    /**
     * V1 <=3.6.0
     */
    V1(-1),

    /**
     * V2 >=3.6.1
     */
    V2(0),

    /**
     * V3
     */
    V3(1);

    private Integer value;

    WorkflowVersion(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static WorkflowVersion getByValue(Integer version) {
        if (version == null) {
            return null;
        }

        for (WorkflowVersion workflowVersion : values()) {
            if (workflowVersion.getValue().equals(version)) {
                return workflowVersion;
            }
        }
        throw new RuntimeException("unknown workflow version: " + version);
    }
}
