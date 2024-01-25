/*
 * Copyright (c) 2024, Alibaba Cloud;
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils.JSONUtils.JsonDataDeserializer;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils.JSONUtils.JsonDataSerializer;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils.StringTypeObjectAdapter;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.conditions.ConditionsParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.dependent.DependentParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Priority;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.annotations.JsonAdapter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author 聿剑
 * @date 2022/10/13
 */
@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode
public class TaskNode {

    /**
     * task node id
     */
    private String id;

    /**
     * task node name
     */
    private String name;

    /**
     * task node description
     */
    private String desc;

    /**
     * task node type
     */
    private TaskType type;

    /**
     * the run flag has two states, NORMAL or FORBIDDEN
     */
    private String runFlag;

    /**
     * the front field
     */
    private String loc;

    /**
     * maximum number of retries
     */
    private int maxRetryTimes;

    /**
     * Unit of retry interval: points
     */
    private int retryInterval;

    /**
     * params information
     */
    @JsonDeserialize(using = JsonDataDeserializer.class)
    @JsonSerialize(using = JsonDataSerializer.class)
    private String params;

    /**
     * inner dependency information
     */
    @JsonDeserialize(using = JsonDataDeserializer.class)
    @JsonSerialize(using = JsonDataSerializer.class)
    private String preTasks;

    /**
     * users store additional information
     */
    @JsonDeserialize(using = JsonDataDeserializer.class)
    @JsonSerialize(using = JsonDataSerializer.class)
    private String extras;

    /**
     * node dependency list
     */
    private List<String> depList;

    /**
     * outer dependency information
     */
    @JsonAdapter(StringTypeObjectAdapter.class)
    private DependentParameters dependence;

    @JsonAdapter(ConditionsParameters.class)
    private ConditionsParameters conditionResult;

    /**
     * task instance priority
     */
    private Priority taskInstancePriority;

    /**
     * worker group
     */
    private String workerGroup;

    /**
     * worker group id
     */
    private Integer workerGroupId;

    /**
     * task time out
     */
    @JsonDeserialize(using = JsonDataDeserializer.class)
    @JsonSerialize(using = JsonDataSerializer.class)
    private String timeout;

}
