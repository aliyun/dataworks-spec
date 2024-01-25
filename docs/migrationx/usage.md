# MigrationX

Use case of client tool `MigrationX` transforming data from DolphinScheduler to DataWorks with all steps in one command line run

## Dolphinscheduler to DataWorks transformation in one command line run

### Step1: shell environment variables

```shell
# dolphinscheduler config variables
export DOLPHINSCHEDULER_API_ENDPOINT=http://server:port
export DOLPHINSCHEDULER_API_TOKEN=md5_token_get_from_dolphinscheduler 
export DOLPHINSCHEDULER_VERSION=1.3.9
export DOLPHINSCHEDULER_PROJECT_NAME=your_dolphinscheduler_project_name

# aliyun config variables
export ALIYUN_ACCESS_KEY_ID=your_access_key_id
export ALIYUN_ACCESS_KEY_SECRET=your_access_key_secret
export ALIYUN_REGION_ID=cn-shanghai
# dataworks workspace id to be imported
export ALIYUN_DATAWORKS_WORKSPACE_ID=your_dataworks_workspace_id
```

### Step2: check migrationx.json config

[${MIGRATIONX_HOME}/conf/migrationx.json](../../client/src/main/conf/migrationx.json) contains configuration of each command step, there is no need to
change it by default.

```json
{
  "reader": {
    "name": "dolphinscheduler",
    "params": [
      "-a dolphinscheduler",
      "-e ${DOLPHINSCHEDULER_API_ENDPOINT}",
      "-t ${DOLPHINSCHEDULER_API_TOKEN}",
      "-v ${DOLPHINSCHEDULER_VERSION}",
      "-p ${DOLPHINSCHEDULER_PROJECT_NAME}",
      "-f ${PWD}/${DOLPHINSCHEDULER_PROJECT_NAME}.zip"
    ]
  },
  "transformer": {
    "name": "dolphinscheduler_to_dataworks",
    "params": [
      "-a dolphinscheduler_to_dataworks",
      "-c ${MIGRATIONX_HOME}/conf/dataworks-transformer-config.json",
      "-s ${PWD}/${DOLPHINSCHEDULER_PROJECT_NAME}.zip",
      "-t ${PWD}/${DOLPHINSCHEDULER_PROJECT_NAME}_dw.zip"
    ]
  },
  "writer": {
    "name": "dataworks",
    "params": [
      "-a dataworks",
      "-e dataworks.${ALIYUN_REGION_ID}.aliyuncs.com",
      "-i ${ALIYUN_ACCESS_KEY_ID}",
      "-k ${ALIYUN_ACCESS_KEY_SECRET}",
      "-p ${ALIYUN_DATAWORKS_WORKSPACE_ID}",
      "-r ${ALIYUN_REGION_ID}",
      "-f ${PWD}/${DOLPHINSCHEDULER_PROJECT_NAME}_dw.zip",
      "-t SPEC"
    ]
  }
}
```

what migrationx tools does in this command line run:

1. export workflows from Dolphinscheduler project configured in variable ```${DOLPHINSCHEDULER_PROJECT_NAME}``` to a zip file
2. convert the exported zip file to standard FlowSpec directory zip file
3. import the standard FlowSpec directory zip file to dataworks project configured in variable ```${ALIYUN_DATAWORKS_WORKSPACE_ID}```

### Step3: start migrationx

```shell
bin/migrationx.py
```

# Reader

Read workflow configurations from the workflow scheduling system

## usage

```shell
usage: Options
migrationx
 -a,--app <arg>   app name

Available apps:
 {
  "reader": [
    "airflow",
    "dolphinscheduler",
    "aliyunemr"
  ]
}
```

* -a option specify the source workflow scheduler system, for example: airflow, dolphinscheduler, aliyunemr

## example:

* dolphinscheduler

```shell
bin/reader.py \
-a dolphinscheduler \
-e http://dolphinscheduler_api_host \
-t {token} \
-v 1.3.9  \
-p project_a \
-f project_a.zip
```

* airflow

```shell
bin/reader.py \
-a airflow \
-d /path/to/dag_folder \
-o output.json
```

* aliyun EMR

```shell
bin/reader.py \
-a aliyunemr \
-d ./target/dump \
-e emr.aliyuncs.com \
-i ${accessId} \
-k ${accessKey} \
-r ${regionId} \
-p emr_prj01,emr_prj02
```

# Transformer

Transform the workflow configuration from one workflow scheduling system to another. Currently implemented the conversion from dolphinscheduler(
v1.3.x) to dataworks

## usage

```shell
usage: Options
Transformer Command App
 -a,--app <arg>             transformer app name, dolphinscheduler_to_dataworks etc.   
 -c,--config <arg>          transform configuration file path
 -s,--sourcePackage <arg>   source package file path
 -t,--targetPackage <arg>   target package file path
```

* -a transformer app name，for example dolphinscheduler_to_dataworks
* -c specify the transform configuration file path，for example dataworks-transformer-config.json
* -s source package file
* -t target package file path

## example:

convert from dolphinscheduler zip to dataworks

```shell
bin/transformer.py \
-a dolphinscheduler_to_dataworks\
-c dataworks-transformer-config.json\
-s project_a.zip\
-t dw.zip
```

![DataWorks Migration Assistant Spec Package Example](../images/spec/dw_spec_package_demo.jpg)

### dataworks transform configuration file

transformation to EMR related node configuration:

```json
{
  "format": "SPEC",
  "locale": "zh_CN",
  "settings": {
    "workflow.converter.shellNodeType": "EMR_SHELL",
    "workflow.converter.commandSqlAs": "EMR_HIVE",
    "workflow.converter.sparkSubmitAs": "EMR_SPARK",
    "workflow.converter.target.unknownNodeTypeAs": "DIDE_SHELL",
    "workflow.converter.mrNodeType": "EMR_MR",
    "workflow.converter.target.engine.type": "EMR",
    "workflow.converter.dolphinscheduler.sqlNodeTypeMapping": {
      "POSTGRESQL": "EMR_HIVE",
      "MYSQL": "EMR_HIVE",
      "HIVE": "EMR_HIVE"
    }
  }
}
```

Other engine node configuration examples:

* [MaxCompute](../../client/migrationx-transformer/src/main/conf/dataworks-transformer-config-maxcompute-sample.json)
* [EMR](../../client/migrationx-transformer/src/main/conf/dataworks-transformer-config-emr-sample.json)
* [Hologres](../../client/migrationx-transformer/src/main/conf/dataworks-transformer-config-hologres-sample.json)

# Writer

Write workflow configurations to the workflow scheduling system. Currently implemented the writing to dataworks based on the OpenAPI of the migration
assistant

## usage

```shell
usage: Options
migrationx
 -a,--app <arg>   app name

Available apps:
 {
  "writer": [
    "dataworks"
  ]
}
```

## write to dataworks

```shell
bin/writer.py \
 -a dataworks \
 -e dataworks.cn-shanghai.aliyuncs.com \
 -i $ALIYUN_ACCESS_KEY_ID \
 -k $ALIYUN_ACCESS_KEY_SECRET \
 -p $ALIYUN_DATAWORKS_WORKSPACE_ID \
 -r cn-shanghai \
 -f demo_space.zip
```

* -a writer app name 'dataworks'
* -e dataworks open api endpoint
* -i aliyun access id
* -k aliyun access key
* -p aliyun dataworks workspace id
* -r aliyun region id
* -f dataworks import package file

