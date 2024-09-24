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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.dataquality;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.parameters.AbstractParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.spark.SparkParameters;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataQualityParameters
 */
public class DataQualityParameters extends AbstractParameters {

    private static final Logger logger = LoggerFactory.getLogger(DataQualityParameters.class);

    /**
     * rule id
     */
    private int ruleId;
    /**
     * rule input entry value map
     */
    private Map<String, String> ruleInputParameter;
    /**
     * spark parameters
     */
    private SparkParameters sparkParameters;

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public Map<String, String> getRuleInputParameter() {
        return ruleInputParameter;
    }

    public void setRuleInputParameter(Map<String, String> ruleInputParameter) {
        this.ruleInputParameter = ruleInputParameter;
    }

    /**
     * In this function ,we need more detailed check every parameter,
     * if the parameter is non-conformant will return false
     *
     * @return boolean result
     */
    @Override
    public boolean checkParameters() {

        if (ruleId == 0) {
            logger.error("rule id is null");
            return false;
        }

        if (MapUtils.isEmpty(ruleInputParameter)) {
            logger.error("rule input parameter is empty");
            return false;
        }

        return sparkParameters != null;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    public SparkParameters getSparkParameters() {
        return sparkParameters;
    }

    public void setSparkParameters(SparkParameters sparkParameters) {
        this.sparkParameters = sparkParameters;
    }
}
