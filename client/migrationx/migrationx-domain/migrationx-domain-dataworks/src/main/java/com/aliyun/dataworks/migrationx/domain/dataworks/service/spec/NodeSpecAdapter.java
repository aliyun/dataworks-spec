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

package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aliyun.dataworks.common.spec.adapter.SpecAdapter;
import com.aliyun.dataworks.common.spec.adapter.SpecHandlerContext;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.enums.ArtifactType;
import com.aliyun.dataworks.common.spec.domain.enums.DependencyType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecFileResourceType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.interfaces.LabelEnum;
import com.aliyun.dataworks.common.spec.domain.interfaces.NodeIO;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFileResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import com.aliyun.dataworks.common.spec.writer.WriterFactory;
import com.aliyun.dataworks.common.spec.writer.impl.SpecificationWriter;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Resource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.DependentType;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntity;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.handler.BasicNodeSpecHandler;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.handler.ComponentSpecHandler;

import com.alibaba.fastjson2.JSONWriter.Feature;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

/**
 * @author 聿剑
 * @date 2023/11/21
 */
@Slf4j
public class NodeSpecAdapter extends SpecAdapter<DwNodeEntity, SpecNode> {
    public NodeSpecAdapter() {
        registerHandlers();
        setDefaultHandler(BasicNodeSpecHandler.class);
    }

    private void registerHandlers() {
        Reflections reflections = new Reflections(BasicNodeSpecHandler.class.getPackage().getName());
        Set<Class<? extends BasicNodeSpecHandler>> handlerClasses = reflections.getSubTypesOf(BasicNodeSpecHandler.class);
        SetUtils.emptyIfNull(handlerClasses).stream()
                .filter(h -> !h.equals(BasicNodeSpecHandler.class))
                .forEach(this::registerHandler);
    }

    public String getNodeSpec(DwNodeEntity dmNodeBO, SpecHandlerContext context) {
        context.setSpecAdapter(this);
        SpecWriterContext specWriterContext = new SpecWriterContext();
        SpecificationWriter writer = (SpecificationWriter) WriterFactory.getWriter(Specification.class, specWriterContext);
        return writer.write(getNodeSpecObject(dmNodeBO, context), specWriterContext).toJSONString(Feature.PrettyFormat);
    }

    public Specification<DataWorksWorkflowSpec> getNodeSpecObject(DwNodeEntity dwNode, SpecHandlerContext context) {
        Preconditions.checkNotNull(dwNode, "node is null");
        context.setSpecAdapter(this);
        Specification<DataWorksWorkflowSpec> spec = new Specification<>();
        spec.setVersion(SpecVersion.V_1_1_0.getLabel());
        spec.setKind(Optional.ofNullable(dwNode.getNodeUseType()).map(useType -> {
            switch (useType) {
                case MANUAL:
                    return SpecKind.MANUAL_NODE.getLabel();
                case MANUAL_WORKFLOW:
                    return SpecKind.MANUAL_WORKFLOW.getLabel();
                case COMPONENT:
                    return SpecKind.COMPONENT.getLabel();
                default:
                    return SpecKind.CYCLE_WORKFLOW.getLabel();
            }
        }).orElse(SpecKind.CYCLE_WORKFLOW.getLabel()));
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("owner", dwNode.getOwner());
        spec.setMetadata(metadata);
        DataWorksWorkflowSpec dataWorksWorkflowSpec = new DataWorksWorkflowSpec();
        dataWorksWorkflowSpec.setId(Optional.ofNullable(dwNode.getBizId()).filter(id -> id > 0).map(String::valueOf).orElse(null));
        dataWorksWorkflowSpec.setName(Optional.ofNullable(dwNode.getBizName()).orElse(null));
        spec.setSpec(dataWorksWorkflowSpec);

        Optional.ofNullable(spec.getKind()).map(kind -> LabelEnum.getByLabel(SpecKind.class, kind)).ifPresent(kind -> {
            if (kind == SpecKind.COMPONENT) {
                ComponentSpecHandler handler = new ComponentSpecHandler();
                List<SpecComponent> nodes = new ArrayList<>();
                Optional.ofNullable(handler.handle(dwNode)).ifPresent(nodes::add);
                dataWorksWorkflowSpec.setComponents(nodes);
            } else {
                BasicNodeSpecHandler nodeSpecHandler = (BasicNodeSpecHandler) getHandler(dwNode, context.getLocale());
                nodeSpecHandler.setContext(context);
                dataWorksWorkflowSpec.setFlow(toFlow(nodeSpecHandler, dwNode, context));
                List<SpecNode> nodes = new ArrayList<>();
                Optional.ofNullable(nodeSpecHandler.handle(dwNode)).ifPresent(nodes::add);
                dataWorksWorkflowSpec.setNodes(nodes);
            }
        });
        return spec;
    }

    public Specification<DataWorksWorkflowSpec> getResourceSpecObject(Resource resource, SpecHandlerContext context) {
        Specification<DataWorksWorkflowSpec> spec = new Specification<>();
        spec.setVersion(SpecVersion.V_1_1_0.getLabel());
        spec.setKind(SpecKind.CYCLE_WORKFLOW.getLabel());
        Map<String, Object> metadata = new LinkedHashMap<>();
        spec.setMetadata(metadata);
        DataWorksWorkflowSpec dataWorksWorkflowSpec = new DataWorksWorkflowSpec();
        spec.setSpec(dataWorksWorkflowSpec);
        List<SpecFileResource> fileResources = new ArrayList<>();
        SpecFileResource fileResource = new SpecFileResource();
        fileResource.setName(resource.getName());
        SpecScript specScript = new SpecScript();
        if (resource.getFolder() != null) {
            String path = Joiner.on(File.separator).join(resource.getFolder(), resource.getName());
            specScript.setPath(path);
        } else {
            specScript.setPath(resource.getName());
        }

        SpecScriptRuntime runtime = new SpecScriptRuntime();
        runtime.setCommand(resource.getType());
        specScript.setRuntime(runtime);
        fileResource.setScript(specScript);
        fileResource.setType(SpecFileResourceType.valueOf(resource.getExtend()));
        fileResources.add(fileResource);
        dataWorksWorkflowSpec.setFileResources(fileResources);
        return spec;
    }

    public List<SpecFlowDepend> toFlow(BasicNodeSpecHandler handler, DwNodeEntity dmNodeBo, SpecHandlerContext context) {
        if (dmNodeBo == null) {
            return ListUtils.emptyIfNull(null);
        }

        SpecFlowDepend flowDepend = new SpecFlowDepend();
        SpecNode nodeId = new SpecNode();
        nodeId.setId(dmNodeBo.getUuid());

        flowDepend.setNodeId(nodeId);
        List<NodeIO> inputs = handler.getNodeInputs(dmNodeBo);
        flowDepend.setDepends(ListUtils.emptyIfNull(inputs).stream().map(in -> (SpecNodeOutput) in).map(dep -> {
            SpecDepend specDepend = new SpecDepend();
            specDepend.setType(DependencyType.NORMAL);
            SpecNodeOutput art = new SpecNodeOutput();
            art.setData(dep.getData());
            art.setArtifactType(ArtifactType.NODE_OUTPUT);
            art.setRefTableName(dep.getRefTableName());
            specDepend.setOutput(art);
            return specDepend;
        }).collect(Collectors.toList()));

        if (Stream.of(DependentType.USER_DEFINE, DependentType.USER_DEFINE_AND_SELF).anyMatch(
                dt -> Objects.equals(dmNodeBo.getDependentType(), dt.getValue()) && StringUtils.isNotBlank(dmNodeBo.getDependentDataNode()))) {
            Optional.ofNullable(StringUtils.split(dmNodeBo.getDependentDataNode(), ",")).map(Arrays::asList).orElse(new ArrayList<>()).stream().map(
                    out -> {
                        SpecDepend specDepend = new SpecDepend();
                        specDepend.setType(DependencyType.CROSS_CYCLE_OTHER_NODE);
                        SpecNodeOutput art = new SpecNodeOutput();
                        art.setData(out);
                        art.setArtifactType(ArtifactType.NODE_OUTPUT);
                        specDepend.setOutput(art);
                        return specDepend;
                    }).forEach(sp -> flowDepend.getDepends().add(sp));
        }

        if (Stream.of(DependentType.SELF, DependentType.USER_DEFINE_AND_SELF, DependentType.CHILD_AND_SELF).anyMatch(
                dt -> Objects.equals(dt.getValue(), dmNodeBo.getDependentType()))) {
            SpecDepend specDepend = new SpecDepend();
            specDepend.setType(DependencyType.CROSS_CYCLE_SELF);
            specDepend.setNodeId(nodeId);
            flowDepend.getDepends().add(specDepend);

            if (Objects.equals(DependentType.CHILD_AND_SELF.getValue(), dmNodeBo.getDependentType())) {
                SpecDepend sp = new SpecDepend();
                sp.setType(DependencyType.CROSS_CYCLE_CHILDREN);
                sp.setNodeId(nodeId);
                flowDepend.getDepends().add(sp);
            }
        }

        if (CollectionUtils.isEmpty(flowDepend.getDepends())) {
            return ListUtils.emptyIfNull(null);
        }

        return Collections.singletonList(flowDepend);
    }
}