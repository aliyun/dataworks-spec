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

package com.aliyun.dataworks.migrationx.transformer.core.loader;

import com.aliyun.dataworks.migrationx.transformer.core.controller.Task;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * load config properties
 *
 * @author sam.liux
 * @date 2019/07/03
 */
public class ConfigPropertiesLoader extends Task<Properties> {
    private String configPropertiesPath;

    public ConfigPropertiesLoader(String configPropertiesPath) {
        super(ConfigPropertiesLoader.class.getSimpleName());
        this.configPropertiesPath = configPropertiesPath;
    }

    public ConfigPropertiesLoader(String configPropertiesPath, String name, Task... dependsOn) {
        super(name, dependsOn);
        this.configPropertiesPath = configPropertiesPath;
    }

    @Override
    public Properties call() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(configPropertiesPath)));
        return properties;
    }
}