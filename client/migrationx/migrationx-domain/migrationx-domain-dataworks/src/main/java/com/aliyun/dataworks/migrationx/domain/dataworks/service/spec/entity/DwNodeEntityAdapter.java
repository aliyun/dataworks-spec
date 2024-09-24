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

package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ComponentSqlCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ComponentSqlCode.ComponentInfo;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.SqlComponentCode;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeContext;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.RerunMode;

import lombok.Getter;
import org.apache.commons.collections4.ListUtils;

/**
 * @author 聿剑
 * @date 2024/6/18
 */
@Getter
public class DwNodeEntityAdapter implements DwNodeEntity {
    private final DwNode dwNode;

    public DwNodeEntityAdapter(DwNode dwNode) {
        this.dwNode = dwNode;
    }

    @Override
    public String getUuid() {
        return dwNode.getGlobalUuid();
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
        return dwNode.getResourceGroupName();
    }

    @Override
    public String getName() {
        return dwNode.getName();
    }

    @Override
    public String getType() {
        return dwNode.getType();
    }

    @Override
    public Integer getTypeId() {
        return Optional.ofNullable(CodeProgramType.getNodeTypeByName(getType())).map(CodeProgramType::getCode).orElse(null);
    }

    @Override
    public String getCronExpress() {
        return dwNode.getCronExpress();
    }

    @Override
    public Date getStartEffectDate() {
        return dwNode.getStartEffectDate();
    }

    @Override
    public Integer getIsAutoParse() {
        return dwNode.getIsAutoParse();
    }

    @Override
    public Date getEndEffectDate() {
        return dwNode.getEndEffectDate();
    }

    @Override
    public String getResourceGroup() {
        return dwNode.getResourceGroup();
    }

    @Override
    public String getDiResourceGroup() {
        return dwNode.getDiResourceGroup();
    }

    @Override
    public String getDiResourceGroupName() {
        return dwNode.getDiResourceGroupName();
    }

    @Override
    public String getCodeMode() {
        return dwNode.getCodeMode();
    }

    @Override
    public Boolean getStartRightNow() {
        return dwNode.getStartRightNow();
    }

    @Override
    public RerunMode getRerunMode() {
        return dwNode.getRerunMode();
    }

    @Override
    public Boolean getPauseSchedule() {
        return dwNode.getPauseSchedule();
    }

    @Override
    public NodeUseType getNodeUseType() {
        return dwNode.getNodeUseType();
    }

    @Override
    public String getRef() {
        return dwNode.getRef();
    }

    @Override
    public String getFolder() {
        return dwNode.getFolder();
    }

    @Override
    public Boolean getRoot() {
        return dwNode.getRoot();
    }

    @Override
    public String getConnection() {
        return dwNode.getConnection();
    }

    @Override
    public String getCode() {
        return dwNode.getCode();
    }

    @Override
    public String getParameter() {
        return dwNode.getParameter();
    }

    @Override
    public List<NodeContext> getInputContexts() {
        return dwNode.getInputContexts();
    }

    @Override
    public List<NodeContext> getOutputContexts() {
        return dwNode.getOutputContexts();
    }

    @Override
    public List<NodeIo> getInputs() {
        return dwNode.getInputs();
    }

    @Override
    public List<NodeIo> getOutputs() {
        return dwNode.getOutputs();
    }

    @Override
    public List<DwNodeEntity> getInnerNodes() {
        return ListUtils.emptyIfNull(dwNode.getInnerNodes()).stream().map(n -> new DwNodeEntityAdapter((DwNode) n)).collect(Collectors.toList());
    }

    @Override
    public String getDescription() {
        return dwNode.getDescription();
    }

    @Override
    public Integer getTaskRerunTime() {
        return dwNode.getTaskRerunTime();
    }

    @Override
    public Integer getTaskRerunInterval() {
        return dwNode.getTaskRerunInterval();
    }

    @Override
    public Integer getDependentType() {
        return dwNode.getDependentType();
    }

    @Override
    public Integer getCycleType() {
        return dwNode.getCycleType();
    }

    @Override
    public Date getLastModifyTime() {
        return dwNode.getLastModifyTime();
    }

    @Override
    public String getLastModifyUser() {
        return dwNode.getLastModifyUser();
    }

    @Override
    public Integer getMultiInstCheckType() {
        return dwNode.getMultiInstCheckType();
    }

    @Override
    public Integer getPriority() {
        return dwNode.getPriority();
    }

    @Override
    public String getDependentDataNode() {
        return dwNode.getDependentDataNode();
    }

    @Override
    public String getOwner() {
        return dwNode.getOwner();
    }

    @Override
    public String getOwnerName() {
        return dwNode.getOwnerName();
    }

    @Override
    public String getExtraConfig() {
        return dwNode.getExtraConfig();
    }

    @Override
    public String getExtraContent() {
        return dwNode.getExtraContent();
    }

    @Override
    public String getTtContent() {
        return dwNode.getTtContent();
    }

    @Override
    public String getAdvanceSettings() {
        return dwNode.getAdvanceSettings();
    }

    @Override
    public String getExtend() {
        return dwNode.getExtend();
    }

    @Override
    public SpecComponent getComponent() {
        return Optional.ofNullable(CodeProgramType.getNodeTypeByName(getType()))
                .map(type -> {
                    if (CodeProgramType.COMPONENT_SQL.name().equalsIgnoreCase(type.name())) {
                        return getComponentSql();
                    }
                    return getSqlComponent();
                })
                .orElse(null);
    }

    @Override
    public String getOrigin() {
        return dwNode.getOrigin();
    }

    @Override
    public String getWorkflowName() {
        return dwNode.getWorkflowRef() == null ? null : dwNode.getWorkflowRef().getName();
    }

    private SpecComponent getSqlComponent() {
        CodeModel<SqlComponentCode> codeModel = CodeModelFactory.getCodeModel(CodeProgramType.SQL_COMPONENT.name(), getCode());
        SpecComponent com = Optional.ofNullable(codeModel.getCodeModel()).map(SqlComponentCode::getConfig).orElse(new SpecComponent());
        com.setId(getUuid());
        com.setName(getName());
        com.setOwner(getOwner());
        com.setDescription(getDescription());
        com.setInputs(com.getInputs());
        com.setOutputs(com.getOutputs());

        SpecScript script = new SpecScript();
        script.setContent(codeModel.getCodeModel().getCode());
        SpecScriptRuntime runtime = new SpecScriptRuntime();
        runtime.setCommand(CodeProgramType.SQL_COMPONENT.getName());
        runtime.setCommandTypeId(CodeProgramType.SQL_COMPONENT.getCode());
        script.setRuntime(runtime);
        com.setScript(script);
        return com;
    }

    private SpecComponent getComponentSql() {
        CodeModel<ComponentSqlCode> codeModel = CodeModelFactory.getCodeModel(CodeProgramType.COMPONENT_SQL.name(), getCode());
        SpecComponent com = Optional.ofNullable(codeModel.getCodeModel().getConfig()).orElse(new SpecComponent());
        Optional.ofNullable(codeModel.getCodeModel().getComponent()).map(ComponentInfo::getId).map(String::valueOf).ifPresent(com::setId);
        Optional.ofNullable(codeModel.getCodeModel().getComponent()).map(ComponentInfo::getName).ifPresent(com::setName);

        Map<String, Object> metadata = new HashMap<>();
        Optional.ofNullable(codeModel.getCodeModel())
                .map(ComponentSqlCode::getComponent)
                .map(ComponentInfo::getVersion)
                .ifPresent(version -> metadata.put("version", version));
        metadata.put("id", com.getId());
        com.setMetadata(metadata);
        return com;
    }
}