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

package com.aliyun.dataworks.migrationx.reader.dolphinscheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aliyun.migrationx.common.command.appbase.CommandApp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DolphinScheduler Exporter
 */
@Slf4j
public class DolphinSchedulerCommandApp extends CommandApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(DolphinSchedulerCommandApp.class);
    private static final String LONG_OPT_SKIP_RESOURCES = "skip-resources";
    private static final String OPT_SKIP_RESOURCES = "sr";

    @Override
    public void run(String[] args) {
        Options options = new Options();
        options.addRequiredOption("e", "endpoint", true,
                "DolphinScheduler Web API endpoint url, example: http://123.123.123.12:12343");
        options.addRequiredOption("t", "token", true, "DolphinScheduler API token");
        options.addRequiredOption("v", "version", true, "DolphinScheduler version");
        options.addOption("p", "projects", true, "DolphinScheduler project names, example: project_a,project_b,project_c");
        options.addOption("f", "file", true, "Output zip file");
        options.addOption(OPT_SKIP_RESOURCES, LONG_OPT_SKIP_RESOURCES, true, "skip exporting resources");

        HelpFormatter helpFormatter = new HelpFormatter();
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, args);
            String endpoint = commandLine.getOptionValue("e");
            String token = commandLine.getOptionValue("t");
            String version = commandLine.getOptionValue("v");
            List<String> projects = StringUtils.isBlank(commandLine.getOptionValue("p")) ?
                    new ArrayList<>() : Arrays.asList(StringUtils.split(commandLine.getOptionValue("p"), ","));
            if (CollectionUtils.isEmpty(projects)) {
                log.error("dolphinscheduler project not specified");
                System.exit(-1);
            }

            String file = commandLine.getOptionValue("f", "output");
            DolphinSchedulerReader exporter = new DolphinSchedulerReader(
                    endpoint, token, version, projects, new File(new File(file).getAbsolutePath()));
            String sr = commandLine.getOptionValue(OPT_SKIP_RESOURCES, "true");
            exporter.setSkipResources(Boolean.parseBoolean(sr));
            File exportedFile = exporter.export();
            LOGGER.info("exported file: {}", exportedFile);
        } catch (ParseException e) {
            LOGGER.error("parser command error: {}", e.getMessage());
            helpFormatter.printHelp("Options", options);
            System.exit(-1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
