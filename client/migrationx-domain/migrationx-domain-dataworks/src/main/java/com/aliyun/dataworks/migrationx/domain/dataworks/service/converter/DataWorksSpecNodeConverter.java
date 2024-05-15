package com.aliyun.dataworks.migrationx.domain.dataworks.service.converter;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.nodemodel.DataWorksNodeAdapter;
import com.aliyun.dataworks.common.spec.domain.dw.nodemodel.DwNodeDependentTypeInfo;
import com.aliyun.dataworks.common.spec.domain.dw.nodemodel.OutputContext;
import com.aliyun.dataworks.common.spec.domain.enums.ArtifactType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeInstanceModeType;
import com.aliyun.dataworks.common.spec.domain.enums.NodeRerunModeType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.ref.SpecArtifact;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDatasource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFile;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecRuntimeResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.file.SpecLocalFile;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.client.File;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.client.FileDetail;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.client.FileNodeCfg;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.client.FileNodeInputOutput;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.client.FileNodeInputOutputContext;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.IoParseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.CronExpressUtil;
import com.aliyun.migrationx.common.utils.DateUtils;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * DataWorks SpecNode 转化为 DataWorks对象
 *
 * @author 戒迷
 * @date 2024/4/16
 */
@Slf4j
public class DataWorksSpecNodeConverter {
    private DataWorksSpecNodeConverter() {
        throw new IllegalStateException("Utility class");
    }

    public static FileDetail functionSpecToFileDetail(Specification<DataWorksWorkflowSpec> spec, String resourceId) {
        FileDetail fileDetail = new FileDetail();
        File file = functionSpecToFile(spec, resourceId);
        fileDetail.setFile(file);
        fileDetail.setNodeCfg(initFileNodeCfgByFile(file));
        return fileDetail;
    }

    public static FileDetail resourceSpecToFileDetail(Specification<DataWorksWorkflowSpec> spec, String resourceId) {
        FileDetail fileDetail = new FileDetail();
        File file = resourceSpecToFile(spec, resourceId);
        fileDetail.setFile(file);
        fileDetail.setNodeCfg(initFileNodeCfgByFile(file));
        return fileDetail;
    }

    private static FileNodeCfg initFileNodeCfgByFile(File file) {
        FileNodeCfg fileNodeCfg = new FileNodeCfg();
        fileNodeCfg.setNodeName(file.getFileName());
        fileNodeCfg.setNodeId(file.getFileId());
        return fileNodeCfg;
    }

    public static FileDetail resourceSpecToFileDetail(Specification<DataWorksWorkflowSpec> spec) {
        return resourceSpecToFileDetail(spec, null);
    }

    public static FileDetail functionSpecToFileDetail(Specification<DataWorksWorkflowSpec> spec) {
        return functionSpecToFileDetail(spec, null);
    }

    private static File functionSpecToFile(Specification<DataWorksWorkflowSpec> spec, String functionId) {
        DataWorksWorkflowSpec dataWorksWorkflowSpec = spec.getSpec();
        if (spec.getSpec() == null) {
            log.warn("dataworks resource spec is null");
            return null;
        }

        return ListUtils.emptyIfNull(dataWorksWorkflowSpec.getFunctions()).stream()
            .filter(x -> StringUtils.isBlank(functionId) || StringUtils.equals(x.getId(), functionId))
            .findFirst()
            .map(specFunc -> {
                File dwFunc = new File();
                dwFunc.setFileName(specFunc.getName());
                dwFunc.setOwner(Optional.ofNullable(specFunc.getMetadata()).map(m -> (String)m.get("owner")).orElse(null));
                dwFunc.setFileTypeStr(Optional.ofNullable(specFunc.getScript()).map(SpecScript::getRuntime).map(SpecScriptRuntime::getCommand)
                    .orElse(null));
                dwFunc.setConnName(Optional.ofNullable(specFunc.getDatasource()).map(SpecDatasource::getName).orElse(null));
                return dwFunc;
            }).orElse(null);
    }

    private static File resourceSpecToFile(Specification<DataWorksWorkflowSpec> spec, String resourceId) {
        DataWorksWorkflowSpec dataWorksWorkflowSpec = spec.getSpec();
        if (spec.getSpec() == null) {
            log.warn("dataworks resource spec is null");
            return null;
        }

        return ListUtils.emptyIfNull(dataWorksWorkflowSpec.getFileResources()).stream()
            .filter(x -> StringUtils.isBlank(resourceId) || StringUtils.equals(x.getId(), resourceId))
            .findFirst()
            .map(specRes -> {
                File dwRes = new File();
                dwRes.setFileName(specRes.getName());
                dwRes.setOwner(Optional.ofNullable(specRes.getMetadata()).map(m -> (String)m.get("owner")).orElse(null));
                String fileName = Optional.ofNullable(specRes.getFile()).filter(SpecLocalFile.class::isInstance).map(f -> (SpecLocalFile)f)
                    .map(f -> Paths.get(f.getPath()).toFile().getName()).orElse(specRes.getName());
                dwRes.setFileTypeStr(Optional.ofNullable(specRes.getScript()).map(SpecScript::getRuntime).map(SpecScriptRuntime::getCommand)
                    .orElse(null));
                dwRes.setOriginResourceName(fileName);
                dwRes.setConnName(Optional.ofNullable(specRes.getDatasource()).map(SpecDatasource::getName).orElse(null));
                return dwRes;
            }).orElse(null);
    }

    public static FileDetail nodeSpecToFileDetail(Specification<DataWorksWorkflowSpec> spec, String nodeId) {
        FileDetail fileDetail = new FileDetail();
        fileDetail.setFile(nodeSpecToFile(spec, nodeId));
        fileDetail.setNodeCfg(nodeSpecToNodeCfg(spec, nodeId));
        return fileDetail;
    }

    public static FileDetail nodeSpecToFileDetail(Specification<DataWorksWorkflowSpec> spec) {
        FileDetail fileDetail = new FileDetail();
        fileDetail.setFile(nodeSpecToFile(spec, null));
        fileDetail.setNodeCfg(nodeSpecToNodeCfg(spec, null));
        return fileDetail;
    }

    public static File nodeSpecToFile(Specification<DataWorksWorkflowSpec> spec, String nodeId) {
        DataWorksWorkflowSpec dataWorksWorkflowSpec = spec.getSpec();
        if (spec.getSpec() == null) {
            log.warn("dataworks workflow spec is null");
            return null;
        }

        return Optional.ofNullable(getMatchSpecNode(dataWorksWorkflowSpec, nodeId)).map(specNode -> {
            File file = new File();
            file.setAppId(null);
            file.setBizId(null);
            file.setCloudUuid(null);
            file.setCommitStatus(null);
            file.setConnName(Optional.ofNullable(specNode.getDatasource()).map(SpecDatasource::getName).orElse(null));
            file.setContent(Optional.ofNullable(specNode.getScript()).map(SpecScript::getContent).orElse(null));
            file.setCreateTime(null);
            file.setCreateUser(null);
            file.setCurrentVersion(null);
            file.setExtend(null);
            file.setExtraContent(null);
            file.setFileDagUrl(null);
            file.setFileDelete(null);
            file.setFileDesc(specNode.getDescription());
            file.setFileFolderId(null);
            file.setFileFolderPath(Optional.ofNullable(specNode.getScript()).map(SpecFile::getPath).orElse(null));
            file.setFileId(Long.valueOf(specNode.getId()));
            file.setFileLockStatus(null);
            file.setFileLockUser(null);
            file.setFileLockUserName(null);
            file.setFileName(specNode.getName());
            file.setFilePublish(null);
            file.setFileTypeStr(Optional.ofNullable(specNode.getScript())
                .map(SpecScript::getRuntime).map(SpecScriptRuntime::getCommand).orElse(null));
            file.setGalaxyResultTableSql(null);
            file.setGalaxySourceTableSql(null);
            file.setGalaxyTaskConfig(null);
            file.setInstanceInfo(null);
            file.setIsAutoParse(null);
            file.setIsLarge(null);
            file.setIsOdps(null);
            file.setIsProtected(null);
            file.setLabelId(null);
            file.setLastEditTime(null);
            file.setLastEditUser(null);
            file.setLastEditUserName(null);
            file.setLimit(null);
            file.setLocked(null);
            file.setLockedBy(null);
            file.setLockedByName(null);
            file.setNodeId(null);
            file.setOriginResourceName(null);
            file.setOwner(specNode.getOwner());
            file.setOwnerName(null);
            file.setParentId(null);
            file.setParentType(null);
            file.setPosition(null);
            file.setReference(null);
            file.setRegion(null);
            file.setSourceApp(null);
            file.setStart(null);
            file.setTenantId(null);
            file.setTtContent(null);
            if (SpecKind.CYCLE_WORKFLOW.getLabel().equals(spec.getKind())) {
                file.setUseType(NodeUseType.SCHEDULED.getValue());
            } else if (SpecKind.MANUAL_WORKFLOW.getLabel().equals(spec.getKind())) {
                file.setUseType(NodeUseType.MANUAL_WORKFLOW.getValue());
            } else if (SpecKind.TEMPORARY_WORKFLOW.getLabel().equals(spec.getKind())) {
                file.setUseType(NodeUseType.MANUAL.getValue());
            }
            file.setWorkspaceUrl(null);
            file.setIgnoreLock(null);

            return file;
        }).orElse(null);
    }

    public static SpecNode getMatchSpecNode(DataWorksWorkflowSpec dataWorksWorkflowSpec, String nodeId) {
        for (SpecNode node : dataWorksWorkflowSpec.getNodes()) {
            if (StringUtils.isBlank(nodeId) || StringUtils.equalsIgnoreCase(node.getId(), nodeId)) {
                return node;
            }
            for (SpecNode innerNode : node.getInnerNodes()) {
                if (StringUtils.isBlank(nodeId) || StringUtils.equalsIgnoreCase(innerNode.getId(), nodeId)) {
                    return innerNode;
                }
            }
        }
        return null;
    }

    /**
     * 处理Node类型的Spec
     *
     * @param spec
     * @param nodeId
     * @return
     */
    public static FileNodeCfg nodeSpecToNodeCfg(Specification<DataWorksWorkflowSpec> spec, String nodeId) {
        DataWorksWorkflowSpec dataWorksWorkflowSpec = spec.getSpec();
        if (spec.getSpec() == null) {
            log.warn("dataworks workflow spec is null");
            return null;
        }

        return Optional.ofNullable(getMatchSpecNode(dataWorksWorkflowSpec, nodeId)).map(specNode -> {
                FileNodeCfg nodeCfg = new FileNodeCfg();
                nodeCfg.setAppId(null);
                nodeCfg.setBaselineId(null);
                nodeCfg.setCreateTime(null);
                nodeCfg.setCreateUser(null);
                nodeCfg.setCronExpress(Optional.ofNullable(specNode.getTrigger()).map(SpecTrigger::getCron).orElse(null));
                nodeCfg.setCycleType(CronExpressUtil.parseCronToCycleType(nodeCfg.getCronExpress()));
                nodeCfg.setDataxFileId(null);
                nodeCfg.setDataxFileVersion(null);

                nodeCfg.setDependentType(0);
                nodeCfg.setDescription(specNode.getDescription());
                nodeCfg.setEndEffectDate(Optional.ofNullable(specNode.getTrigger()).map(SpecTrigger::getEndTime)
                    .map(DateUtils::convertStringToDate).orElse(null));
                nodeCfg.setFileId(Optional.ofNullable(specNode.getId()).map(Long::valueOf).orElse(null));

                nodeCfg.setIsAutoParse(null);
                nodeCfg.setIsStop(null);
                nodeCfg.setLastModifyTime(null);
                nodeCfg.setLastModifyUser(null);
                nodeCfg.setMultiinstCheckType(null);
                nodeCfg.setNodeId(Long.valueOf(specNode.getId()));
                nodeCfg.setNodeName(specNode.getName());
                nodeCfg.setOwner(specNode.getOwner());
                nodeCfg.setPriority(specNode.getPriority());
                nodeCfg.setResgroupId(Optional.ofNullable(specNode.getRuntimeResource()).map(SpecRuntimeResource::getResourceGroupId)
                    .map(Long::valueOf).orElse(null));
                nodeCfg.setStartEffectDate(Optional.ofNullable(specNode.getTrigger()).map(SpecTrigger::getStartTime)
                    .map(DateUtils::convertStringToDate).orElse(null));
                nodeCfg.setStartRightNow(Optional.ofNullable(specNode.getInstanceMode())
                    .map(instanceMode -> instanceMode == NodeInstanceModeType.IMMEDIATELY)
                    .orElse(false));
                nodeCfg.setTaskRerunInterval(specNode.getRerunInterval());
                nodeCfg.setTaskRerunTime(specNode.getRerunTimes());

                setRerunMode(specNode, nodeCfg);
                setInputOutputList(specNode, nodeCfg);
                setByAdaptor(spec, specNode, nodeCfg);
                return nodeCfg;
            }).orElse(null);
    }

    private static void setRerunMode(SpecNode specNode, FileNodeCfg nodeCfg) {
        if (null == specNode.getRerunMode() || NodeRerunModeType.ALL_ALLOWED == specNode.getRerunMode()) {
            nodeCfg.setReRunAble(1);
        } else if (NodeRerunModeType.ALL_DENIED == specNode.getRerunMode()) {
            nodeCfg.setReRunAble(2);
        } else if (NodeRerunModeType.FAILURE_ALLOWED == specNode.getRerunMode()) {
            nodeCfg.setReRunAble(0);
        }
    }

    private static void setInputOutputList(SpecNode specNode, FileNodeCfg nodeCfg) {
        nodeCfg.setInputList(ListUtils.emptyIfNull(specNode.getInputs()).stream()
            .filter(SpecArtifact.class::isInstance)
            .map(io -> (SpecArtifact)io)
            .filter(io -> Objects.equals(io.getArtifactType(), ArtifactType.NODE_OUTPUT))
            .map(io -> (SpecNodeOutput)io)
            .map(io -> {
                FileNodeInputOutput in = new FileNodeInputOutput();
                in.setStr(io.getData());
                in.setParseType(IoParseType.MANUAL.getCode());
                in.setRefTableName(io.getRefTableName());
                return in;
            }).collect(Collectors.toList()));
        nodeCfg.setInputByInputList();

        nodeCfg.setOutputList(ListUtils.emptyIfNull(specNode.getOutputs()).stream()
            .filter(SpecArtifact.class::isInstance)
            .map(io -> (SpecArtifact)io)
            .filter(io -> Objects.equals(io.getArtifactType(), ArtifactType.NODE_OUTPUT))
            .map(io -> (SpecNodeOutput)io)
            .map(io -> {
                FileNodeInputOutput out = new FileNodeInputOutput();
                out.setStr(io.getData());
                out.setParseType(IoParseType.MANUAL.getCode());
                out.setRefTableName(io.getRefTableName());
                return out;
            }).collect(Collectors.toList()));
        nodeCfg.setOutputByOutputList();
    }

    private static void setByAdaptor(Specification<DataWorksWorkflowSpec> spec, SpecNode specNode, FileNodeCfg nodeCfg) {
        DataWorksNodeAdapter adapter = new DataWorksNodeAdapter(spec, specNode);

        ListUtils.emptyIfNull(spec.getSpec().getFlow()).stream()
            .filter(f -> StringUtils.equals(specNode.getId(), Optional.ofNullable(f.getNodeId()).map(SpecRefEntity::getId).orElse(null)))
            .findFirst().ifPresent(flow -> {
                DwNodeDependentTypeInfo depInfo = adapter.getDependentType(list -> null);
                nodeCfg.setDependentType(depInfo.getDependentType());
                if (CollectionUtils.isNotEmpty(depInfo.getDependentNodeOutputList())) {
                    nodeCfg.setDependentDataNode(Joiner.on(",").join(depInfo.getDependentNodeOutputList()));
                }
            });

        nodeCfg.setInputContextList(ListUtils.emptyIfNull(adapter.getInputContexts()).stream().map(ctx -> {
            FileNodeInputOutputContext nc = new FileNodeInputOutputContext();
            nc.setType(0); // input ctx
            nc.setParamName(ctx.getKey());
            nc.setParseType(IoParseType.MANUAL.getCode());
            nc.setParamValue(ctx.getRefKey());
            return nc;
        }).collect(Collectors.toList()));
        nodeCfg.setOutputContextList(ListUtils.emptyIfNull(adapter.getOutputContexts()).stream().map(ctx -> {
            FileNodeInputOutputContext nc = new FileNodeInputOutputContext();
            nc.setType(1); // output ctx
            nc.setParamName(ctx.getKey());
            nc.setParseType(IoParseType.MANUAL.getCode());
            nc.setParamValue(ctx.getValueExpr());

            if (StringUtils.equalsIgnoreCase(OutputContext.CTX_TYPE_CONST, ctx.getCtxType())) {
                nc.setParamType(1);
            } else if (StringUtils.equalsIgnoreCase(OutputContext.CTX_TYPE_CONST_SYSTEM_VARIABLE, ctx.getCtxType())
                || StringUtils.equalsIgnoreCase(OutputContext.CTX_TYPE_SCRIPT_OUTPUTS, ctx.getCtxType())) {
                nc.setParamType(2);
                nc.setParseType(IoParseType.SYSTEM.getCode());
            } else if (StringUtils.equalsIgnoreCase(OutputContext.CTX_TYPE_PARAMETER_NODE_OUTPUTS, ctx.getCtxType())) {
                nc.setParamType(3);
            }
            return nc;
        }).collect(Collectors.toList()));
        nodeCfg.setParaValue(adapter.getParaValue());
        nodeCfg.setExtConfig(adapter.getExtConfig());
    }

}
