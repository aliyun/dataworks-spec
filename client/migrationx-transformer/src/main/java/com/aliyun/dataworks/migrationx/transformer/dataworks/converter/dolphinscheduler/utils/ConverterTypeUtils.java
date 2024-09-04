package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.utils;

import com.aliyun.migrationx.common.utils.JSONUtils;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

public class ConverterTypeUtils {

    public static String getConverterType(String convertType, String projectName, String processName, String taskName, String defaultType) {
        if (StringUtils.isEmpty(convertType)) {
            return defaultType;
        }

        boolean valid = JSONUtils.checkJsonValid(convertType);
        if (!valid) {
            return convertType;
        }

        JsonNode jsonNode = JSONUtils.parseObject(convertType);
        String taskIdentity = String.format("%s.%s.%s", projectName, processName, taskName);
        String processIdentity = String.format("%s.%s.*", projectName, processName);
        String projectIdentity = String.format("%s.*", projectName, processName);

        if (jsonNode.has(taskIdentity)) {
            return jsonNode.get(taskIdentity).asText();
        } else if (jsonNode.has(processIdentity)) {
            return jsonNode.get(processIdentity).asText();
        } else if (jsonNode.has(projectIdentity)) {
            return jsonNode.get(projectIdentity).asText();
        } else if (jsonNode.has("*")) {
            return jsonNode.get("*").asText();
        } else {
            return defaultType;
        }
    }
}
