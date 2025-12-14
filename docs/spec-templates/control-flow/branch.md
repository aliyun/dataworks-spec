# 分支节点 (CONTROLLER_BRANCH)

## 概述

分支节点(CONTROLLER_BRANCH)根据条件判断执行不同的分支路径，是控制流中的重要组件。

## 节点类型信息

- **CodeProgramType**: CONTROLLER_BRANCH (commandTypeId: 1101)
- **CalcEngineType**: GENERAL
- **文件后缀**: .json

## 完整示例

```json
{
  "version": "1.1.0",
  "kind": "CycleWorkflow",
  "spec": {
    "nodes": [
      {
        "recurrence": "Normal",
        "id": "branch_node_1",
        "instanceMode": "T+1",
        "rerunMode": "Allowed",
        "rerunTimes": 0,
        "rerunInterval": 0,
        "autoParse": true,
        "script": {
          "runtime": {
            "command": "CONTROLLER_BRANCH"
          }
        },
        "branch": {
          "branches": [
            {
              "when": "${a} == 1",
              "desc": "b1",
              "output": {
                "data": "autotest.12345_out"
              }
            },
            {
              "when": "${a} == 2",
              "desc": "b2",
              "output": {
                "data": "autotest.56789_out"
              }
            }
          ]
        },
        "trigger": {
          "type": "Scheduler",
          "cron": "00 00 00 * * ?",
          "startTime": "1970-01-01 00:00:00",
          "endTime": "9999-01-01 00:00:00"
        },
        "name": "Branch Node",
        "owner": "owner_id"
      }
    ],
    "flow": [
      {
        "nodeId": "branch_node_1",
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

## Branch 配置

### 分支定义

```json
{
  "branch": {
    "branches": [
      {
        "when": "${a} == 1",
        "desc": "Branch 1 description",
        "output": {
          "data": "branch_1_output"
        }
      },
      {
        "when": "${a} == 2",
        "desc": "Branch 2 description",
        "output": {
          "data": "branch_2_output"
        }
      }
    ]
  }
}
```

## 实际用例

分支节点通常用于:
1. 根据数据条件执行不同的处理流程
2. 在工作流中实现条件逻辑
3. 实现动态路径选择

## 最佳实践

1. **条件明确**: 确保分支条件清晰且互斥，避免多个分支同时满足
2. **完整性**: 考虑所有可能的情况，或提供默认分支
3. **命名规范**: 使用有意义的分支描述和输出名称

## 注意事项

- 分支条件使用表达式语法，例如 `${variable} == value`
- 只有满足条件的分支会被执行
- 分支节点的输出可以作为后续节点的输入依赖