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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.aliyun.migrationx.common.utils.GsonUtils;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.emr.model.v20160408.DescribeFlowCategoryRequest;
import com.aliyuncs.emr.model.v20160408.DescribeFlowCategoryResponse;
import com.aliyuncs.emr.model.v20160408.DescribeFlowJobRequest;
import com.aliyuncs.emr.model.v20160408.DescribeFlowJobResponse;
import com.aliyuncs.emr.model.v20160408.DescribeFlowRequest;
import com.aliyuncs.emr.model.v20160408.DescribeFlowResponse;
import com.aliyuncs.emr.model.v20160408.ListFlowJobRequest;
import com.aliyuncs.emr.model.v20160408.ListFlowJobResponse;
import com.aliyuncs.emr.model.v20160408.ListFlowProjectRequest;
import com.aliyuncs.emr.model.v20160408.ListFlowProjectResponse;
import com.aliyuncs.emr.model.v20160408.ListFlowProjectResponse.Project;
import com.aliyuncs.emr.model.v20160408.ListFlowRequest;
import com.aliyuncs.emr.model.v20160408.ListFlowResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * @author sam.liux
 * @date 2019/06/27
 */
public class AliyunEmrService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AliyunEmrService.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final String FLOW_DETAIL_EXT = ".detail";
    public static final String JSON_FILE_EXT = ".json";
    public static final String FLOW_DIR_NAME = "flow";
    public static final String JOB_DIR_NAME = "job";
    public static final String EMR_POP_PRODUCT = "emr";
    public static final String FILE_ENCODE = "utf-8";
    public static final String FILE_PACKAGE_INFO = ".package_info.txt";

    private final String accessId;
    private final String accessKey;
    private final String endpoint;
    private final String regionId;
    private DefaultAcsClient client;
    private final Map<String, DescribeFlowCategoryResponse> flowCategoryResponseLocalCache = new HashMap<>();

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
            DefaultProfile.addEndpoint(regionId, EMR_POP_PRODUCT, endpoint);
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

        int totalCount = 0;
        while (true) {
            listFlowProjectRequest.setPageNumber(pageNumber);

            ListFlowProjectResponse response = getDefaultAcsClient().getAcsResponse(listFlowProjectRequest);
            for (ListFlowProjectResponse.Project project : response.getProjects()) {
                if (CollectionUtils.isNotEmpty(request.getProjects())
                    && !request.getProjects().contains(project.getName())) {
                    continue;
                }

                List<String> projectFlowList = ListUtils.emptyIfNull(MapUtils.emptyIfNull(request.getProjectFlowList()).get(project.getName()));
                LOGGER.info("exporting project: {}, projectFlowList: {}", project.getName(), projectFlowList);

                File projectFolder = new File(folder.getAbsolutePath() + File.separator + project.getName());
                if (!projectFolder.exists()) {
                    boolean res = projectFolder.mkdirs();
                    LOGGER.info("mkdir project folder: {} {}", projectFolder, res);
                }

                String projectJson = gson.toJson(project);
                File projectFile = new File(projectFolder.getAbsolutePath() +
                    File.separator + project.getName() + JSON_FILE_EXT);
                FileUtils.write(projectFile, projectJson, Charset.forName(FILE_ENCODE));
                dumpProjectJobs(project, projectFolder.getAbsolutePath());
                dumpProjectFlows(project, projectFolder.getAbsolutePath(), projectFlowList, request.getFolderFilter());
            }

            totalCount += response.getProjects().size();
            pageNumber++;
            if (totalCount >= response.getTotal()) {
                break;
            }
        }
        writePackageInfo(folder);
    }

    public void dumpProjectFlows(Project project, String absolutePath, List<String> projectFlowList, String folderFilter)
        throws ClientException, IOException {
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
        request.setProjectId(project.getId());

        while (true) {
            request.setPageNumber(pageNumber);
            ListFlowResponse response = getDefaultAcsClient().getAcsResponse(request);
            File jobFolder = new File(projectFolder.getAbsolutePath() + File.separator + FLOW_DIR_NAME);
            if (!jobFolder.exists()) {
                jobFolder.mkdirs();
            }

            for (ListFlowResponse.FlowItem flow : response.getFlow()) {
                if (CollectionUtils.isNotEmpty(projectFlowList) && !projectFlowList.contains(flow.getName())) {
                    LOGGER.info("skip flow name: {} that not in flow list expected to export", flow.getName());
                    continue;
                }

                flow.setCronExpr(CronUtil.cronToDwCron(flow.getCronExpr()));
                String json = gson.toJson(flow);
                File flowFile = new File(jobFolder.getAbsolutePath() + File.separator + flow.getId() + JSON_FILE_EXT);

                DescribeFlowRequest describeFlowRequest = new DescribeFlowRequest();
                describeFlowRequest.setProjectId(project.getId());
                describeFlowRequest.setId(flow.getId());
                DescribeFlowResponse res = getDefaultAcsClient().getAcsResponse(describeFlowRequest);
                Flow theFlow = new Flow();
                BeanUtils.copyProperties(res, theFlow);
                theFlow.setProject(project);

                List<String> paths = new ArrayList<>();
                supplyFlowCategoryPath(paths, project, res.getCategoryId());
                theFlow.setPath(StringUtils.join(paths, File.separator));
                theFlow.setCronExpr(CronUtil.cronToDwCron(theFlow.getCronExpr()));
                if (StringUtils.isNotBlank(folderFilter) && !StringUtils.startsWith(theFlow.getPath(), folderFilter)) {
                    LOGGER.info("ignore flow that not in path: {}, ignored flow path: {}", folderFilter, theFlow.getPath());
                    continue;
                }

                String flowDetailJson = gson.toJson(theFlow);
                File flowDetailFile = new File(jobFolder.getAbsolutePath() + File.separator + flow.getId() + FLOW_DETAIL_EXT + JSON_FILE_EXT);

                FileUtils.write(flowFile, json, Charset.forName(FILE_ENCODE));
                FileUtils.write(flowDetailFile, flowDetailJson, Charset.forName(FILE_ENCODE));
            }

            pageNumber++;
            total += response.getFlow().size();

            if (total >= response.getTotal()) {
                break;
            }
        }
    }

    public void supplyFlowCategoryPath(List<String> paths, ListFlowProjectResponse.Project project, String categoryId) throws ClientException {
        DescribeFlowCategoryRequest request = new DescribeFlowCategoryRequest();
        request.setProjectId(project.getId());
        request.setId(categoryId);
        DescribeFlowCategoryResponse response = Optional.ofNullable(flowCategoryResponseLocalCache.get(categoryId))
            .orElse(getDefaultAcsClient().getAcsResponse(request));
        if (response == null) {
            LOGGER.warn("response got null by request: {}", GsonUtils.toJsonString(request));
            return;
        }
        flowCategoryResponseLocalCache.put(response.getId(), response);
        paths.add(response.getName());
        if (StringUtils.isBlank(response.getParentId())) {
            Collections.reverse(paths);
            LOGGER.info("got full category path: {}", StringUtils.join(paths, File.separator));
            return;
        }

        supplyFlowCategoryPath(paths, project, response.getParentId());
    }

    public void dumpProjectJobs(ListFlowProjectResponse.Project project, String absolutePath) throws ClientException, IOException {
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
        request.setProjectId(project.getId());
        request.setAdhoc(false);

        while (true) {
            request.setPageNumber(pageNumber);
            ListFlowJobResponse response = getDefaultAcsClient().getAcsResponse(request);
            File jobFolder = new File(projectFolder.getAbsolutePath() + File.separator + JOB_DIR_NAME);
            if (!jobFolder.exists()) {
                boolean res = jobFolder.mkdirs();
                LOGGER.info("mkdir {} {}", jobFolder, res);
            }

            for (ListFlowJobResponse.Job job : response.getJobList()) {
                job.setParamConf(replaceJobDateParam(job.getParamConf()));

                DescribeFlowJobRequest descFlowJobRequest = new DescribeFlowJobRequest();
                descFlowJobRequest.setProjectId(project.getId());
                descFlowJobRequest.setId(job.getId());
                DescribeFlowJobResponse jobResponse = getDefaultAcsClient().getAcsResponse(descFlowJobRequest);
                if (jobResponse != null) {
                    jobResponse.setParamConf(replaceJobDateParam(jobResponse.getParamConf()));
                }

                String json = gson.toJson(jobResponse != null ? jobResponse : job);
                File jobFile = new File(jobFolder.getAbsolutePath() + File.separator + job.getId() + JSON_FILE_EXT);
                FileUtils.write(jobFile, json, Charset.forName(FILE_ENCODE));
            }

            pageNumber++;
            total += response.getJobList().size();

            if (total >= response.getTotal()) {
                break;
            }
        }
    }

    private String replaceJobDateParam(String paramConf) {
        if (StringUtils.isBlank(paramConf)) {
            return paramConf;
        }

        Map<String, String> paramMap = GsonUtils.fromJsonString(paramConf,
            new TypeToken<Map<String, String>>() {}.getType());
        MapUtils.emptyIfNull(paramMap).forEach((key, val) ->
            MapUtils.emptyIfNull(paramMap).put(key, ParamUtil.convertParameterExpression(val)));
        return GsonUtils.toJsonString(paramMap);
    }

    public static List<AliyunEmrProject> load(String fromFolder) throws IOException {
        File folderPath = new File(fromFolder);

        List<AliyunEmrProject> projects = new ArrayList<>();
        for (File projectPath : Objects.requireNonNull(folderPath.listFiles(File::isDirectory))) {
            File projectJsonFile = new File(
                projectPath.getAbsolutePath() + File.separator + projectPath.getName() + JSON_FILE_EXT);
            String prjJson = FileUtils.readFileToString(projectJsonFile, Charset.forName(FILE_ENCODE));

            ListFlowProjectResponse.Project project = gson.fromJson(
                prjJson, new TypeToken<ListFlowProjectResponse.Project>() {}.getType());
            List<ListFlowJobResponse.Job> jobs = loadProjectJobs(projectPath);
            Map<ListFlowResponse.FlowItem, Flow> flows = loadProjectFlows(projectPath);

            AliyunEmrProject aliyunEmrProject = new AliyunEmrProject();
            aliyunEmrProject.setProject(project);
            aliyunEmrProject.setFlows(flows);
            aliyunEmrProject.setJobs(jobs);
            projects.add(aliyunEmrProject);
        }
        return projects;
    }

    private static Map<ListFlowResponse.FlowItem, Flow> loadProjectFlows(File projectPath) throws IOException {
        File flowFolder = new File(projectPath.getAbsolutePath() + File.separator + FLOW_DIR_NAME);
        Map<ListFlowResponse.FlowItem, Flow> flows = new HashMap<>(100);
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
                flowJson, new TypeToken<ListFlowResponse.FlowItem>() {}.getType());

            File jobDetailFile = new File(
                flowJsonFile.getAbsolutePath().replaceAll(JSON_FILE_EXT + "$", "")
                    + FLOW_DETAIL_EXT + JSON_FILE_EXT);
            Flow flowDetail = null;
            if (jobDetailFile.exists()) {
                String jobDetailJson = FileUtils.readFileToString(jobDetailFile, Charset.forName(FILE_ENCODE));
                flowDetail = gson.fromJson(jobDetailJson, new TypeToken<Flow>() {}.getType());
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
