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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrAllocationSpec;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrLauncher;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils.ArgsUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DolphinSchedulerV2Context;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.ProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.process.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.mr.MapReduceParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.utils.ParameterUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DataStudioCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import static com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.mr.MapReduceTaskConstants.MR_NAME;
import static com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.mr.MapReduceTaskConstants.MR_QUEUE;
import static com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.utils.TaskConstants.D;

@Slf4j
public class MrParameterConverter extends AbstractParameterConverter<MapReduceParameters> {
    public MrParameterConverter(DagData processMeta, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo,
                    UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    public List<DwNode> convertParameter() {
        Optional.ofNullable(parameter).map(MapReduceParameters::getMainJar)
                .flatMap(mainJar -> ListUtils.emptyIfNull(converterContext.getDolphinSchedulerPackage().getResources())
                        .stream().filter(res -> Objects.equals(res.getId(), mainJar.getId()))
                        .findFirst()).ifPresent(res -> parameter.setMainJar(res));

        ListUtils.emptyIfNull(Optional.ofNullable(parameter).map(MapReduceParameters::getResourceFilesList)
                        .orElse(ListUtils.emptyIfNull(null)))
                .forEach(res -> ListUtils.emptyIfNull(converterContext.getDolphinSchedulerPackage().getResources()).stream()
                        .filter(res1 -> Objects.equals(res1.getId(), res.getId()))
                        .forEach(res1 -> BeanUtils.copyProperties(res1, res)));

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
            ResourceInfo mainJar = parameter.getMainJar();
            List<String> codeLines = new ArrayList<>();
            List<String> resources = new ArrayList<>();
            if (mainJar != null) {
                DolphinSchedulerV2Context context = DolphinSchedulerV2Context.getContext();
                String resourceName = CollectionUtils.emptyIfNull(context.getResources())
                        .stream()
                        .filter(r -> r.getId() == mainJar.getId())
                        .findAny()
                        .map(r -> r.getName())
                        .orElseGet(() -> "");
                resources.add(resourceName);
                codeLines.add(DataStudioCodeUtils.addResourceReference(CodeProgramType.valueOf(dwNode.getType()), "", resources));
            }

            // convert to ODPS_MR
            String command = Joiner.on(" ").join(
                    "jar", "-resources",
                    Optional.ofNullable(parameter.getMainJar().getName()).orElse(""),
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
                        spec.setQueue(parameter.getQueue());
                        emrCode.getLauncher().setAllocationSpec(spec.toMap());
                    });
        } else {
            throw new RuntimeException("not support type " + type);
        }

        return Arrays.asList(dwNode);
    }

    protected String buildCommand(MapReduceParameters mapreduceParameters) {
        // hadoop jar <jar> [mainClass] [GENERIC_OPTIONS] args...
        List<String> args = new ArrayList<>();

        // other parameters
        args.addAll(buildArgs(mapreduceParameters));

        String command = ParameterUtils.convertParameterPlaceholders(String.join(" ", args),
                new HashMap<>());
        log.info("mapreduce task command: {}", command);

        return command;
    }

    public static List<String> buildArgs(MapReduceParameters param) {
        List<String> args = new ArrayList<>();

        ResourceInfo mainJar = param.getMainJar();
        if (mainJar != null) {
            DolphinSchedulerV2Context context = DolphinSchedulerV2Context.getContext();
            String resourceName = CollectionUtils.emptyIfNull(context.getResources())
                    .stream()
                    .filter(r -> r.getId() == mainJar.getId())
                    .findAny()
                    .map(r -> r.getName())
                    .orElseGet(() -> "");
            String resource = DataStudioCodeUtils.addResourceReference(CodeProgramType.EMR_MR, "", Arrays.asList(resourceName));
            args.add(resource + resourceName);
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
        if (StringUtils.isEmpty(others) || !others.contains(MR_QUEUE)) {
            String queue = param.getQueue();
            if (StringUtils.isNotEmpty(queue)) {
                args.add(String.format("%s%s=%s", D, MR_QUEUE, queue));
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
