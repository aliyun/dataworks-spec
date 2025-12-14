# 组件SQL节点 (COMPONENT_SQL)

## 概述

组件SQL节点(COMPONENT_SQL)是一种特殊类型的节点，它通过预定义的组件来执行SQL查询，通常用于数据处理和分析场景。

## 节点类型信息

- **CodeProgramType**: COMPONENT_SQL (commandTypeId: 1200)
- **CalcEngineType**: GENERAL
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
        "id": "component_sql_1",
        "instanceMode": "T+1",
        "rerunMode": "Allowed",
        "rerunTimes": 0,
        "rerunInterval": 0,
        "autoParse": true,
        "component": {
          "id": "121212",
          "inputs": [
            {
              "name": "in1",
              "value": "va1"
            }
          ],
          "outputs": [
            {
              "name": "out1",
              "value": "va1"
            }
          ]
        },
        "script": {
          "path": "path/to/component_sql",
          "runtime": {
            "command": "COMPONENT_SQL"
          },
          "content": "select @@{in1}"
        },
        "trigger": {
          "type": "Scheduler",
          "cron": "00 00 00 * * ?",
          "startTime": "1970-01-01 00:00:00",
          "endTime": "9999-01-01 00:00:00"
        },
        "name": "Component SQL Node",
        "owner": "owner_id"
      }
    ],
    "flow": [
      {
        "nodeId": "component_sql_1",
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

## Component 配置

### 组件定义

```json
{
  "component": {
    "id": "component_id",
    "inputs": [
      {
        "name": "input_param_name",
        "value": "input_param_value"
      }
    ],
    "outputs": [
      {
        "name": "output_param_name",
        "value": "output_param_value"
      }
    ]
  }
}
```

## 实际用例

组件SQL节点通常用于:
1. 通过预定义组件执行标准化的SQL操作
2. 简化复杂SQL查询的参数化配置
3. 在数据工作流中复用标准化的数据处理组件

## 最佳实践

1. **参数化**: 使用输入参数实现SQL语句的动态化
2. **复用性**: 设计通用的组件以在多个工作流中复用
3. **错误处理**: 在组件中处理潜在的数据异常情况

## 注意事项

- 组件SQL节点的SQL语句中使用`@@{param}`语法来引用输入参数
- 需要确保组件的输入输出参数定义与实际使用的一致
- 组件的执行行为可能依赖于预定义的组件配置