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

package com.aliyun.dataworks.common.spec.domain;

import java.util.Arrays;
import java.util.List;

import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecArtifact;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDatasource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDqcRule;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFile;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFileResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFunction;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecRuntimeResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.aliyun.dataworks.common.spec.domain.enums.SpecKind.BATCH_DEPLOYMENT;
import static com.aliyun.dataworks.common.spec.domain.enums.SpecKind.COMPONENT;
import static com.aliyun.dataworks.common.spec.domain.enums.SpecKind.CYCLE_WORKFLOW;
import static com.aliyun.dataworks.common.spec.domain.enums.SpecKind.DATASOURCE;
import static com.aliyun.dataworks.common.spec.domain.enums.SpecKind.DATA_CATALOG;
import static com.aliyun.dataworks.common.spec.domain.enums.SpecKind.DATA_QUALITY;
import static com.aliyun.dataworks.common.spec.domain.enums.SpecKind.MANUAL_NODE;
import static com.aliyun.dataworks.common.spec.domain.enums.SpecKind.MANUAL_WORKFLOW;
import static com.aliyun.dataworks.common.spec.domain.enums.SpecKind.PAIFLOW;
import static com.aliyun.dataworks.common.spec.domain.enums.SpecKind.TEMPORARY_WORKFLOW;

/**
 * @author yiwei.qyw
 * @date 2023/7/4
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataWorksWorkflowSpec extends SpecRefEntity implements Spec {
    private String name;
    private String type;
    private String owner;
    private String description;
    private List<SpecVariable> variables;
    private List<SpecTrigger> triggers;
    private List<SpecScript> scripts;
    private List<SpecFile> files;
    private List<SpecArtifact> artifacts;
    private List<SpecDatasource> datasources;
    private List<SpecDqcRule> dqcRules;
    private List<SpecRuntimeResource> runtimeResources;
    private List<SpecFileResource> fileResources;
    private List<SpecFunction> functions;
    private List<SpecNode> nodes;
    private List<SpecComponent> components;
    private List<SpecFlowDepend> flow;

    @Override
    public List<SpecKind> getKinds() {
        return Arrays.asList(
            CYCLE_WORKFLOW,
            MANUAL_WORKFLOW,
            MANUAL_NODE,
            TEMPORARY_WORKFLOW,
            PAIFLOW,
            BATCH_DEPLOYMENT,
            DATASOURCE,
            DATA_QUALITY,
            DATA_CATALOG,
            COMPONENT);
    }
}