package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;

import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFile;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponentParameter;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeContext;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.RerunMode;
import com.aliyun.migrationx.common.utils.BeanUtils;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.apache.commons.io.FilenameUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2025-11-13
 */
@Data
public class SpecComponentEntityAdapter implements DwNodeEntity {

    private SpecComponent component;

    @Override
    public String getUuid() {
        return Optional.ofNullable(component).map(SpecRefEntity::getId).orElse(null);
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
    public String getName() {
        return Optional.ofNullable(component).map(SpecComponent::getName).orElse(null);
    }

    @Override
    public String getType() {
        Optional<SpecScriptRuntime> scriptRuntime = Optional.ofNullable(component)
            .map(SpecComponent::getScript)
            .map(SpecScript::getRuntime);
        return scriptRuntime.map(SpecScriptRuntime::getCommand)
            .orElseGet(() -> scriptRuntime.map(SpecScriptRuntime::getCommandTypeId)
                .map(CodeProgramType::getNodeTypeByCode)
                .map(CodeProgramType::name)
                .orElse(null));
    }

    @Override
    public Integer getTypeId() {
        Optional<SpecScriptRuntime> scriptRuntime = Optional.ofNullable(component)
            .map(SpecComponent::getScript)
            .map(SpecScript::getRuntime);
        return scriptRuntime.map(SpecScriptRuntime::getCommandTypeId)
            .orElseGet(() -> scriptRuntime.map(SpecScriptRuntime::getCommand)
                .map(CodeProgramType::getNodeTypeByName)
                .map(CodeProgramType::getCode)
                .orElse(null));
    }

    @Override
    public String getCronExpress() {
        return null;
    }

    @Override
    public Date getStartEffectDate() {
        return null;
    }

    @Override
    public Integer getIsAutoParse() {
        return null;
    }

    @Override
    public Date getEndEffectDate() {
        return null;
    }

    @Override
    public String getResourceGroup() {
        return null;
    }

    @Override
    public String getDiResourceGroup() {
        return null;
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
        return null;
    }

    @Override
    public RerunMode getRerunMode() {
        return null;
    }

    @Override
    public Integer getNodeType() {
        return DwNodeEntity.super.getNodeType();
    }

    @Override
    public Boolean getPauseSchedule() {
        return null;
    }

    @Override
    public NodeUseType getNodeUseType() {
        return NodeUseType.COMPONENT;
    }

    @Override
    public String getRef() {
        return null;
    }

    @Override
    public String getFolder() {
        return Optional.ofNullable(component)
            .map(SpecComponent::getScript)
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
        return null;
    }

    @Override
    public String getCode() {
        String content = Optional.ofNullable(component)
            .map(SpecComponent::getScript)
            .map(SpecScript::getContent)
            .orElse(null);
        ComponentConfig componentConfig = Optional.ofNullable(component)
            .map(c -> {
                ComponentConfig config = new ComponentConfig();
                BeanUtils.copyProperties(c, config);
                return config;
            })
            .orElse(null);
        JSONObject code = new JSONObject();
        code.put("code", content);
        code.put("config", componentConfig);
        return code.toJSONString();
    }

    @Override
    public String getParameter() {
        return Optional.ofNullable(component)
            .map(SpecComponent::getScript)
            .map(SpecScript::getParameters)
            .map(paramList -> paramList.stream()
                .filter(param -> VariableScopeType.NODE_PARAMETER.equals(param.getScope()))
                .map(param -> param.getName() + "=" + param.getValue())
                .collect(Collectors.joining(" ")))
            .orElse(null);
    }

    @Override
    public List<NodeContext> getInputContexts() {
        return null;
    }

    @Override
    public List<NodeContext> getOutputContexts() {
        return null;
    }

    @Override
    public List<NodeIo> getInputs() {
        return null;
    }

    @Override
    public List<NodeIo> getOutputs() {
        return null;
    }

    @Override
    public List<DwNodeEntity> getInnerNodes() {
        return null;
    }

    @Override
    public String getDescription() {
        return Optional.ofNullable(component)
            .map(SpecComponent::getDescription)
            .orElse(null);
    }

    @Override
    public Integer getTaskRerunTime() {
        return null;
    }

    @Override
    public Integer getTaskRerunInterval() {
        return null;
    }

    @Override
    public Integer getDependentType() {
        return null;
    }

    @Override
    public Integer getCycleType() {
        return null;
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
        return null;
    }

    @Override
    public String getDependentDataNode() {
        return null;
    }

    @Override
    public String getOwner() {
        return Optional.ofNullable(component)
            .map(SpecComponent::getOwner)
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
        return null;
    }

    @Override
    public String getExtend() {
        return null;
    }

    @Override
    public SpecComponent getComponent() {
        return component;
    }

    @Override
    public Integer getStreamLaunchMode() {
        return DwNodeEntity.super.getStreamLaunchMode();
    }

    @Override
    public Boolean getIgnoreBranchConditionSkip() {
        return DwNodeEntity.super.getIgnoreBranchConditionSkip();
    }

    /**
     * 超时时间 单位小时
     *
     * @return 超时时间
     */
    @Override
    public Integer getAlisaTaskKillTimeout() {
        return DwNodeEntity.super.getAlisaTaskKillTimeout();
    }

    @Override
    public Long getParentId() {
        return DwNodeEntity.super.getParentId();
    }

    @Override
    public String getCu() {
        return DwNodeEntity.super.getCu();
    }

    @Override
    public String getImageId() {
        return DwNodeEntity.super.getImageId();
    }

    @Data
    static class ComponentConfig {
        private String name;
        private String owner;
        @JSONField(name = "inputs", alternateNames = {"input"})
        @com.alibaba.fastjson.annotation.JSONField(name = "inputs", alternateNames = {"input"})
        @SerializedName(value = "inputs", alternate = "input")
        private List<SpecComponentParameter> inputs;
        @JSONField(name = "outputs", alternateNames = {"output"})
        @com.alibaba.fastjson.annotation.JSONField(name = "outputs", alternateNames = {"output"})
        @SerializedName(value = "outputs", alternate = "output")
        private List<SpecComponentParameter> outputs;
    }
}
