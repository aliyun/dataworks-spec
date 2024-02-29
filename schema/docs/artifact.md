# Artifact Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json
```

Artifact 的类型可以是 NodeOutput、Table、Variable，都可以作为工作流节点的输入或者输出使用。

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                    |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [artifact.schema.json](../../out/artifact.schema.json "open original schema") |

## Artifact Type

`object` ([Artifact](artifact.md))

one (and only one) of

*   [Artifact.Table](artifact-artifacttable.md "check type definition")

*   [Artifact.Variable](artifact-artifactvariable.md "check type definition")

*   [Artifact.NodeOutput](artifact-artifactnodeoutput.md "check type definition")

# Artifact Properties

| Property                      | Type     | Required | Nullable       | Defined by                                                                                                                                      |
| :---------------------------- | :------- | :------- | :------------- | :---------------------------------------------------------------------------------------------------------------------------------------------- |
| [artifactType](#artifacttype) | `string` | Required | cannot be null | [Artifact](artifact-properties-artifacttype.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/properties/artifactType") |

## artifactType



`artifactType`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Artifact](artifact-properties-artifacttype.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/properties/artifactType")

### artifactType Type

`string`

### artifactType Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value          | Explanation |
| :------------- | :---------- |
| `"Table"`      | 表           |
| `"Variable"`   | 变量          |
| `"NodeOutput"` | 节点输出        |
