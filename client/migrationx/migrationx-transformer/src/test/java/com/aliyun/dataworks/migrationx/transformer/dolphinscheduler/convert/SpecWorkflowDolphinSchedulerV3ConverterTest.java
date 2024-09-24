/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.convert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320.DagDataSchedule;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.FlowSpecDolphinSchedulerV3Converter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.SpecWorkflowDolphinSchedulerV3Converter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import com.aliyun.migrationx.common.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-04
 */
@Slf4j
public class SpecWorkflowDolphinSchedulerV3ConverterTest {

    @Test
    public void SingleSqlNodeTest() {
        FlowSpecConverterContext context = new FlowSpecConverterContext();
        context.setProjectCode(14171851731808L);
        doTest("src/test/resources/json/spec/single-sql-node-spec.json", context);
    }

    @Test
    public void realCaseSingleSqlNodeTest() {
        FlowSpecConverterContext context = new FlowSpecConverterContext();
        context.setProjectCode(14171851731808L);
        doTest("src/test/resources/json/spec/real-case-single-sql-spec.json", context);
    }

    @Test
    public void realCaseSinglePythonNodeTest() {
        FlowSpecConverterContext context = new FlowSpecConverterContext();
        context.setProjectCode(14171851731808L);
        doTest("src/test/resources/json/spec/real-case-single-python-spec.json", context);
    }

    @Test
    public void sqlPythonNodeTest() {
        FlowSpecConverterContext context = new FlowSpecConverterContext();
        context.setDefaultFileResourcePath("file:/dolphinscheduler/default/resources");
        context.setProjectCode(14171851731808L);
        doTest("src/test/resources/json/spec/sql-python-node-spec.json", context);
    }

    @Test
    public void shellNodeTest() {
        FlowSpecConverterContext context = new FlowSpecConverterContext();
        context.setDefaultFileResourcePath("file:/dolphinscheduler/default/resources");
        context.setProjectCode(14171851731808L);
        doTest("src/test/resources/json/spec/shell-node-spec.json", context);
    }

    @Test
    public void sqlPythonSparkNodeTest() {
        FlowSpecConverterContext context = new FlowSpecConverterContext();
        context.setDefaultFileResourcePath("file:/dolphinscheduler/default/resources");
        context.setProjectCode(14171851731808L);
        doTest("src/test/resources/json/spec/sql-python-spark-node-spec.json", context);
    }

    @Test
    public void realCaseShellPythonSqlTest() {
        FlowSpecConverterContext context = new FlowSpecConverterContext();
        context.setDefaultFileResourcePath("file:/dolphinscheduler/default/resources");
        context.setProjectCode(14171851731808L);
        doTest("src/test/resources/json/spec/real-case-shell-python-sql-spec.json", context);
    }

    private void doTest(String filePath, FlowSpecConverterContext context) {
        try {
            String content = FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
            Specification<DataWorksWorkflowSpec> specSpecification = SpecUtil.parseToDomain(content);
            if (Objects.isNull(specSpecification) || Objects.isNull(specSpecification.getSpec())) {
                Assert.fail();
            }
            DataWorksWorkflowSpec spec = specSpecification.getSpec();
            ListUtils.emptyIfNull(spec.getWorkflows()).forEach(workflow -> {
                DagDataSchedule dagDataSchedule = new SpecWorkflowDolphinSchedulerV3Converter(spec, workflow, context).convert();
                System.out.println(JSONUtils.toJsonString(dagDataSchedule));
            });
            if (CollectionUtils.isNotEmpty(spec.getNodes())) {
                DagDataSchedule dagDataSchedule = new FlowSpecDolphinSchedulerV3Converter(spec, context).convert();
                System.out.println(JSONUtils.toJsonString(dagDataSchedule));
            }

        } catch (IOException e) {
            Assert.fail();
        }
    }
}
