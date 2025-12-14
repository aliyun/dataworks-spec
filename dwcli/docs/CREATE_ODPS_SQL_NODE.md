# 如何创建一个 ODPS SQL 节点

## 快速开始

### 1️⃣ 创建节点

```bash
dwcli node create ./tasks --name daily_sales_report --template odps-sql-daily --owner data_team
```

**命令说明**:
- `./tasks` - 父目录（节点将创建在 `./tasks/daily_sales_report/`）
- `--name daily_sales_report` - 节点名称（必需）
- `--template odps-sql-daily` - 使用 ODPS SQL 模板
- `--owner data_team` - 所有者（可选，默认 admin）

**执行结果**:
```
✓ Created object directory: tasks/daily_sales_report
  Node directory: daily_sales_report/
  Spec file: daily_sales_report.schedule.json
  Code file: daily_sales_report.sql
```

**生成的目录结构**:
```
tasks/
└── daily_sales_report/
    ├── daily_sales_report.schedule.json  # 配置文件
    └── daily_sales_report.sql            # SQL 代码文件
```

### 2️⃣ 查看生成的配置

```bash
cat tasks/daily_sales_report/daily_sales_report.schedule.json
```

**默认配置**:
```json
{
  "version": "1.0",
  "kind": "CycleWorkflow",
  "metadata": {
    "name": "daily_sales_report",
    "owner": "data_team"
  },
  "spec": {
    "nodes": [
      {
        "id": "daily_sales_report",
        "name": "daily_sales_report",
        "type": "ODPS_SQL",
        "timeout": 3600,
        "script": {
          "path": "daily_sales_report.sql"
        }
      }
    ],
    "flow": []
  }
}
```

### 3️⃣ 编写 SQL 代码

**方式一：直接编辑文件**
```bash
vim tasks/daily_sales_report/daily_sales_report.sql
```

**方式二：使用配置的编辑器**
```bash
dwcli node code edit ./tasks/daily_sales_report
```

**方式三：从文件设置**
```bash
# 先创建 SQL 文件
cat > my_query.sql << 'EOF'
-- 日销售汇总报表
INSERT OVERWRITE TABLE dw.sales_daily_summary 
PARTITION (dt = '${bizdate}')
SELECT 
    sale_date,
    COUNT(DISTINCT order_id) AS order_count,
    SUM(amount) AS total_amount
FROM ods.orders
WHERE dt = '${bizdate}'
GROUP BY sale_date;
EOF

# 设置到节点
dwcli node code set ./tasks/daily_sales_report --file my_query.sql
```

**方式四：通过管道**
```bash
echo "SELECT COUNT(*) FROM table;" | dwcli node code set ./tasks/daily_sales_report
```

### 4️⃣ 配置节点参数

**设置超时时间**
```bash
dwcli node set ./tasks/daily_sales_report 'spec.nodes[0].timeout=7200' --type int
```

**添加描述**
```bash
dwcli node set ./tasks/daily_sales_report 'metadata.description=每日销售汇总报表'
```

**设置调度周期**
```bash
dwcli node set ./tasks/daily_sales_report 'spec.nodes[0].recurrence=@daily'
```

**设置依赖**
```bash
dwcli node set ./tasks/daily_sales_report 'spec.flow[0].nodeId=upstream_task'
```

### 5️⃣ 查看配置

**查看完整配置**
```bash
dwcli node inspect ./tasks/daily_sales_report --output json
```

**查看特定字段**
```bash
# 查看超时设置
dwcli node inspect ./tasks/daily_sales_report 'spec.nodes[0].timeout'

# 查看所有者
dwcli node inspect ./tasks/daily_sales_report 'metadata.owner'
```

**查看 SQL 内容**
```bash
dwcli node code get ./tasks/daily_sales_report
```

## 完整示例

### 场景：创建每日销售汇总任务

```bash
# 1. 创建节点
dwcli node create ./tasks --name daily_sales_report \
  --template odps-sql-daily \
  --owner data_team

# 2. 准备 SQL
cat > sales_summary.sql << 'EOF'
-- 日销售汇总报表
-- 统计每日销售额和订单数

INSERT OVERWRITE TABLE dw.sales_daily_summary 
PARTITION (dt = '${bizdate}')
SELECT 
    sale_date,
    COUNT(DISTINCT order_id) AS order_count,
    COUNT(DISTINCT user_id) AS user_count,
    SUM(amount) AS total_amount,
    AVG(amount) AS avg_amount,
    MAX(amount) AS max_amount,
    MIN(amount) AS min_amount
FROM ods.orders
WHERE dt = '${bizdate}'
  AND status = 'PAID'
GROUP BY sale_date;
EOF

# 3. 设置 SQL
dwcli node code set ./tasks/daily_sales_report --file sales_summary.sql

# 4. 配置参数
dwcli node set ./tasks/daily_sales_report \
  'spec.nodes[0].timeout=7200' --type int

dwcli node set ./tasks/daily_sales_report \
  'metadata.description=每日销售汇总报表，统计订单和用户数据'

# 5. 验证配置
dwcli node inspect ./tasks/daily_sales_report --output json

# 6. 查看最终结果
echo "=== Spec 配置 ==="
cat tasks/daily_sales_report/daily_sales_report.schedule.json

echo -e "\n=== SQL 代码 ==="
cat tasks/daily_sales_report/daily_sales_report.sql
```

## 高级用法

### 批量创建多个 ODPS SQL 节点

```bash
#!/bin/bash
# 批量创建脚本

TASKS=(
  "sales_daily:每日销售统计"
  "user_daily:每日用户统计"
  "order_daily:每日订单统计"
)

for task_info in "${TASKS[@]}"; do
  task_name="${task_info%%:*}"
  description="${task_info##*:}"
  
  echo "创建任务: $task_name"
  
  # 创建节点
  dwcli node create ./tasks --name "$task_name" \
    --template odps-sql-daily \
    --owner data_team
  
  # 设置描述
  dwcli node set "./tasks/$task_name" \
    "metadata.description=$description"
  
  # 设置超时
  dwcli node set "./tasks/$task_name" \
    'spec.nodes[0].timeout=7200' --type int
  
  echo "✓ $task_name 创建完成"
  echo
done
```

### 从模板快速生成

**创建自定义模板** (`~/.config/dwcli/templates/my-odps-sql.template.json`):
```json
{
  "spec": {
    "version": "1.0",
    "kind": "CycleWorkflow",
    "metadata": {
      "name": "{{ name }}",
      "owner": "{{ owner | default('data_team') }}"
    },
    "spec": {
      "nodes": [{
        "id": "{{ name }}",
        "name": "{{ name }}",
        "type": "ODPS_SQL",
        "timeout": 7200,
        "recurrence": "@daily",
        "script": {"path": "{{ name }}.sql"}
      }],
      "flow": []
    }
  },
  "code": {
    "extension": "sql",
    "content": "-- {{ name }}\n-- TODO: Add your ODPS SQL here\n\nSELECT '${bizdate}' as dt;\n"
  }
}
```

**使用自定义模板**:
```bash
dwcli node create ./tasks --name my_etl_task --template my-odps-sql
```

## 常见问题

**Q: 如何查看可用模板？**
```bash
python3 -c "from dwcli.pkg.template.manager import TemplateManager; print(TemplateManager.list_templates())"
```

**Q: 如何修改已创建的节点？**
```bash
# 使用 set 修改配置
dwcli node set ./tasks/daily_sales_report 'spec.nodes[0].timeout=10800' --type int

# 使用 code set 修改代码
dwcli node code set ./tasks/daily_sales_report --file new_query.sql
```

**Q: 如何删除某个配置字段？**
```bash
dwcli node unset ./tasks/daily_sales_report 'spec.nodes[0].recurrence'
```

**Q: 如何预览修改（不实际应用）？**
```bash
dwcli node set ./tasks/daily_sales_report 'spec.nodes[0].timeout=9999' --dry-run
```

## 总结

创建 ODPS SQL 节点的基本步骤：

1. **创建**: `dwcli node create ./tasks --name <名称> --template odps-sql-daily`
2. **编写 SQL**: `dwcli node code set <路径> --file <sql文件>`
3. **配置参数**: `dwcli node set <路径> <字段>=<值>`
4. **验证**: `dwcli node inspect <路径>`

**目录结构**:
```
tasks/
└── <节点名>/
    ├── <节点名>.schedule.json   # 配置文件
    └── <节点名>.sql             # SQL 代码
```
