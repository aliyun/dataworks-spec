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

package com.aliyun.dataworks.common.spec.domain.dw.codemodel;

import java.util.List;

import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * {
 *     "mainPy": {
 *         "fileName": "spark.py",
 *         "fileId": 502824170,
 *         "cloudUuid": 700005553184
 *     },
 *     "assistPys": [
 *         {
 *             "fileName": "spark.py",
 *             "fileId": 502824170,
 *             "cloudUuid": 700005553184
 *         }
 *     ],
 *     "assistFiles": [
 *         {
 *             "fileName": "spark_file",
 *             "fileId": 502824175
 *         }
 *     ],
 *     "assistArchives": [
 *         {
 *             "fileName": "spark_arc.zip",
 *             "fileId": 502824230
 *         }
 *     ]
 * }
 * </pre>
 *
 * @author 聿剑
 * @date 2022/09/19
 */
public class OdpsSparkFileExtend {
    private static final Logger LOGGER = LoggerFactory.getLogger(OdpsSparkFileExtend.class);

    private OdpsSparkResource mainPy;
    private List<OdpsSparkResource> assistPys;
    private List<OdpsSparkResource> assistFiles;
    private List<OdpsSparkResource> assistArchives;

    public static class OdpsSparkResource {
        private String fileName;
        private Long fileId;
        private Long cloudUuid;

        public String getFileName() {
            return fileName;
        }

        public OdpsSparkResource setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Long getFileId() {
            return fileId;
        }

        public OdpsSparkResource setFileId(Long fileId) {
            this.fileId = fileId;
            return this;
        }

        public Long getCloudUuid() {
            return cloudUuid;
        }

        public OdpsSparkResource setCloudUuid(Long cloudUuid) {
            this.cloudUuid = cloudUuid;
            return this;
        }
    }

    public OdpsSparkResource getMainPy() {
        return mainPy;
    }

    public OdpsSparkFileExtend setMainPy(
        OdpsSparkResource mainPy) {
        this.mainPy = mainPy;
        return this;
    }

    public List<OdpsSparkResource> getAssistPys() {
        return assistPys;
    }

    public OdpsSparkFileExtend setAssistPys(
        List<OdpsSparkResource> assistPys) {
        this.assistPys = assistPys;
        return this;
    }

    public List<OdpsSparkResource> getAssistFiles() {
        return assistFiles;
    }

    public OdpsSparkFileExtend setAssistFiles(
        List<OdpsSparkResource> assistFiles) {
        this.assistFiles = assistFiles;
        return this;
    }

    public List<OdpsSparkResource> getAssistArchives() {
        return assistArchives;
    }

    public OdpsSparkFileExtend setAssistArchives(
        List<OdpsSparkResource> assistArchives) {
        this.assistArchives = assistArchives;
        return this;
    }

    public static OdpsSparkFileExtend fromFileExtend(String extend) {
        try {
            return GsonUtils.fromJsonString(extend, new TypeToken<OdpsSparkFileExtend>() {}.getType());
        } catch (Exception e) {
            LOGGER.warn("", e);
        }
        return null;
    }

    public String toJson() {
        return GsonUtils.toJsonString(this);
    }
}
