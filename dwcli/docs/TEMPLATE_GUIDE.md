# dwcli 模板指南

## 模板位置

dwcli 支持两种类型的模板：

### 1. 内置模板 (Built-in Templates)

位置：**代码内嵌** - `dwcli/pkg/template/manager.py`

内置模板列表：
- `odps-sql-daily` - ODPS SQL 调度任务
- `shell-daily` - Shell 脚本调度任务
- `python-daily` - Python 脚本调度任务
- `manual-workflow` - 手动触发工作流

这些模板随 dwcli 安装，无需额外配置。

### 2. 用户自定义模板 (User Templates)

位置：`~/.config/dwcli/templates/`

用户可以在此目录下添加自定义模板文件。

## 查看可用模板

```bash
# 通过 Python API
python3 -c "from dwcli.pkg.template.manager import TemplateManager; print(TemplateManager.list_templates())"

# 或通过创建命令时的错误提示
dwcli node create ./test --name test --template invalid-template
```

## 自定义模板格式

模板文件命名：`<template-name>.template.json`

模板文件结构：
```json
{
  "spec": {
    "version": "1.0",
    "kind": "CycleWorkflow|ManualWorkflow",
    "metadata": {
      "name": "{{ name }}",
      "owner": "{{ owner | default('admin') }}"
    },
    "spec": {
      "nodes": [
        {
          "id": "{{ name }}",
          "name": "{{ name }}",
          "type": "NODE_TYPE",
          "timeout": 3600,
          "script": {
            "path": "{{ name }}.ext"
          }
        }
      ],
      "flow": []
    }
  },
  "code": {
    "extension": "ext",
    "content": "# Code template for {{ name }}\n"
  }
}
```

### 模板变量

支持的 Jinja2 变量：
- `{{ name }}` - 节点名称 (必需)
- `{{ owner }}` - 所有者 (可选，默认 'admin')
- Jinja2 过滤器：`default()`, `title()`, `replace()`, 等

## 示例：创建自定义模板

### 示例1：Spark 任务模板

文件：`~/.config/dwcli/templates/spark-daily.template.json`

```json
{
  "spec": {
    "version": "1.0",
    "kind": "CycleWorkflow",
    "metadata": {
      "name": "{{ name }}",
      "owner": "{{ owner | default('admin') }}",
      "description": "Spark job for {{ name }}"
    },
    "spec": {
      "nodes": [
        {
          "id": "{{ name }}",
          "name": "{{ name }}",
          "type": "SPARK",
          "timeout": 7200,
          "script": {
            "path": "{{ name }}.py"
          },
          "runtime": {
            "engine": "spark",
            "memory": "4096",
            "cores": "2"
          }
        }
      ],
      "flow": []
    }
  },
  "code": {
    "extension": "py",
    "content": "#!/usr/bin/env python3\n# Spark Job for {{ name }}\n\nfrom pyspark.sql import SparkSession\n\ndef main():\n    spark = SparkSession.builder \\\n        .appName(\"{{ name }}\") \\\n        .getOrCreate()\n    \n    # Your Spark code here\n    \n    spark.stop()\n\nif __name__ == \"__main__\":\n    main()\n"
  }
}
```

使用：
```bash
dwcli node create ./jobs --name my_spark_job --template spark-daily
```

### 示例2：Flink 流式任务模板

文件：`~/.config/dwcli/templates/flink-streaming.template.json`

```json
{
  "spec": {
    "version": "1.0",
    "kind": "CycleWorkflow",
    "metadata": {
      "name": "{{ name }}",
      "owner": "{{ owner | default('admin') }}"
    },
    "spec": {
      "nodes": [
        {
          "id": "{{ name }}",
          "name": "{{ name }}",
          "type": "FLINK",
          "timeout": 0,
          "script": {
            "path": "{{ name }}.jar"
          },
          "runtime": {
            "engine": "flink",
            "parallelism": "4"
          }
        }
      ],
      "flow": []
    }
  },
  "code": {
    "extension": "java",
    "content": "// Flink Job for {{ name }}\n\nimport org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;\n\npublic class FlinkJob {\n    public static void main(String[] args) throws Exception {\n        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();\n        \n        // Your Flink code here\n        \n        env.execute(\"{{ name }}\");\n    }\n}\n"
  }
}
```

使用：
```bash
dwcli node create ./streams --name realtime_analytics --template flink-streaming
```

## 模板开发最佳实践

1. **命名规范**
   - 使用描述性名称：`<engine>-<schedule-type>`
   - 例如：`spark-daily`, `flink-streaming`, `hive-hourly`

2. **必需字段**
   - `spec`: 完整的 FlowSpec 结构
   - `code.extension`: 代码文件扩展名
   - `code.content`: 代码模板内容

3. **变量使用**
   - 始终使用 `{{ name }}` 作为主要标识符
   - 为 `{{ owner }}` 提供默认值
   - 在代码模板中添加注释和 TODO 标记

4. **节点类型**
   - 使用正确的 `type` 字段（ODPS_SQL, SHELL, PYTHON, SPARK, FLINK等）
   - 设置合理的默认 `timeout` 值
   - 包含必要的 `runtime` 配置

5. **测试模板**
   ```bash
   # 创建测试节点
   dwcli node create ./test --name test_node --template your-template
   
   # 验证生成的文件
   cat test/test_node/test_node.schedule.json
   cat test/test_node/test_node.ext
   ```

## 模板目录结构

```
~/.config/dwcli/
├── templates/                     # 用户模板目录
│   ├── spark-daily.template.json
│   ├── flink-streaming.template.json
│   ├── hive-hourly.template.json
│   └── custom-workflow.template.json
└── schemas/                       # Schema 验证规则
    └── *.schema.json
```

## 常见问题

**Q: 如何修改内置模板？**

A: 内置模板在代码中定义，不建议直接修改。推荐方式：
1. 基于内置模板创建用户模板
2. 在 `~/.config/dwcli/templates/` 下创建同名模板文件
3. 用户模板会覆盖同名的内置模板

**Q: 模板不生效怎么办？**

A: 检查以下项：
1. 文件命名正确：`*.template.json`
2. JSON 格式有效
3. 文件位置：`~/.config/dwcli/templates/`
4. 重新加载：重启 Python 进程或重新导入模块

**Q: 如何共享模板？**

A: 
1. 导出模板文件
2. 分发给团队成员
3. 成员将文件放入 `~/.config/dwcli/templates/`
4. 或使用 Git 仓库管理团队模板

## 模板示例库

已提供的用户模板示例：
- `spark-daily.template.json` - Spark 批处理任务
- `flink-streaming.template.json` - Flink 流式任务

更多模板可在 `docs/template-examples/` 查看。
