# Untitled string in Flow Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/kind
```

工作流类型

| Abstract            | Extensible | Status         | Identifiable            | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                              |
| :------------------ | :--------- | :------------- | :---------------------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | Unknown identifiability | Forbidden         | Allowed               | none                | [flow.schema.json\*](../../out/flow.schema.json "open original schema") |

## kind Type

`string`

## kind Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value              | Explanation |
| :----------------- | :---------- |
| `"CycleWorkflow"`  | 周期调度工作流     |
| `"ManualWorkflow"` | 手动触发工作流     |
