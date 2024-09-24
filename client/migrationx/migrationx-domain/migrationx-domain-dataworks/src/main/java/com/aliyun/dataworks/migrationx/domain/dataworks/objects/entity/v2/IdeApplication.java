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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sam.liux
 * @date 2019/09/03
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "application")
@Data
public class IdeApplication {
    @JacksonXmlProperty(isAttribute = true, localName = "projectIdentifier")
    private String projectIdentifier;

    @JacksonXmlProperty(localName = "zipType")
    private IdeZipType zipType;

    @JacksonXmlProperty(localName = "solution")
    @JacksonXmlElementWrapper(localName = "solutions")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private List<IdeSolution> solutions = new ArrayList<>();

    @JacksonXmlProperty(localName = "bizInfo")
    @JacksonXmlElementWrapper(localName = "bizInfos")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private List<IdeBizInfo> bizInfos = new ArrayList<>();

    @JacksonXmlProperty(localName = "folder")
    @JacksonXmlElementWrapper(localName = "folders")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private List<IdeFolder> folders = new ArrayList<>();

    /**
     * 这里因为导出包的资源标签本身拼写错误，这里也写成reourceFilex
     */
    @JacksonXmlProperty(localName = "reourceFile")
    @JacksonXmlElementWrapper(localName = "resourceFiles")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private List<IdeFile> resourceFiles = new ArrayList<>();

    @JacksonXmlProperty(localName = "udfFile")
    @JacksonXmlElementWrapper(localName = "udfFiles")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private List<IdeFile> udfFiles = new ArrayList<>();

    @JacksonXmlProperty(localName = "otherFile")
    @JacksonXmlElementWrapper(localName = "otherFiles")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private List<IdeFile> otherFiles = new ArrayList<>();

    @JacksonXmlProperty(localName = "innerFile")
    @JacksonXmlElementWrapper(localName = "innerFiles")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private List<IdeFile> innerFiles = new ArrayList<>();

    @JacksonXmlProperty(localName = "engineInfo")
    private IdeEngineInfo engineInfo;

    @JacksonXmlProperty(localName = "version", isAttribute = true)
    private IdeExportVersion version;

    @JacksonXmlProperty(localName = "userDefinedNodes")
    @JacksonXmlElementWrapper(localName = "userDefinedNodes")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private List<IdeUserDefinedNode> userDefinedNodeList = new ArrayList<>();
}
