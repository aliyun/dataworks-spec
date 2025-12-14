# MaxCompute SQL 节点 (ODPS_SQL)

## 概述

ODPS_SQL 节点用于在 MaxCompute 计算引擎上执行 SQL 语句,是 DataWorks 最常用的节点类型之一。

## 节点类型信息

- **CodeProgramType**: ODPS_SQL (commandTypeId: 10)
- **CalcEngineType**: ODPS (MaxCompute)
- **Language**: odps-sql, sql
- **文件后缀**: .sql

## 完整示例

```json
{
  "id": "node_odps_sql_001",
  "name": "daily_data_aggregation",
  "recurrence": "Normal",
  "priority": 5,
  "timeout": 3600,
  "instanceMode": "T+1",
  "rerunMode": "Allowed",
  "rerunTimes": 3,
  "rerunInterval": 180000,
  "datasource": {
    "name": "odps_first",
    "type": "odps"
  },
  "script": {
    "id": "script_odps_sql_001",
    "path": "/业务流程/数据开发/daily_aggregation.sql",
    "language": "odps-sql",
    "runtime": {
      "engine": "MaxCompute",
      "command": "ODPS_SQL",
      "commandTypeId": 10,
      "maxComputeConf": {
        "odps.sql.mapper.split.size": "256",
        "odps.sql.reducer.instances": "100"
      }
    },
    "parameters": [
      "{{variables.bizdate}}",
      "{{variables.region}}"
    ]
  },
  "trigger": {
    "type": "Scheduler",
    "id": "trigger_daily"
  },
  "runtimeResource": {
    "resourceGroup": "group_524257424564736",
    "id": "runtime_res_odps",
    "resourceGroupId": "310003"
  },
  "inputs": {
    "tables": [
      {
        "artifactType": "Table",
        "guid": "odps.project_name.source_table_001"
      }
    ],
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
    "tables": [
      {
        "artifactType": "Table",
        "guid": "odps.project_name.target_table_001"
      }
    ],
    "nodeOutputs": [
      {
        "artifactType": "NodeOutput",
        "data": "node_odps_sql_001",
        "refTableName": "target_table_001"
      }
    ]
  }
}
```

## Script Runtime 配置

### 基础配置

```json
{
  "runtime": {
    "engine": "MaxCompute",
    "command": "ODPS_SQL",
    "commandTypeId": 10
  }
}
```

### MaxCompute 参数配置

通过 `maxComputeConf` 设置 MaxCompute SQL 参数:

```json
{
  "runtime": {
    "engine": "MaxCompute",
    "command": "ODPS_SQL",
    "commandTypeId": 10,
    "maxComputeConf": {
      "odps.sql.mapper.split.size": "256",
      "odps.sql.reducer.instances": "100",
      "odps.sql.jobconf.odps2": "true",
      "odps.sql.decimal.odps2": "true",
      "odps.sql.hive.compatible": "true",
      "odps.sql.executionengine.coldata.deep.buffer.size.max": "67108864",
      "odps.optimizer.enable.ppd": "true"
    }
  }
}
```

常用 MaxCompute 参数:

| 参数 | 说明 | 示例值 |
|------|------|--------|
| odps.sql.mapper.split.size | Mapper split 大小(MB) | 256 |
| odps.sql.reducer.instances | Reducer 实例数 | 100 |
| odps.sql.jobconf.odps2 | 启用 MaxCompute 2.0 | true |
| odps.sql.decimal.odps2 | 启用 2.0 decimal 类型 | true |
| odps.sql.hive.compatible | Hive 兼容模式 | true |
| odps.optimizer.enable.ppd | 启用谓词下推优化 | true |

## 数据源配置

```json
{
  "datasource": {
    "name": "odps_first",
    "type": "odps"
  }
}
```

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| name | string | 是 | 数据源名称 |
| type | string | 是 | 固定为 "odps" |

## 输入输出配置

### 输入 (Inputs)

```json
{
  "inputs": {
    "tables": [
      {
        "artifactType": "Table",
        "guid": "odps.project_name.table_name"
      }
    ],
    "variables": [
      {
        "artifactType": "Variable",
        "name": "bizdate"
      }
    ]
  }
}
```

### 输出 (Outputs)

```json
{
  "outputs": {
    "tables": [
      {
        "artifactType": "Table",
        "guid": "odps.project_name.output_table"
      }
    ],
    "nodeOutputs": [
      {
        "artifactType": "NodeOutput",
        "data": "node_id",
        "refTableName": "output_table"
      }
    ]
  }
}
```

## SQL 脚本示例

```sql
-- 使用参数: ${bizdate}, ${region}
INSERT OVERWRITE TABLE target_table PARTITION (dt='${bizdate}')
SELECT 
    user_id,
    COUNT(*) as order_count,
    SUM(amount) as total_amount
FROM source_table
WHERE dt = '${bizdate}'
  AND region = '${region}'
GROUP BY user_id;
```

## 最佳实践

1. **分区管理**: 输出表使用分区,在 SQL 中使用 `INSERT OVERWRITE TABLE ... PARTITION`
2. **参数化**: 使用变量参数化日期、区域等动态值
3. **性能优化**:
   - 合理设置 `odps.sql.mapper.split.size` 和 `odps.sql.reducer.instances`
   - 启用 PPD(谓词下推)优化
   - 使用 MapJoin 优化小表关联
4. **资源隔离**: 生产环境使用专用资源组(runtimeResource)
5. **超时设置**: 根据 SQL 复杂度合理设置 timeout,建议 3600-7200 秒

## 注意事项

- MaxCompute SQL 语法与标准 SQL 有差异,参考官方文档
- 分区字段不能在 SELECT 列表中
- `odps.sql.jobconf.odps2` 开启后需注意数据类型兼容性
- 大表关联建议使用 MapJoin hint: `/*+ MAPJOIN(small_table) */`
