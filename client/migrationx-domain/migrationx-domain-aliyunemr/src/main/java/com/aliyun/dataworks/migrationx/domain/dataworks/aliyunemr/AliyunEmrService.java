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

package com.aliyun.dataworks.migrationx.domain.dataworks.aliyunemr;

import com.aliyun.migrationx.common.utils.GsonUtils;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.emr.model.v20160408.*;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author sam.liux
 * @date 2019/06/27
 */
public class AliyunEmrService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AliyunEmrService.class);
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final String FLOW_DETAIL_EXT = ".detail";
    public static final String JSON_FILE_EXT = ".json";
    public static final String FLOW_DIR_NAME = "flow";
    public static final String JOB_DIR_NAME = "job";
    public static final String EMR_POP_PRODUCT = "emr";
    public static final String FILE_ENCODE = "utf-8";
    public static final String FILE_PACKAGE_INFO = ".package_info.txt";

    private String accessId;
    private String accessKey;
    private String endpoint;
    private String regionId;
    private DefaultAcsClient client;

    public AliyunEmrService(String accessId, String accessKey, String endpoint, String regionId) {
        this.accessId = accessId;
        this.accessKey = accessKey;
        this.endpoint = endpoint;
        this.regionId = regionId;
        this.client = null;
    }

    private DefaultAcsClient getDefaultAcsClient() throws ClientException {
        if (client != null) {
            return client;
        }

        synchronized (this) {
            DefaultProfile.addEndpoint(regionId, regionId, EMR_POP_PRODUCT, endpoint);
            IClientProfile profile = DefaultProfile.getProfile(regionId, accessId, accessKey);
            client = new DefaultAcsClient(profile);
            return client;
        }
    }

    private void writePackageInfo(File dir) throws IOException {
        File file = new File(dir, FILE_PACKAGE_INFO);
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.setType(AliyunEmrExporterConstants.EXPORTER_PACKAGE_TYPE);
        packageInfo.setTimestamp(new Date());
        packageInfo.setExporter(AliyunEmrExporterConstants.EXPORTER_NAME);
        FileUtils.writeStringToFile(file, GsonUtils.toJsonString(packageInfo), StandardCharsets.UTF_8);
    }

    public void dump(AliyunEmrExportRequest request) throws ClientException, IOException {
        LOGGER.info("specified export project list: {}", request.getProjects());

        File folder = request.getFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        int pageSize = 20;
        int pageNumber = 1;
        ListFlowProjectRequest listFlowProjectRequest = new ListFlowProjectRequest();
        listFlowProjectRequest.setPageSize(pageSize);
        listFlowProjectRequest.setPageNumber(pageNumber);

        Integer totalCount = 0;
        while (true) {
            listFlowProjectRequest.setPageNumber(pageNumber);

            ListFlowProjectResponse response = getDefaultAcsClient().getAcsResponse(listFlowProjectRequest);
            for (ListFlowProjectResponse.Project project : response.getProjects())
            {
                if (CollectionUtils.isNotEmpty(request.getProjects())
                    && !request.getProjects().contains(project.getName())) {
                    continue;
                }

                LOGGER.info("exporting project: {}", project.getName());
                File projectFolder = new File(folder.getAbsolutePath() + File.separator + project.getName());
                if (!projectFolder.exists()) {
                    projectFolder.mkdirs();
                }
                String projectJson = gson.toJson(project);
                File projectFile = new File(projectFolder.getAbsolutePath() +
                    File.separator + project.getName() + JSON_FILE_EXT);
                FileUtils.write(projectFile, projectJson, Charset.forName(FILE_ENCODE));
                dumpProjectJobs(project.getId(), projectFolder.getAbsolutePath());
                dumpProjectFlows(project.getId(), projectFolder.getAbsolutePath());
            }

            totalCount += response.getProjects().size();
            pageNumber ++;
            if (totalCount >= response.getTotal()) {
                break;
            }
        }
        writePackageInfo(folder);
    }

    public void dumpProjectFlows(String projectId, String absolutePath) throws ClientException, IOException {
        File projectFolder = new File(absolutePath);
        if (!projectFolder.exists()) {
            projectFolder.mkdirs();
        }

        int pageNumber = 1;
        int pageSize = 50;
        int total = 0;
        ListFlowRequest request = new ListFlowRequest();
        request.setPageNumber(pageNumber);
        request.setPageNumber(pageSize);
        request.setProjectId(projectId);

        while (true) {
            request.setPageNumber(pageNumber);
            ListFlowResponse response = getDefaultAcsClient().getAcsResponse(request);
            File jobFolder = new File(projectFolder.getAbsolutePath() + File.separator + FLOW_DIR_NAME);
            if (!jobFolder.exists()) {
                jobFolder.mkdirs();
            }

            for (ListFlowResponse.FlowItem flow : response.getFlow()) {
                String json = gson.toJson(flow);
                File flowFile = new File(jobFolder.getAbsolutePath() + File.separator + flow.getId() + JSON_FILE_EXT);

                DescribeFlowRequest describeFlowRequest = new DescribeFlowRequest();
                describeFlowRequest.setProjectId(projectId);
                describeFlowRequest.setId(flow.getId());
                DescribeFlowResponse res = getDefaultAcsClient().getAcsResponse(describeFlowRequest);
                String flowDetailJson = gson.toJson(res);
                File flowDetailFile = new File(
                    jobFolder.getAbsolutePath() + File.separator + flow.getId() + FLOW_DETAIL_EXT + JSON_FILE_EXT);

                FileUtils.write(flowFile, json, Charset.forName(FILE_ENCODE));
                FileUtils.write(flowDetailFile, flowDetailJson, Charset.forName(FILE_ENCODE));
            }

            pageNumber ++;
            total += response.getFlow().size();

            if (total >= response.getTotal()) {
                break;
            }
        }
    }

    public void dumpProjectJobs(String projectId, String absolutePath) throws ClientException, IOException {
        File projectFolder = new File(absolutePath);
        if (!projectFolder.exists()) {
            projectFolder.mkdirs();
        }

        int pageNumber = 1;
        int pageSize = 50;
        int total = 0;
        ListFlowJobRequest request = new ListFlowJobRequest();
        request.setPageNumber(pageNumber);
        request.setPageNumber(pageSize);
        request.setProjectId(projectId);
        request.setAdhoc(false);

        while (true) {
            request.setPageNumber(pageNumber);
            ListFlowJobResponse response = getDefaultAcsClient().getAcsResponse(request);
            File jobFolder = new File(projectFolder.getAbsolutePath() + File.separator + JOB_DIR_NAME);
            if (!jobFolder.exists()) {
                jobFolder.mkdirs();
            }

            for (ListFlowJobResponse.Job job : response.getJobList()) {
                String json = gson.toJson(job);
                File jobFile = new File(jobFolder.getAbsolutePath() + File.separator + job.getId() + JSON_FILE_EXT);
                FileUtils.write(jobFile, json, Charset.forName(FILE_ENCODE));
            }

            pageNumber ++;
            total += response.getJobList().size();

            if (total >= response.getTotal()) {
                break;
            }
        }
    }

    public static List<AliyunEmrProject> load(String fromFolder) throws IOException {
        File folderPath = new File(fromFolder);

        List<AliyunEmrProject> projects = new ArrayList<>();
        for (File projectPath: folderPath.listFiles(pathname -> pathname.isDirectory())) {
            File projectJsonFile = new File(
                projectPath.getAbsolutePath() + File.separator + projectPath.getName() + JSON_FILE_EXT);
            String prjJson = FileUtils.readFileToString(projectJsonFile, Charset.forName(FILE_ENCODE));

            ListFlowProjectResponse.Project project = gson.fromJson(
                prjJson, new TypeToken<ListFlowProjectResponse.Project>(){}.getType());
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

    private static Map<ListFlowResponse.FlowItem, DescribeFlowResponse> loadProjectFlows(File projectPath) throws IOException {
        File flowFolder = new File(projectPath.getAbsolutePath() + File.separator + FLOW_DIR_NAME);
        Map<ListFlowResponse.FlowItem, DescribeFlowResponse> flows = new HashMap<>(100);
        File[] files = flowFolder.listFiles(f -> f.isFile() && f.getName().endsWith(JSON_FILE_EXT));
        if (files == null) {
            return flows;
        }

        for (File flowJsonFile : files) {
            if (flowJsonFile.getName().contains(FLOW_DETAIL_EXT)) {
                continue;
            }

            String flowJson = FileUtils.readFileToString(flowJsonFile, Charset.forName(FILE_ENCODE));
            ListFlowResponse.FlowItem flowItem = gson.fromJson(
                flowJson, new TypeToken<ListFlowResponse.FlowItem>(){}.getType());

            File jobDetailFile = new File(
                flowJsonFile.getAbsolutePath().replaceAll(JSON_FILE_EXT + "$", "")
                    + FLOW_DETAIL_EXT + JSON_FILE_EXT);
            DescribeFlowResponse flowDetail = null;
            if (jobDetailFile.exists()) {
                String jobDetailJson = FileUtils.readFileToString(jobDetailFile, Charset.forName(FILE_ENCODE));
                flowDetail = gson.fromJson(jobDetailJson, new TypeToken<DescribeFlowResponse>(){}.getType());
            }
            flows.put(flowItem, flowDetail);
        }
        return flows;
    }

    private static List<ListFlowJobResponse.Job> loadProjectJobs(File projectPath) throws IOException {
        File jobFolder = new File(projectPath.getAbsolutePath() + File.separator + JOB_DIR_NAME);
        List<ListFlowJobResponse.Job> jobs = new ArrayList<>(100);
        File[] files = jobFolder.listFiles(f -> f.isFile() && f.getName().endsWith(JSON_FILE_EXT));
        if (files == null) {
            return jobs;
        }

        for (File jobJsonFile : files) {
            String jobJson = FileUtils.readFileToString(jobJsonFile, Charset.forName(FILE_ENCODE));
            ListFlowJobResponse.Job jobItem = gson.fromJson(
                jobJson, new TypeToken<ListFlowJobResponse.Job>() {}.getType());
            jobs.add(jobItem);
        }
        return jobs;
    }
}
