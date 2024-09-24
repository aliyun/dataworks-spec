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

package com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian;

import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import org.apache.commons.lang3.StringUtils;

/**
 * @author sam.liux
 * @date 2021/01/07
 */
public enum DgDatasourceType {
    ftp,

    sftp,

    oceanbase,

    mysql,

    odps;

    public static DgDatasourceType getDgDatasourceType(String type) {
        if (StringUtils.isBlank(type)) {
            return null;
        }

        for (DgDatasourceType dgDatasourceType : values()) {
            if (dgDatasourceType.name().equalsIgnoreCase(type)) {
                return dgDatasourceType;
            }
        }

        throw BizException.of(ErrorCode.UNKNOWN_ENUM_TYPE).with(DgDatasourceType.class, type);
    }
}
