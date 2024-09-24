/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.app;

import com.aliyun.dataworks.client.command.CommandApp;
import com.aliyun.dataworks.migrationx.transformer.flowspec.transformer.dolphinscheduler.DolphinSchedulerV3FlowSpecTransformer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-26
 */
@Slf4j
public class DolphinSchedulerV3FlowSpecTransformerApp extends CommandApp {

    public static void main(String[] args) throws Exception {
        new DolphinSchedulerV3FlowSpecTransformerApp().run(args);
    }

    /**
     * run app
     *
     * @param args String[]
     */
    @Override
    public void run(String[] args) throws Exception {
        Options options = getOptions();

        // read command line args
        CommandLine commandLine = getCommandLine(options, args);
        String configPath = commandLine.getOptionValue("c");
        String sourcePath = commandLine.getOptionValue("s");
        String targetPath = commandLine.getOptionValue("t");

        // do transformer
        log.info("======start transformer======");
        new DolphinSchedulerV3FlowSpecTransformer(configPath, sourcePath, targetPath).transform();
        log.info("======finish transformer======");
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
