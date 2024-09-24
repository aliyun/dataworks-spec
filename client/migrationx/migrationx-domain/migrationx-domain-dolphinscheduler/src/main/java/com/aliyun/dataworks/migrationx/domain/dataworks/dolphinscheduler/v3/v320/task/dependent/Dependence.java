/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320.task.dependent;

import java.util.List;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.DependentRelation;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.DependentTaskModel;
import lombok.Data;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-28
 */
@Data
public class Dependence {

    private List<DependentTaskModel> dependTaskList;
    private DependentRelation relation;
    /**
     * Time unit is second
     */
    private Integer checkInterval;
    private DependentFailurePolicyEnum failurePolicy;
    /**
     * Time unit is minutes
     */
    private Integer failureWaitingTime;

    /**
     * the dependent task failure policy.
     */
    public enum DependentFailurePolicyEnum {
        DEPENDENT_FAILURE_FAILURE,
        DEPENDENT_FAILURE_WAITING
    }
}