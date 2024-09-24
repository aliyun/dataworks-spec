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

package com.aliyun.dataworks.migrationx.transformer.core.utils;import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwDatasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.connection.JdbcConnection;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.CodeModeType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.tenant.EnvType;
import com.aliyun.dataworks.migrationx.transformer.core.sqoop.DICode;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.aliyun.migrationx.common.utils.JdbcUtils;
import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sam.liux
 * @date 2020/12/23
 */
public class DiCodeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiCodeUtils.class);

    public static List<DwDatasource> processSqoopDatasource(DICode code) {
        String diCode = code.getCode();
        if (StringUtils.isBlank(diCode)) {
            return ListUtils.emptyIfNull(null);
        }

        List<DwDatasource> datasourceList = new ArrayList<>();
        JsonObject diJsonObject = GsonUtils.gson.fromJson(diCode, JsonObject.class);
        if (diJsonObject.has("steps")) {
            JsonArray steps = diJsonObject.get("steps").getAsJsonArray();
            if (steps != null && steps.size() > 0) {
                for (int i = 0; i < steps.size(); i++) {
                    JsonObject step = steps.get(i).getAsJsonObject();
                    if (!step.has("stepType")) {
                        continue;
                    }

                    String stepType = step.get("stepType").getAsString();
                    if ("mysql".equalsIgnoreCase(stepType) && step.has("parameter")) {
                        JsonObject parameter = step.get("parameter").getAsJsonObject();
                        JsonArray connections = parameter.has("connection") ?
                            parameter.get("connection").getAsJsonArray() : new JsonArray();
                        String password = parameter.has("password") ? parameter.get("password").getAsString() : "";
                        String username = parameter.has("username") ? parameter.get("username").getAsString() : "";
                        String jdbcUrl = null;
                        for (int j = 0; j < connections.size(); j++) {
                            JsonObject conn = connections.get(j).getAsJsonObject();
                            JsonArray jdbcUrls = conn.has("jdbcUrl") ? conn.get("jdbcUrl").getAsJsonArray()
                                : new JsonArray();
                            if (jdbcUrls.size() > 0) {
                                jdbcUrl = jdbcUrls.get(0).getAsString();
                            }
                        }
                        if (StringUtils.isNotBlank(jdbcUrl) && StringUtils.isNotBlank(username)
                            && StringUtils.isNotBlank(password)) {
                            DwDatasource dwDatasource = new DwDatasource();
                            dwDatasource.setName(JdbcUtils.getDbName(jdbcUrl));
                            JdbcConnection jdbcConnection = new JdbcConnection();
                            jdbcConnection.setJdbcUrl(jdbcUrl);
                            jdbcConnection.setUsername(username);
                            jdbcConnection.setPassword(password);
                            jdbcConnection.setTag("public");
                            dwDatasource.setConnection(GsonUtils.gson.toJson(jdbcConnection));
                            dwDatasource.setType("mysql");
                            dwDatasource.setEnvType(EnvType.PRD.name());
                            if (datasourceList.stream().noneMatch(d -> d.getName().equals(dwDatasource.getName()))) {
                                datasourceList.add(dwDatasource);
                            }

                            boolean datasourceSet = false;
                            for (int k = 0; k < connections.size(); k++) {
                                JsonObject conn = connections.get(k).getAsJsonObject();
                                JsonArray jdbcUrls = conn.has("jdbcUrl") ? conn.get("jdbcUrl").getAsJsonArray()
                                    : new JsonArray();
                                for (int n = 0; n < jdbcUrls.size(); n++) {
                                    if (jdbcUrl.equals(jdbcUrls.get(n).getAsString())) {
                                        conn.addProperty("datasource", dwDatasource.getName());
                                        parameter.addProperty("datasource", dwDatasource.getName());
                                        datasourceSet = true;
                                        break;
                                    }
                                }
                                if (datasourceSet) {
                                    conn.remove("jdbcUrl");
                                    connections.set(k, conn);
                                }
                            }
                            if (datasourceSet) {
                                parameter.remove("username");
                                parameter.remove("password");
                                JsonObject extend = diJsonObject.has("extend") ?
                                    diJsonObject.get("extend").getAsJsonObject() : new JsonObject();
                                extend.addProperty("mode", CodeModeType.WIZARD.getValue());
                                diJsonObject.add("extend", extend);
                            }
                        }
                        step.add("parameter", parameter);
                    }
                }
            }
            diJsonObject.add("steps", steps);
        }
        code.setCode(GsonUtils.gson.toJson(diJsonObject));
        return datasourceList;
    }

    public static String replaceDiCodeOdpsTablePrefix(String code, String newPrefix) {
        if (StringUtils.isBlank(code)) {
            return code;
        }

        try {
            JsonObject codeJson = GsonUtils.fromJsonString(code, JsonObject.class);
            String srcType = codeJson.has("srcType") ? codeJson.get("srcType").getAsString() : null;
            String srcGuid = codeJson.has("srcGuid") ? codeJson.get("srcGuid").getAsString() : null;
            String dstType = codeJson.has("dstType") ? codeJson.get("dstType").getAsString() : null;
            String dstGuid = codeJson.has("dstGuid") ? codeJson.get("dstGuid").getAsString() : null;
            if (StringUtils.equalsIgnoreCase("odps", srcType)) {
                srcGuid = replaceGuidOdpsProjectPrefix(newPrefix, srcGuid);
                codeJson.addProperty("srcGuid", srcGuid);
            }
            if (StringUtils.equalsIgnoreCase("odps", dstType)) {
                dstGuid = replaceGuidOdpsProjectPrefix(newPrefix, dstGuid);
                codeJson.addProperty("dstGuid", dstGuid);
            }

            // 旧版的DI任务
            if (codeJson.has("config") && codeJson.get("config").isJsonObject()) {
                JsonObject config = codeJson.get("config").getAsJsonObject();
                if (StringUtils.equalsIgnoreCase("odps", srcType)) {
                    JsonObject reader = config.has("reader") ? config.get("reader").getAsJsonObject()
                        : new JsonObject();
                    String guid = reader.has("guid") ? reader.get("guid").getAsString() : null;
                    guid = replaceGuidOdpsProjectPrefix(newPrefix, guid);
                    reader.addProperty("guid", guid);
                    config.remove("reader");
                    config.add("reader", reader);
                    codeJson.add("config", config);
                }

                if (StringUtils.equalsIgnoreCase("odps", dstType)) {
                    JsonObject writer = config.has("writer") ? config.get("writer").getAsJsonObject()
                        : new JsonObject();
                    String guid = writer.has("guid") ? writer.get("guid").getAsString() : null;
                    guid = replaceGuidOdpsProjectPrefix(newPrefix, guid);
                    writer.addProperty("guid", guid);
                    config.remove("writer");
                    config.add("writer", writer);
                    codeJson.add("config", config);
                }
            }

            // 新版的DI任务
            if (codeJson.has("steps")) {
                JsonArray steps = codeJson.get("steps").getAsJsonArray();
                if (steps != null && steps.size() > 0) {
                    for (int i = 0; i < steps.size(); i++) {
                        JsonObject step = steps.get(i).getAsJsonObject();
                        if (!step.has("stepType")) {
                            continue;
                        }

                        String stepType = step.get("stepType").getAsString();
                        if (StringUtils.equalsIgnoreCase("odps", stepType)) {
                            if (step.has("parameter")) {
                                JsonObject parameter = step.get("parameter").getAsJsonObject();
                                String guid = parameter.has("guid") ? parameter.get("guid").getAsString() : null;
                                guid = replaceGuidOdpsProjectPrefix(newPrefix, guid);
                                parameter.addProperty("guid", guid);
                            }
                        }
                    }
                }
            }
            return GsonUtils.defaultGson.toJson(codeJson);
        } catch (Exception e) {
            LOGGER.error("replace di code error, code: {}", code, e);
        }
        return code;
    }

    private static String replaceGuidOdpsProjectPrefix(String newPrefix, String guid) {
        if (StringUtils.startsWith(guid, "odps.")) {
            String[] parts = StringUtils.split(guid, ".");
            if (parts.length >= 3) {
                parts[1] = newPrefix;
                guid = Joiner.on(".").join(parts);
            }
        }
        return guid;
    }

    public static String getDiCodeOdpsTablePrefix(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }

        try {
            JsonObject codeJson = GsonUtils.fromJsonString(code, JsonObject.class);
            String srcType = codeJson.has("srcType") ? codeJson.get("srcType").getAsString() : null;
            String srcGuid = codeJson.has("srcGuid") ? codeJson.get("srcGuid").getAsString() : null;
            String dstType = codeJson.has("dstType") ? codeJson.get("dstType").getAsString() : null;
            String dstGuid = codeJson.has("dstGuid") ? codeJson.get("dstGuid").getAsString() : null;

            // 旧版的DI任务
            if (codeJson.has("config") && codeJson.get("config").isJsonObject()) {
                JsonObject config = codeJson.get("config").getAsJsonObject();
                JsonObject reader = config.has("reader") ? config.get("reader").getAsJsonObject() : new JsonObject();
                JsonObject writer = config.has("writer") ? config.get("writer").getAsJsonObject() : new JsonObject();

                String guid = null;
                if (StringUtils.equalsIgnoreCase("odps", srcType)) {
                    guid = reader.has("guid") ? reader.get("guid").getAsString() : null;
                }

                if (StringUtils.equalsIgnoreCase("odps", dstType)) {
                    guid = writer.has("guid") ? writer.get("guid").getAsString() : null;
                }

                if (StringUtils.isNotBlank(guid) && StringUtils.startsWith(guid, "odps.")) {
                    String[] parts = StringUtils.split(guid, ".");
                    if (parts.length >= 3) {
                        return parts[1];
                    }
                }
            }

            // 新版的DI任务
            if (codeJson.has("steps")) {
                JsonArray steps = codeJson.get("steps").getAsJsonArray();
                if (steps != null && steps.size() > 0) {
                    for (int i = 0; i < steps.size(); i++) {
                        JsonObject step = steps.get(i).getAsJsonObject();
                        if (!step.has("stepType")) {
                            continue;
                        }

                        String stepType = step.get("stepType").getAsString();
                        if (StringUtils.equalsIgnoreCase("odps", stepType)) {
                            if (step.has("parameter")) {
                                JsonObject parameter = step.get("parameter").getAsJsonObject();
                                String guid = parameter.has("guid") ? parameter.get("guid").getAsString() : null;
                                if (StringUtils.startsWith(guid, "odps.")) {
                                    String[] parts = StringUtils.split(guid, ".");
                                    if (parts.length >= 3) {
                                        return parts[1];
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("replace di code error, code: {}", code, e);
        }
        return null;
    }
}
