/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowSpecConverterContext {

    private Map<String, Long> idCodeMap = new HashMap<>();

    private boolean onlineProcess;

    private boolean onlineSchedule;

    private long projectCode;

    private int userId = 1;

    private String workerGroup = "default";

    private String tenantCode = "default";

    private long environmentCode = -1L;

    private int defaultDatasourceId = 1;

    private String defaultDatasourceType = "MYSQL";

    private String defaultFileResourcePath = "";
}
