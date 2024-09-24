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
import lombok.ToString;

/**
 * @author sam.liux
 * @date 2020/01/04
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString(callSuper = true)
public class UserDefinedNode extends DmObject{
    @JacksonXmlProperty(localName = "id", isAttribute = true)
    private Long id;

    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;

    @JacksonXmlProperty(isAttribute = true, localName = "owner")
    private String owner;

    @JacksonXmlProperty(isAttribute = true, localName = "ownerName")
    private String ownerName;

    public UserDefinedNode() {
        super(DmObjectType.USER_DEFINED_NODE);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
