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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Workflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.NodeIo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.NodeUtils;
import com.aliyun.dataworks.migrationx.transformer.core.annotation.DependsOn;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.controller.Task;
import com.aliyun.dataworks.migrationx.transformer.core.loader.ConfigPropertiesLoader;
import com.aliyun.dataworks.migrationx.transformer.core.loader.ProjectAssetLoader;
import com.google.common.base.Preconditions;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.aliyun.dataworks.migrationx.transformer.core.common.Constants.CONVERTER_TARGET_UNSUPPORTED_NODE_TYPE_AS;

/**
 * @author sam.liux
 * @date 2019/07/04
 */
public abstract class AbstractBaseConverter extends Task<List<DwWorkflow>> implements WorkflowConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBaseConverter.class);

    @DependsOn
    protected ConfigPropertiesLoader propertiesLoader;
    @DependsOn
    protected ProjectAssetLoader projectAssetLoader;

    protected List<DwWorkflow> workflowList;
    protected AssetType assetType;

    public AbstractBaseConverter(AssetType assetType, String name) {
        super(name);
        this.assetType = assetType;
    }

    public AbstractBaseConverter(AssetType assetType, String name,
        ProjectAssetLoader projectAssetLoader) {
        super(name);
        this.projectAssetLoader = projectAssetLoader;
        this.assetType = assetType;
    }

    public void setPropertiesLoader(ConfigPropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
    }

    public ConfigPropertiesLoader getPropertiesLoader() {
        return propertiesLoader;
    }

    @Override
    public List<DwWorkflow> call() throws Exception {
        List<Asset> assets = this.projectAssetLoader.getResult();

        if (CollectionUtils.isEmpty(assets)) {
            return ListUtils.emptyIfNull(null);
        }

        this.workflowList = new ArrayList<>();
        for (Asset asset : assets.stream().filter(
            asset1 -> asset1.getType().equals(assetType)).collect(Collectors.toList())) {
            List<DwWorkflow> workflows = convert(asset);
            if (!CollectionUtils.isEmpty(workflows)) {
                this.workflowList.addAll(workflows);
            }
        }
        return this.workflowList;
    }

    protected String getDefaultTypeIfNotSupported(Properties properties, CodeProgramType defaultNodeType) {
        Preconditions.checkNotNull(properties, "converter properties should not be null");
        Preconditions.checkNotNull(defaultNodeType, "default node type should not be null");

        return StringUtils.defaultIfBlank(
            properties.getProperty(CONVERTER_TARGET_UNSUPPORTED_NODE_TYPE_AS, null),
            defaultNodeType.name());
    }

    protected String getDefaultCalcEngineDatasource(Properties properties, String defaultValue) {
        Preconditions.checkNotNull(properties, "converter properties should not be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(defaultValue),
            "calc engine default datasource should not be null");

        return StringUtils.defaultIfBlank(
            properties.getProperty(Constants.CONVERTER_TARGET_ENGINE_DATASOURCE_NAME, null),
            defaultValue);
    }

    protected CalcEngineType getDefaultCalcEngineType(Properties properties, CalcEngineType defaultType) {
        Preconditions.checkNotNull(properties, "converter properties should not be null");
        Preconditions.checkNotNull(defaultType, "default calc engine Type should not be null");
        return CalcEngineType.valueOf(
            properties.getProperty(Constants.CONVERTER_TARGET_ENGINE_DATASOURCE_TYPE, defaultType.name()));
    }

    protected void setProjectRootDependForNoInputNode(Project project, List<DwWorkflow> workflows) {
        org.apache.commons.collections4.ListUtils.emptyIfNull(workflows).stream()
            .filter(wf -> BooleanUtils.isTrue(wf.getScheduled()))
            .map(Workflow::getNodes)
            .flatMap(List::stream)
            .filter(Objects::nonNull)
            .forEach(node -> {
                if (CollectionUtils.isEmpty(node.getInputs())) {
                    NodeIo root = new NodeIo();
                    root.setData(NodeUtils.getProjectRootOutput(project));
                    root.setParseType(1);
                    node.getInputs().add(root);
                }
            });
    }
}
