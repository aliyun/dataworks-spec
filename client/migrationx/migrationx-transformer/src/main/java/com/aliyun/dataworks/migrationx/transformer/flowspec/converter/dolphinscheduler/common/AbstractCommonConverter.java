/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common;

import java.util.ArrayList;

import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.Priority;
import com.aliyun.dataworks.migrationx.transformer.core.utils.UuidUtils;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-04
 */

public abstract class AbstractCommonConverter<T> {

    protected final DolphinSchedulerV3ConverterContext context;

    protected AbstractCommonConverter(DolphinSchedulerV3ConverterContext context) {
        this.context = context;
    }

    /**
     * convert to T type object
     *
     * @return T type object
     */
    protected abstract T convert();

    /**
     * convert dolphin priority to spec priority
     *
     * @param priority dolphin priority
     * @return spec priority
     */
    protected Integer convertPriority(Priority priority) {
        return Priority.LOWEST.getCode() - priority.getCode();
    }

    /**
     * generate uuid and save mapping between code and uuid in context.
     * it will be used in build dependencies
     *
     * @param code dolphinScheduler entity code
     * @return uuid
     */
    protected String generateUuid(Long code) {
        context.getCodeUuidMap().computeIfAbsent(code, k -> UuidUtils.genUuidWithoutHorizontalLine());
        return context.getCodeUuidMap().get(code);
    }

    /**
     * generate uuid and save in context
     *
     * @return uuid
     */
    protected String generateUuid() {
        String uuid = UuidUtils.genUuidWithoutHorizontalLine();
        context.getCodeUuidMap().putIfAbsent(Long.valueOf(uuid), uuid);
        return uuid;
    }

    protected SpecFlowDepend newSpecFlowDepend() {
        SpecFlowDepend specFlowDepend = new SpecFlowDepend();
        specFlowDepend.setDepends(new ArrayList<>());
        return specFlowDepend;
    }
}
