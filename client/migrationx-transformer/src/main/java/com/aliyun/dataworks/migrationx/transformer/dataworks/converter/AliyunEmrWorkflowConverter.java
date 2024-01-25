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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrAllocationSpec;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrJobMode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrJobType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.ModelTreeRoot;
import com.aliyun.dataworks.migrationx.domain.dataworks.aliyunemr.AliyunEmrProject;
import com.aliyun.dataworks.migrationx.domain.dataworks.aliyunemr.AliyunEmrService;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwDatasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Workflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowVersion;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.tenant.EnvType;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.CronExpressUtil;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DefaultNodeTypeUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.NodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.RawNodeType;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.loader.ProjectAssetLoader;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItem;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItemType;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportRiskLevel;
import com.aliyun.dataworks.migrationx.transformer.core.sqoop.DICode;
import com.aliyun.dataworks.migrationx.transformer.core.utils.DiCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import com.aliyun.migrationx.common.utils.DateUtils;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.aliyuncs.emr.model.v20160408.DescribeClusterBasicInfoResponse;
import com.aliyuncs.emr.model.v20160408.DescribeFlowResponse;
import com.aliyuncs.emr.model.v20160408.ListFlowJobResponse;
import com.aliyuncs.emr.model.v20160408.ListFlowProjectResponse;
import com.aliyuncs.emr.model.v20160408.ListFlowResponse;
import com.google.common.base.Joiner;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * implementation of aliyun EMR workflow convert to dataworks tasks
 *
 * @author sam.liux
 * @date 2019/06/27
 */
public class AliyunEmrWorkflowConverter extends AbstractBaseConverter implements WorkflowConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AliyunEmrWorkflowConverter.class);

    private static final String FLOW_NODE_DEF_TYPE_ACTION = ":action:";

    private List<ReportItem> reportItems = new ArrayList<>();
    private ArrayList<DwWorkflow> workflowList;
    private Properties properties;
    private Map<EmrJobType, CodeProgramType> nodeTypeMap = new HashMap<>();
    private Project project;
    private List<AliyunEmrProject> emrProjects = new ArrayList<>();
    private Function<ListFlowResponse.FlowItem, DescribeClusterBasicInfoResponse.ClusterInfo> getClusterInfoHook
        = flowItem -> null;

    public AliyunEmrWorkflowConverter setGetClusterInfoHook(
        Function<ListFlowResponse.FlowItem, DescribeClusterBasicInfoResponse.ClusterInfo> getClusterInfoHook) {
        this.getClusterInfoHook = getClusterInfoHook;
        return this;
    }

    public AliyunEmrWorkflowConverter() {
        super(AssetType.EMR, "AliyunEmrWorkflowConverter");
    }

    public AliyunEmrWorkflowConverter(AssetType assetType, String name, ProjectAssetLoader projectAssetLoader) {
        super(assetType, name, projectAssetLoader);
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public static String getWorkflowName(ListFlowProjectResponse.Project project, ListFlowResponse.FlowItem flowItem) {
        return Joiner.on("__").join(project.getName(), flowItem.getName());
    }

    class FlowNodeDef {
        private String jobId;
        private String name;
        private String clusterId;
        private String hostName;
        private String type;
        private Integer dependencies;
        private List<String> transitions;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getClusterId() {
            return clusterId;
        }

        public void setClusterId(String clusterId) {
            this.clusterId = clusterId;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getDependencies() {
            return dependencies;
        }

        public void setDependencies(Integer dependencies) {
            this.dependencies = dependencies;
        }

        public List<String> getTransitions() {
            return transitions;
        }

        public void setTransitions(List<String> transitions) {
            this.transitions = transitions;
        }
    }

    @Override
    public List<DwWorkflow> convert(Asset asset) throws Exception {
        LOGGER.info("convert asset: {}, path: {}", asset.getType().name(), asset.getPath().getAbsolutePath());
        if (properties != null && properties.containsKey(Constants.CONVERTER_ALIYUN_EMR_TO_DATAWORKS_NODE_TYPE_MAP)) {
            String mapJson = properties.getProperty(Constants.CONVERTER_ALIYUN_EMR_TO_DATAWORKS_NODE_TYPE_MAP, "{}");
            nodeTypeMap = GsonUtils.fromJsonString(mapJson,
                new TypeToken<Map<EmrJobType, CodeProgramType>>() {}.getType());
        }

        this.emrProjects = load(asset.getPath().getAbsolutePath());

        workflowList = new ArrayList<>();
        for (AliyunEmrProject project : this.emrProjects) {
            List<DwWorkflow> workflowArray = convertProject(project);
            workflowList.addAll(workflowArray);
        }
        return workflowList;
    }

    private List<DwWorkflow> convertProject(AliyunEmrProject project) throws Exception {
        convertProjectJobList(project);
        List<DwWorkflow> scheduled = convertProjectFlowList(project);
        return scheduled;
    }

    private List<DwWorkflow> convertProjectFlowList(AliyunEmrProject project) throws Exception {
        List<DwWorkflow> workflowList = new ArrayList<>();
        for (ListFlowResponse.FlowItem flowItem : project.getFlows().keySet()) {
            DwWorkflow workflow = new DwWorkflow();
            workflow.setName(getWorkflowName(project.getProject(), flowItem));
            workflow.setScheduled(true);
            workflow.setVersion(WorkflowVersion.V3);

            DescribeFlowResponse flowDetail = project.getFlows().get(flowItem);

            Map<String, Map<String, FlowNodeDef>> app = GsonUtils.gson.fromJson(flowDetail.getApplication(),
                new TypeToken<Map<String, Map<String, FlowNodeDef>>>() {}.getType());

            if (app != null) {
                List<Node> nodes = parseFlowNodes(project, flowItem, app.get("nodeDefMap"), workflow);
                workflow.setNodes(nodes);
            }

            processFlowDependency(workflow, flowDetail);

            ListUtils.emptyIfNull(workflow.getNodes()).forEach(node -> {
                if (CollectionUtils.isEmpty(node.getInputs())) {
                    NodeIo root = new NodeIo();
                    root.setData(NodeUtils.getProjectRootOutput(this.project));
                    root.setParseType(1);
                    node.getInputs().add(root);
                }
            });

            workflowList.add(workflow);
        }
        return workflowList;
    }

    /**
     * flow之间的依赖，通过flow的start和end之间的依赖串联。EMR工作流限制必须只有一个start和一个end
     *
     * @param workflow
     * @param flowDetail
     */
    private void processFlowDependency(DwWorkflow workflow, DescribeFlowResponse flowDetail) {
        if (CollectionUtils.isEmpty(flowDetail.getParentFlowList())) {
            return;
        }

        List<DescribeFlowResponse.ParentFlow> parentFlows = flowDetail.getParentFlowList();
        workflow.getNodes().stream().filter(this::isStart).findFirst().ifPresent(start ->
            ListUtils.emptyIfNull(parentFlows).forEach(parentFlow -> {
                String inStr = getNodeDefaultOutput(parentFlow.getProjectName(), parentFlow.getParentFlowName(), "end");
                List<NodeIo> inputs = start.getInputs();
                if (CollectionUtils.isEmpty(start.getInputs())) {
                    inputs = new ArrayList<>();
                    start.setInputs(inputs);
                }
                NodeIo input = new NodeIo();
                input.setData(inStr);
                input.setParseType(1);
                inputs.add(input);
            }));
    }

    private boolean isStart(Node n) {
        return "start".equalsIgnoreCase(((DwNode)n).getRawNodeType()) && CodeProgramType.VIRTUAL.name()
            .equalsIgnoreCase(n.getType());
    }

    private String getNodeName(Map<String, List<FlowNodeDef>> groupByJobId, ListFlowJobResponse.Job job,
        FlowNodeDef nodeDef) {
        if (!groupByJobId.containsKey(job.getId())) {
            return NodeUtils.normalizedFileName(job.getName());
        }

        if (CollectionUtils.size(groupByJobId.get(job.getId())) > 1) {
            return NodeUtils.normalizedFileName(Joiner.on("_").join(job.getName(), nodeDef.getName()));
        }
        return NodeUtils.normalizedFileName(job.getName());
    }

    /**
     * ERM迁移过来的start/end节点名称: {start|end}_{flow的名称}
     *
     * @param flowItem
     * @param flowNodeDef
     * @return
     */
    private String getStartEndNodeName(ListFlowResponse.FlowItem flowItem, FlowNodeDef flowNodeDef) {
        return Joiner.on("_").join(getStartEndRawNodeName(flowNodeDef), flowItem.getName());
    }

    /**
     * start和end节点的nodeDef type为 ":start:" 和 ":end:", 这里把:去掉作为原始节点名称
     *
     * @param flowNodeDef
     * @return
     */
    private String getStartEndRawNodeName(FlowNodeDef flowNodeDef) {
        return flowNodeDef.getType().replaceAll(":", "");
    }

    private List<Node> parseFlowNodes(AliyunEmrProject project,
        ListFlowResponse.FlowItem flowItem,
        Map<String, FlowNodeDef> app,
        Workflow workflow) {
        List<Node> nodeList = new ArrayList<>();
        Map<String, List<FlowNodeDef>> groupByJobId = app.values().stream()
            .filter(nd -> Objects.nonNull(nd.getJobId()))
            .collect(Collectors.groupingBy(FlowNodeDef::getJobId));
        for (FlowNodeDef nodeDef : app.values()) {
            DwNode node = new DwNode();
            try {
                node.setStartRightNow(supplyStartRightNow());
                node.setCronExpress(
                    CronExpressUtil.normalize(StringUtils.defaultIfBlank(flowItem.getCronExpr(), "day")));
                DescribeFlowResponse flowDetail = project.getFlows().get(flowItem);
                if (FLOW_NODE_DEF_TYPE_ACTION.equals(nodeDef.getType())) {
                    ListFlowJobResponse.Job job = project.getJobById(nodeDef.getJobId());
                    node.setName(getNodeName(groupByJobId, job, nodeDef));
                    node.setType(getNodeType(workflow, node, job));
                    node.setCode(convertCode(job));
                    node.setRawNodeType(RawNodeType.valueOf(job.getType()).name());
                    node.setTaskRerunTime(job.getMaxRetry());
                    node.setTaskRerunInterval((int)(job.getRetryInterval() * 1000));
                    node.setParameter(getNodeParameter(node, job));
                    NodeIo output = new NodeIo();
                    output.setData(getNodeDefaultOutput(project, flowItem, node.getName()));
                    output.setParseType(1);
                    node.getOutputs().add(output);
                    node.setDescription(job.getDescription());
                    node.setCode(handleJobCode(flowItem, node, job));
                    node.setResourceGroup(
                        properties.getProperty(Constants.CONVERTER_TARGET_SCHEDULE_RES_GROUP_IDENTIFIER, null));
                } else {
                    node.setName(getStartEndNodeName(flowItem, nodeDef));
                    node.setType(CodeProgramType.VIRTUAL.name());
                    node.setRawNodeType(getStartEndRawNodeName(nodeDef));
                    NodeIo output = new NodeIo();
                    output.setData(getNodeDefaultOutput(project, flowItem, getStartEndRawNodeName(nodeDef)));
                    output.setParseType(1);
                    node.getOutputs().add(output);
                }
                node.setPauseSchedule(StringUtils.equalsIgnoreCase("STOP_SCHEDULE", flowItem.getStatus()));
                if (flowDetail.getStartSchedule() != null) {
                    node.setStartEffectDate(DateUtils.convertLongToDate(flowDetail.getStartSchedule()));
                }
                if (flowDetail.getEndSchedule() != null) {
                    node.setEndEffectDate(DateUtils.convertLongToDate(flowDetail.getEndSchedule()));
                }
                node.setNodeUseType(NodeUseType.SCHEDULED);
                node.setOwner(this.project.getOpUser());
                node.setIsAutoParse(0);
                nodeList.add(node);
            } catch (Exception e) {
                ReportItem reportItem = new ReportItem();
                reportItem.setMessage(e.getMessage());
                reportItem.setException(ExceptionUtils.getStackTrace(e));
                reportItem.setRiskLevel(ReportRiskLevel.ERROR);
                reportItem.setNode(node);
                reportItem.setWorkflow(workflow);
                reportItem.setType(ReportItemType.EMR_JOB_TO_DATAWORKS_NODE.name());
                reportItem.setName(workflow.getName() + "/" + node.getName());
                reportItems.add(reportItem);
                LOGGER.error("convert node error: ", e);
            }
        }

        // for relations
        for (FlowNodeDef nodeDef : app.values()) {
            ListFlowJobResponse.Job job = project.getJobById(nodeDef.getJobId());
            String name = job != null ? getNodeName(groupByJobId, job, nodeDef) : getStartEndNodeName(flowItem,
                nodeDef);
            Node node = nodeList.stream().filter(n -> n.getName().equals(name)).findFirst().orElse(null);
            if (node == null) {
                continue;
            }

            if (nodeDef.getTransitions() == null) {
                continue;
            }

            for (String nodeKey : nodeDef.getTransitions()) {
                FlowNodeDef downstreamNodeDef = app.get(nodeKey);
                ListFlowJobResponse.Job downstreamJob = project.getJobById(downstreamNodeDef.getJobId());
                String downstreamNodeName = downstreamJob != null ?
                    getNodeName(groupByJobId, downstreamJob, downstreamNodeDef) : getStartEndNodeName(flowItem,
                    downstreamNodeDef);
                Node downstreamNode = nodeList.stream()
                    .filter(n -> n.getName().equals(downstreamNodeName)).findFirst().orElse(null);
                if (downstreamNode == null) {
                    continue;
                }

                downstreamNode.getInputs().add(node.getOutputs().get(0));
            }
        }
        return nodeList;
    }

    private Boolean supplyStartRightNow() {
        return BooleanUtils.toBoolean(properties.getProperty(
            Constants.CONVERTER_ALIYUN_EMR_TO_DATAWORKS_START_RIGHT_NOW, Boolean.FALSE.toString()));
    }

    private String handleJobCode(ListFlowResponse.FlowItem flowItem, DwNode node, ListFlowJobResponse.Job job) {
        DescribeClusterBasicInfoResponse.ClusterInfo clusterInfo = Optional.ofNullable(flowItem).map(getClusterInfoHook)
            .orElse(null);
        String useGatewayMode = properties.getProperty(Constants.CONVERTER_ALIYUN_EMR_TO_DATAWORKS_JOB_SUBMIT_MODE);
        Boolean gatewayAvailable = Optional.ofNullable(clusterInfo)
            .map(c -> CollectionUtils.isNotEmpty(c.getGatewayClusterInfoList()))
            .orElse(false);
        if (!gatewayAvailable) {
            // use gateway: false
            useGatewayMode = EmrJobMode.YARN.name();
        }

        node.setCode(EmrCodeUtils.toEmrCode(node));
        EmrCode emrCode = EmrCodeUtils.asEmrCode(node);
        if (emrCode == null || emrCode.getLauncher() == null || emrCode.getLauncher().getAllocationSpec() == null) {
            return node.getCode();
        }

        EmrAllocationSpec allocSpec = EmrAllocationSpec.of(emrCode.getLauncher().getAllocationSpec());
        EmrJobMode jobMode = EmrJobMode.getByValue(StringUtils.defaultIfBlank(
            StringUtils.defaultIfBlank(useGatewayMode, job.getMode()), EmrJobMode.YARN.getValue()));
        switch (jobMode) {
            case LOCAL:
                allocSpec.setUseGateway(true);
                break;
            case YARN:
            default:
                allocSpec.setUseGateway(false);
                break;
        }

        String reuseSessionStr = properties.getProperty(Constants.CONVERTER_ALIYUN_EMR_TO_DATAWORKS_REUSE_SESSION);
        if (StringUtils.isNotBlank(reuseSessionStr)) {
            allocSpec.setReuseSession(BooleanUtils.toBoolean(reuseSessionStr));
        }

        // cpu/vcores/memory/queue/...
        String runConfJsonStr = job.getRunConf();
        if (StringUtils.isNotBlank(runConfJsonStr)) {
            JsonObject runConfJson = GsonUtils.fromJsonString(runConfJsonStr, new TypeToken<JsonObject>() {}.getType());
            if (runConfJson != null && runConfJson.size() > 0) {
                if (runConfJson.has("cores")) {
                    allocSpec.setVcores(runConfJson.get("cores").getAsString());
                }
                if (runConfJson.has("memory")) {
                    allocSpec.setMemory(runConfJson.get("memory").getAsString());
                }
                if (runConfJson.has("priority")) {
                    allocSpec.setPriority(runConfJson.get("priority").getAsString());
                }
                if (runConfJson.has("userName")) {
                    allocSpec.setUserName(runConfJson.get("userName").getAsString());
                }
                if (runConfJson.has("queue")) {
                    allocSpec.setQueue(runConfJson.get("queue").getAsString());
                }
            }
        }
        emrCode.getLauncher().setAllocationSpec(allocSpec.toMap());
        return EmrCodeUtils.toString(emrCode);
    }

    private String convertCode(ListFlowJobResponse.Job job) {
        EmrJobType jobType = EmrJobType.getJobType(job.getType());
        String originalCode = job.getParams();
        String finalCode = originalCode;
        switch (jobType) {
            case SQOOP:
                finalCode = convertSqoopCode(originalCode);
                break;
            case HIVE:
                // EMR HIVE to EMR_SHELL with hive command name appending with original code
                finalCode = Joiner.on(" ").join("hive", originalCode);
                break;
            default:
        }
        return finalCode;
    }

    private String convertSqoopCode(String originalCode) {
        String datasourceName = getDefaultCalcEngineDatasource(properties, project.getName());
        CalcEngineType engineType = getDefaultCalcEngineType(properties, CalcEngineType.ODPS);
        DICode diCode = DICode.parseDiCode(originalCode, engineType, datasourceName,
            DefaultNodeTypeUtils.getDatasourceTypeByEngineType(engineType));
        List<DwDatasource> dsList = DiCodeUtils.processSqoopDatasource(diCode);
        ListUtils.emptyIfNull(dsList).stream().forEach(ds -> {
            ds.setProjectRef(project);
            ds.setEnvType(EnvType.PRD.name());
            if (project.getDatasources().stream().noneMatch(d -> d.getName().equals(ds.getName()))) {
                project.getDatasources().add(ds);
            }
        });
        return diCode.getCode();
    }

    private String getNodeParameter(DwNode node, ListFlowJobResponse.Job job) {
        if (job != null && StringUtils.isNotBlank(job.getParamConf())) {
            CodeProgramType nodeType = CodeProgramType.valueOf(node.getType());
            Map<String, String> paramMap = GsonUtils.fromJsonString(
                job.getParamConf(), new TypeToken<Map<String, String>>() {}.getType());
            if (MapUtils.isEmpty(paramMap)) {
                return null;
            }

            switch (nodeType) {
                case DIDE_SHELL:
                    return Joiner.on(" ").join(paramMap.values());
                default:
                    return Joiner.on(" ").join(paramMap.entrySet().stream()
                        .map(e -> Joiner.on("=").join(e.getKey(), e.getValue())).collect(Collectors.toList()));
            }
        }
        return null;
    }

    private String getNodeDefaultOutput(
        AliyunEmrProject project, ListFlowResponse.FlowItem flowItem, String jobName) {
        return Joiner.on(".").join(this.project.getName(), project.getProject().getName(), flowItem.getName(), jobName);
    }

    private String getNodeDefaultOutput(String emrProjectName, String emrFlowName, String emrJobName) {
        return Joiner.on(".").join(this.project.getName(), emrProjectName, emrFlowName, emrJobName);
    }

    private void convertProjectJobList(AliyunEmrProject project) {
        this.project.setAdHocQueries(ListUtils.emptyIfNull(project.getJobs()).stream()
            .filter(job -> BooleanUtils.toBoolean(job.getAdhoc()))
            .map(job -> {
                DwNode node = new DwNode();
                node.setName(job.getName());
                node.setCode(job.getParams());
                node.setRawNodeType(RawNodeType.valueOf(job.getType()).name());
                node.setType(getNodeType(null, node, job));
                node.setOwner(this.project.getOpUser());
                node.setNodeUseType(NodeUseType.AD_HOC);
                node.setFolder(ModelTreeRoot.QUERY_ROOT.getRootKey());
                node.setCode(EmrCodeUtils.toEmrCode(node));
                return node;
            }).collect(Collectors.toList()));
    }

    /**
     * 不支持的节点设置为虚节点
     *
     * @param workflow
     * @param node
     * @param job
     * @return
     */
    private String getNodeType(Workflow workflow, Node node, ListFlowJobResponse.Job job) {
        EmrJobType jobType = EmrJobType.getJobType(job.getType());
        if (nodeTypeMap.containsKey(jobType)) {
            return nodeTypeMap.get(jobType).getName();
        }

        if (EmrJobType.HIVE_SQL.equals(jobType)) {
            return CodeProgramType.ODPS_SQL.name();
        }

        if (EmrJobType.SPARK_SQL.equals(jobType)) {
            return CodeProgramType.ODPS_SQL.name();
        }

        if (EmrJobType.SPARK_SQL.equals(jobType)) {
            return CodeProgramType.ODPS_SQL.name();
        }

        if (EmrJobType.SHELL.equals(jobType)) {
            return CodeProgramType.DIDE_SHELL.name();
        }

        if (workflow != null) {
            ReportItem reportItem = new ReportItem();
            reportItem.setWorkflow(workflow);
            reportItem.setNode(node);
            reportItem.setRiskLevel(ReportRiskLevel.ERROR);
            reportItem.setMessage("unsupported job type:" + job.getType());
            reportItem.setName(workflow.getName() + "/" + node.getName());
            reportItem.setType(ReportItemType.UNSUPPORTED_JOB_TYPE.name());
            reportItems.add(reportItem);
        }

        return getDefaultTypeIfNotSupported(properties, CodeProgramType.DIDE_SHELL);
    }

    public static List<AliyunEmrProject> load(String fromFolder) throws IOException {
        File folderPath = new File(fromFolder);

        List<AliyunEmrProject> projects = new ArrayList<>();
        File[] subDirs = folderPath.listFiles(File::isDirectory);
        if (subDirs == null) {
            return ListUtils.emptyIfNull(null);
        }

        for (File projectPath : subDirs) {
            File projectJsonFile = new File(
                projectPath.getAbsolutePath() + File.separator + projectPath.getName() + AliyunEmrService.JSON_FILE_EXT);
            if (!projectJsonFile.exists()) {
                LOGGER.error("project json file not exists: {}", projectJsonFile);
                continue;
            }

            String prjJson = FileUtils.readFileToString(projectJsonFile, Charset.forName(AliyunEmrService.FILE_ENCODE));

            ListFlowProjectResponse.Project project = GsonUtils.gson.fromJson(
                prjJson, new TypeToken<ListFlowProjectResponse.Project>() {}.getType());
            List<ListFlowJobResponse.Job> jobs = loadProjectJobs(projectPath);
            Map<ListFlowResponse.FlowItem, DescribeFlowResponse> flows = loadProjectFlows(projectPath);

            AliyunEmrProject aliyunEmrProject = new AliyunEmrProject();
            aliyunEmrProject.setProject(project);
            aliyunEmrProject.setFlows(flows);
            aliyunEmrProject.setJobs(jobs);
            projects.add(aliyunEmrProject);
        }
        return projects;
    }

    private static Map<ListFlowResponse.FlowItem, DescribeFlowResponse> loadProjectFlows(File projectPath)
        throws IOException {
        File flowFolder = new File(projectPath.getAbsolutePath() + File.separator + AliyunEmrService.FLOW_DIR_NAME);
        Map<ListFlowResponse.FlowItem, DescribeFlowResponse> flows = new HashMap<>(100);
        File[] files = flowFolder.listFiles(f -> f.isFile() && f.getName().endsWith(AliyunEmrService.JSON_FILE_EXT));
        if (files == null) {
            return flows;
        }

        for (File flowJsonFile : files) {
            if (flowJsonFile.getName().contains(AliyunEmrService.FLOW_DETAIL_EXT)) {
                continue;
            }

            String flowJson = FileUtils.readFileToString(flowJsonFile, Charset.forName(AliyunEmrService.FILE_ENCODE));
            ListFlowResponse.FlowItem flowItem = GsonUtils.gson.fromJson(
                flowJson, new TypeToken<ListFlowResponse.FlowItem>() {}.getType());

            File jobDetailFile = new File(
                flowJsonFile.getAbsolutePath().replaceAll(AliyunEmrService.JSON_FILE_EXT + "$", "")
                    + AliyunEmrService.FLOW_DETAIL_EXT + AliyunEmrService.JSON_FILE_EXT);
            DescribeFlowResponse flowDetail = null;
            if (jobDetailFile.exists()) {
                String jobDetailJson = FileUtils.readFileToString(jobDetailFile, Charset.forName(
                    AliyunEmrService.FILE_ENCODE));
                flowDetail = GsonUtils.gson.fromJson(jobDetailJson, new TypeToken<DescribeFlowResponse>() {}.getType());
            }
            flows.put(flowItem, flowDetail);
        }
        return flows;
    }

    private static List<ListFlowJobResponse.Job> loadProjectJobs(File projectPath) throws IOException {
        File jobFolder = new File(projectPath.getAbsolutePath() + File.separator + AliyunEmrService.JOB_DIR_NAME);
        List<ListFlowJobResponse.Job> jobs = new ArrayList<>(100);
        File[] files = jobFolder.listFiles(f -> f.isFile() && f.getName().endsWith(AliyunEmrService.JSON_FILE_EXT));
        if (files == null) {
            return jobs;
        }

        for (File jobJsonFile : files) {
            String jobJson = FileUtils.readFileToString(jobJsonFile, Charset.forName(AliyunEmrService.FILE_ENCODE));
            ListFlowJobResponse.Job jobItem = GsonUtils.gson.fromJson(
                jobJson, new TypeToken<ListFlowJobResponse.Job>() {}.getType());
            jobs.add(jobItem);
        }
        return jobs;
    }
}
