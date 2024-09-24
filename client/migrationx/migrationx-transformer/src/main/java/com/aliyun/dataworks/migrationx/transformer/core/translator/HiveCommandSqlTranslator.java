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
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * hive -e parser
 *
 * @author sam.liux
 * @date 2021/02/20
 */
public class HiveCommandSqlTranslator extends AbstractCommandSqlTranslator {
    private static final Logger LOGGER = LoggerFactory.getLogger(HiveCommandSqlTranslator.class);

    public static Pattern HIVE_PATTERN = Pattern.compile("[\\s|\\n|\\t]*hive[\\s|\\n|\\t]+.*");

    @Override
    public boolean match(DwWorkflow workflow, DwNode node) {
        return super.match(workflow, node) && StringUtils.contains(node.getCode(), " -e ");
    }

    @Override
    protected Pattern getCommandPrefixPattern() {
        return HIVE_PATTERN;
    }

    @Override
    protected String getSqlOption() {
        return "e";
    }

    @Override
    protected Options getGnuOptions() {
        Options options = new Options();
        // -e <query>
        options.addOption(OptionBuilder
            .hasArgs()
            .withArgName("query")
            .withDescription("The query that should be executed")
            .create('e'));
        return options;
    }
}
