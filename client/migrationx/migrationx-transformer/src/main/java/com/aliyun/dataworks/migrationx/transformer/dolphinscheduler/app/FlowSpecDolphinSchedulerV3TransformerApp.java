/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.app;

import com.aliyun.dataworks.client.command.CommandApp;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.transformer.flowspec.FlowSpecDolphinSchedulerV3Transformer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-05
 */
public class FlowSpecDolphinSchedulerV3TransformerApp extends CommandApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowSpecDolphinSchedulerV3TransformerApp.class);

    public static void main(String[] args) throws Exception {
        new FlowSpecDolphinSchedulerV3TransformerApp().run(args);
    }

    /**
     * run app
     *
     * @param args String[]
     */
    @Override
    public void run(String[] args) throws Exception {
        Options options = getOptions();

        CommandLine commandLine = getCommandLine(options, args);

        String configPath = commandLine.getOptionValue("c");
        String sourcePath = commandLine.getOptionValue("s");
        String targetPath = commandLine.getOptionValue("t");
        LOGGER.info("======start transformer======");
        new FlowSpecDolphinSchedulerV3Transformer(configPath, sourcePath, targetPath).transform();
        LOGGER.info("======finish transformer======");
    }

    @Override
    protected Options getOptions() {
        Options options = new Options();
        options.addRequiredOption("c", "config", true, "transform configuration file path");
        options.addRequiredOption("s", "source", true, "source file path");
        options.addRequiredOption("t", "target", true, "target file path");
        return options;
    }
}
