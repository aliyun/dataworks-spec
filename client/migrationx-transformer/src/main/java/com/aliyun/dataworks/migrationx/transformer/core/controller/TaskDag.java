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

package com.aliyun.dataworks.migrationx.transformer.core.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.aliyun.dataworks.migrationx.transformer.core.annotation.DependsOn;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItem;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItemType;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportRiskLevel;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务执行流管理
 *
 * @author sam.liux
 * @date 2019/07/03
 */
public class TaskDag {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskDag.class);

    /**
     * 根据 @see TaskStage 分层存放Task
     */
    private Map<TaskStage, Map<String, AtomTask>> tasks = new ConcurrentHashMap<>();

    /**
     * Task name 全局唯一
     */
    private Map<String, FutureTask> futureTaskMap = new ConcurrentHashMap<>();

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            50,
            100,
            5,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadFactoryBuilder().build()
    );

    class AtomTask implements Callable<Task> {
        private Task task;

        public AtomTask(Task task) {
            this.task = task;
        }

        @Override
        public Task call() throws Exception {
            Thread.currentThread().setName(
                    Joiner.on("-").join(
                            task.getName(),
                            Thread.currentThread().getId()
                    )
            );
            List<Task> dependencies = task.getDependencies();
            task.setTaskStatus(TaskStatus.WAITING);
            for (TaskStage taskStage : tasks.keySet()) {
                Map<String, AtomTask> map = tasks.get(taskStage);
                for (Task dep : dependencies) {
                    if (!map.containsKey(dep.getName())) {
                        continue;
                    }

                    AtomTask depTask = map.get(dep.getName());
                    FutureTask futureTask = futureTaskMap.get(depTask.getTask().getName());
                    futureTask.get();
                }
            }

            try {
                task.setTaskStatus(TaskStatus.RUNNING);
                Object res = task.call();
                task.setTaskStatus(TaskStatus.SUCCESS);
                task.setResult(res);
            } catch (Throwable e) {
                task.setTaskStatus(TaskStatus.FAILURE);
                LOGGER.error("run task {} failed: {}", task.getName(), e);
                ReportItem report = new ReportItem();
                report.setException(ExceptionUtils.getStackTrace(e));
                report.setName(task.getName());
                report.setRiskLevel(ReportRiskLevel.ERROR);
                report.setType(ReportItemType.TASK_FAILURE.name());
                report.setMessage(e.getMessage());
                task.getReport().add(report);
            }
            return task;
        }

        public Task getTask() {
            return task;
        }
    }

    public List<Task> getTasks() {
        return tasks.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(AtomTask::getTask)
                .collect(Collectors.toList());
    }

    public <T> T getTask(TaskStage taskStage, String name, Class<T> clazz) {
        Map<String, AtomTask> map = tasks.get(taskStage);
        if (MapUtils.isEmpty(map)) {
            return null;
        }

        AtomTask atomTask = map.get(name);
        Task task = atomTask.getTask();
        return clazz.cast(task);
    }

    public <T> T getTaskResult(String name) throws ExecutionException, InterruptedException {
        FutureTask futureTask = futureTaskMap.get(name);
        if (!futureTask.isDone()) {
            throw new RuntimeException("task " + name + " is still running");
        }

        Task<T> task = (Task<T>) futureTask.get();
        return task.getTemplateParameterType().cast(task.getResult());
    }

    public void registerTask(TaskStage taskStage, Task task) throws Exception {
        if (futureTaskMap.containsKey(task.getName())) {
            throw new Exception("task exists: " + task.getName());
        } else {
            if (!tasks.containsKey(taskStage)) {
                tasks.put(taskStage, new ConcurrentHashMap<>(10));
            }
            task.setStage(taskStage);
            addTask(taskStage, task);
            task.setTaskDag(this);
        }
    }

    private void handleAnnotationDependsOn() {
        ArrayList<TaskStage> stages = new ArrayList<>(Arrays.asList(TaskStage.values()));
        stages.sort(Comparator.reverseOrder());
        stages.stream().forEach(stage -> {
            if (!tasks.containsKey(stage)) {
                return;
            }

            tasks.get(stage).values().stream().map(AtomTask::getTask).forEach(task -> {
                List<Field> fields = new ArrayList<>();
                getFields(fields, task.getClass());
                // task.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    DependsOn anno = field.getDeclaredAnnotation(DependsOn.class);
                    // 有DependsOn注解，且是Task<?>的子类
                    if (anno != null && Task.class.isAssignableFrom(field.getDeclaringClass())) {
                        List<Task> candidateTasks = getTasks().stream()
                                .filter(t -> t.getClass().equals(field.getType()) || t.getClass()
                                        .equals(field.getGenericType()))
                                .collect(Collectors.toList());
                        if (candidateTasks.size() == 1) {
                            injectTaskDepend(task, field, candidateTasks.get(0));
                        }

                        if (candidateTasks.size() > 1) {
                            if (StringUtils.isBlank(anno.qualifier())) {
                                throw new RuntimeException(
                                        "multi candidate task found by DependsOn: " + field.getType().getName() +
                                                ", but without qualifier specified on DependsOn annotation");
                            }
                        }

                        if (anno.types() != null && anno.types().length > 0) {
                            ArrayList<Class<? extends Task<?>>> typesArr = new ArrayList(Arrays.asList(anno.types()));
                            candidateTasks = getTasks().stream()
                                    .filter(t -> typesArr.contains(t.getClass()))
                                    .collect(Collectors.toList());
                            if (!CollectionUtils.isEmpty(candidateTasks)) {
                                Task[] arr = new Task[candidateTasks.size()];
                                candidateTasks.toArray(arr);
                                injectTaskDepend(task, field, arr);
                            }
                        }

                        if (CollectionUtils.isEmpty(candidateTasks)) {
                            throw new RuntimeException(
                                    "no candidate task found by DependsOn: " + field.getType().getName());
                        }
                    }
                }
            });
        });
    }

    /**
     * 递归获取父类的成员
     *
     * @param fields
     * @param clz
     * @return
     */
    private List<Field> getFields(List<Field> fields, Class<?> clz) {
        if (clz.equals(Task.class)) {
            return fields;
        }

        if (Task.class.isAssignableFrom(clz)) {
            fields.addAll(new ArrayList(Arrays.asList(clz.getDeclaredFields())));
            return getFields(fields, clz.getSuperclass());
        }
        return fields;
    }

    private void injectTaskDepend(Task task, Field field, Task... candidateTasks) {
        for (Task candidateTask : candidateTasks) {
            task.dependsOn(candidateTask);
        }

        try {
            if (List.class.isAssignableFrom(field.getType())) {
                field.set(task, new ArrayList<>(Arrays.asList(candidateTasks)));
            } else {
                field.set(task, candidateTasks[0]);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Task<?> getTaskByName(String name) {
        return getTasks().stream()
                .filter(task -> task.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private void addTask(TaskStage taskStage, Task task) {
        AtomTask atomTask = new AtomTask(task);
        FutureTask<Task> futureTask = new FutureTask(atomTask);
        futureTaskMap.put(task.getName(), futureTask);
        tasks.get(taskStage).put(task.getName(), atomTask);
    }

    public void run() throws InterruptedException {
        start();

        join();
    }

    public void start() {
        futureTaskMap.values().forEach(futureTask -> threadPoolExecutor.submit(futureTask));
    }

    public void init() {
        handleTaskStageDepends();
        handleAnnotationDependsOn();
    }

    private void handleTaskStageDepends() {
        // 根据TaskStage挂基本的Stage之间的依赖
        ArrayList<TaskStage> stages = new ArrayList(Arrays.asList(TaskStage.values()));
        stages.sort(Comparator.reverseOrder());
        for (int index = 0; index < stages.size() - 1; index++) {
            int prevIndex = index + 1;
            TaskStage currStage = stages.get(index);
            TaskStage prevStage = stages.get(prevIndex);
            Map<String, AtomTask> curStageTasks = tasks.get(currStage);
            Map<String, AtomTask> prevStageTasks = tasks.get(prevStage);
            if (MapUtils.isEmpty(curStageTasks) || MapUtils.isEmpty(prevStageTasks)) {
                continue;
            }

            prevStageTasks.values().stream()
                    .map(AtomTask::getTask)
                    .forEach(prevTask ->
                            curStageTasks.values().stream()
                                    .map(AtomTask::getTask)
                                    .forEach(curTask -> curTask.dependsOn(prevTask))
                    );
        }
    }

    public void join() throws InterruptedException {
        futureTaskMap.values().forEach(futureTask -> {
            try {
                futureTask.get();
            } catch (InterruptedException e) {
                LOGGER.error("{}", e);
            } catch (ExecutionException e) {
                LOGGER.error("{}", e);
            }
        });

        LOGGER.info("All tasks completed");
        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(10, TimeUnit.SECONDS);
    }

    public void print() {
        LOGGER.info("=========================== Task Dag ===========================");
        for (TaskStage stage : TaskStage.values()) {
            LOGGER.info("{}", stage);
            Map<String, AtomTask> atomTaskMap = tasks.get(stage);
            Optional.ofNullable(atomTaskMap).ifPresent(map -> map.values().stream()
                    .map(AtomTask::getTask)
                    .forEach(task -> {
                        List<Task<?>> depends = task.getDependencies();
                        LOGGER.info("\t{}, depends: {}", task.getName(),
                                depends.stream().map(Task::getName).collect(Collectors.toList()));
                    })
            );
        }
        LOGGER.info("======================= Task Dag End ===========================");
    }
}
