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

package com.aliyun.dataworks.common.spec.domain.dw.types;

import java.util.Locale;

import com.aliyun.dataworks.common.spec.domain.interfaces.LabelEnum;

import lombok.Getter;

/**
 * @author 聿剑
 * @date 2023/01/12
 */
@Getter
public enum CalcEngineType implements IntEnum<CalcEngineType>, LocaleAware, LabelEnum {
    GENERAL(0, "General", "通用"),
    ODPS(1, "MaxCompute", "MaxCompute"),
    EMR(2, "EMR", "EMR"),
    BLINK(3, "Blink", "Blink"),
    HOLO(4, "Hologres", "Hologres"),
    MaxGraph(5, "MaxGraph", "MaxGraph"),
    HYBRIDDB_FOR_POSTGRESQL(7, "AnalyticDB for PostgreSQL", "AnalyticDB for PostgreSQL"),
    ADB_MYSQL(8, "AnalyticDB for MySQL", "AnalyticDB for MySQL"),
    HADOOP_CDH(11, "CDH", "CDH"),
    CLICKHOUSE(14, "ClickHouse", "ClickHouse"),
    FLINK(15, "Flink", "Flink"),
    DATABASE(100, "Database", "数据库"),
    DI(101, "Data Integration", "数据集成"),
    ALGORITHM(102, "Algorithm", "算法"),
    STAR_ROCKS(10001, "StarRocks", "StarRocks"),
    HIVE(10002, "Hive", "Hive"),

    //only for temp
    CUSTOM(99999, "CUSTOM", "CUSTOM"),
    ;

    private final int value;
    private final String name;
    private final String nameCn;

    CalcEngineType(int value, String name, String nameCn) {
        this.value = value;
        this.name = name;
        this.nameCn = nameCn;
    }

    @Override
    public String getDisplayName(Locale locale) {
        if (Locale.SIMPLIFIED_CHINESE.equals(locale)) {
            return nameCn;
        }
        return name;
    }

    @Override
    public String getLabel() {
        return name;
    }
}
