# dwcli 模板架构说明

## ✅ 当前实现状态

dwcli 已完全实现**双层模板系统**，符合你的需求：

### 1. **内置模板** (源代码中) ✅

**位置**: `dwcli/pkg/template/manager.py` 的 `BUILTIN_TEMPLATES` 字典

**已内置模板**:
- `odps-sql-daily` - ODPS SQL 调度任务
- `shell-daily` - Shell 脚本调度任务  
- `python-daily` - Python 脚本调度任务
- `manual-workflow` - 手动触发工作流

**特点**:
- ✅ 打包在源代码中
- ✅ 随 dwcli 安装自动可用
- ✅ 无需额外配置
- ✅ 覆盖常见场景

### 2. **用户自定义模板** (可选扩展) ✅

**位置**: `~/.config/dwcli/templates/*.template.json`

**加载机制**:
- 自动扫描该目录下的 `*.template.json` 文件
- 用户模板与内置模板合并
- 同名模板时，用户模板优先级更高

**特点**:
- ✅ 支持团队特定需求
- ✅ 无需修改源代码
- ✅ 可覆盖内置模板
- ✅ 即时生效

## 架构验证

```bash
$ python3 -c "from dwcli.pkg.template.manager import TemplateManager; print(TemplateManager.list_templates())"
['odps-sql-daily', 'shell-daily', 'python-daily', 'manual-workflow', 'spark-daily', 'flink-streaming']
#  ↑ 内置模板(4个)                                                     ↑ 用户模板(2个)
```

## 使用示例

### 使用内置模板
```bash
# 直接使用，无需任何配置
dwcli node create ./tasks --name my_sql_task --template odps-sql-daily
dwcli node create ./tasks --name my_shell_task --template shell-daily
```

### 使用自定义模板
```bash
# 1. 创建模板文件
mkdir -p ~/.config/dwcli/templates
cat > ~/.config/dwcli/templates/spark-daily.template.json << 'EOF'
{
  "spec": {
    "version": "1.0",
    "kind": "CycleWorkflow",
    "metadata": {
      "name": "{{ name }}",
      "owner": "{{ owner | default('admin') }}"
    },
    "spec": {
      "nodes": [{
        "id": "{{ name }}",
        "name": "{{ name }}",
        "type": "SPARK",
        "timeout": 7200,
        "script": {"path": "{{ name }}.py"}
      }],
      "flow": []
    }
  },
  "code": {
    "extension": "py",
    "content": "# Spark job for {{ name }}\nprint('Hello')\n"
  }
}
EOF

# 2. 立即使用
dwcli node create ./jobs --name my_spark --template spark-daily
```

## 实现细节

### 模板加载流程

1. **启动时**: `TemplateManager.load_user_templates()` 自动调用
2. **扫描**: 读取 `~/.config/dwcli/templates/*.template.json`
3. **解析**: 验证 JSON 格式并加载到内存
4. **合并**: 用户模板添加到 `_user_templates` 字典
5. **查询**: `get_template()` 优先返回用户模板，其次内置模板

### 优先级规则

```
用户模板 > 内置模板
```

示例：如果用户创建 `~/.config/dwcli/templates/odps-sql-daily.template.json`，
则会覆盖内置的 `odps-sql-daily` 模板。

## 目录结构

```
dwcli/
├── dwcli/pkg/template/manager.py      # 内置模板定义
└── ...

~/.config/dwcli/
├── templates/                          # 用户自定义模板目录
│   ├── spark-daily.template.json      # 用户模板示例1
│   ├── flink-streaming.template.json  # 用户模板示例2
│   └── custom-*.template.json         # 更多自定义模板
└── schemas/                            # Schema 验证规则
    └── *.schema.json
```

## 测试验证

```bash
# 测试内置模板
dwcli node create ./test1 --name builtin --template odps-sql-daily
cat test1/builtin/builtin.sql
# 输出: -- SQL Script for builtin...

# 测试用户模板
dwcli node create ./test2 --name custom --template spark-daily
cat test2/custom/custom.py
# 输出: # Spark job for custom...
```

## 总结

✅ **完全符合需求**:
- 内置模板在源代码中，打包分发
- 支持 `~/.config/dwcli/templates/` 自定义扩展
- 两种模板无缝集成，统一管理

**无需额外操作**，开箱即用！
