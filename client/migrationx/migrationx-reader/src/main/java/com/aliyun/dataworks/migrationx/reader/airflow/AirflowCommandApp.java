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

package com.aliyun.dataworks.migrationx.reader.airflow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aliyun.dataworks.client.command.CommandApp;
import com.google.common.base.Joiner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class AirflowCommandApp extends CommandApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(AirflowCommandApp.class);

    private static final String OPT_DAG_FOLDER = "d";
    private static final String OPT_DAG_FOLDER_LONG = "dagFolder";
    private static final String OPT_OUTPUT_FILE = "o";
    private static final String OPT_OUTPUT_FILE_LONG = "outputFile";
    private static final String OPT_HELP = "h";
    private static final String OPT_HELP_LONG = "help";

    public static void main(String[] args) throws ParseException, IOException {
        AirflowCommandApp app = new AirflowCommandApp();
        app.run(args);
    }

    private void runCommand(File workingDir, String... command) throws IOException {
        LOGGER.info("run command: {}", Joiner.on(" ").join(command));
        try {
            ProcessBuilder pb = new ProcessBuilder().directory(workingDir).command(command);
            File stdoutLog = new File(workingDir, "stdout.log");
            File stderrLog = new File(workingDir, "stderr.log");
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(pb.redirectOutput());
            Process pro = pb.start();
            pro.waitFor();
            if (pro.exitValue() == 0) {
                LOGGER.info("run command completed");
            } else {
                LOGGER.error("run command failed");
            }
        } catch (InterruptedException e) {
            LOGGER.error("run command interrupted: ", e);
        }
    }

    @Override
    public void run(String[] args) {
        try {
            Options options = new Options();
            options.addOption(OPT_DAG_FOLDER, OPT_DAG_FOLDER_LONG, true, "dag folder or dag file");
            options.addOption(OPT_OUTPUT_FILE, OPT_OUTPUT_FILE_LONG, true, "output.json");
            options.addOption(OPT_HELP, OPT_HELP_LONG, false, "show help.");

            CommandLineParser parser = new DefaultParser();
            CommandLine cli = parser.parse(options, args);
            HelpFormatter helpFormatter = new HelpFormatter();
            if (cli.hasOption(OPT_HELP)) {
                helpFormatter.printHelp("Options", options);
                System.exit(0);
            }

            if (!cli.hasOption(OPT_DAG_FOLDER)) {
                helpFormatter.printHelp("Options", options);
                LOGGER.error("Option needed: {}", options.getOption(OPT_DAG_FOLDER).toString());
                System.exit(-1);
            }
            if (!cli.hasOption(OPT_OUTPUT_FILE)) {
                helpFormatter.printHelp("Options", options);
                LOGGER.error("Option needed: {}", options.getOption(OPT_OUTPUT_FILE).toString());
                System.exit(-1);
            }

            File workingDir = new File(System.getProperty("currentDir"));
            File bin = new File(workingDir, Joiner.on(File.separator).join(
                "lib", "migrationx-reader-airflow", "target",
                "migrationx-reader", "python", "airflow-exporter", "parser"
            ));

            List<String> pythonArgs = new ArrayList<>();
            pythonArgs.add("python");
            pythonArgs.add(bin.getAbsolutePath());
            pythonArgs.addAll(Arrays.asList(args));
            LOGGER.info("args: {}", Arrays.asList(args));
            runCommand(workingDir, pythonArgs.toArray(new String[0]));
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
