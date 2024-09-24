/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320.DagDataSchedule;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-03
 */
public interface DolphinSchedulerConverter<T> {

    /**
     * Convert the T type to dolphin scheduler obj
     *
     * @param from origin obj
     * @return dolphin scheduler obj
     */
    DagDataSchedule convert(T from);
}
