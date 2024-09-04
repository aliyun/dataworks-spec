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

package com.aliyun.dataworks.common.spec.domain.ref.runtime;

import java.util.Map;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.container.SpecContainer;
import com.aliyun.dataworks.common.spec.domain.SpecNoRefEntity;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yiwei.qyw
 * @date 2023/7/4
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpecScriptRuntime extends SpecNoRefEntity {
    private String engine;
    /**
     * code program type
     *
     * @see com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType
     */
    private String command;
    /**
     * command type id
     *
     * @see CodeProgramType#getCode()
     */
    private Integer commandTypeId;

    private Map<String, Object> template;
    /**
     * dataworks official emr job config
     * <a href="https://help.aliyun.com/zh/dataworks/user-guide/create-an-emr-spark-node?spm=a2c4g.11186623.0.0.5b997a6c6eiCoN#1983900a620np"></a>
     */
    private Map<String, Object> emrJobConfig;
    /**
     * cdh job config
     */
    private Map<String, Object> cdhJobConfig;
    /**
     * spark configurations for emr job
     * <a href="https://spark.apache.org/docs/latest/configuration.html#application-properties">Spark Configuration</a>
     */
    private Map<String, Object> sparkConf;
    /**
     * flink configurations
     */
    private Map<String, Object> flinkConf;

    /**
     * runtime container info
     */
    private SpecContainer container;
}