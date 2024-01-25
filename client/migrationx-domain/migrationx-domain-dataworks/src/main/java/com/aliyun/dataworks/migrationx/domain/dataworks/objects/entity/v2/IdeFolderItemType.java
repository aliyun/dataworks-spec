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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2;

/**
 * @author sam.liux
 * @date 2020/02/04
 */
public enum IdeFolderItemType {
    // 表目录
    TABLE(1, "TABLE"),
    // 代码目录
    CODE(2, "CODE"),
    // 临时查询目录b
    QUERY(3, "QUERY"),
    // 资源文件目录
    RESOURCE(4, "RESOURCE"),
    // 手工任务目录
    MANUAL_TASK(5, "MANUAL_TASK"),
    // 函数目录
    FUNCTION(6, "FUNCTION"),
    // 回收站目录
    RECYCLE(7, "RECYCLE"),
    // 表目录的根目录
    TABLE_ROOT(8, "表"),
    // 数据流设计的根目录
    DATAFLOW_ROOT(9, "数据流"),
    // 数据流设计的目录
    DATA_FLOW(10, "dataflow"),
    // 流程节点的目录
    GROUP(11, "GROUP"),
    // 实时计算
    REALTIME(12, "REALTIME"),
    // 计划任务
    DATA_STUDIO_PLANTASK(13, "DATA_STUDIO_PLANTASK"),
    DATA_STUDIO_QUERY(14, "DATA_STUDIO_QUERY"),
    INDEX(20, "INDEX"),
    // 手工业务流程目录
    MANUAL_BIZ(21, "MANUAL_TASK"),
    // 组件管理
    COMPONENT(22, "COMPONENT"),
    ;
    private Integer code;
    private String alias;

    private IdeFolderItemType(Integer code, String alias) {
        this.code = code;
        this.alias = alias;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getAlias() {
        return this.alias;
    }

    /**
     * 通过code来获取枚举对象
     *
     * @param code
     * @return
     */
    public static IdeFolderItemType getByCode(Integer code) {
        for (IdeFolderItemType type : IdeFolderItemType.values()) {
            if (code.equals(type.getCode())) {
                return type;
            }
        }
        return null;
    }

    public static IdeFolderItemType getByAlias(String alias) {
        for (IdeFolderItemType type : IdeFolderItemType.values()) {
            if (alias.equals(type.getAlias())) {
                return type;
            }
        }
        return null;
    }

    public static IdeFolderItemType getByUseType(Integer useType) {
        switch (useType) {
            case 0:
            case 3:
                return IdeFolderItemType.CODE;
            case 1:
                return IdeFolderItemType.MANUAL_TASK;
            case 2:
                return IdeFolderItemType.MANUAL_BIZ;
            case 8:
                return IdeFolderItemType.DATA_FLOW;
            case 10:
                return IdeFolderItemType.QUERY;
            case 12:
                return IdeFolderItemType.DATA_STUDIO_QUERY;
            case 13:
                return IdeFolderItemType.DATA_STUDIO_PLANTASK;
            case 30:
                return IdeFolderItemType.COMPONENT;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return code.toString();
    }
}
