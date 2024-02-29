# Untitled string in Node Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/recurrence
```

定义周期调度节点的调度状态

| Abstract            | Extensible | Status         | Identifiable            | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                              |
| :------------------ | :--------- | :------------- | :---------------------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | Unknown identifiability | Forbidden         | Allowed               | none                | [node.schema.json\*](../../out/node.schema.json "open original schema") |

## recurrence Type

`string`

## recurrence Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value      | Explanation                               |
| :--------- | :---------------------------------------- |
| `"Normal"` | 节点会被正常按照指定调度周期调度运行                        |
| `"Skip"`   | 节点会被正常按照指定调度周期实例化，但是代码不会被运行生效，任务状态直接设置为成功 |
| `"Pause"`  | 节点会被正产按照指定调度周期实例化，但是状态直接设置为失败             |
