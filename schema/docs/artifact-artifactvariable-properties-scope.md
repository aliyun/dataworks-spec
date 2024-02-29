# Untitled string in Artifact Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable/properties/scope
```

变量作用域

| Abstract            | Extensible | Status         | Identifiable            | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                      |
| :------------------ | :--------- | :------------- | :---------------------- | :---------------- | :-------------------- | :------------------ | :------------------------------------------------------------------------------ |
| Can be instantiated | No         | Unknown status | Unknown identifiability | Forbidden         | Allowed               | none                | [artifact.schema.json\*](../../out/artifact.schema.json "open original schema") |

## scope Type

`string`

## scope Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value             | Explanation             |
| :---------------- | :---------------------- |
| `"NodeParameter"` | 只有节点内使用的参数              |
| `"NodeContext"`   | 节点的上下文中的参数，可以被下游节点使用    |
| `"Workflow"`      | 工作流级别的参数，可以被工作流所有节点使用   |
| `"Workspace"`     | 工作空间级别的参数，可以被工作空间所有节点使用 |
| `"Tenant"`        | 租户级别参数，可以被租户的所有节点使用     |
