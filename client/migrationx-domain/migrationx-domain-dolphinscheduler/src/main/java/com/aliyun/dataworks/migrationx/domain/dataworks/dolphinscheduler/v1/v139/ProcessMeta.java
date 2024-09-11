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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils.StringTypeObjectAdapter;
import com.aliyun.migrationx.common.utils.GsonUtils;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author 聿剑
 * @date 2022/10/13
 */
@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode
public class ProcessMeta {
    /**
     * project name
     */
    private String projectName;
    /**
     * process definition id
     */
    private Integer processDefinitionId;
    /**
     * process definition name
     */
    private String processDefinitionName;

    /**
     * process definition json
     */
    @JsonAdapter(StringTypeObjectAdapter.class)
    private ProcessData processDefinitionJson;

    /**
     * process definition desc
     */
    private String processDefinitionDescription;

    /**
     * process definition locations
     */
    private String processDefinitionLocations;

    /**
     * process definition connects
     */
    @JsonAdapter(StringTypeObjectAdapter.class)
    private List<TaskNodeConnect> processDefinitionConnects;

    /**
     * warning type
     */
    private String scheduleWarningType;

    /**
     * warning group id
     */
    private Integer scheduleWarningGroupId;

    /**
     * warning group name
     */
    private String scheduleWarningGroupName;

    /**
     * start time
     */
    private String scheduleStartTime;

    /**
     * end time
     */
    private String scheduleEndTime;

    /**
     * crontab
     */
    private String scheduleCrontab;

    /**
     * failure strategy
     */
    private String scheduleFailureStrategy;

    /**
     * release state
     */
    private String scheduleReleaseState;

    /**
     * process instance priority
     */
    private String scheduleProcessInstancePriority;

    /**
     * worker group name
     */
    private String scheduleWorkerGroupName;

    public static void main(String[] args) {
        String str = "[\n"
                + "    {\n"
                + "        \"processDefinitionConnects\": \"[{\\\"endPointSourceId\\\":\\\"tasks-51935\\\","
                + "\\\"endPointTargetId\\\":\\\"tasks-60537\\\"},{\\\"endPointSourceId\\\":\\\"tasks-51935\\\","
                + "\\\"endPointTargetId\\\":\\\"tasks-88301\\\"},{\\\"endPointSourceId\\\":\\\"tasks-51935\\\","
                + "\\\"endPointTargetId\\\":\\\"tasks-51187\\\"},{\\\"endPointSourceId\\\":\\\"tasks-51935\\\","
                + "\\\"endPointTargetId\\\":\\\"tasks-42705\\\"},{\\\"endPointSourceId\\\":\\\"tasks-60537\\\","
                + "\\\"endPointTargetId\\\":\\\"tasks-97378\\\"},{\\\"endPointSourceId\\\":\\\"tasks-42705\\\","
                + "\\\"endPointTargetId\\\":\\\"tasks-30412\\\"},{\\\"endPointSourceId\\\":\\\"tasks-44512\\\","
                + "\\\"endPointTargetId\\\":\\\"tasks-51935\\\"}]\",\n"
                + "        \"processData\": \"{\\\"tenantId\\\":1,"
                + "\\\"globalParams\\\":[{\\\"prop\\\":\\\"flow_param_0\\\",\\\"direct\\\":\\\"IN\\\","
                + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"111\\\"}],"
                + "\\\"tasks\\\":[{\\\"conditionResult\\\":{\\\"successNode\\\":[\\\"\\\"],"
                + "\\\"failedNode\\\":[\\\"\\\"]},\\\"description\\\":\\\"shell\\\",\\\"runFlag\\\":\\\"NORMAL\\\","
                + "\\\"type\\\":\\\"SHELL\\\",\\\"params\\\":{\\\"rawScript\\\":\\\"whoami\\\","
                + "\\\"localParams\\\":[{\\\"prop\\\":\\\"shell_arg0\\\",\\\"direct\\\":\\\"IN\\\","
                + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"arg0\\\"}],\\\"resourceList\\\":[]},"
                + "\\\"timeout\\\":{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"},\\\"maxRetryTimes\\\":\\\"0\\\","
                + "\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"shell_0\\\",\\\"dependence\\\":{},"
                + "\\\"retryInterval\\\":\\\"1\\\",\\\"preTasks\\\":[\\\"depend_task_0\\\"],"
                + "\\\"id\\\":\\\"tasks-51935\\\",\\\"workerGroup\\\":\\\"default\\\"},"
                + "{\\\"conditionResult\\\":{\\\"successNode\\\":[\\\"\\\"],\\\"failedNode\\\":[\\\"\\\"]},"
                + "\\\"description\\\":\\\"\\\",\\\"runFlag\\\":\\\"NORMAL\\\",\\\"type\\\":\\\"PYTHON\\\","
                + "\\\"params\\\":{\\\"rawScript\\\":\\\"import sys\\\\n\\\\nprint(\\\\\\\"HELLO:\\\\\\\" + sys.argv[0])"
                + "\\\",\\\"localParams\\\":[{\\\"prop\\\":\\\"py_arg0\\\",\\\"direct\\\":\\\"IN\\\","
                + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"sam.liux\\\"}],\\\"resourceList\\\":[]},"
                + "\\\"timeout\\\":{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"},\\\"maxRetryTimes\\\":\\\"0\\\","
                + "\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"py_0\\\",\\\"dependence\\\":{},"
                + "\\\"retryInterval\\\":\\\"1\\\",\\\"preTasks\\\":[\\\"shell_0\\\"],\\\"id\\\":\\\"tasks-60537\\\","
                + "\\\"workerGroup\\\":\\\"default\\\"},{\\\"conditionResult\\\":{\\\"successNode\\\":[\\\"\\\"],"
                + "\\\"failedNode\\\":[\\\"\\\"]},\\\"description\\\":\\\"\\\",\\\"runFlag\\\":\\\"NORMAL\\\","
                + "\\\"type\\\":\\\"SQL\\\",\\\"params\\\":{\\\"postStatements\\\":[],\\\"connParams\\\":\\\"\\\","
                + "\\\"receiversCc\\\":\\\"\\\",\\\"udfs\\\":\\\"\\\",\\\"type\\\":\\\"SPARK\\\",\\\"title\\\":\\\"\\\","
                + "\\\"sql\\\":\\\"show tables;\\\",\\\"preStatements\\\":[],\\\"sqlType\\\":\\\"0\\\","
                + "\\\"sendEmail\\\":false,\\\"receivers\\\":\\\"\\\",\\\"datasource\\\":1,\\\"displayRows\\\":10,"
                + "\\\"limit\\\":10000,\\\"showType\\\":\\\"TABLE\\\",\\\"localParams\\\":[],"
                + "\\\"datasourceName\\\":\\\"spark_conn\\\"},\\\"timeout\\\":{\\\"enable\\\":false,"
                + "\\\"strategy\\\":\\\"\\\"},\\\"maxRetryTimes\\\":\\\"0\\\","
                + "\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"spark_sql_0\\\",\\\"dependence\\\":{},"
                + "\\\"retryInterval\\\":\\\"1\\\",\\\"preTasks\\\":[\\\"shell_0\\\"],\\\"id\\\":\\\"tasks-88301\\\","
                + "\\\"workerGroup\\\":\\\"default\\\"},{\\\"conditionResult\\\":{\\\"successNode\\\":[\\\"\\\"],"
                + "\\\"failedNode\\\":[\\\"\\\"]},\\\"description\\\":\\\"\\\",\\\"runFlag\\\":\\\"NORMAL\\\","
                + "\\\"type\\\":\\\"SQL\\\",\\\"params\\\":{\\\"postStatements\\\":[],"
                + "\\\"connParams\\\":\\\"sql_param0=11212\\\",\\\"receiversCc\\\":\\\"\\\",\\\"udfs\\\":\\\"1\\\","
                + "\\\"type\\\":\\\"HIVE\\\",\\\"title\\\":\\\"\\\",\\\"sql\\\":\\\"show tables;\\\","
                + "\\\"preStatements\\\":[],\\\"sqlType\\\":\\\"0\\\",\\\"sendEmail\\\":false,\\\"receivers\\\":\\\"\\\","
                + "\\\"datasource\\\":2,\\\"displayRows\\\":10,\\\"limit\\\":10000,\\\"showType\\\":\\\"TABLE\\\","
                + "\\\"localParams\\\":[],\\\"datasourceName\\\":\\\"hive_conn\\\"},"
                + "\\\"timeout\\\":{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"},\\\"maxRetryTimes\\\":\\\"0\\\","
                + "\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"hive_sql_0\\\",\\\"dependence\\\":{},"
                + "\\\"retryInterval\\\":\\\"1\\\",\\\"preTasks\\\":[\\\"shell_0\\\"],\\\"id\\\":\\\"tasks-51187\\\","
                + "\\\"workerGroup\\\":\\\"default\\\"},{\\\"conditionResult\\\":{\\\"successNode\\\":[\\\"\\\"],"
                + "\\\"failedNode\\\":[\\\"\\\"]},\\\"description\\\":\\\"\\\",\\\"runFlag\\\":\\\"NORMAL\\\","
                + "\\\"type\\\":\\\"MR\\\",\\\"params\\\":{\\\"mainArgs\\\":\\\"arg1 arg2 arg3 ${cargs1}\\\","
                + "\\\"programType\\\":\\\"JAVA\\\",\\\"mainClass\\\":\\\"testMain\\\",\\\"appName\\\":\\\"testMr0\\\","
                + "\\\"mainJar\\\":{\\\"id\\\":4},\\\"localParams\\\":[{\\\"prop\\\":\\\"cargs1\\\","
                + "\\\"direct\\\":\\\"IN\\\",\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"1\\\"}],"
                + "\\\"others\\\":\\\"opt1 opt2\\\",\\\"resourceList\\\":[{\\\"res\\\":\\\"test_folder/commons-pool2-2.4"
                + ".2.jar\\\",\\\"name\\\":\\\"commons-pool2-2.4.2.2.jar\\\",\\\"id\\\":4}]},"
                + "\\\"timeout\\\":{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"},\\\"maxRetryTimes\\\":\\\"0\\\","
                + "\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"mr_0\\\",\\\"dependence\\\":{},"
                + "\\\"retryInterval\\\":\\\"1\\\",\\\"preTasks\\\":[\\\"shell_0\\\"],\\\"id\\\":\\\"tasks-42705\\\","
                + "\\\"workerGroup\\\":\\\"default\\\"},{\\\"conditionResult\\\":{\\\"successNode\\\":[\\\"\\\"],"
                + "\\\"failedNode\\\":[\\\"\\\"]},\\\"description\\\":\\\"\\\",\\\"runFlag\\\":\\\"NORMAL\\\","
                + "\\\"type\\\":\\\"SQOOP\\\",\\\"params\\\":{\\\"jobName\\\":\\\"testSqoop\\\","
                + "\\\"hadoopCustomParams\\\":[{\\\"prop\\\":\\\"hadooparg1\\\",\\\"direct\\\":\\\"IN\\\","
                + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"hadooparg11111\\\"}],\\\"sourceType\\\":\\\"MYSQL\\\","
                + "\\\"targetType\\\":\\\"HIVE\\\",\\\"targetParams\\\":\\\"{\\\\\\\"hiveDatabase\\\\\\\":\\\\\\\"hive_db"
                + "\\\\\\\",\\\\\\\"hiveTable\\\\\\\":\\\\\\\"hive_table_0\\\\\\\",\\\\\\\"createHiveTable\\\\\\\":true,"
                + "\\\\\\\"dropDelimiter\\\\\\\":true,\\\\\\\"hiveOverWrite\\\\\\\":true,"
                + "\\\\\\\"replaceDelimiter\\\\\\\":\\\\\\\",\\\\\\\","
                + "\\\\\\\"hiveTargetDir\\\\\\\":\\\\\\\"/tmp/hive\\\\\\\","
                + "\\\\\\\"hivePartitionKey\\\\\\\":\\\\\\\"id\\\\\\\","
                + "\\\\\\\"hivePartitionValue\\\\\\\":\\\\\\\"111\\\\\\\"}\\\",\\\"modelType\\\":\\\"import\\\","
                + "\\\"sourceParams\\\":\\\"{\\\\\\\"srcType\\\\\\\":\\\\\\\"MYSQL\\\\\\\","
                + "\\\\\\\"srcDatasource\\\\\\\":3,\\\\\\\"srcTable\\\\\\\":\\\\\\\"test_tbv\\\\\\\","
                + "\\\\\\\"srcQueryType\\\\\\\":\\\\\\\"0\\\\\\\",\\\\\\\"srcQuerySql\\\\\\\":\\\\\\\"\\\\\\\","
                + "\\\\\\\"srcColumnType\\\\\\\":\\\\\\\"1\\\\\\\",\\\\\\\"srcColumns\\\\\\\":\\\\\\\"id,user_id,"
                + "name\\\\\\\",\\\\\\\"srcConditionList\\\\\\\":[],"
                + "\\\\\\\"mapColumnHive\\\\\\\":[{\\\\\\\"prop\\\\\\\":\\\\\\\"int\\\\\\\","
                + "\\\\\\\"direct\\\\\\\":\\\\\\\"IN\\\\\\\",\\\\\\\"type\\\\\\\":\\\\\\\"VARCHAR\\\\\\\","
                + "\\\\\\\"value\\\\\\\":\\\\\\\"int\\\\\\\"}],"
                + "\\\\\\\"mapColumnJava\\\\\\\":[{\\\\\\\"prop\\\\\\\":\\\\\\\"Integer\\\\\\\","
                + "\\\\\\\"direct\\\\\\\":\\\\\\\"IN\\\\\\\",\\\\\\\"type\\\\\\\":\\\\\\\"VARCHAR\\\\\\\","
                + "\\\\\\\"value\\\\\\\":\\\\\\\"Integer\\\\\\\"}]}\\\",\\\"jobType\\\":\\\"TEMPLATE\\\","
                + "\\\"localParams\\\":[],\\\"sqoopAdvancedParams\\\":[{\\\"prop\\\":\\\"sqooparg1\\\","
                + "\\\"direct\\\":\\\"IN\\\",\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"sqooparg111111\\\"}],"
                + "\\\"concurrency\\\":1},\\\"timeout\\\":{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"},"
                + "\\\"maxRetryTimes\\\":\\\"0\\\",\\\"taskInstancePriority\\\":\\\"MEDIUM\\\","
                + "\\\"name\\\":\\\"sqoop_form_0\\\",\\\"dependence\\\":{},\\\"retryInterval\\\":\\\"1\\\","
                + "\\\"preTasks\\\":[\\\"mr_0\\\"],\\\"id\\\":\\\"tasks-30412\\\",\\\"workerGroup\\\":\\\"default\\\"},"
                + "{\\\"conditionResult\\\":{\\\"successNode\\\":[\\\"\\\"],\\\"failedNode\\\":[\\\"\\\"]},"
                + "\\\"description\\\":\\\"\\\",\\\"runFlag\\\":\\\"NORMAL\\\",\\\"type\\\":\\\"SQOOP\\\","
                + "\\\"params\\\":{\\\"jobName\\\":\\\"testSqoopSqlMode\\\","
                + "\\\"hadoopCustomParams\\\":[{\\\"prop\\\":\\\"hadooparg1\\\",\\\"direct\\\":\\\"IN\\\","
                + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"hadooparg1111\\\"}],\\\"sourceType\\\":\\\"MYSQL\\\","
                + "\\\"targetType\\\":\\\"HIVE\\\",\\\"targetParams\\\":\\\"{\\\\\\\"hiveDatabase\\\\\\\":\\\\\\\"hivedb"
                + "\\\\\\\",\\\\\\\"hiveTable\\\\\\\":\\\\\\\"hive_table_0\\\\\\\",\\\\\\\"createHiveTable\\\\\\\":false,"
                + "\\\\\\\"dropDelimiter\\\\\\\":false,\\\\\\\"hiveOverWrite\\\\\\\":true,"
                + "\\\\\\\"replaceDelimiter\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"hiveTargetDir\\\\\\\":\\\\\\\"\\\\\\\","
                + "\\\\\\\"hivePartitionKey\\\\\\\":\\\\\\\"\\\\\\\","
                + "\\\\\\\"hivePartitionValue\\\\\\\":\\\\\\\"\\\\\\\"}\\\",\\\"modelType\\\":\\\"import\\\","
                + "\\\"sourceParams\\\":\\\"{\\\\\\\"srcType\\\\\\\":\\\\\\\"MYSQL\\\\\\\","
                + "\\\\\\\"srcDatasource\\\\\\\":3,\\\\\\\"srcTable\\\\\\\":\\\\\\\"\\\\\\\","
                + "\\\\\\\"srcQueryType\\\\\\\":\\\\\\\"1\\\\\\\",\\\\\\\"srcQuerySql\\\\\\\":\\\\\\\"select * from "
                + "table_1\\\\\\\",\\\\\\\"srcColumnType\\\\\\\":\\\\\\\"0\\\\\\\","
                + "\\\\\\\"srcColumns\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"srcConditionList\\\\\\\":[],"
                + "\\\\\\\"mapColumnHive\\\\\\\":[],\\\\\\\"mapColumnJava\\\\\\\":[]}\\\","
                + "\\\"jobType\\\":\\\"TEMPLATE\\\",\\\"localParams\\\":[],"
                + "\\\"sqoopAdvancedParams\\\":[{\\\"prop\\\":\\\"sqooparg1\\\",\\\"direct\\\":\\\"IN\\\","
                + "\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"sqooparg1111\\\"}],\\\"concurrency\\\":1},"
                + "\\\"timeout\\\":{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"},\\\"maxRetryTimes\\\":\\\"0\\\","
                + "\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"sqoop_sql_0\\\",\\\"dependence\\\":{},"
                + "\\\"retryInterval\\\":\\\"1\\\",\\\"preTasks\\\":[\\\"py_0\\\"],\\\"id\\\":\\\"tasks-97378\\\","
                + "\\\"workerGroup\\\":\\\"default\\\"},{\\\"conditionResult\\\":{\\\"successNode\\\":[\\\"\\\"],"
                + "\\\"failedNode\\\":[\\\"\\\"]},\\\"description\\\":\\\"\\\",\\\"runFlag\\\":\\\"NORMAL\\\","
                + "\\\"type\\\":\\\"DEPENDENT\\\",\\\"params\\\":{},\\\"timeout\\\":{\\\"enable\\\":false,"
                + "\\\"strategy\\\":\\\"\\\"},\\\"maxRetryTimes\\\":\\\"0\\\","
                + "\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"depend_task_0\\\","
                + "\\\"dependence\\\":{\\\"dependTaskList\\\":[{\\\"dependItemList\\\":[{\\\"dateValue\\\":\\\"today\\\","
                + "\\\"definitionName\\\":\\\"dws_business_okcard_import_1665651205812\\\","
                + "\\\"depTasks\\\":\\\"dws_okcard_user\\\",\\\"projectName\\\":\\\"project_a\\\",\\\"projectId\\\":1,"
                + "\\\"cycle\\\":\\\"day\\\",\\\"definitionId\\\":2}],\\\"relation\\\":\\\"AND\\\"},"
                + "{\\\"dependItemList\\\":[{\\\"dateValue\\\":\\\"today\\\","
                + "\\\"definitionName\\\":\\\"dws_business_okcard_import_1665651205812\\\","
                + "\\\"depTasks\\\":\\\"dws_okcard_bill\\\",\\\"projectName\\\":\\\"project_a\\\",\\\"projectId\\\":1,"
                + "\\\"cycle\\\":\\\"day\\\",\\\"definitionId\\\":2}],\\\"relation\\\":\\\"AND\\\"},"
                + "{\\\"dependItemList\\\":[{\\\"dateValue\\\":\\\"today\\\","
                + "\\\"definitionName\\\":\\\"dws_business_okcard_import_1665651205812\\\",\\\"depTasks\\\":\\\"ALL\\\","
                + "\\\"projectName\\\":\\\"project_a\\\",\\\"projectId\\\":1,\\\"cycle\\\":\\\"day\\\","
                + "\\\"definitionId\\\":2}],\\\"relation\\\":\\\"AND\\\"}],\\\"relation\\\":\\\"AND\\\"},"
                + "\\\"retryInterval\\\":\\\"1\\\",\\\"preTasks\\\":[],\\\"id\\\":\\\"tasks-44512\\\","
                + "\\\"workerGroup\\\":\\\"default\\\"}],\\\"timeout\\\":0}\",\n"
                + "        \"processDefinitionLocations\": \"{\\\"tasks-51935\\\":{\\\"name\\\":\\\"shell_0\\\","
                + "\\\"targetarr\\\":\\\"tasks-44512\\\",\\\"nodenumber\\\":\\\"4\\\",\\\"x\\\":722,\\\"y\\\":300},"
                + "\\\"tasks-60537\\\":{\\\"name\\\":\\\"py_0\\\",\\\"targetarr\\\":\\\"tasks-51935\\\","
                + "\\\"nodenumber\\\":\\\"1\\\",\\\"x\\\":1052,\\\"y\\\":388},"
                + "\\\"tasks-88301\\\":{\\\"name\\\":\\\"spark_sql_0\\\",\\\"targetarr\\\":\\\"tasks-51935\\\","
                + "\\\"nodenumber\\\":\\\"0\\\",\\\"x\\\":799,\\\"y\\\":570},"
                + "\\\"tasks-51187\\\":{\\\"name\\\":\\\"hive_sql_0\\\",\\\"targetarr\\\":\\\"tasks-51935\\\","
                + "\\\"nodenumber\\\":\\\"0\\\",\\\"x\\\":1119,\\\"y\\\":690},"
                + "\\\"tasks-42705\\\":{\\\"name\\\":\\\"mr_0\\\",\\\"targetarr\\\":\\\"tasks-51935\\\","
                + "\\\"nodenumber\\\":\\\"1\\\",\\\"x\\\":1218,\\\"y\\\":268},"
                + "\\\"tasks-30412\\\":{\\\"name\\\":\\\"sqoop_form_0\\\",\\\"targetarr\\\":\\\"tasks-42705\\\","
                + "\\\"nodenumber\\\":\\\"0\\\",\\\"x\\\":1474,\\\"y\\\":297},"
                + "\\\"tasks-97378\\\":{\\\"name\\\":\\\"sqoop_sql_0\\\",\\\"targetarr\\\":\\\"tasks-60537\\\","
                + "\\\"nodenumber\\\":\\\"0\\\",\\\"x\\\":1432,\\\"y\\\":459},"
                + "\\\"tasks-44512\\\":{\\\"name\\\":\\\"depend_task_0\\\",\\\"targetarr\\\":\\\"\\\","
                + "\\\"nodenumber\\\":\\\"1\\\",\\\"x\\\":463,\\\"y\\\":453}}\",\n"
                + "        \"processDefinitionName\": \"hello_flow_0\",\n"
                + "        \"projectName\": \"project_a\"\n"
                + "    }\n"
                + "]";
        List<ProcessMeta> res = GsonUtils.fromJsonString(str, new TypeToken<List<ProcessMeta>>() {}.getType());
        System.out.println(GsonUtils.toJsonString(res));
    }
}
