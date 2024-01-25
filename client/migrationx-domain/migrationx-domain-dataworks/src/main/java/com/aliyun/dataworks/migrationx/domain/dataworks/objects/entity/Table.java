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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.common.base.Joiner;
import lombok.ToString;

import java.util.UUID;

/**
 * @author sam.liux
 * @date 2019/04/17
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString(callSuper = true)
public class Table extends DmObject {
    public Table() {
        super(DmObjectType.TABLE);
    }

    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;

    @JacksonXmlCData
    @JsonSerialize(converter = CodeControlCharEscapeConverter.class)
    @JsonDeserialize(converter = CodeControlCharUnEscapeConverter.class)
    private String ddl;

    @JacksonXmlProperty(isAttribute = true, localName = "ddlPath")
    private String ddlPath;

    /**
     * 是否分区
     */
    @JacksonXmlProperty(isAttribute = true, localName = "hasPart")
    private Integer hasPart;
    /**
     * 是否可见
     */
    @JacksonXmlProperty(isAttribute = true, localName = "isVisible")
    private Integer isVisible;
    /**
     * 不存在则创建
     */
    @JacksonXmlProperty(isAttribute = true, localName = "createIfNotExists")
    private Integer createIfNotExists;
    /**
     * 主题
     */
    @JacksonXmlProperty(isAttribute = true, localName = "themes")
    private String themes;
    /**
     * table guid
     */
    @JacksonXmlProperty(isAttribute = true, localName = "tableGuid")
    private String tableGuid;
    /**
     * APP guid
     */
    @JacksonXmlProperty(isAttribute = true, localName = "appGuid")
    private String appGuid;
    /**
     * source type
     */
    @JacksonXmlProperty(isAttribute = true, localName = "srcType")
    private Integer srcType;
    /**
     * owner id
     */
    @JacksonXmlProperty(isAttribute = true, localName = "ownerId")
    private String ownerId;
    /**
     * tenant id
     */
    @JacksonXmlProperty(isAttribute = true, localName = "tenantId")
    private Long tenantId;

    @JacksonXmlProperty(isAttribute = true, localName = "owner")
    private String owner;

    @JacksonXmlProperty(isAttribute = true, localName = "ownerName")
    private String ownerName;

    @Override
    public String getUniqueKey() {
        String str = Joiner.on("#").join(appGuid, tableGuid);
        return UUID.nameUUIDFromBytes(str.getBytes()).toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDdl() {
        return ddl;
    }

    public void setDdl(String ddl) {
        this.ddl = ddl;
    }

    public String getDdlPath() {
        return ddlPath;
    }

    public void setDdlPath(String ddlPath) {
        this.ddlPath = ddlPath;
    }

    public Integer getHasPart() {
        return hasPart;
    }

    public void setHasPart(Integer hasPart) {
        this.hasPart = hasPart;
    }

    public Integer getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Integer isVisible) {
        this.isVisible = isVisible;
    }

    public Integer getCreateIfNotExists() {
        return createIfNotExists;
    }

    public void setCreateIfNotExists(Integer createIfNotExists) {
        this.createIfNotExists = createIfNotExists;
    }

    public String getThemes() {
        return themes;
    }

    public void setThemes(String themes) {
        this.themes = themes;
    }

    public String getTableGuid() {
        return tableGuid;
    }

    public void setTableGuid(String tableGuid) {
        this.tableGuid = tableGuid;
    }

    public String getAppGuid() {
        return appGuid;
    }

    public void setAppGuid(String appGuid) {
        this.appGuid = appGuid;
    }

    public Integer getSrcType() {
        return srcType;
    }

    public void setSrcType(Integer srcType) {
        this.srcType = srcType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
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
