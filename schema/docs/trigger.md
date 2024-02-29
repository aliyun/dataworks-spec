# Trigger Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json
```

定义了工作流的触发器

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                  |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :-------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [trigger.schema.json](../../out/trigger.schema.json "open original schema") |

## Trigger Type

`object` ([Trigger](trigger.md))

# Trigger Properties

| Property                | Type     | Required | Nullable       | Defined by                                                                                                                             |
| :---------------------- | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| [id](#id)               | `string` | Required | cannot be null | [Trigger](trigger-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/id")               |
| [type](#type)           | `string` | Required | cannot be null | [Trigger](trigger-properties-type.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/type")           |
| [cron](#cron)           | `string` | Optional | cannot be null | [Trigger](trigger-properties-cron.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/cron")           |
| [startTime](#starttime) | `string` | Optional | cannot be null | [Trigger](trigger-properties-starttime.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/startTime") |
| [endTime](#endtime)     | `string` | Optional | cannot be null | [Trigger](trigger-properties-endtime.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/endTime")     |
| [timezone](#timezone)   | `string` | Optional | cannot be null | [Trigger](trigger-properties-timezone.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/timezone")   |

## id

唯一标识

`id`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Trigger](trigger-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/id")

### id Type

`string`

## type

触发器类型

`type`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Trigger](trigger-properties-type.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/type")

### type Type

`string`

### type Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value         | Explanation |
| :------------ | :---------- |
| `"Scheduler"` |             |
| `"Manual"`    |             |

## cron

周期调度触发器的定时表达式

`cron`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Trigger](trigger-properties-cron.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/cron")

### cron Type

`string`

## startTime

周期调度的起始生效时间，节点只在startTime到endTime时间段内执行周期调度运行

`startTime`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Trigger](trigger-properties-starttime.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/startTime")

### startTime Type

`string`

## endTime

周期调度的结束生效时间，节点只在startTime到endTime时间段内执行周期调度运行

`endTime`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Trigger](trigger-properties-endtime.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/endTime")

### endTime Type

`string`

## timezone

周期调度时间的时区

`timezone`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Trigger](trigger-properties-timezone.md "https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json#/properties/timezone")

### timezone Type

`string`
