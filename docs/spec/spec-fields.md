# version

说明：spec版本号
类型：字符串
取值：目前版本: 1.0.0

```json
{
  "version": "1.0.0",
  "kind": "CycleWorkflow"
}
```

# kind

说明：工作流类型
类型：字符串
取值：

* CycleWorkflow（周期调度工作流）

```json
{
  "version": "1.0.0",
  "kind": "CycleWorkflow"
}
```

* ManualWorkflow（手动调度工作流）

```json
{
  "version": "1.0.0",
  "kind": "ManualWorkflow"
}
```

# metadata

说明：元信息
类型：Map<String, String>
取值：
举例：

* owner
* description

```json
{
  "version": "1.0.0",
  "kind": "CycleWorkflow",
  "metadata": {
    "owner": "12312312",
    "description": "desc"
  }
}
```

# variables

说明：spec内部变量声明
类型：List，每个元素为一个变量声明定义
举例：
变量声明

```json
{
  "version": "1.0.0",
  "kind": "CycleWorkflow",
  "variables": [
    {
      "id": "global_var_0",
      "name": "global_var_0",
      "scope": "Workspace",
      "valueType": "Constant",
      "value": "global_value"
    },
    {
      "id": "bizdate",
      "name": "bizdate",
      "scope": "Parameter",
      "valueType": "System",
      "value": "${yyyymmdd}"
    },
    {
      "id": "hour",
      "name": "hour",
      "scope": "Parameter",
      "valueType": "System",
      "value": "${hi24mmss}"
    },
    {
      "id": "region",
      "name": "region",
      "scope": "Parameter",
      "valueType": "Constant",
      "value": "cn-shanghai"
    },
    {
      "id": "ctx_output_1",
      "name": "output",
      "scope": "Context",
      "node": "node_existed_xx"
    }
  ]
} 
```

# scripts

说明：脚本文件声明
类型：List，每个元素是一个脚本文件声明
举例：声明脚本文件

```json
{
  "version": "1.0.0",
  "kind": "CycleWorkflow",
  "scripts": [
    {
      "id": "script_file1",
      "path": "/path/to/file1.sql",
      "language": "sql",
      "runtime": {
        "engine": "MaxCompute",
        "command": "ODPS_SQL"
      },
      "parameters": [
        {
          "name": "bizdate",
          "value": "${yyyymmdd}"
        }
      ]
    },
    {
      "id": "script_file2",
      "path": "/path/to/file2.py",
      "runtime": {
        "engine": "MaxCompute",
        "command": "PY_ODPS"
      },
      "language": "python"
    }
  ]
}
```

## path

说明：文件系统路径
类型：字符串
取值：文件系统中的路径

## language

说明：脚本语言
类型：字符串
取值：常见语言类型，比如python，sql，odps_sql，hive_sql，shell等

## runtime

说明：运行时信息
类型：Object
举例：

```json
{
  "runtime": {
    "engine": "MaxCompute",
    "command": "ODPS_SQL"
  }
}
```

### engine

说明：运行时引擎类型
类型：字符串
举例：MaxCompute，EMR，Hologres等

### command

说明：运行时引擎上执行的命令，或者执行命令的代号
类型：字符串
举例：节点类型名：ODPS_SCRIPT，ODPS_SQL，EMR_HIVE，HologresSQL等

## parameters

说明：脚本参数
类型：字符串
举例：脚本接受的参数列表，这里使用spec内部variable的id变量值引用，具体定义方式参见：[variables](#variables)

# triggers

说明：调度触发器配置
类型：List，每个元素为一个trigger的定义
举例：

```json
{
  "triggers": [
    {
      "id": "daily",
      "type": "SchedulerTrigger",
      "cron": "00 00 00 * * ?",
      "startTime": "2023-01-01T00:00:00",
      "endTime": "2024-01-01T00:00:00",
      "timezone": "Asia/Shanghai"
    }
  ]
}
```

## type

说明：触发器类型
类型：字符串
取值:

* SchedulerTrigger：周期调度触发
* ManualTrigger：手动触发

## cron

说明：定时表达式
类型：字符串
举例：00 00 00 * * ？

## startTime

说明：周期调度生效时间
类型：字符串
举例：2023-06-01 00:00:00

## endTime

说明：周期调度失效时间
类型：字符串
举例：2024-06-01 00:00:00

# nodes

说明：调度节点声明
类型：List，每个元素为一个节点的定义
举例：声明节点

```json
{
  "version": "1.0.0",
  "kind": "CycleNode",
  "nodes": [
    {
      "id": "node_existed_xx",
      "type": "Ref",
      "reference": "autotest.table_1"
    },
    {
      "id": "node_1",
      "script": "{{script_file1}}",
      "inputs": [
        "{{artifact.table1}}",
        "{{artifact.table2}}",
        "{{variable.ctx_output_1}}"
      ],
      "outputs": [
        "{{artifact.table3}}",
        "{{variable.my_output}}"
      ],
      "trigger": "{{trigger.daily}}",
      "resource": "{{resource.resgroup_1}}"
    },
    {
      "id": "node_2",
      "script": "{{script.script_file2}}",
      "trigger": "{{trigger.daily}}",
      "resource": "{{resource.runtime_resgroup_1}}"
    }
  ]
}
```

## script

节点代码脚本，参见: script

## functions

说明：节点引用的udf列表
类型：List
取值：function

## fileResources

说明：节点引用的资源文件列表
类型：List
取值：fileResource

## inputs

说明: 节点输入列表
类型：List
取值：可以是：artifact，variable

## outputs

说明: 节点产出列表
类型：List
取值：可以是：artifact，variable

## runtimeResource

节点的运行时资源定义（资源组）

## recurrence

说明：调度类型
类型：字符串
取值：

* NORMAL：正常调度
* PAUSE：暂停调度
* SKIP：空跑调度

## priority

说明：调度节点优先级
类型：数字
取值：1，2，3，4，5，6，7，8

## timeout

说明：调度超时时间
类型：数字
举例：86400

## instanceMode

说明：转实例模式
类型：字符串
取值：

* T+1：T+1
* Immediately：实时转实例

## rerunMode

说明：重跑模式
类型：字符串
取值：

* ALL_ALLOWED：允许重跑
* ALL_DENIED：不允许重跑
* FAILURE_ALLOWED：失败时允许重跑

# artifacts

说明：产出物定义，产出物可以是表，调度的输出串，或者一个上下文[variable](#variables)
类型：List，每个元素为一个artifact定义
举例：

```json
{
  "version": "1.0.0",
  "kind": "CycleWorkflow",
  "artifacts": [
    {
      "id": "table1",
      "type": "table",
      "guid": "odps.autotest.table_1"
    },
    {
      "id": "table2",
      "type": "table",
      "guid": "odps.autotest.table_2"
    },
    {
      "id": "table3",
      "type": "table",
      "guid": "odps.autotest.table_3"
    },
    {
      "id": "artifact_1",
      "type": "output",
      "str": "autotest.output_1",
      "refTableName": "autotest.table_1"
    }
  ]
}
```

## type

说明：artifact类型
类型：字符串
取值：

* table：输出表
* output：调度节点输出

## guid

说明：表的guid

## data

说明：调度节点输出标识

## refTableName

说明：调度节点输出的关联表名

# runtimeResources

说明：运行时资源
类型：List，每个元素定义一个运行时资源
举例：

```json
{
  "runtimeResources": [
    {
      "id": "resgroup_1",
      "resourceGroup": "S_xxx_yyy"
    }
  ]
}
```

## resourceGroup

说明：调度资源组identifier
类型：字符串
举例：S_xxx_xxx

# fileResources

说明：文件资源定义
类型：List，每个元素是一个文件资源定义
举例：

```json5
{
  "version": "1.0.0",
  "kind": "CycleWorkflow",
  "scripts": [
    {
      "id": "file1",
      "path": "/path/to/file2.py",
      "runtime": {
        "engine": "MaxCompute",
        "command": "PYODPS"
      },
      "language": "python"
    },
    ...
  ],
  "fileResources": [
    {
      "id": "res_1",
      "name": "res_1.py",
      "script": "{{file1}}"
    }
  ],
  "functions": [
    {
      "id": "func_1",
      "name": "geo_loc",
      "fileResources": [
        "{{res_1}}"
      ]
    }
  ],
  "nodes": [
    {
      "id": "node_1",
      "fileResources": [
        "{{res_1}}"
      ],
      "script": "{{shell1}}"
    },
    {
      "id": "node_2",
      "functions": [
        "{{func_1}}"
      ]
    }
  ]
}
```

## name

说明：资源文件名
类型：字符串

## script

说明：资源文件引用id变量
类型：字符串

# functions

说明：UDF函数定义
类型：List，每个元素为一个函数定义
举例：

```json
{
  "version": "1.0.0",
  "kind": "CycleWorkflow",
  "functions": [
    {
      "id": "func_1",
      "name": "geo_loc",
      "fileResources": [
        "{{res_1}}"
      ]
    }
  ]
}
```

## name

说明：函数名
类型：字符串

## fileResources

说明：函数引用的资源列表
类型：List，每个元素为引用的资源内部id

# flows

说明：定义node之间的依赖关系
类型：List，每个元素为一个依赖关系的定义
举例：

```json
{
  "flow": [
    {
      "nodeId": "{{node_1}}", #通过本地Id变量引用节点
      "depends": [
        {
          "nodeId": "{{node_existed_xx}}",
          # 普通依赖
          "type": "Normal"
        }
      ]
    },
    {
      "nodeId": "{{node_2}}",
      "depends": [
        {
          "nodeId": "{{node_existed_xx}}", # 跨周期依赖指定节点
          "type": "CrossCycleDependsOnOtherNode" # 跨周期依赖指定节点
        },
        {
          "type": "CrossCycleDependsOnSelf" # 跨周期自依赖
        },
        {
          "type": "CrossCycleDependsOnChildren" # 跨周期依赖一层子节点
        }
      ]
    }
  ]
}
```