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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.client;

import java.util.Date;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 发布包对象
 *
 * @author 聿剑
 * @version 1.0
 * @since 2024/01/10
 */
@Data
@ToString(callSuper = true)
@Accessors(chain = true)
public class Deployment {

    /**
     * 发布包id，自增id
     */
    private Long id;

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 发布包名称
     */
    private String name;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 跨项目发布的targetAppId
     */
    private Long targetAppId;

    /**
     * 跨项目对应的deploymentId
     */
    private Long targetDeploymentId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 发布包创建者id
     */
    private String creatorId;

    /**
     * 发布包创建者名称
     */
    private String creatorName;

    /**
     * 发布包处理者
     */
    private String handlerId;

    /**
     * 发布包处理者名称
     */
    private String handlerName;

    /**
     * 发布包创建时间
     */
    private Date createTime;

    /**
     * 发布包执行时间
     */
    private Date executeTime;

    /**
     * 发布包状态 0:ready（创建发布包完成，默认最开始就是这个状态） 1:done（执行发布成功） 2:failed（执行发布失败） 3:canceled（发布包已取消）
     */
    private Integer status;

    /**
     * 该发布包中包含的任务总数（包括资源）
     */
    private Integer totalTaskCount;

    /**
     * 该发布包中执行成功的任务总数
     */
    private Integer successTaskCount;

    /**
     * 描述信息
     */
    private String description;

    /**
     * CloudOpenApi返回的requestId
     */
    private String requestId;

    /**
     * 本次发布之前的环境 0:local（本地环境，还未上调度环境） 1:dev（调度dev环境） 2:prod（调动prod环境）
     */
    private Integer fromEnv;

    /**
     * 本次发布之前的环境 0:local（本地环境，还未上调度环境） 1:dev（调度dev环境） 2:prod（调动prod环境）
     */
    private Integer toEnv;

    /**
     * 发布包错误信息
     */
    private String errMsg;

    /**
     * 是否提交并解锁
     */
    private Integer unlockWhenSubmitted;

    private Integer start = 0;

    private Integer limit = Integer.MAX_VALUE;

    private Integer deployType;
}
