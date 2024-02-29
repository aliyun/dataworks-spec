# Untitled string in Node Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/instanceMode
```

节点的实例化模式

| Abstract            | Extensible | Status         | Identifiable            | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                              |
| :------------------ | :--------- | :------------- | :---------------------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | Unknown identifiability | Forbidden         | Allowed               | none                | [node.schema.json\*](../../out/node.schema.json "open original schema") |

## instanceMode Type

`string`

## instanceMode Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value           | Explanation       |
| :-------------- | :---------------- |
| `"T+1"`         | 节点的配置修改将会在T+1天后生效 |
| `"Immediately"` | 节点配置修改将立即生效       |
