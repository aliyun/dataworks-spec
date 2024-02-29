# Untitled string in Artifact Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable/properties/value
```

变量值

| Abstract            | Extensible | Status         | Identifiable            | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                      |
| :------------------ | :--------- | :------------- | :---------------------- | :---------------- | :-------------------- | :------------------ | :------------------------------------------------------------------------------ |
| Can be instantiated | No         | Unknown status | Unknown identifiability | Forbidden         | Allowed               | none                | [artifact.schema.json\*](../../out/artifact.schema.json "open original schema") |

## value Type

`string`

## value Examples

```json
"$[yyyymmdd]"
```

```json
"$[yyyymmdd-1]"
```

```json
"$[hh24-1/24]"
```

```json
"$[hh24miss-1/24/60]"
```

```json
"$[add_months(yyyymm,-1)]"
```

```json
"$[add_months(yyyymmdd,-12)]"
```
