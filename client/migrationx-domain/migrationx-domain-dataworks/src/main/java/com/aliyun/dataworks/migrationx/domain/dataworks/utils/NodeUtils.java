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

package com.aliyun.dataworks.migrationx.domain.dataworks.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Workflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.client.FileNodeCfg;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.client.FileNodeInputOutput;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.IoParseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.RerunMode;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * @author sam.liux
 * @date 2019/05/14
 */
public class NodeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeUtils.class);
    private static Pattern NODE_IO_PREFIX_PATTERN = Pattern.compile(
            "^[\\s\\t]*(?<projectIdentifier>[a-zA-Z][0-9A-Za-z\\-_]{2,})(\\..*|_root$)", Pattern.CASE_INSENSITIVE);

    public static Pattern PROJECT_ROOT_OUTPUT_PATTERN = Pattern.compile(
            "^[\\s\\t]*(?<projectIdentifier>[a-zA-Z][0-9A-Za-z\\-_]{2,})(_root$)", Pattern.CASE_INSENSITIVE);

    public static final Pattern IDE_SYSTEM_IO_PATTERN = Pattern.compile("^\\w+\\.\\d+_out$", Pattern.CASE_INSENSITIVE);
    public static final String IDE_SYSTEM_IO_REWRITE_SUFFIX = "_original";
    public static Pattern IDE_CROSS_PROJECT_CLONE_SYSTEM_OUTPUT_PATTERN = Pattern.compile(
            "^(\\d+\\.)+(?<projectIdentifier>[a-zA-Z][0-9A-Za-z\\-_]{2,})\\..*", Pattern.CASE_INSENSITIVE);

    public static String getDefaultNodeOutput(Workflow workflow, Node node) {
        return String.format("%s_%s.out", workflow.getName(), node.getName());
    }

    public static String getProjectRootOutput(Project project) {
        return String.format("%s_root", project.getName());
    }

    public static boolean matchProjectIdentifierIo(String projectIdentifier, String ioStr) {
        if (StringUtils.isBlank(ioStr) || StringUtils.isBlank(projectIdentifier)) {
            return false;
        }

        Pattern normalIoPattern = Pattern.compile(
                "^[\\s\\t]*" + projectIdentifier + "(?<suffix>\\.|_root$)", Pattern.CASE_INSENSITIVE);
        Pattern crossProjectCloneIoPattern = Pattern.compile(
                "^(\\d+\\.)+" + projectIdentifier + "\\..*", Pattern.CASE_INSENSITIVE);
        Matcher m1 = normalIoPattern.matcher(ioStr);
        Matcher m2 = crossProjectCloneIoPattern.matcher(ioStr);
        return m1.find() || m2.matches();
    }

    public static boolean matchIdeSystemIo(String io) {
        return IDE_SYSTEM_IO_PATTERN.matcher(io).matches();
    }

    public static String normalize(String io) {
        if (!NodeUtils.matchIdeSystemIo(io)) {
            return io;
        }
        io = io + IDE_SYSTEM_IO_REWRITE_SUFFIX;
        return io;
    }

    public static String replaceSystemOutputProjectIdentifier(String io, String identifier) {
        if (!NodeUtils.matchIdeSystemIo(io)) {
            return io;
        }

        String[] tokens = io.split("\\.");
        tokens[0] = identifier;
        return Joiner.on(".").join(tokens);
    }

    public static String replaceOutputProjectIdentifier(String io, String identifier) {
        if (StringUtils.isBlank(io)) {
            return io;
        }

        String[] tokens = io.split("\\.");
        tokens[0] = identifier;
        return Joiner.on(".").join(tokens);
    }

    public static void copyPropertiesFromNodeToFileNodeCfg(Node nodeFrom, FileNodeCfg fileNodeCfgTo) {
        fileNodeCfgTo.setNodeName(nodeFrom.getName());
        fileNodeCfgTo.setCronExpress(nodeFrom.getCronExpress());
        fileNodeCfgTo.setReRunAble(
                nodeFrom.getRerunMode() == null ? RerunMode.ALL_ALLOWED.getValue() : nodeFrom.getRerunMode().getValue());
        fileNodeCfgTo.setIsStop(nodeFrom.getPauseSchedule() != null && nodeFrom.getPauseSchedule() ? 1 : 0);
        fileNodeCfgTo.setStartRightNow(nodeFrom.getStartRightNow());
        fileNodeCfgTo.setOwner(nodeFrom.getOwner());
        fileNodeCfgTo.setParaValue(nodeFrom.getParameter());
        fileNodeCfgTo.setStartEffectDate(nodeFrom.getStartEffectDate());
        fileNodeCfgTo.setEndEffectDate(nodeFrom.getEndEffectDate());
        fileNodeCfgTo.setTaskRerunInterval(nodeFrom.getTaskRerunInterval());
        fileNodeCfgTo.setTaskRerunTime(nodeFrom.getTaskRerunTime());
        fileNodeCfgTo.setDependentType(nodeFrom.getDependentType());
        fileNodeCfgTo.setCycleType(nodeFrom.getCycleType());
        fileNodeCfgTo.setIsAutoParse(nodeFrom.getIsAutoParse() == null ? 0 : nodeFrom.getIsAutoParse());
        fileNodeCfgTo.setDescription(nodeFrom.getDescription());
        fileNodeCfgTo.setPriority(nodeFrom.getPriority());
        fileNodeCfgTo.setMultiinstCheckType(nodeFrom.getMultiInstCheckType());
        fileNodeCfgTo.setDependentDataNode(nodeFrom.getDependentDataNode());
        fileNodeCfgTo.setExtConfig(nodeFrom.getExtraConfig());
        List<FileNodeInputOutput> inputs = new ArrayList<>();
        for (NodeIo nodeIo : nodeFrom.getInputs()) {
            FileNodeInputOutput input = new FileNodeInputOutput();
            input.setStr(nodeIo.getData());
            input.setRefTableName(nodeIo.getRefTableName());
            input.setParseType(nodeIo.getParseType());
            inputs.add(input);
        }

        List<FileNodeInputOutput> outputs = new ArrayList<>();
        for (NodeIo nodeIo : nodeFrom.getOutputs()) {
            FileNodeInputOutput output = new FileNodeInputOutput();
            output.setStr(nodeIo.getData());
            output.setRefTableName(nodeIo.getRefTableName());
            output.setParseType(nodeIo.getParseType());
            outputs.add(output);
        }

        if (inputs.size() > 0) {
            fileNodeCfgTo.setInput(GsonUtils.toJsonString(inputs));
            fileNodeCfgTo.setInputList(inputs);
        }

        if (outputs.size() > 0) {
            fileNodeCfgTo.setOutput(GsonUtils.toJsonString(outputs));
            fileNodeCfgTo.setOutputList(outputs);
        }
    }

    public static void setProjectRootDependencyIfIsolated(Project project, DwWorkflow workflow, Node node) {
        if (CollectionUtils.isEmpty(node.getInputs()) && workflow.getScheduled()) {
            node.setInputs(Arrays.asList(new NodeIo(NodeUtils.getProjectRootOutput(project))));
            ListUtils.emptyIfNull(node.getInputs()).forEach(in -> in.setParseType(1));
        }
    }
    
    public static DwNode toDwNode(Node node) {
        if (node instanceof DwNode) {
            return (DwNode) node;
        }

        DwNode dwNode = new DwNode();
        BeanUtils.copyProperties(node, dwNode);
        return dwNode;
    }

    public static boolean isIdeCrossProjectCloneSystemOutput(Integer type, String data, Integer parseType) {
        return StringUtils.isNotBlank(data) && IDE_CROSS_PROJECT_CLONE_SYSTEM_OUTPUT_PATTERN.matcher(data).matches();
    }

    public static void parseNodeDiResGroupInfo(DwNode dwNode) {
        if (!DefaultNodeTypeUtils.isDiNode(dwNode.getType())) {
            return;
        }

        if (StringUtils.isNotBlank(dwNode.getDiResourceGroup())) {
            return;
        }

        String code = dwNode.getCode();
        if (StringUtils.isBlank(code) && StringUtils.isBlank(dwNode.getExtend())) {
            return;
        }

        try {
            JsonObject extendJson = null;
            JsonObject codeExtendJson = null;
            try {
                extendJson = GsonUtils.fromJsonString(dwNode.getExtend(), JsonObject.class);
                JsonObject codeJson = GsonUtils.gson.fromJson(code, JsonObject.class);
                if (codeJson == null && extendJson == null) {
                    return;
                }
                if (codeJson != null && codeJson.has("extend")) {
                    codeExtendJson = codeJson.get("extend").getAsJsonObject();
                }
            } catch (Exception e) {
                LOGGER.warn("parse extend failed, extend json: {}", dwNode.getExtend(), e);
            }

            if (extendJson != null && extendJson.has("resourceGroup")) {
                String resourceGroup = extendJson.get("resourceGroup").getAsString();
                dwNode.setDiResourceGroup(resourceGroup);
                dwNode.setDiResourceGroupName(resourceGroup);
                return;
            }

            if (codeExtendJson != null && codeExtendJson.has("resourceGroup")) {
                String resourceGroup = codeExtendJson.get("resourceGroup").getAsString();
                dwNode.setDiResourceGroup(resourceGroup);
                dwNode.setDiResourceGroupName(resourceGroup);
                return;
            }
        } catch (Exception e) {
        }
    }

    public static String normalizedFileName(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }

        return name
                .replaceAll(":", "_")
                .replaceAll("-", "_")
                .replaceAll("\\*", "_")
                .replaceAll("\\\\", "_")
                .replaceAll("\\+", "_")
                .replaceAll("%", "_")
                .replaceAll("/", "_")
                .replaceAll("=", "_")
                .replaceAll("&", "_")
                .replaceAll("#", "_")
                .replaceAll("@", "_")
                .replaceAll("!", "_")
                .replaceAll("~", "_")
                .replaceAll("`", "_")
                .replaceAll("\\$", "_")
                .replaceAll("\\^", "_")
                .replaceAll("\\[", "__").replaceAll("]", "__")
                .replaceAll("\\{", "__").replaceAll("}", "__")
                .replaceAll("\\(", "__").replaceAll("\\)", "__");
    }

    public static boolean isEmrNode(String type) {
        CodeProgramType defaultNodeType = DefaultNodeTypeUtils.getTypeByName(type, null);
        if (defaultNodeType == null) {
            return false;
        }

        switch (defaultNodeType) {
            case EMR_SHELL:
            case EMR_HIVE:
            case EMR_SPARK:
            case EMR_SPARK_SQL:
            case EMR_PRESTO:
            case EMR_SPARK_SHELL:
            case EMR_IMPALA:
            case EMR_MR:
            case EMR_HIVE_CLI:
            case EMR_SCOOP:
            case EMR_STREAMING_SQL:
            case EMR_SPARK_STREAMING:
                return true;
            default:
                return false;
        }
    }

    public static String getProjectIdentifierPrefix(String io) {
        String data = StringUtils.defaultIfBlank(io, "");
        if (NODE_IO_PREFIX_PATTERN.matcher(data).matches()) {
            Matcher matcher = NODE_IO_PREFIX_PATTERN.matcher(data);
            if (matcher.find()) {
                return matcher.group("projectIdentifier");
            }
        }
        return null;
    }

    public static boolean matchNodeIoProjectDependency(String io) {
        String data = StringUtils.defaultIfBlank(io, "");
        if (NODE_IO_PREFIX_PATTERN.matcher(data).matches()) {
            return true;
        }
        return false;
    }

    public static String replaceBranchNodeCodeIoPrefix(CodeProgramType type, String code,
            Map<String, String> prefixMapping) {
        if (!CodeProgramType.CONTROLLER_BRANCH.equals(type)) {
            return code;
        }

        if (StringUtils.isBlank(code)) {
            return code;
        }

        try {
            JsonArray json = GsonUtils.fromJsonString(code, JsonArray.class);
            if (json == null) {
                LOGGER.warn("failed to parse branch node code json: {}", code);
                return code;
            }

            boolean replaced = false;
            for (int i = 0; i < json.size(); i++) {
                JsonObject condition = json.get(i).getAsJsonObject();
                if (condition.has("nodeoutput")) {
                    String out = condition.get("nodeoutput").getAsString();
                    String oldPrefix = getProjectIdentifierPrefix(out);
                    String targetPrefix = MapUtils.emptyIfNull(prefixMapping).get(oldPrefix);
                    if (StringUtils.isNotBlank(targetPrefix)) {
                        condition.addProperty("nodeoutput",
                                normalize(replaceOutputProjectIdentifier(out, targetPrefix)));
                        replaced = true;
                        json.set(i, condition);
                    }
                }
            }

            if (replaced) {
                return GsonUtils.defaultGson.toJson(json);
            }

            return code;
        } catch (Exception e) {
            LOGGER.warn("", e);
            return code;
        }
    }

    public static String replaceJoinNodeCodeIoPrefix(CodeProgramType type, String code,
            Map<String, String> prefixMapping) {
        if (!CodeProgramType.CONTROLLER_JOIN.equals(type)) {
            return code;
        }

        if (StringUtils.isBlank(code)) {
            return code;
        }

        try {
            JsonObject json = GsonUtils.fromJsonString(code, JsonObject.class);
            if (json == null) {
                LOGGER.error("failed to parse join node code json: {}", code);
                return code;
            }

            JsonArray branchList = json.has("branchList") ?
                    GsonUtils.fromJsonString(json.get("branchList").getAsString(), JsonArray.class) : new JsonArray();
            boolean replaced = false;
            for (int i = 0; i < branchList.size(); i++) {
                JsonObject condition = branchList.get(i).getAsJsonObject();
                if (condition.has("node")) {
                    String out = condition.get("node").getAsString();
                    String oldPrefix = getProjectIdentifierPrefix(out);
                    String targetPrefix = prefixMapping.get(oldPrefix);
                    if (StringUtils.isNotBlank(targetPrefix)) {
                        condition.addProperty("node", normalize(replaceOutputProjectIdentifier(out, targetPrefix)));
                        replaced = true;
                        branchList.set(i, condition);
                    }
                }
            }
            json.addProperty("branchList", GsonUtils.toJsonString(branchList));
            if (replaced) {
                return GsonUtils.defaultGson.toJson(json);
            }

            return code;
        } catch (Exception e) {
            LOGGER.warn("", e);
            return code;
        }
    }

    public static boolean matchIdeNodeNameOutput(String nodeName, String data, Integer parseType) {
        Pattern pattern = Pattern.compile("^\\w+\\." + nodeName + "$", Pattern.CASE_INSENSITIVE);
        if (IoParseType.MANUAL.getCode().equals(parseType) && pattern.matcher(data).matches()) {
            return true;
        }

        return false;
    }

    public static boolean matchProjectOutput(String data) {
        return PROJECT_ROOT_OUTPUT_PATTERN.matcher(data).matches();
    }
}