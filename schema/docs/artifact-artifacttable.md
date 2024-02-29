# Artifact.Table Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/table
```

节点使用的上游产出表

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                      |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :------------------------------------------------------------------------------ |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [artifact.schema.json\*](../../out/artifact.schema.json "open original schema") |

## table Type

`object` ([Artifact.Table](artifact-artifacttable.md))

# table Properties

| Property      | Type     | Required | Nullable       | Defined by                                                                                                                                          |
| :------------ | :------- | :------- | :------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------- |
| [guid](#guid) | `string` | Required | cannot be null | [Artifact](artifact-artifacttable-properties-guid.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/table/properties/guid") |

## guid

产出表唯一标识符

`guid`

*   is required

*   Type: `string` ([Guid](artifact-artifacttable-properties-guid.md))

*   cannot be null

*   defined in: [Artifact](artifact-artifacttable-properties-guid.md "https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/table/properties/guid")

### guid Type

`string` ([Guid](artifact-artifacttable-properties-guid.md))
