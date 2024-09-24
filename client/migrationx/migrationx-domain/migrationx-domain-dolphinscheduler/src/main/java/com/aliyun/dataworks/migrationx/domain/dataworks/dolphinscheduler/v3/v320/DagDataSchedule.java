/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320;

import java.util.List;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessTaskRelation;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.Schedule;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DagDataSchedule {

    private ProcessDefinition processDefinition;
    private List<ProcessTaskRelation> processTaskRelationList;
    private List<TaskDefinition> taskDefinitionList;
    private Schedule schedule;

}