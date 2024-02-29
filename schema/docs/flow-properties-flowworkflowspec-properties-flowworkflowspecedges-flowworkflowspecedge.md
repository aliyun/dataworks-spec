# Flow\.WorkflowSpec.Edge Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items
```



| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                              |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [flow.schema.json\*](../../out/flow.schema.json "open original schema") |

## items Type

`object` ([Flow.WorkflowSpec.Edge](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge.md))

# items Properties

| Property            | Type     | Required | Nullable       | Defined by                                                                                                                                                                                                                                                                |
| :------------------ | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| [nodeId](#nodeid)   | `string` | Required | cannot be null | [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge-properties-nodeid.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items/properties/nodeId")                           |
| [depends](#depends) | `array`  | Required | cannot be null | [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge-properties-flowworkflowspecedgenodedepends.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items/properties/depends") |

## nodeId

节点的唯一标识符，全局唯一，用于标识工作流中的节点

`nodeId`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge-properties-nodeid.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items/properties/nodeId")

### nodeId Type

`string`

## depends

节点的唯一标识符，全局唯一，用于标识工作流中的节点

`depends`

*   is required

*   Type: `object[]` ([Flow.WorkflowSpec.Edge.NodeDepend](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge-properties-flowworkflowspecedgenodedepends-flowworkflowspecedgenodedepend.md))

*   cannot be null

*   defined in: [Flow](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge-properties-flowworkflowspecedgenodedepends.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items/properties/depends")

### depends Type

`object[]` ([Flow.WorkflowSpec.Edge.NodeDepend](flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge-properties-flowworkflowspecedgenodedepends-flowworkflowspecedgenodedepend.md))
