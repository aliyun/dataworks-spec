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

package com.aliyun.dataworks.migrationx.domain.dataworks.aliyunemr;

import java.util.List;
import java.util.Map;

import com.aliyuncs.emr.model.v20160408.ListFlowJobResponse;
import com.aliyuncs.emr.model.v20160408.ListFlowProjectResponse;
import com.aliyuncs.emr.model.v20160408.ListFlowResponse;
import lombok.Data;

/**
 * @author sam.liux
 * @date 2020/12/15
 */
@Data
public class AliyunEmrProject {
    private ListFlowProjectResponse.Project project;
    private Map<ListFlowResponse.FlowItem, Flow> flows;
    private List<ListFlowJobResponse.Job> jobs;

    public ListFlowJobResponse.Job getJobById(String jobId) {
        return this.jobs.stream().filter(
            job -> job.getId().equals(jobId)).findFirst().orElse(null);
    }
}
