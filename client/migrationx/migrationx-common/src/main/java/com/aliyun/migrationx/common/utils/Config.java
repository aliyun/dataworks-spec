package com.aliyun.migrationx.common.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Config {
    public static Config INSTANCE = new Config();

    public static void init(Config config) {
        INSTANCE = config;
    }

    /**
     * transformer
     */
    private boolean skipUnSupportType = false;

    private boolean transformContinueWithError = false;

    /**
     * spec continue when error with some spec
     */
    private boolean specContinueWithError = false;

    private List<String> skipTypes = new ArrayList<>();

    private List<String> skipTaskCodes = new ArrayList<>();
    private List<String> tempTaskTypes = new ArrayList<>();

    private boolean zipSpec = true;

    /**
     * filter transform task by list
     *
     * {projectName}.{processName}.{taskName}
     * '*' for all
     */
    private List<String> filterTasks = new ArrayList<>();
}
