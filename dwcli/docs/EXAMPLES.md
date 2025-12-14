# dwcli Examples

## Basic Usage Examples

### Example 1: Create a Daily SQL Task

```bash
# Create the task (creates ./tasks/daily_sales_report/)
dwcli node create ./tasks \
  --name daily_sales_report \
  --template odps-sql-daily \
  --owner analytics_team

# Configure timeout and retry
dwcli node set ./tasks/daily_sales_report \
  spec.nodes[0].timeout=5400 --type int \
  spec.nodes[0].retry=2 --type int

# Add SQL code
cat > sales_query.sql << 'EOF'
-- Daily Sales Report
SELECT 
    date,
    SUM(amount) as total_sales,
    COUNT(DISTINCT customer_id) as unique_customers
FROM sales_table
WHERE date = '${bizdate}'
GROUP BY date;
EOF

dwcli node code set ./tasks/daily_sales_report --file sales_query.sql

# Validate the task
dwcli node validate ./tasks/daily_sales_report

# Inspect the configuration
dwcli node inspect ./tasks/daily_sales_report
```

### Example 2: Create a Shell Script Task

```bash
# Create shell task
dwcli node create ./data_backup \
  --name daily_backup \
  --template shell-daily

# Configure
dwcli node set ./data_backup \
  spec.nodes[0].timeout=3600 --type int \
  metadata.description="Daily data backup job"

# Add shell script
dwcli node code set ./data_backup --content '#!/bin/bash
set -e
echo "Starting backup..."
tar -czf /backup/data_$(date +%Y%m%d).tar.gz /data
echo "Backup complete"
'

# Validate
dwcli node validate ./data_backup
```

### Example 3: Update Existing Task

```bash
# Inspect current configuration
dwcli node inspect ./my_task spec.nodes[0].timeout

# Update timeout
dwcli node set ./my_task spec.nodes[0].timeout=7200 --type int

# Update owner
dwcli node set ./my_task metadata.owner=new_team

# Add new configuration field
dwcli node set ./my_task spec.nodes[0].priority=high

# Validate changes
dwcli node validate ./my_task
```

## Advanced Examples

### Example 4: Batch Processing Multiple Tasks

```bash
#!/bin/bash
# update_all_timeouts.sh

TASKS=(
  ./task1
  ./task2
  ./task3
  ./task4
)

NEW_TIMEOUT=7200

for task in "${TASKS[@]}"; do
  echo "Updating $task..."
  
  # Update timeout
  dwcli node set "$task" spec.nodes[0].timeout=$NEW_TIMEOUT --type int
  
  # Validate
  if dwcli node validate "$task"; then
    echo "✓ $task updated successfully"
  else
    echo "✗ $task validation failed"
    exit 1
  fi
done

echo "All tasks updated!"
```

### Example 5: Template-Based Task Generation

```bash
#!/bin/bash
# generate_monthly_tasks.sh

MONTHS=(
  "january"
  "february"
  "march"
  "april"
  "may"
  "june"
  "july"
  "august"
  "september"
  "october"
  "november"
  "december"
)

for month in "${MONTHS[@]}"; do
  task_name="monthly_report_${month}"
  task_dir="./${task_name}"
  
  # Create task
  dwcli node create "$task_dir" \
    --name "$task_name" \
    --template odps-sql-daily
  
  # Configure
  dwcli node set "$task_dir" \
    metadata.description="Monthly report for ${month}" \
    spec.nodes[0].timeout=10800 --type int
  
  # Set SQL code
  dwcli node code set "$task_dir" --content "
-- Monthly Report for ${month^}
SELECT 
    '${month}' as month,
    SUM(revenue) as total_revenue,
    COUNT(*) as transaction_count
FROM transactions
WHERE month_name = '${month}';
"
  
  # Validate
  dwcli node validate "$task_dir"
done
```

### Example 6: Configuration Migration

```bash
#!/bin/bash
# migrate_config.sh

SOURCE_TASK="./legacy_task"
TARGET_TASK="./new_task"

# Create new task
dwcli node create "$TARGET_TASK" \
  --name new_task \
  --template odps-sql-daily

# Copy timeout from source
TIMEOUT=$(dwcli node inspect "$SOURCE_TASK" spec.nodes[0].timeout --output raw)
dwcli node set "$TARGET_TASK" spec.nodes[0].timeout="$TIMEOUT" --type int

# Copy owner
OWNER=$(dwcli node inspect "$SOURCE_TASK" metadata.owner --output raw)
dwcli node set "$TARGET_TASK" metadata.owner="$OWNER"

# Copy code
dwcli node code get "$SOURCE_TASK" | dwcli node code set "$TARGET_TASK"

# Validate
dwcli node validate "$TARGET_TASK"

echo "Migration complete!"
```

### Example 7: Complex Configuration Updates

```bash
# Update nested configuration
dwcli node set ./my_task \
  'spec.runtime.resources.memory=4096' --type int \
  'spec.runtime.resources.cpu=2' --type int \
  'spec.runtime.env={"JAVA_OPTS":"-Xmx4g"}' --type json

# Add to array
dwcli node set ./my_task \
  'spec.dependencies[0]=upstream_task_1' \
  'spec.dependencies[1]=upstream_task_2'

# Update multiple nodes in workflow
dwcli node set ./workflow \
  'spec.nodes[0].timeout=3600' --type int \
  'spec.nodes[1].timeout=7200' --type int \
  'spec.nodes[0].retry=3' --type int \
  'spec.nodes[1].retry=2' --type int

# Validate
dwcli node validate ./my_task
```

### Example 8: Conditional Updates

```bash
#!/bin/bash
# conditional_update.sh

TASK_DIR="./my_task"

# Get current timeout
CURRENT_TIMEOUT=$(dwcli node inspect "$TASK_DIR" spec.nodes[0].timeout --output raw)

# Update if less than threshold
if [ "$CURRENT_TIMEOUT" -lt 3600 ]; then
  echo "Timeout too low ($CURRENT_TIMEOUT), updating to 3600..."
  dwcli node set "$TASK_DIR" spec.nodes[0].timeout=3600 --type int
  dwcli node validate "$TASK_DIR"
else
  echo "Timeout is sufficient ($CURRENT_TIMEOUT)"
fi
```

### Example 9: Validation and Reporting

```bash
#!/bin/bash
# validate_all_tasks.sh

echo "Validating all tasks..."
echo "======================="

FAILED_TASKS=()

for task_dir in ./*/; do
  task_name=$(basename "$task_dir")
  
  if dwcli node validate "$task_dir" 2>&1 | grep -q "✓"; then
    echo "✓ $task_name"
  else
    echo "✗ $task_name"
    FAILED_TASKS+=("$task_name")
  fi
done

echo "======================="
echo "Summary:"
echo "Total tasks: $(find ./ -maxdepth 1 -type d | wc -l)"
echo "Failed: ${#FAILED_TASKS[@]}"

if [ ${#FAILED_TASKS[@]} -gt 0 ]; then
  echo ""
  echo "Failed tasks:"
  printf '  - %s\n' "${FAILED_TASKS[@]}"
  exit 1
fi
```

### Example 10: Code Template Replacement

```bash
#!/bin/bash
# replace_code_template.sh

TASK_DIR="./etl_task"
TABLE_NAME="customer_data"
PARTITION_KEY="date"

# Generate SQL from template
SQL_CODE="
-- ETL Task for ${TABLE_NAME}
INSERT OVERWRITE TABLE ${TABLE_NAME}
PARTITION (${PARTITION_KEY} = '\${bizdate}')
SELECT 
    id,
    name,
    email,
    created_at
FROM source_${TABLE_NAME}
WHERE ${PARTITION_KEY} = '\${bizdate}';
"

# Set code
echo "$SQL_CODE" | dwcli node code set "$TASK_DIR"

# Update task name
dwcli node set "$TASK_DIR" \
  metadata.name="etl_${TABLE_NAME}" \
  spec.nodes[0].name="etl_${TABLE_NAME}"

# Validate
dwcli node validate "$TASK_DIR"
```

## CI/CD Integration Examples

### Example 11: Git Pre-commit Hook

```bash
#!/bin/bash
# .git/hooks/pre-commit

echo "Validating DataWorks specs..."

CHANGED_DIRS=$(git diff --cached --name-only | grep '\.schedule\.json$' | xargs -n1 dirname | sort -u)

if [ -z "$CHANGED_DIRS" ]; then
  echo "No spec files changed."
  exit 0
fi

VALIDATION_FAILED=0

for dir in $CHANGED_DIRS; do
  if ! dwcli node validate "$dir"; then
    VALIDATION_FAILED=1
  fi
done

if [ $VALIDATION_FAILED -eq 1 ]; then
  echo "❌ Validation failed. Commit aborted."
  exit 1
fi

echo "✓ All specs validated successfully."
exit 0
```

### Example 12: CI Pipeline Script

```bash
#!/bin/bash
# ci/validate-specs.sh

set -e

echo "Installing dwcli..."
pip install -e dwcli/

echo "Validating all specs..."
find . -name "*.schedule.json" -type f | while read spec_file; do
  dir=$(dirname "$spec_file")
  echo "Validating $dir..."
  dwcli node validate "$dir"
done

echo "✓ All validations passed!"
```

## Debugging Examples

### Example 13: Dry-Run Testing

```bash
# Test changes without applying
dwcli node set ./my_task \
  spec.nodes[0].timeout=9999 --type int \
  --dry-run

# Review the output, then apply if correct
dwcli node set ./my_task \
  spec.nodes[0].timeout=9999 --type int
```

### Example 14: Inspect and Debug

```bash
# Show full spec
dwcli node inspect ./my_task --output json | jq .

# Check specific fields
dwcli node inspect ./my_task spec.nodes[0].timeout
dwcli node inspect ./my_task metadata.owner

# Verify code content
dwcli node code get ./my_task | head -20

# Check file structure
ls -la ./my_task/
```

These examples demonstrate the full range of `dwcli` capabilities for managing DataWorks workflow specifications.