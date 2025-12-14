# 归并节点 (CONTROLLER_JOIN)

## 概述

归并节点(CONTROLLER_JOIN)用于将多个分支的执行结果汇聚到一个点，确保多个前置任务都完成后才继续后续流程。

## 节点类型信息

- **CodeProgramType**: CONTROLLER_JOIN (commandTypeId: 1102)
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
        "id": "join_node_1",
        "instanceMode": "T+1",
        "rerunMode": "Allowed",
        "rerunTimes": 0,
        "rerunInterval": 0,
        "autoParse": true,
        "script": {
          "runtime": {
            "command": "CONTROLLER_JOIN"
          }
        },
        "join": {
          "logic": {
            "expression": "b1 and b2 or b3 or b4"
          },
          "branches": [
            {
              "name": "b1",
              "assertion": {
                "field": "status",
                "in": ["SUCCESS", "FAILURE"]
              },
              "output": {
                "data": "dd"
              }
            },
            {
              "name": "b2",
              "assertion": {
                "field": "status",
                "in": ["SUCCESS", "FAILURE"]
              },
              "output": {
                "data": "dd"
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
        "name": "Join Node",
        "owner": "owner_id",
        "inputs": {
          "nodeOutputs": [
            {
              "data": "ctx_param_01.xx_in_shell",
              "artifactType": "NodeOutput",
              "sourceType": "System",
              "isDefault": true
            },
            {
              "data": "ctx_param_01.xx_not_shell",
              "artifactType": "NodeOutput",
              "sourceType": "System",
              "isDefault": true
            }
          ]
        }
      }
    ],
    "flow": [
      {
        "nodeId": "join_node_1",
        "depends": [
          {
            "type": "Normal",
            "output": "ctx_param_01.xx_in_shell"
          },
          {
            "type": "Normal",
            "output": "ctx_param_01.xx_not_shell"
          }
        ]
      }
    ]
  }
}
```

## Join 配置

### 分支逻辑定义

```json
{
  "join": {
    "logic": {
      "expression": "b1 and b2 or b3"  // 逻辑表达式
    },
    "branches": [
      {
        "name": "b1",  // 分支名称，对应逻辑表达式中的变量
        "assertion": {
          "field": "status",  // 断言字段
          "in": ["SUCCESS"]   // 断言值
        },
        "output": {
          "data": "branch_output_1"
        }
      }
    ]
  }
}
```

## 实际用例

归并节点通常用于:
1. 等待多个并行分支完成后继续执行
2. 根据多个分支的状态执行不同的逻辑
3. 实现复杂的依赖关系管理

## 最佳实践

1. **逻辑清晰**: 确保逻辑表达式清晰易懂
2. **状态检查**: 合理定义分支状态的断言条件
3. **依赖管理**: 正确设置归并节点的输入依赖

## 注意事项

- 归并节点会等待所有指定的前置分支完成
- 逻辑表达式中使用的分支名称必须与 branches 中定义的名称一致
- 可以根据分支的执行状态决定归并节点的行为