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

package com.aliyun.dataworks.common.spec.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter.Feature;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Spec;
import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.interfaces.Output;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * copy flow spec, and generate a new spec, discard the ids from original spec,
 * needs to do some id and output mapping replacement
 *
 * @author 聿剑
 * @date 2025/6/13
 */
@Getter
@Slf4j
public class FlowSpecCopier {
    private static final String COPY_SUFFIX_START = "CPS";
    private static final String COPY_SUFFIX_END = "CPE";
    protected static final Pattern COPY_SUFFIX_PATTERN = Pattern.compile(COPY_SUFFIX_START + "[a-zA-Z0-9]{6}" + COPY_SUFFIX_END);

    private final Map<String, String> uuidMapping = new HashMap<>();
    private final Map<String, String> outputMapping = new HashMap<>();

    public Specification<Spec> copy(Specification<Spec> sourceSpec) {
        if (sourceSpec == null) {
            log.info("sourceSpec is null");
            return null;
        }

        Specification<Spec> deepCopy = SpecUtil.parseToDomain(SpecUtil.writeToSpec(sourceSpec));
        uuidMapping.clear();
        outputMapping.clear();
        return doSpecCopy(deepCopy);
    }

    private Specification<Spec> doSpecCopy(Specification<Spec> spec) {
        return Optional.ofNullable(spec.getSpec()).filter(sp -> sp instanceof DataWorksWorkflowSpec)
            .map(sp -> (DataWorksWorkflowSpec)sp)
            .map(dwSpec -> {
                adaptManualWorkflow(spec.getKind(), dwSpec);

                Optional.ofNullable(dwSpec.getWorkflows())
                    .flatMap(wfs -> ListUtils.emptyIfNull(dwSpec.getWorkflows()).stream().findFirst())
                    .ifPresent(workflow -> {
                        processSpecWorkflow(spec.getKind(), workflow);
                        processSpecNodeUuidMapping(workflow.getNodes());
                        processSpecNodes(spec.getKind(), workflow, workflow.getNodes());
                        processSpecDependencies(workflow.getDependencies());
                    });
                Optional.ofNullable(dwSpec.getNodes()).ifPresent(nodes -> {
                    processSpecNodeUuidMapping(nodes);
                    processSpecNodes(spec.getKind(), null, nodes);
                });
                Optional.ofNullable(dwSpec.getFlow()).ifPresent(this::processSpecDependencies);
                resetDwSpec(dwSpec);
                spec.setMetadata(null);
                String copiedSpec = SpecUtil.writeToSpec(spec);
                log.info("copied spec: {}", copiedSpec);
                return SpecUtil.parseToDomain(copiedSpec);
            })
            .orElseThrow(() -> new RuntimeException("copy failed"));
    }

    private void processSpecNodeUuidMapping(List<SpecNode> specNodes) {
        ListUtils.emptyIfNull(specNodes).forEach(specNode -> {
            String newId = UuidUtils.genUuidWithoutHorizontalLine();
            String oldId = specNode.getId();
            uuidMapping.put(oldId, newId);
            outputMapping.put(oldId, newId);
            Optional.ofNullable(specNode.getInnerNodes()).ifPresent(this::processSpecNodeUuidMapping);
        });
    }

    private void processSpecWorkflow(String specKind, SpecWorkflow workflow) {
        String newFlowId = UuidUtils.genUuidWithoutHorizontalLine();
        String oldFlowId = workflow.getId();
        uuidMapping.put(oldFlowId, newFlowId);
        workflow.setId(newFlowId);
        workflow.setMetadata(null);
        ListUtils.emptyIfNull(workflow.getOutputs()).forEach(o -> processOutput(o, oldFlowId, newFlowId, specKind));
        ListUtils.emptyIfNull(workflow.getInputs()).forEach(this::processInput);
        Optional.ofNullable(workflow.getScript()).ifPresent(this::processScript);
    }

    private void processScript(SpecScript script) {
        script.setId(null);
        ListUtils.emptyIfNull(script.getParameters()).forEach(var -> {
            processInput(var);
            var.setId(null);
            Optional.ofNullable(var.getReferenceVariable()).ifPresent(refVar -> {
                if (VariableType.PASS_THROUGH.equals(var.getType())) {
                    refVar.setType(var.getType());
                }
                this.processInput(refVar);
            });
        });
    }

    private void adaptManualWorkflow(String specKind, DataWorksWorkflowSpec dwSpec) {
        // adapt ManualWorkflow spec structure normalization to CycleWorkflow spec
        if (SpecKind.MANUAL_WORKFLOW.getLabel().equalsIgnoreCase(specKind) && CollectionUtils.isEmpty(dwSpec.getWorkflows())) {
            SpecWorkflow specWorkflow = new SpecWorkflow();
            specWorkflow.setId(dwSpec.getId());
            specWorkflow.setName(dwSpec.getName());
            specWorkflow.setOwner(dwSpec.getOwner());
            specWorkflow.setDescription(dwSpec.getDescription());
            specWorkflow.setType(dwSpec.getType());
            SpecScript script = new SpecScript();
            script.setPath("/");
            SpecScriptRuntime runtime = new SpecScriptRuntime();
            runtime.setCommand("MANUAL_WORKFLOW");
            script.setRuntime(runtime);
            script.setParameters(dwSpec.getVariables());
            specWorkflow.setScript(script);
            specWorkflow.setNodes(dwSpec.getNodes());
            specWorkflow.setDependencies(dwSpec.getFlow());
            dwSpec.setWorkflows(List.of(specWorkflow));
            dwSpec.setFlow(null);
            dwSpec.setNodes(null);
        }
    }

    private void resetDwSpec(DataWorksWorkflowSpec dwSpec) {
        dwSpec.setId(null);
        dwSpec.setName(null);
        dwSpec.setType(null);
        dwSpec.setOwner(null);
        dwSpec.setDescription(null);
        dwSpec.setVariables(null);
        dwSpec.setMetadata(null);
    }

    private void processSpecDependencies(List<SpecFlowDepend> dependencies) {
        ListUtils.emptyIfNull(dependencies).forEach(dependency -> {
            Optional.ofNullable(dependency.getNodeId()).filter(node -> uuidMapping.containsKey(node.getId())).ifPresent(node -> {
                log.info("replace dependency.nodeId: {}, to new nodeId: {}", node.getId(), uuidMapping.get(node.getId()));
                node.setId(uuidMapping.get(node.getId()));
            });
            ListUtils.emptyIfNull(dependency.getDepends()).forEach(depend -> {
                Optional.ofNullable(depend.getOutput()).filter(out -> outputMapping.containsKey(out.getData())).ifPresent(output -> {
                    log.info("replace dependency.depends.output: {}, to new output: {}", depend.getOutput(), outputMapping.get(output.getData()));
                    output.setData(outputMapping.get(output.getData()));
                });
                Optional.ofNullable(depend.getNodeId()).filter(node -> uuidMapping.containsKey(node.getId())).ifPresent(nodeId -> {
                    log.info("replace dependency.depends.nodeId: {}, to new nodeId: {}", depend.getOutput(), uuidMapping.get(nodeId.getId()));
                    nodeId.setId(uuidMapping.get(nodeId.getId()));
                });
            });
        });
    }

    private void processSpecNodes(String specKind, SpecWorkflow workflow, List<SpecNode> specNodes) {
        ListUtils.emptyIfNull(specNodes).forEach(specNode -> {
            String oldId = specNode.getId();
            String newId = uuidMapping.get(specNode.getId());

            specNode.setId(newId);
            Optional.ofNullable(specNode.getScript()).ifPresent(this::processScript);
            ListUtils.emptyIfNull(specNode.getOutputs()).forEach(out -> processOutput(out, oldId, newId, specKind));
            ListUtils.emptyIfNull(specNode.getInputs()).forEach(this::processInput);
            specNode.setMetadata(null);

            // foreach/dowhile/combine/paiflow/subflow
            Optional.ofNullable(specNode.getInnerNodes())
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(innerNodes -> this.processSpecNodes(specKind, null, innerNodes));
            Optional.ofNullable(specNode.getInnerDependencies())
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(this::processSpecDependencies);
            Optional.ofNullable(specNode.getSubflow()).ifPresent(subflow -> {
                Optional.ofNullable(subflow.getOutput()).map(outputMapping::get)
                    .or(() -> Optional.ofNullable(subflow.getId()).map(uuidMapping::get))
                    .ifPresent(subflow::setOutput);
                subflow.setId(null);
            });
            Optional.ofNullable(specNode.getJoin()).ifPresent(join ->
                ListUtils.emptyIfNull(join.getBranches()).forEach(branch ->
                    Optional.ofNullable(branch.getOutput()).map(o -> outputMapping.get(o.getData())).ifPresent(o ->
                        branch.getOutput().setData(o))));
            Optional.ofNullable(specNode.getParamHub()).ifPresent(paramHub ->
                ListUtils.emptyIfNull(paramHub.getVariables()).stream()
                    .filter(var -> VariableType.PASS_THROUGH.equals(var.getType()))
                    .forEach(this::processPassThroughVariable));
            Optional.ofNullable(specNode.getBranch()).ifPresent(branch ->
                ListUtils.emptyIfNull(branch.getBranches()).forEach(b ->
                    Optional.ofNullable(b.getOutput()).map(o -> outputMapping.get(o.getData())).ifPresent(o ->
                        b.getOutput().setData(o))));

            // some special nodes content need to be processed
            // foreach/dowhile/branch/join/param-hub/sub_process
            Optional.ofNullable(specNode.getForeach()).ifPresent(foreach ->
                specNode.getScript().setContent(JSONObject.toJSONString(SpecUtil.write(foreach, new SpecWriterContext()), Feature.PrettyFormat)));
            Optional.ofNullable(specNode.getDoWhile()).ifPresent(dowhile ->
                specNode.getScript().setContent(JSONObject.toJSONString(SpecUtil.write(dowhile, new SpecWriterContext()), Feature.PrettyFormat)));
            Optional.ofNullable(specNode.getCombined()).ifPresent(combine ->
                specNode.getScript().setContent(JSONObject.toJSONString(SpecUtil.write(combine, new SpecWriterContext()), Feature.PrettyFormat)));
            Optional.ofNullable(specNode.getParamHub()).ifPresent(paramHub ->
                specNode.getScript().setContent(JSONObject.toJSONString(SpecUtil.write(paramHub, new SpecWriterContext()), Feature.PrettyFormat)));
            Optional.ofNullable(specNode.getBranch()).ifPresent(branch -> {
                outputMapping.forEach((oldOut, newOut) -> {
                    String newContent = StringUtils.replaceIgnoreCase(
                        specNode.getScript().getContent(),
                        "\"" + oldOut + "\"",
                        "\"" + newOut + "\"");
                    specNode.getScript().setContent(newContent);
                });
            });
            Optional.ofNullable(specNode.getJoin()).ifPresent(join -> {
                outputMapping.forEach((oldOut, newOut) -> {
                    String newContent = StringUtils.replaceIgnoreCase(
                        specNode.getScript().getContent(),
                        "\\\"" + oldOut + "\\\"",
                        "\\\"" + newOut + "\\\"");
                    specNode.getScript().setContent(newContent);
                });
            });
        });
    }

    private void processPassThroughVariable(SpecVariable passThroughVar) {
        if (!VariableType.PASS_THROUGH.equals(passThroughVar.getType())) {
            return;
        }

        String[] outputAndName = StringUtils.split(passThroughVar.getValue(), ":");
        if (outputAndName.length > 1 && outputMapping.containsKey(outputAndName[0])) {
            passThroughVar.setValue(Joiner.on(":").join(outputMapping.get(outputAndName[0]), outputAndName[1]));
        }

        Optional.ofNullable(passThroughVar.getReferenceVariable())
            .map(SpecVariable::getNode)
            .filter(node -> node.getOutput() != null && outputMapping.containsKey(node.getOutput().getData()))
            .ifPresent(refVarNode -> refVarNode.getOutput().setData(outputMapping.get(refVarNode.getOutput().getData())));
    }

    private void processInput(Input input) {
        if (input instanceof SpecNodeOutput) {
            SpecNodeOutput nodeInput = (SpecNodeOutput)input;
            if (outputMapping.containsKey(nodeInput.getData())) {
                log.info("found nodeInput: {} in outputMapping, replace to output: {}", nodeInput.getData(), outputMapping.get(nodeInput.getData()));
                nodeInput.setData(outputMapping.get(nodeInput.getData()));
            }
        }

        if (input instanceof SpecVariable) {
            SpecVariable variable = (SpecVariable)input;
            // clear unnecessary info
            variable.setId(null);
            processPassThroughVariable(variable);

            SpecDepend node = Optional.ofNullable(variable.getNode()).orElse(new SpecDepend());
            String outputStr = Optional.ofNullable(node.getOutput()).map(SpecNodeOutput::getData)
                .or(() -> Optional.ofNullable(node.getNodeId()).map(SpecRefEntity::getId))
                .orElse(null);
            if (StringUtils.isBlank(outputStr)) {
                log.warn("variable nodeInput: {} is empty, skip", outputStr);
                return;
            }

            SpecNodeOutput output = new SpecNodeOutput();
            output.setData(outputStr);
            if (outputMapping.containsKey(outputStr) || uuidMapping.containsValue(outputStr)) {
                log.info("found variable nodeInput: {} in outputMapping, replace to output: {}", output, outputMapping.get(outputStr));
                output.setData(outputMapping.get(outputStr));
            }
            node.setNodeId(null);
            node.setOutput(output);
            Optional.ofNullable(variable.getReferenceVariable()).ifPresent(refVar -> {
                refVar.setId(null);
                refVar.setNode(node);
            });
        }
    }

    private void processOutput(Output out, String oldId, String newId, String specKind) {
        if (out instanceof SpecNodeOutput) {
            SpecNodeOutput nodeOutput = (SpecNodeOutput)out;
            if (StringUtils.equalsIgnoreCase(nodeOutput.getData(), oldId)) {
                nodeOutput.setData(newId);
                outputMapping.put(oldId, newId);
            } else {
                if (SpecKind.CYCLE_WORKFLOW.getLabel().equalsIgnoreCase(specKind) ||
                    SpecKind.TRIGGER_WORKFLOW.getLabel().equalsIgnoreCase(specKind)) {
                    // cycle workflow output data replace to avoid deployment output conflicts
                    String oldOutput = nodeOutput.getData();
                    // generate a short random string
                    String randomSuffix = COPY_SUFFIX_START + RandomStringUtils.randomAlphanumeric(6) + COPY_SUFFIX_END;
                    String newOutput;
                    // if oldOutput contains randomSuffix, replace it
                    if (COPY_SUFFIX_PATTERN.matcher(oldOutput).find()) {
                        newOutput = RegExUtils.replacePattern(oldOutput, COPY_SUFFIX_PATTERN.pattern(), randomSuffix);
                    } else {
                        newOutput = Joiner.on("_").join(oldOutput, randomSuffix);
                    }
                    log.info("replace node output: {} to: {}", oldOutput, newOutput);
                    nodeOutput.setData(newOutput);
                    outputMapping.put(oldOutput, newOutput);
                }
            }
        }

        if (out instanceof SpecVariable) {
            SpecVariable variable = (SpecVariable)out;
            // clear unnecessary info
            variable.setId(null);
            Optional.ofNullable(variable.getNode()).ifPresent(n -> n.setNodeId(null));
            variable.setReferenceVariable(null);
            SpecDepend node = Optional.ofNullable(variable.getNode()).orElse(new SpecDepend());
            SpecNodeOutput output = new SpecNodeOutput();
            output.setData(newId);
            node.setOutput(output);
            processPassThroughVariable(variable);
        }
    }
}
