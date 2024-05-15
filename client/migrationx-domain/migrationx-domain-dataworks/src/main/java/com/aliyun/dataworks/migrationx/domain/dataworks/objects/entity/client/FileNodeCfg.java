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
import java.util.List;

import com.alibaba.fastjson.JSON;

import com.aliyun.migrationx.common.utils.GsonUtils;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author sam.liux
 * @date 2020/04/30
 */
@Data
@ToString(callSuper = true)
@Accessors(chain = true)
public class FileNodeCfg {
    Long appId;
    Long baselineId;
    Date createTime;
    String createUser;
    String cronExpress;
    Integer cycleType;
    Long dataxFileId;
    Integer dataxFileVersion;
    String dependentDataNode;
    Integer dependentType;
    String description;
    Date endEffectDate;
    Long fileId;
    String input;
    List<FileNodeInputOutputContext> inputContextList;
    List<FileNodeInputOutput> inputList;
    Integer isAutoParse;
    Integer isStop;
    Date lastModifyTime;
    String lastModifyUser;
    Integer multiinstCheckType;
    Long nodeId;
    String nodeName;
    String output;
    List<FileNodeInputOutputContext> outputContextList;
    List<FileNodeInputOutput> outputList;
    String owner;
    String paraValue;
    Integer priority;
    Integer reRunAble;
    Long resgroupId;
    Date startEffectDate;
    Boolean startRightNow;
    Integer taskRerunInterval;
    Integer taskRerunTime;
    String extConfig;

    public void setOutputByOutputList() {
        if (StringUtils.isBlank(output) && !CollectionUtils.isEmpty(outputList)) {
            this.output = GsonUtils.toJsonString(outputList);
        }
    }

    public void setInputByInputList() {
        if (StringUtils.isBlank(input) && !CollectionUtils.isEmpty(inputList)) {
            this.input = GsonUtils.toJsonString(inputList);
        }
    }
}
