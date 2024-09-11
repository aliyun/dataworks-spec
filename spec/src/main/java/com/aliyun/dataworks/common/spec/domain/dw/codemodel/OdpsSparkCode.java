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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;

import com.google.common.base.Joiner;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Code mode for Odps Spark
 *
 * @author sam.liux
 * @date 2019/07/14
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class OdpsSparkCode extends AbstractBaseCode {
    public static final String SPARK_VERSION_2X = "2.x";
    public static final String SPARK_VERSION_1X = "1.x";
    public static final String SPARK_LANGUAGE_JAVA = "java";
    public static final String SPARK_LANGUAGE_PY = "python";

    private CodeJson sparkJson;

    @Override
    public OdpsSparkCode parse(String code) {
        if (StringUtils.isEmpty(code)) {
            return this;
        }

        List<String> resourceNames = Arrays.stream(code.trim().split("\n"))
                .filter(line -> line.matches("##@resource_reference\\{\"([^\\{|^\\}]+)\"\\}"))
                .map(line -> line.replace("##@resource_reference{\"", "").replace("\"}", ""))
                .collect(Collectors.toList());
        OdpsSparkCode model = new OdpsSparkCode();
        model.setResourceReferences(resourceNames);

        String json = Joiner.on("\\n").join(Arrays.stream(code.split("\n"))
                .filter(line -> !line.matches("##@resource_reference\\{\"([^\\{|^\\}]+)\"\\}"))
                .collect(Collectors.toList()));
        CodeJson codeJson = GsonUtils.gson.fromJson(json, new com.google.gson.reflect.TypeToken<CodeJson>() {}.getType());
        model.setSparkJson(codeJson);

        setResourceReferences(model.getResourceReferences());
        setSparkJson(model.getSparkJson());
        return this;
    }

    @Override
    public List<String> getProgramTypes() {
        return Collections.singletonList(CodeProgramType.ODPS_SPARK.name());
    }

    public static class CodeJson {
        private String version = SPARK_VERSION_2X;
        private String language;
        private String mainClass;
        private String args;
        private List<String> configs;
        private String mainJar;
        private String mainPy;
        private List<String> assistPys;
        private List<String> assistJars;
        private List<String> assistFiles;
        private List<String> assistArchives;
        private List<String> archivesName;

        public String getMainPy() {
            return mainPy;
        }

        public void setMainPy(String mainPy) {
            this.mainPy = mainPy;
        }

        public List<String> getAssistPys() {
            return assistPys;
        }

        public void setAssistPys(List<String> assistPys) {
            this.assistPys = assistPys;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getMainClass() {
            return mainClass;
        }

        public void setMainClass(String mainClass) {
            this.mainClass = mainClass;
        }

        public String getArgs() {
            return args;
        }

        public void setArgs(String args) {
            this.args = args;
        }

        public List<String> getConfigs() {
            return configs;
        }

        public void setConfigs(List<String> configs) {
            this.configs = configs;
        }

        public String getMainJar() {
            return mainJar;
        }

        public void setMainJar(String mainJar) {
            this.mainJar = mainJar;
        }

        public List<String> getAssistJars() {
            return assistJars;
        }

        public void setAssistJars(List<String> assistJars) {
            this.assistJars = assistJars;
        }

        public List<String> getAssistFiles() {
            return assistFiles;
        }

        public void setAssistFiles(List<String> assistFiles) {
            this.assistFiles = assistFiles;
        }

        public List<String> getAssistArchives() {
            return assistArchives;
        }

        public void setAssistArchives(List<String> assistArchives) {
            this.assistArchives = assistArchives;
        }

        public List<String> getArchivesName() {
            return archivesName;
        }

        public void setArchivesName(List<String> archivesName) {
            this.archivesName = archivesName;
        }
    }

    public CodeJson getSparkJson() {
        return sparkJson;
    }

    public void setSparkJson(CodeJson sparkJson) {
        this.sparkJson = sparkJson;
    }

    @Override
    public String getContent() {
        StringBuilder code = new StringBuilder();
        if (!CollectionUtils.isEmpty(resourceReferences)) {
            String refs = Joiner.on("\n").join(
                    resourceReferences.stream()
                            .map(str -> "##@resource_reference{\"" + str + "\"}")
                            .collect(Collectors.toList())
            );
            code.append(refs);
        }

        if (sparkJson != null) {
            code.append("\n").append(GsonUtils.defaultGson.toJson(sparkJson));
        }
        return code.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder code = new StringBuilder();
        if (!CollectionUtils.isEmpty(resourceReferences)) {
            String refs = Joiner.on("\n").join(
                    resourceReferences.stream()
                            .map(str -> "##@resource_reference{\"" + str + "\"}")
                            .collect(Collectors.toList())
            );
            code.append(refs);
        }

        if (sparkJson != null) {
            code.append("\n" + GsonUtils.defaultGson.toJson(sparkJson));
        }
        return code.toString();
    }
}