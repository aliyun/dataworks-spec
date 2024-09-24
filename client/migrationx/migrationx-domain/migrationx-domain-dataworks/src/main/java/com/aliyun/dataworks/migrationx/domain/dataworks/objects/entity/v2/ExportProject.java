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

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.WorkflowFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author sam.liux
 * @date 2019/09/02
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString(callSuper = true)
public class ExportProject extends Project {
    @JacksonXmlProperty(isAttribute = true, localName = "regionId")
    private String regionId;
    @JacksonXmlProperty(isAttribute = true, localName = "exportMode")
    @NotBlank(message = "exportMode cannot be blank")
    private String exportMode = "full";
    @JacksonXmlProperty(isAttribute = true, localName = "versionName")
    @NotBlank(message = "versionName cannot be blank")
    private String versionName;
    @JacksonXmlProperty(isAttribute = true, localName = "version")
    @NotBlank(message = "version cannot be blank")
    private String version;
    @JacksonXmlProperty(isAttribute = true, localName = "cloudVersion")
    @NotNull(message = "cloudVersion cannot be blank")
    private Integer cloudVersion;
    private String localFilePath;
    @JacksonXmlProperty(localName = "WorkflowFilter")
    @JacksonXmlElementWrapper(localName = "WorkflowFilters")
    private List<WorkflowFilter> workflowFilters;

    public List<WorkflowFilter> getWorkflowFilters() {
        return workflowFilters;
    }

    public void setWorkflowFilters(List<WorkflowFilter> workflowFilters) {
        this.workflowFilters = workflowFilters;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public String getExportMode() {
        return exportMode;
    }

    public void setExportMode(String exportMode) {
        this.exportMode = exportMode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getCloudVersion() {
        return cloudVersion;
    }

    public void setCloudVersion(Integer cloudVersion) {
        this.cloudVersion = cloudVersion;
    }

    @Override
    public String toString() {
        return "ExportProject{" +
            "regionId='" + regionId + '\'' +
            ", exportMode='" + exportMode + '\'' +
            ", versionName='" + versionName + '\'' +
            ", version='" + version + '\'' +
            ", cloudVersion=" + cloudVersion +
            ", localFilePath='" + localFilePath + '\'' +
            '}';
    }
}
