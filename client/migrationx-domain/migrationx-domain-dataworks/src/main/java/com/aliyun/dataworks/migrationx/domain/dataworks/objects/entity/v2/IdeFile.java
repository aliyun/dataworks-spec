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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author sam.liux
 * @date 2019/07/17
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "ideFile")
@Data
public class IdeFile {
    @JacksonXmlProperty(isAttribute = true, localName = "fileId")
    @JsonProperty("fileId")
    private String fileIdStr;
    @JacksonXmlProperty(isAttribute = true, localName = "nodeId")
    @JsonProperty("nodeId")
    private String nodeIdStr;
    @JsonProperty("nodeIdLong")
    private Long nodeId;
    @JsonProperty("fileIdLong")
    private Long fileId;
    @JacksonXmlProperty(isAttribute = true, localName = "bizId")
    @JsonProperty("bizId")
    private String bizIdStr;
    @JsonProperty("bizIdLong")
    private Long bizId;
    @JacksonXmlProperty(isAttribute = true, localName = "appId")
    private Long appId;
    @JacksonXmlProperty(isAttribute = true, localName = "fileFolderPath")
    private String fileFolderPath;
    @JacksonXmlProperty(isAttribute = true, localName = "fileName")
    private String fileName;
    @JacksonXmlProperty(isAttribute = true, localName = "fileDesc")
    private String fileDesc;
    @JacksonXmlProperty(isAttribute = true, localName = "fileType")
    private Integer fileType;
    @JacksonXmlProperty(isAttribute = true, localName = "fileDelete")
    private Integer fileDelete;
    @JacksonXmlProperty(isAttribute = true, localName = "isUserNode")
    private Integer isUserNode;
    @JacksonXmlProperty(isAttribute = true, localName = "folderPath")
    private String folderPath;
    @JacksonXmlProperty(isAttribute = true, localName = "labelName")
    private String labelName;
    @JacksonXmlProperty(isAttribute = true, localName = "useType")
    private Integer useType;
    @JacksonXmlProperty(isAttribute = true, localName = "bizUseType")
    private Integer bizUseType;
    @JacksonXmlProperty(isAttribute = true, localName = "commitStatus")
    private Integer commitStatus;
    @JacksonXmlProperty(isAttribute = true, localName = "cloudUuid")
    private Long cloudUuid;
    @JacksonXmlProperty(isAttribute = true, localName = "currentVersion")
    private Integer currentVersion;
    @JacksonXmlProperty(isAttribute = true, localName = "owner")
    private String owner;
    @JacksonXmlProperty(isAttribute = true, localName = "lastEditUser")
    private String lastEditUser;
    @JacksonXmlProperty(isAttribute = true, localName = "lastEditTime")
    private Date lastEditTime;
    @JacksonXmlProperty(isAttribute = true, localName = "fileLockUser")
    private String fileLockUser;
    @JacksonXmlProperty(isAttribute = true, localName = "fileLockStatus")
    private Integer fileLockStatus;
    @JacksonXmlProperty(isAttribute = true, localName = "originResourceName")
    private String originResourceName;
    @JacksonXmlProperty(isAttribute = true, localName = "isAutoParse")
    private Integer isAutoParse;
    @JacksonXmlProperty(isAttribute = true, localName = "isOdps")
    private Integer isOdps;
    @JacksonXmlProperty(isAttribute = true, localName = "connName")
    private String connName;
    @JacksonXmlProperty(isAttribute = true, localName = "sourcePath")
    private String sourcePath;
    @JacksonXmlProperty(isAttribute = true, localName = "isLarge")
    private Integer isLarge;
    @JacksonXmlProperty(localName = "nodeDef")
    private IdeNodeDef nodeDef;
    @JacksonXmlProperty(isAttribute = true, localName = "resGroupConverted")
    private Boolean resGroupConverted;
    @JacksonXmlProperty(isAttribute = true, localName = "diResGroupConverted")
    private Boolean diResGroupConverted;

    @JacksonXmlProperty(localName = "fileRelation")
    @JacksonXmlElementWrapper(localName = "fileRelations")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private List<IdeFileRelation> fileRelations = new ArrayList<>();

    @JacksonXmlProperty(localName = "nodeInputOutput")
    @JacksonXmlElementWrapper(localName = "nodeInputOutputs")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private List<IdeNodeInputOutput> nodeInputOutputs = new ArrayList<>();

    @JacksonXmlProperty(localName = "nodeInputOutputContext")
    @JacksonXmlElementWrapper(localName = "nodeInputOutputContexts")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private List<IdeNodeInputOutputContext> nodeInputOutputContexts = new ArrayList<>();

    @JacksonXmlProperty(isAttribute = true, localName = "outerFile")
    private String outerFile;

    @JacksonXmlProperty(localName = "innerFile")
    @JacksonXmlElementWrapper(localName = "innerFiles")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private List<IdeFile> innerFiles = new ArrayList<>();

    @JacksonXmlProperty(isAttribute = true, localName = "extend")
    private String extend;

    @JsonIgnore
    private String content;
}
