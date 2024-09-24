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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.v3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.OdpsSparkCode;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils.ArgsUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DagData;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.DolphinSchedulerV3Context;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.TaskDefinition;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.DataSource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.entity.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.model.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.spark.SparkConstants;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.spark.SparkParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.transformer.core.common.Constants;
import com.aliyun.dataworks.migrationx.transformer.core.utils.EmrCodeUtils;
import com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler.DolphinSchedulerConverterContext;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class SparkParameterConverter extends AbstractParameterConverter<SparkParameters> {
    public SparkParameterConverter(DagData processMeta, TaskDefinition taskDefinition,
            DolphinSchedulerConverterContext<Project, DagData, DataSource, ResourceInfo,
                    UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    public List<DwNode> convertParameter() throws IOException {
        DwNode dwNode = newDwNode(taskDefinition);
        String type = getSparkConverterType();
        dwNode.setType(CodeProgramType.of(type).name());

        if (CodeProgramType.EMR_SPARK.equals(CodeProgramType.of(type))) {
            List<String> cmd = populateSparkOptions(parameter);
            dwNode.setCode(String.join(" ", cmd));
            dwNode.setCode(EmrCodeUtils.toEmrCode(dwNode));
        } else if (CodeProgramType.ODPS_SPARK.equals(CodeProgramType.of(type))) {
            OdpsSparkCode odpsSparkCode = populateSparkOdpsCode();
            dwNode.setCode(odpsSparkCode.toString());
        }

        return Arrays.asList(dwNode);
    }

    private List<String> populateSparkOptions(SparkParameters sparkParameters) {
        List<String> args = new ArrayList<>();

        ProgramType programType = sparkParameters.getProgramType();
        ResourceInfo mainJar = sparkParameters.getMainJar();
        if (programType != ProgramType.SQL) {
            String resource = mainJar.getResourceName();
            if (StringUtils.isEmpty(resource)) {
                resource = getResourceName(mainJar.getId());
            }

            if (resource != null) {
                String[] resources = resource.split("/");
                if (resources.length > 0) {
                    resource = resources[resources.length - 1];
                }
                mainJar.setResourceName(resource);
                String dwResource = "##@resource_reference{\"" + resource + "\"} \n";
                args.add(dwResource + SparkConstants.SPARK_SUBMIT_COMMAND);
            } else {
                args.add(SparkConstants.SPARK_SUBMIT_COMMAND);
            }
        } else {
            args.add(SparkConstants.SPARK_SUBMIT_COMMAND);
        }

        String deployMode = StringUtils.isNotEmpty(sparkParameters.getDeployMode()) ? sparkParameters.getDeployMode()
                : SparkConstants.DEPLOY_MODE_LOCAL;

        if (!SparkConstants.DEPLOY_MODE_LOCAL.equals(deployMode)) {
            args.add(SparkConstants.MASTER);
            String masterUrl = SparkConstants.SPARK_ON_YARN;
            args.add(masterUrl);
        }
        args.add(SparkConstants.DEPLOY_MODE);
        args.add(deployMode);

        String mainClass = sparkParameters.getMainClass();
        if (programType != ProgramType.PYTHON && programType != ProgramType.SQL && StringUtils.isNotEmpty(mainClass)) {
            args.add(SparkConstants.MAIN_CLASS);
            args.add(mainClass);
        }

        populateSparkResourceDefinitions(args, sparkParameters);

        String appName = sparkParameters.getAppName();
        if (StringUtils.isNotEmpty(appName)) {
            args.add(SparkConstants.SPARK_NAME);
            args.add(ArgsUtils.escape(appName));
        }

        String others = sparkParameters.getOthers();
        if (!SparkConstants.DEPLOY_MODE_LOCAL.equals(deployMode)
                && (StringUtils.isEmpty(others) || !others.contains(SparkConstants.SPARK_YARN_QUEUE))) {
            String yarnQueue = sparkParameters.getYarnQueue();
            if (StringUtils.isNotEmpty(yarnQueue)) {
                args.add(SparkConstants.SPARK_YARN_QUEUE);
                args.add(yarnQueue);
            }
        }

        // --conf --files --jars --packages
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }

        //jar
        if (programType != ProgramType.SQL && mainJar != null) {
            args.add(mainJar.getResourceName());
        }

        String mainArgs = sparkParameters.getMainArgs();
        if (programType != ProgramType.SQL && StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }

        return args;
    }

    private OdpsSparkCode populateSparkOdpsCode() {
        OdpsSparkCode odpsSparkCode = new OdpsSparkCode();
        odpsSparkCode.setResourceReferences(odpsSparkCode.getResourceReferences());
        odpsSparkCode.setSparkJson(new OdpsSparkCode.CodeJson());

        ResourceInfo mainJar = parameter.getMainJar();
        String resource = mainJar.getResourceName();
        if (StringUtils.isEmpty(resource)) {
            resource = getResourceName(mainJar.getId());
        }

        if (resource != null) {
            String[] resources = resource.split("/");
            if (resources.length > 0) {
                resource = resources[resources.length - 1];
            }
            mainJar.setResourceName(resource);
            //String dwResource = "##@resource_reference{\"" + resource + "\"} \n";
            odpsSparkCode.getResourceReferences().add(resource);
            odpsSparkCode.getSparkJson().setMainJar(resource);
        }
        String mainClass = parameter.getMainClass();
        odpsSparkCode.getSparkJson().setMainClass(mainClass);
        odpsSparkCode.getSparkJson().setVersion("2.x");
        odpsSparkCode.getSparkJson().setLanguage("java");

        String mainArgs = parameter.getMainArgs();
        odpsSparkCode.getSparkJson().setArgs(mainArgs);
        List<String> confs = new ArrayList<>();
        populateOdpsSparkResourceDefinitions(confs, parameter);
        odpsSparkCode.getSparkJson().setConfigs(confs);
        return odpsSparkCode;
    }

    private void populateSparkResourceDefinitions(List<String> args, SparkParameters sparkParameters) {
        int driverCores = sparkParameters.getDriverCores();
        if (driverCores > 0) {
            args.add(String.format(SparkConstants.DRIVER_CORES, driverCores));
        }

        String driverMemory = sparkParameters.getDriverMemory();
        if (StringUtils.isNotEmpty(driverMemory)) {
            args.add(String.format(SparkConstants.DRIVER_MEMORY, driverMemory));
        }

        int numExecutors = sparkParameters.getNumExecutors();
        if (numExecutors > 0) {
            args.add(String.format(SparkConstants.NUM_EXECUTORS, numExecutors));
        }

        int executorCores = sparkParameters.getExecutorCores();
        if (executorCores > 0) {
            args.add(String.format(SparkConstants.EXECUTOR_CORES, executorCores));
        }

        String executorMemory = sparkParameters.getExecutorMemory();
        if (StringUtils.isNotEmpty(executorMemory)) {
            args.add(String.format(SparkConstants.EXECUTOR_MEMORY, executorMemory));
        }
    }

    private void populateOdpsSparkResourceDefinitions(List<String> args, SparkParameters sparkParameters) {
        int driverCores = sparkParameters.getDriverCores();
        if (driverCores > 0) {
            args.add(String.format("spark.driver.cores=%d", driverCores));
        }

        String driverMemory = sparkParameters.getDriverMemory();
        if (StringUtils.isNotEmpty(driverMemory)) {
            args.add(String.format("spark.driver.memory=%s", driverMemory));
        }

        int numExecutors = sparkParameters.getNumExecutors();
        if (numExecutors > 0) {
            args.add(String.format("spark.executor.instances=%d", numExecutors));
        }

        int executorCores = sparkParameters.getExecutorCores();
        if (executorCores > 0) {
            args.add(String.format("spark.executor.cores=%d", executorCores));
        }

        String executorMemory = sparkParameters.getExecutorMemory();
        if (StringUtils.isNotEmpty(executorMemory)) {
            args.add(String.format("spark.executor.memory=%s", executorMemory));
        }
    }

    private String getResourceName(Integer id) {
        if (id == null) {
            return null;
        }
        DolphinSchedulerV3Context context = DolphinSchedulerV3Context.getContext();
        return CollectionUtils.emptyIfNull(context.getResources())
                .stream()
                .filter(r -> r.getId() == id)
                .findAny()
                .map(r -> r.getName())
                .orElseGet(null);
    }

    private String getSparkConverterType() {
        String convertType = properties.getProperty(Constants.CONVERTER_TARGET_SPARK_SUBMIT_TYPE_AS);
        String defaultConvertType = CodeProgramType.EMR_SPARK_SHELL.name();
        return getConverterType(convertType, defaultConvertType);
    }
}
