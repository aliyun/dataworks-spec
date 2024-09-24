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
 * 迁移对象类型
 *
 * @author sam.liux
 * @date 2019/11/11
 */
public enum DmObjectType {
    /**
     * 迁移包
     */
    // PACKAGE(0),

    /**
     * 工作空间
     */
    PROJECT(1),
    /**
     * 解决方案
     */
    SOLUTION(2),
    /**
     * 工作流
     */
    WORKFLOW(3),
    /**
     * 节点
     */
    NODE(4),
    /**
     * 资源
     */
    RESOURCE(5),
    /**
     * 函数
     */
    FUNCTION(6),
    /**
     * 表
     */
    TABLE(7),
    /**
     * 数据源
     */
    DATASOURCE(8),

    /**
     * 输入输出
     */
    NODE_IO(9),

    /**
     * 用户自定义节点
     */
    USER_DEFINED_NODE(10),

    /**
     * 文件夹
     */
    FOLDER(11),

    /**
     * 数据服务业务流程
     */
    DS_GROUP(12),

    /**
     * data service API
     */
    DS_API(13),

    /**
     * Data Service 服务编排
     */
    DS_ORCHESTRATION(14),

    /**
     * Data Service 函数
     */
    DS_FUNCTION(15),

    /**
     * DQC Rule
     */
    DQC_RULE(16);

    Integer value;

    DmObjectType(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static DmObjectType getObjectTypeByValue(Integer value) {
        for (DmObjectType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new RuntimeException("unknown object type value: " + value);
    }
}
