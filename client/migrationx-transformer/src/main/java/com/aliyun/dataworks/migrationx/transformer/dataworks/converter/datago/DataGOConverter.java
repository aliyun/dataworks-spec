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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.datago;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.ModelTreeRoot;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.DgDatasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.DgDatasourceUtil;
import com.aliyun.dataworks.migrationx.domain.dataworks.constants.DataWorksConstants;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.DataGoTask;
import com.aliyun.dataworks.migrationx.domain.dataworks.datago.DsfCycle;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwDatasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.FolderUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.NodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.loader.ProjectAssetLoader;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItem;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItemType;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.AbstractBaseConverter;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.aliyun.migrationx.common.utils.IntlUtils;

import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * {@code @description} DataGO任务处理
 * {@code @createTime} 2020-04-03
 */
@Slf4j
public class DataGOConverter extends AbstractBaseConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataGOConverter.class);

    private static final String JSON_FILE_SUFFIX = ".json";
    private static final List<String> FILTER_FILE = Arrays.asList("envUUID.json", "resources.json");
    private ConcurrentMap<String, DwWorkflow> dwWorkflowConcurrentMap = new ConcurrentHashMap();

    private Project project;
    private Properties properties;
    protected Map<String, DgDatasource> datasourceMap;

    public DataGOConverter() {
        super(AssetType.DATAGO, "DataGOConverter");
    }

    public DataGOConverter(AssetType assetType, String name, ProjectAssetLoader projectAssetLoader) {
        super(assetType, name, projectAssetLoader);
    }

    @Override
    public List<DwWorkflow> convert(Asset asset) throws Exception {
        this.properties = propertiesLoader != null && properties == null ? propertiesLoader.getResult() : properties;
        this.datasourceMap = DgDatasourceUtil.processDatasource(new File(asset.getPath(), DgDatasourceUtil.DATASOURCE_EXCEL));
        List<DwWorkflow> list = convertWorkflowList(asset);
        ListUtils.emptyIfNull(list).forEach(wf -> ListUtils.emptyIfNull(wf.getNodes()).stream()
                .peek(n -> NodeUtils.setProjectRootDependencyIfIsolated(project, wf, n))
                .filter(n -> CodeProgramType.DI.name().equalsIgnoreCase(n.getType()))
                .forEach(n -> {
                    processFtpRootPath(n, this.datasourceMap);
                    NodeUtils.setProjectRootDependencyIfIsolated(project, wf, n);
                }));
        this.project.setDatasources(DgDatasourceUtil.convertDatasources(this.datasourceMap));
        ListUtils.emptyIfNull(this.project.getDatasources()).forEach(
                ds -> ((DwDatasource) ds).setProjectRef(this.project));
        return list;
    }

    private void processFtpRootPath(Node node, Map<String, DgDatasource> datasourceMap) {
        String code = node.getCode();
        if (StringUtils.isBlank(code)) {
            return;
        }

        JsonObject diJson = GsonUtils.gson.fromJson(code, JsonObject.class);
        if (diJson == null) {
            return;
        }

        if (!diJson.has("steps")) {
            return;
        }

        JsonArray steps = diJson.get("steps").getAsJsonArray();
        if (steps.size() == 0) {
            return;
        }

        for (int i = 0; i < steps.size(); i++) {
            JsonObject step = steps.get(i).getAsJsonObject();
            if (!step.has("parameter")) {
                continue;
            }

            JsonObject parameter = step.get("parameter").getAsJsonObject();
            String datasource = null;

            if (parameter.has("datasourceId")) {
                Long datasourceId = parameter.get("datasourceId").getAsLong();
                datasource = datasourceMap.values().stream()
                        .filter(ds -> datasourceId != null && datasourceId.equals(ds.getId()))
                        .findAny().map(DgDatasource::getName).orElse(null);
            }

            if (StringUtils.isBlank(datasource) && parameter.has("datasourceName")) {
                datasource = parameter.get("datasourceName").getAsString();
            }

            if (StringUtils.isBlank(datasource) && parameter.has("datasource")) {
                datasource = parameter.get("datasource").getAsString();
            }

            if (StringUtils.isBlank(datasource)) {
                LOGGER.warn("datasource not found, node: {}", node.getName());
                continue;
            }

            parameter.addProperty("datasource", datasource);
            parameter.addProperty("datasourceName", datasource);

            if (!step.has("stepType") || Arrays.asList("sftp", "ftp").stream()
                    .noneMatch(t -> t.equalsIgnoreCase(step.get("stepType").getAsString()))) {
                continue;
            }

            DgDatasource dgDatasource = datasourceMap.get(datasource);
            if (dgDatasource != null && StringUtils.isNotBlank(dgDatasource.getRootPath()) && parameter.has("path")) {
                JsonElement pathEle = parameter.get("path");
                JsonArray path = new JsonArray();
                if (pathEle.isJsonArray()) {
                    path = parameter.get("path").getAsJsonArray();
                } else {
                    path.add(pathEle.getAsString());
                }

                if (path != null && path.size() > 0) {
                    JsonArray pathList = new JsonArray();
                    for (int j = 0; j < path.size(); j++) {
                        String p = path.get(j).getAsString();
                        p = Joiner.on(File.separator).join(dgDatasource.getRootPath(), p);
                        pathList.add(p);
                    }
                    parameter.add("path", pathList);
                }
            }
        }
        node.setCode(GsonUtils.defaultGson.toJson(diJson));
    }

    public List<DwWorkflow> convertWorkflowList(Asset asset) throws Exception {
        workflowList = new ArrayList<>();
        File dir = asset.getPath();
        if (null == dir || !dir.exists()) {
            log.error("[DataGO] asset dir invalid");
            return ListUtils.emptyIfNull(null);
        }

        List<File> fileList = new ArrayList<>();
        File[] files = dir.listFiles(file ->
                file.isFile() && file.getName().endsWith(JSON_FILE_SUFFIX) && !FILTER_FILE.contains(file.getName()));

        File[] directoryFile = dir.listFiles(File::isDirectory);
        if (null != directoryFile && directoryFile.length > 0) {
            for (File value : directoryFile) {
                File[] childFiles = value.listFiles(file ->
                        file.isFile() && file.getName().endsWith(JSON_FILE_SUFFIX) && !FILTER_FILE.contains(
                                file.getName()));
                if (null != childFiles) {
                    fileList.addAll(Arrays.asList(childFiles));
                }
            }
        }

        if (null != files) {
            fileList.addAll(Arrays.asList(files));
        }

        if (fileList.size() <= 0) {
            log.error("[DataGO] json file invalid");
            return ListUtils.emptyIfNull(null);
        }

        convertWorkflow(fileList);
        workflowList.addAll(new ArrayList<>(dwWorkflowConcurrentMap.values()));
        checkCycle();
        return workflowList;
    }

    public void convertWorkflow(List<File> fileList) throws Exception {
        for (File jsonFile : fileList) {
            String json = IOUtils.toString(new FileReader(jsonFile));
            List<DataGoTask> tasks = GsonUtils.defaultGson.fromJson(json, new TypeToken<List<DataGoTask>>() {}.getType());
            ListUtils.emptyIfNull(tasks).forEach(dataGoTask -> {
                DwWorkflow dwWorkflow = getWorkflow();
                try {
                    dataGoTask.setProperties(properties);
                    List<DwNode> dwNodes = dataGoTask.toNodes(this.project.getName(), datasourceMap);
                    dwNodes.stream().forEach(dwNode -> dwNode.setWorkflowRef(dwWorkflow));
                    ListUtils.emptyIfNull(dwNodes).forEach(dwNode -> getNodeFolder(dwNode, dataGoTask));
                    if (BooleanUtils.isTrue(dwWorkflow.getScheduled())) {
                        dwNodes.forEach(dwNode -> dwNode.setNodeUseType(NodeUseType.SCHEDULED));
                    } else {
                        dwNodes.forEach(dwNode -> dwNode.setNodeUseType(NodeUseType.MANUAL_WORKFLOW));
                    }
                    dwWorkflow.getNodes().addAll(dwNodes);

                    if (!dataGoTask.getSuccess()) {
                        ReportItem reportItem = new ReportItem();
                        reportItem.setName(jsonFile.getName() + ":" + dataGoTask.getTaskInstModel().getName());
                        reportItem.setType(ReportItemType.DataGO_FILE_TO_NODE_FAILURE.name());
                        reportItem.setMessage("datago job convert error: " + dataGoTask.getTaskInstModel().getName());
                        reportItems.add(reportItem);
                        throw new Exception(reportItem.getMessage());
                    }
                } catch (Exception e) {
                    log.error("[DataGO] to node exception, file:{} ", jsonFile, e);
                    throw BizException.of(ErrorCode.PACKAGE_CONVERT_FAILED).with(jsonFile.getName() + ":" + e.getMessage());
                }
            });
        }
    }

    private void getNodeFolder(DwNode dwNode, DataGoTask dataGoTask) {
        ModelTreeRoot modelTreeRoot = null;
        if (dwNode.getWorkflowRef() != null) {
            modelTreeRoot = FolderUtils.getModelTreeRoot(dwNode.getWorkflowRef());
        }

        if (StringUtils.isNotBlank(dataGoTask.getTreePathOfName())) {
            List<String> parts = Arrays.stream(dataGoTask.getTreePathOfName().split("/"))
                    .filter(s -> !s.equalsIgnoreCase(dwNode.getName()) && StringUtils.isNotBlank(s))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(parts)) {
                dwNode.setFolder("/");
            } else {
                dwNode.setFolder(Joiner.on("/").join(parts));
            }
        } else {
            dwNode.setFolder("/");
        }

        if (modelTreeRoot != null) {
            dwNode.setFolder(Joiner.on("/").join(
                    modelTreeRoot.getRootKey(),
                    IntlUtils.get("DATA_GO").d("DATA_GO"),
                    dwNode.getFolder().replaceFirst("^/", "")));
        }
    }

    /**
     * DataGO统一放到旧版工作流
     *
     * @return
     */
    private DwWorkflow getWorkflow() {
        return getDefaultWorkflow(DataWorksConstants.OLD_VERSION_WORKFLOW_NAME);
    }

    private DwWorkflow getDefaultWorkflow(String name) {
        if (dwWorkflowConcurrentMap.containsKey(name)) {
            return dwWorkflowConcurrentMap.get(name);
        }

        DwWorkflow dwWorkflow = new DwWorkflow();
        dwWorkflow.setName(name);
        dwWorkflow.setScheduled(Boolean.TRUE);
        dwWorkflowConcurrentMap.putIfAbsent(name, dwWorkflow);
        return dwWorkflow;
    }

    private void checkCycle() {
        DsfCycle dsfCycle = new DsfCycle();
        this.workflowList.forEach(dwWorkflow -> {
            List<Node> dwNodes = dwWorkflow.getNodes();
            if (CollectionUtils.isNotEmpty(dwNodes)) {
                dwNodes.forEach(node -> {
                    String nodeName = node.getName();
                    if (!nodeName.contains(".")) {
                        nodeName = Joiner.on(".").join(dwWorkflow.getName(), nodeName);
                    }
                    List<NodeIo> inputs = node.getInputs();
                    String finalNodeName = nodeName;
                    inputs.forEach(nodeIo -> {
                        dsfCycle.addLine(nodeIo.getData(), finalNodeName);
                    });
                });
            }
            List<String> cycleResult = dsfCycle.findCycle();
            if (CollectionUtils.isNotEmpty(cycleResult)) {
                StringBuilder builder = new StringBuilder();
                cycleResult.forEach(s -> builder.append(s).append(","));
                ReportItem reportItem = new ReportItem();
                reportItem.setName(dwWorkflow.getName());
                reportItem.setType(ReportItemType.EXIST_CYCLE.name());
                reportItem.setMessage(builder.toString());
                reportItems.add(reportItem);
            }
        });
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
