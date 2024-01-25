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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.ToString;

import java.util.UUID;

/**
 * @author sam.liux
 * @date 2020/01/04
 */
@JacksonXmlRootElement(localName = "folder")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString(callSuper = true)
public class Folder extends DmObject {
    public Folder() {
        super(DmObjectType.FOLDER);
    }

    @JacksonXmlProperty(isAttribute = true, localName = "folderItemType")
    private Integer folderItemType;
    @JacksonXmlProperty(isAttribute = true, localName = "folderItemName")
    private String folderItemName;
    @JacksonXmlProperty(isAttribute = true, localName = "folderItemPath")
    private String folderItemPath;
    @JacksonXmlProperty(isAttribute = true, localName = "bizId")
    private String bizId;
    @JacksonXmlProperty(isAttribute = true, localName = "bizUseType")
    private Integer bizUseType;
    @JacksonXmlProperty(isAttribute = true, localName = "type")
    private Integer type;
    @JacksonXmlProperty(isAttribute = true, localName = "subType")
    private Integer subType;
    @JacksonXmlProperty(isAttribute = true, localName = "sourceApp")
    private String sourceApp;
    @JacksonXmlProperty(isAttribute = true, localName = "version")
    private Integer version;
    @JacksonXmlProperty(isAttribute = true, localName = "engineType")
    private String engineType;
    @JacksonXmlProperty(isAttribute = true, localName = "owner")
    private String owner;
    @JacksonXmlProperty(isAttribute = true, localName = "ownerName")
    private String ownerName;

    @Override
    public String getUniqueKey() {
        return UUID.nameUUIDFromBytes(folderItemPath.getBytes()).toString();
    }

    public Integer getFolderItemType() {
        return folderItemType;
    }

    public void setFolderItemType(Integer folderItemType) {
        this.folderItemType = folderItemType;
    }

    public String getFolderItemName() {
        return folderItemName;
    }

    public void setFolderItemName(String folderItemName) {
        this.folderItemName = folderItemName;
    }

    public String getFolderItemPath() {
        return folderItemPath;
    }

    public void setFolderItemPath(String folderItemPath) {
        this.folderItemPath = folderItemPath;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public Integer getBizUseType() {
        return bizUseType;
    }

    public void setBizUseType(Integer bizUseType) {
        this.bizUseType = bizUseType;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }

    public String getSourceApp() {
        return sourceApp;
    }

    public void setSourceApp(String sourceApp) {
        this.sourceApp = sourceApp;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
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
}
