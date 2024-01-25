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

package com.aliyun.dataworks.migrationx.transformer.core.loader;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwResource;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.ResourceUtils;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.controller.Task;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sam.liux
 * @date 2019/07/16
 */
public class ProjectResourceLoader extends Task<List<DwResource>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectResourceLoader.class);
    private String projectDir;
    private List<DwResource> resources = new ArrayList<>();

    public ProjectResourceLoader(String projectDir) {
        super(ProjectResourceLoader.class.getSimpleName());
        this.projectDir = projectDir;
    }

    public ProjectResourceLoader(String projectDir, String name) {
        super(name);
        this.projectDir = projectDir;
    }

    @Override
    public List<DwResource> call() {
        File resourceDir = new File(
            Joiner.on(File.separator).join(projectDir, Constants.SRC_DIR_PRJ_RELATED, Constants.RESOURCES_DIR));
        if (!resourceDir.exists()) {
            return resources;
        }

        resources = Arrays.asList(resourceDir.listFiles()).stream().map(file -> {
            DwResource res = new DwResource();
            res.setName(file.getName());
            res.setLocalPath(file.getAbsolutePath());
            res.setType(ResourceUtils.getFileResourceType(file.getName()));
            return res;
        }).collect(Collectors.toList());

        LOGGER.info("load {} resources", resources.size());
        return resources;
    }
}