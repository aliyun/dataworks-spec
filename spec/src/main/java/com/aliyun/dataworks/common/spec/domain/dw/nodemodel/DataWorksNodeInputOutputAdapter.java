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

package com.aliyun.dataworks.common.spec.domain.dw.nodemodel;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.enums.ArtifactType;
import com.aliyun.dataworks.common.spec.domain.enums.DependencyType;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.interfaces.Output;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecArtifact;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.google.common.base.Joiner;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataWorks调度节点输入输出及上下文参数适配
 *
 * @author 聿剑
 * @date 2023/11/9
 */
public class DataWorksNodeInputOutputAdapter {
    private static final Logger log = LoggerFactory.getLogger(DataWorksNodeInputOutputAdapter.class);

    protected final Specification<DataWorksWorkflowSpec> spec;
    protected final DataWorksWorkflowSpec specification;
    protected final SpecNode specNode;

    public DataWorksNodeInputOutputAdapter(Specification<DataWorksWorkflowSpec> specification, SpecNode specNode) {
        this.spec = specification;
        this.specification = this.spec.getSpec();
        this.specNode = specNode;
    }

    public List<Input> getInputs() {
        SpecNode outerNode = ListUtils.emptyIfNull(specification.getNodes()).stream()
            // current SpecNode is inner node of other node
            .filter(node -> ListUtils.emptyIfNull(node.getInnerNodes()).stream()
                .anyMatch(innerNode -> StringUtils.equals(innerNode.getId(), specNode.getId())))
            .findAny().orElse(null);
        if (outerNode != null) {
            return getInputList(outerNode.getInnerFlow(), outerNode.getInnerNodes(), specNode);
        }
        return getInputList(specification.getFlow(), specification.getNodes(), specNode);
    }

    private List<Input> getInputList(List<SpecFlowDepend> flow, List<SpecNode> allNodes, SpecNode node) {
        List<Input> inputs = ListUtils.emptyIfNull(node.getInputs()).stream()
            .filter(o -> o instanceof SpecNodeOutput)
            .map(o -> (SpecArtifact)o)
            .filter(o -> ArtifactType.NODE_OUTPUT.equals(o.getArtifactType()))
            .collect(Collectors.toList());

        Optional<SpecFlowDepend> specNodeFlowDepend = ListUtils.emptyIfNull(flow).stream()
            .filter(fd -> StringUtils.equalsIgnoreCase(node.getId(), fd.getNodeId().getId()))
            .peek(fd -> log.info("node flow depends source nodeId: {}, depends: {}",
                JSON.toJSONString(fd.getNodeId()), JSON.toJSONString(fd.getDepends())))
            .findFirst();

        specNodeFlowDepend
            .map(SpecFlowDepend::getDepends)
            .orElse(ListUtils.emptyIfNull(null))
            .stream()
            .filter(dep -> DependencyType.NORMAL.equals(dep.getType()))
            .filter(dep -> dep.getOutput() == null || StringUtils.isBlank(dep.getOutput().getData()))
            .filter(dep -> dep.getNodeId() != null)
            .map(out -> ListUtils.emptyIfNull(allNodes).stream().filter(n -> StringUtils.equals(out.getNodeId().getId(), n.getId()))
                .findAny().flatMap(depNode -> depNode.getOutputs().stream()
                    .filter(o -> o instanceof SpecNodeOutput).map(o -> (SpecNodeOutput)o).findAny())
                .map(output -> {
                    SpecNodeOutput io = new SpecNodeOutput();
                    io.setData(output.getData());
                    io.setRefTableName(output.getRefTableName());
                    return io;
                }).orElse(null))
            .filter(Objects::nonNull).forEach(inputs::add);

        specNodeFlowDepend
            .map(SpecFlowDepend::getDepends)
            .orElse(ListUtils.emptyIfNull(null))
            .stream()
            .filter(dep -> DependencyType.NORMAL.equals(dep.getType()))
            .map(SpecDepend::getOutput)
            .filter(Objects::nonNull)
            .map(out -> {
                SpecNodeOutput io = new SpecNodeOutput();
                io.setData(out.getData());
                io.setRefTableName(out.getRefTableName());
                return io;
            }).forEach(inputs::add);
        return inputs;
    }

    public List<Output> getOutputs() {
        return ListUtils.emptyIfNull(specNode.getOutputs()).stream()
            .filter(o -> o instanceof SpecArtifact)
            .map(o -> (SpecArtifact)o)
            .filter(o -> ArtifactType.NODE_OUTPUT.equals(o.getArtifactType()))
            .collect(Collectors.toList());
    }

    public List<InputContext> getInputContexts() {
        return ListUtils.emptyIfNull(specNode.getInputs()).stream()
            .filter(i -> i instanceof SpecArtifact)
            .filter(i -> ArtifactType.VARIABLE.equals(((SpecArtifact)i).getArtifactType()))
            .map(i -> (SpecVariable)i)
            .map(i -> {
                InputContext inCtx = new InputContext();
                inCtx.setKey(getInputContextKey(i));
                inCtx.setRefKey(getIoContextRefKey(i, false));
                return inCtx;
            }).collect(Collectors.toList());
    }

    private String getInputContextKey(SpecVariable i) {
        return Optional.ofNullable(specNode.getScript()).map(SpecScript::getParameters)
            .map(params -> params.stream()
                .filter(param -> param.getReferenceVariable() != null)
                .filter(param -> matchVariable(i, param.getReferenceVariable()))
                .map(SpecVariable::getName).findAny()
                .orElseThrow(() -> new SpecException(SpecErrorCode.PARSE_ERROR,
                    "inputs variable missing binding in script.parameters: " + i.getName())))
            .orElseThrow(() -> new SpecException(SpecErrorCode.PARSE_ERROR,
                "inputs variable missing binding in script.parameters: " + i.getName()));
    }

    private static boolean matchVariable(SpecVariable varA, SpecVariable varB) {
        if (!StringUtils.isBlank(varA.getId()) && !StringUtils.isBlank(varB.getId())) {
            return StringUtils.equals(varA.getId(), varB.getId());
        }

        if (varA.getNode() != null && varB.getNode() != null) {
            if (varA.getNode().getOutput() != null && varB.getNode().getOutput() != null) {
                return StringUtils.equalsIgnoreCase(varA.getNode().getOutput().getData(), varB.getNode().getOutput().getData()) &&
                    StringUtils.equals(varA.getName(), varB.getName());
            }

            if (varA.getNode().getNodeId() != null && varB.getNode().getNodeId() != null) {
                return StringUtils.equals(varA.getNode().getNodeId().getId(), varB.getNode().getNodeId().getId()) &&
                    StringUtils.equals(varA.getName(), varB.getName());
            }
        }

        log.warn("variable identifier missing, cannot compare and match variable: {} and variable: {}", varA, varB);
        throw new SpecException(SpecErrorCode.PARSE_ERROR,
            "variable identifier missing, cannot compare and match variable: " + varA + " and variable: " + varB);
    }

    private static String getIoContextRefKey(SpecVariable i, boolean isOutput) {
        return Optional.ofNullable(i.getReferenceVariable()).map(refVar -> {
            String varName = Optional.ofNullable(refVar.getName()).orElseThrow(() ->
                new SpecException(SpecErrorCode.PARSE_ERROR, "context variable field error, missing 'variable.referenceVariable.name'"));
            String output = Optional.ofNullable(refVar.getNode()).map(SpecDepend::getOutput).map(SpecNodeOutput::getData).orElseThrow(() ->
                new SpecException(SpecErrorCode.PARSE_ERROR, "context variable field error, missing 'variable.referenceVariable.node.output'"));
            return Joiner.on(":").join(output, varName);
        }).orElseGet(() -> {
            if (BooleanUtils.isTrue(isOutput)) {
                return i.getValue();
            }

            String output = Optional.ofNullable(i.getNode()).map(SpecDepend::getOutput).map(SpecNodeOutput::getData).orElseThrow(() ->
                new SpecException(SpecErrorCode.PARSE_ERROR, "context variable field error, missing 'variable.node.output'"));
            return Joiner.on(":").join(output, i.getName());
        });
    }

    public List<OutputContext> getOutputContexts() {
        return ListUtils.emptyIfNull(specNode.getOutputs()).stream()
            .filter(i -> i instanceof SpecArtifact)
            .filter(i -> ArtifactType.VARIABLE.equals(((SpecArtifact)i).getArtifactType()))
            .map(i -> (SpecVariable)i)
            .map(i -> {
                OutputContext outCtx = new OutputContext();
                outCtx.setKey(i.getName());
                switch (i.getType()) {
                    case CONSTANT: {
                        outCtx.setCtxType(OutputContext.CTX_TYPE_CONST);
                        outCtx.setValueExpr(i.getValue());
                        break;
                    }
                    case SYSTEM: {
                        outCtx.setCtxType(OutputContext.CTX_TYPE_CONST_SYSTEM_VARIABLE);
                        outCtx.setValueExpr(i.getValue());
                        break;
                    }
                    case NODE_OUTPUT: {
                        outCtx.setCtxType(OutputContext.CTX_TYPE_SCRIPT_OUTPUTS);
                        outCtx.setValueExpr(i.getValue());
                        break;
                    }
                    case PASS_THROUGH: {
                        outCtx.setCtxType(OutputContext.CTX_TYPE_PARAMETER_NODE_OUTPUTS);
                        outCtx.setValueExpr(getIoContextRefKey(i.getReferenceVariable(), true));
                        break;
                    }
                }
                return outCtx;
            }).collect(Collectors.toList());
    }
}