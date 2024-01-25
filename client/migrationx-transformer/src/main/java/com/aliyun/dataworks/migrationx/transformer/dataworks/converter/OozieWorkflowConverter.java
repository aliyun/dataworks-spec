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

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwDatasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Workflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowVersion;
import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.tenant.EnvType;
import com.aliyun.dataworks.migrationx.domain.dataworks.oozie.FakeElEvaluatorContext;
import com.aliyun.dataworks.migrationx.domain.dataworks.oozie.OozieActionType;
import com.aliyun.dataworks.migrationx.domain.dataworks.oozie.OozieConstants;
import com.aliyun.dataworks.migrationx.domain.dataworks.oozie.OozieNodeType;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.NodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.RawNodeType;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.loader.ProjectAssetLoader;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItem;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItemType;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportRiskLevel;
import com.aliyun.dataworks.migrationx.transformer.core.sqoop.DICode;
import com.aliyun.dataworks.migrationx.transformer.core.translator.TranslateUtils;
import com.aliyun.dataworks.migrationx.transformer.core.utils.DiCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import com.google.common.base.Joiner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.oozie.ErrorCode;
import org.apache.oozie.service.ServiceException;
import org.apache.oozie.util.ELEvaluator;
import org.apache.oozie.util.XmlUtils;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * oozie workflow app 转换
 *
 * @author sam.liux
 * @date 2019/04/24
 */
public class OozieWorkflowConverter extends AbstractBaseConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(OozieWorkflowConverter.class);
    private static final String DEFAULT_DATASOURCE = "odps_first";
    private static final String DEFAULT_DATASOURCE_TYPE = "odps";

    private Map<String, ELEvaluator.Context> contextMap = new ConcurrentHashMap<>();
    private Asset asset;
    private ThreadLocal<List<NodeRelation>> relations = ThreadLocal.withInitial(() -> new ArrayList<>());
    private Project project;
    private Properties properties;

    class NodeRelation {
        public String nodeFrom;
        public String nodeTo;
    }

    public OozieWorkflowConverter() {
        super(AssetType.OOZIE, "OozieWorkflowConverter");
    }

    public OozieWorkflowConverter(AssetType assetType, String name, ProjectAssetLoader projectAssetLoader) {
        super(assetType, name, projectAssetLoader);
    }

    private void loadELFFunctions(ELEvaluator.Context context) {
        try {
            extractFunctionsAndConstants(context, new String[] {
                "wf:id=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_id",
                "wf:name=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_name",
                "wf:appPath=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_appPath",
                "wf:conf=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_conf",
                "wf:user=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_user",
                "wf:group=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_group",
                "wf:callback=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_callback",
                "wf:transition=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_transition",
                "wf:lastErrorNode=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_lastErrorNode",
                "wf:errorCode=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_errorCode",
                "wf:errorMessage=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_errorMessage",
                "wf:run=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_run",
                "wf:actionData=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_actionData",
                "wf:actionExternalId=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_actionExternalId",
                "wf:actionTrackerUri=com.alibaba.dataworks.migration.core.oozie.FakeDagELFunctions#wf_actionTrackerUri",
                "wf:actionExternalStatus=com.alibaba.dataworks.migration.core.oozie"
                    + ".FakeDagELFunctions#wf_actionExternalStatus",

                "coord:days=org.apache.oozie.coord.CoordELFunctions#ph1_coord_days",
                "coord:months=org.apache.oozie.coord.CoordELFunctions#ph1_coord_months",
                "coord:hours=org.apache.oozie.coord.CoordELFunctions#ph1_coord_hours",
                "coord:minutes=org.apache.oozie.coord.CoordELFunctions#ph1_coord_minutes",
                "coord:hoursInDay=org.apache.oozie.coord.CoordELFunctions#ph1_coord_hoursInDay_echo",
                "coord:daysInMonth=org.apache.oozie.coord.CoordELFunctions#ph1_coord_daysInMonth_echo",
                "coord:tzOffset=org.apache.oozie.coord.CoordELFunctions#ph1_coord_tzOffset_echo",
                "coord:current=org.apache.oozie.coord.CoordELFunctions#ph1_coord_current_echo",
                "coord:currentRange=org.apache.oozie.coord.CoordELFunctions#ph1_coord_currentRange_echo",
                "coord:offset=org.apache.oozie.coord.CoordELFunctions#ph1_coord_offset_echo",
                "coord:latest=org.apache.oozie.coord.CoordELFunctions#ph1_coord_latest_echo",
                "coord:latestRange=org.apache.oozie.coord.CoordELFunctions#ph1_coord_latestRange_echo",
                "coord:future=org.apache.oozie.coord.CoordELFunctions#ph1_coord_future_echo",
                "coord:futureRange=org.apache.oozie.coord.CoordELFunctions#ph1_coord_futureRange_echo",
                "coord:formatTime=org.apache.oozie.coord.CoordELFunctions#ph1_coord_formatTime_echo",
                "coord:epochTime=org.apache.oozie.coord.CoordELFunctions#ph1_coord_epochTime_echo",
                "coord:conf=org.apache.oozie.coord.CoordELFunctions#coord_conf",
                "coord:user=org.apache.oozie.coord.CoordELFunctions#coord_user",
                "coord:absolute=org.apache.oozie.coord.CoordELFunctions#ph1_coord_absolute_echo",
                "coord:endOfMonths=org.apache.oozie.coord.CoordELFunctions#ph1_coord_endOfMonths_echo",
                "coord:endOfWeeks=org.apache.oozie.coord.CoordELFunctions#ph1_coord_endOfWeeks_echo",
                "coord:endOfDays=org.apache.oozie.coord.CoordELFunctions#ph1_coord_endOfDays_echo",
                "coord:dateTzOffset=org.apache.oozie.coord.CoordELFunctions#ph1_coord_dateTzOffset_echo",
                "coord:dataIn=org.apache.oozie.coord.CoordELFunctions#ph1_coord_dataIn_echo",
                "coord:dataOut=org.apache.oozie.coord.CoordELFunctions#ph1_coord_dataOut_echo",
                "coord:nominalTime=org.apache.oozie.coord.CoordELFunctions#ph1_coord_nominalTime_echo_wrap",
                "coord:actualTime=org.apache.oozie.coord.CoordELFunctions#ph1_coord_actualTime_echo_wrap",
                "coord:dateOffset=org.apache.oozie.coord.CoordELFunctions#ph1_coord_dateOffset_echo",
                "coord:dateTzOffset=org.apache.oozie.coord.CoordELFunctions#ph1_coord_dateTzOffset_echo",
                "coord:formatTime=org.apache.oozie.coord.CoordELFunctions#ph1_coord_formatTime_echo",
                "coord:epochTime=org.apache.oozie.coord.CoordELFunctions#ph1_coord_epochTime_echo",
                "coord:actionId=org.apache.oozie.coord.CoordELFunctions#ph1_coord_actionId_echo",
                "coord:name=org.apache.oozie.coord.CoordELFunctions#ph1_coord_name_echo",
                "coord:conf=org.apache.oozie.coord.CoordELFunctions#coord_conf",
                "coord:user=org.apache.oozie.coord.CoordELFunctions#coord_user",
                "coord:databaseIn=org.apache.oozie.coord.HCatELFunctions#ph1_coord_databaseIn_echo",
                "coord:databaseOut=org.apache.oozie.coord.HCatELFunctions#ph1_coord_databaseOut_echo",
                "coord:tableIn=org.apache.oozie.coord.HCatELFunctions#ph1_coord_tableIn_echo",
                "coord:tableOut=org.apache.oozie.coord.HCatELFunctions#ph1_coord_tableOut_echo",
                "coord:dataInPartitionFilter=org.apache.oozie.coord"
                    + ".HCatELFunctions#ph1_coord_dataInPartitionFilter_echo",
                "coord:dataInPartitionMin=org.apache.oozie.coord.HCatELFunctions#ph1_coord_dataInPartitionMin_echo",
                "coord:dataInPartitionMax=org.apache.oozie.coord.HCatELFunctions#ph1_coord_dataInPartitionMax_echo",
                "coord:dataInPartitions=org.apache.oozie.coord.HCatELFunctions#ph1_coord_dataInPartitions_echo",
                "coord:dataOutPartitions=org.apache.oozie.coord.HCatELFunctions#ph1_coord_dataOutPartitions_echo",
                "coord:dataOutPartitionValue=org.apache.oozie.coord"
                    + ".HCatELFunctions#ph1_coord_dataOutPartitionValue_echo"
            }, new String[] {
                "MINUTE=org.apache.oozie.coord.CoordELConstants#SUBMIT_MINUTE",
                "HOUR=org.apache.oozie.coord.CoordELConstants#SUBMIT_HOUR",
                "DAY=org.apache.oozie.coord.CoordELConstants#SUBMIT_DAY",
                "MONTH=org.apache.oozie.coord.CoordELConstants#SUBMIT_MONTH",
                "YEAR=org.apache.oozie.coord.CoordELConstants#SUBMIT_YEAR"
            });
        } catch (ServiceException e) {
            LOGGER.error("", e);
        }
    }

    @Override
    public List<DwWorkflow> convert(Asset asset) {
        this.asset = asset;
        this.properties = this.properties == null && propertiesLoader != null ? propertiesLoader.getResult()
            : this.properties;
        List<DwWorkflow> workflowList = new ArrayList<>();
        // for each oozie workflow dir
        File[] workflowDirsArray = asset.getPath().listFiles(f -> f.isDirectory() && !f.isHidden());
        List<File> workflowDirs = new ArrayList<>();
        if (workflowDirsArray != null) {
            workflowDirs.addAll(Arrays.asList(workflowDirsArray));
        }

        LOGGER.info("workflowDirs: {}", workflowDirs);
        if (CollectionUtils.isEmpty(workflowDirs) && !new File(asset.getPath(), "workflow.xml").exists()) {
            return workflowList;
        } else if (CollectionUtils.isEmpty(workflowDirs)) {
            workflowDirs.add(asset.getPath());
        }

        ListUtils.emptyIfNull(workflowDirs).stream().forEach(workflowDir -> {
            DwWorkflow workflow = convertOozieWorkflow(workflowDir);
            if (workflow != null) {
                workflowList.add(workflow);
                LOGGER.info("found oozie workflow: {}, path: {}", workflow.getName(), workflowDir.getAbsolutePath());
            }
        });
        return workflowList;
    }

    private DwWorkflow convertOozieWorkflow(File workflowDir) {
        this.relations.set(new ArrayList<>());
        String wfxmlFile = workflowDir.getAbsolutePath() + File.separator + "workflow.xml";
        try {
            this.contextMap.put(workflowDir.getAbsolutePath(), new FakeElEvaluatorContext());
            ELEvaluator elEvaluator = new ELEvaluator(this.contextMap.get(workflowDir.getAbsolutePath()));
            loadELFFunctions(elEvaluator.getContext());

            String wfxml = IOUtils.toString(new FileInputStream(new File(wfxmlFile)));
            String jobProp = workflowDir.getAbsolutePath() + File.separator + "job.properties";
            Properties jobProperties = new Properties();
            if (new File(jobProp).exists()) {
                jobProperties.load(new FileInputStream(new File(jobProp)));
                // for (Map.Entry<Object, Object> entry : jobProperties.entrySet()) {
                //    this.contextMap.get(workflowDir.getAbsolutePath()).setVariable((String)entry.getKey(), entry
                //    .getValue());
                //}
            }

            Element coordApp = processCoordinateApp(workflowDir, elEvaluator);
            try {
                wfxml = elEvaluator.evaluate(wfxml, String.class);
            } catch (Exception e) {
                LOGGER.error("evaluate xml variables failed: {}", e.getMessage());
                reportException(workflowDir, e);
            }

            Element workflowXml = XmlUtils.parseXml(wfxml);
            DwWorkflow workflow = parseAndConvertWorkflow(workflowXml, workflowDir.getAbsolutePath(), coordApp);
            processWorkflowRelations(workflow);
            processWorkflowNodeParameters(workflow, jobProperties);
            processProjectRootDependency(workflow, coordApp);
            return workflow;
        } catch (Exception e) {
            LOGGER.error("parse workflow failed: {}", e.getMessage());
            reportException(workflowDir, e);
        }
        return null;
    }

    private void processProjectRootDependency(DwWorkflow workflow, Element coordApp) throws Exception {
        for (Node node : workflow.getNodes()) {
            node.setCronExpress(getCronExpress(coordApp));
            if (CollectionUtils.isEmpty(node.getInputs()) && workflow.getScheduled()) {
                node.setInputs(Collections.singletonList(new NodeIo(NodeUtils.getProjectRootOutput(project))));
                ListUtils.emptyIfNull(node.getInputs()).forEach(in -> in.setParseType(1));
            }
        }
    }

    private Element processCoordinateApp(File workflowDir, ELEvaluator elEvaluator) throws Exception {
        Element coordApp = null;
        String coordinatorXml = workflowDir.getAbsolutePath() + File.separator + "coordinator.xml";
        if (new File(coordinatorXml).exists()) {
            String coordXml = IOUtils.toString(new FileInputStream(new File(coordinatorXml)));
            coordApp = XmlUtils.parseXml(coordXml);

            // 预解析input/output，设置到context里面去, 以免后面evaluate报错
            Element inputLogic = coordApp.getChild("input-logic", coordApp.getNamespace());
            if (inputLogic != null) {
                for (Object logicInObj : inputLogic.getChildren()) {
                    Element logicIn = (Element)logicInObj;
                    List<Element> dataIn = logicIn.getChildren();
                    if (CollectionUtils.isNotEmpty(dataIn)) {
                        elEvaluator.setVariable(
                            "oozie.dataname." + logicIn.getAttributeValue("name"), dataIn.get(0).getName());
                    }
                }
            }

            Element inputEvents = coordApp.getChild("input-events", coordApp.getNamespace());
            if (inputEvents != null) {
                for (Object dataInObj : inputEvents.getChildren()) {
                    Element dataIn = (Element)dataInObj;
                    elEvaluator.setVariable(
                        "oozie.dataname." + dataIn.getAttributeValue("name"), dataIn.getName());
                }
            }

            Element outputEvents = coordApp.getChild("output-events", coordApp.getNamespace());
            if (outputEvents != null) {
                for (Object dataOutObj : outputEvents.getChildren()) {
                    Element dataOut = (Element)dataOutObj;
                    elEvaluator.setVariable(
                        "oozie.dataname." + dataOut.getAttributeValue("name"), dataOut.getName());
                }
            }

            coordXml = elEvaluator.evaluate(coordXml, String.class);
            coordApp = XmlUtils.parseXml(coordXml);
            handleCoordinatorAppAction(workflowDir, elEvaluator.getContext(), coordApp);
        }
        return coordApp;
    }

    private void processWorkflowNodeParameters(DwWorkflow workflow, Properties jobProperties) {
        ListUtils.emptyIfNull(workflow.getNodes()).stream().filter(n -> StringUtils.isNotBlank(n.getCode())).forEach(
            n -> {
                List<String> params = parseCodeParameters(n.getCode());
                String nodeParam = Joiner.on(" ").join(ListUtils.emptyIfNull(params).stream()
                    .map(p -> {
                        String value = jobProperties.getProperty(p, Constants.UNDEFINED_VARIABLE_VALUE);
                        if (!CodeProgramType.DIDE_SHELL.name().equalsIgnoreCase(n.getType())) {
                            return Joiner.on("=").join(p, value);
                        }
                        return value;
                    }).collect(Collectors.toList()));
                n.setParameter(nodeParam);
            });
    }

    private void handleCoordinatorAppAction(File workflowDir, ELEvaluator.Context context, Element coordApp) {
        for (Object item : coordApp.getChildren()) {
            Element element = (Element)item;
            try {
                OozieNodeType oozieNodeType = OozieNodeType.getOozieNodeType(element.getName());
                if (oozieNodeType.equals(OozieNodeType.ACTION)) {
                    Element workflow = element.getChild("workflow", element.getNamespace());
                    String appPath = workflow.getChild("app-path", element.getNamespace()).getTextTrim();
                    File appPathFile = new File(appPath);
                    if (appPathFile.getName().equals(workflowDir.getName())) {
                        Element configuration = workflow.getChild("configuration", element.getNamespace());
                        if (configuration != null) {
                            for (Object proObj : configuration.getChildren()) {
                                Element pro = (Element)proObj;
                                String name = pro.getChild("name", element.getNamespace()).getTextTrim();
                                String value = pro.getChild("value", element.getNamespace()).getTextTrim();
                                context.setVariable(name, value);
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private String replaceInvalidChar(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }

        return name
            .replaceAll("-", "_")
            .replaceAll("\\(", "__").replaceAll("\\)", "__");
    }

    private void reportException(File workflowDir, Exception e) {
        ReportItem reportItem = new ReportItem();
        reportItem.setName(workflowDir.getName());
        reportItem.setType(ReportItemType.OOZIE_TO_DATAWORKS.getName());
        reportItem.setPath(workflowDir.getAbsolutePath());
        reportItem.setMessage(e.getMessage());
        reportItem.setException(ExceptionUtils.getStackTrace(e));
        reportItem.setRiskLevel(ReportRiskLevel.ERROR);
        reportItems.add(reportItem);
    }

    private void reportException(Workflow workflow, Exception e) {
        ReportItem reportItem = new ReportItem();
        reportItem.setName(String.format("Workflow:%s", workflow.getName()));
        reportItem.setType(ReportItemType.OOZIE_TO_DATAWORKS.getName());
        reportItem.setMessage(e.getMessage());
        reportItem.setException(ExceptionUtils.getStackTrace(e));
        reportItem.setRiskLevel(ReportRiskLevel.ERROR);
        reportItems.add(reportItem);
    }

    private void reportException(Workflow workflow, Node node, Exception e, ReportItemType reportItemType) {
        ReportItem reportItem = new ReportItem();
        reportItem.setName(String.format("Workflow:%s / Node:%s", workflow.getName(), node.getName()));
        reportItem.setType(reportItemType.getName());
        reportItem.setMessage(e.getMessage());
        reportItem.setException(ExceptionUtils.getStackTrace(e));
        reportItem.setRiskLevel(ReportRiskLevel.ERROR);
        reportItems.add(reportItem);
    }

    private void processWorkflowRelations(Workflow workflow) {
        for (NodeRelation relation : relations.get()) {
            Node nodeFrom = getWorkflowNodeByName(workflow, relation.nodeFrom);
            Node nodeTo = getWorkflowNodeByName(workflow, relation.nodeTo);
            if (nodeFrom == null || nodeTo == null) {
                LOGGER.debug("node not found, nodeFrom: {}={}, nodeTo: nodeTo={}",
                    relation.nodeFrom, nodeFrom == null, relation.nodeTo, nodeTo == null);
                continue;
            }

            if (CollectionUtils.isEmpty(nodeFrom.getOutputs())) {
                LOGGER.debug("node {} output is empty", nodeFrom.getName());
                continue;
            }

            NodeIo input = nodeFrom.getOutputs().get(0);
            boolean match = ListUtils.emptyIfNull(nodeTo.getInputs()).stream().anyMatch(in -> isTheSameIo(in, input));
            if (!match) {
                nodeTo.getInputs().add(input);
            }
        }
    }

    private boolean isTheSameIo(NodeIo a, NodeIo b) {
        if (!a.getData().equalsIgnoreCase(b.getData())) {
            return false;
        }

        if (StringUtils.isNotBlank(a.getRefTableName()) && !a.getRefTableName().equalsIgnoreCase(b.getRefTableName())) {
            return false;
        }

        return true;
    }

    private Node getWorkflowNodeByName(Workflow workflow, String nodeName) {
        for (Node node : workflow.getNodes()) {
            if (node.getName().equalsIgnoreCase(nodeName)) {
                return node;
            }
        }
        return null;
    }

    private DwWorkflow parseAndConvertWorkflow(Element workflowXml, String workflowPath, Element coordApp)
        throws Exception {
        DwWorkflow workflow = new DwWorkflow();
        workflow.setVersion(WorkflowVersion.V3);
        workflow.setName(replaceInvalidChar(Joiner.on("_").join(
            workflowXml.getAttributeValue("name"), new File(workflowPath).getName())));

        workflow.setScheduled(coordApp != null);
        List<Element> children = workflowXml.getChildren();
        for (Element child : children) {
            try {
                handleWorkflowChild(child, workflow, workflowPath);
            } catch (Exception e) {
                LOGGER.error("{}", e.getMessage());
                reportException(workflow, e);
            }
        }

        return workflow;
    }

    private List<String> parseCodeParameters(String code) {
        Pattern pattern = Pattern.compile("\\$\\{\\w+\\}");
        Matcher matcher = pattern.matcher(code);
        Set<String> params = new HashSet<>();
        while (matcher.find()) {
            String param = matcher.group();
            param = param.replaceAll("\\$\\{", "").replaceAll("\\}", "");
            params.add(param);
        }
        return new ArrayList<>(params);
    }

    private void handleWorkflowChild(Element child, Workflow workflow, String workflowPath) throws Exception {
        String type = child.getName();
        OozieNodeType oozieNodeType = OozieNodeType.getOozieNodeType(type);
        switch (oozieNodeType) {
            case START:
            case END:
            case JOIN:
                addVirtualNode(child, workflow);
                break;
            case KILL:
                LOGGER.info("skip kill node: {}", child.toString());
                break;
            case ACTION:
                addActionNode(child, workflow, workflowPath);
                break;
            case FORK:
                addForkNode(child, workflow);
                break;
            default:
                throw new Exception("unsupported oozie action type: " + type);
        }
    }

    private void addForkNode(Element child, Workflow workflow) {
        DwNode node = new DwNode();
        node.setIsAutoParse(0);
        node.setName(
            replaceInvalidChar(child.getAttribute("name") == null ? child.getName() : child.getAttributeValue("name")));
        node.setOwner(this.project.getOpUser());
        node.setType(CodeProgramType.VIRTUAL.getName());
        node.setStartRightNow(false);
        node.setPauseSchedule(false);
        node.setCronExpress("day");
        node.setNodeUseType(
            BooleanUtils.isTrue(workflow.getScheduled()) ? NodeUseType.SCHEDULED : NodeUseType.MANUAL_WORKFLOW);

        NodeIo output = new NodeIo();
        output.setData(NodeUtils.getDefaultNodeOutput(workflow, node));
        output.setParseType(1);
        node.setOutputs(Arrays.asList(output));

        List<Element> paths = child.getChildren("path", child.getNamespace());
        for (Element path : paths) {
            String toNode = replaceInvalidChar(path.getAttributeValue("start"));
            NodeRelation relation = new NodeRelation();
            relation.nodeFrom = node.getName();
            relation.nodeTo = toNode;
            relations.get().add(relation);
        }

        workflow.getNodes().add(node);
    }

    private void addActionNode(Element child, Workflow workflow, String workflowPath) {
        List<Element> children = child.getChildren();
        for (Element actionXml : children) {
            try {
                OozieActionType actionType = OozieActionType.getOozieActionType(actionXml.getName());
                DwNode node = new DwNode();
                node.setIsAutoParse(0);
                node.setName(replaceInvalidChar(
                    child.getAttribute("name") == null ? child.getName() : child.getAttributeValue("name")));
                node.setNodeUseType(
                    BooleanUtils.isTrue(workflow.getScheduled()) ? NodeUseType.SCHEDULED : NodeUseType.MANUAL_WORKFLOW);
                node.setRawNodeType(Optional.ofNullable(getRawNodeType(actionType)).map(Enum::name)
                    .orElse(actionType.getAction()));
                node.setCronExpress("day");
                node.setWorkflowRef(workflow);
                switch (actionType) {
                    case HIVE:
                    case HIVE2:
                        handleHiveNode(actionXml, node, workflow, workflowPath);
                        break;
                    case SQOOP:
                        handleSqoopNode(actionXml, node, workflow);
                        break;
                    case SHELL:
                        handleShellNode(actionXml, node, workflow, workflowPath);
                        break;
                    case OK:
                        handleHiveOk(actionXml);
                        break;
                    case ERROR:
                    case EMAIL:
                        handleHiveError(actionXml);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                LOGGER.error("{}", e.getMessage());
                reportException(workflow, e);
            }
        }
    }

    private RawNodeType getRawNodeType(OozieActionType actionType) {
        switch (actionType) {
            case HIVE:
                return RawNodeType.OOZIE_HIVE;
            case OK:
                return RawNodeType.OOZIE_OK;
            case EMAIL:
                return RawNodeType.OOZIE_EMAIL;
            case ERROR:
                return RawNodeType.OOZIE_ERROR;
            case HIVE2:
                return RawNodeType.OOZIE_HIVE2;
            case SHELL:
                return RawNodeType.OOZIE_SHELL;
            case SQOOP:
                return RawNodeType.OOZIE_SQOOP;
        }
        return null;
    }

    private void handleShellNode(Element actionXml, DwNode node, Workflow workflow, String workflowPath) {
        try {
            String type = properties.getProperty(
                Constants.CONVERTER_TARGET_SHELL_NODE_TYPE_AS, CodeProgramType.EMR_SHELL.name());
            node.setType(type);
            node.setOwner(this.project.getOpUser());
            node.setStartRightNow(false);
            node.setPauseSchedule(false);
            NodeIo output = new NodeIo();
            output.setData(NodeUtils.getDefaultNodeOutput(workflow, node));
            output.setParseType(1);
            node.setOutputs(Arrays.asList(output));

            FakeElEvaluatorContext context = new FakeElEvaluatorContext();
            Element exec = actionXml.getChild(OozieConstants.SHELL_EXEC, actionXml.getNamespace());
            List<Element> arguments = actionXml.getChildren(OozieConstants.SHELL_ARGUMENT, actionXml.getNamespace());
            List<Element> envVars = actionXml.getChildren(OozieConstants.SHELL_ENV_VAR, actionXml.getNamespace());
            LOGGER.info("exec: {}, arguments: {}, envVars: {}", exec, arguments, envVars);
            ArrayList<String> lines = new ArrayList<>();
            ListUtils.emptyIfNull(envVars).stream().forEach(e -> lines.add(e.getTextTrim()));
            lines.add(exec.getTextTrim() + " " +
                Joiner.on(" ").join(
                    ListUtils.emptyIfNull(arguments).stream().map(arg -> arg.getTextTrim())
                        .collect(Collectors.toList()))
            );

            String code = Joiner.on("\n").join(lines);
            node.setCode(code);
            node.setCode(EmrCodeUtils.toEmrCode(node));

            // 处理 param INPUT=/xx/ss
            if (actionXml.getName().equalsIgnoreCase(OozieConstants.PARAM)) {
                String txt = actionXml.getText().trim();
                String[] kv = txt.split("=");
                if (kv.length == 2) {
                    context.setVariable(kv[0], kv[1]);
                }
            }

            if (StringUtils.isNotBlank(node.getCode())) {
                ELEvaluator elEvaluator = new ELEvaluator(context);
                code = elEvaluator.evaluate(node.getCode(), String.class);
                node.setCode(code);
            }

            TranslateUtils.translateSparkSubmit(node, properties);
            TranslateUtils.translateCommandSql((DwWorkflow)node.getWorkflowRef(), node, properties);
            workflow.getNodes().add(node);
        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage());
            reportException(workflow, node, e, ReportItemType.HIVE_SQL_TO_MC_SQL);
        }
    }

    private String getCronExpress(Element coordApp) throws Exception {
        if (coordApp == null) {
            return null;
        }

        String frequency = coordApp.getAttributeValue("frequency");
        String[] cron = new String[] {"0", "0", "0", "*", "*", "*"};
        if (NumberUtils.isNumber(frequency)) {
            Integer minutes = Integer.valueOf(frequency);
            int[] steps = new int[] {1, 60, 1440, 43200};

            for (int i = steps.length - 1; i >= 0; i--) {
                if (minutes / steps[i] > 0) {
                    cron[i + 1] = "*/" + minutes / steps[i];
                    break;
                }
            }
            return Joiner.on(" ").join(cron);
        } else {
            cron = frequency.split(" ");
            if (cron.length > 6) {
                throw new Exception("invalid cron expression: " + frequency);
            }

            if (cron.length == 6) {
                return Joiner.on(" ").join(cron);
            }

            List<String> arr = new ArrayList<>(Arrays.asList(cron));
            arr.add(0, "0");
            return Joiner.on(" ").join(arr);
        }
    }

    private void handleSqoopNode(Element actionXml, Node node, Workflow workflow) {
        try {
            node.setType(CodeProgramType.DI.name());
            node.setOwner(this.project.getOpUser());
            node.setStartRightNow(false);
            node.setPauseSchedule(false);
            NodeIo output = new NodeIo();
            output.setData(NodeUtils.getDefaultNodeOutput(workflow, node));
            output.setParseType(1);
            node.setOutputs(Arrays.asList(output));

            String defaultDatasource = this.properties.getProperty(
                Constants.CONVERTER_TARGET_ENGINE_DATASOURCE_NAME, DEFAULT_DATASOURCE);
            String defaultDatasourceType = this.properties.getProperty(
                Constants.CONVERTER_TARGET_ENGINE_DATASOURCE_TYPE, DEFAULT_DATASOURCE_TYPE);
            CalcEngineType engineType = CalcEngineType.valueOf(this.properties.getProperty(
                Constants.CONVERTER_TARGET_ENGINE_TYPE, CalcEngineType.ODPS.name()));
            DICode code = DICode.parseDiCode(actionXml, engineType, defaultDatasource, defaultDatasourceType);
            List<DwDatasource> datasourceList = DiCodeUtils.processSqoopDatasource(code);
            ListUtils.emptyIfNull(datasourceList).stream().forEach(ds -> {
                ds.setProjectRef(project);
                ds.setEnvType(EnvType.PRD.name());
                if (project.getDatasources().stream().noneMatch(d -> d.getName().equals(ds.getName()))) {
                    project.getDatasources().add(ds);
                }
            });

            if (ReportRiskLevel.ERROR.equals(code.getRiskLevel())) {
                node.setType(CodeProgramType.VIRTUAL.name());
                XMLOutputter xmlOutputter = new XMLOutputter();
                String xmlCode = xmlOutputter.outputString(actionXml);
                node.setCode(xmlCode);
                reportException(workflow, node, new RuntimeException(code.getException()),
                    ReportItemType.SQOOP_TO_DW_DI);
            } else {
                node.setCode(code.getCode());
            }
            workflow.getNodes().add(node);
        } catch (Exception e) {
            LOGGER.error("{}", e);
            reportException(workflow, node, e, ReportItemType.SQOOP_TO_DW_DI);
        }
    }

    private void handleHiveError(Element actionXml) {
        String toNode = replaceInvalidChar(actionXml.getAttributeValue("to"));
        if (StringUtils.isNotBlank(toNode)) {
            NodeRelation relation = new NodeRelation();
            relation.nodeFrom = replaceInvalidChar(actionXml.getParentElement().getAttributeValue("name"));
            relation.nodeTo = toNode;
            relations.get().add(relation);
        }
    }

    private void handleHiveOk(Element actionXml) {
        String toNode = replaceInvalidChar(actionXml.getAttributeValue("to"));
        if (StringUtils.isNotBlank(toNode)) {
            NodeRelation relation = new NodeRelation();
            relation.nodeFrom = replaceInvalidChar(actionXml.getParentElement().getAttributeValue("name"));
            relation.nodeTo = toNode;
            relations.get().add(relation);
        }
    }

    private void handleHiveNode(Element actionXml, Node node, Workflow workflow, String workflowPath) {
        try {
            String hiveNodeType = this.properties.getProperty(Constants.CONVERTER_TARGET_SQL_NODE_TYPE_AS,
                CodeProgramType.EMR_HIVE.name());
            node.setType(CodeProgramType.valueOf(hiveNodeType).name());
            node.setOwner(this.project.getOpUser());
            node.setStartRightNow(false);
            node.setPauseSchedule(false);
            NodeIo output = new NodeIo();
            output.setData(NodeUtils.getDefaultNodeOutput(workflow, node));
            output.setParseType(1);
            node.setOutputs(Arrays.asList(output));

            List<Element> children = actionXml.getChildren();
            FakeElEvaluatorContext context = new FakeElEvaluatorContext();
            for (Element element : children) {
                // 处理 script
                if (element.getName().equalsIgnoreCase(OozieConstants.SCRIPT)) {
                    String scriptFile = element.getText().trim();
                    File scriptPath = new File(workflowPath + File.separator + scriptFile);
                    String code = scriptPath.exists() ? FileUtils.readFileToString(scriptPath, "utf-8") : scriptFile;
                    node.setCode(code);
                    node.setCode(EmrCodeUtils.toEmrCode(node));
                    node.setRef(Constants.WORKFLOWS_DIR_PRJ_RELATED +
                        File.separator + workflow.getName() +
                        File.separator + Constants.NODES_DIR +
                        File.separator + node.getName() +
                        File.separator + scriptFile);
                }

                // 处理 param INPUT=/xx/ss
                if (element.getName().equalsIgnoreCase(OozieConstants.PARAM)) {
                    String txt = element.getText().trim();
                    String[] kv = txt.split("=");
                    if (kv.length == 2) {
                        context.setVariable(kv[0], kv[1]);
                    }
                }
            }

            if (StringUtils.isNotBlank(node.getCode())) {
                ELEvaluator elEvaluator = new ELEvaluator(context);
                String code = elEvaluator.evaluate(node.getCode(), String.class);
                node.setCode(code);
            }

            workflow.getNodes().add(node);
        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage());
            reportException(workflow, node, e, ReportItemType.HIVE_SQL_TO_MC_SQL);
        }
    }

    private void addVirtualNode(Element child, Workflow workflow) throws Exception {
        OozieNodeType oozieNodeType = OozieNodeType.getOozieNodeType(child.getName());
        DwNode node = new DwNode();
        node.setIsAutoParse(0);
        node.setName(
            replaceInvalidChar(child.getAttribute("name") == null ? child.getName() : child.getAttributeValue("name")));
        node.setOwner(this.project.getOpUser());
        node.setType(CodeProgramType.VIRTUAL.getName());
        node.setRawNodeType(Optional.ofNullable(getRawNodeType(oozieNodeType)).map(Enum::name)
            .orElse(oozieNodeType.getType()));
        node.setStartRightNow(false);
        node.setPauseSchedule(false);
        node.setCronExpress("day");
        node.setNodeUseType(
            BooleanUtils.isTrue(workflow.getScheduled()) ? NodeUseType.SCHEDULED : NodeUseType.MANUAL_WORKFLOW);
        NodeIo output = new NodeIo();
        output.setData(NodeUtils.getDefaultNodeOutput(workflow, node));
        output.setParseType(1);
        node.setOutputs(Collections.singletonList(output));

        String toNode = replaceInvalidChar(child.getAttributeValue("to"));
        if (StringUtils.isNotBlank(toNode)) {
            NodeRelation relation = new NodeRelation();
            relation.nodeFrom = node.getName();
            relation.nodeTo = toNode;
            relations.get().add(relation);
        }
        workflow.getNodes().add(node);
    }

    private RawNodeType getRawNodeType(OozieNodeType oozieNodeType) {
        switch (oozieNodeType) {
            case JOIN:
                return RawNodeType.OOZIE_JOIN;
            case FORK:
                return RawNodeType.OOZIE_FORK;
            case END:
                return RawNodeType.OOZIE_END;
            case KILL:
                return RawNodeType.OOZIE_KILL;
            case START:
                return RawNodeType.OOZIE_START;
            default:
                return null;
        }
    }

    private static String[] parseDefinition(String str) throws ServiceException {
        try {
            str = str.trim();
            if (!str.contains(":")) {
                str = ":" + str;
            }
            String[] parts = str.split(":");
            String prefix = parts[0];
            parts = parts[1].split("=");
            String name = parts[0];
            parts = parts[1].split("#");
            String klass = parts[0];
            String method = parts[1];
            return new String[] {prefix, name, klass, method};
        } catch (Exception ex) {
            throw new ServiceException(ErrorCode.E0110, str, ex.getMessage(), ex);
        }
    }

    private void extractFunctionsAndConstants(ELEvaluator.Context context, String[] functions, String[] constants)
        throws ServiceException {
        for (String function : functions) {
            String[] parts = parseDefinition(function);
            Method method = findMethod(parts[2], parts[3]);
            context.addFunction(parts[0], parts[1], method);
        }

        for (String constant : constants) {
            String[] parts = parseDefinition(constant);
            Object value = findConstant(parts[2], parts[3]);
            context.setVariable(parts[1], value);
        }
    }

    public static Object findConstant(String className, String constantName) throws ServiceException {
        try {
            Class klass = Thread.currentThread().getContextClassLoader().loadClass(className);
            Field field = klass.getField(constantName);
            if ((field.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC)) != (Modifier.PUBLIC | Modifier.STATIC)) {
                throw new ServiceException(ErrorCode.E0114, className, constantName);
            }
            return field.get(null);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        } catch (NoSuchFieldException ex) {
            throw new ServiceException(ErrorCode.E0115, className, constantName);
        } catch (ClassNotFoundException ex) {
            throw new ServiceException(ErrorCode.E0113, className);
        }
    }

    private Method findMethod(String className, String methodName) throws ServiceException {
        Method method = null;
        try {
            Class klass = Thread.currentThread().getContextClassLoader().loadClass(className);
            for (Method m : klass.getMethods()) {
                if (m.getName().equals(methodName)) {
                    method = m;
                    break;
                }
            }
            if (method == null) {
                throw new ServiceException(ErrorCode.E0111, className, methodName);
            }
            if ((method.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC)) != (Modifier.PUBLIC | Modifier.STATIC)) {
                throw new ServiceException(ErrorCode.E0112, className, methodName);
            }
        } catch (ClassNotFoundException ex) {
            throw new ServiceException(ErrorCode.E0113, className);
        }
        return method;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}