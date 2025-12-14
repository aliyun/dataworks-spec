# 周期调度工作流 (CycleWorkflow)

## 概述

CycleWorkflow 是按照 cron 表达式定期调度执行的工作流类型,包含任务节点和依赖关系。

## Schema 定义

根据 `schema/flow.schema.json`,CycleWorkflow 必须包含:
- `version`: 版本号(如 "1.1.0")
- `kind`: 必须为 "CycleWorkflow"
- `spec`: 工作流定义,包含 nodes, triggers 等
- `spec.triggers`: 周期调度触发器定义(CycleWorkflow 必需)

## 完整示例

```json
{
  "version": "1.1.0",
  "kind": "CycleWorkflow",
  "spec": {
    "variables": [
      {
        "artifactType": "Variable",
        "id": "bizdate",
        "name": "bizdate",
        "scope": "NodeParameter",
        "type": "System",
        "value": "${yyyymmdd}"
      },
      {
        "artifactType": "Variable",
        "id": "region",
        "name": "region",
        "scope": "Workflow",
        "type": "Constant",
        "value": "cn-shanghai"
      }
    ],
    "scripts": [
      {
        "id": "script_sql_1",
        "path": "/workflows/daily/extract_data.sql",
        "language": "odps-sql",
        "runtime": {
          "engine": "MaxCompute",
          "command": "ODPS_SQL",
          "commandTypeId": 10
        },
        "parameters": [
          "{{variables.bizdate}}"
        ]
      }
    ],
    "runtimeResources": [
      {
        "id": "resgroup_1",
        "resourceGroup": "group_524257424564736",
        "resourceGroupId": "310003"
      }
    ],
    "triggers": [
      {
        "id": "daily_trigger",
        "type": "Scheduler",
        "cron": "00 00 01 * * ?",
        "startTime": "2024-01-01 00:00:00",
        "endTime": "2025-12-31 23:59:59",
        "timezone": "Asia/Shanghai"
      }
    ],
    "artifacts": {
      "tables": [
        {
          "artifactType": "Table",
          "id": "table_source",
          "guid": "odps.project_name.source_table"
        },
        {
          "artifactType": "Table",
          "id": "table_target",
          "guid": "odps.project_name.target_table"
        }
      ]
    },
    "nodes": [
      {
        "id": "node_extract",
        "name": "extract_data",
        "recurrence": "Normal",
        "priority": 5,
        "timeout": 3600,
        "instanceMode": "T+1",
        "rerunMode": "Allowed",
        "rerunTimes": 3,
        "rerunInterval": 180000,
        "script": "{{scripts.script_sql_1}}",
        "trigger": "{{triggers.daily_trigger}}",
        "runtimeResource": "{{runtimeResources.resgroup_1}}",
        "datasource": {
          "name": "odps_datasource",
          "type": "odps"
        },
        "inputs": {
          "tables": [
            "{{artifacts.tables.table_source}}"
          ]
        },
        "outputs": {
          "tables": [
            "{{artifacts.tables.table_target}}"
          ],
          "nodeOutputs": [
            {
              "artifactType": "NodeOutput",
              "data": "node_extract",
              "refTableName": "target_table"
            }
          ]
        }
      }
    ],
    "flow": [
      {
        "nodeId": "node_extract",
        "depends": []
      }
    ]
  }
}
```

## 字段说明

### 触发器 (Triggers)

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| id | string | 是 | 触发器唯一标识 |
| type | string | 是 | 必须为 "Scheduler" |
| cron | string | 是 | Cron 表达式,如 "00 00 01 * * ?" 表示每天凌晨1点 |
| startTime | string | 否 | 生效开始时间 |
| endTime | string | 否 | 生效结束时间 |
| timezone | string | 否 | 时区,如 "Asia/Shanghai" |

### 节点属性 (Node Properties)

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| recurrence | string | 否 | 调度状态: Normal(正常), Skip(跳过), Pause(暂停) |
| priority | integer | 否 | 优先级,数值越大优先级越高 |
| timeout | integer | 否 | 超时时间(秒) |
| instanceMode | string | 否 | 实例化模式: T+1(次日生效), Immediately(立即生效) |
| rerunMode | string | 否 | 重跑策略: Allowed(允许), Denied(拒绝), FailureAllowed(失败时允许) |
| rerunTimes | integer | 否 | 重试次数 |
| rerunInterval | integer | 否 | 重试间隔(毫秒) |

### 依赖关系 (Flow Dependencies)

```json
{
  "flow": [
    {
      "nodeId": "node_b",
      "depends": [
        {
          "type": "Normal",
          "nodeId": "node_a"
        },
        {
          "type": "CrossCycleDependsOnSelf"
        }
      ]
    }
  ]
}
```

依赖类型说明:
- **Normal**: 同周期依赖,依赖同一调度周期的节点
- **CrossCycleDependsOnSelf**: 依赖上一周期的自己
- **CrossCycleDependsOnChildren**: 依赖上一周期自己的所有子节点
- **CrossCycleDependsOnOtherNode**: 依赖上一周期的指定节点

## 最佳实践

1. **Cron 表达式**: 使用标准 Quartz Cron 格式 "秒 分 时 日 月 星期"
2. **超时设置**: 根据任务实际运行时间合理设置 timeout
3. **重试策略**: 生产任务建议设置 rerunMode 为 "Allowed",rerunTimes 为 2-3 次
4. **变量使用**: 使用系统变量 ${yyyymmdd} 等实现日期参数化
5. **资源隔离**: 通过 runtimeResource 指定资源组实现资源隔离

## 注意事项

- CycleWorkflow 必须包含 `spec.triggers` 定义
- 所有节点必须包含 `trigger` 引用
- 节点 ID 在 Spec 内必须唯一
- 依赖关系不能形成循环依赖
