package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrAllocationSpec;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.enums.ArtifactType;
import com.aliyun.dataworks.common.spec.domain.enums.DependencyType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeInstanceModeType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRecurrenceType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRerunModeType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.enums.TriggerType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.interfaces.NodeIO;
import com.aliyun.dataworks.common.spec.domain.interfaces.Output;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDatasource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecRuntimeResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.container.SpecContainer;
import com.aliyun.dataworks.common.spec.utils.JSONUtils;
import com.aliyun.dataworks.common.spec.utils.ReflectUtils;
import com.aliyun.dataworks.common.spec.utils.VariableUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.client.NodeType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.CycleType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.DependentType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.IoParseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.RerunMode;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntity;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DefaultNodeTypeUtils;
import com.aliyun.migrationx.common.utils.DateUtils;
import com.aliyun.migrationx.common.utils.UuidUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import static com.aliyun.dataworks.common.spec.domain.dw.nodemodel.DataWorksNodeAdapter.STREAM_LAUNCH_MODE;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-11-21
 */
@Slf4j
public class NodeSpecUpdateAdapter {

    /**
     * update info in specification by dwNode
     *
     * @param dwNode        dwNode
     * @param specification specification
     */
    public void updateSpecification(DwNodeEntity dwNode, Specification<DataWorksWorkflowSpec> specification) {
        specification.setVersion(SpecVersion.V_1_1_0.getLabel());
        specification.setKind(Optional.ofNullable(dwNode.getNodeUseType()).map(useType -> {
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

        Map<String, Object> metadata = ObjectUtils.defaultIfNull(specification.getMetadata(), new LinkedHashMap<>());
        Optional.ofNullable(dwNode.getOwner()).ifPresent(owner -> metadata.put("owner", owner));
        Optional.ofNullable(dwNode.getUuid()).ifPresent(uuid -> metadata.put("uuid", uuid));
        Optional.ofNullable(dwNode.getParentId()).ifPresent(containerId -> metadata.put("containerId", String.valueOf(containerId)));
        specification.setMetadata(metadata);

        DataWorksWorkflowSpec spec = ObjectUtils.defaultIfNull(specification.getSpec(), new DataWorksWorkflowSpec());
        Optional.ofNullable(dwNode.getBizId()).filter(id -> id > 0).map(String::valueOf).ifPresent(spec::setId);
        Optional.ofNullable(dwNode.getBizName()).ifPresent(spec::setName);
        specification.setSpec(spec);

        SpecNode specNode = ListUtils.emptyIfNull(spec.getNodes()).stream()
            .findFirst()
            .orElseGet(SpecNode::new);
        spec.setNodes(Collections.singletonList(specNode));
        SpecFlowDepend specFlowDepend = ListUtils.emptyIfNull(spec.getFlow()).stream()
            .filter(dependence -> StringUtils.equals(specNode.getId(),
                Optional.ofNullable(dependence.getNodeId()).map(SpecRefEntity::getId).orElse(null)))
            .findFirst()
            .orElseGet(() -> {
                SpecFlowDepend dependence = new SpecFlowDepend();
                dependence.setNodeId(specNode);
                return dependence;
            });
        spec.setFlow(Collections.singletonList(specFlowDepend));

        // fill node
        fillSpecNode(dwNode, specNode);

        // fill flow dependence
        fillSpecFlowDepend(dwNode, specNode, specFlowDepend);

        log.info("specification: {}", specification);
    }

    /**
     * fill spec node by dwNode
     *
     * @param dwNode   dwNode
     * @param specNode spec node
     */
    private void fillSpecNode(DwNodeEntity dwNode, SpecNode specNode) {
        SpecScript script = ObjectUtils.defaultIfNull(specNode.getScript(), new SpecScript());
        SpecTrigger trigger = ObjectUtils.defaultIfNull(specNode.getTrigger(), new SpecTrigger());
        specNode.setScript(script);
        specNode.setTrigger(trigger);

        // parse node
        fillSpecNodeBasicInfo(dwNode, specNode);

        // parse script
        fillSpecScript(dwNode, script, specNode);

        // parse trigger
        fillTrigger(dwNode, trigger);

        // parse resource group and datasource
        fillResourceGroup(dwNode, specNode);
        fillDatasource(dwNode, specNode);

        // parse inputs and outputs
        fillInputs(dwNode, specNode, script);
        fillOutputs(dwNode, specNode);
    }

    /**
     * fill node inputs
     *
     * @param dwNode   dwNode
     * @param specNode spec node
     * @param script   script
     */
    private void fillInputs(DwNodeEntity dwNode, SpecNode specNode, SpecScript script) {
        List<Input> inputList = ObjectUtils.defaultIfNull(specNode.getInputs(), new ArrayList<>());
        specNode.setInputs(inputList);
        List<SpecVariable> specVariableList = ObjectUtils.defaultIfNull(script.getParameters(), new ArrayList<>());
        script.setParameters(specVariableList);

        if (dwNode.getInputs() != null) {
            // clear old inputs and reset new inputs
            inputList.removeIf(input -> input == null || input instanceof SpecNodeOutput);
            inputList.addAll(toNodeIos(dwNode.getInputs()));
        }

        if (dwNode.getInputContexts() != null) {
            // clear old context input and reset new context inputs
            inputList.removeIf(input -> input == null || input instanceof SpecVariable);
            specVariableList.removeIf(v -> v == null || VariableScopeType.NODE_CONTEXT.equals(v.getScope()));

            SpecDepend specDepend = new SpecDepend();
            specDepend.setNodeId(specNode);
            List<Input> inputVariables = dwNode.getInputContexts().stream().map(inCtx -> {
                SpecVariable specVariable = new SpecVariable();
                specVariable.setScope(VariableScopeType.NODE_CONTEXT);
                specVariable.setType(convertParamType(inCtx.getParamType()));
                specVariable.setName(inCtx.getParamName());
                specVariable.setDescription(inCtx.getDescription());
                String[] kv = StringUtils.split(inCtx.getParamValue(), ":");
                if (kv != null && kv.length == 2) {
                    String output = kv[0];
                    SpecVariable ref = new SpecVariable();
                    SpecDepend refNode = new SpecDepend();
                    SpecNodeOutput o = new SpecNodeOutput();
                    o.setData(output);
                    refNode.setOutput(o);
                    ref.setNode(refNode);
                    ref.setName(kv[1]);
                    ref.setInputName(inCtx.getParamName());
                    ref.setType(VariableType.NODE_OUTPUT);
                    ref.setScope(VariableScopeType.NODE_CONTEXT);
                    specVariable.setReferenceVariable(ref);
                } else {
                    log.warn("invalid input context value: {}", inCtx);
                }
                specVariable.setNode(specDepend);
                return specVariable;
            }).collect(Collectors.toList());

            inputVariables.stream().map(SpecVariable.class::cast).forEach(specVariableList::add);

            ListUtils.emptyIfNull(inputVariables).stream()
                .map(v -> (SpecVariable)v).map(SpecVariable::getReferenceVariable)
                .filter(Objects::nonNull)
                .forEach(inputList::add);
        }
    }

    /**
     * fill node outputs
     *
     * @param dwNode   dwNode
     * @param specNode spec node
     */
    private void fillOutputs(DwNodeEntity dwNode, SpecNode specNode) {
        List<Output> outputList = ObjectUtils.defaultIfNull(specNode.getOutputs(), new ArrayList<>());
        specNode.setOutputs(outputList);

        if (dwNode.getOutputs() != null) {
            // clear old outputs and reset new outputs
            outputList.removeIf(output -> output == null || output instanceof SpecNodeOutput);
            outputList.addAll(toNodeIos(dwNode.getOutputs()));
        }

        if (dwNode.getOutputContexts() != null) {
            // clear old context outputs and reset new context outputs
            outputList.removeIf(output -> output == null || output instanceof SpecVariable);

            SpecDepend specDepend = new SpecDepend();
            specDepend.setNodeId(specNode);
            dwNode.getOutputContexts().stream()
                .map(outCtx -> {
                    SpecVariable specVariable = new SpecVariable();
                    specVariable.setScope(VariableScopeType.NODE_CONTEXT);
                    specVariable.setType(convertParamType(outCtx.getParamType()));
                    specVariable.setName(outCtx.getParamName());
                    specVariable.setValue(outCtx.getParamValue());
                    specVariable.setNode(specDepend);
                    specVariable.setDescription(outCtx.getDescription());
                    return specVariable;
                })
                .forEach(outputList::add);
        }
    }

    /**
     * convert node io to spec node io
     *
     * @param ios node ios
     * @param <T> Input or Output
     * @return spec node ios
     */
    @SuppressWarnings("unchecked")
    private <T extends NodeIO> List<T> toNodeIos(List<NodeIo> ios) {
        return ListUtils.emptyIfNull(ios).stream().map(out -> {
            SpecNodeOutput a = new SpecNodeOutput();
            a.setArtifactType(ArtifactType.NODE_OUTPUT);
            a.setData(out.getData());
            a.setRefTableName(out.getRefTableName());
            a.setIsDefault(Objects.equals(IoParseType.SYSTEM.getCode(), out.getParseType()));
            return (T)a;
        }).collect(Collectors.toList());
    }

    /**
     * fill resource group
     *
     * @param dwNode   dwNode
     * @param specNode spec node
     */
    private void fillResourceGroup(DwNodeEntity dwNode, SpecNode specNode) {
        // parse resource group
        if (StringUtils.isNotBlank(dwNode.getResourceGroup())) {
            SpecRuntimeResource specRuntimeResource = new SpecRuntimeResource();
            specRuntimeResource.setResourceGroup(dwNode.getResourceGroup());
            specNode.setRuntimeResource(specRuntimeResource);
        }
    }

    /**
     * fill datasource
     *
     * @param dwNode   dwNode
     * @param specNode spec node
     */
    private void fillDatasource(DwNodeEntity dwNode, SpecNode specNode) {
        // parse datasource
        if (StringUtils.isNotBlank(dwNode.getConnection())) {
            SpecDatasource specDatasource = new SpecDatasource();
            specDatasource.setName(dwNode.getConnection());
            specNode.setDatasource(specDatasource);
        }
    }

    /**
     * fill trigger
     *
     * @param dwNode  dwNode
     * @param trigger trigger need to fill
     */
    private void fillTrigger(DwNodeEntity dwNode, SpecTrigger trigger) {

        NodeUseType useType = dwNode.getNodeUseType();
        switch (useType) {
            case SKIP:
            case SCHEDULED: {
                Optional.ofNullable(dwNode.getStartEffectDate()).map(DateUtils::convertDateToString).ifPresent(trigger::setStartTime);
                Optional.ofNullable(dwNode.getEndEffectDate()).map(DateUtils::convertDateToString).ifPresent(trigger::setEndTime);
                Optional.ofNullable(dwNode.getCronExpress()).map(String::trim).ifPresent(trigger::setCron);
                Optional.ofNullable(dwNode.getCalendarId()).ifPresent(trigger::setCalendarId);
                Optional.ofNullable(dwNode.getCycleType()).ifPresent(cycleType -> {
                    if (CycleType.DAY.getCode() == cycleType) {
                        trigger.setCycleType(com.aliyun.dataworks.common.spec.domain.enums.CycleType.DAILY);
                    } else {
                        trigger.setCycleType(com.aliyun.dataworks.common.spec.domain.enums.CycleType.NOT_DAILY);
                    }
                });
                trigger.setType(TriggerType.SCHEDULER);
                trigger.setTimezone(ZoneId.systemDefault().getId());
                break;
            }
            default: {
                trigger.setType(TriggerType.MANUAL);
                break;
            }
        }
    }

    /**
     * fill spec node basic info
     *
     * @param dwNode   dwNode
     * @param specNode spec node need to fill
     */
    private void fillSpecNodeBasicInfo(DwNodeEntity dwNode, SpecNode specNode) {
        String id = Optional.ofNullable(dwNode.getUuid()).orElseGet(UuidUtils::genUuidWithoutHorizontalLine);
        specNode.setId(id);
        Optional.ofNullable(dwNode.getName()).ifPresent(specNode::setName);
        Optional.ofNullable(dwNode.getDescription()).ifPresent(specNode::setDescription);
        Optional.ofNullable(dwNode.getOwner()).ifPresent(specNode::setOwner);
        Optional.ofNullable(dwNode.getAlisaTaskKillTimeout()).ifPresent(specNode::setTimeout);
        Optional.ofNullable(dwNode.getTaskRerunTime()).ifPresent(specNode::setRerunTimes);
        Optional.ofNullable(dwNode.getTaskRerunInterval()).ifPresent(specNode::setRerunInterval);
        Optional.ofNullable(dwNode.getRerunMode()).map(this::convertRerunMode).ifPresent(specNode::setRerunMode);
        Optional.ofNullable(dwNode.getNodeType()).map(this::convertRecurrence).ifPresent(specNode::setRecurrence);
        Optional.ofNullable(dwNode.getPauseSchedule()).filter(BooleanUtils::isTrue)
            .ifPresent(ignore -> specNode.setRecurrence(NodeRecurrenceType.PAUSE));
        Optional.ofNullable(dwNode.getNodeUseType()).filter(NodeUseType.SKIP::equals)
            .ifPresent(ignore -> specNode.setRecurrence(NodeRecurrenceType.SKIP));
        Optional.ofNullable(dwNode.getStartRightNow())
            .map(startRightNow -> startRightNow ? NodeInstanceModeType.IMMEDIATELY : NodeInstanceModeType.T_PLUS_1)
            .ifPresent(specNode::setInstanceMode);

        Optional.ofNullable(dwNode.getIsAutoParse()).map(auto -> Objects.equals(auto, 1)).ifPresent(specNode::setAutoParse);
        Optional.ofNullable(dwNode.getIgnoreBranchConditionSkip()).ifPresent(specNode::setIgnoreBranchConditionSkip);
    }

    /**
     * fill script
     *
     * @param dwNode   node
     * @param script   script
     * @param specNode spec node
     */
    private void fillSpecScript(DwNodeEntity dwNode, SpecScript script, SpecNode specNode) {
        Optional.ofNullable(dwNode.getFolder())
            .map(path -> FilenameUtils.concat(path, specNode.getName()))
            .ifPresent(script::setPath);

        // check file path and node name are logically consistent
        if (script.getPath() == null || !StringUtils.equals(FilenameUtils.getName(script.getPath()), specNode.getName())) {
            String parentPath = FilenameUtils.getFullPathNoEndSeparator(script.getPath());
            script.setPath(FilenameUtils.concat(parentPath, specNode.getName()));
        }

        Optional.ofNullable(dwNode.getCode()).ifPresent(script::setContent);

        fillScriptParam(dwNode, script);

        fillSpecScriptRuntime(dwNode, script);
    }

    /**
     * fill script runtime
     *
     * @param dwNode dwNode
     * @param script script
     */
    private void fillSpecScriptRuntime(DwNodeEntity dwNode, SpecScript script) {
        SpecScriptRuntime runtime = ObjectUtils.defaultIfNull(script.getRuntime(), new SpecScriptRuntime());
        script.setRuntime(runtime);

        Optional.ofNullable(dwNode.getType())
            .map(CodeProgramType::getNodeTypeByName)
            .ifPresent(codeProgramType -> {
                runtime.setCommand(codeProgramType.name());
                runtime.setEngine(codeProgramType.getCalcEngineType().getLabel());
            });
        Optional.ofNullable(dwNode.getTypeId())
            .ifPresent(runtime::setCommandTypeId);

        Optional.ofNullable(dwNode.getCu())
            .ifPresent(runtime::setCu);

        Optional.ofNullable(dwNode.getImageId())
            .ifPresent(imageId -> {
                SpecContainer specContainer = Optional.ofNullable(runtime.getContainer()).orElseGet(SpecContainer::new);
                runtime.setContainer(specContainer);
                specContainer.setImageId(imageId);
            });

        parseAdvancedSettings(dwNode.getAdvanceSettings(), dwNode.getStreamLaunchMode(), runtime);
    }

    /**
     * parse advanced settings
     *
     * @param advancedSettings advanced settings
     * @param startImmediately stream launch mode
     * @param runtime          runtime
     */
    private void parseAdvancedSettings(String advancedSettings, Integer startImmediately, SpecScriptRuntime runtime) {
        String command = runtime.getCommand();
        if (CodeProgramType.EMR_SPARK_STREAMING.getName().equalsIgnoreCase(command)
            || CodeProgramType.EMR_STREAMING_SQL.getName().equalsIgnoreCase(command)) {
            // set spark config
            if (StringUtils.isNotBlank(advancedSettings)) {
                parseEmrCode(runtime, advancedSettings);
            }

            // set stream job config
            if (startImmediately != null) {
                if (MapUtils.isEmpty(runtime.getStreamJobConfig())) {
                    runtime.setStreamJobConfig(new HashMap<>());
                }
                runtime.getStreamJobConfig().put(STREAM_LAUNCH_MODE, startImmediately);
            }
        }
    }

    private void parseEmrCode(SpecScriptRuntime specScriptRuntime, String code) {
        Map<String, Object> allocSpecMap = JSONUtils.parseObject(code, new TypeReference<Map<String, Object>>() {});
        EmrAllocationSpec allocSpec = EmrAllocationSpec.of(allocSpecMap);
        if (allocSpec != null) {
            Map<String, Object> emrJobConfig = Optional.ofNullable(specScriptRuntime).map(SpecScriptRuntime::getEmrJobConfig)
                .orElseGet(() -> {
                    Map<String, Object> emrJobConfigMap = new HashMap<>();
                    Optional.ofNullable(specScriptRuntime).ifPresent(runtime -> runtime.setEmrJobConfig(emrJobConfigMap));
                    return emrJobConfigMap;
                });
            emrJobConfig.put("priority", allocSpec.getPriority());
            emrJobConfig.put("cores", allocSpec.getVcores());
            emrJobConfig.put("memory", allocSpec.getMemory());
            emrJobConfig.put("queue", allocSpec.getQueue());
            emrJobConfig.put("submitter", allocSpec.getUserName());
            Optional.ofNullable(allocSpec.getDataworksSessionDisable()).ifPresent(
                disable -> emrJobConfig.put(EmrAllocationSpec.UPPER_KEY_DATAWORKS_SESSION_DISABLE, disable));
            Optional.ofNullable(allocSpec.getEnableJdbcSql()).ifPresent(
                enable -> emrJobConfig.put(EmrAllocationSpec.UPPER_KEY_ENABLE_SPARKSQL_JDBC, enable));
            Optional.ofNullable(allocSpec.getReuseSession()).ifPresent(
                reuse -> emrJobConfig.put(EmrAllocationSpec.UPPER_KEY_REUSE_SESSION, reuse));
            Optional.ofNullable(allocSpec.getUseGateway()).ifPresent(useGateway ->
                emrJobConfig.put(EmrAllocationSpec.UPPER_KEY_USE_GATEWAY, useGateway));
            Optional.ofNullable(allocSpec.getBatchMode()).ifPresent(batchMode ->
                emrJobConfig.put(EmrAllocationSpec.UPPER_KEY_FLOW_SKIP_SQL_ANALYZE, batchMode));
        }

        Optional.ofNullable(allocSpecMap).map(Map::entrySet)
            .orElse(Collections.emptySet()).stream()
            .filter(ent -> ReflectUtils.getPropertyFields(allocSpec).stream().noneMatch(f -> f.getName().equals(ent.getKey())))
            .filter(ent -> !EmrAllocationSpec.UPPER_KEYS.contains(ent.getKey()))
            .forEach(ent -> {
                Map<String, Object> sparkConf = Optional.ofNullable(specScriptRuntime).map(SpecScriptRuntime::getSparkConf)
                    .orElseGet(() -> {
                        Map<String, Object> sparkConfMap = new HashMap<>();
                        Optional.ofNullable(specScriptRuntime).ifPresent(runtime -> runtime.setSparkConf(sparkConfMap));
                        return sparkConfMap;
                    });
                sparkConf.put(ent.getKey(), ent.getValue());
            });
    }

    /**
     * convert rerun mode
     *
     * @param rerunMode rerun mode
     * @return rerun mode in spec
     */
    private NodeRerunModeType convertRerunMode(RerunMode rerunMode) {
        if (rerunMode == null) {
            return null;
        }
        return Arrays.stream(NodeRerunModeType.values())
            .filter(nodeRerunModeType -> StringUtils.equalsIgnoreCase(nodeRerunModeType.name(), rerunMode.name()))
            .findFirst().orElse(null);
    }

    /**
     * convert node type
     *
     * @param nodeType node type
     * @return recurrence type
     */
    private NodeRecurrenceType convertRecurrence(Integer nodeType) {
        return Arrays.stream(NodeType.values())
            .filter(t -> Objects.equals(t.getCode(), nodeType))
            .flatMap(t -> Arrays.stream(NodeRecurrenceType.values()).filter(r -> r.name().equalsIgnoreCase(t.name())))
            .findFirst()
            .orElse(null);
    }

    /**
     * convert param type
     *
     * @param paramType param type
     * @return variable type
     */
    private VariableType convertParamType(Integer paramType) {
        if (paramType == null) {
            return VariableType.CONSTANT;
        }

        if (1 == paramType) {
            return VariableType.CONSTANT;
        } else if (2 == paramType) {
            return VariableType.NODE_OUTPUT;
        } else if (3 == paramType) {
            return VariableType.PASS_THROUGH;
        }
        return VariableType.CONSTANT;
    }

    /**
     * fill script parameters
     *
     * @param dwNode dwNode
     * @param script script
     */
    private void fillScriptParam(DwNodeEntity dwNode, SpecScript script) {
        if (dwNode.getParameter() == null) {
            return;
        }
        List<SpecVariable> specVariableList = ListUtils.defaultIfNull(script.getParameters(), new ArrayList<>());
        script.setParameters(specVariableList);
        // clear old parameters
        specVariableList.removeIf(specVariable -> VariableScopeType.NODE_PARAMETER.equals(specVariable.getScope()));

        List<SpecVariable> scriptParameters = toScriptParameters(dwNode);
        specVariableList.addAll(scriptParameters);
    }

    /**
     * check whether the parameter value is no kv pair
     *
     * @param dmNodeBO  dwNode
     * @param paraValue parameter value
     * @return true if no kv pair
     */
    private boolean isNoKvPairParaValue(DwNodeEntity dmNodeBO, String paraValue) {
        if (CodeProgramType.TT_MERGE.name().equalsIgnoreCase(dmNodeBO.getType())) {
            return true;
        }

        if (CodeProgramType.DIDE_SHELL.name().equalsIgnoreCase(dmNodeBO.getType())) {
            return true;
        }

        if (StringUtils.isBlank(paraValue)) {
            return false;
        }

        if (DefaultNodeTypeUtils.isDiNode(dmNodeBO.getType())) {
            return VariableUtils.NO_KV_PAIR_PARA_VALUE.matcher(paraValue).find();
        }

        String[] parts = StringUtils.trim(paraValue).split(" ");
        if (parts.length > 1) {
            for (String kv : parts) {
                String[] kvPair = kv.split("=");
                if (kvPair.length != 2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * convert node parameter to spec variable
     *
     * @param dmNodeBO node
     * @return variables
     */
    private List<SpecVariable> toScriptParameters(DwNodeEntity dmNodeBO) {
        if (isNoKvPairParaValue(dmNodeBO, dmNodeBO.getParameter())) {
            SpecVariable noKvPairVar = new SpecVariable();
            noKvPairVar.setName("-");
            noKvPairVar.setScope(VariableScopeType.NODE_PARAMETER);
            noKvPairVar.setType(VariableType.NO_KV_PAIR_EXPRESSION);
            noKvPairVar.setValue(dmNodeBO.getParameter());
            return Collections.singletonList(noKvPairVar);
        }

        AtomicInteger paraIndex = new AtomicInteger(1);
        return Arrays.stream(StringUtils.split(StringUtils.defaultString(dmNodeBO.getParameter(), ""), " ")).map(kvStr -> {
            SpecVariable var = new SpecVariable();
            var.setType(VariableType.SYSTEM);
            var.setScope(VariableScopeType.NODE_PARAMETER);
            String[] kv = StringUtils.split(kvStr, "=");
            if (kv.length == 2) {
                var.setName(kv[0]);
                var.setValue(kv[1]);
                return var;
            } else {
                var.setValue(kvStr);
                var.setName(String.valueOf(paraIndex.getAndIncrement()));
            }
            return var;
        }).collect(Collectors.toList());
    }

    /**
     * fill spec flow depend
     *
     * @param dwNode         dwNode
     * @param specNode       specNode
     * @param specFlowDepend specFlowDepend
     */
    private void fillSpecFlowDepend(DwNodeEntity dwNode, SpecNode specNode, SpecFlowDepend specFlowDepend) {
        if (dwNode.getInputs() == null && StringUtils.isNotBlank(dwNode.getDependentDataNode())) {
            return;
        }
        specFlowDepend.setDepends(new ArrayList<>());

        ListUtils.emptyIfNull(specNode.getInputs()).stream()
            .filter(SpecNodeOutput.class::isInstance)
            .map(SpecNodeOutput.class::cast)
            .map(input -> {
                SpecDepend specDepend = new SpecDepend();
                specDepend.setType(DependencyType.NORMAL);
                SpecNodeOutput art = new SpecNodeOutput();
                art.setData(input.getData());
                art.setArtifactType(ArtifactType.NODE_OUTPUT);
                art.setRefTableName(input.getRefTableName());
                specDepend.setOutput(art);
                return specDepend;
            })
            .forEach(specFlowDepend.getDepends()::add);

        if (Stream.of(DependentType.USER_DEFINE, DependentType.USER_DEFINE_AND_SELF)
            .anyMatch(dt -> Objects.equals(dwNode.getDependentType(), dt.getValue()))) {
            Optional.ofNullable(StringUtils.split(dwNode.getDependentDataNode(), ","))
                .map(Arrays::asList)
                .orElse(new ArrayList<>()).stream()
                .map(out -> {
                    SpecDepend specDepend = new SpecDepend();
                    specDepend.setType(DependencyType.CROSS_CYCLE_OTHER_NODE);
                    SpecNodeOutput art = new SpecNodeOutput();
                    art.setData(out);
                    art.setArtifactType(ArtifactType.NODE_OUTPUT);
                    specDepend.setOutput(art);
                    return specDepend;
                }).forEach(specFlowDepend.getDepends()::add);
        }

        if (Stream.of(DependentType.SELF, DependentType.USER_DEFINE_AND_SELF, DependentType.CHILD_AND_SELF).anyMatch(
            dt -> Objects.equals(dt.getValue(), dwNode.getDependentType()))) {
            SpecDepend specDepend = new SpecDepend();
            specDepend.setType(DependencyType.CROSS_CYCLE_SELF);
            specDepend.setNodeId(specNode);
            specFlowDepend.getDepends().add(specDepend);

            if (Objects.equals(DependentType.CHILD_AND_SELF.getValue(), dwNode.getDependentType())) {
                SpecDepend sp = new SpecDepend();
                sp.setType(DependencyType.CROSS_CYCLE_CHILDREN);
                sp.setNodeId(specNode);
                specFlowDepend.getDepends().add(sp);
            }
        }
    }
}
