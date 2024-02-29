# Node Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json
```

工作流节点的定义描述

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                            |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :-------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [node.schema.json](../../out/node.schema.json "open original schema") |

## Node Type

`object` ([Node](node.md))

# Node Properties

| Property                            | Type      | Required | Nullable       | Defined by                                                                                                                                |
| :---------------------------------- | :-------- | :------- | :------------- | :---------------------------------------------------------------------------------------------------------------------------------------- |
| [id](#id)                           | `string`  | Required | cannot be null | [Node](node-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/id")                           |
| [name](#name)                       | `string`  | Required | cannot be null | [Node](node-properties-name.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/name")                       |
| [script](#script)                   | `object`  | Required | cannot be null | [Node](script.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/script")                                 |
| [trigger](#trigger)                 | `object`  | Optional | cannot be null | [Node](trigger.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/trigger")                              |
| [inputs](#inputs)                   | `object`  | Optional | cannot be null | [Node](node-properties-nodeinputartifact.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/inputs")        |
| [outputs](#outputs)                 | `object`  | Optional | cannot be null | [Node](node-properties-nodeoutputartifact.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/outputs")      |
| [functions](#functions)             | `array`   | Optional | cannot be null | [Node](node-properties-nodefunctions.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/functions")         |
| [fileResources](#fileresources)     | `array`   | Optional | cannot be null | [Node](node-properties-nodefileresources.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/fileResources") |
| [runtimeResource](#runtimeresource) | `object`  | Optional | cannot be null | [Node](runtimeresource.md "https://dataworks.data.aliyun.com/schemas/1.1.0/runtimeResource.schema.json#/properties/runtimeResource")      |
| [recurrence](#recurrence)           | `string`  | Optional | cannot be null | [Node](node-properties-recurrence.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/recurrence")           |
| [priority](#priority)               | `integer` | Optional | cannot be null | [Node](node-properties-priority.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/priority")               |
| [timeout](#timeout)                 | `integer` | Optional | cannot be null | [Node](node-properties-timeout.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/timeout")                 |
| [instanceMode](#instancemode)       | `string`  | Optional | cannot be null | [Node](node-properties-instancemode.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/instanceMode")       |
| [rerunMode](#rerunmode)             | `string`  | Optional | cannot be null | [Node](node-properties-rerunmode.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/rerunMode")             |

## id

节点在 Spec 中的唯一 Id

`id`

*   is required

*   Type: `string` ([Id](node-properties-id.md))

*   cannot be null

*   defined in: [Node](node-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/id")

### id Type

`string` ([Id](node-properties-id.md))

## name

节点名

`name`

*   is required

*   Type: `string` ([Name](node-properties-name.md))

*   cannot be null

*   defined in: [Node](node-properties-name.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/name")

### name Type

`string` ([Name](node-properties-name.md))

## script

节点所需的脚本定义或者引用

`script`

*   is required

*   Type: `object` ([Script](script.md))

*   cannot be null

*   defined in: [Node](script.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/script")

### script Type

`object` ([Script](script.md))

## trigger

定义了工作流的触发器

`trigger`

*   is optional

*   Type: `object` ([Trigger](trigger.md))

*   cannot be null

*   defined in: [Node](trigger.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/trigger")

### trigger Type

`object` ([Trigger](trigger.md))

## inputs

节点的输入

`inputs`

*   is optional

*   Type: `object` ([Node.InputArtifact](node-properties-nodeinputartifact.md))

*   cannot be null

*   defined in: [Node](node-properties-nodeinputartifact.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/inputs")

### inputs Type

`object` ([Node.InputArtifact](node-properties-nodeinputartifact.md))

## outputs

输出定义了工作流节点的产出信息

`outputs`

*   is optional

*   Type: `object` ([Node.OutputArtifact](node-properties-nodeoutputartifact.md))

*   cannot be null

*   defined in: [Node](node-properties-nodeoutputartifact.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/outputs")

### outputs Type

`object` ([Node.OutputArtifact](node-properties-nodeoutputartifact.md))

## functions

节点所需的函数定义或者引用

`functions`

*   is optional

*   Type: `object[]` ([Function](function.md))

*   cannot be null

*   defined in: [Node](node-properties-nodefunctions.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/functions")

### functions Type

`object[]` ([Function](function.md))

## fileResources

节点所需的文件资源定义或者引用

`fileResources`

*   is optional

*   Type: `object[]` ([FileResource](fileresource.md))

*   cannot be null

*   defined in: [Node](node-properties-nodefileresources.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/fileResources")

### fileResources Type

`object[]` ([FileResource](fileresource.md))

## runtimeResource

节点的运行时资源定义或引用

`runtimeResource`

*   is optional

*   Type: `object` ([RuntimeResource](runtimeresource.md))

*   cannot be null

*   defined in: [Node](runtimeresource.md "https://dataworks.data.aliyun.com/schemas/1.1.0/runtimeResource.schema.json#/properties/runtimeResource")

### runtimeResource Type

`object` ([RuntimeResource](runtimeresource.md))

## recurrence

定义周期调度节点的调度状态

`recurrence`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Node](node-properties-recurrence.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/recurrence")

### recurrence Type

`string`

### recurrence Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value      | Explanation                               |
| :--------- | :---------------------------------------- |
| `"Normal"` | 节点会被正常按照指定调度周期调度运行                        |
| `"Skip"`   | 节点会被正常按照指定调度周期实例化，但是代码不会被运行生效，任务状态直接设置为成功 |
| `"Pause"`  | 节点会被正产按照指定调度周期实例化，但是状态直接设置为失败             |

## priority

定义节点的优先级, 数值越大优先级越高

`priority`

*   is optional

*   Type: `integer`

*   cannot be null

*   defined in: [Node](node-properties-priority.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/priority")

### priority Type

`integer`

## timeout

定义节点的超时时间, 运行超过指定时间后, 节点将会被kill掉, 单位为秒

`timeout`

*   is optional

*   Type: `integer`

*   cannot be null

*   defined in: [Node](node-properties-timeout.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/timeout")

### timeout Type

`integer`

## instanceMode

节点的实例化模式

`instanceMode`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Node](node-properties-instancemode.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/instanceMode")

### instanceMode Type

`string`

### instanceMode Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value           | Explanation       |
| :-------------- | :---------------- |
| `"T+1"`         | 节点的配置修改将会在T+1天后生效 |
| `"Immediately"` | 节点配置修改将立即生效       |

## rerunMode

节点重试策略

`rerunMode`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Node](node-properties-rerunmode.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/rerunMode")

### rerunMode Type

`string`

### rerunMode Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value              | Explanation |
| :----------------- | :---------- |
| `"Allowed"`        | 任何情况下都允许重跑  |
| `"Denied"`         | 任何情况下都不允许重跑 |
| `"FailureAllowed"` | 失败的情况下允许重跑  |
