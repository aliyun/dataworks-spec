package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler;

import java.util.Properties;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.AssetType;
import com.aliyun.dataworks.migrationx.transformer.core.loader.ProjectAssetLoader;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.AbstractBaseConverter;

public abstract class AbstractDolphinSchedulerConverter extends AbstractBaseConverter {
    protected com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Project project;
    protected Properties properties = new Properties();

    public AbstractDolphinSchedulerConverter(AssetType assetType, String name) {
        super(assetType, name);
    }

    public AbstractDolphinSchedulerConverter(AssetType assetType, String name, ProjectAssetLoader projectAssetLoader) {
        super(assetType, name, projectAssetLoader);
    }

    public AbstractDolphinSchedulerConverter setProject(Project project) {
        this.project = project;
        return this;
    }

    public AbstractDolphinSchedulerConverter setProperties(Properties properties) {
        this.properties = properties;
        return this;
    }
}
