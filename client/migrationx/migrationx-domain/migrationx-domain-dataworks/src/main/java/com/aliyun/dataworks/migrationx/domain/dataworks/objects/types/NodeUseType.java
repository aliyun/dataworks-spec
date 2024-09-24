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
 * @date 2020/01/03
 */
public enum  NodeUseType {
    /**
     * 周期调度
     */
    SCHEDULED(0),

    /**
     * 手动任务
     */
    MANUAL(1),

    /**
     * 手动业务流程
     */
    MANUAL_WORKFLOW(2),

    /**
     * 空跑任务
     */
    SKIP(3),

    /**
     * 数据流
      */
    DATAFLOW(8),

    /**
     *  实时计算
     */
    REALTIME(11),
    /**
     *  数据圈圈的临时查询
     */
    DATA_STUDIO_QUERY(12),
    /**
     * 数据圈圈的计划任务
     */
    DATA_STUDIO_PLAN(13),
    /**
     * 指标系统
     */
    INDEXSYSTEM(20),

    COMPONENT(30),
    /**
     * 临时查询
     */
    AD_HOC(10),
    /**
     * Temporary file
     */
    TEMP_FILE(99);

    int value;

    NodeUseType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static NodeUseType getNodeUseTypeByValue(int value) {
        for (NodeUseType useType : values()) {
            if (useType.getValue() == value) {
                return useType;
            }
        }

        throw new RuntimeException("unknown node use type value: " + value);
    }

    public static Boolean isScheduled(int value) {
        if (SCHEDULED.getValue() == value) {
            return true;
        }
        return false;
    }
}
