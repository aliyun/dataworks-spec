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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.serialize.CodeControlCharEscapeConverter;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.serialize.CodeControlCharUnEscapeConverter;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.CodeModeType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.DmObjectType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.RerunMode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * @author sam.liux
 * @date 2019/04/17
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString(callSuper = true, exclude = {"code"})
@JsonTypeInfo(
        use = Id.MINIMAL_CLASS,
        property = "@class")
public class Node extends DmObject {
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;

    /**
     * 任务类型
     */
    @JacksonXmlProperty(isAttribute = true, localName = "type")
    private String type;

    /**
     * 定时表达式
     */
    @JacksonXmlProperty(isAttribute = true, localName = "cronExpress")
    private String cronExpress;

    /**
     * 生效时间
     */
    @JacksonXmlProperty(localName = "startEffectDate", isAttribute = true)
    private Date startEffectDate;

    @JacksonXmlProperty(localName = "isAutoParse", isAttribute = true)
    private Integer isAutoParse;

    /**
     * 实效时间
     */
    @JacksonXmlProperty(localName = "endEffectDate", isAttribute = true)
    private Date endEffectDate;

    /**
     * 调度资源组
     */
    @JacksonXmlProperty(localName = "resourceGroup", isAttribute = true)
    private String resourceGroup;

    /**
     * 调度资源组名称
     */
    @JacksonXmlProperty(localName = "resourceGroupName", isAttribute = true)
    private String resourceGroupName;

    /**
     * 数据集成资源组
     */
    @JacksonXmlProperty(localName = "diResourceGroup", isAttribute = true)
    private String diResourceGroup;

    /**
     * 数据集成资源组名称
     */
    @JacksonXmlProperty(localName = "diResourceGroupName", isAttribute = true)
    private String diResourceGroupName;

    /**
     * code mode: wizard/code
     */
    @JacksonXmlProperty(localName = "codeMode", isAttribute = true)
    private String codeMode;

    /**
     * 实时转实例
     */
    @JacksonXmlProperty(isAttribute = true, localName = "startRightNow")
    private Boolean startRightNow;

    /**
     * 是否可重跑
     */
    @JacksonXmlProperty(isAttribute = true, localName = "rerunMode")
    private RerunMode rerunMode;

    /**
     * 是否暂停调度
     */
    @JacksonXmlProperty(isAttribute = true, localName = "pauseSchedule")
    private Boolean pauseSchedule;

    /**
     * 手动任务、周期任务、临时查询
     */
    @JacksonXmlProperty(isAttribute = true, localName = "useType")
    private NodeUseType nodeUseType;

    @JacksonXmlProperty(isAttribute = true, localName = "ref")
    private String ref;

    @JacksonXmlProperty(isAttribute = true, localName = "folder")
    private String folder;

    /**
     * 是否根节点
     */
    @JacksonXmlProperty(localName = "isRoot", isAttribute = true)
    private Boolean isRoot;

    /**
     * 连接串名称
     */
    @JacksonXmlProperty(localName = "connection", isAttribute = true)
    private String connection;

    /**
     * 代码
     */
    @JacksonXmlCData
    @JacksonXmlElementWrapper(localName = "Code")
    @JacksonXmlProperty(localName = "Code")
    @JsonSerialize(converter = CodeControlCharEscapeConverter.class)
    @JsonDeserialize(converter = CodeControlCharUnEscapeConverter.class)
    private String code;

    /**
     * 节点参数
     */
    @JacksonXmlCData
    @JacksonXmlProperty(localName = "Parameter")
    @JacksonXmlElementWrapper(localName = "Parameter")
    @JsonSerialize(converter = CodeControlCharEscapeConverter.class)
    @JsonDeserialize(converter = CodeControlCharUnEscapeConverter.class)
    private String parameter;

    /**
     * 节点上下文输入信息
     */
    @JacksonXmlProperty(localName = "InputContext")
    @JacksonXmlElementWrapper(localName = "InputContexts")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<NodeContext> inputContexts = new ArrayList<>();

    /**
     * 节点上下文输出信息
     */
    @JacksonXmlProperty(localName = "OutputContext")
    @JacksonXmlElementWrapper(localName = "OutputContexts")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<NodeContext> outputContexts = new ArrayList<>();

    /**
     * 节点输入信息
     */
    @JacksonXmlProperty(localName = "Input")
    @JacksonXmlElementWrapper(localName = "Inputs")
    private List<NodeIo> inputs = new ArrayList<>();

    /**
     * 节点输出信息
     */
    @JacksonXmlProperty(localName = "Output")
    @JacksonXmlElementWrapper(localName = "Outputs")
    private List<NodeIo> outputs = new ArrayList<>();

    /**
     * 工作流节点内部节点
     */
    @JacksonXmlProperty(localName = "Node")
    @JacksonXmlElementWrapper(localName = "InnerNodes")
    private List<Node> innerNodes = new ArrayList<>();

    /**
     * 描述信息
     */
    @JacksonXmlCData
    @JacksonXmlProperty(localName = "Description")
    @JsonSerialize(converter = CodeControlCharEscapeConverter.class)
    @JsonDeserialize(converter = CodeControlCharUnEscapeConverter.class)
    private String description;

    /**
     * 重跑次数
     */
    @JacksonXmlProperty(localName = "taskRerunTime", isAttribute = true)
    private Integer taskRerunTime;

    /**
     * 重跑间隔
     */
    @JacksonXmlProperty(localName = "taskRerunInterval", isAttribute = true)
    private Integer taskRerunInterval;

    /**
     * 依赖类型
     */
    @JacksonXmlProperty(localName = "dependentType", isAttribute = true)
    private Integer dependentType;

    @JacksonXmlProperty(localName = "cycleType", isAttribute = true)
    private Integer cycleType;

    @JacksonXmlProperty(localName = "lastModifyTime", isAttribute = true)
    private Date lastModifyTime;

    @JacksonXmlProperty(localName = "lastModifyUser", isAttribute = true)
    private String lastModifyUser;

    @JacksonXmlProperty(localName = "multiInstCheckType", isAttribute = true)
    private Integer multiInstCheckType;

    @JacksonXmlProperty(localName = "priority", isAttribute = true)
    private Integer priority;

    @JacksonXmlProperty(localName = "dependentDataNode", isAttribute = true)
    private String dependentDataNode;

    @JacksonXmlProperty(isAttribute = true, localName = "owner")
    private String owner;

    @JacksonXmlProperty(isAttribute = true, localName = "ownerName")
    private String ownerName;

    @JacksonXmlProperty(isAttribute = true, localName = "extraConfig")
    private String extraConfig;

    @JacksonXmlCData
    @JacksonXmlElementWrapper(localName = "ExtraContent")
    @JacksonXmlProperty(localName = "ExtraContent")
    private String extraContent;

    @JacksonXmlCData
    @JacksonXmlElementWrapper(localName = "TtContent")
    @JacksonXmlProperty(localName = "TtContent")
    private String ttContent;

    @JacksonXmlCData
    @JacksonXmlElementWrapper(localName = "advanceSettings")
    @JacksonXmlProperty(localName = "advanceSettings")
    private String advanceSettings;

    @JacksonXmlCData
    @JacksonXmlElementWrapper(localName = "Extend")
    @JacksonXmlProperty(localName = "Extend")
    private transient String extend;

    public Node() {
        super(DmObjectType.NODE);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCronExpress() {
        return cronExpress;
    }

    public void setCronExpress(String cronExpress) {
        this.cronExpress = cronExpress;
    }

    public Date getStartEffectDate() {
        return startEffectDate;
    }

    public void setStartEffectDate(Date startEffectDate) {
        this.startEffectDate = startEffectDate;
    }

    public Date getEndEffectDate() {
        return endEffectDate;
    }

    public void setEndEffectDate(Date endEffectDate) {
        this.endEffectDate = endEffectDate;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    public String getDiResourceGroup() {
        return diResourceGroup;
    }

    public void setDiResourceGroup(String diResourceGroup) {
        this.diResourceGroup = diResourceGroup;
    }

    public String getDiResourceGroupName() {
        return diResourceGroupName;
    }

    public void setDiResourceGroupName(String diResourceGroupName) {
        this.diResourceGroupName = diResourceGroupName;
    }

    public String getCodeMode() {
        return Arrays.stream(CodeModeType.values())
                .filter(t -> t.name().equals(codeMode) || StringUtils.equals(t.getValue(), codeMode))
                .map(CodeModeType::getValue)
                .findFirst().orElse(codeMode);
    }

    public void setCodeMode(String codeMode) {
        this.codeMode = codeMode;
    }

    public Boolean getStartRightNow() {
        return startRightNow;
    }

    public void setStartRightNow(Boolean startRightNow) {
        this.startRightNow = startRightNow;
    }

    public RerunMode getRerunMode() {
        return rerunMode;
    }

    public void setRerunMode(RerunMode rerunMode) {
        this.rerunMode = rerunMode;
    }

    public Boolean getPauseSchedule() {
        return pauseSchedule;
    }

    public void setPauseSchedule(Boolean pauseSchedule) {
        this.pauseSchedule = pauseSchedule;
    }

    public NodeUseType getNodeUseType() {
        return nodeUseType;
    }

    public void setNodeUseType(NodeUseType nodeUseType) {
        this.nodeUseType = nodeUseType;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public Boolean getRoot() {
        return isRoot;
    }

    public void setRoot(Boolean root) {
        isRoot = root;
    }

    public Boolean getIsRoot() {
        return isRoot;
    }

    public void setIsRoot(Boolean root) {
        isRoot = root;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public List<NodeContext> getInputContexts() {
        return inputContexts;
    }

    public void setInputContexts(List<NodeContext> inputContexts) {
        this.inputContexts = inputContexts;
    }

    public List<NodeContext> getOutputContexts() {
        return outputContexts;
    }

    public void setOutputContexts(List<NodeContext> outputContexts) {
        this.outputContexts = outputContexts;
    }

    public List<NodeIo> getInputs() {
        return inputs;
    }

    public void setInputs(List<NodeIo> inputs) {
        this.inputs = inputs;
    }

    public List<NodeIo> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<NodeIo> outputs) {
        this.outputs = outputs;
    }

    public List<Node> getInnerNodes() {
        return innerNodes;
    }

    public void setInnerNodes(List<Node> innerNodes) {
        this.innerNodes = innerNodes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTaskRerunTime() {
        return taskRerunTime;
    }

    public void setTaskRerunTime(Integer taskRerunTime) {
        this.taskRerunTime = taskRerunTime;
    }

    public Integer getTaskRerunInterval() {
        return taskRerunInterval;
    }

    public void setTaskRerunInterval(Integer taskRerunInterval) {
        this.taskRerunInterval = taskRerunInterval;
    }

    public Integer getDependentType() {
        return dependentType;
    }

    public void setDependentType(Integer dependentType) {
        this.dependentType = dependentType;
    }

    public Integer getCycleType() {
        return cycleType;
    }

    public void setCycleType(Integer cycleType) {
        this.cycleType = cycleType;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getLastModifyUser() {
        return lastModifyUser;
    }

    public void setLastModifyUser(String lastModifyUser) {
        this.lastModifyUser = lastModifyUser;
    }

    public Integer getMultiInstCheckType() {
        return multiInstCheckType;
    }

    public void setMultiInstCheckType(Integer multiInstCheckType) {
        this.multiInstCheckType = multiInstCheckType;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDependentDataNode() {
        return dependentDataNode;
    }

    public void setDependentDataNode(String dependentDataNode) {
        this.dependentDataNode = dependentDataNode;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Integer getIsAutoParse() {
        return isAutoParse;
    }

    public void setIsAutoParse(Integer isAutoParse) {
        this.isAutoParse = isAutoParse;
    }

    public String getExtraConfig() {
        return extraConfig;
    }

    public void setExtraConfig(String extraConfig) {
        this.extraConfig = extraConfig;
    }

    public String getExtraContent() {
        return extraContent;
    }

    public Node setExtraContent(String extraContent) {
        this.extraContent = extraContent;
        return this;
    }

    public String getTtContent() {
        return ttContent;
    }

    public Node setTtContent(String ttContent) {
        this.ttContent = ttContent;
        return this;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public String getAdvanceSettings() {
        return advanceSettings;
    }

    public Node setAdvanceSettings(String advanceSettings) {
        this.advanceSettings = advanceSettings;
        return this;
    }
}
