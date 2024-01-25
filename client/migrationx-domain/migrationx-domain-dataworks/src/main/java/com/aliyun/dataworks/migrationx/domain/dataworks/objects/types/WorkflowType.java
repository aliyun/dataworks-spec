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
 * @date 2020/03/09
 */
public enum WorkflowType {
    /**
     * 业务流程
     */
    BUSINESS(0),
    /**
     * 手动业务流程
     */
    MANUAL_BUSINESS(1),
    /**
     * 旧版工作流
     */
    OLD_WORKFLOW(2);


    private Integer value;

    WorkflowType(Integer value) {
        this.value = value;
    }

    public static WorkflowType getWorkflowTypeByValue(Integer typeValue) {
        for (WorkflowType type : values()) {
            if (type.getValue().equals(typeValue)) {
                return type;
            }
        }
        throw new RuntimeException("unknown workflow type value: " + typeValue);
    }

    public Integer getValue() {
        return value;
    }

}