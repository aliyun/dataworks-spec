# Untitled string in Artifact Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable/properties/type
```

变量类型

| Abstract            | Extensible | Status         | Identifiable            | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                      |
| :------------------ | :--------- | :------------- | :---------------------- | :---------------- | :-------------------- | :------------------ | :------------------------------------------------------------------------------ |
| Can be instantiated | No         | Unknown status | Unknown identifiability | Forbidden         | Allowed               | none                | [artifact.schema.json\*](../../out/artifact.schema.json "open original schema") |

## type Type

`string`

## type Constraints

**enum**: the value of this property must be equal to one of the following values:

| Value        | Explanation           |
| :----------- | :-------------------- |
| `"System"`   | 系统变量，例如: $\[yyyymmdd] |
| `"Constant"` | 常量                    |
