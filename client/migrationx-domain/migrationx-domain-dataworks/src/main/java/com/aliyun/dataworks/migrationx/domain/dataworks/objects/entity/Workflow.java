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


import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.DmObjectType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowVersion;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sam.liux
 * @date 2019/04/17
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "Workflow")
@ToString(callSuper = true)
public class Workflow extends DmObject{
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;

    @JacksonXmlProperty(isAttribute = true, localName = "version")
    private WorkflowVersion version;

    @JacksonXmlProperty(isAttribute = true, localName = "type")
    private WorkflowType type;

    @JacksonXmlProperty(isAttribute = true, localName = "scheduled")
    private Boolean scheduled;

    @JacksonXmlProperty(localName = "Parameters")
    private String parameters;

    @JacksonXmlProperty(localName = "Node")
    @JacksonXmlElementWrapper(localName = "Nodes")
    private List<Node> nodes = new ArrayList<>();

    @JacksonXmlProperty(localName = "Resource")
    @JacksonXmlElementWrapper(localName = "Resources")
    private List<Resource> resources = new ArrayList<>();

    @JacksonXmlProperty(localName = "Function")
    @JacksonXmlElementWrapper(localName = "Functions")
    private List<Function> functions = new ArrayList<>();

    @JacksonXmlProperty(localName = "Table")
    @JacksonXmlElementWrapper(localName = "Tables")
    private List<Table> tables = new ArrayList<>();

    @JacksonXmlProperty(isAttribute = true, localName = "ref")
    private String ref;

    @JacksonXmlProperty(isAttribute = true, localName = "owner")
    private String owner;

    @JacksonXmlProperty(isAttribute = true, localName = "ownerName")
    private String ownerName;
    /**
     * 工作流布局信息
     */
    @JacksonXmlProperty(localName = "DisplaySettings")
    @JacksonXmlCData
    private String displaySettings;

    public Workflow() {
        super(DmObjectType.WORKFLOW);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WorkflowVersion getVersion() {
        return version;
    }

    public void setVersion(WorkflowVersion version) {
        this.version = version;
    }

    public Boolean getScheduled() {
        return scheduled;
    }

    public void setScheduled(Boolean scheduled) {
        this.scheduled = scheduled;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(List<Function> functions) {
        this.functions = functions;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
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

    public WorkflowType getType() {
        return type;
    }

    public void setType(WorkflowType type) {
        this.type = type;
    }

    public String getDisplaySettings() {
        return displaySettings;
    }

    public Workflow setDisplaySettings(String displaySettings) {
        this.displaySettings = displaySettings;
        return this;
    }
}
