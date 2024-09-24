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

package com.aliyun.dataworks.client.toolkits.command;

import com.aliyun.dataworks.client.command.CommandApp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Spec Command App
 *
 * @author 聿剑
 * @date 2024/9/18
 */
@Slf4j
public class SpecCommandApp extends CommandApp {

    public static void main(String[] args) throws Exception {
        SpecCommandApp app = new SpecCommandApp();
        app.run(args);
    }

    @Override
    protected void doCommandRun(Options options, CommandLine cli, String[] args) {
        log.info("===== start spec command =====");

        log.info("===== finish spec command =====");
    }

    @Override
    protected Options getOptions() {
        Options options = new Options();
        options.addRequiredOption("t", "type", true,
            "Object type");
        options.addRequiredOption("o", "operation", true, "Operation of specified object");
        return options;
    }
}
