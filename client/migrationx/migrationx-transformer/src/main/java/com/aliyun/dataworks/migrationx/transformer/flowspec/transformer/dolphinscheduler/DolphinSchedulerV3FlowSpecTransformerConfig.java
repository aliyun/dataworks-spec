/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.transformer.dolphinscheduler;

import java.util.Locale;

import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;
import lombok.Data;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-26
 */
@Data
public class DolphinSchedulerV3FlowSpecTransformerConfig {
    private DolphinSchedulerV3ConverterContext context;

    private Locale locale;
}
