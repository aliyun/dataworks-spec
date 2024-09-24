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

import lombok.Data;

import java.util.List;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description
 * @createTime 2020-04-16
 */
@Data
public class ReaderModel {

    /**
     * RDBMS关系型数据库（mysql,oracle,oceanbase）
     */
    @Data
    public static class RdbmsReaderModel extends BaseModel {
        private static final long serialVersionUID = 7838118146445792879L;
        /**
         * 筛选Sql
         */
        private String filterSql;

        /**
         * 表名
         */
        private String tableName;
        /**
         * 表的唯一标识符
         */
        private String guid;
        /**
         * 切分键
         */
        private String splitPk;
        /**
         * 编码
         */
        private String encoding;

    }

    @Data
    public static class OceanBaseReaderModel extends RdbmsReaderModel {

        private static final long serialVersionUID = -1485357909354118492L;
        /**
         * OB1.0 分区表按分区读
         */
        private Boolean readByPartition;
    }

    /**
     * ODPS
     */
    @Data
    public static class OdpsReaderModel extends BaseModel {
        private static final long serialVersionUID = -1209729000266241030L;
        /**
         * 分区名
         */
        private String pt;
        /**
         * 忽略null记录
         */
        private Boolean skipNull = false;
        /**
         * pre Filter
         */
        private String preFilterSql;
        /**
         * post filter
         */
        private String postFilterSql;

        /**
         * 表名
         */
        private String tableName;

        private String guid;
    }

    /**
     * OSS
     */
    @Data
    public static class OssReaderModel extends UnstructuredFileReaderModel {

        private static final long serialVersionUID = 4109626537825146404L;
        /**
         * 当指定单个OSS Object，OSSReader暂时只能使用单线程进行数据抽取。 当指定多个OSS Object，OSSReader支持使用多线程进行数据抽取。线程并发数通过通道数指定。
         * 当指定通配符，OSSReader尝试遍历出多个Object信息。例如: 指定/*代表读取bucket下游所有的Object，指定/bazhen/*代表读取bazhen目录下游所有的Object。
         */
        private List<String> object;

    }

    /**
     * 非结构化数据
     */
    @Data
    public static class UnstructuredFileReaderModel extends BaseModel {

        private static final long serialVersionUID = 2598659840614125526L;
        /**
         * 描述：读取的字段分隔符, 需要是 ascii 码值的 String 必选：否 默认值：Datax 插件为准，一般是,
         */
        private String fieldDelimiter;
        /**
         * 描述：读取文件的编码配置。 必选：否 默认值：Datax 插件为准，一般是utf-8
         */
        private String encoding;

        /**
         * 描述：文本文件中无法使用标准字符串定义null(空指针)，CDP提供nullFormat定义哪些字符串可以表示为null。 例如如果用户配置:
         * nullFormat="\N"，那么如果源头数据是"\N"，CDP视作null字段。 必选：否 默认值：Datax 插件为准，\N
         */
        private String nullFormat;
        /**
         * 是否跳过表头
         */
        private boolean skipHeader;
        /**
         * 检测文件
         */
        private String checkFile;
        /**
         * 列模板
         */
        private String columnTemplate;
        /**
         * 压缩
         */
        private String compress;
        /**
         * 描述：文件写出的格式，包括csv 和text两种， csv是严格的csv格式，如果待写数据包括列分隔符，则会按照csv的转义语法转义，转义符号为双引号"；
         * text格式是用列分隔符简单分割待写数据，对于待写数据包括列分隔符情况下不做转义。 必选：否 默认值：以Datax插件为准，一般为text
         */
        private String fileFormat = "text";

    }
}
