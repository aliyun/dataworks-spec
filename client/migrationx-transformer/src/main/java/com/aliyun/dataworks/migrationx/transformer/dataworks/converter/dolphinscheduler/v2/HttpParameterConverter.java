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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.v2;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.process.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v2.task.http.HttpParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;

import org.apache.commons.collections4.CollectionUtils;

public class HttpParameterConverter extends AbstractParameterConverter<HttpParameters> {
    public HttpParameterConverter(DagData processMeta, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo,
                    UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    public List<DwNode> convertParameter() throws IOException {
        DwNode dwNode = newDwNode(taskDefinition);
        String type = getConverterType();
        dwNode.setType(type);
        String cmd = "curl -X " + parameter.getHttpMethod().name() + " -s -w \"\\n%{http_code}\" ";
        String url = parameter.getUrl();
        parameter.getHttpMethod().name();
        if (CollectionUtils.isNotEmpty(parameter.getHttpParams())) {
            List<String> param = parameter.getHttpParams().stream()
                    .map(h -> h.getProp() + "=" + h.getValue())
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(param)) {
                String query = String.join("&", param);
                url = url + "?" + query;
            }
        }
        cmd = cmd + " '" + url + "' | { \n"
                + "    read body \n"
                + "    read code\n"
                + " }";
        dwNode.setCode("# get http status $code get response body $body \n" + cmd);
        if (CodeProgramType.EMR_SHELL.name().equals(type)) {
            dwNode.setCode(EmrCodeUtils.toEmrCode(dwNode));
        }
        return Arrays.asList(dwNode);
    }

    private String getConverterType() {
        String convertType = properties.getProperty(Constants.CONVERTER_TARGET_SHELL_NODE_TYPE_AS);
        String defaultConvertType = CodeProgramType.DIDE_SHELL.name();
        return getConverterType(convertType, defaultConvertType);
    }
}
