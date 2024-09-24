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

package com.aliyun.dataworks.migrationx.domain.dataworks.azkaban.objects;

import com.google.common.base.Joiner;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sam.liux
 * @date 2020/08/03
 */
@Data
@ToString(callSuper = true)
public class HiveJob extends Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(HiveJob.class);

    @ConfigProperty(value = "user.to.proxy")
    private String userToProxy;
    @ConfigProperty(pattern = "hive.query(\\.\\d+)?")
    private List<String> queries;
    @ConfigProperty(value = "azk.hive.action")
    private String azkHiveAction;
    @ConfigProperty(value = "hive.script")
    private String hiveScript;
    @ConfigProperty(value = "hive.query.file")
    private String hiveQueryFile;

    @Override
    public void processJobRelativeFiles() {
        if (getRelatedFiles() == null) {
            setRelatedFiles(new ArrayList<>());
        }

        for (String query : queries) {
            String[] tokens = query.split(" ");
            if (tokens == null) {
                continue;
            }

            File jobFile = getJobFile();
            if (jobFile == null || !jobFile.exists()) {
                continue;
            }

            File dir = jobFile.getParentFile();
            for (String token : tokens) {
                File file = new File(dir, token);
                if (file.exists() && file.isFile() && !file.isHidden()) {
                    getRelatedFiles().add(file);
                }
            }
        }

        if (StringUtils.isNotBlank(getHiveScript())) {
            File file = new File(getJobFile().getParentFile(), getHiveScript());
            if (file.exists() && file.isFile() && !file.isHidden()) {
                getRelatedFiles().add(file);
            }
        }

        if (StringUtils.isNotBlank(getHiveQueryFile())) {
            File file = new File(getJobFile().getParentFile(), getHiveQueryFile());
            if (file.exists() && file.isFile() && !file.isHidden()) {
                getRelatedFiles().add(file);
            }
        }
    }

    @Override
    public String getCode() {
        if (CollectionUtils.isNotEmpty(getQueries())) {
            return Joiner.on("\n").join(getQueries());
        }

        File jobFile = getJobFile();
        if (jobFile == null || !jobFile.exists()) {
            return null;
        }

        File file = null;
        if (StringUtils.isNotBlank(getHiveScript())) {
            file = new File(getJobFile().getParentFile(), getHiveScript());
            if (!file.exists()) {
                return null;
            }
        }

        if (StringUtils.isNotBlank(getHiveQueryFile())) {
            file = new File(getJobFile().getParentFile(), getHiveQueryFile());
            if (!file.exists()) {
                return null;
            }
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            while (br.ready()) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line + "\n");
            }
            br.close();
            return sb.toString();
        } catch (FileNotFoundException e) {
            LOGGER.error("read hive script file failed: ", e);
            return null;
        } catch (IOException e) {
            LOGGER.error("read hive script file failed: ", e);
            return null;
        }
    }
}
