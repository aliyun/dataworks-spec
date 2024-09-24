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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.ToString;

import java.util.UUID;

/**
 * @author sam.liux
 * @date 2019/12/19
 */
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uniqueIdentity", "dmObjectUuid", "uniqueKey", "uniqueId"})
public class DmObject {
    @JacksonXmlProperty(localName = "dmObjectType", isAttribute = true)
    private DmObjectType dmObjectType;
    @JacksonXmlProperty(localName = "dmMajorVersion", isAttribute = true)
    private String dmMajorVersion;
    @JacksonXmlProperty(localName = "dmMinorVersion", isAttribute = true)
    private String dmMinorVersion;
    @JacksonXmlProperty(localName = "dmObjectUuid", isAttribute = true)
    private String dmObjectUuid = UUID.randomUUID().toString();
    @JacksonXmlProperty(localName = "globalUuid", isAttribute = true)
    private String globalUuid;
    private DmObjectUniqueIdentity uniqueIdentity;
    private Long uniqueId;

    public DmObject(DmObjectType dmObjectType) {
        this.dmObjectType = dmObjectType;
    }

    /**
     * 全局唯一的，有业务含义的key
     * @return
     */
    @JacksonXmlProperty(localName = "uniqueKey", isAttribute = true)
    public String getUniqueKey() {
        return dmObjectUuid;
    }

    public DmObjectType getDmObjectType() {
        return dmObjectType;
    }

    public void setDmObjectType(DmObjectType dmObjectType) {
        this.dmObjectType = dmObjectType;
    }

    public String getDmMajorVersion() {
        return dmMajorVersion;
    }

    public void setDmMajorVersion(String dmMajorVersion) {
        this.dmMajorVersion = dmMajorVersion;
    }

    public String getDmMinorVersion() {
        return dmMinorVersion;
    }

    public void setDmMinorVersion(String dmMinorVersion) {
        this.dmMinorVersion = dmMinorVersion;
    }

    public String getDmObjectUuid() {
        return dmObjectUuid;
    }

    public void setDmObjectUuid(String dmObjectUuid) {
        this.dmObjectUuid = dmObjectUuid;
    }

    public DmObjectUniqueIdentity getUniqueIdentity() {
        return uniqueIdentity;
    }

    public DmObject setUniqueIdentity(DmObjectUniqueIdentity uniqueIdentity) {
        this.uniqueIdentity = uniqueIdentity;
        return this;
    }

    public String getGlobalUuid() {
        return globalUuid;
    }

    public DmObject setGlobalUuid(String globalUuid) {
        this.globalUuid = globalUuid;
        return this;
    }

    public Long getUniqueId() {
        return uniqueId;
    }

    public DmObject setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
        return this;
    }
}
