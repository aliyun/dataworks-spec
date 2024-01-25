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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.ToString;

/**
 * @author sam.liux
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "nodeInputOutput")
@Data
@ToString(callSuper = true)
public class IdeNodeInputOutput {
    @JacksonXmlProperty(localName = "parentId", isAttribute = true)
    private String parentId;

    @JacksonXmlProperty(localName = "isDifferentApp", isAttribute = true)
    private Boolean isDifferentApp;

    @JacksonXmlProperty(localName = "parentFileDelete", isAttribute = true)
    private String parentFileDelete;

    @JacksonXmlProperty(isAttribute = true, localName = "parentFileFolderPath")
    private String parentFileFolderPath;

    @JacksonXmlProperty(isAttribute = true, localName = "parentOuterFileDelete")
    private Integer parentOuterFileDelete;

    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private Integer type;
    /**
     * 输入或者输出String
     */
    @JacksonXmlProperty(localName = "str", isAttribute = true)
    private String str;

    /**
     * 手动或者自动解析类型
     */
    @JacksonXmlProperty(localName = "parseType", isAttribute = true)
    private Integer parseType;

    /**
     * 关联的表名
     */
    @JacksonXmlProperty(localName = "refTableName", isAttribute = true)
    private String refTableName;

    @JacksonXmlProperty(localName = "nodeId", isAttribute = true)
    private Long nodeId;
}