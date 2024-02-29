# Untitled string in Artifact Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/properties/artifactType
```



| Abstract            | Extensible | Status         | Identifiable            | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                      |
| :------------------ | :--------- | :------------- | :---------------------- | :---------------- | :-------------------- | :------------------ | :------------------------------------------------------------------------------ |
| Can be instantiated | No         | Unknown status | Unknown identifiability | Forbidden         | Allowed               | none                | [artifact.schema.json\*](../../out/artifact.schema.json "open original schema") |

## artifactType Type

`string`

## artifactType Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value          | Explanation |
| :------------- | :---------- |
| `"Table"`      | 表           |
| `"Variable"`   | 变量          |
| `"NodeOutput"` | 节点输出        |
