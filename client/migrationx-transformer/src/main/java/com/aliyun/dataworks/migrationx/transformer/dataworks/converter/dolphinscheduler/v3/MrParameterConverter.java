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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrAllocationSpec;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrLauncher;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils.ArgsUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.mr.MapReduceParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DataStudioCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import static com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.mr.MapReduceTaskConstants.MR_NAME;
import static com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.utils.TaskConstants.D;

@Slf4j
public class MrParameterConverter extends AbstractParameterConverter<MapReduceParameters> {
    public static final String MR_YARN_QUEUE = "mapreduce.job.queuename";

    public MrParameterConverter(DagData processMeta, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo,
                    UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    public List<DwNode> convertParameter() {
        DwNode dwNode = newDwNode(taskDefinition);
        String type = getConverterType();
        dwNode.setType(type);
        dwNode.setParameter(Joiner.on(" ").join(ListUtils.emptyIfNull(parameter.getLocalParams()).stream()
                .map(property -> property.getProp() + "=" + property.getValue())
                .collect(Collectors.toList())));

        // convert to EMR_MR
        if (StringUtils.equalsIgnoreCase(CodeProgramType.EMR_MR.name(), dwNode.getType())) {
            String cmd = buildCommand(parameter);
            dwNode.setCode(cmd);
            dwNode.setCode(EmrCodeUtils.toEmrCode(dwNode));
        } else if (StringUtils.equalsIgnoreCase(CodeProgramType.ODPS_MR.name(), dwNode.getType())) {
            List<String> resources = ListUtils.emptyIfNull(parameter.getResourceFilesList()).stream()
                    .filter(Objects::nonNull)
                    .map(ResourceInfo::getResourceName)
                    .filter(name -> StringUtils.isNotEmpty(name))
                    .distinct().collect(Collectors.toList());

            List<String> codeLines = new ArrayList<>();
            codeLines.add(DataStudioCodeUtils.addResourceReference(CodeProgramType.valueOf(dwNode.getType()), "", resources));

            // convert to ODPS_MR
            String command = Joiner.on(" ").join(
                    "jar", "-resources",
                    Optional.ofNullable(parameter.getMainJar().getResourceName()).orElse(""),
                    "-classpath",
                    Joiner.on(",").join(resources),
                    Optional.ofNullable(parameter.getMainClass()).orElse(""),
                    Optional.ofNullable(parameter.getMainArgs()).orElse(""),
                    Optional.ofNullable(parameter.getOthers()).orElse("")
            );
            codeLines.add(command);

            dwNode.setCode(Joiner.on("\n").join(codeLines));
            dwNode.setCode(EmrCodeUtils.toEmrCode(dwNode));
            EmrCode emrCode = EmrCodeUtils.asEmrCode(dwNode);
            Optional.ofNullable(emrCode).map(EmrCode::getLauncher)
                    .map(EmrLauncher::getAllocationSpec)
                    .map(EmrAllocationSpec::of)
                    .ifPresent(spec -> {
                        spec.setQueue(parameter.getYarnQueue());
                        emrCode.getLauncher().setAllocationSpec(spec.toMap());
                    });
        } else {
            throw new RuntimeException("not support type " + type);
        }

        return Arrays.asList(dwNode);
    }

    private String buildCommand(MapReduceParameters mapreduceParameters) {
        List<String> args = buildArgs(mapreduceParameters);
        String command = String.join(" ", args);
        log.info("mapreduce task command: {}", command);

        return command;
    }

    private static List<String> buildArgs(MapReduceParameters param) {
        List<String> args = new ArrayList<>();
        ResourceInfo mainJar = param.getMainJar();
        if (mainJar != null) {
            String resourceName = mainJar.getResourceName();
            if (StringUtils.isNotEmpty(resourceName)) {
                String[] resourceNames = resourceName.split("/");
                if (resourceNames.length > 0) {
                    resourceName = resourceNames[resourceNames.length - 1];
                }
                String resource = DataStudioCodeUtils.addResourceReference(CodeProgramType.EMR_MR, "", Arrays.asList(resourceName));
                args.add(resource + resourceName);
            }
        }

        ProgramType programType = param.getProgramType();
        String mainClass = param.getMainClass();
        if (programType != null && programType != ProgramType.PYTHON && StringUtils.isNotEmpty(mainClass)) {
            args.add(mainClass);
        }

        String appName = param.getAppName();
        if (StringUtils.isNotEmpty(appName)) {
            args.add(String.format("%s%s=%s", D, MR_NAME, ArgsUtils.escape(appName)));
        }

        String others = param.getOthers();
        if (StringUtils.isEmpty(others) || !others.contains(MR_YARN_QUEUE)) {
            String yarnQueue = param.getYarnQueue();
            if (StringUtils.isNotEmpty(yarnQueue)) {
                args.add(String.format("%s%s=%s", D, MR_YARN_QUEUE, yarnQueue));
            }
        }

        // -conf -archives -files -libjars -D
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }

        String mainArgs = param.getMainArgs();
        if (StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }
        return args;
    }

    private String getConverterType() {
        String convertType = properties.getProperty(Constants.CONVERTER_TARGET_MR_NODE_TYPE_AS);
        String defaultConvertType = CodeProgramType.EMR_MR.name();
        return getConverterType(convertType, defaultConvertType);
    }
}
