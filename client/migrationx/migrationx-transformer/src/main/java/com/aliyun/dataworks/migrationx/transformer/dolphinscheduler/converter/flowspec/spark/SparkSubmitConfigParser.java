/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.spark;

import java.util.List;
import java.util.Objects;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.spark.SparkParameters;
import com.aliyun.dataworks.migrationx.transformer.core.spark.command.SparkLauncher;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Desc: This is a parser that writes spark runtime configuration to spark parameter
 *
 * @author 莫泣
 * @date 2024-07-08
 */
public class SparkSubmitConfigParser {

    private final List<String> configs;

    private final SparkParameters sparkParameters;

    public SparkSubmitConfigParser(SparkParameters sparkParameters, List<String> configs) {
        this.configs = configs;
        this.sparkParameters = sparkParameters;
    }

    /**
     * default config
     */
    private void init() {
        if (Objects.nonNull(sparkParameters)) {
            sparkParameters.setDriverCores(1);
            sparkParameters.setNumExecutors(2);
            sparkParameters.setExecutorCores(2);
            sparkParameters.setDriverMemory("512M");
            sparkParameters.setExecutorMemory("2G");
        }
    }

    public void parse() {
        init();
        ListUtils.emptyIfNull(configs).stream()
            .filter(StringUtils::isNotBlank)
            .forEach(config -> {
                String[] split = config.trim().split("=");
                if (split.length == 2) {
                    handle(sparkParameters, split[0], split[1]);
                }
            });
    }

    private void handle(SparkParameters sparkParameters, String key, String value) {
        switch (key) {
            case SparkLauncher.DRIVER_CORES:
                sparkParameters.setDriverCores(Integer.parseInt(value));
                break;
            case SparkLauncher.EXECUTOR_INSTANCES:
                sparkParameters.setNumExecutors(Integer.parseInt(value));
                break;
            case SparkLauncher.EXECUTOR_CORES:
                sparkParameters.setExecutorCores(Integer.parseInt(value));
                break;
            case SparkLauncher.DRIVER_MEMORY:
                sparkParameters.setDriverMemory(value);
                break;
            case SparkLauncher.EXECUTOR_MEMORY:
                sparkParameters.setExecutorMemory(value);
                break;
            case SparkLauncher.DRIVER_EXTRA_JAVA_OPTIONS:
            case SparkLauncher.EXECUTOR_EXTRA_JAVA_OPTIONS:
                appendConf(sparkParameters, key, value);
                break;
            default:
                break;
        }
    }

    /**
     * add other config
     *
     * @param sparkParameters sparkParameters
     * @param key             config key
     * @param value           config value
     */
    private void appendConf(SparkParameters sparkParameters, String key, String value) {
        String others = sparkParameters.getOthers();
        others = others + " --conf \"" + key + "=" + value + "\"";
        sparkParameters.setOthers(others);
    }
}
