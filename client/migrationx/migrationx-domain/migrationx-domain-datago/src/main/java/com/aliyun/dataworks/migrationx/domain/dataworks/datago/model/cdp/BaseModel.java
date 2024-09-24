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

package com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp;

import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.enums.CDPTypeEnum;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.enums.TypeofEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description
 * @createTime 2020-04-16
 */
@Data
public class BaseModel implements Serializable {
    private static final long serialVersionUID = 8276028502892711987L;
    private TypeofEnum typedef;
    private String datasourceName;
    private CDPTypeEnum resourceType;
    private List<Column> columns;

    @Data
    public static class Column {
        private static final long serialVersionUID = 8370493517602316900L;
        /**
         * 字段index
         */
        private int index;
        /**
         * 字段名称
         */
        private String name;
        /**
         * 字段类型
         */
        private String type;
        /**
         * 字段描述
         */
        private String description;
        /**
         * 是否主健
         */
        private Boolean primaryKey;
        /**
         * 样例数据
         */
        private String sample;
        /**
         * 列簇
         */
        private String columnFamily;
        /**
         * 是否选中
         */
        private boolean checked;
        /**
         * 是否可编辑
         */
        private boolean editable;
    }
}
