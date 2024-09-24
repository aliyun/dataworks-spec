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
public class WriterModel {

    @Data
    public static class OceanBaseWriterModel extends RdbmsWriterModel {

        private static final long serialVersionUID = -4792292467149797792L;
        /**
         * 写入模式
         */
        private String writeMode;
    }

    @Data
    public static class RdbmsWriterModel extends BaseModel {
        private static final long serialVersionUID = -8278125532416441763L;
        /**
         * 表名
         */
        private String tableName;
        /**
         * guid
         */
        private String guid;
        /**
         * 表描述信息
         */
        private String desc;
        /**
         * 写入前置
         */
        private List<String> preSql;
        /**
         * 写入后置
         */
        private List<String> postSql;
        /**
         * 编码
         */
        private String encoding;

    }

    @Data
    public static class OdpsWriterModel extends BaseModel {
        private static final long serialVersionUID = -8838732722560966994L;
        /**
         * 表名
         */
        private String tableName;
        /**
         * 分区值（原始传入值）
         */
        private String pt;

        /**
         * 分区字段名
         */
        private String[] ptKey;

        private String guid;
        private String desc;

        /**
         * /** 是否truncate表
         */
        private Boolean truncate;
        /**
         * 目标表的生命周期
         */
        private Integer lifeCycle;

        public String getDeltaTableName() {
            return "done_delta_" + tableName;
        }
    }

    @Data
    public static class OssWriterModel extends UnstructuredFileWriterModel {

        private static final long serialVersionUID = 5669199249092763088L;
        /**
         * 描述：OSSWriter写入的文件名，OSS使用文件名模拟目录的实现，请注意OSS对于object名字的限制。 使用"object": "datax"，写入object以datax开头，后缀添加随机字符串。
         * 使用"object": "cdo/datax"，写入的object以/cdo/datax开头，后缀随机添加字符串，/作为OSS模拟目录的分隔符。 必选: 是。
         */
        protected String object;

        /**
         * 描述：Oss写出时单个objectName文件的最大大小，默认为10000*10MB，类似log4j日志打印时根据日志文件大小轮转。OSS分块上传时，每个分块大小为10MB（也是日志轮转文件最小粒度，即小于10MB
         * 的maxFileSize会被作为10MB），每个OSS
         * InitiateMultipartUploadRequest支持的分块最大数量为10000。轮转发生时，objectName名字规则是：在原有objectName前缀加UUID随机数的基础上，拼接_1,_2,
         * _3等后缀。
         * 必选：否 默认值：100000MB
         */
        protected Long maxFileSize = -1L;

        /**
         * 描述：oss 标档文件的名称 必选：否
         */
        protected String markDoneFileName;
    }

    @Data
    public static class UnstructuredFileWriterModel extends BaseModel {

        private static final long serialVersionUID = 5618180626348780907L;
        /**
         * 描述：读取的字段分隔符 必选：否 默认值：以Datax插件为准，一般为','
         */
        private String fieldDelimiter = ",";

        /**
         * 描述：写入前数据清理处理模式： truncate，写入前清理目录下一fileName前缀的所有文件。 append，写入前不做任何处理，CDP FtpWriter直接使用filename写入，并保证文件名不冲突。
         * nonConflict，如果目录下有fileName前缀的文件，直接报错。 必选：是 默认值：无
         */
        private String writeMode = "truncate";

        /**
         * 描述：文件的编码配置。 必选：否 默认值：以Datax插件为准，一般为utf-8
         */
        private String encoding;

        /**
         * 描述：文本文件中无法使用标准字符串定义null(空指针)，CDP提供nullFormat定义哪些字符串可以表示为null。 例如如果用户配置:
         * nullFormat="\N"，那么如果源头数据是"\N"，CDP视作null字段。 必选：否 默认值：以Datax插件为准，一般为\N
         */
        private String nullFormat;

        /**
         * 描述：文件写出的格式，包括csv 和text两种， csv是严格的csv格式，如果待写数据包括列分隔符，则会按照csv的转义语法转义，转义符号为双引号"；
         * text格式是用列分隔符简单分割待写数据，对于待写数据包括列分隔符情况下不做转义。 必选：否 默认值：以Datax插件为准，一般为text
         */
        private String fileFormat = "text";

        /**
         * 描述：日期类型的数据序列化到文件中时的格式，例如 "dateFormat": "yyyy-MM-dd"。 必选：否 默认值：无
         */
        private String dateFormat;

        /**
         * 描述：txt写出时的表头，示例['id', 'name', 'age']。 必选：否 默认值：无
         */
        private List<String> header;
    }

}
