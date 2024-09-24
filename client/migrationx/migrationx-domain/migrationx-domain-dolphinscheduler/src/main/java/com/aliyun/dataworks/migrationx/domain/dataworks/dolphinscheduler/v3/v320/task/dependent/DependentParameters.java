/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320.task.dependent;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.parameters.AbstractParameters;
import lombok.Data;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-28
 */
@Data
public class DependentParameters extends AbstractParameters {
    private Dependence dependence;

    @Override
    public boolean checkParameters() {
        return true;
    }

}