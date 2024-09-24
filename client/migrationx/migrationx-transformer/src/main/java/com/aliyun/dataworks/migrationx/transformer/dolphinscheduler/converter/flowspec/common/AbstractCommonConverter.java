/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common;

import java.util.Objects;

import com.aliyun.dataworks.migrationx.transformer.core.utils.CodeGenerateUtils;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import org.apache.commons.lang3.RandomUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-04
 */
public abstract class AbstractCommonConverter<T> {

    protected T result;

    protected FlowSpecConverterContext context;

    protected AbstractCommonConverter(T result, FlowSpecConverterContext context) {
        this.result = result;
        this.context = context;
    }

    protected AbstractCommonConverter(FlowSpecConverterContext context) {
        this.context = context;
    }

    /**
     * convert member field to variable
     *
     * @return result
     */
    protected abstract T convert();

    /**
     * use other id generate dolphin style id
     *
     * @param id id
     * @return dolphin style id
     */
    protected int generateId(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return RandomUtils.nextInt(100000000, 999999999);
        }
    }

    /**
     * use other id generate dolphin style code
     *
     * @param id id
     * @return dolphin style code
     */
    protected long generateCode(String id) {
        if (Objects.isNull(id)) {
            throw new BizException(ErrorCode.PARAMETER_NOT_SET, "id");
        }
        Long code = context.getIdCodeMap().get(id);
        if (Objects.nonNull(code)) {
            return code;
        }

        try {
            code = Long.parseLong(id);
        } catch (NumberFormatException e) {
            code = CodeGenerateUtils.getInstance().genCode();
        }
        context.getIdCodeMap().put(id, code);
        return code;
    }
}
