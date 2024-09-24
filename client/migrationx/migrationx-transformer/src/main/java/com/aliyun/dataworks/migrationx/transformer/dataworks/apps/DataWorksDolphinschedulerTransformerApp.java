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

package com.aliyun.dataworks.migrationx.transformer.dataworks.apps;

import java.io.File;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.DolphinSchedulerPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DataWorksPackage;
import com.aliyun.dataworks.migrationx.domain.dataworks.standard.objects.Package;
import com.aliyun.dataworks.migrationx.transformer.core.BaseTransformerApp;
import com.aliyun.dataworks.migrationx.transformer.core.transformer.Transformer;
import com.aliyun.dataworks.migrationx.transformer.dataworks.transformer.DataWorksDolphinSchedulerTransformer;
import com.aliyun.migrationx.common.context.TransformerContext;
import com.aliyun.migrationx.common.metrics.enums.CollectorType;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 聿剑
 * @date 2023/02/10
 */
@Slf4j
public class DataWorksDolphinschedulerTransformerApp extends BaseTransformerApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataWorksDolphinschedulerTransformerApp.class);

    public DataWorksDolphinschedulerTransformerApp() {
        super(DolphinSchedulerPackage.class, DataWorksPackage.class);
    }

    @Override
    public void initCollector() {
        TransformerContext.init(CollectorType.DolphinScheduler);
        super.initCollector();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Transformer createTransformer(File config, Package from, Package to) {
        DolphinSchedulerPackage dolphinSchedulerPackage = (DolphinSchedulerPackage) from;
        DataWorksPackage dataWorksPackage = (DataWorksPackage) to;
        return new DataWorksDolphinSchedulerTransformer(config, dolphinSchedulerPackage, dataWorksPackage);
    }
}
