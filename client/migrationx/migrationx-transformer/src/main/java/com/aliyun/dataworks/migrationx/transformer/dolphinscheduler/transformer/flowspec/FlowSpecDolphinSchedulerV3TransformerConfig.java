/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.transformer.flowspec;

import java.util.Locale;

import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import lombok.Data;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-05
 */
@Data
public class FlowSpecDolphinSchedulerV3TransformerConfig {

    private FlowSpecConverterContext context;

    private Locale locale;
}
