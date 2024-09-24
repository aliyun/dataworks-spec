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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.LabelType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DolphinSchedulerV3Context;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.python.PythonParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwResource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.ResourceType;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DataStudioCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;
import com.aliyun.dataworks.migrationx.transformer.dataworks.transformer.DataWorksTransformerConfig;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2022/10/26
 */
@Slf4j
public class PythonParameterConverter extends AbstractParameterConverter<PythonParameters> {
    public PythonParameterConverter(DagData processMeta, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo,
                    UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    public List<DwNode> convertParameter() throws IOException {
        DwNode dwNode = newDwNode(taskDefinition);
        String shellType = getConverterType();
        String type = properties.getProperty(Constants.CONVERTER_TARGET_PYTHON_NODE_TYPE_AS, shellType);
        dwNode.setType(type);
        
        String usingCmd = properties.getProperty(Constants.CONVERTER_PYTHON_CODE_BLOCK, "false");
        if (Boolean.parseBoolean(usingCmd)) {
            //python -c 'some python code block'
            String cmd = "python -c \"" + parameter.getRawScript() + "\"";
            dwNode.setCode(cmd);
        } else if (CodeProgramType.ODPS_PYTHON.equals(CodeProgramType.of(type))
                || CodeProgramType.PYODPS.equals(CodeProgramType.of(type))
                || CodeProgramType.PYODPS3.equals(CodeProgramType.of(type))) {
            dwNode.setCode(parameter.getRawScript());
        } else {
            DwResource pyRes = new DwResource();
            pyRes.setName(Joiner.on("_").join(processMeta.getName(), taskDefinition.getName()) + ".py");
            pyRes.setWorkflowRef(dwWorkflow);
            dwWorkflow.getResources().add(pyRes);
            DolphinSchedulerV3Context context = DolphinSchedulerV3Context.getContext();
            List<String> resources = ListUtils.emptyIfNull(parameter.getResourceList()).stream()
                    .map(resource -> {
                        CollectionUtils.emptyIfNull(context.getResources())
                                .stream()
                                .filter(r -> r.getFullName() == resource.getResourceName())
                                .findAny()
                                .ifPresent(
                                        r -> resource.setResourceName(r.getName())
                                );
                        return resource.getResourceName();
                    }).filter(name -> StringUtils.isNotEmpty(name))
                    .distinct()
                    .collect(Collectors.toList());

            resources.add(pyRes.getName());
            dwNode.setCode(Joiner.on("\n").join(
                    DataStudioCodeUtils.addResourceReference(CodeProgramType.of(dwNode.getType()), "", resources),
                    "python ./" + pyRes.getName()
                    // TODO: how about parameters and replacement
            ));

            String engineType = properties.getProperty(Constants.CONVERTER_TARGET_ENGINE_TYPE, "");
            if (StringUtils.equalsIgnoreCase(CalcEngineType.EMR.name(), engineType)) {
                pyRes.setType(CodeProgramType.EMR_FILE.name());
                pyRes.setExtend(ResourceType.PYTHON.name());
                dwNode.setCode(EmrCodeUtils.toEmrCode(dwNode));
                List<String> paths = new ArrayList<>();
                DataWorksTransformerConfig config = DataWorksTransformerConfig.getConfig();
                if (config != null) {
                    paths.add(CalcEngineType.EMR.getDisplayName(config.getLocale()));
                    paths.add(LabelType.RESOURCE.getDisplayName(config.getLocale()));
                } else {
                    paths.add(CalcEngineType.EMR.getDisplayName(Locale.SIMPLIFIED_CHINESE));
                    paths.add(LabelType.RESOURCE.getDisplayName(Locale.SIMPLIFIED_CHINESE));
                }

                pyRes.setFolder(Joiner.on(File.separator).join(paths));
            } else if (StringUtils.equalsIgnoreCase(CalcEngineType.HADOOP_CDH.name(), engineType)) {
                pyRes.setExtend(ResourceType.PYTHON.name());
                pyRes.setType(CodeProgramType.CDH_FILE.name());
                List<String> paths = new ArrayList<>();
                DataWorksTransformerConfig config = DataWorksTransformerConfig.getConfig();
                if (config != null) {
                    paths.add(CalcEngineType.HADOOP_CDH.getDisplayName(config.getLocale()));
                    paths.add(LabelType.RESOURCE.getDisplayName(config.getLocale()));
                } else {
                    paths.add(CalcEngineType.HADOOP_CDH.getDisplayName(Locale.SIMPLIFIED_CHINESE));
                    paths.add(LabelType.RESOURCE.getDisplayName(Locale.SIMPLIFIED_CHINESE));
                }
                pyRes.setFolder(Joiner.on(File.separator).join(paths));
            } else {
                pyRes.setExtend(ResourceType.PYTHON.name());
                pyRes.setType(CodeProgramType.of(type).getName());
                List<String> paths = new ArrayList<>();
                DataWorksTransformerConfig config = DataWorksTransformerConfig.getConfig();
                if (config != null) {
                    paths.add(CalcEngineType.ODPS.getDisplayName(config.getLocale()));
                    paths.add(LabelType.RESOURCE.getDisplayName(config.getLocale()));
                } else {
                    paths.add(CalcEngineType.ODPS.getDisplayName(Locale.SIMPLIFIED_CHINESE));
                    paths.add(LabelType.RESOURCE.getDisplayName(Locale.SIMPLIFIED_CHINESE));
                }
                pyRes.setFolder(Joiner.on(File.separator).join(paths));
            }
            File tmpFIle = new File(FileUtils.getTempDirectory(), pyRes.getName());
            FileUtils.writeStringToFile(tmpFIle, parameter.getRawScript(), StandardCharsets.UTF_8);
            pyRes.setLocalPath(tmpFIle.getAbsolutePath());
        }

        return Arrays.asList(dwNode);
    }

    private String getConverterType() {
        String convertType = properties.getProperty(Constants.CONVERTER_TARGET_SHELL_NODE_TYPE_AS);
        String defaultConvertType = CodeProgramType.DIDE_SHELL.name();
        return getConverterType(convertType, defaultConvertType);
    }
}
