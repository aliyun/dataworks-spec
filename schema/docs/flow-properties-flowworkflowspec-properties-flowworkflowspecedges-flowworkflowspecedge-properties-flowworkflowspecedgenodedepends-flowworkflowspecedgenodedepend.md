# Flow\.WorkflowSpec.Edge.NodeDepend Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items/properties/depends/items
```



| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                              |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [flow.schema.json\*](../../out/flow.schema.json "open original schema") |

## items Type

`object` ([Flow.WorkflowSpec.Edge.NodeDepend](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge-properties-flowworkflowspecedgenodedepends-flowworkflowspecedgenodedepend.md))

# items Properties

| Property          | Type     | Required | Nullable       | Defined by                                                                                                                                                                                                                                                                                                                                         |
| :---------------- | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [nodeId](#nodeid) | `string` | Required | cannot be null | [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge-properties-flowworkflowspecedgenodedepends-flowworkflowspecedgenodedepend-properties-nodeid.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items/properties/depends/items/properties/nodeId") |
| [type](#type)     | `string` | Required | cannot be null | [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge-properties-flowworkflowspecedgenodedepends-flowworkflowspecedgenodedepend-properties-type.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items/properties/depends/items/properties/type")     |

## nodeId

节点的唯一标识

`nodeId`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge-properties-flowworkflowspecedgenodedepends-flowworkflowspecedgenodedepend-properties-nodeid.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items/properties/depends/items/properties/nodeId")

### nodeId Type

`string`

## type

依赖类型

`type`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge-properties-flowworkflowspecedgenodedepends-flowworkflowspecedgenodedepend-properties-type.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items/properties/depends/items/properties/type")

### type Type

`string`

### type Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value                            | Explanation                 |
| :------------------------------- | :-------------------------- |
| `"Normal"`                       | 节点依赖的是同周期的对应的节点示例           |
| `"CrossCycleDependsOnSelf"`      | 节点当前周期的实例依赖的是上一周期的自己的实例     |
| `"CrossCycleDependsOnChildren"`  | 节点当前周期的实例依赖的是上一周期自己的所有子节点实例 |
| `"CrossCycleDependsOnOtherNode"` | 节点当前周期的实例依赖的是上一周期的指定节点的实例   |
