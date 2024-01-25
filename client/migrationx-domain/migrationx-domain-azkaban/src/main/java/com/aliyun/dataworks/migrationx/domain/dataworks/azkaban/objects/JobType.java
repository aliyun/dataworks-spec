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

/**
 * @author sam.liux
 * @date 2020/07/30
 */
public enum JobType {
    /**
     * noop
     */
    noop,

    /**
     * command
     */
    command,

    /**
     * hive
     */
    hive,

    /**
     * flow 嵌套类型
     */
    flow,

    /**
     * 未知类型
     */
    unknown;

    public static JobType getByName(String type) {
        for (JobType jobType : values()) {
            if (jobType.name().equalsIgnoreCase(type)) {
                return jobType;
            }
        }
        return unknown;
    }

    public static Job newJobInstance(JobType jobType) {
        Job job = null;
        switch (jobType) {
            case hive:
                job = new HiveJob();
                break;
            case command:
                job = new CommandJob();
                break;
            case noop:
                job = new NoopJob();
                break;
            case unknown:
            default: {
                job = new Job() {
                    @Override
                    public String getCode() {
                        return null;
                    }
                };
            }
        }

        job.setType(jobType);
        return job;
    }
}
