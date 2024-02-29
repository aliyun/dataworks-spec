# RuntimeResource Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/runtimeResource.schema.json
```

节点的运行时资源定义或引用

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                                  |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :------------------------------------------------------------------------------------------ |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [runtimeResource.schema.json](../../out/runtimeResource.schema.json "open original schema") |

## RuntimeResource Type

`object` ([RuntimeResource](runtimeresource.md))

# RuntimeResource Properties

| Property                        | Type     | Required | Nullable       | Defined by                                                                                                                                                             |
| :------------------------------ | :------- | :------- | :------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [id](#id)                       | `string` | Required | cannot be null | [RuntimeResource](runtimeresource-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/runtimeResource.schema.json#/properties/id")                       |
| [resourceGroup](#resourcegroup) | `string` | Required | cannot be null | [RuntimeResource](runtimeresource-properties-resourcegroup.md "https://dataworks.data.aliyun.com/schemas/1.1.0/runtimeResource.schema.json#/properties/resourceGroup") |

## id

唯一标识符

`id`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [RuntimeResource](runtimeresource-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/runtimeResource.schema.json#/properties/id")

### id Type

`string`

## resourceGroup

资源组唯一标识

`resourceGroup`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [RuntimeResource](runtimeresource-properties-resourcegroup.md "https://dataworks.data.aliyun.com/schemas/1.1.0/runtimeResource.schema.json#/properties/resourceGroup")

### resourceGroup Type

`string`
