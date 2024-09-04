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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.ToString;

/**
 * @author sam.liux
 * @date 2019/04/17
 */
@JsonTypeInfo(
        use = Id.MINIMAL_CLASS,
        property = "@class")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString(callSuper = true)
public class Resource extends DmObject {
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;

    @JacksonXmlProperty(isAttribute = true, localName = "type")
    private String type;

    @JacksonXmlProperty(isAttribute = true, localName = "folder")
    private String folder;

    @JacksonXmlProperty(isAttribute = true, localName = "path")
    private String path;

    @JacksonXmlProperty(isAttribute = true, localName = "isOdps")
    private Boolean isOdps;

    @JacksonXmlProperty(isAttribute = true, localName = "isLargeFile")
    private Boolean isLargeFile;

    @JacksonXmlProperty(isAttribute = true, localName = "connection")
    private String connection;

    @JacksonXmlProperty(isAttribute = true, localName = "originResourceName")
    private String originResourceName;

    @JacksonXmlProperty(isAttribute = true, localName = "owner")
    private String owner;

    @JacksonXmlProperty(isAttribute = true, localName = "ownerName")
    private String ownerName;

    @JacksonXmlProperty(isAttribute = true, localName = "extend")
    private String extend;

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public Resource() {
        super(DmObjectType.RESOURCE);
    }

    public Boolean getOdps() {
        return isOdps;
    }

    public void setOdps(Boolean odps) {
        isOdps = odps;
    }

    public Boolean getLargeFile() {
        return isLargeFile;
    }

    public void setLargeFile(Boolean largeFile) {
        isLargeFile = largeFile;
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

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOriginResourceName() {
        return originResourceName;
    }

    public void setOriginResourceName(String originResourceName) {
        this.originResourceName = originResourceName;
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
