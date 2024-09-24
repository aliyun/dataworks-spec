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

package com.aliyun.migrationx.common.metrics;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.aliyun.migrationx.common.metrics.enums.CollectorType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
public class DolphinMetricsCollector implements MetricsCollector {
    private final static Gson GSON = new Gson();
    public static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final static Gson PrettyGson = new GsonBuilder()
            .setDateFormat(DATE_FORMAT)
            .setPrettyPrinting()
            .create();

    private final List<Metrics> metricsList = new ArrayList<>();
    private int totalTasks;
    private final Map<String, List<Metrics>> workflowNameMap = new HashMap<>();
    private final Set<Metrics> successDolphinSchedulerTasks = new HashSet<>();
    private final Set<Metrics> failedDolphinSchedulerTasks = new HashSet<>();
    private final Set<Metrics> skippedDolphinSchedulerTasks = new HashSet<>();
    private final Set<Metrics> tempDolphinSchedulerTasks = new HashSet<>();

    private String transformerType;
    private Date startTime;

    private Consumer<Metrics> defaultMetricsConsumer = metrics -> metricsLogger.warn("{}", GSON.toJson(metrics));

    public DolphinMetricsCollector() {
        startTime = new Date();
    }

    @Override
    public void setMetricsConsumer(Consumer<Metrics> consumer) {
        this.defaultMetricsConsumer = consumer;
    }

    @Override
    public void setTransformerType(String type) {
        this.transformerType = type;
    }

    @Override
    public void setTotalTasks(int total) {
        this.totalTasks = total;
    }

    @Override
    public synchronized void markSuccessMiddleProcess(Metrics metrics) {
        metricsList.add(metrics);
        List<Metrics> metricsList = workflowNameMap.get(metrics.getWorkflowName());
        if (metricsList == null) {
            workflowNameMap.put(metrics.getWorkflowName(), new ArrayList<>());
        }
        workflowNameMap.get(metrics.getWorkflowName()).add(metrics);
    }

    @Override
    public synchronized void markFailedMiddleProcess(Metrics metrics) {
        failedDolphinSchedulerTasks.add(metrics);
    }

    @Override
    public void markSuccessSpecProcess(String workflowName, String nodeName) {
        Metrics metrics = new Metrics();
        metrics.setWorkflowName(workflowName);
        metrics.setDwName(nodeName);
        markSuccessSpecProcess(metrics);
    }

    @Override
    public synchronized void markSuccessSpecProcess(Metrics tmp) {
        markSuccessSpecProcess(tmp, defaultMetricsConsumer);
    }

    @Override
    public synchronized void markSuccessSpecProcess(Metrics tmp, Consumer<Metrics> c) {
        Metrics metrics = findMetrics(tmp);
        if (metrics == null) {
            log.error("print metrics error, workflowName {}, nodeName {}", tmp.getWorkflowName(), tmp.getDwName());
            return;
        }
        metrics.setTimestamp(System.currentTimeMillis());
        successDolphinSchedulerTasks.add(metrics);
        c.accept(metrics);
    }

    @Override
    public void markFailedSpecProcess(String workflowName, String nodeName) {
        Metrics metrics = new Metrics();
        metrics.setWorkflowName(workflowName);
        metrics.setDwName(nodeName);
        markFailedSpecProcess(metrics);
    }

    @Override
    public void markFailedSpecProcess(Metrics tmp) {
        Metrics metrics = findMetrics(tmp);
        if (metrics == null) {
            log.error("print metrics error, workflowName {}, nodeName {}", tmp.getWorkflowName(), tmp.getDwName());
            return;
        }
        metrics.setTimestamp(System.currentTimeMillis());
        failedDolphinSchedulerTasks.add(metrics);
    }

    @Override
    public void markSkippedProcess(Metrics metrics) {
        metrics.setTimestamp(System.currentTimeMillis());
        skippedDolphinSchedulerTasks.add(metrics);
    }

    @Override
    public void markTempSpecProcess(String workflowName, String nodeName) {
        Metrics metrics = new Metrics();
        metrics.setWorkflowName(workflowName);
        metrics.setDwName(nodeName);
        markTempSpecProcess(metrics);
    }

    @Override
    public void markTempSpecProcess(Metrics tmp) {
        Metrics metrics = findMetrics(tmp);
        if (metrics == null) {
            log.error("print metrics error, workflowName {}, nodeName {}", tmp.getWorkflowName(), tmp.getDwName());
            return;
        }
        metrics.setTimestamp(System.currentTimeMillis());
        tempDolphinSchedulerTasks.add(metrics);
    }

    private Metrics findMetrics(Metrics tmp) {
        String workflowName = tmp.getWorkflowName();
        String nodeName = tmp.getDwName();
        List<Metrics> metricsList = workflowNameMap.get(workflowName);
        if (metricsList == null) {
            throw new RuntimeException("can not find metrics by " + workflowName);
        }
        Metrics metrics = metricsList.stream().filter(m -> m.getDwName().equals(nodeName))
                .findAny().orElse(null);
        return metrics;
    }

    @Override
    public void finishCollector() {
        finishCollector((summary) -> summaryLogger.warn("{}", PrettyGson.toJson(summary)));
    }

    @Override
    public void finishCollector(Consumer<Summary> c) {
        Summary summary = new Summary();
        summary.setStartTime(startTime);
        summary.setEndTime(new Date());
        summary.setTransformerType(transformerType);
        summary.setType(CollectorType.DolphinScheduler.name());
        summary.setSuccess(toSummary(successDolphinSchedulerTasks));
        summary.setFailed(toSummary(failedDolphinSchedulerTasks));
        summary.setSkipped(toSummary(skippedDolphinSchedulerTasks));
        summary.setTemp(toSummary(tempDolphinSchedulerTasks));
        summary.setTotalSuccess(successDolphinSchedulerTasks.size());
        summary.setTotalFailed(failedDolphinSchedulerTasks.size());
        summary.setTotalSkipped(skippedDolphinSchedulerTasks.size());
        summary.setTotalTemp(tempDolphinSchedulerTasks.size());
        c.accept(summary);
    }

    private List<Project> toSummary(Set<Metrics> metricsList) {
        Map<Long, Project> projectMap = new HashMap<>();
        Map<Long, Process> processMap = new HashMap<>();
        Map<Long, Task> taskMap = new HashMap<>();
        for (Metrics m : CollectionUtils.emptyIfNull(metricsList)) {
            DolphinMetrics dolphinMetrics = (DolphinMetrics) m;
            Project project = projectMap.get(dolphinMetrics.getProjectCode());
            if (project == null) {
                project = new Project();
                project.setProjectName(dolphinMetrics.getProjectName());
                project.setProjectCode(dolphinMetrics.getProjectCode());
                projectMap.put(dolphinMetrics.getProjectCode(), project);
            }
            Process process = processMap.get(dolphinMetrics.getProcessCode());
            if (process == null) {
                process = new Process();
                process.setProcessCode(dolphinMetrics.getProcessCode());
                process.setProcessName(dolphinMetrics.getProcessName());
                processMap.put(dolphinMetrics.getProcessCode(), process);
                project.getProcesses().add(process);
            }
            Task task = taskMap.get(dolphinMetrics.getTaskCode());
            if (task == null) {
                task = new Task();
                task.setTaskCode(dolphinMetrics.getTaskCode());
                task.setTaskName(dolphinMetrics.getTaskName());
                taskMap.put(dolphinMetrics.getTaskCode(), task);
                process.getTasks().add(task);
            }
        }
        return new ArrayList<>(projectMap.values());
    }

    @Override
    public Progress progress() {
        Progress rate = new Progress();
        rate.setTotal(totalTasks);
        AtomicInteger counter = new AtomicInteger(0);
        workflowNameMap.values().forEach(s -> s.forEach(ss -> counter.incrementAndGet()));
        rate.setMiddleSuccess(counter.get());
        rate.setSuccess(successDolphinSchedulerTasks.size());
        rate.setFailed(failedDolphinSchedulerTasks.size());
        rate.setSkipped(skippedDolphinSchedulerTasks.size());
        return rate;
    }

    @Data
    public static class Summary {
        private String type;
        private Date startTime;
        private Date endTime;
        private String transformerType;
        private int totalSuccess;
        private int totalFailed;
        private int totalSkipped;
        private int totalTemp;
        private List<Project> success;
        private List<Project> failed;
        private List<Project> skipped;
        private List<Project> temp;
    }

    @Data
    public static class Project {
        private String projectName;
        private Long projectCode;
        private List<Process> processes = new ArrayList<>();
    }

    @Data
    public static class Process {
        private String processName;
        private Long processCode;
        private List<Task> tasks = new ArrayList<>();
    }

    @Data
    public static class Task {
        private String taskName;
        private Long taskCode;
    }
}
