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
 * beeline -e parser
 *
 * @author sam.liux
 * @date 2021/02/20
 */
public class BeeLineCommandSqlTranslator extends AbstractCommandSqlTranslator {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeeLineCommandSqlTranslator.class);

    public static Pattern BEELINE_PATTERN = Pattern.compile("[\\s|\\n|\\t]*beeline.*");

    @Override
    public boolean match(DwWorkflow workflow, DwNode node) {
        return super.match(workflow, node) && StringUtils.contains(node.getCode(), " -e ");
    }

    @Override
    protected Pattern getCommandPrefixPattern() {
        return BEELINE_PATTERN;
    }

    @Override
    protected String getSqlOption() {
        return "e";
    }

    @Override
    protected Options getGnuOptions() {
        Options options = new Options();

        options.addOption(OptionBuilder
            .hasArg()
            .withArgName("driver class")
            .withDescription("The driver class to use")
            .create('d'));

        // -u <database url>
        options.addOption(OptionBuilder
            .hasArg()
            .withArgName("database url")
            .withDescription("The JDBC URL to connect to")
            .create('u'));

        // -c <named url in the beeline-hs2-connection.xml>
        options.addOption(OptionBuilder
            .hasArg()
            .withArgName("named JDBC URL in beeline-site.xml")
            .withDescription("The named JDBC URL to connect to, which should be present in "
                + "beeline-site.xml as the value of beeline.hs2.jdbc.url.<namedUrl>")
            .create('c'));

        // -r
        options.addOption(OptionBuilder
            .withLongOpt("reconnect")
            .withDescription("Reconnect to last saved connect url (in conjunction with !save)")
            .create('r'));

        // -n <username>
        options.addOption(OptionBuilder
            .hasArg()
            .withArgName("username")
            .withDescription("The username to connect as")
            .create('n'));

        // -p <password>
        options.addOption(OptionBuilder
            .hasArg()
            .withArgName("password")
            .withDescription("The password to connect as")
            .hasOptionalArg()
            .create('p'));

        // -w (or) --password-file <file>
        options.addOption(OptionBuilder
            .hasArg()
            .withArgName("password-file")
            .withDescription("The password file to read password from")
            .withLongOpt("password-file")
            .create('w'));

        // -a <authType>
        options.addOption(OptionBuilder
            .hasArg()
            .withArgName("authType")
            .withDescription("The authentication type")
            .create('a'));

        // -i <init file>
        options.addOption(OptionBuilder
            .hasArgs()
            .withArgName("init")
            .withDescription("The script file for initialization")
            .create('i'));

        // -e <query>
        options.addOption(OptionBuilder
            .hasArgs()
            .withArgName("query")
            .withDescription("The query that should be executed")
            .create('e'));

        // -f <script file>
        options.addOption(OptionBuilder
            .hasArg()
            .withArgName("file")
            .withDescription("The script file that should be executed")
            .create('f'));

        // -help
        options.addOption(OptionBuilder
            .withLongOpt("help")
            .withDescription("Display this message")
            .create('h'));

        // Substitution option --hivevar
        options.addOption(OptionBuilder
            .withValueSeparator()
            .hasArgs(2)
            .withArgName("key=value")
            .withLongOpt("hivevar")
            .withDescription("Hive variable name and value")
            .create());

        // hive conf option --hiveconf
        options.addOption(OptionBuilder
            .withValueSeparator()
            .hasArgs(2)
            .withArgName("property=value")
            .withLongOpt("hiveconf")
            .withDescription("Use value for given property")
            .create());

        // --property-file <file>
        options.addOption(OptionBuilder
            .hasArg()
            .withLongOpt("property-file")
            .withDescription("The file to read configuration properties from")
            .create());

        return options;
    }
}
