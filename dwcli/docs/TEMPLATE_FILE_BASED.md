# dwcli 模板管理架构

## ✅ 已实现：统一文件管理模式

所有模板（内置+用户）均通过 **JSON 模板文件** 方式管理。

## 📁 目录结构

```
dwcli/
├── dwcli/
│   ├── templates/                    # 内置模板目录（随源代码分发）
│   │   ├── odps-sql-daily.template.json
│   │   ├── shell-daily.template.json
│   │   ├── python-daily.template.json
│   │   └── manual-workflow.template.json
│   └── pkg/template/manager.py       # 模板加载器
│
└── setup.py                          # 打包配置（包含模板文件）

~/.config/dwcli/
└── templates/                         # 用户自定义模板目录（可选）
    ├── spark-daily.template.json
    ├── flink-streaming.template.json
    └── *.template.json                # 用户可添加更多
```

## 🔄 加载机制

### 1. 内置模板加载
**位置**: `dwcli/templates/*.template.json`

```python
# manager.py 中的加载逻辑
builtin_dir = Path(__file__).parent.parent.parent / "templates"
# 自动扫描并加载所有 *.template.json 文件
```

**特点**:
- ✅ 随 pip install 自动安装
- ✅ 打包在 wheel/egg 中
- ✅ 无需手动配置

### 2. 用户模板加载
**位置**: `~/.config/dwcli/templates/*.template.json`

```python
# manager.py 中的加载逻辑
user_dir = Path.home() / ".config" / "dwcli" / "templates"
# 自动扫描并加载所有 *.template.json 文件
```

**特点**:
- ✅ 可选扩展
- ✅ 覆盖同名内置模板
- ✅ 无需重启程序

## 📝 模板文件格式

统一格式：`<template-name>.template.json`

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
      "nodes": [{
        "id": "{{ name }}",
        "name": "{{ name }}",
        "type": "ODPS_SQL",
        "timeout": 3600,
        "script": {"path": "{{ name }}.sql"}
      }],
      "flow": []
    }
  },
  "code": {
    "extension": "sql",
    "content": "-- SQL for {{ name }}\nSELECT 1;\n"
  }
}
```

## 🎯 优先级规则

```
用户模板 (~/.config) > 内置模板 (dwcli/templates)
```

同名模板时，用户模板会覆盖内置模板。

## ✨ 优势

### 相比硬编码方式：

1. **易于维护**
   - 修改模板无需改动 Python 代码
   - 模板文件可独立编辑和测试

2. **易于扩展**
   - 添加新模板只需创建 JSON 文件
   - 用户可自定义模板而不修改源码

3. **版本管理友好**
   - 模板文件可单独版本控制
   - 模板变更清晰可见

4. **分发简单**
   - 模板随 package_data 自动打包
   - pip install 后立即可用

## 📦 打包配置

**setup.py**:
```python
package_data={
    "dwcli": ["templates/*.template.json"],
},
include_package_data=True,
```

**MANIFEST.in**:
```
include dwcli/templates/*.template.json
```

## 🧪 验证

```bash
# 列出所有模板
python3 -c "from dwcli.pkg.template.manager import TemplateManager; \
print('内置:', TemplateManager.list_builtin_templates()); \
print('用户:', TemplateManager.list_user_templates())"

# 输出:
# 内置: ['manual-workflow', 'odps-sql-daily', 'python-daily', 'shell-daily']
# 用户: ['flink-streaming', 'spark-daily']
```

```bash
# 使用模板创建任务
dwcli node create ./tasks --name my_task --template odps-sql-daily
dwcli node create ./jobs --name my_spark --template spark-daily
```

## 📊 对比总结

| 特性 | 硬编码方式 | 文件管理方式 |
|------|-----------|-------------|
| 修改模板 | 需改 Python 代码 | ✅ 直接编辑 JSON |
| 添加模板 | 需改代码并重装 | ✅ 创建文件即可 |
| 用户自定义 | 不支持 | ✅ 完全支持 |
| 版本控制 | 混在代码中 | ✅ 独立文件 |
| 打包分发 | 无需额外配置 | ✅ package_data |

## 🎉 总结

✅ **完全实现统一文件管理**:
- 内置模板：`dwcli/templates/*.template.json`
- 用户模板：`~/.config/dwcli/templates/*.template.json`
- 统一格式、统一加载、统一使用

**完全符合需求！** 🚀
