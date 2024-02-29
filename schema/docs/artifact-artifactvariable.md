# Artifact.Variable Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable
```

Variable定义了工作流的变量, Variable可以被在工作流节点中引用

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                      |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :------------------------------------------------------------------------------ |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [artifact.schema.json\*](../../out/artifact.schema.json "open original schema") |

## variable Type

`object` ([Artifact.Variable](artifact-artifactvariable.md))

# variable Properties

| Property        | Type     | Required | Nullable       | Defined by                                                                                                                                                  |
| :-------------- | :------- | :------- | :------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [id](#id)       | `string` | Required | cannot be null | [Artifact](artifact-artifactvariable-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable/properties/id")       |
| [name](#name)   | `string` | Required | cannot be null | [Artifact](artifact-artifactvariable-properties-name.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable/properties/name")   |
| [scope](#scope) | `string` | Required | cannot be null | [Artifact](artifact-artifactvariable-properties-scope.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable/properties/scope") |
| [type](#type)   | `string` | Required | cannot be null | [Artifact](artifact-artifactvariable-properties-type.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable/properties/type")   |
| [value](#value) | `string` | Required | cannot be null | [Artifact](artifact-artifactvariable-properties-value.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable/properties/value") |

## id

唯一标识

`id`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Artifact](artifact-artifactvariable-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable/properties/id")

### id Type

`string`

## name

变量名

`name`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Artifact](artifact-artifactvariable-properties-name.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable/properties/name")

### name Type

`string`

## scope

变量作用域

`scope`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Artifact](artifact-artifactvariable-properties-scope.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable/properties/scope")

### scope Type

`string`

### scope Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value             | Explanation             |
| :---------------- | :---------------------- |
| `"NodeParameter"` | 只有节点内使用的参数              |
| `"NodeContext"`   | 节点的上下文中的参数，可以被下游节点使用    |
| `"Workflow"`      | 工作流级别的参数，可以被工作流所有节点使用   |
| `"Workspace"`     | 工作空间级别的参数，可以被工作空间所有节点使用 |
| `"Tenant"`        | 租户级别参数，可以被租户的所有节点使用     |

## type

变量类型

`type`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Artifact](artifact-artifactvariable-properties-type.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable/properties/type")

### type Type

`string`

### type Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value        | Explanation           |
| :----------- | :-------------------- |
| `"System"`   | 系统变量，例如: $\[yyyymmdd] |
| `"Constant"` | 常量                    |

## value

变量值

`value`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Artifact](artifact-artifactvariable-properties-value.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable/properties/value")

### value Type

`string`

### value Examples

```json
"$[yyyymmdd]"
```

```json
"$[yyyymmdd-1]"
```

```json
"$[hh24-1/24]"
```

```json
"$[hh24miss-1/24/60]"
```

```json
"$[add_months(yyyymm,-1)]"
```

```json
"$[add_months(yyyymmdd,-12)]"
```
