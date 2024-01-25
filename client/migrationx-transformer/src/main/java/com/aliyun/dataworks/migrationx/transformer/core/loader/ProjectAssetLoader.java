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

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.controller.Task;
import com.aliyun.dataworks.migrationx.transformer.core.report.Reportable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author sam.liux
 * @date 2019/07/03
 */
public class ProjectAssetLoader extends Task<List<Asset>> implements Reportable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectAssetLoader.class);

    private String projectDir;
    private List<Asset> assetList = new ArrayList<>();

    public ProjectAssetLoader(String projectDir) {
        super(ProjectAssetLoader.class.getSimpleName());
        this.projectDir = projectDir;
    }

    public ProjectAssetLoader(String projectDir, String name) {
        super(name);
        this.projectDir = projectDir;
    }

    @Override
    public List<Asset> call() throws Exception {
        File projectPath = new File(projectDir);
        File assetPath = new File(
            projectPath.getAbsolutePath() + File.separator +
                Constants.SRC_DIR_PRJ_RELATED + File.separator +
                Constants.ASSETS_DIR);
        if (!assetPath.exists()) {
            return assetList;
        }

        for (File assetDir : Objects.requireNonNull(
            assetPath.listFiles(f -> f.isDirectory() && !f.isHidden() && !f.getName().startsWith(".")))) {
            try {
                AssetType type = AssetType.getAssetType(assetDir.getName());
                Asset asset = new Asset();
                asset.setType(type);
                asset.setPath(assetDir);
                assetList.add(asset);
            } catch (Exception e) {
                LOGGER.error("{}", e.getMessage());
            }
        }
        return assetList;
    }
}
