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
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils.MapReduceArgsUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.ProcessMeta;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.TaskNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.datasource.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.entity.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.mr.MapReduceParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DataStudioCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;

import com.google.common.base.Joiner;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

/**
 * @author 聿剑
 * @date 2022/10/21
 */
public class MrParameterConverter extends AbstractParameterConverter<MapReduceParameters> {
    public MrParameterConverter(ProcessMeta processMeta,
            TaskNode taskDefinition,
            DolphinSchedulerConverterContext<Project, ProcessMeta, DataSource, ResourceInfo,
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

        DwNode dwNode = newDwNode(processMeta, taskDefinition);
        String type = getConverterType();
        dwNode.setType(type);
        dwNode.setParameter(Joiner.on(" ").join(ListUtils.emptyIfNull(parameter.getLocalParams()).stream()
                .map(property -> property.getProp() + "=" + property.getValue())
                .collect(Collectors.toList())));

        List<String> resources = ListUtils.emptyIfNull(parameter.getResourceFilesList()).stream()
                .filter(Objects::nonNull)
                .map(ResourceInfo::getName).distinct().collect(Collectors.toList());

        List<String> codeLines = new ArrayList<>();
        codeLines.add(DataStudioCodeUtils.addResourceReference(CodeProgramType.valueOf(dwNode.getType()), "", resources));

        // convert to EMR_MR
        if (StringUtils.equalsIgnoreCase(CodeProgramType.EMR_MR.name(), dwNode.getType())) {
            String command = Joiner.on(" ").join(MapReduceArgsUtils.buildArgs(parameter).stream()
                    .map(String::valueOf).collect(Collectors.toList()));
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
        }

        // convert to ODPS_MR
        if (StringUtils.equalsIgnoreCase(CodeProgramType.ODPS_MR.name(), dwNode.getType())) {

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
        }

        return Arrays.asList(dwNode);
    }

    private String getConverterType() {
        String convertType = properties.getProperty(Constants.CONVERTER_TARGET_MR_NODE_TYPE_AS);
        String defaultConvertType = CodeProgramType.EMR_MR.name();
        return getConverterType(convertType, defaultConvertType);
    }
}
