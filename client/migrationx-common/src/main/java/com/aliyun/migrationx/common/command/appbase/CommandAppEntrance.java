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

package com.aliyun.migrationx.common.command.appbase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.common.base.Joiner;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author 聿剑
 * @date 2022/10/20
 */
@Slf4j
public class CommandAppEntrance {
    public static void main(String[] args) throws IOException {
        String currentDir = System.getProperty("currentDir");
        AppType appType = AppType.valueOf(System.getProperty("appType"));

        Map<AppType, Map<String, AppMeta>> apps = loadApps(Joiner.on(File.separator).join(currentDir, "conf"));

        Options options = new Options();
        options.addRequiredOption("a", "app", true, "app name");

        HelpFormatter helpFormatter = new HelpFormatter();
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, args, true);
            String appName = commandLine.getOptionValue("a");

            if (!apps.get(appType).containsKey(appName)) {
                throw new BizException(ErrorCode.UNKNOWN_COMMAND_APP).with(appName);
            }

            CommandApp app = CommandAppFactory.create(appType, appName);
            app.setAppMeta(apps.get(appType).get(appName));
            log.info("start running app {} with args: {}", appName, commandLine.getArgs());
            app.run(commandLine.getArgs());
            log.info("app command success");
        } catch (ParseException e) {
            log.error("app command parse error: {}", e.getMessage());
            String footer = Joiner.on(" ").join("\nAvailable apps: \n", GsonUtils.toJsonString(CommandAppFactory.getApps()));
            helpFormatter.printHelp("Options", "migrationx", options, footer);
        } catch (Exception e) {
            log.error("app command failed: ", e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<AppType, Map<String, AppMeta>> loadApps(String conf) throws IOException {
        File config = new File(conf);
        File appsJson = config;
        if (config.isDirectory()) {
            appsJson = new File(config, "apps.json");
        }

        String json = FileUtils.readFileToString(appsJson, StandardCharsets.UTF_8);
        Map<AppType, Map<String, AppMeta>> apps = GsonUtils.fromJsonString(json, new TypeToken<Map<AppType, Map<String, AppMeta>>>() {}.getType());
        MapUtils.emptyIfNull(apps).forEach((appType, map) -> MapUtils.emptyIfNull(map).forEach((appName, appMeta) -> {
            try {
                appMeta.setName(appName);
                appMeta.setType(appType);
                log.info("register command app type: {}, name: {}, class: {}", appType, appName, appMeta.getAppClass());
                CommandAppFactory.register(appType, appName, (Class<? extends CommandApp>) Class.forName(appMeta.getAppClass()));
            } catch (ClassNotFoundException e) {
                log.info("register command app failed, appType: {}, appName: {}, class: {}, error: {}",
                        appType, appName, appMeta.getAppClass(), e);
            }
        }));

        log.info("apps map: {}", GsonUtils.toJsonString(apps));
        return apps;
    }
}
