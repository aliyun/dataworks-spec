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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter;

import com.aliyun.dataworks.migrationx.domain.dataworks.airflow.AirflowDumpItem;
import com.aliyun.dataworks.migrationx.domain.dataworks.airflow.AirflowNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.airflow.AirflowWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.airflow.OperatorType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowVersion;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.NodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.loader.ProjectAssetLoader;
import com.aliyun.dataworks.migrationx.transformer.core.translator.TranslateUtils;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import com.aliyun.migrationx.common.utils.FileNameUtils;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.google.common.base.Joiner;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Implementation of parsing airflow dag python code to workflow definition
 *
 * @author sam.liux
 * @date 2019/05/27
 */
public class AirflowDagConverter extends AbstractBaseConverter implements WorkflowConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AirflowDagConverter.class);
    private static final String JSON_FILE_SUFFIX = ".json";

    private Properties properties = new Properties();
    private Project project;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public AirflowDagConverter() {
        super(AssetType.AIRFLOW, "AirflowDagConverter");
    }

    public AirflowDagConverter(AssetType assetType, String name,
        ProjectAssetLoader projectAssetLoader) {
        super(assetType, name, projectAssetLoader);
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public List<DwWorkflow> convert(Asset asset) throws Exception {
        workflowList = new ArrayList<>();
        File airflowDir = asset.getPath();
        if (airflowDir == null || !airflowDir.exists()) {
            LOGGER.error("airflow asset dir invalid");
            return ListUtils.emptyIfNull(null);
        }

        for (File jsonFile : airflowDir.listFiles(file -> file.isFile() && file.getName().endsWith(JSON_FILE_SUFFIX))) {
            LOGGER.info("parsing airflow dump json: {}", jsonFile);
            String json = IOUtils.toString(new FileReader(jsonFile));
            List<AirflowDumpItem> list =
                GsonUtils.defaultGson.fromJson(json, new TypeToken<List<AirflowDumpItem>>() {}.getType());

            List<DwWorkflow> wfs = list.stream()
                .map(AirflowDumpItem::getWorkflow)
                .filter(Objects::nonNull)
                .map(this::toDwWorkflow)
                .collect(Collectors.toList());
            workflowList.addAll(wfs);
        }

        ListUtils.emptyIfNull(workflowList).forEach(wf ->
            ListUtils.emptyIfNull(wf.getNodes()).forEach(n ->
                NodeUtils.setProjectRootDependencyIfIsolated(project, wf, n)));
        return workflowList;
    }

    private DwWorkflow toDwWorkflow(AirflowWorkflow airflowWorkflow) {
        DwWorkflow dwWorkflow = new DwWorkflow();
        dwWorkflow.setName(airflowWorkflow.getName());
        dwWorkflow.setScheduled(airflowWorkflow.getScheduled());
        dwWorkflow.setVersion(WorkflowVersion.V3);
        dwWorkflow.setNodes(org.apache.commons.collections4.ListUtils.emptyIfNull(airflowWorkflow.getNodes())
            .stream().map(airflow -> toDwNode(dwWorkflow, airflow)).peek(dwNode -> {
                dwNode.setNodeUseType(NodeUseType.SCHEDULED);
                org.apache.commons.collections4.ListUtils.emptyIfNull(dwNode.getInputs())
                    .forEach(in -> in.setParseType(1));
                org.apache.commons.collections4.ListUtils.emptyIfNull(dwNode.getOutputs())
                    .forEach(in -> in.setParseType(1));
            }).collect(Collectors.toList()));
        return dwWorkflow;
    }

    private DwNode toDwNode(DwWorkflow dwWorkflow, AirflowNode airflowNode) {
        DwNode dwNode = new DwNode();
        BeanUtils.copyProperties(airflowNode, dwNode);
        dwNode.setName(FileNameUtils.normalizedFileName(dwNode.getName()));
        dwNode.setParameter(airflowNode.getParameters());
        dwNode.setCronExpress(dwNode.getCronExpress());
        dwNode.setIsAutoParse(0);
        dwNode.setWorkflowRef(dwWorkflow);
        LOGGER.debug("node parameters: {}, airflow parameters: {}", dwNode.getParameter(), airflowNode.getParameters());
        convertAirflowTask(dwNode, airflowNode);
        return dwNode;
    }

    private void convertAirflowTask(DwNode dwNode, AirflowNode airflowNode) {
        JsonObject airflowTask = airflowNode.getAirflowTask();
        if (StringUtils.isBlank(dwNode.getType()) && airflowTask == null) {
            LOGGER.error("invalid airflow task: {}", airflowNode);
            throw new RuntimeException("invalid airflow task " + airflowNode.getName());
        }

        LOGGER.debug("airflowTask: {}", airflowTask);
        if (StringUtils.isBlank(dwNode.getParameter())
            && airflowTask.has("params") && airflowTask.get("params").getAsJsonObject() != null) {
            String params = Joiner.on(" ").join(airflowTask.get("params").getAsJsonObject().keySet().stream()
                .map(k -> Joiner.on("=").join(k, String.valueOf(airflowTask.get("params").getAsJsonObject().get(k))))
                .collect(Collectors.toList()));
            dwNode.setParameter(params);
        }

        String operator = airflowTask.has("operator") && !airflowTask.get("operator").isJsonNull() ?
            airflowTask.get("operator").getAsString() : null;
        if (StringUtils.isBlank(operator)) {
            LOGGER.error("airflow task operator is blank: {}", airflowNode);
            throw new RuntimeException("airflow task operator is blank");
        }

        OperatorType operatorType = OperatorType.getOperatorTypeByName(operator);
        dwNode.setRawNodeType(operatorType.name());
        handleOperators(operatorType, dwNode, airflowTask);
    }

    private void handleOperators(OperatorType operator, DwNode dwNode, JsonObject airflowTask) {
        switch (operator) {
            case BashOperator:
                processBashOperator(dwNode, airflowTask);
                return;
            case PythonOperator:
            case PythonVirtualenvOperator:
            case BranchPythonOperator:
            case _PythonDecoratedOperator:
                processPythonOperator(dwNode, airflowTask);
                return;
            case ExternalTaskSensor:
            case ExternalTaskMarker:
            case ExternalTaskSensorLink:
                processAsVirtual(dwNode, airflowTask);
                return;
        }
        processDefault(dwNode, airflowTask);
    }

    private void processAsVirtual(DwNode dwNode, JsonObject airflowTask) {
        dwNode.setType(CodeProgramType.VIRTUAL.name());
        dwNode.setCode(GsonUtils.toJsonString(airflowTask));
    }

    private void processPythonOperator(DwNode dwNode, JsonObject airflowTask) {
        dwNode.setType(CodeProgramType.PYODPS.name());
        dwNode.setCode(airflowTask.has("python_callable") ?
            airflowTask.get("python_callable").getAsString() : "");
    }

    private void processDefault(DwNode dwNode, JsonObject airflowTask) {
        dwNode.setType(getDefaultTypeIfNotSupported(properties, CodeProgramType.DIDE_SHELL));
        dwNode.setDescription(BizException.of(ErrorCode.PACKAGE_CONVERT_FAILED).with(dwNode.getType()).getMessage());
        dwNode.setCode(GsonUtils.toJsonString(airflowTask));
    }

    private void processBashOperator(DwNode dwNode, JsonObject airflowTask) {
        dwNode.setType(StringUtils.defaultIfBlank(
            properties.getProperty(Constants.CONVERTER_TARGET_SHELL_NODE_TYPE_AS), CodeProgramType.DIDE_SHELL.name()));
        String bashCommand = airflowTask.get("bash_command") != null ? airflowTask.get("bash_command").getAsString()
            : null;
        dwNode.setCode(StringUtils.defaultIfBlank(dwNode.getCode(), bashCommand));
        if (CodeProgramType.EMR_SHELL.name().equals(dwNode.getType())) {
            dwNode.setCode(EmrCodeUtils.toEmrCode(dwNode));
        }

        if (airflowTask.has("env")) {
            JsonObject envs = airflowTask.get("env") instanceof JsonNull ? null : airflowTask.get("env")
                .getAsJsonObject();
            if (envs != null) {
                dwNode.setParameter(GsonUtils.toJsonString(envs));
            }
        }

        LOGGER.debug("node: {}, parameter: {}", dwNode.getName(), dwNode.getParameter());
        TranslateUtils.translateSparkSubmit(dwNode, properties);
        TranslateUtils.translateCommandSql((DwWorkflow)dwNode.getWorkflowRef(), dwNode, properties);
    }
}
