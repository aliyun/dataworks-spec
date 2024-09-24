/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.LanguageEnum;
import com.aliyun.dataworks.common.spec.domain.enums.DependencyType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableType;
import com.aliyun.dataworks.common.spec.domain.interfaces.Output;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDatasource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.sql.SqlParameters;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.AbstractParameterConverter;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;
import com.aliyun.migrationx.common.utils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-05
 */
public class SqlParameterConverter extends AbstractParameterConverter<SqlParameters> {

    private static final SpecScriptRuntime RUNTIME = new SpecScriptRuntime();

    static {
        RUNTIME.setEngine(CodeProgramType.ODPS_SQL.getCalcEngineType().getLabel());
        RUNTIME.setCommand(CodeProgramType.ODPS_SQL.getName());
    }

    public SqlParameterConverter(DataWorksWorkflowSpec spec, SpecWorkflow specWorkflow, TaskDefinition taskDefinition,
        DolphinSchedulerV3ConverterContext context) {
        super(spec, specWorkflow, taskDefinition, context);
    }

    @Override
    protected void convertParameter(SpecNode specNode) {
        List<SpecVariable> specVariableList = convertSpecNodeParam(specNode);

        SpecScript script = new SpecScript();
        script.setId(generateUuid());
        script.setLanguage(LanguageEnum.ODPS_SQL.getIdentifier());
        script.setRuntime(RUNTIME);
        script.setPath(getScriptPath(specNode));
        script.setContent(parameter.getSql());
        script.setParameters(ListUtils.emptyIfNull(specVariableList).stream().filter(v -> !VariableType.NODE_OUTPUT.equals(v.getType()))
            .collect(Collectors.toList()));
        specNode.setScript(script);

        SpecDatasource datasource = MapUtils.emptyIfNull(context.getDataSourceMap()).get(String.valueOf(parameter.getDatasource()));
        if (Objects.nonNull(datasource)) {
            specNode.setDatasource(datasource);
        }

        dealPreAndPostSql(specNode, parameter.getPreStatements(), parameter.getPostStatements());
    }

    /**
     * parse pre sql and post sql to extra node
     *
     * @param specNodeRef origin node
     * @param preSqlList  pre sql list
     * @param postSqlList post sql list
     */
    private void dealPreAndPostSql(SpecNode specNodeRef, List<String> preSqlList, List<String> postSqlList) {
        List<SpecNode> specNodeList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(preSqlList)) {
            for (int i = 0; i < preSqlList.size(); i++) {
                String sql = preSqlList.get(i);
                SpecNode preNode = copySpecNode(specNodeRef, sql, "-pre-" + i);
                specNodeList.add(preNode);
                if (i == 0) {
                    headList.add(preNode);
                }
            }
        }
        specNodeList.add(specNodeRef);
        if (CollectionUtils.isNotEmpty(postSqlList)) {
            for (int i = 0; i < postSqlList.size(); i++) {
                String sql = postSqlList.get(i);
                SpecNode postNode = copySpecNode(specNodeRef, sql, "-post-" + i);
                specNodeList.add(postNode);
                if (i == postSqlList.size() - 1) {
                    tailList.add(postNode);
                }
            }
        }
        if (specNodeList.size() > 1) {
            SpecNode pre = specNodeList.get(0);
            for (int i = 1; i < specNodeList.size(); i++) {
                SpecNode specNode = specNodeList.get(i);
                specNode.getInputs().add(getDefaultOutput(pre));
                specNode.getInputs().addAll(getContextOutputs(pre));
                SpecFlowDepend specFlowDepend = newSpecFlowDepend();
                specFlowDepend.setNodeId(specNode);
                specFlowDepend.getDepends().add(new SpecDepend(pre, DependencyType.NORMAL, getDefaultOutput(pre)));
                getWorkflowDependencyList().add(specFlowDepend);
                pre = specNode;
            }
        }
    }

    /**
     * copy node, only used in pre and post sql.
     *
     * @param specNode origin node
     * @param sql      new sql
     * @param suffix   new suffix
     * @return copied node
     */
    private SpecNode copySpecNode(SpecNode specNode, String sql, String suffix) {
        SpecNode specNodeCopy = BeanUtils.deepCopy(specNode, SpecNode.class);
        specNodeCopy.setId(generateUuid());
        specNodeCopy.setName(specNodeCopy.getName() + suffix);
        for (Output output : specNodeCopy.getOutputs()) {
            if (output instanceof SpecNodeOutput && Boolean.TRUE.equals(((SpecNodeOutput)output).getIsDefault())) {
                ((SpecNodeOutput)output).setId(generateUuid());
                ((SpecNodeOutput)output).setData(specNodeCopy.getId());
                ((SpecNodeOutput)output).setRefTableName(specNodeCopy.getName());
            } else if (output instanceof SpecRefEntity) {
                ((SpecRefEntity)output).setId(generateUuid());
            }

        }
        getWorkflowNodeList().add(specNodeCopy);

        SpecScript scriptCopy = BeanUtils.deepCopy(specNodeCopy.getScript(), SpecScript.class);
        scriptCopy.setId(generateUuid());
        scriptCopy.setPath(scriptCopy.getPath() + suffix);
        scriptCopy.setContent(sql);

        specNodeCopy.setScript(scriptCopy);
        return specNodeCopy;
    }

}
