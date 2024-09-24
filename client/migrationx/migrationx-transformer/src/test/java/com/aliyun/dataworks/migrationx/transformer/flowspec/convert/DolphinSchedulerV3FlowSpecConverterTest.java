/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.convert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320.DagDataSchedule;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.DolphinSchedulerV3FlowSpecConverter;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;
import com.aliyun.migrationx.common.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-05
 */
public class DolphinSchedulerV3FlowSpecConverterTest {

    @Test
    public void singleSqlNodeTest() throws IOException {
        //List<Specification<DataWorksWorkflowSpec>> specificationV110List = readFileAndTransform(
        //    "src/test/resources/json/dolphin/singleSqlNode-dolphin.json", SpecVersion.V_1_1_0);
        //Assert.assertEquals(1, specificationV110List.size());
        //Specification<DataWorksWorkflowSpec> specificationV110 = specificationV110List.stream().findFirst().orElse(null);
        //Assert.assertNotNull(specificationV110);
        //DataWorksWorkflowSpec spec = specificationV110.getSpec();
        //Assert.assertNotNull(spec);
        //List<SpecNode> nodeList = spec.getNodes();
        //Assert.assertEquals(3, nodeList.size());
        //List<SpecFlowDepend> flow = spec.getFlow();
        //Assert.assertNotNull(flow);
        //Assert.assertEquals(2, flow.size());
        //
        //specificationV110List.forEach(specification -> System.out.println(SpecUtil.writeToSpec(specification)));

        List<Specification<DataWorksWorkflowSpec>> specificationV120List = readFileAndTransform(
            "src/test/resources/json/dolphin/singleSqlNode-dolphin.json", SpecVersion.V_1_2_0);
        Assert.assertEquals(1, specificationV120List.size());
        Specification<DataWorksWorkflowSpec> specificationV120 = specificationV120List.stream().findFirst().orElse(null);
        Assert.assertNotNull(specificationV120);
        DataWorksWorkflowSpec spec120 = specificationV120.getSpec();
        Assert.assertNotNull(spec120);
        List<SpecWorkflow> workflows = spec120.getWorkflows();
        Assert.assertEquals(1, workflows.size());
        SpecWorkflow workflow = workflows.stream().findFirst().orElse(null);
        Assert.assertNotNull(workflow);
        List<SpecNode> nodeListV120 = workflow.getNodes();
        Assert.assertEquals(3, nodeListV120.size());
        List<SpecFlowDepend> flowV120 = workflow.getDependencies();
        Assert.assertNotNull(flowV120);
        Assert.assertEquals(2, flowV120.size());
        specificationV120List.forEach(specification -> System.out.println(SpecUtil.writeToSpec(specification)));
    }

    @Test
    public void multiSqlNodeTest() throws IOException {
        //List<Specification<DataWorksWorkflowSpec>> specificationListV110 = readFileAndTransform(
        //    "src/test/resources/json/dolphin/multiSqlNode-dolphin.json", SpecVersion.V_1_1_0);
        //Assert.assertEquals(1, specificationListV110.size());
        //Specification<DataWorksWorkflowSpec> specification = specificationListV110.get(0);
        //Assert.assertNotNull(specification);
        //DataWorksWorkflowSpec spec = specification.getSpec();
        //Assert.assertNotNull(spec);
        //List<SpecNode> nodeList = spec.getNodes();
        //Assert.assertTrue(CollectionUtils.isNotEmpty(nodeList));
        //specificationListV110.forEach(s -> System.out.println(SpecUtil.writeToSpec(s)));

        List<Specification<DataWorksWorkflowSpec>> specificationListV120 = readFileAndTransform(
            "src/test/resources/json/dolphin/multiSqlNode-dolphin.json", SpecVersion.V_1_2_0);
        Assert.assertEquals(1, specificationListV120.size());
        List<SpecWorkflow> specWorkflows = Optional.ofNullable(specificationListV120.get(0)).map(Specification::getSpec).map(
            DataWorksWorkflowSpec::getWorkflows).orElse(null);
        Assert.assertTrue(CollectionUtils.isNotEmpty(specWorkflows));
        Assert.assertEquals(1, specWorkflows.size());
        List<SpecNode> nodeList1 = specWorkflows.get(0).getNodes();
        Assert.assertTrue(CollectionUtils.isNotEmpty(nodeList1));
        specificationListV120.forEach(s -> System.out.println(SpecUtil.writeToSpec(s)));
    }

    @Test
    public void sqlPythonNodeTest() throws IOException {
        //List<Specification<DataWorksWorkflowSpec>> specificationListV110 = readFileAndTransform(
        //    "src/test/resources/json/dolphin/sql-python-dolphin.json",
        //    SpecVersion.V_1_1_0);
        //Assert.assertEquals(1, specificationListV110.size());
        //Specification<DataWorksWorkflowSpec> specification = specificationListV110.get(0);
        //Assert.assertNotNull(specification);
        //DataWorksWorkflowSpec spec = specification.getSpec();
        //Assert.assertNotNull(spec);
        //List<SpecNode> nodeList = spec.getNodes();
        //Assert.assertTrue(CollectionUtils.isNotEmpty(nodeList));
        //Assert.assertEquals(2, nodeList.size());
        //specificationListV110.forEach(s -> System.out.println(SpecUtil.writeToSpec(s)));

        List<Specification<DataWorksWorkflowSpec>> specificationListV120 = readFileAndTransform(
            "src/test/resources/json/dolphin/sql-python-dolphin.json",
            SpecVersion.V_1_2_0);
        Assert.assertEquals(1, specificationListV120.size());
        List<SpecWorkflow> specWorkflows = Optional.ofNullable(specificationListV120.get(0)).map(Specification::getSpec).map(
            DataWorksWorkflowSpec::getWorkflows).orElse(null);
        Assert.assertTrue(CollectionUtils.isNotEmpty(specWorkflows));
        Assert.assertEquals(1, specWorkflows.size());
        List<SpecNode> nodeList1 = specWorkflows.get(0).getNodes();
        Assert.assertTrue(CollectionUtils.isNotEmpty(nodeList1));
        Assert.assertEquals(2, nodeList1.size());
        specificationListV120.forEach(s -> System.out.println(SpecUtil.writeToSpec(s)));

    }

    @Test
    public void sqlPythonSparkNodeTest() throws IOException {
        //List<Specification<DataWorksWorkflowSpec>> specificationListV110 = readFileAndTransform(
        //    "src/test/resources/json/dolphin/sql-python-spark-dolphin.json", SpecVersion.V_1_1_0);
        //Assert.assertEquals(1, specificationListV110.size());
        //Specification<DataWorksWorkflowSpec> specification = specificationListV110.get(0);
        //Assert.assertNotNull(specification);
        //DataWorksWorkflowSpec spec = specification.getSpec();
        //Assert.assertNotNull(spec);
        //List<SpecNode> nodeList = spec.getNodes();
        //Assert.assertTrue(CollectionUtils.isNotEmpty(nodeList));
        //Assert.assertEquals(3, nodeList.size());
        //specificationListV110.forEach(s -> System.out.println(SpecUtil.writeToSpec(s)));

        List<Specification<DataWorksWorkflowSpec>> specificationListV120 = readFileAndTransform(
            "src/test/resources/json/dolphin/sql-python-spark-dolphin.json", SpecVersion.V_1_2_0);
        Assert.assertEquals(1, specificationListV120.size());
        List<SpecWorkflow> specWorkflows = Optional.ofNullable(specificationListV120.get(0)).map(Specification::getSpec).map(
            DataWorksWorkflowSpec::getWorkflows).orElse(null);
        Assert.assertTrue(CollectionUtils.isNotEmpty(specWorkflows));
        Assert.assertEquals(1, specWorkflows.size());
        List<SpecNode> nodeList1 = specWorkflows.get(0).getNodes();
        Assert.assertTrue(CollectionUtils.isNotEmpty(nodeList1));
        Assert.assertEquals(3, nodeList1.size());
        specificationListV120.forEach(s -> System.out.println(SpecUtil.writeToSpec(s)));

    }

    @Test
    public void realCaseTest() throws IOException {
        //List<Specification<DataWorksWorkflowSpec>> specificationListV110 = readFileAndTransform("src/test/resources/json/dolphin/real-case1.json",
        //    SpecVersion.V_1_1_0);
        //Assert.assertEquals(1, specificationListV110.size());
        //Specification<DataWorksWorkflowSpec> specification = specificationListV110.get(0);
        //Assert.assertNotNull(specification);
        //DataWorksWorkflowSpec spec = specification.getSpec();
        //Assert.assertNotNull(spec);
        //List<SpecNode> nodeList = spec.getNodes();
        //Assert.assertTrue(CollectionUtils.isNotEmpty(nodeList));
        //specificationListV110.forEach(s -> System.out.println(SpecUtil.writeToSpec(s)));

        List<Specification<DataWorksWorkflowSpec>> specificationListV120 = readFileAndTransform("src/test/resources/json/dolphin/real-case1.json",
            SpecVersion.V_1_2_0);
        Assert.assertEquals(1, specificationListV120.size());
        List<SpecWorkflow> specWorkflows = Optional.ofNullable(specificationListV120.get(0)).map(Specification::getSpec).map(
            DataWorksWorkflowSpec::getWorkflows).orElse(null);
        Assert.assertTrue(CollectionUtils.isNotEmpty(specWorkflows));
        Assert.assertEquals(1, specWorkflows.size());
        List<SpecNode> nodeList1 = specWorkflows.get(0).getNodes();
        Assert.assertTrue(CollectionUtils.isNotEmpty(nodeList1));
        specificationListV120.forEach(s -> System.out.println(SpecUtil.writeToSpec(s)));
    }

    @Test
    public void shellNodeTest() throws IOException {
        //List<Specification<DataWorksWorkflowSpec>> specificationListV110 = readFileAndTransform("src/test/resources/json/dolphin/shell-dolphin
        // .json",
        //    SpecVersion.V_1_1_0);
        //Assert.assertEquals(1, specificationListV110.size());
        //Specification<DataWorksWorkflowSpec> specification = specificationListV110.get(0);
        //Assert.assertNotNull(specification);
        //DataWorksWorkflowSpec spec = specification.getSpec();
        //Assert.assertNotNull(spec);
        //List<SpecNode> nodeList = spec.getNodes();
        //Assert.assertTrue(CollectionUtils.isNotEmpty(nodeList));
        //specificationListV110.forEach(s -> System.out.println(SpecUtil.writeToSpec(s)));

        List<Specification<DataWorksWorkflowSpec>> specificationListV120 = readFileAndTransform("src/test/resources/json/dolphin/shell-dolphin.json",
            SpecVersion.V_1_2_0);
        Assert.assertEquals(1, specificationListV120.size());
        List<SpecWorkflow> specWorkflows = Optional.ofNullable(specificationListV120.get(0)).map(Specification::getSpec).map(
            DataWorksWorkflowSpec::getWorkflows).orElse(null);
        Assert.assertTrue(CollectionUtils.isNotEmpty(specWorkflows));
        Assert.assertEquals(1, specWorkflows.size());
        List<SpecNode> nodeList1 = specWorkflows.get(0).getNodes();
        Assert.assertTrue(CollectionUtils.isNotEmpty(nodeList1));
        specificationListV120.forEach(s -> System.out.println(SpecUtil.writeToSpec(s)));
    }

    @Test
    public void dependentNodeTest() throws IOException {
        //List<Specification<DataWorksWorkflowSpec>> specificationListV110 = readFileAndTransform(
        //    "src/test/resources/json/dolphin/dependent-dolphin.json",
        //    SpecVersion.V_1_1_0);
        //Assert.assertEquals(1, specificationListV110.size());
        //Specification<DataWorksWorkflowSpec> specification = specificationListV110.get(0);
        //Assert.assertNotNull(specification);
        //DataWorksWorkflowSpec spec = specification.getSpec();
        //Assert.assertNotNull(spec);
        //List<SpecNode> nodeList = spec.getNodes();
        //Assert.assertTrue(CollectionUtils.isNotEmpty(nodeList));
        //specificationListV110.forEach(s -> System.out.println(SpecUtil.writeToSpec(s)));

        List<Specification<DataWorksWorkflowSpec>> specificationListV120 = readFileAndTransform(
            "src/test/resources/json/dolphin/dependent-dolphin.json",
            SpecVersion.V_1_2_0);
        Assert.assertEquals(1, specificationListV120.size());
        List<SpecWorkflow> specWorkflows = Optional.ofNullable(specificationListV120.get(0)).map(Specification::getSpec).map(
            DataWorksWorkflowSpec::getWorkflows).orElse(null);
        Assert.assertTrue(CollectionUtils.isNotEmpty(specWorkflows));
        Assert.assertEquals(1, specWorkflows.size());
        List<SpecNode> nodeList1 = specWorkflows.get(0).getNodes();
        Assert.assertTrue(CollectionUtils.isNotEmpty(nodeList1));
        specificationListV120.forEach(s -> System.out.println(SpecUtil.writeToSpec(s)));
    }

    @Test
    public void realCaseDependentNodeTest() throws IOException {
        List<DagDataSchedule> dependentDag = readFile("src/test/resources/json/dolphin/dependent-real-case.json");
        List<DagDataSchedule> realCase1Dag = readFile("src/test/resources/json/dolphin/real-case1.json");
        List<DagDataSchedule> realCase2Dag = readFile("src/test/resources/json/dolphin/real-case2.json");

        DolphinSchedulerV3ConverterContext dolphinSchedulerV3ConverterContext = new DolphinSchedulerV3ConverterContext();
        dolphinSchedulerV3ConverterContext.setDependSpecification(new ArrayList<>());
        for (DagDataSchedule dagDataSchedule : ListUtils.emptyIfNull(realCase1Dag)) {
            DolphinSchedulerV3FlowSpecConverter dolphinSchedulerV3FlowSpecConverter = new DolphinSchedulerV3FlowSpecConverter(dagDataSchedule,
                dolphinSchedulerV3ConverterContext);
            dolphinSchedulerV3FlowSpecConverter.convert();
            dolphinSchedulerV3ConverterContext.getDependSpecification().add(dolphinSchedulerV3FlowSpecConverter.getSpecification());
        }
        for (DagDataSchedule dagDataSchedule : ListUtils.emptyIfNull(realCase2Dag)) {
            DolphinSchedulerV3FlowSpecConverter dolphinSchedulerV3FlowSpecConverter = new DolphinSchedulerV3FlowSpecConverter(dagDataSchedule,
                dolphinSchedulerV3ConverterContext);
            dolphinSchedulerV3FlowSpecConverter.convert();
            dolphinSchedulerV3ConverterContext.getDependSpecification().add(dolphinSchedulerV3FlowSpecConverter.getSpecification());
        }

        for (DagDataSchedule dagDataSchedule : ListUtils.emptyIfNull(dependentDag)) {
            DolphinSchedulerV3FlowSpecConverter dolphinSchedulerV3FlowSpecConverter = new DolphinSchedulerV3FlowSpecConverter(dagDataSchedule,
                dolphinSchedulerV3ConverterContext);
            dolphinSchedulerV3FlowSpecConverter.convert();

            System.out.println(SpecUtil.writeToSpec(dolphinSchedulerV3FlowSpecConverter.getSpecification()));
        }

        Assert.assertTrue(true);
    }

    private List<Specification<DataWorksWorkflowSpec>> readFileAndTransform(String path, SpecVersion specVersion) throws IOException {
        DolphinSchedulerV3ConverterContext context = new DolphinSchedulerV3ConverterContext();
        List<DagDataSchedule> dagDataScheduleList = readFile(path);
        List<Specification<DataWorksWorkflowSpec>> res = new ArrayList<>();
        if (dagDataScheduleList != null) {
            context.setSpecVersion(specVersion.getLabel());
            for (DagDataSchedule dagDataSchedule : dagDataScheduleList) {
                Specification<DataWorksWorkflowSpec> specification = new DolphinSchedulerV3FlowSpecConverter(dagDataSchedule, context).convert();
                res.add(specification);
            }
        }
        return res;
    }

    private List<DagDataSchedule> readFile(String path) throws IOException {
        String fileContent = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
        return JSONUtils.toList(fileContent, DagDataSchedule.class);
    }

}
