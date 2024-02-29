# Flow\.WorkflowSpec Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec
```

Workflow 的 Spec 定义

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                              |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [flow.schema.json\*](../../out/flow.schema.json "open original schema") |

## spec Type

`object` ([Flow.WorkflowSpec](flow-properties-flowworkflowspec.md))

# spec Properties

| Property                              | Type    | Required | Nullable       | Defined by                                                                                                                                                                                              |
| :------------------------------------ | :------ | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| [nodes](#nodes)                       | `array` | Required | cannot be null | [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecnodes.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/nodes")                       |
| [scripts](#scripts)                   | `array` | Optional | cannot be null | [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecscripts.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/scripts")                   |
| [artifacts](#artifacts)               | `array` | Optional | cannot be null | [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecartifacts.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/artifacts")               |
| [variables](#variables)               | `array` | Optional | cannot be null | [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecvariables.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/variables")               |
| [runtimeResources](#runtimeresources) | `array` | Optional | cannot be null | [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecruntimeresources.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/runtimeResources") |
| [fileResources](#fileresources)       | `array` | Optional | cannot be null | [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecfileresources.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/fileResources")       |
| [functions](#functions)               | `array` | Optional | cannot be null | [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecfunctions.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/functions")               |
| [flow](#flow)                         | `array` | Optional | cannot be null | [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecedges.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow")                        |

## nodes

任务节点列表

`nodes`

*   is required

*   Type: `object[]` ([Node](node.md))

*   cannot be null

*   defined in: [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecnodes.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/nodes")

### nodes Type

`object[]` ([Node](node.md))

## scripts

定义工作流的变量

`scripts`

*   is optional

*   Type: `object[]` ([Script](script.md))

*   cannot be null

*   defined in: [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecscripts.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/scripts")

### scripts Type

`object[]` ([Script](script.md))

## artifacts

定义工作流的变量

`artifacts`

*   is optional

*   Type: `object[]` ([Artifact](artifact.md))

*   cannot be null

*   defined in: [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecartifacts.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/artifacts")

### artifacts Type

`object[]` ([Artifact](artifact.md))

## variables

定义工作流的变量

`variables`

*   is optional

*   Type: `object[]` ([Artifact](artifact.md))

*   cannot be null

*   defined in: [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecvariables.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/variables")

### variables Type

`object[]` ([Artifact](artifact.md))

## runtimeResources

运行时资源定义列表

`runtimeResources`

*   is optional

*   Type: `object[]` ([RuntimeResource](runtimeresource.md))

*   cannot be null

*   defined in: [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecruntimeresources.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/runtimeResources")

### runtimeResources Type

`object[]` ([RuntimeResource](runtimeresource.md))

## fileResources

文件资源列表

`fileResources`

*   is optional

*   Type: `object[]` ([FileResource](fileresource.md))

*   cannot be null

*   defined in: [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecfileresources.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/fileResources")

### fileResources Type

`object[]` ([FileResource](fileresource.md))

## functions

函数定义列表

`functions`

*   is optional

*   Type: `object[]` ([Function](function.md))

*   cannot be null

*   defined in: [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecfunctions.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/functions")

### functions Type

`object[]` ([Function](function.md))

## flow

工作流节点之间的依赖关系

`flow`

*   is optional

*   Type: `object[]` ([Flow.WorkflowSpec.Edge](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge.md))

*   cannot be null

*   defined in: [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecedges.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow")

### flow Type

`object[]` ([Flow.WorkflowSpec.Edge](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge.md))
