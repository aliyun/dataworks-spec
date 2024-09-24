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
import com.aliyun.dataworks.migrationx.transformer.core.sqoop.AntCommandLine;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * beeline -e parser
 *
 * @author sam.liux
 * @date 2021/02/20
 */
public abstract class AbstractCommandSqlTranslator implements Reportable, NodePropertyTranslator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommandSqlTranslator.class);
    protected List<ReportItem> reportItems = new ArrayList<>();

    @Override
    public boolean match(DwWorkflow workflow, DwNode node) {
        if (StringUtils.isBlank(node.getCode())) {
            return false;
        }

        LOGGER.debug("pattern: {}, find: {}, code: {}", getCommandPrefixPattern(),
            getCommandPrefixPattern().matcher(node.getCode()).find(), node.getCode());
        return getCommandPrefixPattern() != null && getCommandPrefixPattern().matcher(node.getCode()).find();
    }

    public static class MyParser extends GnuParser {
        @Override
        protected void processOption(String arg, ListIterator<String> iter) throws ParseException {
            try {
                super.processOption(arg, iter);
            } catch (Exception e) {
                LOGGER.warn("{}", e.getMessage());
            }
        }
    }

    @Override
    public List<ReportItem> getReport() {
        return reportItems;
    }

    abstract protected Pattern getCommandPrefixPattern();

    @Override
    public boolean translate(DwWorkflow workflow, DwNode node) throws ParseException {
        if (!matchPattern(node)) {
            return false;
        }

        String sql = StringEscapeUtils.unescapeJava(parseGnuCommandLine(node.getCode(), getSqlOption()));
        node.setCode(sql);
        if (NodeUtils.isEmrNode(node.getType())) {
            node.setCode(EmrCodeUtils.toEmrCode(node));
        }
        return true;
    }

    private boolean matchPattern(DwNode node) {
        if (StringUtils.isBlank(node.getCode())) {
            return false;
        }

        Pattern pattern = getCommandPrefixPattern();
        Matcher matcher = pattern.matcher(node.getCode());
        if (!matcher.find()) {
            LOGGER.info("code pattern not match with: {}", pattern);
            return false;
        }
        return true;
    }

    protected abstract String getSqlOption();

    abstract protected Options getGnuOptions();

    protected String parseGnuCommandLine(String code, String sqlOption) throws ParseException {
        Options options = getGnuOptions();
        MyParser parser = new MyParser();
        AntCommandLine antCommandLine = new AntCommandLine(code);
        CommandLine cli = parser.parse(options, antCommandLine.getCommandline());
        return cli.getOptionValue(sqlOption, code);
    }
}
