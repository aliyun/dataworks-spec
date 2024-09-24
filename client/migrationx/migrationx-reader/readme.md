# Reader命令说明
## usage
```shell
usage: Options
migrationx
 -a,--app <arg>   app name
Available apps:  [airflow, dolphinscheduler, aliyunemr]
```

* -a 转换app，比如dolphinscheduler_to_dataworks为转换到dataworks的转换器
* -c 指定转换配置文件，下文有介绍具体格式，主要定义转换逻辑里面节点类型映射配置
* -s 指定源转换
命令生成的targetPackage可以直接拿到 DataWorks迁移助手 -> DataWorks迁移 -> 导入界面去进行导入项目空间操作。

## example:
* read from dolphinscheduler
```shell
python ./migrationx-reader/bin/reader.py \
-a dolphinscheduler \
-e http://dolphinscheduler_api_host \
-t {token} \
-v 1.3.9  \
-p project_a \
-f project_a.zip
```

* read from airflow
```shell
python ./migrationx-reader/bin/reader.py \
-a airflow \
-d /path/to/dag_folder \
-o output.json
```

* read from aliyun EMR 
```shell
python ./migrationx-reader/bin/reader.py \
-a aliyunemr \
-d ./target/dump \
-e emr.aliyuncs.com \
-i ${accessId} \
-k ${accessKey} \
-r ${regionId} \
-p emr_prj01,emr_prj02
```

