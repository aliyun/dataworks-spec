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

import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.CaiyunjianTask;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.DateParser;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.DgDatasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.DgDatasourceUtil;
import com.aliyun.dataworks.migrationx.domain.dataworks.constants.DataWorksConstants;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwDatasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.ModelTreeRoot;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.FolderUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.NodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.loader.ProjectAssetLoader;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItem;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItemType;
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
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description 采云间任务处理
 * @createTime 2020-04-03
 */
@Slf4j
public class CaiyunjianConverter extends AbstractBaseConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaiyunjianConverter.class);

    private static final String JSON_FILE_SUFFIX = ".json";
    private ConcurrentMap<String, DwWorkflow> dwWorkflowConcurrentMap = new ConcurrentHashMap();

    public Project project;
    private Properties properties;
    private Map<String, DgDatasource> datasourceMap;

    public CaiyunjianConverter() {
        super(AssetType.CAIYUNJIAN, "CaiyunjianConverter");
    }

    public CaiyunjianConverter(AssetType assetType,
        String name,
        ProjectAssetLoader projectAssetLoader) {
        super(assetType, name, projectAssetLoader);
    }

    @Override
    public List<DwWorkflow> convert(Asset asset) throws Exception {
        this.properties = propertiesLoader != null && properties == null ? propertiesLoader.getResult() : properties;
        this.datasourceMap = DgDatasourceUtil.processDatasource(new File(asset.getPath(), DgDatasourceUtil.DATASOURCE_EXCEL));
        this.project.setDatasources(DgDatasourceUtil.convertDatasources(this.datasourceMap));
        List<DwWorkflow> list = convertWorkflowList(asset);
        ListUtils.emptyIfNull(list).forEach(wf -> ListUtils.emptyIfNull(wf.getNodes()).stream()
            .peek(n -> NodeUtils.setProjectRootDependencyIfIsolated(project, wf, n))
            .filter(n -> CodeProgramType.DI.name().equalsIgnoreCase(n.getType()))
            .forEach(n -> {
                processFtpRootPath(n, this.datasourceMap);
                NodeUtils.setProjectRootDependencyIfIsolated(project, wf, n);
            }));
        ListUtils.emptyIfNull(this.project.getDatasources()).forEach(
            ds -> ((DwDatasource)ds).setProjectRef(this.project));
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
        dwWorkflowConcurrentMap = new ConcurrentHashMap<>();
        File dir = asset.getPath();
        if (null == dir || !dir.exists()) {
            log.error("Caiyunjian asset dir invalid");
            return ListUtils.emptyIfNull(null);
        }
        List<File> fileList = new ArrayList<>();
        File[] files = dir.listFiles(file -> file.isFile() && file.getName().endsWith(JSON_FILE_SUFFIX));
        File[] directoryFile = dir.listFiles(file -> file.isDirectory());
        if (null != directoryFile && directoryFile.length > 0) {
            for (int i = 0; i < directoryFile.length; i++) {
                File[] childFiles = directoryFile[i].listFiles(
                    file -> file.isFile() && file.getName().endsWith(JSON_FILE_SUFFIX));
                if (null != childFiles) {
                    fileList.addAll(Arrays.asList(childFiles));
                }
            }
        }
        if (null != files) {
            fileList.addAll(Arrays.asList(files));
        }
        if (fileList.size() <= 0) {
            log.error("Caiyunjian json file invalid");
            return ListUtils.emptyIfNull(null);
        }
        convertWorkflow(fileList);
        workflowList.addAll(new ArrayList<>(dwWorkflowConcurrentMap.values()));
        ListUtils.emptyIfNull(workflowList).forEach(wf ->
            ListUtils.emptyIfNull(wf.getNodes()).stream().forEach(n ->
                NodeUtils.setProjectRootDependencyIfIsolated(project, wf, n)));
        return workflowList;
    }

    public void convertWorkflow(List<File> fileList) {
        fileList.forEach(jsonFile -> {
            try {
                String json = IOUtils.toString(new FileReader(jsonFile));
                CaiyunjianTask caiyunjianTask = GsonUtils.defaultGson.fromJson(json, new TypeToken<CaiyunjianTask>() {}.getType());
                if (caiyunjianTask == null) {
                    log.error("error parse json file: {}", jsonFile);
                    throw new Exception("parse cai yun jian task json error: " + jsonFile.getName());
                }

                DwWorkflow dwWorkflow = getWorkflow();
                DwNode dwNode = caiyunjianTask.toNode(this.project.getName());
                processDetectTask(caiyunjianTask, dwNode);

                dwNode.setWorkflowRef(dwWorkflow);
                getNodeFolder(dwNode, caiyunjianTask);
                if (null != dwNode) {
                    dwWorkflow.getNodes().add(dwNode);
                    dwNode.setWorkflowRef(dwWorkflow);
                }

                if (!caiyunjianTask.getSuccess()) {
                    ReportItem reportItem = new ReportItem();
                    reportItem.setName(jsonFile.getName());
                    reportItem.setType(ReportItemType.CAIYUNJIAN_FILE_TO_NODE_FAILURE.name());
                    reportItem.setMessage("failed to convert job: " + jsonFile.getName());
                    reportItems.add(reportItem);
                    throw new Exception(reportItem.getMessage());
                }
            } catch (Exception e) {
                log.error("[Caiyunjian] file parse exception,file :{}", jsonFile.getName(), e);
                throw BizException.of(ErrorCode.PACKAGE_CONVERT_FAILED).with(jsonFile.getName() + ": " + e.getMessage());
            }
        });
    }

    private void processDetectTask(CaiyunjianTask caiyunjianTask, DwNode dwNode) {
        if (caiyunjianTask.getTaskDetectInfoDO() == null || MapUtils.isEmpty(datasourceMap)) {
            return;
        }

        CaiyunjianTask.TaskDetectInfoDo taskDetectInfoDo = caiyunjianTask.getTaskDetectInfoDO();
        CaiyunjianTask.TaskDetectInfoDo.Setting setting = taskDetectInfoDo.getSetting();
        List<String> intervalParameter = Arrays
            .stream(Arrays
                .stream(StringUtils.split(StringUtils.defaultIfBlank(
                    caiyunjianTask.getTaskDefineDO().getParameter(), "").trim(), " "))
                .filter(p -> p.startsWith("--interval-"))
                .filter(StringUtils::isNotBlank)
                .findFirst().orElse("")
                .split("-"))
            .filter(StringUtils::isNotBlank).collect(Collectors.toList());
        Integer intervalSeconds = CollectionUtils.size(intervalParameter) == 2 &&
            intervalParameter.get(0).equalsIgnoreCase("interval") &&
            StringUtils.isNumeric(intervalParameter.get(1)) ? Integer.parseInt(intervalParameter.get(1)) : 180;

        datasourceMap.values().stream().filter(ds -> Objects.equals(ds.getId(), setting.getDatasourceId())).findFirst()
            .ifPresent(ds -> {
                try {
                    String path = DateParser.parse(Joiner.on(File.separator).join(Stream
                        .of(
                            StringUtils.defaultIfBlank(ds.getRootPath(), ""),
                            StringUtils.defaultIfBlank(setting.getPath(), ""),
                            StringUtils.defaultIfBlank(setting.getFileName(), "")
                        ).filter(StringUtils::isNotBlank).collect(Collectors.toList())));
                    String stopAt = Optional.ofNullable(this.properties)
                        .map(prop -> prop.getProperty(Constants.PROPERTIES_CONVERTER_DETECT_TASK_STOP_AT))
                        .orElse("2350");

                    // StringUtils.defaultIfBlank(detectConfig.getStopTime(), "23:59:59");

                    String params = Joiner.on(" ").join(
                        StringUtils.defaultIfBlank(ds.getName(), "datasourceName"),
                        path, intervalSeconds, stopAt);
                    String code = IOUtils.toString(
                        new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("res/ftpcheck.sh")));
                    dwNode.setParameter(params);
                    dwNode.setCode(code);
                    dwNode.setType(CodeProgramType.DIDE_SHELL.name());
                } catch (Exception e) {
                    log.error("convert detect config error: ", e);
                    throw new RuntimeException(e);
                }
            });
    }

    private void getNodeFolder(DwNode dwNode, CaiyunjianTask caiyunjianTask) {
        ModelTreeRoot modelTreeRoot = null;
        if (dwNode.getWorkflowRef() != null) {
            modelTreeRoot = FolderUtils.getModelTreeRoot(dwNode.getWorkflowRef());
        }

        if (CollectionUtils.isNotEmpty(caiyunjianTask.getDirPath())) {
            List<String> parts = caiyunjianTask.getDirPath().stream()
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
                IntlUtils.get("CAI_YUN_JIAN").d("CAI_YUN_JIAN"),
                dwNode.getFolder().replaceFirst("^/", "")));
        }
    }

    /**
     * 彩云间统一放到旧版工作流目录下
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

    public void setProject(Project project) {
        this.project = project;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
