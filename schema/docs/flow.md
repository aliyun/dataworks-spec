# Flow Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json
```

DataWorks 通用的工作流描述规范

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                            |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :-------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [flow.schema.json](../../out/flow.schema.json "open original schema") |

## Flow Type

`object` ([Flow](flow.md))

# Flow Properties

| Property              | Type     | Required | Nullable       | Defined by                                                                                                                      |
| :-------------------- | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------ |
| [version](#version)   | `string` | Required | cannot be null | [Flow](flow-properties-version.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/version")       |
| [kind](#kind)         | `string` | Required | cannot be null | [Flow](flow-properties-kind.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/kind")             |
| [spec](#spec)         | `object` | Required | cannot be null | [Flow](flow-properties-flowworkflowspec.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec") |
| [metadata](#metadata) | `object` | Optional | cannot be null | [Flow](flow-properties-flowmetedata.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/metadata") |

## version

版本号

`version`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Flow](flow-properties-version.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/version")

### version Type

`string`

## kind

工作流类型

`kind`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Flow](flow-properties-kind.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/kind")

### kind Type

`string`

### kind Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value              | Explanation |
| :----------------- | :---------- |
| `"CycleWorkflow"`  | 周期调度工作流     |
| `"ManualWorkflow"` | 手动触发工作流     |

## spec

Workflow 的 Spec 定义

`spec`

*   is required

*   Type: `object` ([Flow.WorkflowSpec](flow-properties-flowworkflowspec.md))

*   cannot be null

*   defined in: [Flow](flow-properties-flowworkflowspec.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec")

### spec Type

`object` ([Flow.WorkflowSpec](flow-properties-flowworkflowspec.md))

## metadata

定义工作流的扩展元信息

`metadata`

*   is optional

*   Type: `object` ([Flow.Metedata](flow-properties-flowmetedata.md))

*   cannot be null

*   defined in: [Flow](flow-properties-flowmetedata.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/metadata")

### metadata Type

`object` ([Flow.Metedata](flow-properties-flowmetedata.md))
