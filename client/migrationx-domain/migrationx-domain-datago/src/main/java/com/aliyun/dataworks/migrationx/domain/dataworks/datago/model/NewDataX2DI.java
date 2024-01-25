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

package com.aliyun.dataworks.migrationx.domain.dataworks.datago.model;

import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.DITask;
import com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian.constant.DataXConstants;
import com.google.gson.JsonObject;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description DataGO新版同步任务
 * @createTime 2020-04-15
 */
@Data
public class NewDataX2DI {
    private String type;
    private String dataSyncType;
    private Content content;

    @Data
    public static class Content {
        private Step reader;
        private Step writer;
        private DITask.Setting dataxSetting;
    }

    @Data
    private static class Step {
        private String type;
        private String datasourceName;
        private String datasourceType;
        private JsonObject parameter;
    }

    public DITask.Step toReader() {
        DITask.Step step = new DITask.Step(this.content.reader.type, this.content.reader.parameter,
            this.content.reader.type, DataXConstants.DI_READER);
        step.getParameter().addProperty(DataXConstants.DI_DATASOURCE, this.content.reader.datasourceName);
        processGuidTable(step);
        return step;
    }

    private void processGuidTable(DITask.Step step) {
        if (step.getParameter().has("guid")) {
            if (!step.getParameter().has("table") || StringUtils.isBlank(
                step.getParameter().get("table").getAsString())) {
                String guid = step.getParameter().get("guid").getAsString();
                String[] tokens = StringUtils.split(guid, ".");
                if (tokens != null && tokens.length > 0) {
                    step.getParameter().addProperty("table", tokens[tokens.length - 1]);
                }
            }
            step.getParameter().remove("guid");
        }
    }

    public DITask.Step toWriter() {
        DITask.Step step = new DITask.Step(this.content.writer.type, this.content.writer.parameter,
            this.content.writer.type, DataXConstants.DI_WRITER);
        step.getParameter().addProperty(DataXConstants.DI_DATASOURCE, content.writer.datasourceName);
        processGuidTable(step);
        return step;
    }
}
