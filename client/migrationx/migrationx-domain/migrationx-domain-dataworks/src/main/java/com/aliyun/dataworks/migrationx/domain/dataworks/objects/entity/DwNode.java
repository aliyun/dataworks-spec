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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v1.DeployType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeNodeDef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;
import lombok.ToString;

/**
 * 内部需要处理存储，但是不想通过model包透出的字段
 *
 * @author sam.liux
 * @date 2019/07/08
 */

@ToString(callSuper = true, exclude = {"workflowRef"})
public class DwNode extends Node {

    @JsonIgnore
    private transient String fileRelations;

    @JsonIgnore
    private transient String rawNodeType;

    @JsonIgnore
    private transient IdeNodeDef nodeDef;

    @JsonIgnore
    private transient Workflow workflowRef;

    @JsonIgnore
    private transient Node outerNode;

    @JsonIgnore
    private transient DeployType deployType;

    @JsonIgnore
    private transient String extension;

    @JsonIgnore
    private transient Integer commitStatus;

    @JsonIgnore
    private transient Integer isUserNode;

    @JsonIgnore
    private transient Integer fileDelete;

    @JsonIgnore
    private transient String sourcePath;

    @JsonIgnore
    private transient String origin;

    @Override
    public String getUniqueKey() {
        List<String> parts = new ArrayList<>();
        Optional.ofNullable(workflowRef).ifPresent(workflow -> parts.add(workflow.getUniqueKey()));
        parts.add(getName());
        Optional.ofNullable(outerNode).ifPresent(node -> parts.add(outerNode.getUniqueKey()));
        Optional.ofNullable(getFolder()).ifPresent(folder -> parts.add(folder));
        String str = Joiner.on("#").join(parts);
        return UUID.nameUUIDFromBytes(str.getBytes()).toString();
    }

    public String getFileRelations() {
        return fileRelations;
    }

    public void setFileRelations(String fileRelations) {
        this.fileRelations = fileRelations;
    }

    public String getRawNodeType() {
        return rawNodeType;
    }

    public void setRawNodeType(String rawNodeType) {
        this.rawNodeType = rawNodeType;
    }

    public IdeNodeDef getNodeDef() {
        return nodeDef;
    }

    public void setNodeDef(IdeNodeDef nodeDef) {
        this.nodeDef = nodeDef;
    }

    public Workflow getWorkflowRef() {
        return workflowRef;
    }

    public void setWorkflowRef(Workflow workflowRef) {
        this.workflowRef = workflowRef;
    }

    public Node getOuterNode() {
        return outerNode;
    }

    public void setOuterNode(Node outerNode) {
        this.outerNode = outerNode;
    }

    public DeployType getDeployType() {
        return deployType;
    }

    public void setDeployType(DeployType deployType) {
        this.deployType = deployType;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Integer getCommitStatus() {
        return commitStatus;
    }

    public void setCommitStatus(Integer commitStatus) {
        this.commitStatus = commitStatus;
    }

    public Integer getIsUserNode() {
        return isUserNode;
    }

    public void setIsUserNode(Integer isUserNode) {
        this.isUserNode = isUserNode;
    }

    public Integer getFileDelete() {
        return fileDelete;
    }

    public void setFileDelete(Integer fileDelete) {
        this.fileDelete = fileDelete;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}