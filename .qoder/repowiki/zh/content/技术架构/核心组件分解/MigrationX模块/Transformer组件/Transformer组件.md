# Transformer组件

<cite>
**本文档引用的文件**
- [BaseTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/BaseTransformerApp.java)
- [Transformer.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/transformer/Transformer.java)
- [AbstractPackageTransformer.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/transformer/AbstractPackageTransformer.java)
- [DataWorksAdfTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/apps/DataWorksAdfTransformerApp.java)
- [DataWorksAdfTransformer.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/transformer/DataWorksAdfTransformer.java)
- [ConfigPropertiesLoader.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/loader/ConfigPropertiesLoader.java)
- [ProjectWorkflowLoader.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/loader/ProjectWorkflowLoader.java)
- [CheckPoint.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/checkpoint/CheckPoint.java)
- [LocalFileCheckPoint.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/checkpoint/file/LocalFileCheckPoint.java)
- [Task.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/controller/Task.java)
- [TaskDag.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/controller/TaskDag.java)
- [dataworks-transformer-config.json](file://client/migrationx/migrationx-transformer/src/main/conf/dataworks-transformer-config.json)
- [AdfConverter.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/converter/adf/AdfConverter.java)
</cite>

## 目录
1. [简介](#简介)
2. [核心架构](#核心架构)
3. [转换任务生命周期](#转换任务生命周期)
4. [配置文件与规则引擎](#配置文件与规则引擎)
5. [上下文加载机制](#上下文加载机制)
6. [断点续传与状态恢复](#断点续传与状态恢复)
7. [具体实现分析](#具体实现分析)
8. [依赖关系分析](#依赖关系分析)

## 简介
Transformer组件是数据迁移管道的核心处理引擎，负责将Reader组件读取的源系统模型转换为DataWorks规范模型。该组件通过配置文件驱动的转换规则引擎，实现了从多种源系统（如Azure Data Factory、Airflow等）到DataWorks平台的自动化转换。组件采用模块化设计，通过任务依赖图（TaskDag）管理转换流程，支持断点续传和状态恢复功能。

## 核心架构

```mermaid
classDiagram
class BaseTransformerApp {
+String optConfig
+String optSourcePackage
+String optTargetPackage
+String checkpoint
+String load
+String resourceDir
+run(String[] args)
+doTransform()
+initCollector()
+checkAndSetCheckpoint()
+finishCollector()
}
class Transformer {
<<interface>>
+init()
+load()
+transform()
+write()
}
class AbstractPackageTransformer {
+PackageFileService sourcePackageFileService
+PackageFileService targetPackageFileService
+File configFile
+SP sourcePackage
+TP targetPackage
+AbstractPackageTransformer(File, SP, TP)
+getTargetPackage()
}
class DataWorksAdfTransformer {
+AdfConf adfConf
+List<Specification<DataWorksWorkflowSpec>> specs
+DataWorksAdfTransformer(File, AdfPackage, DataWorksPackage)
+loadConf(File)
+init()
+load()
+transform()
+write()
}
class DataWorksAdfTransformerApp {
+DataWorksAdfTransformerApp()
+initCollector()
+createTransformer(File, Package, Package)
}
BaseTransformerApp <|-- DataWorksAdfTransformerApp
Transformer <|-- AbstractPackageTransformer
AbstractPackageTransformer <|-- DataWorksAdfTransformer
DataWorksAdfTransformerApp --> DataWorksAdfTransformer : "创建"
```

**图示来源**
- [BaseTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/BaseTransformerApp.java)
- [Transformer.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/transformer/Transformer.java)
- [AbstractPackageTransformer.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/transformer/AbstractPackageTransformer.java)
- [DataWorksAdfTransformer.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/transformer/DataWorksAdfTransformer.java)
- [DataWorksAdfTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/apps/DataWorksAdfTransformerApp.java)

**本节来源**
- [BaseTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/BaseTransformerApp.java)
- [Transformer.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/transformer/Transformer.java)
- [AbstractPackageTransformer.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/transformer/AbstractPackageTransformer.java)

## 转换任务生命周期

```mermaid
sequenceDiagram
participant App as DataWorksAdfTransformerApp
participant BaseApp as BaseTransformerApp
participant Transformer as DataWorksAdfTransformer
participant Context as TransformerContext
App->>BaseApp : run(args)
BaseApp->>BaseApp : 解析命令行参数
BaseApp->>BaseApp : 初始化上下文
BaseApp->>BaseApp : checkAndSetCheckpoint()
BaseApp->>App : createTransformer()
App->>Transformer : 创建实例
BaseApp->>BaseApp : initCollector()
BaseApp->>Transformer : transformer.init()
BaseApp->>Transformer : transformer.load()
BaseApp->>Transformer : transformer.transform()
BaseApp->>Transformer : transformer.write()
BaseApp->>BaseApp : finishCollector()
BaseApp->>Context : 清理上下文
```

**图示来源**
- [BaseTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/BaseTransformerApp.java)
- [DataWorksAdfTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/apps/DataWorksAdfTransformerApp.java)
- [DataWorksAdfTransformer.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/transformer/DataWorksAdfTransformer.java)

**本节来源**
- [BaseTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/BaseTransformerApp.java)

## 配置文件与规则引擎

```mermaid
flowchart TD
Start([开始]) --> LoadConfig["加载配置文件"]
LoadConfig --> ParseConfig["解析JSON配置"]
ParseConfig --> ExtractSettings["提取转换设置"]
ExtractSettings --> MapNodeTypes["映射节点类型"]
MapNodeTypes --> ApplyRules["应用转换规则"]
ApplyRules --> ProcessShell["处理Shell节点类型"]
ApplyRules --> ProcessSql["处理SQL节点类型"]
ApplyRules --> ProcessSpark["处理Spark提交类型"]
ApplyRules --> ProcessUnknown["处理未知节点类型"]
ProcessShell --> SetShellType["设置为DIDE_SHELL"]
ProcessSql --> SetHiveType["设置为EMR_HIVE"]
ProcessSpark --> SetSparkType["设置为EMR_SPARK"]
ProcessUnknown --> SetDefaultType["设置为默认类型"]
SetShellType --> End([完成])
SetHiveType --> End
SetSparkType --> End
SetDefaultType --> End
```

**图示来源**
- [dataworks-transformer-config.json](file://client/migrationx/migrationx-transformer/src/main/conf/dataworks-transformer-config.json)

**本节来源**
- [dataworks-transformer-config.json](file://client/migrationx/migrationx-transformer/src/main/conf/dataworks-transformer-config.json)

## 上下文加载机制

```mermaid
classDiagram
class ConfigPropertiesLoader {
+String configPropertiesPath
+ConfigPropertiesLoader(String)
+ConfigPropertiesLoader(String, String, Task...)
+call() Properties
}
class ProjectWorkflowLoader {
+String projectDir
+List<DwWorkflow> workflowList
+List<DwResource> resources
+ProjectWorkflowLoader(String)
+ProjectWorkflowLoader(String, String)
+call() List<DwWorkflow>
+getResources()
+processWorkflowType(File, WorkflowType)
+isNewWorkflowDirectory(File)
+loadWorkflowDir(File)
+setDiResourceGroupInfo(DwNode)
}
class Task {
+String name
+List<Task<?>> dependencies
+T result
+Context context
+TaskStage stage
+TaskStatus taskStatus
+Task()
+Task(String)
+Task(String, Task...)
+call() T
+dependsOn(Task<?>)
}
Task <|-- ConfigPropertiesLoader
Task <|-- ProjectWorkflowLoader
ProjectWorkflowLoader --> Task : "依赖"
ConfigPropertiesLoader --> Task : "依赖"
```

**图示来源**
- [ConfigPropertiesLoader.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/loader/ConfigPropertiesLoader.java)
- [ProjectWorkflowLoader.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/loader/ProjectWorkflowLoader.java)
- [Task.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/controller/Task.java)

**本节来源**
- [ConfigPropertiesLoader.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/loader/ConfigPropertiesLoader.java)
- [ProjectWorkflowLoader.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/loader/ProjectWorkflowLoader.java)

## 断点续传与状态恢复

```mermaid
classDiagram
class CheckPoint {
<<interface>>
+ThreadLocal<CheckPoint> INSTANCE
+doWithCheckpoint(Function<WRITER, List<SPEC>>, String)
+doCheckpoint(WRITER, List<SPEC>, String, String)
+loadFromCheckPoint(String, String)
+getInstance()
}
class LocalFileCheckPoint {
+String SUFFIX
+boolean FILE_APPEND
+TypeReference<List<SPEC>> REF
+ObjectMapper objectMapper
+doWithCheckpoint(Function<BufferedFileWriter, List<SPEC>>, String)
+doWithCheckpoint(Function<BufferedFileWriter, List<SPEC>>, File)
+doCheckpoint(BufferedFileWriter, List<SPEC>, String, String)
+loadFromCheckPoint(String, String)
+loadFromCheckpoint(File, String)
+doLoad(File, LineConsumer)
+readFile(BufferedReader, LineConsumer)
+readFile(BufferedReader, int, LineConsumer)
+toJson(Object)
+parseJson(String)
}
class BufferedFileWriter {
+writeLine(String, String, String)
}
class TransformerContext {
+File checkpoint
+File load
+setCheckpoint(File)
+setLoad(File)
+getCheckpoint()
+getLoad()
}
CheckPoint <|-- LocalFileCheckPoint
LocalFileCheckPoint --> BufferedFileWriter : "使用"
BaseTransformerApp --> TransformerContext : "设置"
DataWorksAdfTransformer --> CheckPoint : "使用"
```

**图示来源**
- [CheckPoint.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/checkpoint/CheckPoint.java)
- [LocalFileCheckPoint.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/checkpoint/file/LocalFileCheckPoint.java)
- [BaseTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/BaseTransformerApp.java)

**本节来源**
- [CheckPoint.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/checkpoint/CheckPoint.java)
- [LocalFileCheckPoint.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/checkpoint/file/LocalFileCheckPoint.java)
- [BaseTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/BaseTransformerApp.java)

## 具体实现分析

```mermaid
sequenceDiagram
participant App as DataWorksAdfTransformerApp
participant Transformer as DataWorksAdfTransformer
participant Converter as AdfConverter
participant AdfPackage as AdfPackage
participant WorkflowSpec as Specification<DataWorksWorkflowSpec>
App->>Transformer : createTransformer()
Transformer->>Transformer : loadConf(configFile)
Transformer->>Transformer : init()
Transformer->>Transformer : load()
Transformer->>AdfPackage : sourcePackageFileService.load()
AdfPackage-->>Transformer : 加载完成
Transformer->>Transformer : transform()
Transformer->>Converter : new AdfConverter()
Converter->>Converter : convert()
Converter->>Converter : toWorkflow(pipeline, trigger)
Converter->>Converter : setTrigger(flow, trigger)
Converter->>Converter : setFlowNodesAndDependencies()
Converter-->>Transformer : 返回SpecWorkflow列表
Transformer->>Transformer : toWorkflowSpecFile()
Transformer-->>Transformer : 构建Specification列表
Transformer->>Transformer : write()
Transformer->>Transformer : 写入JSON文件
Transformer-->>App : 转换完成
```

**图示来源**
- [DataWorksAdfTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/apps/DataWorksAdfTransformerApp.java)
- [DataWorksAdfTransformer.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/transformer/DataWorksAdfTransformer.java)
- [AdfConverter.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/converter/adf/AdfConverter.java)

**本节来源**
- [DataWorksAdfTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/apps/DataWorksAdfTransformerApp.java)
- [DataWorksAdfTransformer.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/transformer/DataWorksAdfTransformer.java)
- [AdfConverter.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/converter/adf/AdfConverter.java)

## 依赖关系分析

```mermaid
graph TD
A[DataWorksAdfTransformerApp] --> B[BaseTransformerApp]
B --> C[TransformerContext]
C --> D[LocalFileCheckPoint]
A --> E[DataWorksAdfTransformer]
E --> F[AdfConverter]
F --> G[AdfPackage]
E --> H[ConfigPropertiesLoader]
E --> I[ProjectWorkflowLoader]
H --> J[Task]
I --> J
J --> K[TaskDag]
K --> L[TaskStage]
K --> M[TaskStatus]
E --> N[SpecUtil]
N --> O[Specification]
P[CommandApp] --> B
Q[PackageFileService] --> G
R[Transformer] --> E
S[AbstractPackageTransformer] --> E
T[Context] --> E
style A fill:#f9f,stroke:#333
style B fill:#bbf,stroke:#333
style C fill:#f96,stroke:#333
style D fill:#f96,stroke:#333
style E fill:#f9f,stroke:#333
style F fill:#bbf,stroke:#333
style G fill:#9f9,stroke:#333
style H fill:#bbf,stroke:#333
style I fill:#bbf,stroke:#333
style J fill:#f96,stroke:#333
style K fill:#f96,stroke:#333
```

**图示来源**
- [BaseTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/BaseTransformerApp.java)
- [DataWorksAdfTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/apps/DataWorksAdfTransformerApp.java)
- [DataWorksAdfTransformer.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/transformer/DataWorksAdfTransformer.java)
- [AdfConverter.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/converter/adf/AdfConverter.java)
- [ConfigPropertiesLoader.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/loader/ConfigPropertiesLoader.java)
- [ProjectWorkflowLoader.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/loader/ProjectWorkflowLoader.java)
- [Task.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/controller/Task.java)
- [TaskDag.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/controller/TaskDag.java)
- [CheckPoint.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/checkpoint/CheckPoint.java)
- [LocalFileCheckPoint.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/checkpoint/file/LocalFileCheckPoint.java)

**本节来源**
- [BaseTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/BaseTransformerApp.java)
- [DataWorksAdfTransformerApp.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/apps/DataWorksAdfTransformerApp.java)
- [DataWorksAdfTransformer.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/transformer/DataWorksAdfTransformer.java)
- [AdfConverter.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/dataworks/converter/adf/AdfConverter.java)
- [ConfigPropertiesLoader.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/loader/ConfigPropertiesLoader.java)
- [ProjectWorkflowLoader.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/loader/ProjectWorkflowLoader.java)
- [Task.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/controller/Task.java)
- [TaskDag.java](file://client/migrationx/migrationx-transformer/src/main/java/com/aliyun/dataworks/migrationx/transformer/core/controller/TaskDag.java)