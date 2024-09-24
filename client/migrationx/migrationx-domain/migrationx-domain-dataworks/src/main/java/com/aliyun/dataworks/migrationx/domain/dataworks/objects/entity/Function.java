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

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.serialize.CodeControlCharEscapeConverter;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.serialize.CodeControlCharUnEscapeConverter;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.DmObjectType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.ToString;

/**
 * @author sam.liux
 * @date 2019/04/17
 */
@ToString(callSuper = true)
public class Function extends DmObject{
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;

    @JacksonXmlProperty(isAttribute = true, localName = "type")
    private String type;

    @JacksonXmlProperty(isAttribute = true, localName = "functionType")
    private String functionType;

    @JacksonXmlProperty(isAttribute = true, localName = "folder")
    private String folder;

    @JacksonXmlProperty(isAttribute = true, localName = "clazz")
    private String clazz;

    @JacksonXmlProperty(isAttribute = true, localName = "resource")
    private String resource;

    @JacksonXmlProperty(isAttribute = true, localName = "command")
    @JsonSerialize(converter = CodeControlCharEscapeConverter.class)
    @JsonDeserialize(converter = CodeControlCharUnEscapeConverter.class)
    private String command;

    @JacksonXmlProperty(isAttribute = true, localName = "arguments")
    @JsonSerialize(converter = CodeControlCharEscapeConverter.class)
    @JsonDeserialize(converter = CodeControlCharUnEscapeConverter.class)
    private String arguments;

    @JacksonXmlProperty(isAttribute = true, localName = "returnValue")
    @JsonSerialize(converter = CodeControlCharEscapeConverter.class)
    @JsonDeserialize(converter = CodeControlCharUnEscapeConverter.class)
    private String returnValue;

    @JacksonXmlProperty(isAttribute = true, localName = "example")
    @JsonSerialize(converter = CodeControlCharEscapeConverter.class)
    @JsonDeserialize(converter = CodeControlCharUnEscapeConverter.class)
    private String example;

    @JacksonXmlProperty(isAttribute = true, localName = "description")
    @JsonSerialize(converter = CodeControlCharEscapeConverter.class)
    @JsonDeserialize(converter = CodeControlCharUnEscapeConverter.class)
    private String description;

    @JacksonXmlProperty(isAttribute = true, localName = "isOdps")
    private Boolean isOdps;

    @JacksonXmlProperty(isAttribute = true, localName = "connection")
    private String connection;

    @JacksonXmlProperty(isAttribute = true, localName = "owner")
    private String owner;

    @JacksonXmlProperty(isAttribute = true, localName = "ownerName")
    private String ownerName;

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

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public Boolean getIsOdps() {
        return isOdps;
    }

    public void setIsOdps(Boolean odps) {
        isOdps = odps;
    }

    public Function() {
        super(DmObjectType.FUNCTION);
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

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFunctionType() {
        return functionType;
    }

    public void setFunctionType(String functionType) {
        this.functionType = functionType;
    }
}
