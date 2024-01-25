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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DataStudioCodeUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Datasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils.SparkArgsUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.ProcessMeta;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.TaskNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.spark.SparkParameters;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.translator.TranslateUtils;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 聿剑
 * @date 2022/10/21
 */
@Slf4j
public class SparkParameterConverter extends AbstractParameterConverter<SparkParameters> {
    public SparkParameterConverter(ProcessMeta processMeta,
        TaskNode taskDefinition,
        DolphinSchedulerConverterContext<Project, ProcessMeta, Datasource, ResourceInfo,
            UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    protected void convertParameter() {
        Optional.ofNullable(parameter).map(SparkParameters::getMainJar)
            .flatMap(mainJar -> ListUtils.emptyIfNull(converterContext.getDolphinSchedulerPackage().getResources())
                .stream().filter(res -> Objects.equals(res.getId(), mainJar.getId()))
                .findFirst()).ifPresent(res -> parameter.setMainJar(res));

        ListUtils.emptyIfNull(Optional.ofNullable(parameter).map(SparkParameters::getResourceFilesList)
                .orElse(ListUtils.emptyIfNull(null)))
            .forEach(res -> ListUtils.emptyIfNull(converterContext.getDolphinSchedulerPackage().getResources()).stream()
                .filter(res1 -> Objects.equals(res1.getId(), res.getId()))
                .forEach(res1 -> {
                    BeanUtils.copyProperties(res1, res);
                    res.setRes(res1.getName());
                    res.setName(res1.getName());
                }));

        DwNode dwNode = newDwNode(processMeta, taskDefinition);
        dwNode.setType(
            properties.getProperty(Constants.CONVERTER_SPARK_SUBMIT_TYPE_AS, CodeProgramType.DIDE_SHELL.name()));
        dwNode.setParameter(Joiner.on(" ").join(ListUtils.emptyIfNull(parameter.getLocalParams()).stream()
            .map(property -> property.getProp() + "=" + property.getValue())
            .collect(Collectors.toList())));

        List<String> resources = ListUtils.emptyIfNull(parameter.getResourceFilesList()).stream()
            .filter(Objects::nonNull)
            .map(ResourceInfo::getName).distinct().collect(Collectors.toList());

        List<String> codeLines = new ArrayList<>();
        codeLines.add(DataStudioCodeUtils.addResourceReference(CodeProgramType.valueOf(dwNode.getType()), "", resources));

        String sparkCmd = Joiner.on(" ").join(SparkArgsUtils.buildArgs(parameter).stream().map(String::valueOf)
            .collect(Collectors.toList()));
        dwNode.setCode(sparkCmd);
        log.info("node: {}, spark command: {}", dwNode.getName(), sparkCmd);
        TranslateUtils.translateSparkSubmit(dwNode, properties);

        codeLines.add(dwNode.getCode());
        dwNode.setCode(codeLines.stream().filter(Objects::nonNull).collect(Collectors.joining("\n")));
    }
}
