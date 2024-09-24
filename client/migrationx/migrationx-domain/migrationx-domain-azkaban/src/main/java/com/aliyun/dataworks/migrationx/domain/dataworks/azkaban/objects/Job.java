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

package com.aliyun.dataworks.migrationx.domain.dataworks.azkaban.objects;

import lombok.Data;
import lombok.ToString;

import java.io.File;
import java.util.List;

/**
 * @author sam.liux
 * @date 2020/07/30
 */
@Data
@ToString
public abstract class Job {
    private String name;
    private File jobFile;
    private List<File> relatedFiles;
    @ConfigProperty(value = "type")
    private JobType type;
    @ConfigProperty(value = "dependencies")
    private List<String> dependencies;
    @ConfigProperty(value = "condition")
    private String condition;
    @ConfigProperty(value = "retries")
    private Integer retries;
    @ConfigProperty(value = "retry.backoff")
    private Integer retryBackOff;
    @ConfigProperty(value = "working.dir")
    private String workingDir;
    @ConfigProperty(value = "env.property")
    private String envProperty;
    @ConfigProperty(value = "failure.emails")
    private String failureEmails;
    @ConfigProperty(value = "success.emails")
    private String successEmails;
    @ConfigProperty(value = "notify.emails")
    private String notifyEmails;

    /**
     * 获取Job相关的文件
     */
    public void processJobRelativeFiles() {
    }

    /**
     * 拼装job的代码
     *
     * @return code
     */
    public abstract String getCode();
}
