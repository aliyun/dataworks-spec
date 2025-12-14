# ~/.config/dwcli/schemas/ 配置示例

本目录包含 JSON Schema 文件，用于验证 FlowSpec 配置的正确性。

## 目录结构

```
~/.config/dwcli/
└── schemas/
    ├── CycleWorkflow.schedule.schema.json      # 周期调度工作流
    ├── ManualWorkflow.schedule.schema.json     # 手动触发工作流
    ├── CustomWorkflow.schedule.schema.json     # 自定义工作流示例
    └── README.md                                # 本说明文件
```

## Schema 文件示例

### 1. CycleWorkflow.schedule.schema.json

**用途**: 验证周期调度工作流（带定时触发器）

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "title": "CycleWorkflow Schema",
  "description": "Schema for DataWorks CycleWorkflow specification",
  "required": ["version", "kind", "spec"],
  "properties": {
    "version": {
      "type": "string",
      "description": "Spec version",
      "pattern": "^\\d+\\.\\d+(\\.\\d+)?$",
      "examples": ["1.0", "1.1.0"]
    },
    "kind": {
      "type": "string",
      "description": "Workflow type",
      "enum": ["CycleWorkflow"]
    },
    "metadata": {
      "type": "object",
      "properties": {
        "name": {
          "type": "string",
          "description": "Workflow name",
          "minLength": 1,
          "maxLength": 128
        },
        "owner": {
          "type": "string",
          "description": "Workflow owner"
        },
        "description": {
          "type": "string",
          "description": "Workflow description",
          "maxLength": 1024
        }
      }
    },
    "spec": {
      "type": "object",
      "required": ["nodes"],
      "properties": {
        "nodes": {
          "type": "array",
          "description": "List of workflow nodes",
          "minItems": 1,
          "items": {
            "type": "object",
            "required": ["id", "name", "type"],
            "properties": {
              "id": {
                "type": "string",
                "description": "Node ID",
                "minLength": 1
              },
              "name": {
                "type": "string",
                "description": "Node name",
                "minLength": 1
              },
              "type": {
                "type": "string",
                "description": "Node type",
                "enum": [
                  "ODPS_SQL",
                  "SHELL",
                  "PYTHON",
                  "SPARK",
                  "FLINK",
                  "EMR_HIVE",
                  "EMR_SPARK",
                  "VIRTUAL"
                ]
              },
              "timeout": {
                "type": "integer",
                "description": "Timeout in seconds",
                "minimum": 0,
                "maximum": 86400
              },
              "recurrence": {
                "type": "string",
                "description": "Recurrence pattern",
                "pattern": "^@(daily|hourly|weekly|monthly)$"
              },
              "script": {
                "type": "object",
                "required": ["path"],
                "properties": {
                  "path": {
                    "type": "string",
                    "description": "Script file path",
                    "minLength": 1
                  },
                  "runtime": {
                    "type": "object",
                    "description": "Runtime configuration"
                  }
                }
              },
              "runtime": {
                "type": "object",
                "description": "Node runtime settings",
                "properties": {
                  "engine": {
                    "type": "string",
                    "enum": ["spark", "flink", "hive"]
                  },
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
        },
        "flow": {
          "type": "array",
          "description": "Node dependencies",
          "items": {
            "type": "object",
            "required": ["nodeId"],
            "properties": {
              "nodeId": {
                "type": "string",
                "description": "Target node ID"
              },
              "depends": {
                "type": "array",
                "description": "Dependency node IDs",
                "items": {
                  "type": "string"
                }
              }
            }
          }
        },
        "trigger": {
          "type": "object",
          "description": "Workflow trigger configuration",
          "properties": {
            "type": {
              "type": "string",
              "enum": ["Scheduler", "Manual"]
            },
            "cron": {
              "type": "string",
              "description": "Cron expression",
              "pattern": "^\\d{2} \\d{2} \\d{2} \\* \\* \\?$"
            }
          }
        }
      }
    }
  }
}
```

### 2. ManualWorkflow.schedule.schema.json

**用途**: 验证手动触发工作流（无定时触发器）

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "title": "ManualWorkflow Schema",
  "description": "Schema for DataWorks ManualWorkflow specification",
  "required": ["version", "kind", "spec"],
  "properties": {
    "version": {
      "type": "string",
      "description": "Spec version",
      "pattern": "^\\d+\\.\\d+(\\.\\d+)?$"
    },
    "kind": {
      "type": "string",
      "description": "Workflow type",
      "enum": ["ManualWorkflow"]
    },
    "metadata": {
      "type": "object",
      "properties": {
        "name": {
          "type": "string",
          "description": "Workflow name"
        },
        "owner": {
          "type": "string",
          "description": "Workflow owner"
        },
        "description": {
          "type": "string",
          "description": "Workflow description"
        }
      }
    },
    "spec": {
      "type": "object",
      "required": ["nodes"],
      "properties": {
        "nodes": {
          "type": "array",
          "description": "List of workflow nodes",
          "items": {
            "type": "object",
            "required": ["id", "name", "type"],
            "properties": {
              "id": {
                "type": "string",
                "description": "Node ID"
              },
              "name": {
                "type": "string",
                "description": "Node name"
              },
              "type": {
                "type": "string",
                "description": "Node type",
                "enum": [
                  "ODPS_SQL",
                  "SHELL",
                  "PYTHON",
                  "SPARK",
                  "FLINK",
                  "EMR_HIVE",
                  "EMR_SPARK",
                  "VIRTUAL"
                ]
              },
              "script": {
                "type": "object",
                "required": ["path"],
                "properties": {
                  "path": {
                    "type": "string",
                    "description": "Script file path"
                  }
                }
              }
            }
          }
        },
        "flow": {
          "type": "array",
          "description": "Node dependencies",
          "items": {
            "type": "object",
            "properties": {
              "nodeId": {
                "type": "string"
              },
              "depends": {
                "type": "array",
                "items": {
                  "type": "string"
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

### 3. 自定义示例：严格的 ODPS SQL Schema

**文件**: `ODPSSQLStrict.schedule.schema.json`

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "title": "Strict ODPS SQL Workflow Schema",
  "description": "Strict validation for ODPS SQL workflows",
  "required": ["version", "kind", "metadata", "spec"],
  "properties": {
    "version": {
      "type": "string",
      "const": "1.0"
    },
    "kind": {
      "type": "string",
      "const": "CycleWorkflow"
    },
    "metadata": {
      "type": "object",
      "required": ["name", "owner"],
      "properties": {
        "name": {
          "type": "string",
          "pattern": "^[a-zA-Z][a-zA-Z0-9_]{0,127}$"
        },
        "owner": {
          "type": "string",
          "minLength": 1
        }
      }
    },
    "spec": {
      "type": "object",
      "required": ["nodes"],
      "properties": {
        "nodes": {
          "type": "array",
          "minItems": 1,
          "maxItems": 1,
          "items": {
            "type": "object",
            "required": ["id", "name", "type", "timeout", "script"],
            "properties": {
              "type": {
                "const": "ODPS_SQL"
              },
              "timeout": {
                "type": "integer",
                "minimum": 60,
                "maximum": 7200
              },
              "script": {
                "type": "object",
                "required": ["path"],
                "properties": {
                  "path": {
                    "type": "string",
                    "pattern": "\\.sql$"
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

### 4. 自定义示例：Spark Job Schema

**文件**: `SparkJob.schedule.schema.json`

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "title": "Spark Job Schema",
  "required": ["version", "kind", "spec"],
  "properties": {
    "version": {
      "type": "string"
    },
    "kind": {
      "type": "string",
      "enum": ["CycleWorkflow", "ManualWorkflow"]
    },
    "spec": {
      "type": "object",
      "required": ["nodes"],
      "properties": {
        "nodes": {
          "type": "array",
          "items": {
            "type": "object",
            "required": ["type", "runtime"],
            "properties": {
              "type": {
                "const": "SPARK"
              },
              "runtime": {
                "type": "object",
                "required": ["engine", "memory", "cores"],
                "properties": {
                  "engine": {
                    "const": "spark"
                  },
                  "memory": {
                    "type": "string",
                    "pattern": "^(1024|2048|4096|8192|16384)$",
                    "description": "Memory in MB, must be: 1024, 2048, 4096, 8192, or 16384"
                  },
                  "cores": {
                    "type": "string",
                    "pattern": "^([1-8]|16)$",
                    "description": "CPU cores, must be: 1-8 or 16"
                  },
                  "sparkVersion": {
                    "type": "string",
                    "enum": ["2.4", "3.0", "3.1", "3.2"]
                  }
                }
              },
              "script": {
                "type": "object",
                "required": ["path"],
                "properties": {
                  "path": {
                    "type": "string",
                    "pattern": "\\.(py|jar)$"
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

## 使用说明

### 1. 安装 Schema

将上述 JSON 文件保存到 `~/.config/dwcli/schemas/` 目录：

```bash
# 创建目录
mkdir -p ~/.config/dwcli/schemas

# 复制 schema 文件
cp CycleWorkflow.schedule.schema.json ~/.config/dwcli/schemas/
cp ManualWorkflow.schedule.schema.json ~/.config/dwcli/schemas/
```

### 2. 验证效果

```bash
# 创建节点
dwcli node create ./tasks --name my_task --template odps-sql-daily

# 尝试设置无效值（会被 schema 阻止）
dwcli node set ./tasks/my_task 'spec.nodes[0].timeout=-100' --type int

# 输出错误：
# ❌ VALIDATION FAILED
# Error: timeout must be >= 0
```

### 3. 自定义验证规则

根据团队需求修改 schema：

```json
{
  "timeout": {
    "type": "integer",
    "minimum": 300,      // 最少5分钟
    "maximum": 3600      // 最多1小时
  }
}
```

## Schema 验证规则说明

### 常用约束

| 约束类型 | 说明 | 示例 |
|---------|------|------|
| `type` | 数据类型 | `"type": "string"` |
| `enum` | 枚举值 | `"enum": ["A", "B"]` |
| `pattern` | 正则表达式 | `"pattern": "^\\d+$"` |
| `minimum` | 最小值 | `"minimum": 0` |
| `maximum` | 最大值 | `"maximum": 100` |
| `minLength` | 最小长度 | `"minLength": 1` |
| `maxLength` | 最大长度 | `"maxLength": 128` |
| `required` | 必需字段 | `"required": ["id"]` |
| `const` | 常量值 | `"const": "fixed"` |

### 示例约束组合

**限制节点名称格式**:
```json
{
  "name": {
    "type": "string",
    "pattern": "^[a-zA-Z][a-zA-Z0-9_]{0,63}$",
    "description": "Must start with letter, alphanumeric + underscore, max 64 chars"
  }
}
```

**限制 timeout 范围**:
```json
{
  "timeout": {
    "type": "integer",
    "minimum": 60,
    "maximum": 86400,
    "multipleOf": 60,
    "description": "Timeout in seconds, 1min-24hrs, multiple of 60"
  }
}
```

**限制节点类型**:
```json
{
  "type": {
    "enum": ["ODPS_SQL", "SHELL", "PYTHON"],
    "description": "Only SQL, Shell, and Python are allowed"
  }
}
```

## 测试 Schema

```bash
# 1. 创建测试节点
dwcli node create ./test --name test_node --template odps-sql-daily

# 2. 测试有效修改
dwcli node set ./test/test_node 'spec.nodes[0].timeout=3600' --type int
# ✓ 成功

# 3. 测试超出范围
dwcli node set ./test/test_node 'spec.nodes[0].timeout=999999' --type int
# ✗ 失败：超过最大值

# 4. 测试类型错误
dwcli node set ./test/test_node 'spec.nodes[0].timeout=abc'
# ✗ 失败：类型不匹配

# 5. 完整验证
dwcli node validate ./test/test_node
```

## 参考资源

- [JSON Schema 官方文档](https://json-schema.org/)
- [JSON Schema 验证工具](https://www.jsonschemavalidator.net/)
- [DataWorks FlowSpec 规范](../schema/)
