# Hologres数据同步节点 (HOLOGRES_SYNC_DATA)

## 概述

Hologres数据同步节点(HOLOGRES_SYNC_DATA)用于将数据从源端同步到Hologres实例，支持结构化和非结构化的数据同步。

## 节点类型信息

- **CodeProgramType**: HOLOGRES_SYNC_DATA (commandTypeId: 800)
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
        "id": "hologres_sync_1",
        "instanceMode": "T+1",
        "rerunMode": "Allowed",
        "rerunTimes": 0,
        "rerunInterval": 0,
        "autoParse": true,
        "script": {
          "path": "path/to/hologres_sync",
          "runtime": {
            "command": "HOLOGRES_SYNC_DATA"
          },
          "content": {
            "content": "IMPORT FOREIGN SCHEMA shanghai_onlineTest_simple LIMIT TO (wq_test_dataworks_pt_001) from SERVER odps_server INTO public OPTIONS(prefix 'tmp_foreign_', suffix 'xozi4mmb', if_table_exist 'error',if_unsupported_type 'error');\\nDROP TABLE IF EXISTS \\\"public\\\".tmp_holo_8gwvxopb_wqtest;\\nBEGIN;\\nCREATE TABLE IF NOT EXISTS \\\"public\\\".tmp_holo_8gwvxopb_wqtest (\\n \\\"f1\\\" text NOT NULL,\\n \\\"f2\\\" text NOT NULL,\\n \\\"f4\\\" text NOT NULL,\\n \\\"f5\\\" text NOT NULL,\\n \\\"f3\\\" text NOT NULL,\\n \\\"f6\\\" text NOT NULL,\\n \\\"f7\\\" text NOT NULL,\\n \\\"f10\\\" text NOT NULL,\\n \\\"ds\\\" bigint NOT NULL,\\n \\\"pt\\\" text NOT NULL\\n);\\nCALL SET_TABLE_PROPERTY('\\\"public\\\".tmp_holo_8gwvxopb_wqtest', 'orientation', 'column');\\ncomment on column \\\"public\\\".tmp_holo_8gwvxopb_wqtest.pt is '分区字段';\\nCOMMIT;\\nINSERT INTO \\\"public\\\".tmp_holo_8gwvxopb_wqtest\\nSELECT \\n    CAST(\\\"f1\\\" as text),\\n    CAST(\\\"f2\\\" as text),\\n    CAST(\\\"f4\\\" as text),\\n    CAST(\\\"f5\\\" as text),\\n    CAST(\\\"f3\\\" as text),\\n    CAST(\\\"f6\\\" as text),\\n    CAST(\\\"f7\\\" as text),\\n    CAST(\\\"f10\\\" as text),\\n    CAST(\\\"ds\\\" as bigint),\\n    CAST(\\\"pt\\\" as text)\\nFROM \\\"public\\\".tmp_foreign_wq_test_dataworks_pt_001xozi4mmb\\nWHERE pt='${bizdate}';\\nDROP FOREIGN TABLE IF EXISTS \\\"public\\\".tmp_foreign_wq_test_dataworks_pt_001xozi4mmb;BEGIN;\\nDROP TABLE IF EXISTS \\\"public\\\".wqtest;\\nALTER TABLE \\\"public\\\".tmp_holo_8gwvxopb_wqtest RENAME TO wqtest;\\nCOMMIT;\\n",
            "extraContent": "{\"connId\":\"yongxunqa_holo_shanghai\",\"dbName\":\"yongxunqa_hologres_db\",\"syncType\":1,\"extendProjectName\":\"shanghai_onlineTest_simple\",\"schemaName\":\"public\",\"tableName\":\"wqtest\",\"partitionColumn\":\"\",\"orientation\":\"column\",\"columns\":[{\"name\":\"f1\",\"comment\":\"\",\"type\":\"STRING\",\"allowNull\":false,\"holoName\":\"f1\",\"holoType\":\"text\"},{\"name\":\"f2\",\"comment\":\"\",\"type\":\"STRING\",\"allowNull\":false,\"holoName\":\"f2\",\"holoType\":\"text\"},{\"name\":\"f4\",\"comment\":\"\",\"type\":\"STRING\",\"allowNull\":false,\"holoName\":\"f4\",\"holoType\":\"text\"},{\"name\":\"f5\",\"comment\":\"\",\"type\":\"STRING\",\"allowNull\":false,\"holoName\":\"f5\",\"holoType\":\"text\"},{\"name\":\"f3\",\"comment\":\"\",\"type\":\"STRING\",\"allowNull\":false,\"holoName\":\"f3\",\"holoType\":\"text\"},{\"name\":\"f6\",\"comment\":\"\",\"type\":\"STRING\",\"allowNull\":false,\"holoName\":\"f6\",\"holoType\":\"text\"},{\"name\":\"f7\",\"comment\":\"\",\"type\":\"STRING\",\"allowNull\":false,\"holoName\":\"f7\",\"holoType\":\"text\"},{\"name\":\"f10\",\"comment\":\"\",\"type\":\"STRING\",\"allowNull\":false,\"holoName\":\"f10\",\"holoType\":\"text\"},{\"name\":\"ds\",\"comment\":\"\",\"type\":\"BIGINT\",\"allowNull\":false,\"holoName\":\"ds\",\"holoType\":\"bigint\"},{\"name\":\"pt\",\"comment\":\"分区字段\",\"type\":\"STRING\",\"allowNull\":false,\"holoName\":\"pt\",\"holoType\":\"text\"}],\"serverName\":\"odps_server\",\"extendTableName\":\"wq_test_dataworks_pt_001\",\"foreignSchemaName\":\"public\",\"foreignTableName\":\"\",\"instanceId\":\"yongxunqa_holo_shanghai\",\"engineType\":\"Hologres\",\"clusteringKey\":[],\"bitmapIndexKey\":[],\"segmentKey\":[],\"dictionaryEncoding\":[]}"
          }
        },
        "trigger": {
          "type": "Scheduler",
          "cron": "00 00 00 * * ?",
          "startTime": "1970-01-01 00:00:00",
          "endTime": "9999-01-01 00:00:00"
        },
        "name": "Hologres Sync Node",
        "owner": "owner_id"
      }
    ],
    "flow": [
      {
        "nodeId": "hologres_sync_1",
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

### 数据同步配置

```json
{
  "script": {
    "runtime": {
      "command": "HOLOGRES_SYNC_DATA"
    },
    "content": {
      "content": "IMPORT FOREIGN SCHEMA...",
      "extraContent": "{...}"
    }
  }
}
```

## 实际用例

Hologres数据同步节点通常用于:
1. 从MaxCompute/ODPS同步数据到Hologres
2. 实现实时或准实时数据同步
3. 数据仓库到分析型数据库的数据流转

## 最佳实践

1. **字段映射**: 确保源表和目标表的字段类型正确映射
2. **分区管理**: 合理设置分区字段以优化查询性能
3. **增量同步**: 使用分区字段实现增量数据同步

## 注意事项

- 需要预先配置好Hologres实例和连接信息
- 注意数据类型兼容性，特别是时间类型和特殊字符
- 同步作业会生成大量SQL操作，注意资源使用情况