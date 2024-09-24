/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.transformer.flowspec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320.DagDataSchedule;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.FlowSpecDolphinSchedulerV3Converter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.SpecWorkflowDolphinSchedulerV3Converter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import com.aliyun.dataworks.migrationx.transformer.flowspec.transformer.AbstractTransformer;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import com.aliyun.migrationx.common.utils.JSONUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-05
 */
@Slf4j
public class FlowSpecDolphinSchedulerV3Transformer extends AbstractTransformer {

    private static final Set<String> SUPPORT_KIND_SET = new HashSet<>();

    private FlowSpecConverterContext context;

    private List<Specification<DataWorksWorkflowSpec>> specificationList = new ArrayList<>();

    private List<DagDataSchedule> dagDataScheduleList = new ArrayList<>();

    static {
        SUPPORT_KIND_SET.add(SpecKind.MANUAL_WORKFLOW.getLabel());
        SUPPORT_KIND_SET.add(SpecKind.CYCLE_WORKFLOW.getLabel());
    }

    public FlowSpecDolphinSchedulerV3Transformer(String configPath, String sourcePath, String targetPath) {
        this.configPath = configPath;
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
    }

    /**
     * transform entry point
     *
     * @throws Exception exception
     */
    @Override
    public void transform() throws Exception {
        read();
        doTransform();
        write();
    }

    private void read() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            context = parseContext();
            String sourceFileContent = FileUtils.readFileToString(new File(sourcePath), StandardCharsets.UTF_8);
            JsonNode jsonNode = objectMapper.readTree(sourceFileContent);
            if (jsonNode.isArray()) {
                log.info("source file is array");
                for (JsonNode node : jsonNode) {
                    specificationList.add(SpecUtil.parseToDomain(objectMapper.writeValueAsString(node)));
                }
            } else {
                specificationList.add(SpecUtil.parseToDomain(objectMapper.writeValueAsString(jsonNode)));
            }

        } catch (IOException e) {
            log.error("read config or source file error", e);
            throw new RuntimeException(e);
        }
    }

    private void write() {
        File targetFile = new File(targetPath);
        if (targetFile.exists() && !targetFile.delete()) {
            log.error("target file exists and can not be deleted, file: {}", targetFile);
            throw new BizException(ErrorCode.NO_PERMISSION, "delete file " + targetFile);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, true))) {
            writer.write(JSONUtils.toJsonString(dagDataScheduleList));
        } catch (IOException e) {
            log.error("write to target file error", e);
            throw new RuntimeException(e);
        }
    }

    private void doTransform() {
        // spec need to convert
        List<DataWorksWorkflowSpec> specList = ListUtils.emptyIfNull(specificationList).stream()
            .filter(this::checkSpecification)
            .map(Specification::getSpec)
            .filter(Objects::nonNull).collect(Collectors.toList());

        dagDataScheduleList.addAll(specList.stream()
            .map(spec -> ListUtils.emptyIfNull(spec.getWorkflows()).stream()
                .map(workflow -> new SpecWorkflowDolphinSchedulerV3Converter(spec, workflow, context).convert())
                .collect(Collectors.toList()))
            .flatMap(List::stream).collect(Collectors.toList()));

        // if spec contain nodes which is not in workflow, then convert these nodes to a default workflow, such as manual workflows
        dagDataScheduleList.addAll(specList.stream()
            .filter(spec -> CollectionUtils.isNotEmpty(spec.getNodes()))
            .map(spec -> new FlowSpecDolphinSchedulerV3Converter(spec, context).convert())
            .collect(Collectors.toList()));
    }

    private boolean checkSpecification(Specification<DataWorksWorkflowSpec> specification) {
        return Objects.nonNull(specification)
            && SUPPORT_KIND_SET.contains(specification.getKind())
            && Objects.nonNull(specification.getSpec());
    }

    private FlowSpecConverterContext parseContext() throws IOException {
        String content = FileUtils.readFileToString(new File(configPath), StandardCharsets.UTF_8);
        FlowSpecDolphinSchedulerV3TransformerConfig config = JSONUtils.parseObject(content, FlowSpecDolphinSchedulerV3TransformerConfig.class);
        return Optional.ofNullable(config).orElseThrow(() -> new BizException(ErrorCode.PARSE_CONFIG_FILE_FAILED, config)).getContext();
    }

}
