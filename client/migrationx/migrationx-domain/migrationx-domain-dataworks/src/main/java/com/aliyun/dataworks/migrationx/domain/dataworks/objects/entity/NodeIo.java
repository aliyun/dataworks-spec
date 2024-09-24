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
 * @date 2019/04/17
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString(callSuper = true)
public class NodeIo extends DmObject {
    @JacksonXmlProperty(isAttribute = true, localName = "data")
    private String data;

    @JacksonXmlProperty(isAttribute = true, localName = "refTableName")
    private String refTableName;

    @JacksonXmlProperty(isAttribute = true, localName = "parseType")
    private Integer parseType;

    public NodeIo() {
        super(DmObjectType.NODE_IO);
    }

    public NodeIo(String data) {
        super(DmObjectType.NODE_IO);
        this.data = data;
    }

    public NodeIo(String data, String refTableName) {
        super(DmObjectType.NODE_IO);
        this.data = data;
        this.refTableName = refTableName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRefTableName() {
        return refTableName;
    }

    public void setRefTableName(String refTableName) {
        this.refTableName = refTableName;
    }

    public Integer getParseType() {
        return parseType;
    }

    public void setParseType(Integer parseType) {
        this.parseType = parseType;
    }
}
