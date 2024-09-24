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

package com.aliyun.dataworks.migrationx.domain.dataworks.airflow;

import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;

/**
 * airflow operator types
 *
 * @author sam.liux
 * @date 2021/01/22
 */
public enum OperatorType {
    ShortCircuitOperator,
    EmailOperator,
    DummyOperator,
    DummySkipOperator,
    BashOperator,
    HiveOperator,
    NamedHivePartitionSensor,
    SparkSqlOperator,
    SparkSubmitOperator,
    PythonOperator,
    _PythonDecoratedOperator,
    PythonVirtualenvOperator,
    BranchPythonOperator,
    SqoopOperator,
    HiveToMysqlTransfer,
    PrestoToMysqlTransfer,
    SimpleHttpOperator,
    ExternalTaskSensor,
    TriggerDagRunOperator,
    ExternalTaskMarker,
    ExternalTaskSensorLink,
    LatestOnlyOperator,
    GetRequestOperator;

    public static OperatorType getOperatorTypeByName(String operator) {
        for (OperatorType type : values()) {
            if (type.name().equalsIgnoreCase(operator)) {
                return type;
            }
        }

        throw BizException.of(ErrorCode.UNKNOWN_ENUM_TYPE).with(OperatorType.class, operator);
    }
}
