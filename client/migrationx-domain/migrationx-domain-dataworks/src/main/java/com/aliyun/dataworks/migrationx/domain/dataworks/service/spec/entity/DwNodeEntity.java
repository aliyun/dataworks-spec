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

package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeContext;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.nodemarket.AppConfigPack;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.RerunMode;

/**
 * @author 聿剑
 * @date 2024/6/18
 */
public interface DwNodeEntity {
    /**
     * uuid
     *
     * @return node uuid
     */
    String getUuid();

    /**
     * node biz id
     *
     * @return bizId
     */
    Long getBizId();

    /**
     * get node biz name
     *
     * @return name
     */
    String getBizName();

    /**
     * node resource group name
     *
     * @return resource group name
     */
    String getResourceGroupName();

    /**
     * node name
     *
     * @return node name
     */
    String getName();

    /**
     * node type
     *
     * @return type String
     * @see com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType
     */
    String getType();

    /**
     * node type id
     *
     * @return Integer
     * @see CodeProgramType#getCode()
     */
    Integer getTypeId();

    /**
     * node cron express
     *
     * @return cron express string
     */
    String getCronExpress();

    /**
     * start effect date
     *
     * @return date
     */
    Date getStartEffectDate();

    /**
     * is auto parse
     *
     * @return 0/1
     */
    Integer getIsAutoParse();

    /**
     * end effect date
     *
     * @return date
     */
    Date getEndEffectDate();

    /**
     * resource group identifier
     * example: S_resgroup_xxx
     *
     * @return identifier string
     */
    String getResourceGroup();

    /**
     * Data integration resource group
     *
     * @return identifier string
     */
    String getDiResourceGroup();

    /**
     * Data integration resource group name
     *
     * @return name string
     */
    String getDiResourceGroupName();

    /**
     * code mode for data integration
     *
     * @return code mode String
     * @see com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.CodeModeType
     */
    String getCodeMode();

    /**
     * instance mode of node, whether to start immediately
     *
     * @return true/false
     */
    Boolean getStartRightNow();

    /**
     * rerun mode
     *
     * @return rerun mode
     */
    RerunMode getRerunMode();

    /**
     * is paused
     *
     * @return true/false
     */
    Boolean getPauseSchedule();

    /**
     * node use type
     *
     * @return NodeUseType
     */
    @NotNull
    NodeUseType getNodeUseType();

    String getRef();

    /**
     * node folder path
     *
     * @return path string
     */
    String getFolder();

    /**
     * is root or not
     *
     * @return true/false
     */
    Boolean getRoot();

    /**
     * connection name of node
     *
     * @return name string
     */
    String getConnection();

    /**
     * code content of node
     *
     * @return code string
     */
    String getCode();

    /**
     * node parameter
     *
     * @return string
     */
    String getParameter();

    /**
     * node input context info
     *
     * @return List of context
     */
    List<NodeContext> getInputContexts();

    /**
     * node output context info
     *
     * @return List of context
     */
    List<NodeContext> getOutputContexts();

    /**
     * input dependencies
     *
     * @return List of node input dependencies
     */
    List<NodeIo> getInputs();

    /**
     * node outputs strings
     *
     * @return List of node output strings
     */
    List<NodeIo> getOutputs();

    /**
     * inner nodes of node
     *
     * @return List of node
     */
    List<DwNodeEntity> getInnerNodes();

    /**
     * description
     *
     * @return string
     */
    String getDescription();

    /**
     * task rerun repeats
     *
     * @return count of repeats
     */
    Integer getTaskRerunTime();

    /**
     * task rerun interval
     *
     * @return seconds of rerun interval
     */
    Integer getTaskRerunInterval();

    /**
     * dependency type
     *
     * @return value of type
     * @see com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.DependentType
     */
    Integer getDependentType();

    /**
     * cycle type
     *
     * @return value of type
     * @see com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.CycleType
     */
    Integer getCycleType();

    /**
     * last modify time
     *
     * @return date
     */
    Date getLastModifyTime();

    /**
     * last modify user
     *
     * @return string
     */
    String getLastModifyUser();

    /**
     * multi instance check type
     *
     * @return value of type
     */
    Integer getMultiInstCheckType();

    /**
     * priority
     *
     * @return number of priority
     */
    Integer getPriority();

    /**
     * cross cycle dependent nodes
     *
     * @return string of node ids with comma separated
     */
    String getDependentDataNode();

    /**
     * owner
     *
     * @return String
     */
    String getOwner();

    /**
     * owner name
     *
     * @return owner name string
     */
    String getOwnerName();

    /**
     * extra config string
     *
     * @return string
     */
    String getExtraConfig();

    /**
     * extra content string
     *
     * @return string
     */
    String getExtraContent();

    /**
     * tt content for tt_merge/dd_merge specially
     *
     * @return string of content
     */
    String getTtContent();

    /**
     * advance settings
     *
     * @return string
     */
    String getAdvanceSettings();

    /**
     * extend config
     *
     * @return json string
     */
    String getExtend();

    /**
     * get component
     *
     * @return DwNodeEntity
     */
    SpecComponent getComponent();

    default String getOrigin() {
        return null;
    }

    default String getWorkflowName() {
        return null;
    }

    /**
     * get config pack
     *
     * @return Map of config pack
     */
    default Map<String, AppConfigPack> getConfigPack() {
        return null;
    }
}
