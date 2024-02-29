# Node.InputArtifact Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/inputs
```

节点的输入

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                              |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [node.schema.json\*](../../out/node.schema.json "open original schema") |

## inputs Type

`object` ([Node.InputArtifact](node-properties-nodeinputartifact.md))

# inputs Properties

| Property                    | Type    | Required | Nullable       | Defined by                                                                                                                                                                                        |
| :-------------------------- | :------ | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| [tables](#tables)           | `array` | Optional | cannot be null | [Node](node-properties-nodeinputartifact-properties-nodeinputartifacttables.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/inputs/properties/tables")           |
| [variables](#variables)     | `array` | Optional | cannot be null | [Node](node-properties-nodeinputartifact-properties-nodeinputartifactvariables.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/inputs/properties/variables")     |
| [nodeOutputs](#nodeoutputs) | `array` | Optional | cannot be null | [Node](node-properties-nodeinputartifact-properties-nodeinputartifactnodeoutputs.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/inputs/properties/nodeOutputs") |

## tables

节点使用的上游产出表列表

`tables`

*   is optional

*   Type: `object[]` ([Artifact](artifact.md))

*   cannot be null

*   defined in: [Node](node-properties-nodeinputartifact-properties-nodeinputartifacttables.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/inputs/properties/tables")

### tables Type

`object[]` ([Artifact](artifact.md))

## variables

节点依赖的变量Variable列表

`variables`

*   is optional

*   Type: `object[]` ([Artifact](artifact.md))

*   cannot be null

*   defined in: [Node](node-properties-nodeinputartifact-properties-nodeinputartifactvariables.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/inputs/properties/variables")

### variables Type

`object[]` ([Artifact](artifact.md))

## nodeOutputs

节点使用的上游节点的预定义输出列表

`nodeOutputs`

*   is optional

*   Type: `object[]` ([Artifact](artifact.md))

*   cannot be null

*   defined in: [Node](node-properties-nodeinputartifact-properties-nodeinputartifactnodeoutputs.md "https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/inputs/properties/nodeOutputs")

### nodeOutputs Type

`object[]` ([Artifact](artifact.md))
