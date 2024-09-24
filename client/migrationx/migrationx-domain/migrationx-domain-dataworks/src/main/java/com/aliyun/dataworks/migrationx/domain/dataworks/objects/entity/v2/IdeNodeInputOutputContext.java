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

/**
 *
 */
package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Date;

/**
 * @author luojian.lj (玄佑)
 * @ClassName: NodeInputOutputContext
 * @Description: 节点的输出输出上下文信息
 * @date 2018年9月19日 下午3:46:09
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "nodeInputOutputContext")
public class IdeNodeInputOutputContext {

    /**
     * 主键
     */
    @JacksonXmlProperty(localName = "id", isAttribute = true)
    private Long id;

    /**
     * 创建时间
     */
    @JacksonXmlProperty(localName = "createTime", isAttribute = true)
    private Date createTime;

    /**
     * 修改时间
     */
    @JacksonXmlProperty(localName = "modifyTime", isAttribute = true)
    private Date modifyTime;

    /**
     * 关联node_def的node_id
     */
    @JacksonXmlProperty(localName = "nodeId", isAttribute = true)
    private Long nodeId;

    /**
     * 输入参数匹配的上游节点的nodeId
     */
    @JacksonXmlProperty(localName = "paramNodeId", isAttribute = true)
    private Long paramNodeId;

    /**
     * 参数名称
     */
    @JacksonXmlProperty(localName = "paramName", isAttribute = true)
    private String paramName;

    /**
     * 参数值
     */
    @JacksonXmlProperty(localName = "paramValue", isAttribute = true)
    private String paramValue;

    /**
     * 0-上游key 1-const, 2-变量
     */
    @JacksonXmlProperty(localName = "paramType", isAttribute = true)
    private Integer paramType;

    /**
     * 0-in, 1-out
     */
    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private Integer type;

    /**
     * 描述信息
     */
    @JacksonXmlProperty(localName = "description", isAttribute = true)
    private String description;

    /**
     * 输入参数匹配的上游节点的调度节点ID
     */
    @JacksonXmlProperty(localName = "cloudUuid", isAttribute = true)
    private Long cloudUuid;

    @JacksonXmlProperty(localName = "output", isAttribute = true)
    private String output;

    @JacksonXmlProperty(localName = "editable", isAttribute = true)
    private boolean editable = true;

    /**
     * 参数类型，0 自动解析，1 手动添加，2 系统添加
     */
    @JacksonXmlProperty(localName = "parseType", isAttribute = true)
    private Integer parseType;

    /**
     * setter for column 主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * getter for column 主键
     */
    public Long getId() {
        return this.id;
    }

    /**
     * setter for column 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * getter for column 创建时间
     */
    public Date getCreateTime() {
        return this.createTime;
    }

    /**
     * setter for column 修改时间
     */
    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    /**
     * getter for column 修改时间
     */
    public Date getModifyTime() {
        return this.modifyTime;
    }

    /**
     * setter for column 关联node_def的node_id
     */
    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * getter for column 关联node_def的node_id
     */
    public Long getNodeId() {
        return this.nodeId;
    }

    /**
     * setter for column 参数名称
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    /**
     * getter for column 参数名称
     */
    public String getParamName() {
        return this.paramName;
    }

    /**
     * setter for column 参数值
     */
    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    /**
     * getter for column 参数值
     */
    public String getParamValue() {
        return this.paramValue;
    }

    /**
     * setter for column 0-in, 1-out
     */
    public void setParamType(Integer paramType) {
        this.paramType = paramType;
    }

    /**
     * getter for column 0-in, 1-out
     */
    public Integer getParamType() {
        return this.paramType;
    }

    /**
     * setter for column 0-上游key 1-const, 2-变量
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * getter for column 0-上游key 1-const, 2-变量
     */
    public Integer getType() {
        return this.type;
    }

    /**
     * setter for column 描述信息
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * getter for column 描述信息
     */
    public String getDescription() {
        return this.description;
    }

    public Long getParamNodeId() {
        return paramNodeId;
    }

    public void setParamNodeId(Long paramNodeId) {
        this.paramNodeId = paramNodeId;
    }

    public Long getCloudUuid() {
        return cloudUuid;
    }

    public void setCloudUuid(Long cloudUuid) {
        this.cloudUuid = cloudUuid;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public Integer getParseType() {
        return parseType;
    }

    public void setParseType(Integer parseType) {
        this.parseType = parseType;
    }
}
