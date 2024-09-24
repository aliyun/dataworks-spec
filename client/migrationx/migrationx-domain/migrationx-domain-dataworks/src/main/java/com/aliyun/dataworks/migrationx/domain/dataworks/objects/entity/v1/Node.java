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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author sam.liux
 * @date 2019/12/12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "node")
public class Node {
    @JacksonXmlProperty(localName = "connection", isAttribute = true)
    private String connection;
    @JacksonXmlProperty(localName = "description", isAttribute = true)
    private String description;
    @JacksonXmlProperty(localName = "display_name", isAttribute = true)
    private String displayName;
    /**
     * yyyy-MM-dd
     */
    @JacksonXmlProperty(localName = "end_effect_date", isAttribute = true)
    private Date endEffectDate;
    @JacksonXmlProperty(localName = "start_effect_date", isAttribute = true)
    private Date startEffectDate;
    @JacksonXmlProperty(localName = "file_path", isAttribute = true)
    private String filePath;
    @JacksonXmlProperty(localName = "folder_path", isAttribute = true)
    private String folderPath;
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;
    @JacksonXmlProperty(localName = "owner", isAttribute = true)
    private String owner;
    @JacksonXmlProperty(localName = "parents", isAttribute = true)
    private String parents;
    /**
     * normal, pause
     */
    @JacksonXmlProperty(localName = "run_type", isAttribute = true)
    private String runType;
    @JacksonXmlProperty(localName = "schedule_expression", isAttribute = true)
    private String scheduleExpression;
    /**
     * odps_sql, odps_mr
     */
    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private String type;

    @JacksonXmlProperty(localName = "is_root", isAttribute = true)
    private Boolean isRoot;

    @JacksonXmlProperty(localName = "extension")
    private Extension extension;
}
