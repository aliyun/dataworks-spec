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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.tis;

import java.util.Collections;
import java.util.List;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.process.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.AbstractParameters;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TIS parameter
 */
public class PigeonCommonParameters extends AbstractParameters {

    private static final Logger logger = LoggerFactory.getLogger(PigeonCommonParameters.class);
    /**
     * TIS target job name
     */
    private String jobName;

    public String getTargetJobName() {
        return jobName;
    }

    public void setTargetJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotBlank(this.jobName);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return Collections.emptyList();
    }
}
