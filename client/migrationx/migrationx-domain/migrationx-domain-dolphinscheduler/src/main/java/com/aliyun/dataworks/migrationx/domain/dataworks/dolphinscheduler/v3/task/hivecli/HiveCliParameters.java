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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.hivecli;

import java.util.List;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.parameters.AbstractParameters;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class HiveCliParameters extends AbstractParameters {

    private String hiveSqlScript;

    private String hiveCliTaskExecutionType;

    private String hiveCliOptions;

    private List<ResourceInfo> resourceList;

    @Override
    public boolean checkParameters() {
        if (!StringUtils.isNotEmpty(hiveCliTaskExecutionType)) {
            return false;
        }

        if (HiveCliConstants.TYPE_SCRIPT.equals(hiveCliTaskExecutionType)) {
            return StringUtils.isNotEmpty(hiveSqlScript);
        } else if (HiveCliConstants.TYPE_FILE.equals(hiveCliTaskExecutionType)) {
            return (resourceList != null) && (resourceList.size() > 0);
        } else {
            return false;
        }
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return this.resourceList;
    }
}
