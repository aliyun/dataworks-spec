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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2;

/**
 * @author sam.liux
 * @date 2019/04/28
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "nodeDef")
@Data
@JsonIgnoreProperties(value = {"fileId"})
public class IdeNodeDef implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 关联file表的file_id，调整依赖关系
     */
    @JacksonXmlProperty(isAttribute = true, localName = "fileId")
    private String fileId;

    /**
     * 任务自动重跑次数
     */
    @JacksonXmlProperty(isAttribute = true, localName = "taskRerunTime")
    private Integer taskRerunTime;

    /**
     * 任务自动重跑次数， 兼容
     */
    @JacksonXmlProperty(isAttribute = true, localName = "taskReRunTime")
    private Integer taskReRunTime;

    /**
     * 任务自动重跑间隔，单位ms
     */
    @JacksonXmlProperty(isAttribute = true, localName = "taskRerunInterval")
    private Integer taskRerunInterval;

    /**
     * 任务自动重跑间隔，单位ms，兼容
     */
    @JacksonXmlProperty(isAttribute = true, localName = "taskReRunInterval")
    private Integer taskReRunInterval;

    /**
     * 1：可重跑（默认值），0：不可重跑
     */
    @JacksonXmlProperty(isAttribute = true, localName = "reRunAble")
    private Integer reRunAble;

    /**
     * 自增ID
     */
    @JacksonXmlProperty(isAttribute = true, localName = "nodeId")
    private Long nodeId;

    /**
     * 节点名称，与file表中的file_name相同
     */
    @JacksonXmlProperty(isAttribute = true, localName = "nodeName")
    private String nodeName;

    /**
     * 是否暂停,0:不暂停,1：暂停
     */
    @JacksonXmlProperty(isAttribute = true, localName = "isStop")
    private Integer isStop;

    /**
     * 描述
     */
    @JacksonXmlProperty(isAttribute = true, localName = "description")
    private String description;

    /**
     * 变量
     */
    @JacksonXmlProperty(isAttribute = true, localName = "paraValue")
    private String paraValue;

    /**
     * 优先级
     */
    @JacksonXmlProperty(isAttribute = true, localName = "priority")
    private Integer priority;

    /**
     * 开始执行日期
     */
    @JacksonXmlProperty(isAttribute = true, localName = "startEffectDate")
    private Date startEffectDate;

    /**
     * 结束执行日期
     */
    @JacksonXmlProperty(isAttribute = true, localName = "endEffectDate")
    private Date endEffectDate;

    /**
     * 正则表达式
     */
    @JacksonXmlProperty(isAttribute = true, localName = "cronExpress")
    private String cronExpress;

    /**
     * 任务OWNER
     */
    @JacksonXmlProperty(isAttribute = true, localName = "owner")
    private String owner;

    /**
     * 资源组ID
     */
    @JacksonXmlProperty(isAttribute = true, localName = "resGroupId")
    private String resgroupId;

    /**
     * 基线ID
     */
    @JacksonXmlProperty(isAttribute = true, localName = "baselineId")
    private Long baselineId;

    /**
     * 调度项目ID
     */
    @JacksonXmlProperty(isAttribute = true, localName = "appId")
    private Long appId;

    /**
     * 创建时间
     */
    @JacksonXmlProperty(isAttribute = true, localName = "createTime")
    private Date createTime;

    /**
     * 创建人
     */
    @JacksonXmlProperty(isAttribute = true, localName = "createUser")
    private String createUser;

    /**
     * 上次修改时间
     */
    @JacksonXmlProperty(isAttribute = true, localName = "lastModifyTime")
    private Date lastModifyTime;

    /**
     * 0:不做 1：单天之内做 2：每天都做
     */
    @JacksonXmlProperty(isAttribute = true, localName = "multiinstCheckType")
    private Integer multiinstCheckType;

    /**
     * 0:周期大于天 1:周期小于天 2:周期小于天并且实例按照顺序执行
     */
    @JacksonXmlProperty(isAttribute = true, localName = "cycleType")
    private Integer cycleType;

    /**
     * 依赖类型： 0:不跨版本 1:跨版本自定义依赖 2:跨版本,一级子节点依赖 3:跨版本,自依赖
     */
    @JacksonXmlProperty(isAttribute = true, localName = "dependentType")
    private Integer dependentType;

    /**
     * 上次修改人
     */
    @JacksonXmlProperty(isAttribute = true, localName = "lastModifyUser")
    private String lastModifyUser;

    /**
     * 依赖的任务节点
     */
    @JacksonXmlProperty(isAttribute = true, localName = "dependentDataNode")
    private String dependentDataNode = "";

    /**
     * datax fileId
     */
    @JacksonXmlProperty(isAttribute = true, localName = "dataxFileId")
    private String dataxFileId;

    /**
     * datax fileVersion
     */
    @JacksonXmlProperty(isAttribute = true, localName = "dataxFileVersion")
    private Long dataxFileVersion;

    /**
     * 输入json字符串
     */
    @JacksonXmlProperty(isAttribute = true, localName = "input")
    private String input;

    /**
     * 输出json字符串
     */
    @JacksonXmlProperty(isAttribute = true, localName = "output")
    private String output;

    @JsonIgnore
    private List<IdeNodeInputOutput> inputList;

    @JsonIgnore
    private List<IdeNodeInputOutput> outputList;

    /**
     * 是否自动解析：
     */
    @JacksonXmlProperty(isAttribute = true, localName = "isAutoParse")
    private Integer isAutoParse;

    @JacksonXmlProperty(localName = "startRightNow", isAttribute = true)
    private Boolean startRightNow;

    public Integer getTaskRerunTime() {
        return taskRerunTime == null ? taskReRunTime : taskRerunTime;
    }

    public IdeNodeDef setTaskRerunTime(Integer taskRerunTime) {
        this.taskRerunTime = taskRerunTime;
        this.taskReRunTime = taskRerunTime;
        return this;
    }

    public Integer getTaskRerunInterval() {
        return taskRerunInterval == null ? taskReRunInterval : taskRerunInterval;
    }

    public IdeNodeDef setTaskRerunInterval(Integer taskRerunInterval) {
        this.taskRerunInterval = taskRerunInterval;
        this.taskReRunInterval = taskRerunInterval;
        return this;
    }
}
