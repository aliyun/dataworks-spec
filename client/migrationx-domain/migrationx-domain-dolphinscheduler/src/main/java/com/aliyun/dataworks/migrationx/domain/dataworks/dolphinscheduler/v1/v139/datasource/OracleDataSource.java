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
package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.datasource;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.enums.DbConnectType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.enums.DbType;

/**
 * data source of Oracle
 */
public class OracleDataSource extends BaseDataSource {

    private DbConnectType connectType;

    public DbConnectType getConnectType() {
        return connectType;
    }

    public void setConnectType(DbConnectType connectType) {
        this.connectType = connectType;
    }

    /**
     * @return driver class
     */
    @Override
    public String driverClassSelector() {
        return Constants.COM_ORACLE_JDBC_DRIVER;
    }

    /**
     * @return db type
     */
    @Override
    public DbType dbTypeSelector() {
        return DbType.ORACLE;
    }
}
