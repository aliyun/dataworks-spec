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

import com.aliyun.dataworks.migrationx.transformer.core.common.Context;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItem;
import com.aliyun.dataworks.migrationx.transformer.core.report.Reportable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author sam.liux
 * @date 2019/07/03
 */
public abstract class Task<T> implements Callable<T>, Reportable {
    protected String name;
    protected List<Task<?>> dependencies = new ArrayList<>();
    protected T result;
    protected Context context;
    protected TaskStage stage;
    protected List<ReportItem> reportItems = new ArrayList<>();
    protected TaskDag taskDag;
    protected TaskStatus taskStatus = TaskStatus.INIT;

    public Task() {
        this.name = this.getClass().getSimpleName();
    }

    public Task(String name) {
        this.name = name;
    }

    public Task(String name, Task... dependsOn) {
        this.name = name;
        for (Task dep : dependsOn) {
            this.dependencies.add(dep);
        }
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void setTaskDag(TaskDag taskDag) {
        this.taskDag = taskDag;
    }

    public Class<T> getTemplateParameterType() {
        Method[] methods = this.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if ("call".equals(method.getName())) {
                return (Class<T>)method.getReturnType();
            }
        }

        throw new RuntimeException("get template parameter type exception");
    }

    public TaskStage getStage() {
        return stage;
    }

    public void setStage(TaskStage stage) {
        this.stage = stage;
    }

    public String getName() {
        return name;
    }

    public List<Task<?>> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Task<?>> dependencies) {
        this.dependencies = dependencies;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Task<T> dependsOn(Task<?> dependTask) {
        long count = getDependencies().stream().filter(o -> o.getName().equals(dependTask.getName())).count();
        if (count == 0) {
            this.dependencies.add(dependTask);
        }
        return this;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public T getResult() {
        return result;
    }

    @Override
    public T call() throws Exception {
        result = this.call();
        return result;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public List<ReportItem> getReport() {
        return reportItems;
    }
}
