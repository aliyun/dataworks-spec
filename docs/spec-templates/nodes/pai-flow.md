# PAI Flow节点 (PAI_FLOW)

## 概述

PAI Flow节点(PAI_FLOW)用于在DataWorks中执行PAI平台的复杂机器学习工作流，支持多步骤的AI任务编排。

## 节点类型信息

- **CodeProgramType**: PAI_FLOW (commandTypeId: 1250)
- **CalcEngineType**: PAI
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
        "id": "paiflow_1",
        "timeout": 10000,
        "instanceMode": "T+1",
        "rerunMode": "Allowed",
        "rerunTimes": 3,
        "rerunInterval": 180000,
        "ignoreBranchConditionSkip": true,
        "script": {
          "path": "paiflows/test_create_paiflow_1739519049",
          "runtime": {
            "command": "PAI_FLOW",
            "commandTypeId": 1250,
            "cu": "0.25"
          },
          "parameters": [
            {
              "name": "bizdate",
              "artifactType": "Variable",
              "scope": "NodeParameter",
              "type": "System",
              "value": "${yyyymmdd}"
            }
          ]
        },
        "trigger": {
          "type": "Scheduler",
          "cron": "0 0 0 * * ? *",
          "startTime": "1970-01-01 00:00:00",
          "endTime": "9999-01-01 00:00:00",
          "timezone": "Asia/Shanghai",
          "delaySeconds": 0
        },
        "runtimeResource": {
          "resourceGroup": "group_56152229",
          "id": "8177987838884455485",
          "resourceGroupId": "1"
        },
        "name": "test_create_paiflow_1739519049",
        "owner": "WORKER_1482465063962",
        "description": "test_create_paiflow_1739519049",
        "paiflow": {
          "nodes": [
            {
              "recurrence": "Normal",
              "id": "8245048540225720582",
              "timeout": 10000,
              "instanceMode": "T+1",
              "rerunMode": "Allowed",
              "rerunTimes": 3,
              "rerunInterval": 180000,
              "ignoreBranchConditionSkip": true,
              "script": {
                "path": "paiflows/test_create_paiflow_1739519049/rag_sync_index",
                "runtime": {
                  "engine": "General",
                  "command": "PAI_FLOW_RAG_GENERATE_EMBEDDING",
                  "commandTypeId": 1252
                },
                "content": "paiflowPipeline:\\n  apiVersion: core/v1\\n  metadata:\\n    provider: '1326689413376250'\\n    version: v1\\n    identifier: rag_sync_index\\n    uuid: k9wbjh70610s550m8w\\n    annotations: {}\\n    labels: {}\\n  spec:\\n    arguments:\\n      artifacts: []\\n      parameters: []\\n    container:\\n      image: pai-official-cn-hangzhou-registry-vpc.cn-hangzhou.cr.aliyuncs.com/official/max-compute-executor:v20241210134900\\n      command:\\n      - bash\\n      - /paiflow-bin/start.sh\\n      envs: {}\\n      volumeMounts:\\n      - name: pai-volume\\n        path: /pai\\n    dependencies: []\\n    initContainers:\\n    - image: pai-official-cn-hangzhou-registry-vpc.cn-hangzhou.cr.aliyuncs.com/official/paiflow-init:v1.0.0\\n      command:\\n      - /bin/sh\\n      - -c\\n      - paiflow-init download --source 'oss://pai-studio-cn-hangzhou-prod/algorithm/pai/rag_sync_index/v1/afb415fb780f5cd50ca65d91b0e4d90e43972afd/working?endpoint=oss-cn-hangzhou-internal.aliyuncs.com&roleARN=acs:ram::1326689413376250:role/pai-studio-algo-download-role&ownerId=1326689413376250'\\n      --destination /pai/main/resource\\n      name: paiflow-init\\n      envs: {}\\n      volumeMounts:\\n      - name: pai-volume\\n        path: /pai/main\\n    inputs:\\n      artifacts:\\n      - desc: Input Data\\n        metadata:\\n          type:\\n            DataSet:\\n              locationType: OSS\\n        name: input_data\\n        repeated: false\\n        required: false\\n      parameters:\\n      - desc: role_arn\\n        name: role_arn\\n        type: String\\n        value: ''\\n      - desc: The ID of the registered index in PAI (Dataset, DataType=INDEX).\\n        name: target_index\\n        type: String\\n      - desc: Training service config\\n        name: training_service_config\\n        type: Map\\n        value: ''\\n      - name: execution\\n        type: Map\\n    outputs:\\n      artifacts:\\n      - desc: Output Data\\n        metadata:\\n          type:\\n            DataSet:\\n              locationType: OSS\\n        name: output_data\\n        repeated: false\\n        required: false\\n        value: {}\\n      parameters: []\\n    pipelines: []\\n    sideCarContainers: []\\n    volumes:\\n    - name: pai-volume\\n      emptyDir: {}\\npipelineId: pipeline-tvxe0krijvvmps4ksr\\n",
                "parameters": [
                  {
                    "name": "input_data",
                    "artifactType": "Variable",
                    "scope": "NodeContext",
                    "type": "PaiOutput",
                    "value": "",
                    "referenceVariable": {
                      "name": "output_data",
                      "artifactType": "Variable",
                      "scope": "NodeContext",
                      "type": "PaiOutput",
                      "value": "",
                      "node": {
                        "output": "8170100874044141528",
                        "refTableName": "rag_parse_chunk"
                      }
                    }
                  }
                ]
              },
              "runtimeResource": {
                "resourceGroup": "group_56152229",
                "id": "8177987838884455485",
                "resourceGroupId": "1"
              },
              "name": "rag_sync_index",
              "owner": "WORKER_1482465063962",
              "description": "test_create_paiflow_1739519049/rag_sync_index",
              "inputs": {
                "variables": [
                  {
                    "name": "output_data",
                    "artifactType": "Variable",
                    "scope": "NodeContext",
                    "type": "PaiOutput",
                    "value": "",
                    "node": {
                      "output": "8170100874044141528",
                      "refTableName": "rag_parse_chunk"
                    }
                  }
                ]
              },
              "outputs": {
                "nodeOutputs": [
                  {
                    "data": "8245048540225720582",
                    "artifactType": "NodeOutput",
                    "refTableName": "rag_sync_index",
                    "isDefault": true
                  }
                ]
              }
            }
          ],
          "flow": [
            {
              "nodeId": "8245048540225720582",
              "depends": [
                {
                  "type": "Normal",
                  "output": "8170100874044141528",
                  "refTableName": "rag_parse_chunk"
                }
              ]
            }
          ]
        },
        "inputs": {
          "nodeOutputs": [
            {
              "data": "mock_local_project_root",
              "artifactType": "NodeOutput"
            }
          ]
        },
        "outputs": {
          "nodeOutputs": [
            {
              "data": "7693295738347184676",
              "artifactType": "NodeOutput",
              "refTableName": "test_create_paiflow_1739519049",
              "isDefault": true
            },
            {
              "data": "mock_local_project.test_create_paiflow_1739519049",
              "artifactType": "NodeOutput",
              "refTableName": "test_create_paiflow_1739519049",
              "isDefault": false
            }
          ]
        }
      }
    ],
    "flow": [
      {
        "nodeId": "paiflow_1",
        "depends": [
          {
            "type": "Normal",
            "output": "8170100874044141528",
            "refTableName": "rag_parse_chunk"
          }
        ]
      }
    ]
  }
}
```

## PAI Flow 配置

### PAI工作流定义

```json
{
  "paiflow": {
    "nodes": [ /* PAI工作流中的子节点 */ ],
    "flow": [ /* 子节点间的依赖关系 */ ]
  }
}
```

## 实际用例

PAI Flow节点通常用于:
1. 编排复杂的机器学习训练流程
2. 执行多步骤的数据预处理和模型训练任务
3. 实现端到端的AI模型开发工作流

## 最佳实践

1. **流程设计**: 设计合理的PAI工作流结构，确保任务间的依赖关系正确
2. **资源规划**: 为不同的PAI任务分配合适的计算资源
3. **监控调试**: 使用PAI平台提供的监控工具追踪工作流执行状态

## 注意事项

- PAI Flow节点内可以包含多个子节点，形成嵌套的工作流
- 需要确保PAI平台已正确配置并有足够资源执行任务
- 复杂的PAI工作流可能需要较长时间执行，注意设置合理的超时时间