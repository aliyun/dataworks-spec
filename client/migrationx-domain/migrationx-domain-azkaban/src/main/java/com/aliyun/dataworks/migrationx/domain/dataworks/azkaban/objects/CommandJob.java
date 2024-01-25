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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sam.liux
 * @date 2020/08/03
 */
@Data
@ToString(callSuper = true)
public class CommandJob extends Job {
    @ConfigProperty(pattern = "command(\\.\\d+)?")
    private List<String> commands;

    @Override
    public void processJobRelativeFiles() {
        if (getRelatedFiles() == null) {
            setRelatedFiles(new ArrayList<>());
        }

        for (String command : commands) {
            String[] tokens = command.split(" ");
            if (tokens == null) {
                continue;
            }

            File jobFile = getJobFile();
            if (jobFile == null || !jobFile.exists()) {
                continue;
            }

            File dir = jobFile.getParentFile();
            for (String token : tokens) {
                File testFile = new File(dir, token);
                if (testFile.exists() && testFile.isFile() && !testFile.isHidden()) {
                    getRelatedFiles().add(testFile);
                }
            }
        }
    }

    @Override
    public String getCode() {
        if (CollectionUtils.isNotEmpty(commands)) {
            return Joiner.on("\n").join(commands);
        }

        return null;
    }
}
