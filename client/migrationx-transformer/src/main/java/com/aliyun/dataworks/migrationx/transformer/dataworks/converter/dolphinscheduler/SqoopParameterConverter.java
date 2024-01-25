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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter.dolphinscheduler;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.CodeModeType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Datasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.Project;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.UdfFunc;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils.JSONUtils;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.ProcessMeta;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.TaskNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.sqoop.SqoopParameters;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.sqoop.sources.SourceHdfsParameter;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.sqoop.sources.SourceHiveParameter;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.sqoop.sources.SourceMysqlParameter;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.sqoop.targets.TargetHdfsParameter;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.sqoop.targets.TargetHiveParameter;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v139.task.sqoop.targets.TargetMysqlParameter;
import com.aliyun.dataworks.migrationx.transformer.core.sqoop.DIConfigTemplate;
import com.aliyun.dataworks.migrationx.transformer.core.sqoop.DIJsonProcessor;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 聿剑
 * @date 2022/10/24
 */
@Slf4j
public class SqoopParameterConverter extends AbstractParameterConverter<SqoopParameters> {
    public SqoopParameterConverter(ProcessMeta processMeta, TaskNode taskDefinition,
        DolphinSchedulerConverterContext<Project, ProcessMeta, Datasource, ResourceInfo,
            UdfFunc> converterContext) {
        super(processMeta, taskDefinition, converterContext);
    }

    @Override
    protected void convertParameter() {
        DwNode dwNode = newDwNode(processMeta, taskDefinition);
        dwNode.setType(CodeProgramType.DI.name());
        DIJsonProcessor diProcessor = DIJsonProcessor.from(DIConfigTemplate.DI_CODE_TEMPLATE);
        DIJsonProcessor readerJp = diProcessor.getConfiguration("steps[0]");
        DIJsonProcessor writerJp = diProcessor.getConfiguration("steps[1]");
        processSqoopSource(readerJp);
        processSqoopTarget(writerJp);
        diProcessor.set("extend.mode", CodeModeType.CODE.getValue());
        dwNode.setCode(diProcessor.toJSON());
        dwNode.setCodeMode(CodeModeType.CODE);
    }

    private void processSqoopTarget(DIJsonProcessor writerJp) {
        writerJp.set("stepType", StringUtils.lowerCase(parameter.getTargetType()));
        if (StringUtils.equalsIgnoreCase(parameter.getTargetType(), "mysql")) {
            TargetMysqlParameter targetMysqlParameter = JSONUtils.parseObject(parameter.getTargetParams(),
                TargetMysqlParameter.class);
            Optional.ofNullable(targetMysqlParameter).ifPresent(p -> {
                ListUtils.emptyIfNull(converterContext.getDolphinSchedulerPackage().getDatasources()).stream()
                    .filter(ds -> Objects.equals(ds.getId(), targetMysqlParameter.getTargetDatasource()))
                    .findFirst().ifPresent(ds -> writerJp.set("parameter.datasource", ds.getName()));
                writerJp.set("parameter.table", targetMysqlParameter.getTargetTable());
                writerJp.set("parameter.preSql", Optional.ofNullable(StringUtils.split(
                    targetMysqlParameter.getPreQuery(), ",")).orElse(new String[] {}));
                writerJp.set("parameter.column", StringUtils.isBlank(targetMysqlParameter.getTargetColumns()) ?
                    new String[] {"*"} : StringUtils.split(targetMysqlParameter.getTargetColumns(), ","));
                if (StringUtils.equalsIgnoreCase(targetMysqlParameter.getTargetUpdateMode(), "updateonly")) {
                    writerJp.set("parameter.writeMode", "update");
                } else {
                    writerJp.set("parameter.writeMode", "replace");
                }
            });
        }

        if (StringUtils.equalsIgnoreCase(parameter.getTargetType(), "hive")) {
            TargetHiveParameter targetHiveParameter = JSONUtils.parseObject(parameter.getTargetParams(),
                TargetHiveParameter.class);
            Optional.ofNullable(targetHiveParameter).ifPresent(p -> {
                ListUtils.emptyIfNull(converterContext.getDolphinSchedulerPackage().getDatasources()).stream()
                    .filter(ds -> Objects.equals(ds.getId(), targetHiveParameter.getHiveDatabase()))
                    .findFirst().ifPresent(ds -> writerJp.set("parameter.datasource", ds.getName()));
                writerJp.set("parameter.table", targetHiveParameter.getHiveTable());
                String[] keys = Optional.ofNullable(StringUtils.split(
                    targetHiveParameter.getHivePartitionKey(), ",")).orElse(new String[] {});
                String[] values = Optional.ofNullable(StringUtils.split(
                    targetHiveParameter.getHivePartitionValue(), ",")).orElse(new String[] {});
                List<String> partitions = new ArrayList<>();
                for (int i = 0; i < Math.min(keys.length, values.length); i++) {
                    partitions.add(Joiner.on("=").join(keys[i], values[i]));
                }
                writerJp.set("parameter.partition", Joiner.on(",").join(partitions));
                writerJp.set("parameter.hdfsUsername", "hdfs");
                writerJp.set("parameter.writeMode", "append");
            });
        }

        if (StringUtils.equalsIgnoreCase(parameter.getTargetType(), "hdfs")) {
            TargetHdfsParameter targetHdfsParameter = JSONUtils.parseObject(parameter.getSourceParams(),
                TargetHdfsParameter.class);
            Optional.ofNullable(targetHdfsParameter).ifPresent(p -> {
                writerJp.set("parameter.path", p.getTargetPath());
                writerJp.set("parameter.compress", targetHdfsParameter.getCompressionCodec());
                writerJp.set("parameter.datasource", "hdfs");
                writerJp.set("parameter.fileType", targetHdfsParameter.getFileType());
                if (StringUtils.equalsIgnoreCase("parquet", targetHdfsParameter.getFileType())) {
                    writerJp.set("parameter.writeMode", "noConflict");
                } else {
                    writerJp.set("parameter.writeMode", "append");
                }
            });
        }
    }

    private void processSqoopSource(DIJsonProcessor readerJp) {
        readerJp.set("stepType", StringUtils.lowerCase(parameter.getSourceType()));
        if (StringUtils.equalsIgnoreCase(parameter.getSourceType(), "mysql")) {
            SourceMysqlParameter mysqlParameter = JSONUtils.parseObject(parameter.getSourceParams(),
                SourceMysqlParameter.class);
            Optional.ofNullable(mysqlParameter).ifPresent(p -> {
                ListUtils.emptyIfNull(converterContext.getDolphinSchedulerPackage().getDatasources()).stream()
                    .filter(ds -> Objects.equals(ds.getId(), mysqlParameter.getSrcDatasource()))
                    .findFirst().ifPresent(ds -> readerJp.set("parameter.datasource", ds.getName()));
                readerJp.set("parameter.table", mysqlParameter.getSrcTable());
                readerJp.set("parameter.where", StringUtils.substringAfter(
                    StringUtils.lowerCase(mysqlParameter.getSrcQuerySql()), "where"));
                readerJp.set("parameter.column", StringUtils.isBlank(mysqlParameter.getSrcColumns()) ?
                    new String[] {"*"} : StringUtils.split(mysqlParameter.getSrcColumns(), ","));
            });
        }

        if (StringUtils.equalsIgnoreCase(parameter.getSourceType(), "hive")) {
            SourceHiveParameter sourceHiveParameter = JSONUtils.parseObject(parameter.getSourceParams(),
                SourceHiveParameter.class);
            Optional.ofNullable(sourceHiveParameter).ifPresent(p -> {
                ListUtils.emptyIfNull(converterContext.getDolphinSchedulerPackage().getDatasources()).stream()
                    .filter(ds -> Objects.equals(ds.getId(), sourceHiveParameter.getHiveDatabase()))
                    .findFirst().ifPresent(ds -> readerJp.set("parameter.datasource", ds.getName()));
                readerJp.set("parameter.table", sourceHiveParameter.getHiveTable());
                String[] keys = Optional.ofNullable(StringUtils.split(
                    sourceHiveParameter.getHivePartitionKey(), ",")).orElse(new String[] {});
                String[] values = Optional.ofNullable(StringUtils.split(
                    sourceHiveParameter.getHivePartitionValue(), ",")).orElse(new String[] {});
                List<String> partitions = new ArrayList<>();
                for (int i = 0; i < Math.min(keys.length, values.length); i++) {
                    partitions.add(Joiner.on("=").join(keys[i], values[i]));
                }
                readerJp.set("parameter.partition", Joiner.on(",").join(partitions));
                readerJp.set("parameter.readMode", "hdfs");
                readerJp.set("parameter.hdfsUsername", "hdfs");
                List<Map<String, String>> columns = Arrays.stream(keys).map(key -> {
                    Map<String, String> column = new HashMap<>();
                    column.put("type", "string");
                    column.put("value", key);
                    return column;
                }).collect(Collectors.toList());
                readerJp.set("parameter.hivePartitionColumn", columns);
            });
        }

        if (StringUtils.equalsIgnoreCase(parameter.getSourceType(), "hdfs")) {
            SourceHdfsParameter sourceHdfsParameter = JSONUtils.parseObject(parameter.getSourceParams(),
                SourceHdfsParameter.class);
            Optional.ofNullable(sourceHdfsParameter).ifPresent(
                p -> readerJp.set("parameter.path", p.getExportDir()));
        }
    }
}
