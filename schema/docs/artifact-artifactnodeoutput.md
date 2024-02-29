# Artifact.NodeOutput Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/nodeOutput
```

节点使用的上游节点的预定义输出

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                      |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :------------------------------------------------------------------------------ |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [artifact.schema.json\*](../../out/artifact.schema.json "open original schema") |

## nodeOutput Type

`object` ([Artifact.NodeOutput](artifact-artifactnodeoutput.md))

# nodeOutput Properties

| Property          | Type     | Required | Nullable       | Defined by                                                                                                                                                        |
| :---------------- | :------- | :------- | :------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [output](#output) | `string` | Required | cannot be null | [Artifact](artifact-artifactnodeoutput-properties-output.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/nodeOutput/properties/output") |

## output

节点调度输出标识

`output`

*   is required

*   Type: `string` ([Output](artifact-artifactnodeoutput-properties-output.md))

*   cannot be null

*   defined in: [Artifact](artifact-artifactnodeoutput-properties-output.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/nodeOutput/properties/output")

### output Type

`string` ([Output](artifact-artifactnodeoutput-properties-output.md))
