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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.SpecConstants;
import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.Code;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ComponentSqlCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ControllerBranchCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ControllerBranchCode.Branch;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ControllerJoinCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.DataIntegrationCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrAllocationSpec;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrJobType;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrLauncher;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.MultiLanguageScriptingCode;
import com.aliyun.dataworks.common.spec.domain.dw.nodemodel.DataWorksNodeAdapter.Context;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.ProductModule;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.interfaces.LabelEnum;
import com.aliyun.dataworks.common.spec.domain.noref.SpecBranch;
import com.aliyun.dataworks.common.spec.domain.noref.SpecLogic;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.emr.EmrJobExecuteMode;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.emr.EmrJobSubmitMode;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.aliyun.dataworks.common.spec.utils.ReflectUtils;

import com.google.gson.JsonObject;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 聿剑
 * @date 2023/11/9
 */
public class DataWorksNodeCodeAdapter implements DataWorksNodeAdapterContextAware {
    private static final Logger log = LoggerFactory.getLogger(DataWorksNodeCodeAdapter.class);
    private static final String LOGIC_AND = "and";
    private static final String LOGIC_OR = "or";
    private static final List<String> JOIN_BRANCH_LOGICS = Arrays.asList(LOGIC_OR, LOGIC_AND);

    private final SpecEntityDelegate<? extends SpecRefEntity> delegate;
    private Context context;

    public DataWorksNodeCodeAdapter(SpecRefEntity entity) {
        this.delegate = new SpecEntityDelegate<>(entity);
    }

    public String getCode() {
        SpecScript script = Optional.ofNullable(delegate.getScript()).orElseThrow(
            () -> new SpecException(SpecErrorCode.PARSE_ERROR, "node.script is null"));

        SpecScriptRuntime runtime = Optional.ofNullable(script.getRuntime()).orElseThrow(
                () -> new SpecException(SpecErrorCode.PARSE_ERROR, "node.script.runtime is null"));

        try {
            String command = runtime.getCommand();
            CodeModel<Code> codeModel = CodeModelFactory.getCodeModel(command, null);
            Code code = codeModel.getCodeModel();
            Class<? extends Code> codeClass = code.getClass();

            // logics of get code content for special node code
            if (MultiLanguageScriptingCode.class.equals(codeClass)) {
                return getMultiLanguageScriptingCode(script);
            }

            if (ControllerBranchCode.class.equals(codeClass)) {
                return getControllerBranchCode((SpecNode)delegate.getObject(), script);
            }

            if (ControllerJoinCode.class.equals(codeClass)) {
                return getControllerJoinCode((SpecNode)delegate.getObject());
            }

            if (DataIntegrationCode.class.equals(codeClass)) {
                return getDiCode(script);
            }

            if (codeClass.equals(EmrCode.class)) {
                return getEmrCode(script);
            }

            if (ComponentSqlCode.class.equals(codeClass)) {
                return getComponentSqlCode((SpecNode)delegate.getObject(), script);
            }

            // common default logic to get content
            code.setSourceCode(script.getContent());
            Code parsed = code.parse(script.getContent());
            return Optional.ofNullable(parsed).map(Code::getContent).orElse(code.getContent());
        } catch (Exception ex) {
            log.warn("get code model error: ", ex);
        }
        return script.getContent();
    }

    private String getComponentSqlCode(SpecNode specNode, SpecScript script) {
        CodeModel<ComponentSqlCode> code = CodeModelFactory.getCodeModel(CodeProgramType.COMPONENT_SQL.name(), "{}");
        code.getCodeModel().setSourceCode(script.getContent());
        code.getCodeModel().setConfig(specNode.getComponent());
        boolean isDeployToScheduler = Optional.ofNullable(context)
            .map(Context::isDeployToScheduler)
            .orElse(false);
        return isDeployToScheduler ? code.getSourceCode() : code.getContent();
    }

    private String getDiCode(SpecScript script) {
        return script.getContent();
    }

    private String getControllerJoinCode(SpecNode specNode) {
        return Optional.ofNullable(specNode.getJoin()).map(join -> {
            CodeModel<ControllerJoinCode> code = CodeModelFactory.getCodeModel(CodeProgramType.CONTROLLER_JOIN.name(), "");
            code.getCodeModel().setResultStatus(Optional.ofNullable(join.getResultStatus()).orElse("1"));
            String logic = Optional.ofNullable(join.getLogic()).map(SpecLogic::getExpression).filter(StringUtils::isNotBlank)
                    .orElseThrow(() -> new SpecException(SpecErrorCode.PARSE_ERROR, "node.join.logic.expression is empty"));

            ControllerJoinCode.Branch tempBranch = null;
            String tempBranchName = null;
            Map<String, ControllerJoinCode.Branch> branchMap = new HashMap<>();
            for (String token : StringUtils.split(logic, " ")) {
                if (tempBranch == null) {
                    tempBranchName = token;
                    tempBranch = newJoinBranch(tempBranchName, branchMap);
                } else {
                    if (tempBranchName != null && JOIN_BRANCH_LOGICS.stream().anyMatch(l -> StringUtils.equalsIgnoreCase(l, token))) {
                        tempBranch.setLogic(StringUtils.equalsIgnoreCase(LOGIC_AND, token) ? 1 : 0);
                        tempBranch = null;
                        tempBranchName = null;
                    } else {
                        // for a new branch name
                        tempBranch = newJoinBranch(tempBranchName, branchMap);
                    }
                }
            }

            code.getCodeModel().setBranchList(ListUtils.emptyIfNull(join.getBranches()).stream().map(b -> {
                ControllerJoinCode.Branch theBranch = Optional.ofNullable(branchMap.get(b.getName())).orElseThrow(
                        () -> new SpecException(SpecErrorCode.PARSE_ERROR, "logic branch " + b.getName() + " is not exist"));
                theBranch.setNode(b.getOutput().getData());
                theBranch.setRunStatus(ListUtils.emptyIfNull(b.getAssertion().getIn().getValue()).stream().map(s -> (String) s)
                        .collect(Collectors.toList()));
                return theBranch;
            }).collect(Collectors.toList()));
            return code.getContent();
        }).orElseThrow(() -> new SpecException(SpecErrorCode.PARSE_ERROR, "node.join field is null"));
    }

    private ControllerJoinCode.Branch newJoinBranch(String branchName, Map<String, ControllerJoinCode.Branch> branchMap) {
        ControllerJoinCode.Branch branch = new ControllerJoinCode.Branch();
        branch.setLogic(1);
        branchMap.put(branchName, branch);
        return branch;
    }

    private String getControllerBranchCode(SpecNode specNode, SpecScript script) {
        CodeModel<ControllerBranchCode> code = CodeModelFactory.getCodeModel(CodeProgramType.CONTROLLER_BRANCH.name(), "");
        code.getCodeModel().setSourceCode(script.getContent());
        List<Branch> branches = ListUtils.emptyIfNull(Optional.ofNullable(specNode)
                        .map(SpecNode::getBranch).map(SpecBranch::getBranches).orElse(null))
                .stream().map(branch -> {
                    Branch b = new Branch();
                    b.setCondition(branch.getWhen());
                    b.setNodeoutput(branch.getOutput().getData());
                    b.setDescription(branch.getDesc());
                    return b;
                }).collect(Collectors.toList());
        code.getCodeModel().setBranchList(branches);
        return code.getContent();
    }

    private String getEmrCode(SpecScript script) {
        String command = script.getRuntime().getCommand();
        CodeModel<EmrCode> code = CodeModelFactory.getCodeModel(script.getRuntime().getCommand(), script.getContent());
        EmrCode codeModel = code.getCodeModel();
        codeModel.setSourceCode(script.getContent());
        Optional.ofNullable(CodeProgramType.getNodeTypeByName(command)).ifPresent(type -> {
            switch (type) {
                case EMR_SHELL: {
                    codeModel.setType(EmrJobType.SHELL);
                    break;
                }
                case EMR_STREAMING_SQL: {
                    codeModel.setType(EmrJobType.STREAMING_SQL);
                    break;
                }
                case EMR_HIVE: {
                    codeModel.setType(EmrJobType.HIVE_SQL);
                    break;
                }
                case EMR_HIVE_CLI: {
                    codeModel.setType(EmrJobType.HIVE);
                    break;
                }
                case EMR_MR: {
                    codeModel.setType(EmrJobType.MR);
                    break;
                }
                case EMR_IMPALA: {
                    codeModel.setType(EmrJobType.IMPALA_SQL);
                    break;
                }
                case EMR_PRESTO: {
                    codeModel.setType(EmrJobType.PRESTO_SQL);
                    break;
                }
                case EMR_SPARK_SQL: {
                    codeModel.setType(EmrJobType.SPARK_SQL);
                    break;
                }
                case EMR_SPARK: {
                    codeModel.setType(EmrJobType.SPARK);
                    break;
                }
                case EMR_SPARK_SHELL: {
                    codeModel.setType(EmrJobType.SPARK_SHELL);
                    break;
                }
                case EMR_SPARK_STREAMING: {
                    codeModel.setType(EmrJobType.SPARK_STREAMING);
                    break;
                }
            }
        });
        codeModel.setName(UUID.randomUUID().toString());

        EmrLauncher launcher = new EmrLauncher();
        Optional.ofNullable(script.getRuntime()).ifPresent(rt -> {
            Map<String, Object> allocationSpecProps = new HashMap<>();
            launcher.setAllocationSpec(allocationSpecProps);
            Optional.ofNullable(rt.getSparkConf()).filter(MapUtils::isNotEmpty).ifPresent(allocationSpecProps::putAll);

            Optional.ofNullable(rt.getEmrJobConfig()).ifPresent(emrJobConfig -> {
                EmrAllocationSpec allocationSpec = new EmrAllocationSpec();
                allocationSpec.setUserName((String) emrJobConfig.get("submitter"));
                allocationSpec.setQueue(Optional.ofNullable((String) emrJobConfig.get("queue")).filter(StringUtils::isNotBlank).orElse("default"));
                allocationSpec.setMemory(Optional.ofNullable(emrJobConfig.get("memory")).map(String::valueOf).orElse("2048"));
                allocationSpec.setVcores(Optional.ofNullable(emrJobConfig.get("cores")).map(String::valueOf).orElse("1"));
                allocationSpec.setPriority(Optional.ofNullable(emrJobConfig.get("priority")).map(String::valueOf).orElse("1"));
                allocationSpec.setUseGateway(Optional.ofNullable((String) emrJobConfig.get("submitMode"))
                        .map(mode -> LabelEnum.getByLabel(EmrJobSubmitMode.class, mode))
                        .map(mode -> Objects.equals(mode, EmrJobSubmitMode.LOCAL)).orElse(false));
                allocationSpec.setReuseSession(Optional.ofNullable(emrJobConfig.get("sessionEnabled"))
                        .map(String::valueOf).map(BooleanUtils::toBoolean).orElse(false));
                allocationSpec.setBatchMode(Optional.ofNullable((String) emrJobConfig.get("executeMode"))
                        .map(mode -> LabelEnum.getByLabel(EmrJobExecuteMode.class, mode))
                        .map(mode -> Objects.equals(mode, EmrJobExecuteMode.BATCH)).orElse(false));
                allocationSpec.setEnableJdbcSql(Optional.ofNullable(emrJobConfig.get("enableJdbcSql"))
                        .map(String::valueOf).map(BooleanUtils::toBoolean).orElse(false));
                codeModel.getProperties().getEnvs().put(EmrCode.ENVS_KEY_FLOW_SKIP_SQL_ANALYZE, String.valueOf(allocationSpec.getBatchMode()));
                Optional.ofNullable((JsonObject) GsonUtils.fromJsonString(GsonUtils.toJsonString(allocationSpec), JsonObject.class)).ifPresent(
                        json -> json.entrySet().forEach(entry -> allocationSpecProps.put(entry.getKey(), entry.getValue().getAsString())));
                emrJobConfig.keySet().stream().filter(key -> ListUtils.emptyIfNull(ReflectUtils.getPropertyFields(allocationSpec)).stream()
                        .noneMatch(f -> StringUtils.equals(f.getName(), key))).forEach(key -> allocationSpecProps.put(key, emrJobConfig.get(key)));
            });
        });
        codeModel.setLauncher(launcher);
        codeModel.getProperties().setTags(Arrays.asList(ProductModule.DATA_STUDIO.getName(), SpecConstants.FLOW_SPEC + "/" + SpecVersion.V_1_1_0));
        return code.getContent();
    }

    private String getMultiLanguageScriptingCode(SpecScript script) {
        CodeModel<MultiLanguageScriptingCode> code = CodeModelFactory.getCodeModel(script.getRuntime().getCommand(), "");
        code.getCodeModel().setSourceCode(script.getContent());
        code.getCodeModel().setLanguage(script.getLanguage());

        if (StringUtils.containsIgnoreCase(script.getLanguage(), "odps")) {
            code.getCodeModel().setLanguage(MultiLanguageScriptingCode.LANGUAGE_ODPS_SQL);
        } else if (StringUtils.containsIgnoreCase(script.getLanguage(), "python")) {
            code.getCodeModel().setLanguage(MultiLanguageScriptingCode.LANGUAGE_PYTHON);
        } else if (StringUtils.containsIgnoreCase(script.getLanguage(), "shell")) {
            code.getCodeModel().setLanguage(MultiLanguageScriptingCode.LANGUAGE_SHELL);
        }
        return code.getContent();
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return this.context;
    }
}
