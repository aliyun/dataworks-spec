# Flow\.Metedata Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/metadata
```

定义工作流的扩展元信息

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                              |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [flow.schema.json\*](../../out/flow.schema.json "open original schema") |

## metadata Type

`object` ([Flow.Metedata](flow-properties-flowmetedata.md))

# metadata Properties

| Property                    | Type     | Required | Nullable       | Defined by                                                                                                                                                                    |
| :-------------------------- | :------- | :------- | :------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [owner](#owner)             | `string` | Optional | cannot be null | [Flow](flow-properties-flowmetedata-properties-owner.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/metadata/properties/owner")             |
| [description](#description) | `string` | Optional | cannot be null | [Flow](flow-properties-flowmetedata-properties-description.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/metadata/properties/description") |

## owner

责任人

`owner`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Flow](flow-properties-flowmetedata-properties-owner.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/metadata/properties/owner")

### owner Type

`string`

## description

字段说明

`description`

*   is optional

*   Type: `string`

*   cannot be null

*   defined in: [Flow](flow-properties-flowmetedata-properties-description.md "https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/metadata/properties/description")

### description Type

`string`
