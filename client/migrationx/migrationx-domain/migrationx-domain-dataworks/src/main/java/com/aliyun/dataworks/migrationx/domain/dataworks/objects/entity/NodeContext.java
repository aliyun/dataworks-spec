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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.ToString;

/**
 * @author sam.liux
 * @date 2019/04/17
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString(callSuper = true)
public class NodeContext {

    @JacksonXmlProperty(isAttribute = true, localName = "type")
    private Integer type;

    @JacksonXmlProperty(isAttribute = true, localName = "paramName")
    private String paramName;

    @JacksonXmlProperty(isAttribute = true, localName = "paramValue")
    private String paramValue;

    @JacksonXmlProperty(isAttribute = true, localName = "paramType")
    private Integer paramType;

    @JacksonXmlProperty(isAttribute = true, localName = "parseType")
    private Integer parseType;

    @JacksonXmlProperty(isAttribute = true, localName = "description")
    private String description;

    @JacksonXmlProperty(isAttribute = true, localName = "editable")
    private Boolean editable;

    @JacksonXmlProperty(isAttribute = true, localName = "nodeId")
    private Long nodeId;

    @JacksonXmlProperty(isAttribute = true, localName = "paramNodeId")
    private Long paramNodeId;

    @JacksonXmlProperty(isAttribute = true, localName = "output")
    private String output;

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String key) {
        this.paramName = key;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String value) {
        this.paramValue = value;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getParamType() {
        return paramType;
    }

    public void setParamType(Integer paramType) {
        this.paramType = paramType;
    }

    public Integer getParseType() {
        return parseType;
    }

    public void setParseType(Integer parseType) {
        this.parseType = parseType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Long getParamNodeId() {
        return paramNodeId;
    }

    public void setParamNodeId(Long paramNodeId) {
        this.paramNodeId = paramNodeId;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
