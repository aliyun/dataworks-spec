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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.filters;

import java.util.List;

import com.aliyun.migrationx.common.utils.Config;

import org.apache.commons.collections4.CollectionUtils;

/**
 * Check whether the task to convert
 */
public class DolphinSchedulerConverterFilter {
    private final List<String> filters;
    private final boolean skip;

    public DolphinSchedulerConverterFilter() {
        this.filters = Config.INSTANCE.getFilterTasks();
        if (CollectionUtils.isEmpty(filters) || (
                filters.size() == 1 && filters.get(0).equals("*"))) {
            skip = true;
        } else {
            skip = false;
        }
    }

    /**
     * 如果含有*，所有的都需要转换
     * 如果还有project.*,project下面全部转换
     * 如果还有project.process.* ,process 下面全部转换
     * 如果含有project.process.task, 单向匹配
     */
    public boolean filter(String projectName, String processName, String taskName) {
        if (skip) {
            return true;
        }
        String taskIdentity = String.format("%s.%s.%s", projectName, processName, taskName);
        if (filters.contains(taskIdentity)) {
            return true;
        }

        String processIdentity = String.format("%s.%s.*", projectName, processName);
        if (filters.contains(processIdentity)) {
            return true;
        }
        String projectIdentity = String.format("%s.*", projectName, processName);
        if (filters.contains(projectIdentity)) {
            return true;
        }
        return false;
    }
}
