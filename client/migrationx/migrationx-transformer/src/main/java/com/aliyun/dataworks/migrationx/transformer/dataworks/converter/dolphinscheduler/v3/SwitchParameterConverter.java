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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.v3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ControllerBranchCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ControllerBranchCode.Branch;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DolphinSchedulerV3Context;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.SwitchResultVo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.switchs.SwitchParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;
import com.aliyun.migrationx.common.utils.JSONUtils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SwitchParameterConverter extends AbstractParameterConverter<SwitchParameters> {

    public SwitchParameterConverter(DagData dagData, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo, UdfFunc> converterContext) {
        super(dagData, taskDefinition, converterContext);
    }

    @Override
    public List<DwNode> convertParameter() throws IOException {
        log.info("params : {}", taskDefinition.getTaskParams());

        DwNode dwNode = newDwNode(taskDefinition);
        dwNode.setType(CodeProgramType.CONTROLLER_BRANCH.getName());

        JsonNode param = JSONUtils.parseObject(taskDefinition.getTaskParams(), JsonNode.class);
        SwitchParameters switchParameters = null;
        if (param.get("switchResult") != null) {
            switchParameters = JSONUtils.parseObject(param.get("switchResult"), SwitchParameters.class);
        }
        if (switchParameters == null || switchParameters.getDependTaskList() == null || switchParameters.getDependTaskList().isEmpty()) {
            log.warn("no dependence param {}", taskDefinition.getTaskParams());
            return Arrays.asList(dwNode);
        }

        Long defaultNextCode = switchParameters.getNextNode();
        List<Branch> branchList = new ArrayList<>();

        for (SwitchResultVo switchResultVo : switchParameters.getDependTaskList()) {
            String condition = switchResultVo.getCondition();
            //task code
            Long nextNodeCode = switchResultVo.getNextNode();
            TaskDefinition branchTask = DolphinSchedulerV3Context.getContext().getTaskCodeMap().get(nextNodeCode);
            if (branchTask == null) {
                continue;
            }
            Branch branch = new Branch();
            branch.setCondition(condition);
            String taskName = branchTask.getName();
            String output = getDefaultNodeOutput(processMeta, taskName);
            branch.setNodeoutput(output);
            branchList.add(branch);
        }

        ControllerBranchCode branchCode = new ControllerBranchCode();
        branchCode.setBranchList(branchList);
        dwNode.setCode(branchCode.getContent());
        return Arrays.asList(dwNode);
    }
}
