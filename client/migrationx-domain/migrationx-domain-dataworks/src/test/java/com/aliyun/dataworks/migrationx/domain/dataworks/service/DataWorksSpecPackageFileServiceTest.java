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

package com.aliyun.dataworks.migrationx.domain.dataworks.service;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DataWorksPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Datasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwProject;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Workflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowType;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.impl.DataWorksSpecPackageFileService;

import com.aliyun.migrationx.common.context.TransformerContext;
import com.aliyun.migrationx.common.metrics.enums.CollectorType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/5/23
 */
public class DataWorksSpecPackageFileServiceTest {
    @Test
    public void testWrite() throws Exception {
        DataWorksSpecPackageFileService service = new DataWorksSpecPackageFileService();
        service.setLocale(Locale.SIMPLIFIED_CHINESE);
        File targetFile = new File(new File(DataWorksSpecPackageFileServiceTest.class.getClassLoader().getResource("").getFile()), "test.zip");

        // foreach
        DwNode foreach = new DwNode();
        foreach.setName("test1");
        foreach.setType(CodeProgramType.CONTROLLER_TRAVERSE.getName());
        foreach.setNodeUseType(NodeUseType.SCHEDULED);
        DwNode start = new DwNode();
        start.setType(CodeProgramType.CONTROLLER_TRAVERSE_START.getName());
        start.setName("start");
        start.setNodeUseType(NodeUseType.SCHEDULED);

        DwNode sql = new DwNode();
        sql.setName("sql");
        sql.setType(CodeProgramType.ODPS_SQL.getName());
        sql.setNodeUseType(NodeUseType.SCHEDULED);

        DwNode end = new DwNode();
        end.setName("end");
        end.setType(CodeProgramType.CONTROLLER_TRAVERSE_END.getName());
        end.setNodeUseType(NodeUseType.SCHEDULED);
        foreach.setInnerNodes(Arrays.asList(start, sql, end));

        // node1
        DwNode node1 = new DwNode();
        node1.setName("test1");
        node1.setType("ODPS_SQL");
        node1.setNodeUseType(NodeUseType.SCHEDULED);
        node1.setFolder("业务流程/test_flow1/MaxCompute/数据开发");

        DataWorksPackage dwPackage = new DataWorksPackage();
        dwPackage.setPackageFile(targetFile);

        DwProject dwPrj = new DwProject();
        dwPrj.setName("test");

        Workflow workflow1 = new Workflow();
        workflow1.setName("test_flow1");
        workflow1.setType(WorkflowType.BUSINESS);
        workflow1.setNodes(Arrays.asList(node1, foreach));
        dwPrj.setWorkflows(Collections.singletonList(workflow1));

        Datasource ds = new Datasource();
        ds.setName("ds_test");
        ds.setConnection("{}");
        ds.setType("mysql");
        ds.setSubType("mysql");
        dwPrj.setDatasources(Collections.singletonList(ds));
        dwPackage.setDwProject(dwPrj);
        service.write(dwPackage, targetFile);
        Assert.assertTrue(targetFile.exists());
    }
}
