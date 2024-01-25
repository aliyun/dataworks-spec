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

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2022/10/20
 */
@Slf4j
public abstract class CommandApp {
    protected AppMeta appMeta;

    /**
     * run app
     *
     * @param args String[]
     */
    public abstract void run(String[] args) throws Exception;

    /**
     * set app meta
     *
     * @param appMeta AppMeta
     */
    public void setAppMeta(AppMeta appMeta) {
        this.appMeta = appMeta;
    }

    protected CommandLine getCommandLine(Options options, String[] args) throws Exception {
        HelpFormatter helpFormatter = new HelpFormatter();
        try {
            CommandLineParser parser = new DefaultParser();
            return parser.parse(options, args, true);
        } catch (ParseException e) {
            log.error("parser command error: {}", e.getMessage());
            helpFormatter.printHelp("Options: ", options);
            System.exit(-1);
        }
        throw new RuntimeException("Command line parse failed");
    }

    protected Options getOptions() {
        return new Options();
    }

    protected Locale getLocale() {
        String lang = Optional.ofNullable(System.getenv("LANG")).orElse("zh_CN.UTF-8");
        String locale = StringUtils.split(lang, ".")[0];
        return Arrays.stream(Locale.getAvailableLocales())
            .filter(l -> StringUtils.equalsIgnoreCase(locale, l.toString())).findFirst()
            .orElse(Locale.US);
    }
}
