# 组合节点 (Combined Node)

## 概述

组合节点(COMBINED_NODE)允许将多个子节点组合成一个逻辑单元,子节点间可以定义内部依赖关系,对外作为一个整体节点。

## 节点类型信息

- **CodeProgramType**: COMBINED_NODE (commandTypeId: 98)
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
        "id": "combined_node_1",
        "name": "data_processing_pipeline",
        "script": {
          "id": "combined_script",
          "path": "/workflows/combined/data_pipeline.json",
          "language": "json",
          "runtime": {
            "engine": "MaxCompute",
            "command": "COMBINED_NODE",
            "commandTypeId": 98
          }
        },
        "runtimeResource": {
          "resourceGroup": "group_524257424564736",
          "id": "runtime_res_001"
        },
        "combined": {
          "nodes": [
            {
              "id": "extract_task",
              "name": "extract_data",
              "script": {
                "id": "extract_script",
                "path": "/workflows/combined/extract.sql",
                "language": "odps-sql",
                "runtime": {
                  "engine": "MaxCompute",
                  "command": "ODPS_SQL",
                  "commandTypeId": 10
                }
              },
              "inputs": {},
              "outputs": {
                "tables": [
                  {
                    "artifactType": "Table",
                    "guid": "odps.project.temp_extract_table"
                  }
                ]
              }
            },
            {
              "id": "transform_task",
              "name": "transform_data",
              "script": {
                "id": "transform_script",
                "path": "/workflows/combined/transform.py",
                "language": "python",
                "runtime": {
                  "command": "PYODPS",
                  "commandTypeId": 221
                }
              },
              "inputs": {
                "tables": [
                  {
                    "artifactType": "Table",
                    "guid": "odps.project.temp_extract_table"
                  }
                ]
              },
              "outputs": {
                "tables": [
                  {
                    "artifactType": "Table",
                    "guid": "odps.project.temp_transform_table"
                  }
                ]
              }
            },
            {
              "id": "load_task",
              "name": "load_data",
              "script": {
                "id": "load_script",
                "path": "/workflows/combined/load.sql",
                "language": "odps-sql",
                "runtime": {
                  "engine": "MaxCompute",
                  "command": "ODPS_SQL",
                  "commandTypeId": 10
                }
              },
              "inputs": {
                "tables": [
                  {
                    "artifactType": "Table",
                    "guid": "odps.project.temp_transform_table"
                  }
                ]
              },
              "outputs": {
                "tables": [
                  {
                    "artifactType": "Table",
                    "guid": "odps.project.final_table"
                  }
                ]
              }
            }
          ],
          "flow": [
            {
              "nodeId": "transform_task",
              "depends": [
                {
                  "nodeId": "extract_task",
                  "type": "Normal"
                }
              ]
            },
            {
              "nodeId": "load_task",
              "depends": [
                {
                  "nodeId": "transform_task",
                  "type": "Normal"
                }
              ]
            }
          ]
        }
      }
    ]
  }
}
```

## Combined 配置说明

### 基础结构

```json
{
  "combined": {
    "nodes": [ /* 子节点列表 */ ],
    "flow": [ /* 子节点间的依赖关系 */ ]
  }
}
```

### 子节点定义 (nodes)

子节点的定义与普通节点类似,包含完整的 script, inputs, outputs 等配置:

```json
{
  "nodes": [
    {
      "id": "sub_node_1",
      "name": "sub_task_1",
      "script": {
        "id": "script_1",
        "path": "/path/to/script.sql",
        "language": "odps-sql",
        "runtime": {
          "command": "ODPS_SQL"
        }
      },
      "inputs": { /* 输入配置 */ },
      "outputs": { /* 输出配置 */ }
    }
  ]
}
```

### 子节点依赖 (flow)

使用 `flow` 数组定义子节点间的执行顺序:

```json
{
  "flow": [
    {
      "nodeId": "sub_node_2",
      "depends": [
        {
          "nodeId": "sub_node_1",
          "type": "Normal"
        }
      ]
    }
  ]
}
```

## ETL Pipeline 示例

```json
{
  "id": "etl_pipeline",
  "name": "Complete ETL Pipeline",
  "combined": {
    "nodes": [
      {
        "id": "extract",
        "name": "Extract from Source",
        "script": {
          "path": "/etl/extract.sql",
          "runtime": {"command": "ODPS_SQL"}
        }
      },
      {
        "id": "validate",
        "name": "Validate Data Quality",
        "script": {
          "path": "/etl/validate.py",
          "runtime": {"command": "PYODPS"}
        }
      },
      {
        "id": "transform",
        "name": "Transform Data",
        "script": {
          "path": "/etl/transform.sql",
          "runtime": {"command": "ODPS_SQL"}
        }
      },
      {
        "id": "load",
        "name": "Load to Target",
        "script": {
          "path": "/etl/load.sql",
          "runtime": {"command": "ODPS_SQL"}
        }
      }
    ],
    "flow": [
      {
        "nodeId": "validate",
        "depends": [{"nodeId": "extract", "type": "Normal"}]
      },
      {
        "nodeId": "transform",
        "depends": [{"nodeId": "validate", "type": "Normal"}]
      },
      {
        "nodeId": "load",
        "depends": [{"nodeId": "transform", "type": "Normal"}]
      }
    ]
  }
}
```

## 使用场景

1. **ETL Pipeline**: 将 Extract, Transform, Load 组合成一个逻辑单元
2. **数据质量检查流程**: 组合数据抽取、校验、清洗等步骤
3. **模块化任务**: 将相关的多个任务组织成可复用的模块
4. **原子性操作**: 确保一组相关任务作为整体执行

## 优势

1. **逻辑清晰**: 将相关任务组织在一起,便于理解和维护
2. **封装复用**: 组合节点可以作为模板复用
3. **依赖简化**: 外部节点只需依赖组合节点,无需关心内部细节
4. **资源共享**: 组合节点内的子节点共享 runtimeResource 配置

## 最佳实践

1. **功能内聚**: 将功能相关的节点组合在一起
2. **清晰命名**: 为组合节点和子节点使用清晰的命名
3. **输入输出**: 明确定义组合节点的整体输入和输出
4. **错误处理**: 考虑子节点失败时的重试和回滚策略
5. **适度组合**: 避免组合节点过于复杂,建议子节点数量不超过10个

## 与工作流的区别

| 特性 | 组合节点 | 工作流 |
|------|----------|--------|
| 作用域 | 节点内部 | 顶层 |
| 调度 | 作为整体调度 | 独立调度 |
| 资源 | 共享组合节点的资源 | 各节点独立资源 |
| 可见性 | 对外是单个节点 | 所有节点可见 |
| 使用场景 | 内聚的原子操作 | 完整的业务流程 |

## 注意事项

- 子节点 ID 在组合节点内必须唯一
- 子节点间不能形成循环依赖
- 组合节点的 script 主要用于描述,实际执行由子节点完成
- 组合节点失败时,所有子节点都会被标记为失败
- 子节点不能引用组合节点外部的 scripts, variables 等资源(需通过组合节点传递)
