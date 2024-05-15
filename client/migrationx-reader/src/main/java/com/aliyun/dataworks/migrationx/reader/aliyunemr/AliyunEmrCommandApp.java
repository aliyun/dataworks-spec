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

package com.aliyun.dataworks.migrationx.reader.aliyunemr;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

import com.aliyun.dataworks.migrationx.domain.dataworks.aliyunemr.AliyunEmrExportRequest;
import com.aliyun.dataworks.migrationx.domain.dataworks.aliyunemr.AliyunEmrExporterConstants;
import com.aliyun.dataworks.migrationx.domain.dataworks.aliyunemr.AliyunEmrService;
import com.aliyun.migrationx.common.command.appbase.CommandApp;
import com.aliyun.migrationx.common.utils.ZipUtils;
import com.aliyuncs.exceptions.ClientException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 阿里云EMR工作流导出工具
 *
 * @author sam.liux
 * @date 2019/06/27
 */
public class AliyunEmrCommandApp extends CommandApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(AliyunEmrCommandApp.class);

    private static final String OPT_ACCESS_ID = "i";
    private static final String OPT_ACCESS_ID_LONG = "accessId";
    private static final String OPT_ACCESS_KEY = "k";
    private static final String OPT_ACCESS_KEY_LONG = "accessKey";
    private static final String OPT_ENDPOINT = "e";
    private static final String OPT_ENDPOINT_LONG = "endpoint";
    private static final String OPT_REGION_ID = "r";
    private static final String OPT_REGION_ID_LONG = "regionId";
    private static final String OPT_EXPORT_FILE = "f";
    private static final String OPT_EXPORT_DIR_LONG = "exportDirectory";
    private static final String OPT_HELP = "h";
    private static final String OPT_HELP_LONG = "help";
    private static final String OPT_PROJECTS = "p";
    private static final String OPT_PROJECTS_LONG = "projects";
    private static final String OPT_FOLDER_FILTER = "ff";
    private static final String OPT_FOLDER_FILTER_LONG = "folderFilter";

    public static void main(String[] args) throws ParseException, IOException, ClientException {
        Options options = new Options();
        options.addOption(OPT_ACCESS_ID, OPT_ACCESS_ID_LONG, true, "aliyun access id");
        options.addOption(OPT_ACCESS_KEY, OPT_ACCESS_KEY_LONG, true, "aliyun access key");
        options.addOption(OPT_ENDPOINT, OPT_ENDPOINT_LONG, true, "pop sdk endpoint domain");
        options.addOption(OPT_REGION_ID, OPT_REGION_ID_LONG, true, "region id, cn-shanghai etc.");
        options.addOption(OPT_PROJECTS, OPT_PROJECTS_LONG, true, "emr project, multiply projects separated by comma, prj_01,prj_02 etc.");
        options.addOption(OPT_EXPORT_FILE, OPT_EXPORT_DIR_LONG, true, "/home/admin/emr_dumps/aaa.zip etc.");
        options.addOption(OPT_FOLDER_FILTER, OPT_FOLDER_FILTER_LONG, true, "/FLOW/folder1/folder2");
        options.addOption(OPT_HELP, OPT_HELP_LONG, false, "show help.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cli = parser.parse(options, args);
        HelpFormatter helpFormatter = new HelpFormatter();
        if (cli.hasOption(OPT_HELP)) {
            helpFormatter.printHelp("Options", options);
            System.exit(0);
        }

        if (!cli.hasOption(OPT_ACCESS_ID)) {
            helpFormatter.printHelp("Options", options);
            LOGGER.error("Option needed: {}", options.getOption(OPT_ACCESS_ID).toString());
            System.exit(-1);
        }
        if (!cli.hasOption(OPT_ACCESS_KEY)) {
            helpFormatter.printHelp("Options", options);
            LOGGER.error("Option needed: {}", options.getOption(OPT_ACCESS_KEY).toString());
            System.exit(-1);
        }
        if (!cli.hasOption(OPT_ENDPOINT)) {
            helpFormatter.printHelp("Options", options);
            LOGGER.error("Option needed: {}", options.getOption(OPT_ENDPOINT).toString());
            System.exit(-1);
        }
        if (!cli.hasOption(OPT_REGION_ID)) {
            helpFormatter.printHelp("Options", options);
            LOGGER.error("Option needed: {}", options.getOption(OPT_REGION_ID).toString());
            System.exit(-1);
        }

        String accessId = cli.getOptionValue(OPT_ACCESS_ID);
        String accessKey = cli.getOptionValue(OPT_ACCESS_KEY);
        String endpoint = cli.getOptionValue(OPT_ENDPOINT);
        String regionId = cli.getOptionValue(OPT_REGION_ID);
        String projects = cli.getOptionValue(OPT_PROJECTS);
        String exportFile = cli.getOptionValue(OPT_EXPORT_FILE, new File("./emr_dumps/").getAbsolutePath());
        String folderFilter = cli.getOptionValue(OPT_FOLDER_FILTER);
        AliyunEmrService client = new AliyunEmrService(accessId, accessKey, endpoint, regionId);

        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(AliyunEmrExporterConstants.EXPORTER_OUTPUT_DIR_DATE_FORMAT);
        String dirName = dateTimeFormatter.format(new Date());
        File folder = new File(new File("."), dirName);
        LOGGER.info("workspace folder: {}", folder);
        AliyunEmrExportRequest exportRequest = new AliyunEmrExportRequest();
        exportRequest.setFolder(folder);
        exportRequest.setFolderFilter(folderFilter);
        if (StringUtils.isNotBlank(projects)) {
            String[] tokens = StringUtils.split(projects, ",");
            if (tokens != null) {
                exportRequest.setProjects(Arrays.stream(tokens).filter(Objects::nonNull).collect(Collectors.toList()));
            }
        }

        client.dump(exportRequest);

        folder.mkdirs();
        File zipFile = new File(new File("."), exportFile);
        File f = ZipUtils.zipDir(folder.getAbsoluteFile(), zipFile);
        LOGGER.info("exported file to: {}", f);
    }

    @Override
    public void run(String[] args) {
        try {
            main(args);
        } catch (ParseException | IOException | ClientException e) {
            throw new RuntimeException(e);
        }
    }
}