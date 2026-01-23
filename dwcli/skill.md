# dwcli 使用教程

## 概述

dwcli 是 DataWorks CLI 工具，用于管理 FlowSpec 规范的配置即代码（Configuration-as-Code）工具。它提供了创建、验证和管理 DataWorks 工作流节点的能力。

## 安装

### 从源码安装

```bash
# 克隆仓库
git clone https://github.com/aliyun/dataworks-spec.git
cd dataworks-spec/dwcli

# 构建并安装
python3 setup.py sdist bdist_wheel
pip3 install dist/dwcli-1.0.0-py3-none-any.whl --force-reinstall
```

### 验证安装

```bash
dwcli --version
# 输出: dwcli, version 1.0.0
```

## 基本概念

- **Node**: 工作流节点，如 ODPS_SQL、SHELL、PYTHON 等
- **Workflow**: 工作流，包含多个节点和依赖关系
- **CycleWorkflow**: 周期调度工作流
- **ManualWorkflow**: 手动触发工作流
- **Spec**: 节点或工作流的配置规范文件（JSON格式）

## 命令结构

```bash
dwcli [GLOBAL_OPTIONS] COMMAND [COMMAND_OPTIONS] [ARGS]
```

## 主要命令

### 1. 节点管理 (node)

#### 创建节点

```bash
# 创建 ODPS SQL 节点
dwcli node create <目录路径> --name <节点名称> --template <模板名称> --owner <负责人>

# 示例：创建一个名为 test_sql 的 ODPS SQL 节点
dwcli node create myworkspace/test_sql --name test_sql --template odps-sql-daily --owner admin
```

#### 可用模板

- `odps-sql-daily`: ODPS SQL 节点（每日调度）
- `shell-daily`: Shell 节点（每日调度）
- `python-daily`: Python 节点（每日调度）
- `dide-shell-daily`: DIDE Shell 节点（每日调度）
- `foreach-daily`: ForEach 循环节点
- `manual-workflow`: 手动工作流
- `workspace`: 工作空间

#### 设置节点属性

```bash
# 设置节点属性
dwcli node set <目录路径> <属性路径>=<值> [更多属性...]

# 示例：设置调度时间
dwcli node set myworkspace/test_sql/test_sql "spec.nodes[0].trigger.type=Scheduler" "spec.nodes[0].trigger.cron=0 10 2 * * ?"

# 示例：设置重跑配置
dwcli node set myworkspace/test_sql/test_sql "spec.nodes[0].rerunMode=FailureAllowed" "spec.nodes[0].rerunTimes=3" "spec.nodes[0].rerunInterval=180000"

# 示例：添加依赖
dwcli node set myworkspace/test_sql/test_sql "spec.flow[0].depends[0].type=Normal" "spec.flow[0].depends[0].output=autotest_root"
```

#### 删除节点属性

```bash
# 删除节点属性
dwcli node unset <目录路径> <属性路径> [更多属性...]

# 示例：删除超时设置
dwcli node unset myworkspace/test_sql/test_sql spec.nodes[0].timeout
```

#### 查看节点配置

```bash
# 查看完整配置
dwcli node inspect <目录路径>

# 查看特定路径的值
dwcli node inspect <目录路径> spec.nodes[0].trigger

# 以 JSON 格式输出
dwcli node inspect <目录路径> --output json
```

#### 验证节点配置

```bash
# 验证单个节点
dwcli node validate <目录路径>

# 验证整个工作空间
dwcli workspace validate <工作空间目录>
```

#### 编译节点

```bash
# 编译节点（验证）
dwcli node compile <目录路径>
```

### 2. 代码管理 (code)

#### 编辑代码文件

```bash
# 使用默认编辑器打开代码文件
dwcli node code edit <目录路径>
```

#### 设置代码内容

```bash
# 从文件设置
dwcli node code set <目录路径> --file <代码文件路径>

# 直接设置内容
dwcli node code set <目录路径> --content "代码内容"

# 从标准输入读取
echo "代码内容" | dwcli node code set <目录路径>
```

#### 获取代码内容

```bash
# 查看代码内容
dwcli node code get <目录路径>
```

### 3. 工作空间管理 (workspace)

#### 创建工作空间

```bash
# 创建工作空间
dwcli workspace create <目录路径> --name <工作空间名称> --owner <负责人>

# 示例
dwcli workspace create myproject --name myproject --owner admin
```

#### 验证工作空间

```bash
# 验证整个工作空间
dwcli workspace validate <工作空间目录>
```

## 常用场景

### 1. 创建周期调度的 ODPS SQL 节点

```bash
# 1. 创建节点
dwcli node create myworkspace/daily_sql --name daily_sql --template odps-sql-daily --owner admin

# 2. 修改为周期调度工作流
dwcli node set myworkspace/daily_sql/daily_sql "kind=CycleWorkflow"

# 3. 设置每天02:10运行
dwcli node set myworkspace/daily_sql/daily_sql "spec.nodes[0].trigger.type=Scheduler" "spec.nodes[0].trigger.cron=0 10 2 * * ?"

# 4. 设置重跑配置（最多重试3次，间隔3分钟）
dwcli node set myworkspace/daily_sql/daily_sql "spec.nodes[0].rerunMode=FailureAllowed" "spec.nodes[0].rerunTimes=3" "spec.nodes[0].rerunInterval=180000"

# 5. 添加依赖
dwcli node set myworkspace/daily_sql/daily_sql "spec.flow[0].depends[0].type=Normal" "spec.flow[0].depends[0].output=autotest_root"

# 6. 验证配置
dwcli node validate myworkspace/daily_sql/daily_sql
```

### 2. 创建 ForEach 循环节点

```bash
# 创建 ForEach 节点
dwcli node create myworkspace/foreach_example --name foreach_example --template foreach-daily --owner admin

# 验证配置
dwcli node validate myworkspace/foreach_example/foreach_example
```

### 3. 批量验证工作空间

```bash
# 验证整个工作空间的所有节点
dwcli workspace validate myworkspace
```

## 依赖配置格式

### 新格式（推荐）

```json
{
  "flow": [
    {
      "nodeId": "node_id",
      "depends": [
        {
          "type": "Normal",
          "output": "autotest_root"
        },
        {
          "type": "Normal", 
          "output": "ods.tbitem_usr1"
        }
      ]
    }
  ]
}
```

### 依赖类型说明

- `Normal`: 同周期依赖
- `CrossCycleDependsOnSelf`: 依赖上一周期的自己
- `CrossCycleDependsOnChildren`: 依赖上一周期自己的所有子节点
- `CrossCycleDependsOnOtherNode`: 依赖上一周期的指定节点

## Cron 表达式格式

使用标准 Quartz Cron 格式：`秒 分 时 日 月 星期`

示例：
- `0 10 2 * * ?` - 每天02:10执行
- `0 0 1 * * ?` - 每天01:00执行
- `0 30 18 ? * MON-FRI` - 工作日18:30执行

## 最佳实践

1. **使用有意义的命名**：节点名称应清晰表达其功能
2. **合理设置超时时间**：根据任务实际执行时间设置
3. **配置重试策略**：生产任务建议设置2-3次重试
4. **使用依赖管理**：明确定义节点间的依赖关系
5. **定期验证配置**：使用 validate 命令确保配置正确

## 故障排除

### 常见错误

1. **验证失败**
   - 检查 JSON 格式是否正确
   - 确认必填字段都已填写
   - 验证枚举值是否在允许范围内

2. **路径错误**
   - 确保使用正确的目录路径
   - 检查文件是否存在

3. **权限问题**
   - 确保有读写权限
   - 检查目录是否可访问

### 调试技巧

1. 使用 `--dry-run` 参数预览更改
2. 使用 `inspect` 命令查看当前配置
3. 使用 `validate` 命令验证配置正确性

## 进阶功能

### 使用变量

在节点配置中可以使用变量：
```json
{
  "spec": {
    "variables": [
      {
        "artifactType": "Variable",
        "id": "bizdate",
        "name": "bizdate",
        "scope": "NodeParameter",
        "type": "System",
        "value": "${yyyymmdd}"
      }
    ]
  }
}
```

### 使用资源引用

可以通过引用方式复用配置：
```json
{
  "script": "{{scripts.script_sql_1}}",
  "trigger": "{{triggers.daily_trigger}}",
  "runtimeResource": "{{runtimeResources.resgroup_1}}"
}
```