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

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DataWorksPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwProject;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwResource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Resource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.WorkflowType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.tenant.EnvType;
import com.aliyun.dataworks.migrationx.domain.dataworks.standard.service.AbstractPackageFileService;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.aliyun.migrationx.common.utils.ZipUtils;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * DataWorks迁移助手导出包“标准格式”
 *
 * @author 聿剑
 * @date 2023/02/15
 */
@Slf4j
public class DataWorksDwmaPackageFileService extends AbstractPackageFileService<DataWorksPackage> {
    public static final String SRC_WORKFLOW = "workflows";
    public static final String SRC_DATASOURCE = "datasources";
    public static final String SRC_TABLE = "tables";
    public static final String SRC_COMPONENT = "components";
    public static final String SRC_ADHOC_QUERY = "queries";
    public static final String SRC_RESOURCE = "resources";

    @Override
    protected boolean isProjectRoot(File file) {
        return false;
    }

    private XmlMapper newXmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        return xmlMapper;
    }

    @Override
    public void load(DataWorksPackage packageObj) throws Exception {

    }

    @Override
    public DataWorksPackage getPackage() throws Exception {
        return null;
    }

    @Override
    public void write(DataWorksPackage packageModelObject, File targetPackageFile) throws Exception {
        File targetWorkspace = new File(targetPackageFile.getParentFile(), ".tmp");
        FileUtils.forceMkdir(targetWorkspace);

        writeProject(packageModelObject, targetWorkspace);
        writeSrc(packageModelObject, targetWorkspace);
        ZipUtils.zipDir(targetWorkspace, targetPackageFile);
        log.info("zipped file: {}", targetPackageFile);
    }

    private void writeSrc(DataWorksPackage packageModelObject, File targetWorkspace) throws IOException {
        File srcDir = new File(targetWorkspace, "src");
        FileUtils.forceMkdir(srcDir);

        writeSrcWorkflows(packageModelObject, srcDir);
        writeSrcOthers(packageModelObject, srcDir, NodeUseType.AD_HOC, SRC_ADHOC_QUERY);
        writeSrcOthers(packageModelObject, srcDir, NodeUseType.COMPONENT, SRC_COMPONENT);
        writeSrcDatasources(packageModelObject, srcDir);
        writeSrcTables(packageModelObject, srcDir);
    }

    private void writeSrcOthers(DataWorksPackage packageModelObject, File srcDir, NodeUseType nodeUseType, String dirName)
        throws IOException {
        File adHocQueriesDir = new File(srcDir, dirName);
        FileUtils.forceMkdir(adHocQueriesDir);
        ListUtils.emptyIfNull(packageModelObject.getDwProject().getAdHocQueries()).stream()
            .parallel()
            .forEach(n -> {
                try {
                    File file = new File(adHocQueriesDir, n.getUniqueKey() + "_" + n.getName() + ".xml");
                    XmlMapper xmlMapper = newXmlMapper();
                    xmlMapper.writeValue(file, n);
                } catch (IOException e) {
                    throw BizException.of(ErrorCode.PACKAGE_CONVERT_FAILED).with(
                        "write " + dirName + " failed: " + e.getMessage());
                }
            });
    }

    private void writeSrcTables(DataWorksPackage packageModelObject, File srcDir) throws IOException {
        File tableDir = new File(srcDir, SRC_TABLE);
        FileUtils.forceMkdir(tableDir);
        ListUtils.emptyIfNull(packageModelObject.getDwProject().getTables()).stream().parallel().forEach(table -> {
            File tableFile = new File(tableDir, table.getName() + ".xml");
            XmlMapper xmlMapper = newXmlMapper();
            try {
                xmlMapper.writeValue(tableFile, table);
            } catch (IOException e) {
                throw BizException.of(ErrorCode.PACKAGE_CONVERT_FAILED).with("write tables failed: " + e.getMessage());
            }
        });
    }

    private void writeSrcDatasources(DataWorksPackage packageModelObject, File srcDir) throws IOException {
        File datasourceDir = new File(srcDir, SRC_DATASOURCE);
        FileUtils.forceMkdir(datasourceDir);
        ListUtils.emptyIfNull(packageModelObject.getDwProject().getDatasources()).stream().parallel().forEach(ds -> {
            if (ds.getEnvType() == null) {
                ds.setEnvType(EnvType.DEV.name());
            }

            File datasourceFile = new File(datasourceDir, Joiner.on("_").join(ds.getEnvType(), ds.getName()) + ".xml");
            XmlMapper xmlMapper = newXmlMapper();
            try {
                xmlMapper.writeValue(datasourceFile, ds);
            } catch (IOException e) {
                log.error("write datasource file: {}, error: ", datasourceFile, e);
                throw BizException.of(ErrorCode.PACKAGE_CONVERT_FAILED).with(
                    "write datasource failed: " + e.getMessage());
            }
        });
    }

    private void writeSrcWorkflows(DataWorksPackage packageModelObject, File srcDir) throws IOException {
        File workflowDir = new File(srcDir, SRC_WORKFLOW);
        FileUtils.forceMkdir(workflowDir);
        packageModelObject.getDwProject().getWorkflows().forEach(workflow -> {
            try {
                WorkflowType workflowType = workflow.getType();
                File workflowPath = new File(Joiner.on(File.separator).join(
                    workflowDir, Joiner.on("_").join(workflowType.name(), workflow.getName())));
                FileUtils.forceMkdir(workflowPath);
                writeResourceFiles(workflowPath, workflow.getResources());
                File workflowXml = new File(workflowPath, "workflow.xml");
                log.info("write workflow xml file: {}", workflowXml);
                XmlMapper xmlMapper = newXmlMapper();
                xmlMapper.writeValue(workflowXml, workflow);
            } catch (IOException e) {
                log.error("workflow json: {}", GsonUtils.gson.toJson(workflow));
                throw BizException.of(ErrorCode.PACKAGE_CONVERT_FAILED).with(
                    "write workflow.xml failed: " + e.getMessage());
            }
        });
    }

    private void writeResourceFiles(File workflowPath, List<Resource> resources) {
        File resourcesDir = new File(workflowPath, SRC_RESOURCE);
        resources.stream().filter(r -> r instanceof DwResource).forEach(res -> {
            try {
                File resFile = new File(resourcesDir, res.getName());
                FileUtils.copyFile(new File(((DwResource)res).getLocalPath()), resFile);
                res.setPath(Joiner.on(File.separator).join(SRC_RESOURCE, resFile.getName()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void writeProject(DataWorksPackage packageModelObject, File targetWorkspace) throws IOException {
        File projectXmlFile = new File(targetWorkspace, "project.xml");
        XmlMapper xmlMapper = newXmlMapper();
        DwProject projToWrite = new DwProject();
        projToWrite.setName(packageModelObject.getDwProject().getName());
        xmlMapper.writeValue(new FileWriter(projectXmlFile), projToWrite);
    }
}
