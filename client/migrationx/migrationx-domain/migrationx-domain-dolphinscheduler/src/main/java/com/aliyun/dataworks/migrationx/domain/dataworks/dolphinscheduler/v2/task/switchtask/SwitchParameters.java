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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.switchtask;

import java.util.ArrayList;
import java.util.List;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.DependentRelation;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.process.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.AbstractParameters;

public class SwitchParameters extends AbstractParameters {

    private DependentRelation dependRelation;
    private String relation;
    private Long nextNode;
    private int resultConditionLocation;
    private List<SwitchResultVo> dependTaskList;

    @Override
    public boolean checkParameters() {
        return true;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    public DependentRelation getDependRelation() {
        return dependRelation;
    }

    public void setDependRelation(DependentRelation dependRelation) {
        this.dependRelation = dependRelation;
    }

    public int getResultConditionLocation() {
        return resultConditionLocation;
    }

    public void setResultConditionLocation(int resultConditionLocation) {
        this.resultConditionLocation = resultConditionLocation;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public List<SwitchResultVo> getDependTaskList() {
        return dependTaskList;
    }

    public void setDependTaskList(List<SwitchResultVo> dependTaskList) {
        this.dependTaskList = dependTaskList;
    }

    public Long getNextNode() {
        return nextNode;
    }

    public void setNextNode(Long nextNode) {
        this.nextNode = nextNode;
    }
}