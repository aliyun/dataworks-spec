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

package com.aliyun.dataworks.common.spec.domain.dw.codemodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

/**
 * @author 聿剑
 * @date 2024/3/21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ToString
@Slf4j
public class PaiFlowCode extends AbstractBaseCode implements SpecDescribableCode<DataWorksWorkflowSpec> {
    @Override
    public Specification<DataWorksWorkflowSpec> getSpec() {
        Yaml yaml = new Yaml();
        yaml.setBeanAccess(BeanAccess.FIELD);
        PaiFlowSpec paiFlowSpec = Optional.ofNullable((PaiFlowSpec)yaml.loadAs(getPaiflowPipeline(), PaiFlowSpec.class)).orElse(new PaiFlowSpec());
        log.info("spec: {}", GsonUtils.toJsonString(paiFlowSpec));
        Specification<DataWorksWorkflowSpec> specObj = new Specification<>();
        DataWorksWorkflowSpec dwSpec = new DataWorksWorkflowSpec();
        specObj.setVersion(SpecVersion.V_1_1_0.getLabel());
        specObj.setKind(SpecKind.PAIFLOW.getLabel());
        specObj.setSpec(dwSpec);

        dwSpec.setMetadata(paiFlowSpec.getMetadata());
        List<SpecFlowDepend> flow = new ArrayList<>();
        List<SpecNode> nodes = Optional.ofNullable(paiFlowSpec.getSpec()).map(Spec::getPipelines).orElse(ListUtils.emptyIfNull(null)).stream().map(
            node -> {
                // node info.
                SpecNode nodeObj = new SpecNode();
                nodeObj.setMetadata(node.getMetadata());
                nodeObj.setId((String)MapUtils.emptyIfNull(node.getMetadata()).get("name"));
                nodeObj.setName((String)MapUtils.emptyIfNull(node.getMetadata()).get("displayName"));
                SpecScript script = new SpecScript();
                SpecScriptRuntime runtime = new SpecScriptRuntime();
                runtime.setEngine("PAI");
                runtime.setCommand((String)MapUtils.emptyIfNull(node.getMetadata()).get("identifier"));
                script.setRuntime(runtime);

                List<SpecVariable> parameters = Optional.ofNullable(node.getSpec())
                    .map(spec -> MapUtils.emptyIfNull(spec.getArguments()).get("parameters"))
                    .map(paraList -> ListUtils.emptyIfNull(paraList).stream().map(para -> {
                        SpecVariable var = new SpecVariable();
                        var.setName((String)para.get("name"));
                        var.setValue(Optional.ofNullable(para.get("value")).map(val -> {
                            if (val instanceof String) {
                                return (String)val;
                            }
                            return GsonUtils.toJsonString(val);
                        }).orElse(null));
                        var.setType(VariableType.CONSTANT);
                        var.setScope(VariableScopeType.NODE_PARAMETER);
                        Optional.ofNullable(para.get("from")).ifPresent(from -> var.setValue((String)from));
                        return var;
                    }).collect(Collectors.toList())).orElse(ListUtils.emptyIfNull(null));
                script.setParameters(parameters);
                nodeObj.setScript(script);

                // node flow depends
                List<SpecDepend> depends = Optional.ofNullable(node.getSpec()).map(Spec::getDependencies).map(dependList ->
                    ListUtils.emptyIfNull(dependList).stream().map(dep -> {
                        SpecDepend specDepend = new SpecDepend();
                        SpecNode depId = new SpecNode();
                        depId.setId(dep);
                        specDepend.setNodeId(depId);
                        return specDepend;
                    }).collect(Collectors.toList())).orElse(ListUtils.emptyIfNull(null));
                if (CollectionUtils.isNotEmpty(depends)) {
                    SpecFlowDepend depend = new SpecFlowDepend();
                    SpecNode nodeId = new SpecNode();
                    nodeId.setId((String)MapUtils.emptyIfNull(node.getMetadata()).get("name"));
                    depend.setNodeId(nodeId);
                    depend.setDepends(depends);
                    flow.add(depend);
                }
                return nodeObj;
            }).collect(Collectors.toList());
        dwSpec.setNodes(nodes);
        dwSpec.setFlow(flow);
        return specObj;
    }

    @Data
    @ToString
    @Accessors(chain = true)
    @EqualsAndHashCode
    public static final class InOut {
        private String type;
        private String value;
    }

    @Data
    @ToString
    @Accessors(chain = true)
    @EqualsAndHashCode
    public static final class TaskRelation {
        private String childTaskUniqueCode;
        private String parentTaskUniqueCode;
    }

    @Data
    @ToString
    @Accessors(chain = true)
    @EqualsAndHashCode
    public static final class Task {
        private Boolean root = true;
        private String taskName;
        private String taskUniqueCode;
    }

    @Data
    @ToString
    @Accessors(chain = true)
    @EqualsAndHashCode
    public static class PaiFlowSpec {
        private String apiVersion;
        private Map<String, Object> metadata;
        private Spec spec;

    }

    @Data
    @ToString
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static final class Spec extends PaiFlowSpec {
        private Map<String, List<Map<String, Object>>> inputs;
        private Map<String, List<Map<String, Object>>> outputs;
        private Map<String, List<Map<String, Object>>> arguments;
        private List<String> dependencies;
        private List<Object> initContainers;
        private List<Object> sideCarContainers;
        private List<Object> volumes;
        private List<PaiFlowSpec> pipelines;
    }

    private Long appId;
    private String workspaceId;
    private String name;
    private Map<String, String> computeResource;
    private String flowUniqueCode;
    private List<InOut> inputs;
    private List<InOut> outputs;
    /**
     * paiflowArguments in YAML
     */
    private String paiflowArguments;
    private Map<String, String> paiflowParameters;
    /**
     * paiflowPipeline spec in YAML
     */
    private String paiflowPipeline;
    private String paraValue;
    private String prgType;
    private String requestId;
    private List<TaskRelation> taskRelations;
    private List<Task> tasks;

    @Override
    public PaiFlowCode parse(String code) {
        return GsonUtils.fromJsonString(code, getClass());
    }

    @Override
    public List<String> getProgramTypes() {
        return Arrays.asList(CodeProgramType.PAI_STUDIO.name(), CodeProgramType.RECOMMEND_PLUS.name());
    }
}
