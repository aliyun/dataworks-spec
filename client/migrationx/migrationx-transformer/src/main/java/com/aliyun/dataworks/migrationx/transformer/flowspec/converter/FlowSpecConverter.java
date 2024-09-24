/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-04
 */
public interface FlowSpecConverter<T> {

    /**
     * Convert the T type to flowSpec
     *
     * @param from origin obj
     * @return flowSpec result
     */
    Specification<DataWorksWorkflowSpec> convert(T from);

}
