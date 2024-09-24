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

package com.aliyun.dataworks.migrationx.transformer.core.translator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.OdpsSparkCode;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwResource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Resource;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.ResourceUtils;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItem;
import com.aliyun.dataworks.migrationx.transformer.core.report.Reportable;
import com.aliyun.dataworks.migrationx.transformer.core.spark.command.SparkSubmitCommandBuilder;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import com.google.common.base.Joiner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * open source spark submit task to ODPS_SPARK
 *
 * @author sam.liux
 * @date 2019/07/12
 */
public class SparkSubmitTranslator implements Reportable, NodePropertyTranslator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SparkSubmitTranslator.class);

    public static Pattern SPARK_SUBMIT_PATTERN = Pattern.compile("[\\s|\\n|\\t]*spark-submit.*");

    private List<ReportItem> reportItems = new ArrayList<>();

    @Override
    public List<ReportItem> getReport() {
        return reportItems;
    }

    @Override
    public boolean match(DwWorkflow workflow, DwNode node) {
        if (StringUtils.isBlank(node.getCode())) {
            return false;
        }

        Matcher matcher = SPARK_SUBMIT_PATTERN.matcher(node.getCode());
        boolean matched = matcher.find();
        LOGGER.debug("pattern: {}, matched: {}, code: {}", SPARK_SUBMIT_PATTERN, matched, node.getCode());
        if (!matched) {
            return false;
        }

        return true;
    }

    @Override
    public boolean translate(DwWorkflow workflow, DwNode node) {
        if (!match(workflow, node)) {
            return false;
        }

        CodeProgramType defaultNodeType = CodeProgramType.valueOf(node.getType());
        LOGGER.info("node type: {}", defaultNodeType);
        switch (defaultNodeType) {
            case ODPS_SPARK: {
                convertToOdpsSpark(workflow, node);
                break;
            }
            case EMR_SPARK:
            case CDH_SPARK: {
                convertSpark(workflow, node, defaultNodeType.getCalcEngineType().name());
                node.setType(defaultNodeType.name());
                break;
            }
        }
        return true;
    }

    private void convertSpark(DwWorkflow workflow, DwNode node, String calcEngineType) {
        SparkSubmitCommandBuilder sparkSubmitCommandBuilder = getSparkSubmitCommandBuilder(node);
        if (sparkSubmitCommandBuilder == null) {
            return;
        }

        List<String> appResources = handleResources(workflow, Arrays.asList(sparkSubmitCommandBuilder.getAppResource()),
            calcEngineType);
        List<String> jars = handleResources(workflow, sparkSubmitCommandBuilder.getJars(), calcEngineType)
            .stream().filter(str -> str.endsWith(ResourceUtils.FILE_EXT_JAR)).collect(Collectors.toList());
        List<String> assistFiles = handleResources(workflow, sparkSubmitCommandBuilder.getFiles(), calcEngineType)
            .stream().filter(str -> str.split("\\.").length == 1).collect(Collectors.toList());
        List<String> pyFiles = handleResources(workflow, sparkSubmitCommandBuilder.getPyFiles(), calcEngineType)
            .stream().filter(str -> str.endsWith(ResourceUtils.FILE_EXT_PY)).collect(Collectors.toList());
        List<String> assistArchives = handleResources(workflow, sparkSubmitCommandBuilder.getFiles(), calcEngineType)
            .stream().filter(str ->
                str.split("\\.").length == 2 &&
                    ResourceUtils.FILE_EXT_ARCHIVE.contains(str.split("\\.")[1].toLowerCase()))
            .collect(Collectors.toList());
        List<String> referenceResources = new ArrayList<>();
        referenceResources.addAll(appResources);
        referenceResources.addAll(jars);
        referenceResources.addAll(assistFiles);
        referenceResources.addAll(pyFiles);
        referenceResources.addAll(assistArchives);
        String resReferenceCode = referenceResources.stream()
            .map(res -> "##@resource_reference{\"" + res + "\"}").collect(Collectors.joining("\n"));
        referenceResources.stream().forEach(res -> {
            String code = Arrays.stream(StringUtils.split(node.getCode(), " "))
                .map(arg -> arg.contains(res) ? res : arg)
                .collect(Collectors.joining(" "));
            node.setCode(code);
        });
        node.setCode(Joiner.on("\n").join(resReferenceCode, node.getCode()).trim());
    }

    private void convertToOdpsSpark(DwWorkflow workflow, DwNode node) {
        SparkSubmitCommandBuilder sparkSubmitCommandBuilder = getSparkSubmitCommandBuilder(node);

        String calcEngineType = "odps";
        List<String> appResources = handleResources(workflow, Arrays.asList(sparkSubmitCommandBuilder.getAppResource()),
            calcEngineType);
        String main = ListUtils.emptyIfNull(appResources).stream().findFirst().orElse("");
        List<String> jars = handleResources(workflow, sparkSubmitCommandBuilder.getJars(), calcEngineType)
            .stream().filter(str -> str.endsWith(ResourceUtils.FILE_EXT_JAR)).collect(Collectors.toList());
        List<String> assistFiles = handleResources(workflow, sparkSubmitCommandBuilder.getFiles(), calcEngineType)
            .stream().filter(str -> str.split("\\.").length == 1).collect(Collectors.toList());
        List<String> pyFiles = handleResources(workflow, sparkSubmitCommandBuilder.getPyFiles(), calcEngineType)
            .stream().filter(str -> str.endsWith(ResourceUtils.FILE_EXT_PY)).collect(Collectors.toList());
        List<String> assistArchives = handleResources(workflow, sparkSubmitCommandBuilder.getFiles(), calcEngineType)
            .stream().filter(str ->
                str.split("\\.").length == 2 &&
                    ResourceUtils.FILE_EXT_ARCHIVE.contains(str.split("\\.")[1].toLowerCase()))
            .collect(Collectors.toList());

        OdpsSparkCode odpsSparkCode = new OdpsSparkCode();
        List<String> referenceResources = new ArrayList<>();
        referenceResources.add(main);
        referenceResources.addAll(handleResources(workflow, sparkSubmitCommandBuilder.getJars(), calcEngineType));
        odpsSparkCode.setResourceReferences(referenceResources);
        OdpsSparkCode.CodeJson sparkJson = new OdpsSparkCode.CodeJson();
        sparkJson.setArgs(Joiner.on(" ").join(sparkSubmitCommandBuilder.getAppArgs()));
        sparkJson.setAssistFiles(assistFiles);
        sparkJson.setAssistJars(jars);
        sparkJson.setAssistPys(pyFiles);
        sparkJson.setAssistArchives(assistArchives);
        sparkJson.setArchivesName(assistArchives);
        sparkJson.setMainClass(sparkSubmitCommandBuilder.getMainClass());
        if (main.endsWith(ResourceUtils.FILE_EXT_JAR)) {
            sparkJson.setMainJar(main);
            sparkJson.setLanguage(OdpsSparkCode.SPARK_LANGUAGE_JAVA);
        }

        if (main.endsWith(ResourceUtils.FILE_EXT_PY)) {
            sparkJson.setMainPy(main);
            sparkJson.setLanguage(OdpsSparkCode.SPARK_LANGUAGE_PY);
        }

        sparkSubmitCommandBuilder.getConf().put("spark.hadoop.odps.task.major.version", "cupid_v2");
        sparkJson.setConfigs(
            sparkSubmitCommandBuilder.getConf().entrySet()
                .stream()
                .map(ent -> Joiner.on("=").join(ent.getKey(), ent.getValue()))
                .collect(Collectors.toList())
        );
        odpsSparkCode.setSparkJson(sparkJson);

        node.setCode(odpsSparkCode.toString());
        node.setType(CodeProgramType.ODPS_SPARK.name());
    }

    private SparkSubmitCommandBuilder getSparkSubmitCommandBuilder(DwNode node) {
        List<String> args = new ArrayList<>(
            Arrays.asList(node.getCode().split(" "))
                .stream()
                .filter(str -> str.trim().length() > 0)
                .map(String::trim)
                .collect(Collectors.toList())
        );

        if (!args.isEmpty() && SPARK_SUBMIT_PATTERN.matcher(args.get(0)).find()) {
            args = args.subList(1, args.size());
        }
        try {
            SparkSubmitCommandBuilder sparkSubmitCommandBuilder = new SparkSubmitCommandBuilder(args);
            return sparkSubmitCommandBuilder;
        } catch (Exception e) {
            LOGGER.warn("code: {}, exception: ", node.getCode(), e);
        }
        return null;
    }

    private List<String> handleResources(DwWorkflow workflow, List<String> files, String calcEngineType) {
        if (workflow == null || CollectionUtils.isEmpty(files)) {
            return Collections.emptyList();
        }

        List<Resource> resources = CollectionUtils.isEmpty(workflow.getResources()) ?
            new ArrayList<>() : workflow.getResources();

        List<String> resourceNames = resources.stream().map(Resource::getName).collect(Collectors.toList());
        List<String> fileNames = files.stream().map(file -> new File(file).getName()).collect(Collectors.toList());
        fileNames.stream()
            .filter(fileName -> !resourceNames.contains(fileName))
            .forEach(fileName -> addPlaceholderResource(workflow, fileName, calcEngineType));
        return fileNames;
    }

    private void addPlaceholderResource(DwWorkflow workflow, String fileName, String calcEngineType) {
        DwResource resource = new DwResource();
        resource.setName(fileName);
        resource.setType(ResourceUtils.getFileResourceType(fileName, calcEngineType));
        resource.setOdps(true);

        String filePath = ResourceUtils.getPlaceholderFile(fileName);
        try {
            ClassPathResource classPathResource = new ClassPathResource(filePath);
            resource.setLocalPath(classPathResource.getFile().getAbsolutePath());
            resource.setWorkflowRef(workflow);
            workflow.getResources().add(resource);
        } catch (Exception e) {
            throw new BizException(ErrorCode.TRANSLATE_NODE_ERROR, e);
        }
    }

}
