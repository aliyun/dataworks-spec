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

package com.aliyun.dataworks.migrationx.domain.dataworks.datago.enums;

import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.ReaderModel.OceanBaseReaderModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.ReaderModel.OdpsReaderModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.ReaderModel.OssReaderModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.ReaderModel.RdbmsReaderModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.ReaderModel.UnstructuredFileReaderModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.WriterModel.OceanBaseWriterModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.WriterModel.OdpsWriterModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.WriterModel.OssWriterModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.WriterModel.RdbmsWriterModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.WriterModel.UnstructuredFileWriterModel;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description
 * @createTime 2020-04-07
 */
public enum TypeofEnum {
    /**
     * Mysql Reader
     */
    MYSQL_READER(RdbmsReaderModel.class),
    /**
     * Oracle Reader
     */
    ORACLE_READER(RdbmsReaderModel.class),
    /**
     * ODPS Reader
     */
    ODPS_READER(OdpsReaderModel.class),
    /**
     * OSS Reader
     */
    OSS_READER(OssReaderModel.class),

    /**
     * SFTP Reader
     */
    SFTP_READER(UnstructuredFileReaderModel.class),

    /**
     * OceanBase 1.x Reader
     */
    OCEAN_BASE_V1_0_READER(OceanBaseReaderModel.class),

    /**
     * ODPS类型
     */
    ODPS_WRITER(OdpsWriterModel.class),
    /**
     * mysql类型
     */
    MYSQL_WRITER(RdbmsWriterModel.class),
    /**
     * oracle类型
     */
    ORACLE_WRITER(RdbmsWriterModel.class),
    /**
     * OSS Writer
     */
    OSS_WRITER(OssWriterModel.class),
    /**
     * SFTP Writer
     */
    SFTP_WRITER(UnstructuredFileWriterModel.class),
    /**
     * OceanBase 1.x
     */
    OCEAN_BASE_V1_0_WRITER(OceanBaseWriterModel.class);

    private Class type;

    TypeofEnum(Class type) {
        this.type = type;
    }

    public Class getTypedef() {
        return type;
    }
}
