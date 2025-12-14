# Hologres DDL节点 (HOLOGRES_SYNC_DDL)

## 概述

Hologres DDL节点(HOLOGRES_SYNC_DDL)用于在Hologres实例中执行数据定义语言(DDL)操作，如创建、修改或删除表结构。

## 节点类型信息

- **CodeProgramType**: HOLOGRES_SYNC_DDL (commandTypeId: 801)
- **CalcEngineType**: HOLOGRES
- **Language**: sql
- **文件后缀**: .sql

## 完整示例

```json
{
  "version": "1.1.0",
  "kind": "CycleWorkflow",
  "spec": {
    "nodes": [
      {
        "recurrence": "Normal",
        "id": "hologres_ddl_1",
        "instanceMode": "T+1",
        "rerunMode": "Allowed",
        "rerunTimes": 0,
        "rerunInterval": 0,
        "autoParse": true,
        "script": {
          "path": "path/to/hologres_ddl",
          "runtime": {
            "command": "HOLOGRES_SYNC_DDL"
          },
          "content": "select 1"
        },
        "trigger": {
          "type": "Scheduler",
          "cron": "00 00 00 * * ?",
          "startTime": "1970-01-01 00:00:00",
          "endTime": "9999-01-01 00:00:00"
        },
        "name": "Hologres DDL Node",
        "owner": "owner_id"
      }
    ],
    "flow": [
      {
        "nodeId": "hologres_ddl_1",
        "depends": [
          {
            "type": "Normal",
            "output": "some_input"
          }
        ]
      }
    ]
  }
}
```

## Script 配置

### DDL 任务定义

```json
{
  "script": {
    "path": "path/to/hologres_ddl.sql",
    "runtime": {
      "command": "HOLOGRES_SYNC_DDL"
    },
    "content": "CREATE TABLE example_table (id int, name text);"
  }
}
```

## 实际用例

Hologres DDL节点通常用于:
1. 在工作流开始前创建所需的表结构
2. 在数据处理完成后修改表的结构或属性
3. 执行表的分区管理操作

## 最佳实践

1. **事务管理**: 使用事务确保DDL操作的原子性
2. **备份策略**: 在执行重要的DDL操作前备份数据
3. **兼容性检查**: 检查DDL操作与现有数据的兼容性

## 注意事项

- DDL操作可能影响正在运行的查询，需要在低峰期执行
- 某些DDL操作可能需要较高的权限
- 复杂的DDL操作可能需要较长时间，注意设置合理的超时时间