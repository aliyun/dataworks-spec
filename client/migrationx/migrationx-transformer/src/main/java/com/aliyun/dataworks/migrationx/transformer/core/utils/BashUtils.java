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

package com.aliyun.dataworks.migrationx.transformer.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sam.liux
 * @date 2019/05/14
 */
public class BashUtils {
    public static List<String> extractSingleLineCommands(String script, String[] patterns, String splitRegex) {
        List<String> commands = new ArrayList<>();
        String[] tokens = script.split(splitRegex);
        for (String token : tokens) {
            for (String regex : patterns) {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(token);
                while (matcher.find()) {
                    String g = matcher.group();
                    commands.add(g.trim());
                }
            }
        }
        return commands;
    }
}
