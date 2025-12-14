# DataWorks Spec 模板文档

本目录包含 DataWorks FlowSpec 的详细模板和示例，帮助用户理解和创建符合 Schema 规范的工作流定义。

## Spec 模块核心功能总结

### 1. 规范解析与序列化 (Parse & Write)
- **`SpecUtil.parseToDomain(String spec)`**: 将 JSON 字符串解析为 Spec 领域对象
- **`SpecUtil.writeToSpec(Specification<T> specification)`**: 将 Spec 对象序列化为 JSON 字符串
- 支持多种 Spec 版本 (1.0.0, 1.1.0 等) 的解析和转换

### 2. 实体类型系统
FlowSpec 定义了完整的实体类型系统:
- **SpecRefEntity**: 可被引用的实体(如 Node, Script, Variable, Function, RuntimeResource)
- **SpecNoRefEntity**: 不可被引用的实体(如 Branch, DoWhile, ForEach)
- **引用机制**: 支持 `{{variables.xxx}}`, `{{scripts.yyy}}` 等模板引用语法

### 3. 工作流类型 (Workflow Types)
| 类型 | 说明 | 触发方式 |
|------|------|----------|
| **CycleWorkflow** | 周期调度工作流 | Scheduler (cron expression) |
| **ManualWorkflow** | 手动触发工作流 | Manual |
| **TriggerWorkflow** | 事件触发工作流 | Event-based triggers |

### 4. 节点类型 (Node Types)
根据 `CodeProgramType` 枚举,DataWorks 支持100+种节点类型:

#### 数据处理节点
- **ODPS/MaxCompute**: ODPS_SQL, PYODPS, ODPS_MR, ODPS_SPARK 等
- **EMR**: EMR_HIVE, EMR_SPARK, EMR_SPARK_SQL, EMR_MR, EMR_SHELL, EMR_PRESTO, EMR_IMPALA 等
- **Flink**: FLINK_SQL_STREAM, FLINK_SQL_BATCH, BLINK_DATASTREAM 等
- **Hologres**: HOLOGRES_SQL, HOLOGRES_SYNC_DDL, HOLOGRES_SYNC_DATA 等
- **数据库**: MYSQL, POSTGRESQL, SQLSERVER, Oracle, ClickHouse, StarRocks 等

#### 数据集成节点
- DATAX, DI, CDP, RI

#### 控制流节点
- CONTROLLER_ASSIGNMENT (赋值)
- CONTROLLER_BRANCH (分支)
- CONTROLLER_JOIN (汇聚)
- CONTROLLER_CYCLE (DoWhile 循环)
- CONTROLLER_TRAVERSE (ForEach 遍历)
- PARAM_HUB (参数传递中心)

#### 通用节点
- SHELL, DIDE_SHELL, PYTHON, PERL
- VIRTUAL (虚拟节点)
- COMBINED_NODE (组合节点)
- SUB_PROCESS (子流程)

### 5. 核心组件

#### Script Runtime
支持多种计算引擎和命令类型:
- **engine**: MaxCompute, EMR, Hologres, Flink, Database 等
- **command**: ODPS_SQL, EMR_SPARK, HOLOGRES_SQL 等
- **配置**: sparkConf, flinkConf, emrJobConfig, maxComputeConf 等

#### 依赖类型 (Dependency Types)
- **Normal**: 同周期依赖
- **CrossCycleDependsOnSelf**: 跨周期自依赖
- **CrossCycleDependsOnChildren**: 跨周期子节点依赖  
- **CrossCycleDependsOnOtherNode**: 跨周期其他节点依赖

#### 变量范围 (Variable Scopes)
- **Tenant**: 租户级别
- **Workspace**: 工作空间级别
- **Workflow**: 工作流级别
- **NodeParameter**: 节点参数级别
- **NodeContext**: 节点上下文级别

### 6. 验证与转换
- JSON Schema 验证 (基于 schema/ 目录的 schema 定义)
- 版本适配与转换
- DataWorks 与其他调度系统的模型转换 (通过 MigrationX)

## 目录结构

```
spec-templates/
├── README.md                           # 本文件
├── workflows/                          # 工作流模板
│   ├── cycle-workflow.md              # 周期调度工作流
│   ├── manual-workflow.md             # 手动工作流
│   └── trigger-workflow.md            # 事件触发工作流
├── nodes/                              # 节点类型模板
│   ├── odps-sql.md                    # MaxCompute SQL 节点
│   ├── emr-spark.md                   # EMR Spark 节点
│   ├── hologres-sql.md                # Hologres SQL 节点
│   ├── flink-sql.md                   # Flink SQL 节点
│   ├── datax.md                       # 数据集成节点
│   ├── shell.md                       # Shell 节点
│   ├── pyodps.md                      # PyODPS 节点
│   ├── hologres-sync-data.md          # Hologres数据同步节点
│   ├── component-sql.md               # 组件SQL节点
│   ├── pai-studio.md                  # PAI Studio节点
│   ├── xlab.md                        # Xlab节点
│   ├── hologres-sync-ddl.md           # Hologres DDL节点
│   ├── pai-flow.md                    # PAI Flow节点
│   └── virtual-node.md                # 虚拟节点
├── control-flow/                       # 控制流节点模板
│   ├── branch.md                      # 分支节点
│   ├── join.md                        # 汇聚节点
│   ├── dowhile.md                     # DoWhile 循环节点
│   ├── foreach.md                     # ForEach 遍历节点
│   ├── assignment.md                  # 赋值节点
│   └── param-hub.md                   # 参数传递中心
└── resources/                          # 资源定义模板
    ├── runtime-resource.md            # 运行时资源
    ├── file-resource.md               # 文件资源
    ├── function.md                    # 函数定义
    └── variable.md                    # 变量定义

```

## 使用说明

1. **选择合适的工作流类型**: 根据调度需求选择 CycleWorkflow 或 ManualWorkflow
2. **定义节点**: 根据计算引擎选择对应的节点类型模板
3. **配置依赖关系**: 在 `flow` 部分定义节点间的依赖
4. **添加资源**: 配置 runtimeResources, fileResources, functions, variables 等
5. **验证规范**: 使用 `SpecUtil.parseToDomain()` 验证 JSON 格式是否符合规范

## Schema 规范

所有模板严格遵守 `schema/` 目录下的 JSON Schema 定义:
- `flow.schema.json`: 工作流顶层结构
- `node.schema.json`: 节点定义
- `script.schema.json`: 脚本定义
- `trigger.schema.json`: 触发器定义
- `artifact.schema.json`: 产物定义
- `runtimeResource.schema.json`: 运行时资源定义
- `fileResource.schema.json`: 文件资源定义
- `function.schema.json`: 函数定义

## 参考资料

- [FlowSpec 字段参考](../../README.md#flowspec-field-reference)
- [JSON Schema 文档](../../schema/docs/README.md)
- [MigrationX 使用文档](../migrationx/usage.md)
