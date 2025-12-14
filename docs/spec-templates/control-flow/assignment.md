# 赋值节点 (CONTROLLER_ASSIGNMENT)

## 概述

赋值节点(CONTROLLER_ASSIGNMENT)用于将一个值赋给变量，常用于控制流中传递或计算值。

## 节点类型信息

- **CodeProgramType**: CONTROLLER_ASSIGNMENT (commandTypeId: 1100)
- **CalcEngineType**: GENERAL
- **Language**: OdpsScript
- **文件后缀**: .txt

## 完整示例

```json
{
  "version": "1.1.0",
  "kind": "CycleWorkflow",
  "spec": {
    "nodes": [
      {
        "recurrence": "Normal",
        "id": "assign_node_1",
        "instanceMode": "T+1",
        "rerunMode": "Allowed",
        "rerunTimes": 0,
        "rerunInterval": 0,
        "autoParse": true,
        "script": {
          "language": "OdpsScript",
          "path": "path/to/assignment",
          "runtime": {
            "command": "CONTROLLER_ASSIGNMENT"
          },
          "content": "select 'true';"
        },
        "trigger": {
          "type": "Scheduler",
          "cron": "00 00 00 * * ?",
          "startTime": "1970-01-01 00:00:00",
          "endTime": "9999-01-01 00:00:00"
        },
        "name": "Assignment Node",
        "owner": "owner_id"
      }
    ],
    "flow": [
      {
        "nodeId": "assign_node_1",
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

### 基础配置

```json
{
  "script": {
    "language": "OdpsScript",
    "runtime": {
      "command": "CONTROLLER_ASSIGNMENT"
    },
    "content": "select 'true';"
  }
}
```

## 实际用例

赋值节点通常用于:
1. 将计算结果赋给变量供后续节点使用
2. 在控制流中设置条件判断的值

## 最佳实践

1. **内容简洁**: 赋值节点的内容应尽量简洁，避免复杂的计算逻辑
2. **明确用途**: 使用清晰的节点名称描述赋值的目的
3. **输出变量**: 通过节点输出将赋值结果传递给其他节点

## 注意事项

- 赋值节点的执行结果可以通过节点输出在其他节点中引用
- 注意变量的作用域和生命周期