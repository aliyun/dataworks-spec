# Untitled string in Flow Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items/properties/depends/items/properties/type
```

依赖类型

| Abstract            | Extensible | Status         | Identifiable            | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                              |
| :------------------ | :--------- | :------------- | :---------------------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | Unknown identifiability | Forbidden         | Allowed               | none                | [flow.schema.json\*](../../out/flow.schema.json "open original schema") |

## type Type

`string`

## type Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value                            | Explanation                 |
| :------------------------------- | :-------------------------- |
| `"Normal"`                       | 节点依赖的是同周期的对应的节点示例           |
| `"CrossCycleDependsOnSelf"`      | 节点当前周期的实例依赖的是上一周期的自己的实例     |
| `"CrossCycleDependsOnChildren"`  | 节点当前周期的实例依赖的是上一周期自己的所有子节点实例 |
| `"CrossCycleDependsOnOtherNode"` | 节点当前周期的实例依赖的是上一周期的指定节点的实例   |
