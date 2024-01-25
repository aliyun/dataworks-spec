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

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author sam.liux
 * @date 2021/01/14
 */
@ToString
@Accessors(chain = true)
@Data
public class DeployDetail {
    /**
     * 发布包详情id。自增的id。
     */
    private Long id;

    /**
     * 发布包记录id
     */
    private Long deployId;

    /**
     * 文件id
     */
    private Long fileId;

    /**
     * 目标项目的文件id
     */
    private Long targetFileId;

    /**
     * 该任务文件的当前版本
     */
    private Long currentFileVersion;

    /**
     * 该任务文件的当前在调度生产环境中的版本
     * <p>
     * 记录currentFileVersion和prodFileVersion这两个字段，主要是为了做diff
     * </p>
     */
    private Long prodFileVersion;

    /**
     * 该任务的发布状态 0:unpublished（未发布） 1:success（发布成功） 2:error（发布失败） 3:cloned（克隆任务完成）
     */
    private Integer status;

    /**
     * 扩展信息
     */
    private String message;

    /**
     * 需要发布到的调度环境 0:local（本地环境，还未上调度环境） 1:dev（调度dev环境） 2:prod（调动prod环境）
     */
    private Integer toEnv;

    private Long dataVersion = -1L;
}
