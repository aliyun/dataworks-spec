# JSON Schema 配置指南

## 📁 Schema 文件位置

**位置**: `~/.config/dwcli/schemas/`

Schema 文件用于验证 FlowSpec 配置的正确性。

## 🗂️ 目录结构

```
~/.config/dwcli/
├── schemas/                                    # Schema 定义目录
│   ├── CycleWorkflow.schedule.schema.json     # 周期调度工作流
│   ├── ManualWorkflow.schedule.schema.json    # 手动触发工作流
│   └── *.schedule.schema.json                 # 其他自定义 schema
└── templates/                                  # 模板定义目录
    └── *.template.json
```

## 📋 Schema 命名规则

Schema 文件命名格式：`<Kind>.schedule.schema.json`

- `<Kind>` - 对应 spec JSON 中的 `kind` 字段
- 例如：
  - `CycleWorkflow.schedule.schema.json` → 验证 `"kind": "CycleWorkflow"`
  - `ManualWorkflow.schedule.schema.json` → 验证 `"kind": "ManualWorkflow"`

## ✅ 已提供的 Schema

### 1. CycleWorkflow Schema

**文件**: `~/.config/dwcli/schemas/CycleWorkflow.schedule.schema.json`

**验证规则**:
- 必需字段: `version`, `kind`, `spec`
- `kind` 必须为 `"CycleWorkflow"`
- `spec.nodes` 必须存在且为数组
- 节点类型: `ODPS_SQL`, `SHELL`, `PYTHON`, `SPARK`, `FLINK`, 等
- `timeout` 必须为整数且 ≥ 0

**示例验证错误**:
```bash
dwcli node set ./task 'spec.nodes[0].timeout=invalid'
# 输出:
❌ VALIDATION FAILED: Changes Reverted!
--------------------------------------------------------------------------
#1. Error Type: Type Mismatch
   Error Path: spec.nodes.0.timeout
   Error Reason: 'invalid' is not of type 'integer'
   Solution: Use 'dwcli node set ./task spec.nodes.0.timeout=0 --type int'
--------------------------------------------------------------------------
```

### 2. ManualWorkflow Schema

**文件**: `~/.config/dwcli/schemas/ManualWorkflow.schedule.schema.json`

**验证规则**:
- 类似 CycleWorkflow
- `kind` 必须为 `"ManualWorkflow"`
- 手动工作流特定字段

## 🔧 如何使用

### 自动验证

Schema 验证在以下操作时自动触发：

1. **创建节点时**:
```bash
dwcli node create ./tasks --name my_task --template odps-sql-daily
# 自动验证生成的 spec
```

2. **修改配置时**:
```bash
dwcli node set ./tasks/my_task 'spec.nodes[0].timeout=7200' --type int
# 修改后自动验证，失败则回滚
```

3. **删除字段时**:
```bash
dwcli node unset ./tasks/my_task 'spec.nodes[0].recurrence'
# 删除后自动验证
```

### 手动验证

```bash
dwcli node validate ./tasks/my_task
```

## 📝 创建自定义 Schema

### Schema 文件格式

使用 **JSON Schema Draft 7** 标准：

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "title": "CycleWorkflow Schema",
  "required": ["version", "kind", "spec"],
  "properties": {
    "version": {
      "type": "string",
      "pattern": "^\\d+\\.\\d+(\\.\\d+)?$"
    },
    "kind": {
      "type": "string",
      "enum": ["CycleWorkflow"]
    },
    "spec": {
      "type": "object",
      "required": ["nodes"],
      "properties": {
        "nodes": {
          "type": "array",
          "items": {
            "type": "object",
            "required": ["id", "name", "type"],
            "properties": {
              "timeout": {
                "type": "integer",
                "minimum": 0
              }
            }
          }
        }
      }
    }
  }
}
```

### 示例：创建 Spark 专用 Schema

**文件**: `~/.config/dwcli/schemas/SparkWorkflow.schedule.schema.json`

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": ["version", "kind", "spec"],
  "properties": {
    "kind": {
      "type": "string",
      "enum": ["SparkWorkflow"]
    },
    "spec": {
      "type": "object",
      "required": ["nodes"],
      "properties": {
        "nodes": {
          "type": "array",
          "items": {
            "properties": {
              "type": {
                "enum": ["SPARK"]
              },
              "runtime": {
                "type": "object",
                "required": ["memory", "cores"],
                "properties": {
                  "memory": {
                    "type": "string",
                    "pattern": "^\\d+$"
                  },
                  "cores": {
                    "type": "string",
                    "pattern": "^\\d+$"
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
```

## 🛠️ Schema 开发最佳实践

### 1. 明确必需字段

```json
{
  "required": ["version", "kind", "spec"],
  "properties": {
    "spec": {
      "required": ["nodes"]
    }
  }
}
```

### 2. 使用类型约束

```json
{
  "timeout": {
    "type": "integer",
    "minimum": 0,
    "maximum": 86400
  }
}
```

### 3. 使用枚举限制

```json
{
  "type": {
    "enum": ["ODPS_SQL", "SHELL", "PYTHON", "SPARK"]
  }
}
```

### 4. 使用正则验证

```json
{
  "cron": {
    "type": "string",
    "pattern": "^\\d{2} \\d{2} \\d{2} \\* \\* \\?$"
  }
}
```

## 🔍 验证错误类型

dwcli 会提供以下类型的错误信息：

| 错误类型 | 说明 | 示例 |
|---------|------|------|
| Type Mismatch | 类型不匹配 | timeout 应为整数，实际为字符串 |
| Missing Required Field | 缺少必需字段 | 缺少 spec.nodes |
| Invalid Enum Value | 枚举值无效 | kind 只能是 CycleWorkflow 或 ManualWorkflow |
| Pattern Mismatch | 正则不匹配 | version 格式错误 |
| Range Constraint Violation | 范围约束违反 | timeout 不能为负数 |

## 📚 参考资源

### JSON Schema 文档

- [JSON Schema 官方文档](https://json-schema.org/)
- [Understanding JSON Schema](https://json-schema.org/understanding-json-schema/)

### DataWorks Schema 参考

项目中已有的 schema 定义：
```
dataworks-spec/schema/
├── node.schema.json          # 节点定义
├── flow.schema.json          # 流程定义
├── trigger.schema.json       # 触发器定义
└── ...
```

## 🧪 测试 Schema

### 测试流程

```bash
# 1. 创建测试节点
dwcli node create ./test --name test_node --template odps-sql-daily

# 2. 测试有效值
dwcli node set ./test/test_node 'spec.nodes[0].timeout=3600' --type int
# ✓ 应该成功

# 3. 测试无效值
dwcli node set ./test/test_node 'spec.nodes[0].timeout=abc'
# ✗ 应该失败并显示错误

# 4. 测试类型错误
dwcli node set ./test/test_node 'spec.nodes[0].timeout=-100' --type int
# ✗ 应该失败（负数）

# 5. 验证完整配置
dwcli node validate ./test/test_node
```

## ❓ 常见问题

**Q: Schema 文件没有生效？**

A: 检查：
1. 文件位置：`~/.config/dwcli/schemas/`
2. 文件命名：`<Kind>.schedule.schema.json`
3. JSON 格式有效
4. `kind` 字段匹配

**Q: 如何禁用验证？**

A: 目前验证是自动的，但你可以：
1. 删除或重命名 schema 文件
2. 使用 `--dry-run` 预览而不应用更改

**Q: 如何查看当前加载的 schema？**

```python
from dwcli.pkg.schema.validator import SchemaValidator
validator = SchemaValidator()
print(validator.schemas.keys())
```

**Q: 验证失败后如何恢复？**

A: 不用担心！验证失败时，dwcli 会自动回滚更改，原文件保持不变。

## ✅ 总结

- **位置**: `~/.config/dwcli/schemas/*.schedule.schema.json`
- **命名**: 根据 `kind` 字段命名
- **自动加载**: 无需配置，自动生效
- **自动验证**: set/unset 操作自动触发
- **错误回滚**: 验证失败自动恢复原状态
- **友好提示**: 提供详细错误信息和修复建议

已提供 2 个基础 schema，可根据需要添加更多！
