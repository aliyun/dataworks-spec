/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.transformer;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-27
 */
public abstract class AbstractTransformer {

    protected String configPath;

    protected String sourcePath;

    protected String targetPath;

    /**
     * transform entry point
     *
     * @throws Exception exception
     */
    public abstract void transform() throws Exception;
}
