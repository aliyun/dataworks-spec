/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.spark;

import java.util.ArrayList;
import java.util.Optional;

import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.OdpsSparkCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.OdpsSparkCode.CodeJson;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.enums.TaskType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.spark.SparkParameters;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.AbstractNodeConverter;
import com.aliyun.dataworks.migrationx.transformer.dolphinscheduler.converter.flowspec.common.context.FlowSpecConverterContext;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-07
 */
public class SparkNodeConverter extends AbstractNodeConverter<SparkParameters> {
    public SparkNodeConverter(SpecNode specNode,
        FlowSpecConverterContext context) {
        super(specNode, context);
    }

    /**
     * convert spec node to dolphin scheduler task parameters
     *
     * @return dolphin scheduler task parameters
     */
    @Override
    protected SparkParameters convertParameter() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setResourceList(new ArrayList<>());

        Optional.ofNullable(specNode.getScript()).map(SpecScript::getContent).ifPresent(content -> {
            OdpsSparkCode sparkCode = JSONObject.parseObject(content, OdpsSparkCode.class);
            CodeJson sparkJson = sparkCode.getSparkJson();

            ResourceInfo mainJar = new ResourceInfo();
            String mainResourceName = FilenameUtils.concat(context.getDefaultFileResourcePath(),
                StringUtils.defaultString(sparkJson.getMainJar(), sparkJson.getMainPy()));
            mainJar.setResourceName(mainResourceName);
            sparkParameters.setMainJar(mainJar);
            sparkParameters.setMainClass(sparkJson.getMainClass());

            sparkParameters.setDeployMode("local");
            sparkParameters.setMainArgs(sparkJson.getArgs());

            new SparkSubmitConfigParser(sparkParameters, sparkJson.getConfigs()).parse();

            sparkParameters.setAppName(null);
            sparkParameters.setOthers(null);
            sparkParameters.setProgramType(ProgramType.valueOf(StringUtils.upperCase(sparkJson.getLanguage())));
        });

        convertResourceList().forEach(sparkParameters.getResourceList()::add);

        return sparkParameters;
    }

    /**
     * judge task type from spec node and set in taskDefinition
     */
    @Override
    protected void setTaskType() {
        result.setTaskType(TaskType.SPARK.name());
    }
}
