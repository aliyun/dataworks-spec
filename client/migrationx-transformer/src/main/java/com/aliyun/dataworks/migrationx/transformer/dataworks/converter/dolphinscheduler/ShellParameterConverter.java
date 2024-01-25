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

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Datasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Property;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DataStudioCodeUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DefaultNodeTypeUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.ProcessMeta;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.TaskNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.shell.ShellParameters;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.translator.HiveEofDelimiterCommandSqlTranslator;
import com.aliyun.dataworks.migrationx.transformer.core.translator.SqoopToDITranslator;
import com.aliyun.dataworks.migrationx.transformer.core.translator.TranslateUtils;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 聿剑
 * @date 2022/10/24
 */
@Slf4j
public class ShellParameterConverter extends AbstractParameterConverter<ShellParameters> {
    public ShellParameterConverter(ProcessMeta processMeta, TaskNode taskDefinition,
        DolphinSchedulerConverterContext<Project, ProcessMeta, Datasource, ResourceInfo,
            UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    protected void convertParameter() {
        DwNode dwNode = newDwNode(processMeta, taskDefinition);
        dwNode.setType(
            properties.getProperty(Constants.CONVERTER_TARGET_SHELL_NODE_TYPE_AS, CodeProgramType.DIDE_SHELL.name()));
        dwNode.setCode(parameter.getRawScript());
        List<String> resources = ListUtils.emptyIfNull(parameter.getResourceFilesList()).stream()
            .map(ResourceInfo::getName).distinct().collect(Collectors.toList());

        List<String> codeLines = new ArrayList<>();
        codeLines.add(DataStudioCodeUtils.addResourceReference(CodeProgramType.valueOf(dwNode.getType()), "", resources));

        if (StringUtils.equalsIgnoreCase(CodeProgramType.DIDE_SHELL.name(), dwNode.getType())) {
            dwNode.setParameter(Joiner.on(" ").join(ListUtils.emptyIfNull(parameter.getLocalParams()).stream()
                .map(Property::getValue)
                .collect(Collectors.toList())));
            String codeArgPredefine = ListUtils.emptyIfNull(parameter.getLocalParams()).stream()
                .map(property -> property.getProp() + "=" + property.getValue())
                .collect(Collectors.joining("\n"));
            codeLines.add(codeArgPredefine);
        }
        codeLines.add(parameter.getRawScript());
        dwNode.setCode(Joiner.on("\n").join(codeLines));

        boolean changed = TranslateUtils.translate(dwWorkflow, dwNode,
            Collections.singletonList(SqoopToDITranslator.class), () -> CodeProgramType.DI);
        if (!changed) {
            String type = properties.getProperty(Constants.CONVERTER_TARGET_COMMAND_SQL_TYPE_AS, dwNode.getType());
            CodeProgramType nodeType = DefaultNodeTypeUtils.getTypeByName(type, CodeProgramType.DIDE_SHELL);
            changed = TranslateUtils.translate(dwWorkflow, dwNode,
                Collections.singletonList(HiveEofDelimiterCommandSqlTranslator.class), () -> nodeType);
        }

        if (!changed) {
            changed = TranslateUtils.translateSparkSubmit(dwNode, properties);
        }

        if (!changed) {
            changed = TranslateUtils.translateCommandSql(dwWorkflow, dwNode, properties);
        }
        log.info("node: {}, type: {}, translation changed: {}", dwNode.getName(), dwNode.getType(), changed);
    }
}
