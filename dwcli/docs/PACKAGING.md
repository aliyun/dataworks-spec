# dwcli 打包说明文档

## 📦 打包内容分析

### 问题：schemas 和 templates 会被打包吗？

**回答**：

#### ✅ templates/ - 会被打包

**位置**: `dwcli/templates/*.template.json`

**打包配置**:
```python
# setup.py
package_data={
    "dwcli": ["templates/*.template.json"],
}
```

```
# MANIFEST.in
include dwcli/templates/*.template.json
```

**验证**:
```bash
$ tar -tzf dist/dwcli-1.0.0.tar.gz | grep templates
dwcli-1.0.0/dwcli/templates/manual-workflow.template.json
dwcli-1.0.0/dwcli/templates/odps-sql-daily.template.json
dwcli-1.0.0/dwcli/templates/python-daily.template.json
dwcli-1.0.0/dwcli/templates/shell-daily.template.json
```

✅ **内置模板会随 pip install 自动安装**

---

#### ❌ schemas/ - 不应该被打包（设计决策）

**位置**: `~/.config/dwcli/schemas/*.schema.json`

**设计理由**:

1. **用户可配置性** 
   - Schema 是验证规则，不同团队/项目可能有不同要求
   - 应该由用户自定义，而不是强制内置

2. **灵活性**
   - 用户可以根据需要添加/修改 schema
   - 不需要重装 dwcli 就能更新验证规则

3. **可选性**
   - 没有 schema 时，工具仍然能正常工作
   - Schema 验证是可选功能，不是必需的

**当前实现**: 已提供示例文件到 `~/.config/dwcli/schemas/`，但**不打包进 wheel/egg**

---

## 📂 目录结构对比

### 打包后的结构（pip install 后）

```
site-packages/
└── dwcli/                              # 安装的包
    ├── __init__.py
    ├── cli.py
    ├── pkg/
    │   ├── domain/
    │   ├── jsonpath/
    │   ├── schema/
    │   └── template/
    └── templates/                      # ✅ 打包进来
        ├── odps-sql-daily.template.json
        ├── shell-daily.template.json
        ├── python-daily.template.json
        └── manual-workflow.template.json
```

### 用户配置目录（手动创建）

```
~/.config/dwcli/
├── templates/                          # 用户自定义模板（可选）
│   ├── spark-daily.template.json
│   └── flink-streaming.template.json
└── schemas/                            # 用户配置 Schema（可选）
    ├── CycleWorkflow.schedule.schema.json
    ├── ManualWorkflow.schedule.schema.json
    └── README.md
```

---

## 🔄 加载优先级

### Templates 加载顺序

```python
# TemplateManager._load_builtin_templates()
1. 加载内置模板: site-packages/dwcli/templates/*.template.json
2. 加载用户模板: ~/.config/dwcli/templates/*.template.json
3. 优先级: 用户模板 > 内置模板
```

### Schemas 加载顺序

```python
# SchemaValidator.__init__()
1. 只加载用户 schema: ~/.config/dwcli/schemas/*.schema.json
2. 如果目录不存在，跳过验证（工具仍可用）
```

---

## 🎯 设计理念

### 内置 vs 用户配置

| 类型 | 内置（打包） | 用户配置（~/.config） |
|------|-------------|---------------------|
| **Templates** | ✅ 基础模板 | ✅ 扩展模板 |
| **Schemas** | ❌ 不打包 | ✅ 完全由用户定义 |

**原因**:

1. **Templates** - 工具的核心功能
   - 用户期望 `dwcli node create` 立即可用
   - 内置常用模板（ODPS SQL, Shell, Python）
   - 用户可扩展自定义模板

2. **Schemas** - 可选的验证功能
   - 不同项目有不同验证需求
   - 强制内置会限制灵活性
   - 提供示例但不强制使用

---

## 💡 推荐使用方式

### 首次使用

```bash
# 1. 安装 dwcli
pip install dwcli

# 2. 立即可用（使用内置模板）
dwcli node create ./tasks --name my_task --template odps-sql-daily
# ✅ 工作！使用内置模板

# 3. （可选）配置 Schema 验证
mkdir -p ~/.config/dwcli/schemas
cat > ~/.config/dwcli/schemas/CycleWorkflow.schedule.schema.json << 'EOF'
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  ...
}
EOF
```

### 高级使用

```bash
# 添加自定义模板
mkdir -p ~/.config/dwcli/templates
cat > ~/.config/dwcli/templates/my-spark.template.json << 'EOF'
{
  "spec": {...},
  "code": {...}
}
EOF

# 使用自定义模板
dwcli node create ./jobs --name spark_job --template my-spark
```

---

## 🛠️ 如果要打包 schemas（不推荐）

如果确实需要打包默认 schema：

**setup.py**:
```python
package_data={
    "dwcli": [
        "templates/*.template.json",
        "schemas/*.schema.json",      # 添加
        "schemas/README.md",           # 添加
    ],
}
```

**MANIFEST.in**:
```
include dwcli/schemas/*.schema.json
include dwcli/schemas/README.md
```

**SchemaValidator 改动**:
```python
def __init__(self, schema_dir: Optional[Path] = None):
    if schema_dir is None:
        # 先尝试用户目录
        user_dir = Path.home() / ".config" / "dwcli" / "schemas"
        if user_dir.exists():
            schema_dir = user_dir
        else:
            # 回退到内置目录
            schema_dir = Path(__file__).parent.parent / "schemas"
    ...
```

**但这样做的问题**:
- ❌ 用户修改 schema 需要找到 site-packages 目录
- ❌ 更新 dwcli 会覆盖用户的 schema 修改
- ❌ 不符合配置文件放 ~/.config 的惯例

---

## ✅ 总结

| 目录 | 打包到 wheel | 位置 | 用途 |
|------|-------------|------|------|
| **dwcli/templates/** | ✅ 是 | site-packages/dwcli/templates/ | 内置模板 |
| **~/.config/dwcli/templates/** | ❌ 否 | 用户目录 | 用户自定义模板 |
| **dwcli/schemas/** | ❌ 否 | 不打包 | （已移除） |
| **~/.config/dwcli/schemas/** | ❌ 否 | 用户目录 | 用户配置验证规则 |

**最佳实践**:
- ✅ 内置模板打包 → 开箱即用
- ✅ Schemas 用户配置 → 灵活可定制
- ✅ 两者都支持用户扩展 → 满足高级需求

**当前实现完全符合设计理念！** 🎯
