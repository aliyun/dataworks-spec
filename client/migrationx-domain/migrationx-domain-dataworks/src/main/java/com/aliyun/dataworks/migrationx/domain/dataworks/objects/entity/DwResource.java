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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;

import java.util.UUID;

/**
 * @author sam.liux
 * @date 2019/07/16
 */
@ToString(callSuper = true, exclude = {"workflowRef"})
public class DwResource extends Resource {
    private String localPath;
    private Long cloudUuid;
    private Long fileId;
    @JsonIgnore
    private transient DwWorkflow workflowRef;
    private Boolean autoCommit = false;
    private Boolean autoDeploy = false;

    public Boolean getAutoDeploy() {
        return autoDeploy;
    }

    public void setAutoDeploy(Boolean autoDeploy) {
        this.autoDeploy = autoDeploy;
    }

    public Boolean getAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(Boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public DwWorkflow getWorkflowRef() {
        return workflowRef;
    }

    public void setWorkflowRef(DwWorkflow workflowRef) {
        this.workflowRef = workflowRef;
    }

    public Long getCloudUuid() {
        return cloudUuid;
    }

    public void setCloudUuid(Long cloudUuid) {
        this.cloudUuid = cloudUuid;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    @Override
    public String getUniqueKey() {
        String str = getName();
        return UUID.nameUUIDFromBytes(str.getBytes()).toString();
    }
}
