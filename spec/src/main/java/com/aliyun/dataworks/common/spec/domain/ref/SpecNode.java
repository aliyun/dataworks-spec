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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.enums.NodeInstanceModeType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRecurrenceType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRerunModeType;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.interfaces.Output;
import com.aliyun.dataworks.common.spec.domain.noref.SpecBranch;
import com.aliyun.dataworks.common.spec.domain.noref.SpecCombined;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDoWhile;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecForEach;
import com.aliyun.dataworks.common.spec.domain.noref.SpecJoin;
import com.aliyun.dataworks.common.spec.domain.noref.SpecNodeRef;
import com.aliyun.dataworks.common.spec.domain.noref.SpecParamHub;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.ListUtils;

/**
 * @author yiwei.qyw
 * @date 2023/7/4
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpecNode extends SpecRefEntity implements ContainerNode {
    private NodeRecurrenceType recurrence;

    private Integer priority;

    private Integer timeout;

    private NodeInstanceModeType instanceMode;

    private NodeRerunModeType rerunMode;

    private Integer rerunTimes;

    private Integer rerunInterval;

    /**
     * 是否忽略分支条件跳过
     */
    private Boolean ignoreBranchConditionSkip;

    private SpecDatasource datasource;

    private SpecScript script;

    private SpecTrigger trigger;

    private SpecRuntimeResource runtimeResource;

    private List<SpecFileResource> fileResources;

    private List<SpecFunction> functions;

    private List<Input> inputs;

    private List<Output> outputs;

    private SpecNodeRef reference;

    private SpecBranch branch;

    private SpecJoin join;

    private SpecDoWhile doWhile;

    private SpecForEach foreach;

    private SpecCombined combined;

    private SpecParamHub paramHub;

    private String name;

    private String owner;

    private String description;

    @Override
    public List<SpecNode> getInnerNodes() {
        List<SpecNode> nodes = new ArrayList<>();
        Optional.ofNullable(doWhile).ifPresent(dw -> {
            Optional.ofNullable(dw.getSpecWhile()).ifPresent(nodes::add);
            nodes.addAll(ListUtils.emptyIfNull(dw.getNodes()));
        });

        Optional.ofNullable(foreach).ifPresent(fe -> nodes.addAll(ListUtils.emptyIfNull(foreach.getNodes())));
        Optional.ofNullable(combined).ifPresent(cb -> nodes.addAll(ListUtils.emptyIfNull(cb.getNodes())));
        return Collections.unmodifiableList(nodes);
    }

    @Override
    public List<SpecFlowDepend> getInnerFlow() {
        if (doWhile != null) {
            return doWhile.getFlow();
        }

        if (foreach != null) {
            return foreach.getFlow();
        }

        if (combined != null) {
            return combined.getFlow();
        }

        return null;
    }
}