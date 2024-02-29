# FileResource Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/fileResource.schema.json
```

定义了文件资源, 如: jar, python, text file, archive files等

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                            |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :------------------------------------------------------------------------------------ |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [fileResource.schema.json](../../out/fileResource.schema.json "open original schema") |

## FileResource Type

`object` ([FileResource](fileresource.md))

# FileResource Properties

| Property          | Type     | Required | Nullable       | Defined by                                                                                                                                  |
| :---------------- | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------------------ |
| [id](#id)         | `string` | Required | cannot be null | [FileResource](fileresource-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/fileResource.schema.json#/properties/id")     |
| [name](#name)     | `string` | Required | cannot be null | [FileResource](fileresource-properties-name.md "https://dataworks.data.aliyun.com/schemas/1.1.0/fileResource.schema.json#/properties/name") |
| [script](#script) | `object` | Required | cannot be null | [FileResource](script.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/script")                           |

## id

唯一标识

`id`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [FileResource](fileresource-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/fileResource.schema.json#/properties/id")

### id Type

`string`

## name

函数名称

`name`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [FileResource](fileresource-properties-name.md "https://dataworks.data.aliyun.com/schemas/1.1.0/fileResource.schema.json#/properties/name")

### name Type

`string`

## script

节点所需的脚本定义或者引用

`script`

*   is required

*   Type: `object` ([Script](script.md))

*   cannot be null

*   defined in: [FileResource](script.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/script")

### script Type

`object` ([Script](script.md))
