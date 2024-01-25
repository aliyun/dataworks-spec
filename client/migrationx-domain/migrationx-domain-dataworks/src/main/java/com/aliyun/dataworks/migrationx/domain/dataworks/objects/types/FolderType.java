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
 * 文件夹类型
 *
 * @author sam.liux
 * @date 2020/01/07
 */
public enum FolderType {
    /**
     *
     */
    NORMAL(-1, "normal", "", ""),

    /**
     * 业务流程
     */
    BUSINESS(0, "business", "", ""),

    /**
     * 数据同步
     */
    DATA_SYNC(1, "data_sync", "数据集成", "folderDi"),

    /**
     * 数据开发
     */
    DATA_PROCESS(2, "data_process", "数据开发", "folderProcess"),

    /**
     * 表
     */
    TABLE(3, "table", "表", "folderTable"),

    /**
     * 函数
     */
    FUNCTION(5, "function", "函数", "folderFunction"),

    /**
     * 资源
     */
    RESOURCE(4, "resource", "资源", "folderResource"),

    /**
     * 算法
     */
    ALGORITHM(6, "algorithm", "算法", "folderAlgm"),

    /**
     * 数据流
     */
    DATA_FLOW(7, "dataflow", "数据流", "folderWorkflow"),

    /**
     * 数据服务
     */
    DATA_SERVICE(8, "dataservice", "数据服务", "folderService"),

    /**
     * 控制
     */
    CONTROL_FLOW(9, "control", "控制", "folderControl"),

    /**
     * 计算引擎
      */
    ENGINE_TYPE(10, "engine_type", "", "");

    int code;
    String name;
    String nameV2;
    String key;

    FolderType(int code, String name, String nameV2, String key) {
        this.code = code;
        this.name = name;
        this.key = key;
        this.nameV2 = nameV2;
    }

    public String getKey() {
        return key;
    }

    public int getCode() {
        return code;
    }

    public String getNameV2() {
        return nameV2;
    }

    public String getName() {
        return name;
    }

    public static FolderType getFolderTypeByCode(int code) {
        for (FolderType folderType : values()) {
            if (folderType.getCode() == code) {
                return folderType;
            }
        }
        throw new RuntimeException("unknown folder type code: " + code);
    }
}
