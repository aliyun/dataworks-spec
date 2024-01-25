# MigrationX

流程聚合场景

## Dolphinscheduler to DataWorks一键迁移

### Step1: 配置环境变量

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

### Step2: 查看migrationx.json配置文件

[${MIGRATIONX_HOME}/conf/migrationx.json](../../client/src/main/conf/migrationx.json)目录下有migrationx.json配置了每个步骤的命令行参数，默认情况下不需要改动。

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

这里migrationx做了3件事：

1. 从dolphinscheduler读取```${DOLPHINSCHEDULER_PROJECT_NAME}```项目的工作流配置生成一个zip文件
2. 将zip文件转换为dataworks的工作流的zip文件
3. 将dataworks的工作流zip文件导入到```${ALIYUN_DATAWORKS_WORKSPACE_ID}```指定的dataworks的工作空间

### Step3: 执行Migrationx，启动迁移

```shell
bin/migrationx.py
```

# Reader

从调度系统读取工作流配置数据

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

* -a 读取源端的调度工作流系统的工作流信息app，比如airflow, dolphinscheduler, aliyunemr

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

转换命令将一种调度系统的工作流导出包转换为另外一种调度系统工作流包。目前实现了dolphinscheduler(v1.3.x)到dataworks的包转换，后续支持其他转换器。

## usage

```shell
usage: Options
Transformer Command App
 -a,--app <arg>             transformer app name, dolphinscheduler_to_dataworks etc.   
 -c,--config <arg>          transform configuration file path
 -s,--sourcePackage <arg>   source package file path
 -t,--targetPackage <arg>   target package file path
```

* -a 转换app，比如dolphinscheduler_to_dataworks为转换到dataworks的转换器
* -c 指定转换配置文件，下文有介绍具体格式，主要定义转换逻辑里面节点类型映射配置
* -s 指定源转换
  命令生成的targetPackage可以直接拿到 DataWorks迁移助手 -> DataWorks迁移 -> 导入界面去进行导入项目空间操作。也可以用writer命令导入DataWorks数据开发。

## example:

convert from dolphinscheduler zip to dataworks

```shell
bin/transformer.py \
-a dolphinscheduler_to_dataworks\
-c dataworks-transformer-config.json\
-s project_a.zip\
-t dw.zip
```

![迁移助手Spec包示例](../images/spec/dw_spec_package_demo.jpg)

### dataworks transform configuration file

转换为EMR引擎相关类型节点配置:

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

这里还有转换其他引擎的节点配置示例：

* [MaxCompute](../../client/migrationx-transformer/src/main/conf/dataworks-transformer-config-maxcompute-sample.json)
* [EMR](../../client/migrationx-transformer/src/main/conf/dataworks-transformer-config-emr-sample.json)
* [Hologres](../../client/migrationx-transformer/src/main/conf/dataworks-transformer-config-hologres-sample.json)

# Writer

往调度系统写入工作流配置数据。目前实现了基于DataWorks迁移助手OpenAPI的写入逻辑

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

