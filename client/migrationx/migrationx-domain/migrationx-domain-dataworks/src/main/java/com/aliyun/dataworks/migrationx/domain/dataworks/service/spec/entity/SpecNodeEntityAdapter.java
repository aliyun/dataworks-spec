package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.dw.nodemodel.DataWorksNodeAdapter;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.enums.CycleType;
import com.aliyun.dataworks.common.spec.domain.enums.DependencyType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeInstanceModeType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRecurrenceType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRerunModeType;
import com.aliyun.dataworks.common.spec.domain.enums.TriggerType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.interfaces.NodeIO;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDatasource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFile;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecRuntimeResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.container.SpecContainer;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeContext;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.client.NodeType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.DependentType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.RerunMode;
import lombok.Data;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-12-08
 */
@Data
public class SpecNodeEntityAdapter implements DwNodeEntity {

    private SpecNode node;

    private List<SpecDepend> specDepends;

    @Override
    public String getUuid() {
        return Optional.ofNullable(node).map(SpecRefEntity::getId).orElse(null);
    }

    @Override
    public Long getBizId() {
        return null;
    }

    @Override
    public String getBizName() {
        return null;
    }

    @Override
    public String getResourceGroupName() {
        return getResourceGroup();
    }

    @Override
    public Long getResourceGroupId() {
        return Optional.ofNullable(node)
            .map(SpecNode::getRuntimeResource)
            .map(SpecRuntimeResource::getResourceGroupId)
            .map(Long::valueOf)
            .orElse(null);
    }

    @Override
    public String getName() {
        return Optional.ofNullable(node).map(SpecNode::getName).orElse(null);
    }

    @Override
    public String getType() {
        Optional<SpecScriptRuntime> scriptRuntime = Optional.ofNullable(node)
            .map(SpecNode::getScript)
            .map(SpecScript::getRuntime);
        return scriptRuntime.map(SpecScriptRuntime::getCommand)
            .orElseGet(() -> scriptRuntime.map(SpecScriptRuntime::getCommandTypeId)
                .map(CodeProgramType::getNodeTypeByCode)
                .map(CodeProgramType::name)
                .orElse(null));
    }

    @Override
    public Integer getTypeId() {
        Optional<SpecScriptRuntime> scriptRuntime = Optional.ofNullable(node)
            .map(SpecNode::getScript)
            .map(SpecScript::getRuntime);
        return scriptRuntime.map(SpecScriptRuntime::getCommandTypeId)
            .orElseGet(() -> scriptRuntime.map(SpecScriptRuntime::getCommand)
                .map(CodeProgramType::getNodeTypeByName)
                .map(CodeProgramType::getCode)
                .orElse(null));
    }

    @Override
    public String getCronExpress() {
        return Optional.ofNullable(node)
            .map(SpecNode::getTrigger)
            .map(SpecTrigger::getCron)
            .orElse(null);
    }

    @Override
    public Date getStartEffectDate() {
        return Optional.ofNullable(node)
            .map(SpecNode::getTrigger)
            .map(SpecTrigger::getStartTime)
            .map(time -> Instant.from(getDateTimeFormatter().parse(time)))
            .map(Date::from)
            .orElse(null);
    }

    @Override
    public Integer getIsAutoParse() {
        return Optional.ofNullable(node)
            .map(SpecNode::getAutoParse)
            .map(b -> BooleanUtils.isTrue(b) ? 1 : 0)
            .orElse(0);
    }

    @Override
    public Date getEndEffectDate() {
        return Optional.ofNullable(node)
            .map(SpecNode::getTrigger)
            .map(SpecTrigger::getEndTime)
            .map(time -> Instant.from(getDateTimeFormatter().parse(time)))
            .map(Date::from)
            .orElse(null);
    }

    @Override
    public String getResourceGroup() {
        return Optional.ofNullable(node)
            .map(SpecNode::getRuntimeResource)
            .map(SpecRuntimeResource::getResourceGroup)
            .orElse(null);
    }

    @Override
    public String getDiResourceGroup() {
        return Optional.ofNullable(getCode())
            .map(code -> {
                try {
                    return JSONObject.parseObject(code);
                } catch (Exception e) {
                    return null;
                }
            })
            .map(json -> json.getJSONObject("extend"))
            .map(json -> json.getString("resourceGroup"))
            .orElse(null);
    }

    @Override
    public String getDiResourceGroupName() {
        return getDiResourceGroup();
    }

    @Override
    public String getCodeMode() {
        return null;
    }

    @Override
    public Boolean getStartRightNow() {
        return Optional.ofNullable(node)
            .map(SpecNode::getInstanceMode)
            .map(NodeInstanceModeType.IMMEDIATELY::equals)
            .orElse(false);
    }

    @Override
    public RerunMode getRerunMode() {
        return Optional.ofNullable(node)
            .map(SpecNode::getRerunMode)
            .map(NodeRerunModeType::name)
            .map(name -> Arrays.stream(RerunMode.values())
                .filter(rerunMode -> StringUtils.equalsIgnoreCase(rerunMode.name(), name))
                .findFirst()
                .orElse(RerunMode.UNKNOWN))
            .orElse(RerunMode.UNKNOWN);
    }

    @Override
    public Integer getNodeType() {
        boolean isManual = Optional.ofNullable(node).map(SpecNode::getTrigger)
            .map(SpecTrigger::getType)
            .map(TriggerType.MANUAL::equals)
            .orElse(false);
        if (isManual) {
            return NodeType.MANUAL.getCode();
        }
        return Optional.ofNullable(node).map(SpecNode::getRecurrence)
            .map(NodeRecurrenceType::name)
            .map(NodeType::valueOf)
            .map(NodeType::getCode)
            .orElse(null);
    }

    @Override
    public Boolean getPauseSchedule() {
        return Optional.ofNullable(node)
            .map(SpecNode::getRecurrence)
            .map(NodeRecurrenceType.PAUSE::equals)
            .orElse(false);
    }

    @Override
    public NodeUseType getNodeUseType() {
        if (Optional.ofNullable(getNodeType()).filter(nodeType -> nodeType == NodeType.MANUAL.getCode()).isPresent()) {
            return NodeUseType.MANUAL_WORKFLOW;
        }
        return Optional.of(node)
            .map(SpecNode::getRecurrence)
            .map(NodeRecurrenceType.SKIP::equals)
            .filter(skip -> skip)
            .map(skip -> NodeUseType.SKIP)
            .orElse(NodeUseType.SCHEDULED);
    }

    @Override
    public String getRef() {
        return null;
    }

    @Override
    public String getFolder() {
        return Optional.ofNullable(node)
            .map(SpecNode::getScript)
            .map(SpecFile::getPath)
            .map(FilenameUtils::getFullPathNoEndSeparator)
            .orElse(null);
    }

    @Override
    public Boolean getRoot() {
        return null;
    }

    @Override
    public String getConnection() {
        return Optional.ofNullable(node)
            .map(SpecNode::getDatasource)
            .map(SpecDatasource::getName)
            .orElse(null);
    }

    @Override
    public String getCode() {
        return Optional.ofNullable(node)
            .map(SpecNode::getScript)
            .map(SpecScript::getContent)
            .orElse(null);
    }

    @Override
    public String getParameter() {
        return Optional.ofNullable(node)
            .map(SpecNode::getScript)
            .map(SpecScript::getParameters)
            .map(paramList -> paramList.stream()
                .filter(param -> VariableScopeType.NODE_PARAMETER.equals(param.getScope()))
                .map(param -> param.getName() + "=" + param.getValue())
                .collect(Collectors.joining(" ")))
            .orElse(null);
    }

    @Override
    public List<NodeContext> getInputContexts() {
        return Optional.ofNullable(node)
            .map(SpecNode::getScript)
            .map(SpecScript::getParameters)
            .map(paramList -> paramList.stream()
                .filter(param -> VariableScopeType.NODE_CONTEXT.equals(param.getScope()))
                .map(param -> {
                    NodeContext nodeInputOutputContext = new NodeContext();
                    nodeInputOutputContext.setParamName(param.getName());
                    SpecVariable referenceVariable = Optional.ofNullable(param.getReferenceVariable()).orElse(param);
                    String refName = referenceVariable.getName();
                    String output = Optional.ofNullable(referenceVariable.getNode())
                        .map(SpecDepend::getOutput)
                        .map(SpecNodeOutput::getData)
                        .orElseGet(() -> Optional.ofNullable(referenceVariable.getNode())
                            .map(SpecDepend::getNodeId)
                            .map(SpecRefEntity::getId)
                            .orElse(null));
                    if (StringUtils.isNotBlank(refName) && StringUtils.isNotBlank(output)) {
                        nodeInputOutputContext.setParamValue(output + ":" + refName);
                    }
                    return nodeInputOutputContext;
                })
                .collect(Collectors.toList())
            ).orElse(null);
    }

    @Override
    public List<NodeContext> getOutputContexts() {
        return Optional.ofNullable(node)
            .map(SpecNode::getOutputs)
            .map(outputList -> outputList.stream()
                .filter(SpecVariable.class::isInstance)
                .map(SpecVariable.class::cast)
                .map(v -> {
                    NodeContext nodeInputOutputContext = new NodeContext();
                    nodeInputOutputContext.setParamName(v.getName());
                    nodeInputOutputContext.setParamValue(v.getValue());
                    nodeInputOutputContext.setDescription(v.getDescription());
                    // this field is used in pop to describe type of param, so set type in paramType
                    nodeInputOutputContext.setParamType(VariableType.CONSTANT.equals(v.getType()) ? 1 : 2);
                    return nodeInputOutputContext;
                })
                .collect(Collectors.toList()))
            .orElse(null);
    }

    @Override
    public List<NodeIo> getInputs() {
        List<NodeIo> nodeIos = Optional.ofNullable(node)
            .map(SpecNode::getInputs)
            .map(this::buildInputOutput)
            .orElse(null);
        if (nodeIos == null && specDepends == null) {
            return null;
        }
        nodeIos = new ArrayList<>(ListUtils.emptyIfNull(nodeIos));
        ListUtils.emptyIfNull(specDepends).stream()
            .filter(x -> Optional.ofNullable(x.getType()).orElse(DependencyType.NORMAL) == DependencyType.NORMAL)
            .map(specDepend -> {
                NodeIo nodeInputOutput = new NodeIo();
                nodeInputOutput.setParseType(1);
                if (specDepend.getNodeId() != null) {
                    nodeInputOutput.setData(specDepend.getNodeId().getId());
                    nodeInputOutput.setRefTableName(specDepend.getNodeId().getName());
                } else if (specDepend.getOutput() != null) {
                    nodeInputOutput.setData(specDepend.getOutput().getData());
                    nodeInputOutput.setRefTableName(specDepend.getOutput().getRefTableName());
                }
                return nodeInputOutput;
            })
            .forEach(nodeIos::add);
        return nodeIos;
    }

    @Override
    public List<NodeIo> getOutputs() {
        return Optional.ofNullable(node)
            .map(SpecNode::getOutputs)
            .map(this::buildInputOutput)
            .orElse(null);
    }

    @Override
    public List<DwNodeEntity> getInnerNodes() {
        Map<String, SpecFlowDepend> specFlowDependMap = Optional.ofNullable(node)
            .map(SpecNode::getInnerDependencies)
            .orElse(Collections.emptyList()).stream()
            .collect(Collectors.toMap(depend -> depend.getNodeId().getId(), Function.identity()));
        return Optional.ofNullable(node)
            .map(SpecNode::getInnerNodes)
            .orElse(Collections.emptyList()).stream()
            .map(node -> {
                SpecFlowDepend specFlowDepend = specFlowDependMap.get(node.getId());
                SpecNodeEntityAdapter specNodeEntityAdapter = new SpecNodeEntityAdapter();
                specNodeEntityAdapter.setNode(node);
                specNodeEntityAdapter.setSpecDepends(specFlowDepend.getDepends());
                return specNodeEntityAdapter;
            })
            .collect(Collectors.toList());
    }

    @Override
    public String getDescription() {
        return Optional.ofNullable(node)
            .map(SpecNode::getDescription)
            .orElse(null);
    }

    @Override
    public Integer getTaskRerunTime() {
        return Optional.ofNullable(node)
            .map(SpecNode::getRerunTimes)
            .orElse(null);
    }

    @Override
    public Integer getTaskRerunInterval() {
        return Optional.ofNullable(node)
            .map(SpecNode::getRerunInterval)
            .orElse(null);
    }

    @Override
    public Integer getDependentType() {
        AtomicBoolean dependSelf = new AtomicBoolean(false);
        AtomicBoolean dependChildren = new AtomicBoolean(false);
        AtomicBoolean dependOtherNode = new AtomicBoolean(false);
        ListUtils.emptyIfNull(specDepends).forEach(depend -> {
            if (DependencyType.CROSS_CYCLE_SELF.equals(depend.getType())) {
                dependSelf.set(true);
            } else if (DependencyType.CROSS_CYCLE_CHILDREN.equals(depend.getType())) {
                dependChildren.set(true);
            } else if (DependencyType.CROSS_CYCLE_OTHER_NODE.equals(depend.getType())) {
                dependOtherNode.set(true);
            }
        });

        if (dependSelf.get() && dependChildren.get()) {
            return DependentType.CHILD_AND_SELF.getValue();
        } else if (dependSelf.get() && dependOtherNode.get()) {
            return DependentType.USER_DEFINE_AND_SELF.getValue();
        } else if (dependSelf.get()) {
            return DependentType.SELF.getValue();
        } else if (dependChildren.get()) {
            return DependentType.CHILD.getValue();
        } else if (dependOtherNode.get()) {
            return DependentType.USER_DEFINE.getValue();
        }
        return DependentType.NONE.getValue();
    }

    @Override
    public Integer getCycleType() {
        return Optional.ofNullable(node)
            .map(SpecNode::getTrigger)
            .map(SpecTrigger::getCycleType)
            .map(cycleType -> CycleType.DAILY.equals(cycleType) ?
                com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.CycleType.DAY.getCode()
                : com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.CycleType.NOT_DAY.getCode())
            .orElse(null);
    }

    @Override
    public Date getLastModifyTime() {
        return null;
    }

    @Override
    public String getLastModifyUser() {
        return null;
    }

    @Override
    public Integer getMultiInstCheckType() {
        return null;
    }

    @Override
    public Integer getPriority() {
        return Optional.ofNullable(node)
            .map(SpecNode::getPriority)
            .orElse(null);
    }

    @Override
    public String getDependentDataNode() {
        if (DependentType.NONE.getValue() == getDependentType()) {
            return null;
        }
        return ListUtils.emptyIfNull(specDepends).stream()
            .filter(depend -> depend != null && depend.getType() != null && depend.getType() != DependencyType.NORMAL)
            .map(depend -> Optional.ofNullable(depend.getOutput()).map(SpecNodeOutput::getData).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.joining(","));
    }

    @Override
    public String getOwner() {
        return Optional.ofNullable(node)
            .map(SpecNode::getOwner)
            .orElse(null);
    }

    @Override
    public String getOwnerName() {
        return null;
    }

    @Override
    public String getExtraConfig() {
        return null;
    }

    @Override
    public String getExtraContent() {
        return null;
    }

    @Override
    public String getTtContent() {
        return null;
    }

    @Override
    public String getAdvanceSettings() {
        return Optional.ofNullable(node)
            .map(SpecNode::getScript)
            .map(SpecScript::getRuntime)
            .map(runtime -> {
                if (MapUtils.isEmpty(runtime.getEmrJobConfig()) && MapUtils.isEmpty(runtime.getSparkConf())) {
                    return null;
                }
                HashMap<Object, Object> advancedSettings = new HashMap<>();
                advancedSettings.putAll(MapUtils.emptyIfNull(runtime.getEmrJobConfig()));
                advancedSettings.putAll(MapUtils.emptyIfNull(runtime.getSparkConf()));
                return advancedSettings;
            })
            .map(JSONObject::toJSONString)
            .orElse(null);
    }

    @Override
    public String getExtend() {
        return null;
    }

    @Override
    public SpecComponent getComponent() {
        return null;
    }

    @Override
    public Integer getStreamLaunchMode() {
        return Optional.ofNullable(node)
            .map(SpecNode::getScript)
            .map(SpecScript::getRuntime)
            .map(SpecScriptRuntime::getStreamJobConfig)
            .map(map -> map.get(DataWorksNodeAdapter.STREAM_LAUNCH_MODE))
            .filter(Integer.class::isInstance)
            .map(Integer.class::cast)
            .orElse(null);
    }

    @Override
    public Boolean getIgnoreBranchConditionSkip() {
        return Optional.ofNullable(node)
            .map(SpecNode::getIgnoreBranchConditionSkip)
            .orElse(null);
    }

    /**
     * 超时时间 单位小时
     *
     * @return 超时时间
     */
    @Override
    public Integer getAlisaTaskKillTimeout() {
        return Optional.ofNullable(node)
            .map(SpecNode::getTimeout)
            .orElse(null);
    }

    @Override
    public Long getParentId() {
        return DwNodeEntity.super.getParentId();
    }

    @Override
    public String getCu() {
        return Optional.ofNullable(node)
            .map(SpecNode::getScript)
            .map(SpecScript::getRuntime)
            .map(SpecScriptRuntime::getCu)
            .orElse(null);
    }

    @Override
    public String getImageId() {
        return Optional.ofNullable(node)
            .map(SpecNode::getScript)
            .map(SpecScript::getRuntime)
            .map(SpecScriptRuntime::getContainer)
            .map(SpecContainer::getImageId)
            .orElse(null);
    }

    /**
     * build input and output from specNode input or output
     *
     * @param nodeInputOutputList node input or output list
     * @param <T>                 Input or Output
     * @return input or output in node def
     */
    private <T extends NodeIO> List<NodeIo> buildInputOutput(List<T> nodeInputOutputList) {
        return ListUtils.emptyIfNull(nodeInputOutputList).stream()
            .filter(SpecNodeOutput.class::isInstance)
            .map(SpecNodeOutput.class::cast)
            .map(output -> {
                NodeIo nodeInputOutput = new NodeIo();
                nodeInputOutput.setData(output.getData());
                nodeInputOutput.setRefTableName(output.getRefTableName());
                nodeInputOutput.setParseType(1);
                return nodeInputOutput;
            })
            .collect(Collectors.toList());
    }

    private DateTimeFormatter getDateTimeFormatter() {
        ZoneId zoneId = Optional.ofNullable(node)
            .map(SpecNode::getTrigger)
            .map(SpecTrigger::getTimezone)
            .map(zone -> {
                try {
                    return ZoneId.of(zone);
                } catch (Exception ignored) {
                    return null;
                }
            })
            .orElse(ZoneId.systemDefault());
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(zoneId);
    }
}
