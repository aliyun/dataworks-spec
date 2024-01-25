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

package com.aliyun.dataworks.migrationx.transformer.dataworks.transformer;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DataWorksPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwProject;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.DataWorksDwmaPackageFileService;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.DataWorksSpecPackageFileService;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.service.DolphinSchedulerPackageFileService;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerV1Converter;
import com.aliyun.dataworks.migrationx.transformer.core.transformer.AbstractPackageTransformer;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.aliyun.migrationx.common.utils.ZipUtils;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * @author 聿剑
 * @date 2023/02/15
 */
@SuppressWarnings("ALL")
@Slf4j
public class DataWorksDolphinSchedulerTransformer extends AbstractPackageTransformer<DolphinSchedulerPackage, DataWorksPackage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataWorksDolphinSchedulerTransformer.class);
    private File packageFile;
    private DwProject dwProject;
    private DataWorksTransformerConfig dataWorksTransformerConfig;
    private Properties converterProperties;

    public DataWorksDolphinSchedulerTransformer(File configFile, DolphinSchedulerPackage sourcePacakgeFile,
        DataWorksPackage targetPackageFile) {
        super(configFile, sourcePacakgeFile, targetPackageFile);
    }

    @Override
    public void init() throws Exception {
        this.sourcePackageFileService = new DolphinSchedulerPackageFileService();

        initConfig(this.configFile);
        log.info("target package format: {}", this.dataWorksTransformerConfig.getFormat());
        switch (this.dataWorksTransformerConfig.getFormat()) {
            case DWMA:
                this.targetPackageFileService = new DataWorksDwmaPackageFileService();
            case SPEC:
                this.targetPackageFileService = new DataWorksSpecPackageFileService();
        }
        this.targetPackageFileService.setLocale(this.dataWorksTransformerConfig.getLocale());
    }

    private void initConfig(File configFile) throws IOException {
        if (!configFile.exists()) {
            log.error("config file not exists: {}", configFile);
            new BizException(ErrorCode.FILE_NOT_FOUND).with(configFile);
        }

        String config = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
        this.dataWorksTransformerConfig
            = GsonUtils.fromJsonString(config, new TypeToken<DataWorksTransformerConfig>() {}.getType());
        if (this.dataWorksTransformerConfig == null) {
            log.error("config file: {}, config class: {}", configFile, DataWorksTransformerConfig.class);
            throw new BizException(ErrorCode.PARSE_CONFIG_FILE_FAILED).with(configFile);
        }

        this.dwProject = Optional.ofNullable(this.dataWorksTransformerConfig.getProject()).orElseGet(() -> {
            DwProject p = new DwProject();
            p.setName("tmp_transform_project");
            return p;
        });
        if (this.dwProject == null || StringUtils.isBlank(this.dwProject.getName())) {
            throw new BizException(ErrorCode.CONFIG_ITEM_INVALID).with("project.name").with("empty");
        }

        this.converterProperties = new Properties();
        Optional.ofNullable(this.dataWorksTransformerConfig).map(DataWorksTransformerConfig::getSettings).ifPresent(settings -> {
            settings.entrySet().stream().forEach(ent -> {
                if (ent.getValue().isJsonPrimitive()) {
                    log.info("key: {}, value: {}", ent.getKey(), ent.getValue().getAsString());
                    this.converterProperties.put(ent.getKey(), ent.getValue().getAsString());
                } else {
                    log.info("key: {}, value: {}", ent.getKey(), GsonUtils.toJsonString(ent.getValue()));
                    this.converterProperties.put(ent.getKey(), GsonUtils.toJsonString(ent.getValue()));
                }
            });
        });
        this.converterProperties.put("format", this.dataWorksTransformerConfig.getFormat());
    }

    @Override
    public void load() throws Exception {
        sourcePackageFileService.load(sourcePackage);
    }

    @Override
    public void transform() throws Exception {
        this.targetPackage.setDwProject(this.dwProject);
        this.packageFile = this.sourcePackage.getPackageFile();

        File unzippedDir = ZipUtils.decompress(this.packageFile);
        Preconditions.checkArgument(unzippedDir != null,
            BizException.of(ErrorCode.PACKAGE_ANALYZE_FAILED).with("unzip package failed"));

        sourcePackageFileService.load(sourcePackage);

        LOGGER.info("package dir root: {}", unzippedDir);
        Asset asset = new Asset();
        asset.setType(AssetType.DW_EXPORT);
        asset.setPath(unzippedDir);

        DolphinSchedulerV1Converter converter = new DolphinSchedulerV1Converter();
        converter.setProject(this.dwProject);
        converter.setProperties(this.converterProperties);
        List<DwWorkflow> workflowList = converter.convert(asset);
        ListUtils.emptyIfNull(workflowList).stream().forEach(wf -> {
            wf.setProjectRef(this.dwProject);
        });
        this.dwProject.setWorkflows(new ArrayList<>(ListUtils.emptyIfNull(workflowList)));
    }

    @Override
    public void write() throws Exception {
        targetPackageFileService.write(targetPackage, targetPackage.getPackageFile());
    }
}
