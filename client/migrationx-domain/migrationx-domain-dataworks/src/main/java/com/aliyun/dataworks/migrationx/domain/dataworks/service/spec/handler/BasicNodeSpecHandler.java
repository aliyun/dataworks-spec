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

package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.adapter.SpecHandlerContext;
import com.aliyun.dataworks.common.spec.adapter.handler.AbstractEntityHandler;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.Code;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.MultiLanguageScriptingCode;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.LanguageEnum;
import com.aliyun.dataworks.common.spec.domain.enums.ArtifactType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeInstanceModeType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRecurrenceType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRerunModeType;
import com.aliyun.dataworks.common.spec.domain.enums.TriggerType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.interfaces.NodeIO;
import com.aliyun.dataworks.common.spec.domain.interfaces.Output;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDatasource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecRuntimeResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.common.spec.utils.VariableUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeContext;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.nodemarket.AppConfigPack;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.IoParseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.NodeSpecAdapter;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntity;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DefaultNodeTypeUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.FolderUtils;
import com.aliyun.migrationx.common.utils.DateUtils;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.alibaba.fastjson2.JSON;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2023/11/21
 */
@Slf4j
public class BasicNodeSpecHandler extends AbstractEntityHandler<DwNodeEntity, SpecNode> {
    @Override
    public boolean support(DwNodeEntity dwNode) {
        return true;
    }

    protected NodeSpecAdapter getSpecAdapter() {
        return Optional.ofNullable(context)
                .map(SpecHandlerContext::getSpecAdapter)
                .map(adapter -> (NodeSpecAdapter) adapter)
                .orElseThrow(() -> new RuntimeException("SpecAdapter is null"));
    }

    protected boolean matchNodeType(DwNodeEntity dwNode, CodeProgramType nodeType) {
        return Optional.ofNullable(nodeType).map(Enum::name).map(t -> matchNodeType(dwNode, t)).orElse(false);
    }

    protected boolean matchNodeType(DwNodeEntity dwNode, String nodeType) {
        return Optional.ofNullable(dwNode)
                .map(DwNodeEntity::getType)
                .map(type -> StringUtils.equalsIgnoreCase(nodeType, type))
                .orElse(false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SpecNode handle(DwNodeEntity dmNode) {
        Preconditions.checkNotNull(dmNode, "dmNode is null");

        SpecNode specNode = new SpecNode();
        return handle(dmNode, specNode);
    }

    public SpecNode handle(DwNodeEntity dmNode, SpecNode specNode) {
        specNode.setId(dmNode.getUuid());
        specNode.setName(dmNode.getName());
        specNode.setOwner(dmNode.getOwner());
        specNode.setDescription(dmNode.getDescription());
        NodeUseType useType = Optional.ofNullable(dmNode.getNodeUseType()).orElse(NodeUseType.SCHEDULED);
        if (useType == NodeUseType.SKIP) {
            specNode.setRecurrence(NodeRecurrenceType.SKIP);
        }
        Optional.ofNullable(dmNode.getPauseSchedule()).ifPresent(pause -> {
            if (pause) {
                specNode.setRecurrence(NodeRecurrenceType.PAUSE);
            } else {
                specNode.setRecurrence(NodeRecurrenceType.NORMAL);
            }
        });
        Optional.ofNullable(dmNode.getRerunMode()).ifPresent(rerunMode -> {
            switch (rerunMode) {
                case ALL_DENIED: {
                    specNode.setRerunMode(NodeRerunModeType.ALL_DENIED);
                    break;
                }
                case FAILURE_ALLOWED: {
                    specNode.setRerunMode(NodeRerunModeType.FAILURE_ALLOWED);
                    break;
                }
                case ALL_ALLOWED: {
                    specNode.setRerunMode(NodeRerunModeType.ALL_ALLOWED);
                    break;
                }
                default:
                    break;
            }
        });
        specNode.setRerunTimes(dmNode.getTaskRerunTime());
        specNode.setRerunInterval(dmNode.getTaskRerunInterval());
        specNode.setInstanceMode(Optional.ofNullable(dmNode.getStartRightNow()).map(instanceMode -> {
            if (instanceMode) {
                return NodeInstanceModeType.IMMEDIATELY;
            } else {
                return NodeInstanceModeType.T_PLUS_1;
            }
        }).orElse(NodeInstanceModeType.T_PLUS_1));

        specNode.setPriority(dmNode.getPriority());

        specNode.setTimeout(Optional.ofNullable(dmNode.getExtraConfig())
                .map(json -> GsonUtils.fromJsonString(json, new TypeToken<Map<String, Object>>() {}.getType()))
                .map(map -> (Map<String, Object>) map)
                .map(map -> MapUtils.getInteger(map, "alisaTaskKillTimeout")).orElse(null));
        specNode.setRuntimeResource(Optional.ofNullable(dmNode.getResourceGroup()).map(resGroup -> {
            SpecRuntimeResource rt = new SpecRuntimeResource();
            rt.setResourceGroup(resGroup);
            return rt;
        }).orElse(null));

        specNode.setScript(toSpecScript(dmNode, context));
        specNode.setTrigger(toSpecTrigger(dmNode));

        Optional.ofNullable(dmNode.getConnection()).map(ds -> {
            SpecDatasource sds = new SpecDatasource();
            sds.setName(ds);
            return sds;
        }).ifPresent(specNode::setDatasource);
        setNodeInputOutputs(specNode, dmNode, context);
        return specNode;
    }

    protected SpecScript toSpecScript(DwNodeEntity dmNodeBO, SpecHandlerContext context) {
        SpecScript specScript = new SpecScript();
        specScript.setPath(getPath(dmNodeBO, context));
        specScript.setLanguage(getScriptLanguage(dmNodeBO));

        specScript.setParameters(toScriptParameters(dmNodeBO));
        specScript.setRuntime(toSpecScriptRuntime(dmNodeBO));
        specScript.setContent(toSpectScriptContent(dmNodeBO));
        return specScript;
    }

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
     * get script language by code model
     *
     * @param dmNodeBO 节点
     * @return 语言
     */
    private String getScriptLanguage(DwNodeEntity dmNodeBO) {
        LanguageEnum language = Optional.ofNullable(CodeProgramType.getNodeTypeByName(dmNodeBO.getType())).map(nodeType -> {
            switch (nodeType) {
                case SHELL:
                case DIDE_SHELL:
                case CDH_SHELL:
                case EMR_SPARK_SHELL:
                case CDH_SPARK_SHELL:
                case EMR_SHELL:
                case EMR_HIVE_CLI:
                case PERL:
                    return LanguageEnum.SHELL_SCRIPT;
                case EMR_SPARK_SQL:
                    return LanguageEnum.SPARK_SQL;
                case CDH_HIVE:
                case HIVE:
                case EMR_HIVE:
                    return LanguageEnum.HIVE_SQL;
                case EMR_IMPALA:
                case CDH_IMPALA:
                    return LanguageEnum.IMPALA_SQL;
                case CLICK_SQL:
                    return LanguageEnum.CLICKHOUSE_SQL;
                case ODPS_SQL:
                case ODPS_PERL:
                    return LanguageEnum.ODPS_SQL;
                case ODPS_SCRIPT:
                    return LanguageEnum.ODPS_SCRIPT;
                case EMR_PRESTO:
                case CDH_PRESTO:
                    return LanguageEnum.PRESTO_SQL;
                case PYODPS:
                    return LanguageEnum.PYTHON2;
                case PYODPS3:
                    return LanguageEnum.PYTHON3;
                case DATAX2:
                case DATAX:
                case RI:
                case DI:
                    return LanguageEnum.JSON;
                case HOLOGRES_SQL:
                    return LanguageEnum.HOLOGRES_SQL;
                default:
                    return null;
            }
        }).orElse(null);

        if (language != null) {
            return language.getIdentifier();
        }

        CodeModel<Code> cm = CodeModelFactory.getCodeModel(dmNodeBO.getType(), dmNodeBO.getCode());
        return Optional.ofNullable(cm.getCodeModel()).filter(m -> m instanceof MultiLanguageScriptingCode)
                .map(codeModel -> ((MultiLanguageScriptingCode) codeModel).getLanguage()).orElse(null);
    }

    public SpecScriptRuntime toSpecScriptRuntime(DwNodeEntity scr) {
        SpecScriptRuntime sr = new SpecScriptRuntime();
        CodeProgramType type = CodeProgramType.getNodeTypeByName(scr.getType());
        sr.setCommand(type.getName());
        return sr;
    }

    public String toSpectScriptContent(DwNodeEntity dmNodeBO) {
        return null;
    }

    public void setNodeInputOutputs(SpecNode specNode, DwNodeEntity dmNodeBO, SpecHandlerContext context) {
        SpecDepend node = new SpecDepend();
        SpecNode nodeId = new SpecNode();
        nodeId.setId(specNode.getId());
        node.setNodeId(nodeId);
        List<NodeContext> inputCtxList = dmNodeBO.getInputContexts();
        List<Input> inputVariables = ListUtils.emptyIfNull(inputCtxList).stream().map(inCtx -> {
            SpecVariable specVariable = new SpecVariable();
            specVariable.setScope(VariableScopeType.NODE_CONTEXT);
            specVariable.setType(convertParamTypeToVariableType(inCtx.getParamType()));
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
                ref.setType(VariableType.NODE_OUTPUT);
                ref.setScope(VariableScopeType.NODE_CONTEXT);
                specVariable.setReferenceVariable(ref);
            } else {
                log.warn("invalid input context value: {}", inCtx);
            }
            specVariable.setNode(node);
            return (Input) specVariable;
        }).collect(Collectors.toList());

        Optional.ofNullable(specNode.getScript()).ifPresent(scr -> {
            List<SpecVariable> parameters = new ArrayList<>(Optional.ofNullable(scr.getParameters()).orElse(new ArrayList<>()));
            parameters.addAll(ListUtils.emptyIfNull(inputVariables).stream().map(v -> (SpecVariable) v).collect(Collectors.toList()));
            scr.setParameters(parameters);
        });
        specNode.setInputs(ListUtils.emptyIfNull(inputVariables).stream()
                .map(v -> (SpecVariable) v).map(SpecVariable::getReferenceVariable)
                .filter(Objects::nonNull).collect(Collectors.toList()));
        specNode.getInputs().addAll(getNodeInputs(dmNodeBO));

        List<NodeContext> outputCtxList = dmNodeBO.getOutputContexts();
        List<Output> outputVariables = ListUtils.emptyIfNull(outputCtxList).stream().map(outCtx -> {
            SpecVariable specVariable = new SpecVariable();
            specVariable.setScope(VariableScopeType.NODE_CONTEXT);
            specVariable.setType(convertParamTypeToVariableType(outCtx.getParamType()));
            specVariable.setName(outCtx.getParamName());
            specVariable.setValue(outCtx.getParamValue());
            specVariable.setNode(node);
            specVariable.setDescription(outCtx.getDescription());
            return (Output) specVariable;
        }).collect(Collectors.toList());
        specNode.setOutputs(outputVariables);
        specNode.getOutputs().addAll(getNodeOutputs(dmNodeBO, context));

        // 进行排序，这样每次的spec里的input/output顺序是稳定的
        sortNodeInputOutput(specNode.getInputs());
        sortNodeInputOutput(specNode.getOutputs());
    }

    private <T extends NodeIO> void sortNodeInputOutput(List<T> nodeInputOutputs) {
        if (CollectionUtils.isEmpty(nodeInputOutputs)) {
            return;
        }

        nodeInputOutputs.sort(Comparator.comparing(x -> {
            if (x instanceof SpecNodeOutput) {
                return ((SpecNodeOutput) x).getArtifactType() + ((SpecNodeOutput) x).getData();
            } else if (x instanceof SpecTable) {
                return ((SpecTable) x).getArtifactType() + ((SpecTable) x).getName();
            } else if (x instanceof SpecVariable) {
                return ((SpecVariable) x).getArtifactType() + ((SpecVariable) x).getName();
            } else {
                return x.toString();
            }
        }));
    }

    private static VariableType convertParamTypeToVariableType(Integer paramType) {
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

    private SpecTrigger toSpecTrigger(DwNodeEntity dmNodeBO) {
        if (dmNodeBO == null) {
            return null;
        }

        SpecTrigger specTrigger = new SpecTrigger();
        NodeUseType useType = dmNodeBO.getNodeUseType();
        switch (useType) {
            case SKIP:
            case SCHEDULED: {
                specTrigger.setType(TriggerType.SCHEDULER);
                specTrigger.setCron(dmNodeBO.getCronExpress());
                specTrigger.setStartTime(DateUtils.convertDateToString(dmNodeBO.getStartEffectDate()));
                specTrigger.setEndTime(DateUtils.convertDateToString(dmNodeBO.getEndEffectDate()));
                break;
            }
            default: {
                specTrigger.setType(TriggerType.MANUAL);
                break;
            }
        }
        return specTrigger;
    }

    private Map<String, AppConfigPack> getConfigPack() {
        try {
            InputStream inputStream = BasicNodeSpecHandler.class.getResourceAsStream("/nodemarket/config_pack_cache.json");
            return JSON.parseObject(
                    IOUtils.toString(inputStream, StandardCharsets.UTF_8),
                    new TypeToken<Map<String, AppConfigPack>>() {}.getType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getPath(DwNodeEntity dwNode, SpecHandlerContext context) {
        CodeProgramType type = CodeProgramType.getNodeTypeByName(dwNode.getType());
        if (type == null) {
            return dwNode.getFolder();
        }
        if (context == null) {
            log.error("getPath context null");
            return dwNode.getFolder();
        }

        try {
            return FolderUtils.normalizeConfigPackPathToSpec(
                    type.getCode(), dwNode.getFolder(), getConfigPack(), context.getLocale()) + "/" + dwNode.getName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends NodeIO> List<T> getNodeInputs(DwNodeEntity dmNodeBO) {
        return toNodeIos(dmNodeBO.getInputs());
    }

    public List<DwNodeEntity> getInnerNodes(DwNodeEntity dwNode) {
        return new ArrayList<>(ListUtils.emptyIfNull(dwNode.getInnerNodes()));
    }

    @SuppressWarnings("unchecked")
    private <T extends NodeIO> List<T> toNodeIos(List<NodeIo> ios) {
        return ListUtils.emptyIfNull(ios).stream().map(out -> {
            SpecNodeOutput a = new SpecNodeOutput();
            a.setArtifactType(ArtifactType.NODE_OUTPUT);
            a.setData(out.getData());
            a.setRefTableName(out.getRefTableName());
            a.setIsDefault(Objects.equals(IoParseType.SYSTEM.getCode(), out.getParseType()));
            return (T) a;
        }).collect(Collectors.toList());
    }

    protected <T extends NodeIO> List<T> getNodeOutputs(DwNodeEntity dmNodeBO, SpecHandlerContext context) {
        return toNodeIos(dmNodeBO.getOutputs());
    }
}