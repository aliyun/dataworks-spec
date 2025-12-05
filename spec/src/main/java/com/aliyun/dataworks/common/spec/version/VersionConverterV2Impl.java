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

package com.aliyun.dataworks.common.spec.version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.enums.TriggerType;
import com.aliyun.dataworks.common.spec.domain.interfaces.LabelEnum;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFileResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFunction;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

import static com.aliyun.dataworks.common.spec.domain.enums.SpecKind.MANUAL_WORKFLOW;

/**
 * convert to flow spec 2.0
 *
 * @author 聿剑
 * @date 2025/9/7
 */
@Slf4j
public class VersionConverterV2Impl extends AbstractVersionConverter<DataWorksWorkflowSpec> {
    @Override
    public boolean support(SpecVersion sourceVersion, SpecVersion targetVersion) {
        return SpecVersion.V_2_0_0.equals(targetVersion);
    }

    @Override
    public Specification<DataWorksWorkflowSpec> convert(Specification<DataWorksWorkflowSpec> sourceSpecification) {
        Specification<DataWorksWorkflowSpec> specification = new Specification<>();
        specification.setVersion(SpecVersion.V_2_0_0.getLabel());
        SpecKind newSpecKind = convertKind(sourceSpecification);
        specification.setKind(newSpecKind.getLabel());
        specification.setSpec(convertSpec(sourceSpecification, newSpecKind));
        specification.setMetadata(sourceSpecification.getMetadata());
        return specification;
    }

    private DataWorksWorkflowSpec convertSpec(Specification<DataWorksWorkflowSpec> sourceSpecification, SpecKind newSpecKind) {
        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        switch (newSpecKind) {
            case COMPONENT: {
                spec.setComponents(sourceSpecification.getSpec().getComponents());
                break;
            }
            case RESOURCE: {
                spec.setFileResources(sourceSpecification.getSpec().getFileResources());
                break;
            }
            case FUNCTION: {
                spec.setFunctions(sourceSpecification.getSpec().getFunctions());
                break;
            }
            case CYCLE_WORKFLOW:
            case TRIGGER_WORKFLOW: {
                SpecRefEntity workflow = SpecUtil.getMatchIdSpecRefEntity(sourceSpecification, spec::setDependencies);
                if (workflow instanceof SpecWorkflow) {
                    // set inner nodes and dependencies to null because we only keep basic workflow properties for workflow itself in spec
                    ((SpecWorkflow)workflow).setDependencies(null);
                    ((SpecWorkflow)workflow).setNodes(null);
                    ((SpecWorkflow)workflow).setType(
                        Optional.ofNullable(((SpecWorkflow)workflow).getType()).orElse(sourceSpecification.getSpec().getType()));
                    spec.setWorkflows(List.of((SpecWorkflow)workflow));
                }
                spec.setNodes(null);
                break;
            }
            case MANUAL_WORKFLOW: {
                SpecRefEntity entity = SpecUtil.getMatchIdSpecRefEntity(sourceSpecification);
                if (entity instanceof SpecWorkflow) {
                    ((SpecWorkflow)entity).setDependencies(null);
                    ((SpecWorkflow)entity).setNodes(null);
                    SpecScript script = Optional.of(entity)
                        .map(SpecWorkflow.class::cast)
                        .map(SpecWorkflow::getScript)
                        .orElse(new SpecScript());
                    SpecScriptRuntime runtime = new SpecScriptRuntime();
                    runtime.setCommand(MANUAL_WORKFLOW.name());
                    script.setRuntime(runtime);
                    convertManualFlowVariables(sourceSpecification.getSpec(), script);
                    ((SpecWorkflow)entity).setScript(script);

                    Optional.ofNullable(sourceSpecification.getSpec())
                        .map(DataWorksWorkflowSpec::getStrategy)
                        .ifPresent(((SpecWorkflow)entity)::setStrategy);

                    SpecTrigger trigger = new SpecTrigger();
                    trigger.setType(TriggerType.MANUAL);
                    ((SpecWorkflow)entity).setTrigger(trigger);
                    spec.setWorkflows(Collections.singletonList((SpecWorkflow)entity));
                }
                spec.setNodes(null);
                spec.setDependencies(null);
                break;
            }
            case MANUAL_NODE:
            case NODE: {
                SpecRefEntity entity = SpecUtil.getMatchIdSpecRefEntity(sourceSpecification, spec::setDependencies);
                if (entity instanceof SpecNode) {
                    spec.setNodes(Collections.singletonList((SpecNode)entity));
                }
                break;
            }
            default:
                throw new RuntimeException("not supported kind: " + sourceSpecification.getKind());
        }

        // make sure and DataWorksWorkflowSpec basic properties reset to null
        spec.setId(null);
        spec.setName(null);
        spec.setType(null);
        spec.setOwner(null);
        spec.setStrategy(null);
        return spec;
    }

    private SpecKind convertKind(Specification<DataWorksWorkflowSpec> sourceSpecification) {
        return Optional.ofNullable(sourceSpecification).map(Specification::getSpec).map(spec -> {
            // SpecKind.COMPONENT
            if (SpecKind.COMPONENT.getLabel().equalsIgnoreCase(sourceSpecification.getKind())) {
                return SpecKind.COMPONENT;
            }

            // SpecKind.RESOURCE
            Optional<SpecFileResource> fileResource = ListUtils.emptyIfNull(spec.getFileResources()).stream().findAny();
            if (fileResource.isPresent()) {
                return SpecKind.RESOURCE;
            }

            // SpecKind.FUNCTION
            Optional<SpecFunction> function = ListUtils.emptyIfNull(spec.getFunctions()).stream().findAny();
            if (function.isPresent()) {
                return SpecKind.FUNCTION;
            }

            // SpecKind.MANUAL_NODE
            if (SpecKind.MANUAL_NODE.getLabel().equalsIgnoreCase(sourceSpecification.getKind())) {
                return SpecKind.MANUAL_NODE;
            }

            // SpecKind.MANUAL_WORKFLOW
            if (MANUAL_WORKFLOW.getLabel().equalsIgnoreCase(sourceSpecification.getKind())) {
                SpecRefEntity entity = SpecUtil.getMatchIdSpecRefEntity(sourceSpecification);
                if (entity instanceof SpecNode) {
                    return SpecKind.NODE;
                }

                return MANUAL_WORKFLOW;
            }

            // SpecKind.CYCLE_WORKFLOW
            // SpecKind.TRIGGER_WORKFLOW
            Optional<SpecWorkflow> workflow = ListUtils.emptyIfNull(spec.getWorkflows()).stream().findAny();
            if (workflow.isPresent()) {
                SpecRefEntity entity = SpecUtil.getMatchIdSpecRefEntity(sourceSpecification);
                if (entity instanceof SpecNode) {
                    return SpecKind.NODE;
                }

                return LabelEnum.getByLabel(SpecKind.class, sourceSpecification.getKind());
            }

            Optional<SpecNode> node = ListUtils.emptyIfNull(spec.getNodes()).stream().findAny();
            // node
            if (node.isPresent()) {
                return SpecKind.NODE;
            }

            throw new RuntimeException("not supported kind: " + sourceSpecification.getKind());
        }).orElseThrow(() -> new RuntimeException("no valid spec found"));
    }

    private void convertManualFlowVariables(DataWorksWorkflowSpec dataWorksWorkflowSpec, SpecScript specScript) {
        List<SpecVariable> variables = new ArrayList<>(ListUtils.emptyIfNull(specScript.getParameters()));
        variables.addAll(ListUtils.emptyIfNull(dataWorksWorkflowSpec.getVariables()));
        specScript.setParameters(variables.stream().distinct().collect(Collectors.toList()));
        dataWorksWorkflowSpec.setVariables(null);
    }
}
