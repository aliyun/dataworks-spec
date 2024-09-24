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

package com.aliyun.dataworks.migrationx.transformer.core.translator;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItem;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItemType;
import com.aliyun.dataworks.migrationx.transformer.core.report.Reportable;
import com.aliyun.dataworks.migrationx.transformer.core.sqoop.DICode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * sqoop code to data x code
 *
 * @author sam.liux
 * @date 2019/07/01
 */
public class SqoopToDITranslator implements NodePropertyTranslator, Reportable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqoopToDITranslator.class);

    private static final Pattern SQOOP_PATTERN = Pattern.compile(
        "[\\s|\\n|\\t]*sqoop[\\s|\\n|\\t]+(import|IMPORT|export|EXPORT)[\\s|\\n|\\t]+");
    private static final Pattern SHELL_LINE_CONTINUATION_CHAR = Pattern.compile(
        "[\\s|\\t]*\\\\[\\s|\\t]*");

    List<ReportItem> reportItems = new ArrayList<>();

    @Override
    public boolean match(DwWorkflow workflow, DwNode node) {
        return checkNeedTranslate(node);
    }

    @Override
    public boolean translate(DwWorkflow workflow, DwNode node) {
        if (!match(workflow, node)) {
            return false;
        }

        DICode code = DICode.parseDiCode(extractSqoopCommand(node.getCode()));
        node.setCode(code.getCode());
        node.setType(CodeProgramType.DI.name());

        ReportItem reportItem = new ReportItem();
        reportItem.setWorkflow(workflow);
        reportItem.setNode(node);
        reportItem.setRiskLevel(code.getRiskLevel());
        reportItem.setAdvice(code.getAdvice());
        reportItem.setMessage(code.getMessage());
        reportItem.setType(ReportItemType.SQOOP_TO_DW_DI.getName());
        reportItem.setDescription(code.getDescription());
        reportItem.setName(String.format("%s: %s / :%s",
            SqoopToDITranslator.class.getSimpleName(), workflow.getName(), node.getName()));
        reportItems.add(reportItem);
        return true;
    }

    private String extractSqoopCommand(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }

        StringBuilder sqoopCmd = new StringBuilder();
        AtomicBoolean commandStarted = new AtomicBoolean(false);
        AtomicBoolean commandEnded = new AtomicBoolean(false);
        Arrays.stream(StringUtils.split(code, '\n')).forEach(line -> {
            if (commandEnded.get()) {
                return;
            }

            if (!commandStarted.get() && SQOOP_PATTERN.matcher(line).find()) {
                sqoopCmd.append(line);
                commandStarted.set(true);
            }

            if (commandStarted.get()) {
                if (SHELL_LINE_CONTINUATION_CHAR.matcher(line).find()) {
                    sqoopCmd.append(line);
                } else if (StringUtils.isNotBlank(line)) {
                    sqoopCmd.append(line);
                    commandEnded.set(true);
                }
            }
        });
        return sqoopCmd.toString();
    }

    private boolean checkNeedTranslate(DwNode node) {
        if (node == null || StringUtils.isBlank(node.getCode())) {
            return false;
        }

        String code = node.getCode();
        Matcher matcher = SQOOP_PATTERN.matcher(code);
        if (matcher.find()) {
            return true;
        }

        return false;
    }

    @Override
    public List<ReportItem> getReport() {
        return reportItems;
    }
}
