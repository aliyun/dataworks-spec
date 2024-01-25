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

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.ExportProject;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeEngineInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.DmObjectType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sam.liux
 * @date 2019/04/17
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString(callSuper = true)
public class Project extends DmObject {

    @JacksonXmlProperty(localName = "EngineInfo")
    private IdeEngineInfo engineInfo;

    @JacksonXmlProperty(localName = "ExportProject")
    private ExportProject exportProject;

    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;

    @JacksonXmlProperty(isAttribute = true, localName = "appId")
    private Long appId;

    @JacksonXmlProperty(isAttribute = true, localName = "tenantId")
    private Long tenantId;

    @JacksonXmlProperty(isAttribute = true, localName = "opUser")
    private String opUser;

    @JacksonXmlProperty(localName = "Solution")
    @JacksonXmlElementWrapper(localName = "Solutions")
    private List<Solution> solutions = new ArrayList<>();

    @JacksonXmlProperty(localName = "Folder")
    @JacksonXmlElementWrapper(localName = "Folders")
    private List<Folder> folders = new ArrayList<>();

    @JacksonXmlProperty(localName = "UserDefinedNode")
    @JacksonXmlElementWrapper(localName = "UserDefinedNodes")
    private List<UserDefinedNode> userDefinedNodes = new ArrayList<>();

    @JacksonXmlProperty(localName = "Workflow")
    @JacksonXmlElementWrapper(localName = "Workflows")
    private List<Workflow> workflows = new ArrayList<>();

    @JacksonXmlProperty(localName = "Datasource")
    @JacksonXmlElementWrapper(localName = "Datasources")
    private List<Datasource> datasources = new ArrayList<>();

    @JacksonXmlProperty(localName = "Resource")
    @JacksonXmlElementWrapper(localName = "Resources")
    private List<Resource> resources = new ArrayList<>();

    @JacksonXmlProperty(localName = "Function")
    @JacksonXmlElementWrapper(localName = "Functions")
    private List<Function> functions = new ArrayList<>();

    @JacksonXmlProperty(localName = "Table")
    @JacksonXmlElementWrapper(localName = "Tables")
    private List<Table> tables = new ArrayList<>();

    @JacksonXmlProperty(localName = "AdHocQuery")
    @JacksonXmlElementWrapper(localName = "AdHocQueries")
    private List<Node> adHocQueries = new ArrayList<>();

    @JacksonXmlProperty(localName = "Component")
    @JacksonXmlElementWrapper(localName = "Components")
    private List<Node> components = new ArrayList<>();

    @JacksonXmlProperty(localName = "projectDir", isAttribute = true)
    private String projectDir;

    @JacksonXmlProperty(isAttribute = true, localName = "owner")
    private String owner;

    @JacksonXmlProperty(isAttribute = true, localName = "ownerName")
    private String ownerName;

    public Project() {
        super(DmObjectType.PROJECT);
    }

    public List<Node> getComponents() {
        return components;
    }

    public void setComponents(List<Node> components) {
        this.components = components;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getOpUser() {
        return opUser;
    }

    public void setOpUser(String opUser) {
        this.opUser = opUser;
    }

    public List<Solution> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<Solution> solutions) {
        this.solutions = solutions;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public List<UserDefinedNode> getUserDefinedNodes() {
        return userDefinedNodes;
    }

    public void setUserDefinedNodes(List<UserDefinedNode> userDefinedNodes) {
        this.userDefinedNodes = userDefinedNodes;
    }

    public List<Workflow> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(List<Workflow> workflows) {
        this.workflows = workflows;
    }

    public List<Datasource> getDatasources() {
        return datasources;
    }

    public void setDatasources(List<Datasource> datasources) {
        this.datasources = datasources;
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

    public List<Node> getAdHocQueries() {
        return adHocQueries;
    }

    public void setAdHocQueries(List<Node> adHocQueries) {
        this.adHocQueries = adHocQueries;
    }

    public String getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(String projectDir) {
        this.projectDir = projectDir;
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

    public ExportProject getExportProject() {
        return exportProject;
    }

    public Project setExportProject(ExportProject exportProject) {
        this.exportProject = exportProject;
        return this;
    }

    public IdeEngineInfo getEngineInfo() {
        return engineInfo;
    }

    public Project setEngineInfo(IdeEngineInfo engineInfo) {
        this.engineInfo = engineInfo;
        return this;
    }
}
