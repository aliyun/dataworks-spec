# 手动触发工作流 (ManualWorkflow)

## 概述

ManualWorkflow 是手动触发执行的工作流类型,不需要配置调度策略,适用于临时任务、测试任务等场景。

## Schema 定义

根据 `schema/flow.schema.json`,ManualWorkflow 的特点:
- `kind`: 必须为 "ManualWorkflow"
- **不需要** `spec.triggers` 定义
- 节点的 `trigger.type` 为 "Manual"

## 完整示例

```json
{
  "version": "1.1.0",
  "kind": "ManualWorkflow",
  "spec": {
    "name": "manual_test_workflow",
    "id": "6853333959175030943",
    "type": "ManualWorkflow",
    "owner": "1107550004253538",
    "strategy": {
      "priority": 1,
      "priorityWeightStrategy": "Disabled",
      "maxInternalConcurrency": 0
    },
    "variables": [
      {
        "artifactType": "Variable",
        "name": "input_param",
        "scope": "Workflow",
        "type": "Constant",
        "value": "test_value"
      }
    ],
    "nodes": [
      {
        "id": "node_001",
        "name": "data_processing_task",
        "timeout": 3600,
        "rerunMode": "Allowed",
        "rerunTimes": 2,
        "rerunInterval": 180000,
        "datasource": {
          "name": "odps_datasource_001",
          "type": "odps"
        },
        "script": {
          "id": "script_001",
          "language": "odps-sql",
          "path": "manual_workflow/data_processing.sql",
          "runtime": {
            "command": "ODPS_SQL",
            "commandTypeId": 10,
            "engine": "MaxCompute"
          }
        },
        "trigger": {
          "type": "Manual",
          "id": "trigger_manual_001"
        },
        "runtimeResource": {
          "resourceGroup": "group_524257424564736",
          "id": "runtime_res_001",
          "resourceGroupId": "310003"
        },
        "inputs": {},
        "outputs": {
          "nodeOutputs": [
            {
              "data": "node_001",
              "artifactType": "NodeOutput",
              "refTableName": "output_table_001",
              "isDefault": true
            },
            {
              "data": "project_name.output_table_001",
              "artifactType": "NodeOutput",
              "refTableName": "output_table_001",
              "isDefault": false
            }
          ]
        }
      },
      {
        "id": "node_002",
        "name": "downstream_task",
        "timeout": 1800,
        "rerunMode": "Allowed",
        "rerunTimes": 2,
        "rerunInterval": 180000,
        "script": {
          "id": "script_002",
          "language": "odps-sql",
          "path": "manual_workflow/downstream_task.sql",
          "runtime": {
            "command": "ODPS_SQL",
            "commandTypeId": 10
          }
        },
        "trigger": {
          "type": "Manual",
          "id": "trigger_manual_002"
        },
        "runtimeResource": {
          "resourceGroup": "group_524257424564736",
          "id": "runtime_res_001",
          "resourceGroupId": "310003"
        },
        "inputs": {},
        "outputs": {
          "nodeOutputs": [
            {
              "data": "node_002",
              "artifactType": "NodeOutput",
              "refTableName": "output_table_002",
              "isDefault": true
            }
          ]
        }
      }
    ],
    "flow": [
      {
        "nodeId": "node_002",
        "depends": [
          {
            "type": "Normal",
            "output": "node_001",
            "refTableName": "output_table_001"
          }
        ]
      }
    ]
  }
}
```

## 字段说明

### 触发器 (Trigger)

ManualWorkflow 中每个节点的触发器配置简单:

```json
{
  "trigger": {
    "type": "Manual",
    "id": "trigger_id"
  }
}
```

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| type | string | 是 | 必须为 "Manual" |
| id | string | 是 | 触发器唯一标识 |

### 工作流策略 (Strategy)

```json
{
  "strategy": {
    "priority": 1,
    "priorityWeightStrategy": "Disabled",
    "maxInternalConcurrency": 0
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| priority | integer | 工作流优先级 |
| priorityWeightStrategy | string | 优先级权重策略,可选值: Disabled |
| maxInternalConcurrency | integer | 最大内部并发数,0 表示无限制 |

### 节点输出 (Outputs)

```json
{
  "outputs": {
    "nodeOutputs": [
      {
        "data": "node_001",
        "artifactType": "NodeOutput",
        "refTableName": "output_table_001",
        "isDefault": true
      }
    ]
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| data | string | 输出数据标识,通常为节点 ID 或 "项目名.表名" |
| artifactType | string | 固定为 "NodeOutput" |
| refTableName | string | 引用的表名 |
| isDefault | boolean | 是否为默认输出 |

## ManualWorkflow vs CycleWorkflow

| 特性 | ManualWorkflow | CycleWorkflow |
|------|----------------|---------------|
| 触发方式 | 手动触发 | 定时调度 |
| Triggers 定义 | 不需要 spec.triggers | 必须定义 spec.triggers |
| 节点 trigger.type | Manual | Scheduler |
| 使用场景 | 临时任务、测试、一次性任务 | 周期性数据处理任务 |
| 调度周期 | 无 | Cron 表达式定义 |

## 最佳实践

1. **命名规范**: 使用清晰的节点 name,便于识别和管理
2. **超时设置**: 根据任务预估时间合理设置 timeout
3. **输出定义**: 明确定义 nodeOutputs,便于下游节点依赖
4. **元数据**: 完善 metadata 信息,包括 owner, createTime 等
5. **依赖管理**: 通过 flow 部分清晰定义节点间的依赖关系

## 使用场景

- 数据修复任务
- 临时数据分析任务
- 测试和开发环境的工作流
- 一次性数据迁移任务
- 需要人工触发的审批流程

## 注意事项

- ManualWorkflow 不能包含 `spec.triggers` 数组定义
- 所有节点的 `trigger.type` 必须为 "Manual"
- 手动工作流不支持跨周期依赖(如 CrossCycleDependsOnSelf)
- 节点 ID 在整个 Spec 中必须唯一
