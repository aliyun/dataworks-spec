# PAI Studio节点 (PAI_STUDIO)

## 概述

PAI Studio节点(PAI_STUDIO)用于在DataWorks中执行PAI(Platform of Artificial Intelligence) Studio中的机器学习和深度学习任务。

## 节点类型信息

- **CodeProgramType**: PAI_STUDIO (commandTypeId: 1000)
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
        "id": "pai_studio_1",
        "instanceMode": "T+1",
        "rerunMode": "Allowed",
        "rerunTimes": 0,
        "rerunInterval": 0,
        "autoParse": true,
        "script": {
          "path": "path/to/pai_studio",
          "runtime": {
            "command": "PAI_STUDIO"
          },
          "content": {
            "content": "{\"appId\":23620,\"computeResource\":{\"MaxCompute\":\"execution_maxcompute\"},\"connectionType\":\"MaxCompute\",\"description\":\"\",\"flowUniqueCode\":\"ee1b1797-9f7c-4937-b672-028c4ced649b\",\"inputs\":[],\"name\":\"haozhen-designer-001\",\"outputs\":[],\"paiflowArguments\":\"---\\\\narguments:\\\\n  parameters:\\\\n  - name: \\\\\\\"execution_maxcompute\\\\\\\"\\\\n    value:\\\\n      endpoint: \\\\\\\"http://service.odps.aliyun-inc.com/api\\\\\\\"\\\\n     odpsProject: \\\\\\\"dw_scheduler_pre_dev\\\\\\\"\\\\n      spec:\\\\n        endpoint: \\\\\\\"http://service.odps.aliyun-inc.com/api\\\\\\\"\\\\n        odpsProject: \\\\\\\"dw_scheduler_pre_dev\\\\\\\"\\\\n      resourceType: \\\\\\\"MaxCompute\\\\\\\"\\\\n\",\"paiflowParameters\":{},\"paiflowPipeline\":\"---\\\\napiVersion: \\\\\\\"core/v1\\\\\\\"\\\\nmetadata:\\\\n  provider: \\\\\\\"067848\\\\\\\"\\\\n  version: \\\\\\\"v1\\\\\\\"\\\\n  identifier: \\\\\\\"job-root-pipeline-identifier\\\\\\\"\\\\n  annotations: {}\\\\nspec:\\\\n  inputs:\\\\n    artifacts: []\\\\n    parameters:\\\\n    - name: \\\\\\\"execution_maxcompute\\\\\\\"\\\\n      type: \\\\\\\"Map\\\\\\\"\\\\n  arguments:\\\\n    artifacts: []\\\\n    parameters: []\\\\n  dependencies: []\\\\n  initContainers: []\\\\n  sideCarContainers: []\\\\n  pipelines:\\\\n  - apiVersion: \\\\\\\"core/v1\\\\\\\"\\\\n    metadata:\\\\n      provider: \\\\\\\"pai\\\\\\\"\\\\n      version: \\\\\\\"v1\\\\\\\"\\\\n      identifier: \\\\\\\"sql\\\\\\\"\\\\n      name: \\\\\\\"id-fdb222dd-1396-4cdc-baf8\\\\\\\"\\\\n      displayName: \\\\\\\"SQL脚本-1\\\\\\\"\\\\n      annotations: {}\\\\n    spec:\\\\n      arguments:\\\\n        artifacts: []\\\\n        parameters:\\\\n        - name: \\\\\\\"scriptMode\\\\\\\"\\\\n          value: false\\\\n        - name: \\\\\\\"addCreateTableStatement\\\\\\\"\\\\n          value: true\\\\n        - name: \\\\\\\"sql\\\\\\\"\\\\n          value: \\\\\\\"select 1;\\\\\\\"\\\\n        - name: \\\\\\\"execution\\\\\\\"\\\\n          from: \\\\\\\"{{inputs.parameters.execution_maxcompute}}\\\\\\\"\\\\n      dependencies: []\\\\n      initContainers: []\\\\n      sideCarContainers: []\\\\n      pipelines: []\\\\n      volumes: []\\\\n  - apiVersion: \\\\\\\"core/v1\\\\\\\"\\\\n    metadata:\\\\n      provider: \\\\\\\"pai\\\\\\\"\\\\n      version: \\\\\\\"v1\\\\\\\"\\\\n      identifier: \\\\\\\"sql\\\\\\\"\\\\n      name: \\\\\\\"id-c541f1eb-81b8-4e10-921b\\\\\\\"\\\\n      displayName: \\\\\\\"SQL脚本-2\\\\\\\"\\\\n      annotations: {}\\\\n    spec:\\\\n      arguments:\\\\n        artifacts:\\\\n        - name: \\\\\\\"inputTable3\\\\\\\"\\\\n          from: \\\\\\\"{{pipelines.id-fdb222dd-1396-4cdc-baf8.outputs.artifacts.outputTable}}\\\\\\\"\\\\n        parameters:\\\\n        - name: \\\\\\\"scriptMode\\\\\\\"\\\\n          value: false\\\\n        - name: \\\\\\\"addCreateTableStatement\\\\\\\"\\\\n          value: true\\\\n        - name: \\\\\\\"sql\\\\\\\"\\\\n          value: \\\\\\\"SELECT 2;\\\\\\\"\\\\n        - name: \\\\\\\"execution\\\\\\\"\\\\n          from: \\\\\\\"{{inputs.parameters.execution_maxcompute}}\\\\\\\"\\\\n      dependencies:\\\\n      - \\\\\\\"id-fdb222dd-1396-4cdc-baf8\\\\\\\"\\\\n      initContainers: []\\\\n      sideCarContainers: []\\\\n      pipelines: []\\\\n      volumes: []\\\\n  - apiVersion: \\\\\\\"core/v1\\\\\\\"\\\\n    metadata:\\\\n      provider: \\\\\\\"pai\\\\\\\"\\\\n      version: \\\\\\\"v1\\\\\\\"\\\\n      identifier: \\\\\\\"data_source\\\\\\\"\\\\n      name: \\\\\\\"id-151c5da3-ad9c-4be7-8ac3\\\\\\\"\\\\n      displayName: \\\\\\\"读数据表-1\\\\\\\"\\\\n      annotations: {}\\\\n    spec:\\\\n      arguments:\\\\n        artifacts: []\\\\n        parameters:\\\\n        - name: \\\\\\\"hasPartition\\\\\\\"\\\\n          value: \\\\\\\"false\\\\\\\"\\\\n        - name: \\\\\\\"execution\\\\\\\"\\\\n          from: \\\\\\\"{{inputs.parameters.execution_maxcompute}}\\\\\\\"\\\\n      dependencies: []\\\\n      initContainers: []\\\\n      sideCarContainers: []\\\\n      pipelines: []\\\\n      volumes: []\\\\n  volumes: []\\\\n\",\"paraValue\":\"--paiflow_endpoint=paiflowinner-share.aliyuncs.com --region=inner\",\"prgType\":\"1000138\",\"requestId\":\"7A497B66-62ED-52A3-A64D-F1EC7B61052A\",\"taskRelations\":[{\"childTaskUniqueCode\":\"id-c541f1eb-81b8-4e10-921b\",\"parentTaskUniqueCode\":\"id-fdb222dd-1396-4cdc-baf8\"}],\"tasks\":[{\"root\":true,\"taskName\":\"SQL脚本-1\",\"taskUniqueCode\":\"id-fdb222dd-1396-4cdc-baf8\"},{\"root\":false,\"taskName\":\"SQL脚本-2\",\"taskUniqueCode\":\"id-c541f1eb-81b8-4e10-921b\"},{\"root\":true,\"taskName\":\"读数据表-1\",\"taskUniqueCode\":\"id-151c5da3-ad9c-4be7-8ac3\"}],\"workspaceId\":\"23620\"}",
            "extraContent": "{\"experimentId\":\"experiment-mwzpv9cbwjx0tc7gc3\",\"name\":\"haozhen-designer-001\",\"desc\":\"\"}"
          }
        },
        "trigger": {
          "type": "Scheduler",
          "cron": "00 00 00 * * ?",
          "startTime": "1970-01-01 00:00:00",
          "endTime": "9999-01-01 00:00:00"
        },
        "name": "PAI Studio Node",
        "owner": "owner_id"
      }
    ],
    "flow": [
      {
        "nodeId": "pai_studio_1",
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

### PAI Studio 任务定义

```json
{
  "script": {
    "runtime": {
      "command": "PAI_STUDIO"
    },
    "content": {
      "content": "PAI Studio 任务的 JSON 配置",
      "extraContent": "额外的配置信息"
    }
  }
}
```

## 实际用例

PAI Studio节点通常用于:
1. 在DataWorks工作流中集成机器学习训练任务
2. 执行深度学习模型的训练和推理
3. 运行PAI平台提供的各种算法组件

## 最佳实践

1. **资源配置**: 根据训练任务的需求合理配置计算资源
2. **模型管理**: 使用PAI平台的模型管理功能管理训练好的模型
3. **监控调试**: 利用PAI Studio的可视化界面监控训练过程

## 注意事项

- 需要预先在PAI Studio中创建和配置好机器学习项目
- 注意PAI Studio任务的计费方式和资源消耗
- 确保输入数据已准备好且格式符合模型要求