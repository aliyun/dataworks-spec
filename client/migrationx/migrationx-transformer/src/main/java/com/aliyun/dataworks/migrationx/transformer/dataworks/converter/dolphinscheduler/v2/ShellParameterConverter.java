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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.process.Property;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.process.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.shell.ShellParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DataStudioCodeUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.DefaultNodeTypeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.translator.HiveEofDelimiterCommandSqlTranslator;
import com.aliyun.dataworks.migrationx.transformer.core.translator.SqoopToDITranslator;
import com.aliyun.dataworks.migrationx.transformer.core.translator.TranslateUtils;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class ShellParameterConverter extends AbstractParameterConverter<ShellParameters> {
    public ShellParameterConverter(DagData processMeta, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo,
                    UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    public List<DwNode> convertParameter() {
        DwNode dwNode = newDwNode(taskDefinition);
        String converterType = getShellConverterType();
        dwNode.setType(converterType);
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
        if (StringUtils.equalsIgnoreCase(CodeProgramType.EMR_SHELL.name(), dwNode.getType())) {
            dwNode.setCode(EmrCodeUtils.toEmrCode(dwNode));
        }

        boolean changed = TranslateUtils.translate(dwWorkflow, dwNode,
                Collections.singletonList(SqoopToDITranslator.class), () -> CodeProgramType.DI);
        String sqlConverterType = getSQLConverterType(dwNode.getType());
        if (!changed) {
            CodeProgramType nodeType = DefaultNodeTypeUtils.getTypeByName(sqlConverterType, CodeProgramType.DIDE_SHELL);
            changed = TranslateUtils.translate(dwWorkflow, dwNode,
                    Collections.singletonList(HiveEofDelimiterCommandSqlTranslator.class), () -> nodeType);
        }

        if (!changed) {
            changed = TranslateUtils.translateSparkSubmit(dwNode, properties);
        }

        if (!changed) {
            changed = TranslateUtils.translateCommandSql(dwWorkflow, dwNode, sqlConverterType);
        }
        log.info("node: {}, type: {}, translation changed: {}", dwNode.getName(), dwNode.getType(), changed);
        return Arrays.asList(dwNode);
    }

    private String getShellConverterType() {
        String convertType = properties.getProperty(Constants.CONVERTER_TARGET_SHELL_NODE_TYPE_AS);
        String defaultConvertType = CodeProgramType.DIDE_SHELL.name();
        return getConverterType(convertType, defaultConvertType);
    }

    private String getSQLConverterType(String defaultConvertType) {
        String convertType = properties.getProperty(Constants.CONVERTER_TARGET_COMMAND_SQL_TYPE_AS);
        return getConverterType(convertType, defaultConvertType);
    }
}
