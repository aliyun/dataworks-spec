/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDatasource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ResourceInfo;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.python.PythonVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DolphinSchedulerV3ConverterContext {

    private final Map<Long, List<SpecNode>> nodeHeadMap = new HashMap<>();

    private final Map<Long, List<SpecNode>> nodeTailMap = new HashMap<>();

    /**
     * file name and full name map
     */
    private final Map<String, String> fileNameMap = new HashMap<>();

    private final Map<Long, String> codeUuidMap = new HashMap<>();

    private String specVersion = "1.2.0";

    private Map<String, SpecDatasource> dataSourceMap;

    private String defaultScriptPath;

    private PythonVersion pythonVersion;

    private List<ResourceInfo> resourceInfoList;

    private List<Specification<DataWorksWorkflowSpec>> dependSpecification;
}
