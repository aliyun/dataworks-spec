# Function Schema

```txt
https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json
```

定义工作流节点使用的UDF

| Abstract            | Extensible | Status         | Identifiable | Custom Properties | Additional Properties | Access Restrictions | Defined In                                                                    |
| :------------------ | :--------- | :------------- | :----------- | :---------------- | :-------------------- | :------------------ | :---------------------------------------------------------------------------- |
| Can be instantiated | No         | Unknown status | No           | Forbidden         | Allowed               | none                | [function.schema.json](../../out/function.schema.json "open original schema") |

## Function Type

`object` ([Function](function.md))

# Function Properties

| Property                        | Type     | Required | Nullable       | Defined by                                                                                                                       |
| :------------------------------ | :------- | :------- | :------------- | :------------------------------------------------------------------------------------------------------------------------------- |
| [id](#id)                       | `string` | Required | cannot be null | [Function](function-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/id")        |
| [name](#name)                   | `string` | Required | cannot be null | [Function](function-properties-name.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/name")    |
| [script](#script)               | `object` | Required | cannot be null | [Function](script.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/script")                    |
| [fileResources](#fileresources) | `object` | Optional | cannot be null | [Function](fileresource.md "https://dataworks.data.aliyun.com/schemas/1.1.0/fileResource.schema.json#/properties/fileResources") |

## id

唯一标识

`id`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Function](function-properties-id.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/id")

### id Type

`string`

## name

函数名称

`name`

*   is required

*   Type: `string`

*   cannot be null

*   defined in: [Function](function-properties-name.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/name")

### name Type

`string`

## script

节点所需的脚本定义或者引用

`script`

*   is required

*   Type: `object` ([Script](script.md))

*   cannot be null

*   defined in: [Function](script.md "https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/script")

### script Type

`object` ([Script](script.md))

## fileResources

定义了文件资源, 如: jar, python, text file, archive files等

`fileResources`

*   is optional

*   Type: `object` ([FileResource](fileresource.md))

*   cannot be null

*   defined in: [Function](fileresource.md "https://dataworks.data.aliyun.com/schemas/1.1.0/fileResource.schema.json#/properties/fileResources")

### fileResources Type

`object` ([FileResource](fileresource.md))
