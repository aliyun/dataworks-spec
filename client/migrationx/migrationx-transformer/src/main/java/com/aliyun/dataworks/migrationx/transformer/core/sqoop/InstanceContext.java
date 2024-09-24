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

package com.aliyun.dataworks.migrationx.transformer.core.sqoop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InstanceContext {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(InstanceContext.class);

    // reader
    public static String READER_PLUGIN_PATH = "configuration.reader.plugin";
    public static String READER_PLUGIN_PARAMETER_PATH = "configuration.reader.parameter";

    // writer
    public static String WRITER_PLUGIN_PATH = "configuration.writer.plugin";
    public static String WRITER_PLUGIN_PARAMETER_PATH = "configuration.writer.parameter";

    // setting
    public static String SETTING_PATH = "configuration.setting";

    // transformer
    public static String TRANSFORMER_PATH = "configuration.transformer";

    // extra
    public static String EXTEND_PATH = "extend";

    public static String ID_PATH = "id";
    public static String TYPE_PATH = "type";
    public static String TRACEID_PATH = "traceId";
    public static String VERSION_PATH = "version";

    private DIJsonProcessor diCode;

    public InstanceContext(String diCodeInJsonString) {
        this.diCode = DIJsonProcessor.from(diCodeInJsonString);
    }

    public String getReaderPlugin() {
        return this.diCode.getString(InstanceContext.READER_PLUGIN_PATH);
    }

    public void setReaderPlugin(String pluginName) {
        this.diCode.set(InstanceContext.READER_PLUGIN_PATH, pluginName);
    }

    public DIJsonProcessor getReaderParameter() {
        return this.diCode
            .getConfiguration(InstanceContext.READER_PLUGIN_PARAMETER_PATH);
    }

    public DIJsonProcessor setReaderParameter(Object parameter) {
        this.diCode.set(InstanceContext.READER_PLUGIN_PARAMETER_PATH,
            parameter);
        return this.diCode;
    }

    public String getWriterPlugin() {
        return this.diCode.getString(InstanceContext.WRITER_PLUGIN_PATH);
    }

    public void setWriterPlugin(String pluginName) {
        this.diCode.set(InstanceContext.WRITER_PLUGIN_PATH, pluginName);
    }

    public DIJsonProcessor getWriterParameter() {
        return this.diCode
            .getConfiguration(InstanceContext.WRITER_PLUGIN_PARAMETER_PATH);
    }

    public DIJsonProcessor setWriterParameter(Object parameter) {
        this.diCode.set(InstanceContext.WRITER_PLUGIN_PARAMETER_PATH,
            parameter);
        return this.diCode;
    }

    public DIJsonProcessor getSetting() {
        return this.diCode.getConfiguration(InstanceContext.SETTING_PATH);
    }

    public DIJsonProcessor setSetting(Object parameter) {
        this.diCode.set(InstanceContext.SETTING_PATH, parameter);
        return this.diCode;
    }

    public List<DIJsonProcessor> getTransformer() {
        return this.diCode
            .getListConfiguration(InstanceContext.TRANSFORMER_PATH);
    }

    public DIJsonProcessor getExtend() {
        return this.diCode.getConfiguration(InstanceContext.EXTEND_PATH);
    }

    public String getId() {
        return this.diCode.getString(InstanceContext.ID_PATH);
    }

    public String getType() {
        return this.diCode.getString(InstanceContext.TYPE_PATH);
    }

    public String getTraceId() {
        return this.diCode.getString(InstanceContext.TRACEID_PATH);
    }

    public String getVersion() {
        return this.diCode.getString(InstanceContext.VERSION_PATH);
    }

    public Object get(String path) {
        return this.diCode.get(path);
    }

    public void set(String path, Object value) {
        this.diCode.set(path, value);
    }

    public DIJsonProcessor getDiCode() {
        return this.diCode;
    }

    public boolean isList(String path) {
        return this.diCode.get(path) instanceof List;
    }

    public boolean isString(String path) {
        return this.diCode.get(path) instanceof String;
    }

    @Override
    public String toString() {
        return this.diCode.toJSON();
    }

    public class ReaderContext {
        private DIJsonProcessor code;

        public DIJsonProcessor getCode() {
            return code;
        }

        public void setCode(DIJsonProcessor code) {
            this.code = code;
        }

        public ReaderContext(DIJsonProcessor code) {
            this.code = code;
        }
    }

    public class WriterContext {
        private DIJsonProcessor code;

        public DIJsonProcessor getCode() {
            return code;
        }

        public void setCode(DIJsonProcessor code) {
            this.code = code;
        }

        public WriterContext(DIJsonProcessor code) {
            this.code = code;
        }
    }

    public class SettingContext {
        private DIJsonProcessor code;

        public DIJsonProcessor getCode() {
            return code;
        }

        public void setCode(DIJsonProcessor code) {
            this.code = code;
        }

        public SettingContext(DIJsonProcessor code) {
            this.code = code;
        }
    }

    public class TransformerContext {
        private DIJsonProcessor code;

        public DIJsonProcessor getCode() {
            return code;
        }

        public void setCode(DIJsonProcessor code) {
            this.code = code;
        }

        public TransformerContext(DIJsonProcessor code) {
            this.code = code;
        }
    }

    public static String streamX2Cdp(String streamXJson) {
        String cdpSkeleton = "{\n" + "    \"configuration\": {\n"
            + "        \"reader\": {\n" + "            \"plugin\": \"\",\n"
            + "            \"parameter\": {}\n" + "        },\n"
            + "        \"writer\": {\n" + "            \"plugin\": \"\",\n"
            + "            \"parameter\": {}\n" + "        },\n"
            + "        \"setting\": {}\n" + "    }\n" + "}";
        DIJsonProcessor cdpCode = DIJsonProcessor.from(streamXJson);
        cdpCode = cdpCode.merge(DIJsonProcessor.from(cdpSkeleton), false);
        if (cdpCode.containsKey("steps")) {
            cdpCode.remove("steps");
        }

        DIJsonProcessor streamXCode = DIJsonProcessor.from(streamXJson);
        List<DIJsonProcessor> steps = streamXCode.getListConfiguration("steps");
        if (null != steps) {
            for (DIJsonProcessor eachPlugin : steps) {
                String category = eachPlugin.getString("category");
                String stepType = eachPlugin.getString("stepType");
                if ("reader".equalsIgnoreCase(category)) {
                    cdpCode.set(InstanceContext.READER_PLUGIN_PATH, stepType);
                    cdpCode.set(InstanceContext.READER_PLUGIN_PARAMETER_PATH,
                        eachPlugin.get("parameter"));
                } else if ("writer".equalsIgnoreCase(category)) {
                    cdpCode.set(InstanceContext.WRITER_PLUGIN_PATH, stepType);
                    cdpCode.set(InstanceContext.WRITER_PLUGIN_PARAMETER_PATH,
                        eachPlugin.get("parameter"));
                } else {
                    LOGGER.warn("found not support parameters, ignore: {}",
                        eachPlugin.toJSON());
                }
            }
        }
        if (streamXCode.containsKey("setting")) {
            cdpCode.set(InstanceContext.SETTING_PATH,
                streamXCode.get("setting"));
            streamXCode.remove("setting");
        }
        return cdpCode.toJSON();
    }
}