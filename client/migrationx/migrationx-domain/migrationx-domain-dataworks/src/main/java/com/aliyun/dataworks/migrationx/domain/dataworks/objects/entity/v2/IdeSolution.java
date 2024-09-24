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

/**
 * @author sam.liux
 * @date 2019/09/03
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "solution")
@Data
public class IdeSolution {
    @JacksonXmlProperty(isAttribute = true, localName = "solutionName")
    private String solutionName;

    @JacksonXmlProperty(isAttribute = true, localName = "solDesc")
    private String solDesc;

    @JacksonXmlProperty(isAttribute = true, localName = "owner")
    private String owner;

    @JacksonXmlProperty(isAttribute = true, localName = "sourceApp")
    private String sourceApp;
}
