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

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.connection.AbstractConnection;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.connection.FtpConnection;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.connection.JdbcConnection;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * @author sam.liux
 * @date 2020/08/19
 */
@ToString
@Data
@Accessors(chain = true)
public class DgDatasource {
    private Long id;
    private String name;
    private String type;
    private String rootPath;
    private String host;
    private String port;
    private String username;
    private String password;
    private String database;

    public AbstractConnection toConnection() {
        if (StringUtils.isBlank(type)) {
            return new AbstractConnection();
        }

        DgDatasourceType dgDatasourceType = DgDatasourceType.getDgDatasourceType(type);
        if (dgDatasourceType == null) {
            return new AbstractConnection();
        }

        AbstractConnection connection;
        switch (dgDatasourceType) {
            case ftp:
            case sftp:
                connection = new FtpConnection()
                    .setHost(host)
                    .setPassword(password)
                    .setPort(port)
                    .setProtocol(type)
                    .setUsername(username)
                    .setRootPath(rootPath);
                break;
            case mysql:
            case oceanbase:
                connection = new JdbcConnection()
                    .setJdbcUrl(
                        "jdbc:" + type + "://" + host + ":" + port + "/" + StringUtils.defaultIfBlank(database, name))
                    .setUsername(username)
                    .setPassword(password);
                break;
            default:
                connection = new AbstractConnection();
                break;
        }
        connection.setConfigType(1);
        connection.setTag("public");
        return connection;
    }
}
