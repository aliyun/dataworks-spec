# EMR Spark 节点

## 概述

EMR_SPARK 节点用于在 EMR 集群上提交 Spark 作业,支持 Spark Submit 方式运行 Jar 包或 Python 脚本。

## 节点类型信息

- **CodeProgramType**: EMR_SPARK (commandTypeId: 228)
- **CalcEngineType**: EMR
- **Language**: shell, python, java
- **文件后缀**: .sh

## 完整示例

```json
{
  "id": "node_emr_spark_001",
  "name": "spark_batch_processing",
  "recurrence": "Normal",
  "priority": 3,
  "timeout": 7200,
  "instanceMode": "T+1",
  "rerunMode": "Allowed",
  "rerunTimes": 2,
  "rerunInterval": 300000,
  "script": {
    "id": "script_emr_spark_001",
    "path": "/emr/spark_jobs/batch_processing.sh",
    "language": "shell",
    "runtime": {
      "engine": "EMR",
      "command": "EMR_SPARK",
      "commandTypeId": 228,
      "emrJobConfig": {
        "jobMode": "LOCAL",
        "jobType": "SPARK",
        "executeMode": "Single",
        "clusterId": "C-ABC123DEF456",
        "args": [
          "--date",
          "${bizdate}",
          "--input",
          "oss://bucket/input/",
          "--output",
          "oss://bucket/output/"
        ],
        "sparkConf": {
          "spark.executor.memory": "4g",
          "spark.executor.cores": "2",
          "spark.driver.memory": "2g",
          "spark.sql.shuffle.partitions": "200"
        },
        "files": [
          "oss://bucket/conf/application.conf"
        ],
        "jars": [
          "oss://bucket/libs/spark-job.jar"
        ],
        "mainClass": "com.example.SparkBatchJob",
        "mainJar": "oss://bucket/libs/spark-job.jar"
      }
    }
  },
  "trigger": {
    "type": "Scheduler",
    "id": "trigger_daily"
  },
  "runtimeResource": {
    "resourceGroup": "group_emr",
    "id": "runtime_res_emr",
    "resourceGroupId": "310004"
  },
  "inputs": {
    "variables": [
      {
        "artifactType": "Variable",
        "name": "bizdate",
        "scope": "NodeParameter",
        "type": "System",
        "value": "${yyyymmdd}"
      }
    ]
  },
  "outputs": {
    "nodeOutputs": [
      {
        "artifactType": "NodeOutput",
        "data": "node_emr_spark_001",
        "refTableName": "spark_output"
      }
    ]
  }
}
```

## EMR Job Config 配置

### 基础配置

```json
{
  "emrJobConfig": {
    "jobMode": "LOCAL",
    "jobType": "SPARK",
    "executeMode": "Single",
    "clusterId": "C-ABC123DEF456"
  }
}
```

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| jobMode | string | 是 | 提交模式: LOCAL(Header/Gateway 提交) |
| jobType | string | 是 | 作业类型: SPARK |
| executeMode | string | 是 | 执行模式: Single |
| clusterId | string | 是 | EMR 集群 ID |

### Spark Submit 参数

```json
{
  "emrJobConfig": {
    "mainJar": "oss://bucket/libs/spark-job.jar",
    "mainClass": "com.example.SparkBatchJob",
    "args": [
      "--date", "${bizdate}",
      "--input", "oss://bucket/input/",
      "--output", "oss://bucket/output/"
    ],
    "files": [
      "oss://bucket/conf/application.conf",
      "oss://bucket/conf/log4j.properties"
    ],
    "jars": [
      "oss://bucket/libs/dependency1.jar",
      "oss://bucket/libs/dependency2.jar"
    ]
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| mainJar | string | 主 Jar 包路径(支持 OSS/HDFS 路径) |
| mainClass | string | Main 类全限定名 |
| args | array | 应用程序参数 |
| files | array | 需要分发到 Executor 的文件列表 |
| jars | array | 依赖 Jar 包列表 |

### Spark 配置参数

通过 `sparkConf` 设置 Spark 运行时参数:

```json
{
  "emrJobConfig": {
    "sparkConf": {
      "spark.executor.memory": "4g",
      "spark.executor.cores": "2",
      "spark.executor.instances": "10",
      "spark.driver.memory": "2g",
      "spark.driver.cores": "1",
      "spark.sql.shuffle.partitions": "200",
      "spark.default.parallelism": "200",
      "spark.dynamicAllocation.enabled": "true",
      "spark.shuffle.service.enabled": "true",
      "spark.serializer": "org.apache.spark.serializer.KryoSerializer"
    }
  }
}
```

常用 Spark 参数:

| 参数 | 说明 | 推荐值 |
|------|------|--------|
| spark.executor.memory | Executor 内存 | 4g-8g |
| spark.executor.cores | Executor CPU 核数 | 2-4 |
| spark.executor.instances | Executor 实例数 | 根据数据量调整 |
| spark.driver.memory | Driver 内存 | 1g-2g |
| spark.sql.shuffle.partitions | Shuffle 分区数 | 200-500 |
| spark.dynamicAllocation.enabled | 动态资源分配 | true |
| spark.serializer | 序列化器 | KryoSerializer |

## Spark Streaming 作业

对于 Spark Streaming 作业,使用 EMR_SPARK_STREAMING 类型:

```json
{
  "script": {
    "runtime": {
      "command": "EMR_SPARK_STREAMING",
      "commandTypeId": 264,
      "emrJobConfig": {
        "jobType": "SPARK_STREAMING",
        "sparkConf": {
          "spark.streaming.kafka.maxRatePerPartition": "10000",
          "spark.streaming.backpressure.enabled": "true",
          "spark.streaming.receiver.maxRate": "10000"
        }
      }
    }
  }
}
```

## PySpark 作业

```json
{
  "script": {
    "path": "/emr/pyspark/data_processing.py",
    "language": "python",
    "runtime": {
      "command": "EMR_SPARK",
      "emrJobConfig": {
        "mainJar": "local:///opt/spark/python/lib/pyspark.zip",
        "args": [
          "oss://bucket/scripts/data_processing.py",
          "--date", "${bizdate}"
        ],
        "files": [
          "oss://bucket/scripts/utils.py"
        ],
        "sparkConf": {
          "spark.pyspark.python": "/usr/bin/python3",
          "spark.pyspark.driver.python": "/usr/bin/python3"
        }
      }
    }
  }
}
```

## 脚本示例

### Spark Submit Shell 脚本

```bash
#!/bin/bash
# EMR Spark 作业提交脚本

spark-submit \
  --class com.example.SparkBatchJob \
  --master yarn \
  --deploy-mode cluster \
  --executor-memory 4G \
  --executor-cores 2 \
  --num-executors 10 \
  --driver-memory 2G \
  --conf spark.sql.shuffle.partitions=200 \
  --conf spark.dynamicAllocation.enabled=true \
  --jars oss://bucket/libs/dependency1.jar,oss://bucket/libs/dependency2.jar \
  --files oss://bucket/conf/application.conf \
  oss://bucket/libs/spark-job.jar \
  --date ${bizdate} \
  --input oss://bucket/input/ \
  --output oss://bucket/output/
```

## 最佳实践

1. **资源配置**:
   - 根据数据量合理设置 executor.memory 和 executor.cores
   - 启用动态资源分配(dynamicAllocation)提高资源利用率

2. **性能优化**:
   - 使用 Kryo 序列化器提升序列化性能
   - 合理设置 shuffle.partitions,避免小文件问题
   - 启用 backpressure 机制(Streaming 作业)

3. **存储路径**:
   - 优先使用 OSS 路径存储 Jar 包和配置文件
   - 使用 `local://` 引用集群本地文件

4. **监控与调试**:
   - 设置合理的 timeout(建议 7200-14400 秒)
   - 启用 Spark UI 监控作业执行
   - 配置 log4j 输出详细日志

## 注意事项

- EMR 集群必须处于运行状态
- OSS 路径需要配置正确的访问权限
- PySpark 作业需确保 Python 版本一致
- 大作业建议使用 cluster 模式而非 client 模式
- 注意 Spark 版本与 EMR 集群版本兼容性

# ODPS Spark 节点 (ODPS_SPARK)

## 概述

ODPS_SPARK 节点用于在 MaxCompute 环境中提交 Spark 作业,支持 Spark Submit 方式运行 Jar 包或 Python 脚本。

## 节点类型信息

- **CodeProgramType**: ODPS_SPARK (commandTypeId: 228)
- **CalcEngineType**: ODPS (MaxCompute)
- **Language**: shell, python, java
- **文件后缀**: .sh

## 完整示例

```json
{
  "id": "node_odps_spark_1",
  "name": "odps_spark_processing",
  "recurrence": "Normal",
  "priority": 3,
  "timeout": 7200,
  "instanceMode": "T+1",
  "rerunMode": "Allowed",
  "rerunTimes": 2,
  "rerunInterval": 300000,
  "script": {
    "path": "path/to/odps_spark",
    "runtime": {
      "command": "ODPS_SPARK"
    },
    "content": {
      "resourceReferences": [
        "xxxx.jar",
        "yyyy.zip",
        "zzzz.tar.gz"
      ],
      "sparkJson": {
        "mainJar": "xxxx.jar",
        "mainClass": "org.apache.spark.examples.JavaSparkPi",
        "version": "2.x",
        "language": "java",
        "args": "test_res_01",
        "configs": [
          "spark.hadoop.odps.task.major.version=cupid_v2",
          "xxxxx=yyyy"
        ],
        "assistJars": [
          "test_res_01"
        ],
        "assistFiles": [
          "test.zip"
        ],
        "assistArchives": [
          "test.zip"
        ],
        "archivesName": [
          "test.zip"
        ]
      }
    }
  },
  "trigger": {
    "type": "Scheduler",
    "cron": "00 00 00 * * ?",
    "startTime": "1970-01-01 00:00:00",
    "endTime": "9999-01-01 00:00:00"
  },
  "name": "ODPS Spark Node",
  "owner": "owner_id"
}
```

## ODPS Spark 配置

### 基础配置

```json
{
  "script": {
    "runtime": {
      "command": "ODPS_SPARK"
    },
    "content": {
      "resourceReferences": [
        "xxxx.jar"
      ],
      "sparkJson": {
        "mainJar": "xxxx.jar",
        "mainClass": "org.apache.spark.examples.JavaSparkPi",
        "version": "2.x",
        "language": "java",
        "args": "test_res_01"
      }
    }
  }
}
```

### Spark JSON 配置

```json
{
  "sparkJson": {
    "mainJar": "xxxx.jar",
    "mainClass": "org.apache.spark.examples.JavaSparkPi",
    "version": "2.x",
    "language": "java",
    "args": "test_res_01",
    "configs": [
      "spark.hadoop.odps.task.major.version=cupid_v2",
      "xxxxx=yyyy"
    ],
    "assistJars": [
      "test_res_01"
    ],
    "assistFiles": [
      "test.zip"
    ],
    "assistArchives": [
      "test.zip"
    ],
    "archivesName": [
      "test.zip"
    ]
  }
}
```

## 实际用例

ODPS Spark节点通常用于:
1. 在MaxCompute环境下执行Spark大数据处理作业
2. 利用Spark的高级API进行复杂的数据转换
3. 处理大规模数据集的机器学习任务

## 最佳实践

1. **资源配置**: 合理设置Spark作业的内存和CPU配置
2. **依赖管理**: 确保所有依赖的jar包和文件都正确上传并引用
3. **性能优化**: 合理设置Spark配置参数以优化执行性能

## 注意事项

- ODPS Spark作业需要在MaxCompute环境中运行
- 确保资源文件路径正确且有访问权限
- 注意Spark版本与MaxCompute环境的兼容性
