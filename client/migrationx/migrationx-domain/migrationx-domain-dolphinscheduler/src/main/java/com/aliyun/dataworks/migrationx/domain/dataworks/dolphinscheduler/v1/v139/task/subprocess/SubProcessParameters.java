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
package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.subprocess;

import java.util.ArrayList;
import java.util.List;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.entity.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.AbstractParameters;

public class SubProcessParameters extends AbstractParameters {

    /**
     * process definition id
     */
    private Integer processDefinitionId;

    public void setProcessDefinitionId(Integer processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public Integer getProcessDefinitionId() {
        return this.processDefinitionId;
    }

    @Override
    public boolean checkParameters() {
        return this.processDefinitionId != null && this.processDefinitionId != 0;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }
}