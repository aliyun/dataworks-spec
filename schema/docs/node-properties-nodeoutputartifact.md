# Node.OutputArtifact Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/outputs
```

输出定义了工作流节点的产出信息

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                              |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [node.schema.json\*](../../out/node.schema.json "open original schema") |

## outputs Type

`object` ([Node.OutputArtifact](node-properties-nodeoutputartifact.md))

# outputs Properties

| Property                    | Type    | Required | Nullable       | Defined by                                                                                                                                                                                           |
| :-------------------------- | :------ | :------- | :------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [tables](#tables)           | `array` | Optional | cannot be null | [Node](node-properties-nodeoutputartifact-properties-nodeoutputartifacttables.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/outputs/properties/tables")           |
| [variables](#variables)     | `array` | Optional | cannot be null | [Node](node-properties-nodeoutputartifact-properties-nodeoutputartifactvariables.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/outputs/properties/variables")     |
| [nodeOutputs](#nodeoutputs) | `array` | Optional | cannot be null | [Node](node-properties-nodeoutputartifact-properties-nodeoutputartifactnodeoutputs.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/outputs/properties/nodeOutputs") |

## tables

节点使用的上游产出表列表

`tables`

*   is optional

*   Type: `object[]` ([Artifact](artifact.md))

*   cannot be null

*   defined in: [Node](node-properties-nodeoutputartifact-properties-nodeoutputartifacttables.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/outputs/properties/tables")

### tables Type

`object[]` ([Artifact](artifact.md))

## variables

节点依赖的变量Variable列表

`variables`

*   is optional

*   Type: `object[]` ([Artifact](artifact.md))

*   cannot be null

*   defined in: [Node](node-properties-nodeoutputartifact-properties-nodeoutputartifactvariables.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/outputs/properties/variables")

### variables Type

`object[]` ([Artifact](artifact.md))

## nodeOutputs

节点使用的上游节点的预定义输出列表

`nodeOutputs`

*   is optional

*   Type: `object[]` ([Artifact](artifact.md))

*   cannot be null

*   defined in: [Node](node-properties-nodeoutputartifact-properties-nodeoutputartifactnodeoutputs.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/outputs/properties/nodeOutputs")

### nodeOutputs Type

`object[]` ([Artifact](artifact.md))
