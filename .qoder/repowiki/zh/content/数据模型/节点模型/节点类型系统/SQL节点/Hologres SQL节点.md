# Hologres SQL节点

<cite>
**本文档引用文件**  
- [hologres-sync-data.md](file://docs/spec-templates/nodes/hologres-sync-data.md)
- [hologres-sync-ddl.md](file://docs/spec-templates/nodes/hologres-sync-ddl.md)
- [dataworks-transformer-config-hologres-sample.json](file://client/migrationx/migrationx-transformer/src/main/conf/dataworks-transformer-config-hologres-sample.json)
- [SqlComponentCode.java](file://spec/src/main/java/com/aliyun/dataworks/common/spec/domain/dw/codemodel/SqlComponentCode.java)
- [ComponentSqlCode.java](file://spec/src/main/java/com/aliyun/dataworks/common/spec/domain/dw/codemodel/ComponentSqlCode.java)
- [SpecComponent.java](file://spec/src/main/java/com/aliyun/dataworks/common/spec/domain/ref/component/SpecComponent.java)
- [SpecComponentParameter.java](file://spec/src/main/java/com/aliyun/dataworks/common/spec/domain/ref/component/SpecComponentParameter.java)
</cite>

## 目录

1. [Hologres SQL节点](#hologres-sql节点)
2. [实现机制](#实现机制)
3. [配置属性与执行参数](#配置属性与执行参数)
4. [DDL同步与数据同步配置模式](#ddl同步与数据同步配置模式)
5. [连接配置与同步模式](#连接配置与同步模式)
6. [性能调优参数](#性能调优参数)
7. [典型配置示例](#典型配置示例)
8. [集成方式与权限配置](#集成方式与权限配置)

## 实现机制

Hologres SQL节点在dataworks-spec项目中通过`SqlComponentCode`和`ComponentSqlCode`类实现，这些类继承自`AbstractBaseCode`，负责处理SQL组件节点的解析和代码生成。`SqlComponentCode`类用于处理标准SQL组件节点，而`ComponentSqlCode`类则用于处理更复杂的组件SQL节点。

`SqlComponentCode`类的`parse`方法负责解析JSON格式的SQL组件代码内容，提取代码、配置和程序类型等信息。`ComponentSqlCode`类的`mergeSqlComponentParamsIntoCode`方法则负责将组件节点的参数合并到SQL代码中，生成最终的执行代码。

Hologres SQL节点通过`SpecComponent`类定义组件的通用属性，包括名称、所有者、描述、脚本、输入和输出参数。`SpecComponentParameter`类定义了组件参数的属性，包括名称、类型、值、参数、默认值和描述。

**Section sources**
- [SqlComponentCode.java](file://spec/src/main/java/com/aliyun/dataworks/common/spec/domain/dw/codemodel/SqlComponentCode.java#L74-L107)
- [ComponentSqlCode.java](file://spec/src/main/java/com/aliyun/dataworks/common/spec/domain/dw/codemodel/ComponentSqlCode.java#L34-L125)
- [SpecComponent.java](file://spec/src/main/java/com/aliyun/dataworks/common/spec/domain/ref/component/SpecComponent.java#L1-L52)
- [SpecComponentParameter.java](file://spec/src/main/java/com/aliyun/dataworks/common/spec/domain/ref/component/SpecComponentParameter.java#L1-L39)

## 配置属性与执行参数

Hologres SQL节点的配置属性和执行参数主要通过`SpecComponent`和`SpecComponentParameter`类定义。`SpecComponent`类定义了组件的通用属性，包括：

- **id**: 组件的唯一标识符
- **name**: 组件的名称
- **owner**: 组件的所有者
- **description**: 组件的描述
- **script**: 组件的脚本定义
- **inputs**: 组件的输入参数列表
- **outputs**: 组件的输出参数列表

`SpecComponentParameter`类定义了组件参数的属性，包括：

- **name**: 参数的名称
- **type**: 参数的类型
- **value**: 参数的值
- **args**: 参数的额外参数
- **defaultValue**: 参数的默认值
- **description**: 参数的描述

在Hologres SQL节点中，这些配置属性和执行参数通过JSON格式的代码内容传递，`SqlComponentCode`类的`parse`方法负责解析这些内容，并将其转换为相应的Java对象。

**Section sources**
- [SpecComponent.java](file://spec/src/main/java/com/aliyun/dataworks/common/spec/domain/ref/component/SpecComponent.java#L1-L52)
- [SpecComponentParameter.java](file://spec/src/main/java/com/aliyun/dataworks/common/spec/domain/ref/component/SpecComponentParameter.java#L1-L39)

## DDL同步与数据同步配置模式

Hologres SQL节点支持两种主要的配置模式：DDL同步和数据同步。这两种模式通过不同的`CodeProgramType`来区分：

- **HOLOGRES_SYNC_DDL (commandTypeId: 801)**: 用于在Hologres实例中执行数据定义语言(DDL)操作，如创建、修改或删除表结构。
- **HOLOGRES_SYNC_DATA (commandTypeId: 800)**: 用于将数据从源端同步到Hologres实例，支持结构化和非结构化的数据同步。

在DDL同步模式下，Hologres SQL节点的脚本内容通常包含CREATE TABLE、ALTER TABLE、DROP TABLE等DDL语句。而在数据同步模式下，脚本内容通常包含IMPORT FOREIGN SCHEMA、INSERT INTO、SELECT等数据操作语句。

两种模式的配置结构相似，都包含`script`、`runtime`和`content`等属性。但在数据同步模式下，`content`属性包含一个额外的`extraContent`字段，用于定义数据同步的详细配置，如连接ID、数据库名称、同步类型、扩展项目名称、模式名称、表名称、分区列、方向、列定义、服务器名称、扩展表名称、外部模式名称、外部表名称、实例ID、引擎类型、聚类键、位图索引键、段键和字典编码等。

**Section sources**
- [hologres-sync-ddl.md](file://docs/spec-templates/nodes/hologres-sync-ddl.md#L1-L95)
- [hologres-sync-data.md](file://docs/spec-templates/nodes/hologres-sync-data.md#L1-L100)

## 连接配置与同步模式

Hologres SQL节点的连接配置和同步模式通过`extraContent`字段定义。`extraContent`是一个JSON字符串，包含以下属性：

- **connId**: 连接ID，用于标识Hologres实例的连接
- **dbName**: 数据库名称，指定要操作的数据库
- **syncType**: 同步类型，1表示全量同步，2表示增量同步
- **extendProjectName**: 扩展项目名称，用于标识源端项目
- **schemaName**: 模式名称，指定要操作的模式
- **tableName**: 表名称，指定要操作的表
- **partitionColumn**: 分区列，指定用于分区的列
- **orientation**: 方向，'row'表示行存储，'column'表示列存储
- **columns**: 列定义，包含列名、注释、类型、是否允许空值、Hologres名称和Hologres类型
- **serverName**: 服务器名称，用于标识源端服务器
- **extendTableName**: 扩展表名称，用于标识源端表
- **foreignSchemaName**: 外部模式名称，用于标识外部模式
- **foreignTableName**: 外部表名称，用于标识外部表
- **instanceId**: 实例ID，用于标识Hologres实例
- **engineType**: 引擎类型，'Hologres'表示Hologres引擎
- **clusteringKey**: 聚类键，用于定义表的聚类键
- **bitmapIndexKey**: 位图索引键，用于定义表的位图索引键
- **segmentKey**: 段键，用于定义表的段键
- **dictionaryEncoding**: 字典编码，用于定义表的字典编码

这些配置属性允许Hologres SQL节点灵活地连接到不同的Hologres实例，并根据需要进行数据同步。

**Section sources**
- [hologres-sync-data.md](file://docs/spec-templates/nodes/hologres-sync-data.md#L1-L100)

## 性能调优参数

Hologres SQL节点的性能调优参数主要通过`extraContent`字段中的`clusteringKey`、`bitmapIndexKey`、`segmentKey`和`dictionaryEncoding`属性定义。这些参数允许用户优化Hologres表的性能：

- **clusteringKey**: 聚类键，用于定义表的聚类键。聚类键可以提高查询性能，特别是在范围查询和排序操作中。
- **bitmapIndexKey**: 位图索引键，用于定义表的位图索引键。位图索引可以提高等值查询的性能，特别是在低基数列上。
- **segmentKey**: 段键，用于定义表的段键。段键可以提高数据分布的均匀性，从而提高查询性能。
- **dictionaryEncoding**: 字典编码，用于定义表的字典编码。字典编码可以减少存储空间，提高查询性能。

通过合理配置这些性能调优参数，用户可以显著提高Hologres SQL节点的执行效率和查询性能。

**Section sources**
- [hologres-sync-data.md](file://docs/spec-templates/nodes/hologres-sync-data.md#L1-L100)

## 典型配置示例

### DDL变更脚本

以下是一个Hologres DDL节点的典型配置示例，用于创建一个新表：

```json
{
  "version": "1.1.0",
  "kind": "CycleWorkflow",
  "spec": {
    "nodes": [
      {
        "recurrence": "Normal",
        "id": "hologres_ddl_1",
        "instanceMode": "T+1",
        "rerunMode": "Allowed",
        "rerunTimes": 0,
        "rerunInterval": 0,
        "autoParse": true,
        "script": {
          "path": "path/to/hologres_ddl.sql",
          "runtime": {
            "command": "HOLOGRES_SYNC_DDL"
          },
          "content": "CREATE TABLE example_table (id int, name text);"
        },
        "trigger": {
          "type": "Scheduler",
          "cron": "00 00 00 * * ?",
          "startTime": "1970-01-01 00:00:00",
          "endTime": "9999-01-01 00:00:00"
        },
        "name": "Hologres DDL Node",
        "owner": "owner_id"
      }
    ],
    "flow": [
      {
        "nodeId": "hologres_ddl_1",
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

### 数据同步任务

以下是一个Hologres数据同步节点的典型配置示例，用于将数据从MaxCompute同步到Hologres：

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

**Section sources**
- [hologres-sync-ddl.md](file://docs/spec-templates/nodes/hologres-sync-ddl.md#L1-L95)
- [hologres-sync-data.md](file://docs/spec-templates/nodes/hologres-sync-data.md#L1-L100)

## 集成方式与权限配置

Hologres SQL节点通过DataWorks平台与Hologres实例集成。用户需要在DataWorks中配置Hologres实例的连接信息，包括连接ID、数据库名称、用户名、密码等。这些连接信息存储在DataWorks的元数据中，Hologres SQL节点在执行时会使用这些连接信息来连接到Hologres实例。

权限配置方面，Hologres SQL节点需要具有足够的权限来执行DDL和DML操作。通常，这需要用户具有CREATE、ALTER、DROP、INSERT、UPDATE、DELETE等权限。用户可以在Hologres实例中创建一个专门用于DataWorks的用户，并授予该用户必要的权限。

此外，Hologres SQL节点还支持通过`extraContent`字段中的`connId`属性来指定不同的连接。这允许用户在同一个工作流中使用不同的Hologres实例，或者在不同的环境中使用不同的连接配置。

**Section sources**
- [hologres-sync-data.md](file://docs/spec-templates/nodes/hologres-sync-data.md#L1-L100)
- [dataworks-transformer-config-hologres-sample.json](file://client/migrationx/migrationx-transformer/src/main/conf/dataworks-transformer-config-hologres-sample.json#L1-L17)