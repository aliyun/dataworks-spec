
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

package com.aliyun.dataworks.common.spec.domain.ref;

import java.util.Collections;
import java.util.List;

import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.interfaces.Output;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.ListUtils;

/**
 * the top level concept of dataworks Workflow
 *
 * @author 聿剑
 * @date 2024/07/02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpecWorkflow extends SpecRefEntity implements Container, InputOutputWired, ScriptWired {
    @EqualsAndHashCode.Include
    private SpecScript script;

    @EqualsAndHashCode.Include
    private SpecTrigger trigger;

    @EqualsAndHashCode.Include
    private List<Input> inputs;

    @EqualsAndHashCode.Include
    private List<Output> outputs;

    @EqualsAndHashCode.Include
    private SpecScheduleStrategy strategy;

    @EqualsAndHashCode.Include
    private List<SpecNode> nodes;

    @EqualsAndHashCode.Include
    private List<SpecFlowDepend> dependencies;

    private String name;

    private String owner;

    private String description;

    @Override
    public List<SpecNode> getInnerNodes() {
        return Collections.unmodifiableList(ListUtils.emptyIfNull(nodes));
    }

    @Override
    public List<SpecFlowDepend> getInnerDependencies() {
        return Collections.unmodifiableList(ListUtils.emptyIfNull(dependencies));
    }
}