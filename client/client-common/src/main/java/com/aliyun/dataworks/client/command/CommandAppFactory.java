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

package com.aliyun.dataworks.client.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

/**
 * @author 聿剑
 * @date 2022/10/20
 */
@Slf4j
public class CommandAppFactory {
    private static final Map<AppType, Map<String, Class<? extends CommandApp>>> commandApps = new HashMap<>();

    public static void register(AppType appType, String appName, Class<? extends CommandApp> readerAppClz) {
        Map<String, Class<? extends CommandApp>> apps = commandApps.computeIfAbsent(appType, type -> new HashMap<>());
        if (apps.containsKey(appName)) {
            return;
        }

        apps.put(appName, readerAppClz);
    }

    @SuppressWarnings("unchecked")
    public static <T extends CommandApp> T create(AppType appType, String appName) throws InstantiationException, IllegalAccessException {
        if (!commandApps.containsKey(appType)) {
            throw new RuntimeException("unregistered app type: " + appType);
        }

        Map<String, Class<? extends CommandApp>> apps = commandApps.get(appType);
        if (!apps.containsKey(appName)) {
            log.error("appName: {} not found in apps: {}", appName, apps);
            throw new RuntimeException("unregistered app name: " + appName);
        }

        Class<? extends CommandApp> clz = apps.get(appName);
        return (T)clz.newInstance();
    }

    public static Map<AppType, List<String>> getApps() {
        return MapUtils.emptyIfNull(commandApps).entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, ent -> new ArrayList<>(ent.getValue().keySet())));
    }
}
