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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author sam.liux
 * @date 2019/12/12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "project")
public class Project {
    @JacksonXmlProperty(localName = "function")
    @JacksonXmlElementWrapper(localName = "functions")
    private List<Function> functions;

    @JacksonXmlProperty(localName = "node")
    @JacksonXmlElementWrapper(localName = "nodes")
    private List<Node> nodes;

    @JacksonXmlProperty(localName = "file")
    @JacksonXmlElementWrapper(localName = "resources")
    private List<Resource> resources;

    @JacksonXmlProperty(localName = "flow")
    @JacksonXmlElementWrapper(localName = "flows")
    private List<Flow> flows;
}
