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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.v1;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.ProcessMeta;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.TaskNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.datasource.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.entity.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.python.PythonParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwResource;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DataStudioCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2022/10/26
 */
@Slf4j
public class PythonParameterConverter extends AbstractParameterConverter<PythonParameters> {
    public PythonParameterConverter(ProcessMeta processMeta,
            TaskNode taskDefinition,
            DolphinSchedulerConverterContext<Project, ProcessMeta, DataSource, ResourceInfo,
                    UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    public List<DwNode> convertParameter() throws IOException {
        DwNode dwNode = newDwNode(processMeta, taskDefinition);
        String type = getConverterType();
        dwNode.setType(type);

        DwResource pyRes = new DwResource();
        pyRes.setName(Joiner.on("_").join(processMeta.getProcessDefinitionName(), taskDefinition.getName()) + ".py");
        pyRes.setWorkflowRef(dwWorkflow);
        dwWorkflow.getResources().add(pyRes);

        List<String> resources = ListUtils.emptyIfNull(parameter.getResourceList()).stream()
                .map(ResourceInfo::getName).distinct().collect(Collectors.toList());
        resources.add(pyRes.getName());

        dwNode.setCode(Joiner.on("\n").join(
                DataStudioCodeUtils.addResourceReference(CodeProgramType.valueOf(dwNode.getType()), "", resources),
                "python ./" + pyRes.getName()
                // TODO: how about parameters and replacement
        ));

        String engineType = properties.getProperty(Constants.CONVERTER_TARGET_ENGINE_TYPE, "");
        if (StringUtils.equalsIgnoreCase(CalcEngineType.EMR.name(), engineType)) {
            pyRes.setType(CodeProgramType.EMR_FILE.name());
            dwNode.setCode(EmrCodeUtils.toEmrCode(dwNode));
        } else if (StringUtils.equalsIgnoreCase(CalcEngineType.HADOOP_CDH.name(), engineType)) {
            pyRes.setType(CodeProgramType.CDH_FILE.name());
        } else {
            pyRes.setType(CodeProgramType.ODPS_PYTHON.name());
        }
        File tmpFIle = new File(FileUtils.getTempDirectory(), pyRes.getName());
        FileUtils.writeStringToFile(tmpFIle, parameter.getRawScript(), StandardCharsets.UTF_8);
        pyRes.setLocalPath(tmpFIle.getAbsolutePath());
        return Arrays.asList(dwNode);
    }

    private String getConverterType() {
        String convertType = properties.getProperty(Constants.CONVERTER_TARGET_SHELL_NODE_TYPE_AS);
        String defaultConvertType = CodeProgramType.DIDE_SHELL.name();
        return getConverterType(convertType, defaultConvertType);
    }
}
