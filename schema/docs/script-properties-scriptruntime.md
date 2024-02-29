# Script.Runtime Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/runtime
```

脚本语言

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                  |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :-------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [script.schema.json\*](../../out/script.schema.json "open original schema") |

## runtime Type

`object` ([Script.Runtime](script-properties-scriptruntime.md))

# runtime Properties

| Property            | Type     | Required | Nullable       | Defined by                                                                                                                                                                  |
| :------------------ | :------- | :------- | :------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [engine](#engine)   | `string` | Optional | cannot be null | [Script](script-properties-scriptruntime-properties-engine.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/runtime/properties/engine")   |
| [command](#command) | `string` | Required | cannot be null | [Script](script-properties-scriptruntime-properties-command.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/runtime/properties/command") |

## engine

运行时引擎

`engine`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Script](script-properties-scriptruntime-properties-engine.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/runtime/properties/engine")

### engine Type

`string`

## command

运行时环境的命令标识

`command`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Script](script-properties-scriptruntime-properties-command.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/runtime/properties/command")

### command Type

`string`
