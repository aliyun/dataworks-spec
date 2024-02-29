# Untitled string in Node Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/rerunMode
```

节点重试策略

| Abstract            | Extensible | Status         | Identifiable            | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                              |
| :------------------ | :--------- | :------------- | :---------------------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | Unknown identifiability | Forbidden         | Allowed               | none                | [node.schema.json\*](../../out/node.schema.json "open original schema") |

## rerunMode Type

`string`

## rerunMode Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value              | Explanation |
| :----------------- | :---------- |
| `"Allowed"`        | 任何情况下都允许重跑  |
| `"Denied"`         | 任何情况下都不允许重跑 |
| `"FailureAllowed"` | 失败的情况下允许重跑  |
