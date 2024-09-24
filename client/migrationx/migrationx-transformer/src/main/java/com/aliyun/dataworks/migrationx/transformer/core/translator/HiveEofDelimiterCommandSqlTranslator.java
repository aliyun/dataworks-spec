
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
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.NodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItem;
import com.aliyun.dataworks.migrationx.transformer.core.report.Reportable;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * interactive shell command sql code translator
 * XXX<<<EOF
 * EOF
 *
 * @author sam.liux
 * @date 2023/3/07
 */
public class HiveEofDelimiterCommandSqlTranslator implements Reportable, NodePropertyTranslator {
    private static final Logger LOGGER = LoggerFactory.getLogger(HiveEofDelimiterCommandSqlTranslator.class);

    public static Pattern START_DELIMITER_PATTERN = Pattern.compile("\\s*(?i)hive\\s*<<\\s*(?i)EOF\\s*\\n*");
    public static Pattern END_DELIMITER_PATTERN = Pattern.compile("\\n*\\s*(?i)EOF\\s*\\n*");
    public static Pattern ECHO_COMMENT_PATTERN = Pattern.compile("^\\s*(?i)echo\\s+'(?<comment>.+)'");

    @Override
    public boolean match(DwWorkflow workflow, DwNode node) {
        return START_DELIMITER_PATTERN.matcher(node.getCode()).find();
    }

    @Override
    public boolean translate(DwWorkflow workflow, DwNode node) throws Exception {
        if (!match(workflow, node)) {
            return false;
        }

        String sql = parseSqlCode(node.getCode());
        node.setCode(sql);
        if (NodeUtils.isEmrNode(node.getType())) {
            node.setCode(EmrCodeUtils.toEmrCode(node));
            return true;
        }
        return true;
    }

    private String parseSqlCode(String nodeCode) {
        if (StringUtils.isBlank(nodeCode)) {
            return nodeCode;
        }

        StringBuilder code = new StringBuilder();
        AtomicBoolean codeStarts = new AtomicBoolean(false);
        AtomicBoolean codeEnds = new AtomicBoolean(false);
        Arrays.stream(StringUtils.split(nodeCode, "\n")).forEach(line -> {
            if (START_DELIMITER_PATTERN.matcher(line).find()) {
                codeStarts.set(true);
                line = line.replaceFirst(START_DELIMITER_PATTERN.pattern(), "");
            } else if (codeStarts.get() && END_DELIMITER_PATTERN.matcher(line).find()) {
                codeEnds.set(true);
            }

            Matcher commentMatcher = ECHO_COMMENT_PATTERN.matcher(line);
            if (!codeStarts.get() && commentMatcher.matches()) {
                String comment = commentMatcher.group("comment");
                code.append("--").append(comment).append("\n");
            }
            if (codeStarts.get() && !codeEnds.get()) {
                code.append(line).append("\n");
            }
        });
        return code.toString();
    }

    @Override
    public List<ReportItem> getReport() {
        return null;
    }
}
