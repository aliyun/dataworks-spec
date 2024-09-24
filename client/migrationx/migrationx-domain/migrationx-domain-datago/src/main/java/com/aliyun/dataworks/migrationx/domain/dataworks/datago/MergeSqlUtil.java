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

package com.aliyun.dataworks.migrationx.domain.dataworks.datago;

import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.DateParser;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.BaseModel;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.model.cdp.WriterModel.OdpsWriterModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description
 * @createTime 2020-04-29
 */
public class MergeSqlUtil {

    public static String generateMergeSql(boolean isDaily, OdpsWriterModel writerModel,
                                          List<String> joinKeys) throws Exception {
        Map<String, Object> params = new HashMap<>();
        String template;
        if (isDaily) {
            template = "datagoTemplate/merge_template_daily.sql.vm";
            String bizDate = DateParser.parsePartition(writerModel.getPt());
            params.put("pt", writerModel.getPtKey()[0]);
            // 业务日期
            params.put("bizDate", bizDate);
            // 前天
            params.put("bizYesterday", bizDate.replace("}", "-1}"));
        } else {
            template = "datagoTemplate/merge_template_hour.sql.vm";
            // 执行日期
            params.put("today", DateParser.parsePartition(writerModel.getPt().split(",")[0])
                .replace("{", "[")
                .replace("}", "]"));
            params.put("dayPt", writerModel.getPtKey()[0]);
            params.put("hourPt", writerModel.getPtKey()[1]);

            params.put("bizHour", "@@[HH -1h]");
            params.put("bizLastHour", "@@[HH -2h]");

        }
        List<String> columns = writerModel.getColumns().stream().map(BaseModel.Column::getName).collect(Collectors.toList());

        String tableName = writerModel.getTableName();
        String deltaTableName = writerModel.getDeltaTableName();
        params.put("targetTableName", tableName);
        params.put("targetDeltaTableName", deltaTableName);
        params.put("joinKeys", joinKeys);
        params.put("columns", columns);
        return VelocityUtil.render(template, params);
    }

    public static String generateWhere(Boolean isDaily, OdpsWriterModel writerModel, String incrColumn)
        throws Exception {
        Map<String, Object> params = new HashMap<>();
        String template;
        if (isDaily) {
            template = "datagoTemplate/where_template_daily.sql.vm";
            String bizDate = DateParser.parsePartition(writerModel.getPt());
            params.put("bizdate", bizDate);
            params.put("today", bizDate.replace("}", "]").replace("{", "["));
        } else {
            template = "datagoTemplate/where_template_hour.sql.vm";
            String bizDate = DateParser.parsePartition(writerModel.getPt().split(",")[0]);
            params.put("today", bizDate.replace("{", "[").replace("}", "]"));
            params.put("bizHour", "@@[HH -1h]");
            params.put("hour", "@@[HH]");
        }
        params.put("incrColumn", incrColumn);
        return VelocityUtil.render(template, params);
    }
}