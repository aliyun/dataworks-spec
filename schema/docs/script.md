# Script Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json
```

节点所需的脚本定义或者引用

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :------------------------------------------------------------------------ |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [script.schema.json](../../out/script.schema.json "open original schema") |

## Script Type

`object` ([Script](script.md))

# Script Properties

| Property                  | Type     | Required | Nullable       | Defined by                                                                                                                                  |
| :------------------------ | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------ |
| [id](#id)                 | `string` | Required | cannot be null | [Script](script-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/id")                       |
| [path](#path)             | `string` | Required | cannot be null | [Script](script-properties-path.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/path")                   |
| [language](#language)     | `string` | Optional | cannot be null | [Script](script-properties-language.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/language")           |
| [runtime](#runtime)       | `object` | Required | cannot be null | [Script](script-properties-scriptruntime.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/runtime")       |
| [parameters](#parameters) | `array`  | Optional | cannot be null | [Script](script-properties-scriptparameters.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/parameters") |

## id

唯一标识

`id`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Script](script-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/id")

### id Type

`string`

## path

脚本路径

`path`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Script](script-properties-path.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/path")

### path Type

`string`

## language

脚本语言

`language`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Script](script-properties-language.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/language")

### language Type

`string`

## runtime

脚本语言

`runtime`

*   is required

*   Type: `object` ([Script.Runtime](script-properties-scriptruntime.md))

*   cannot be null

*   defined in: [Script](script-properties-scriptruntime.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/runtime")

### runtime Type

`object` ([Script.Runtime](script-properties-scriptruntime.md))

## parameters

脚本参数列表

`parameters`

*   is optional

*   Type: `object[]` ([Artifact](artifact.md))

*   cannot be null

*   defined in: [Script](script-properties-scriptparameters.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/parameters")

### parameters Type

`object[]` ([Artifact](artifact.md))
