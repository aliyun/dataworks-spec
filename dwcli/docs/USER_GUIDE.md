# dwcli User Guide

## Overview

`dwcli` is a Configuration-as-Code (CaC) management tool for DataWorks FlowSpec. It provides commands to create, modify, validate, and manage workflow specifications using JSON Path operations.

## Installation

```bash
cd dwcli
pip install -e .
```

Or install from requirements:
```bash
pip install -r requirements.txt
```

## Commands

### Create Command

Create a new object directory with spec and code files.

```bash
dwcli node create <parent-dir> --name <name> [--template <template>] [--owner <owner>]
```

**Behavior**: Creates `<parent-dir>/<name>/` directory containing spec and code files.

**Examples:**

```bash
# Create ODPS SQL task
# Creates: ./tasks/daily_report/
dwcli node create ./tasks --name daily_report --template odps-sql-daily

# Create Shell task
# Creates: ./jobs/backup_job/
dwcli node create ./jobs --name backup_job --template shell-daily --owner admin

# Create Python task
# Creates: ./etl/data_process/
dwcli node create ./etl --name data_process --template python-daily

# Create manual workflow
# Creates: ./manual/adhoc_task/
dwcli node create ./manual --name adhoc_task --template manual-workflow
```

**Directory Structure Created:**
```
./tasks/
└── daily_report/              # Node directory (name)
    ├── daily_report.schedule.json
    └── daily_report.sql
```

**Available Templates:**
- `odps-sql-daily`: ODPS SQL scheduled workflow
- `shell-daily`: Shell script scheduled workflow
- `python-daily`: Python script scheduled workflow
- `manual-workflow`: Manual trigger workflow

### Set Command

Modify spec JSON fields using JSON Path.

```bash
dwcli node set <dirpath> <path>=<value>... [--type <type>] [--dry-run]
```

**Examples:**

```bash
# Set timeout
dwcli node set ./my_task spec.nodes[0].timeout=7200 --type int

# Set multiple fields
dwcli node set ./my_task \
  spec.nodes[0].timeout=3600 --type int \
  metadata.owner=admin

# Set nested object
dwcli node set ./my_task spec.config.maxRetry=3 --type int

# Dry run (preview changes)
dwcli node set ./my_task spec.timeout=1800 --dry-run

# Set JSON object
dwcli node set ./my_task 'spec.metadata={"key":"value"}' --type json

# Set boolean
dwcli node set ./my_task spec.enabled=true --type bool
```

**Type Options:**
- `string` or `str`: String value (default for text)
- `int` or `integer`: Integer number
- `float` or `number`: Floating point number
- `bool` or `boolean`: Boolean (true/false)
- `json`: Parse value as JSON object/array

### Unset Command

Remove fields from spec JSON.

```bash
dwcli node unset <dirpath> <path>... [--dry-run]
```

**Examples:**

```bash
# Remove a field
dwcli node unset ./my_task spec.timeout

# Remove multiple fields
dwcli node unset ./my_task spec.timeout spec.retry

# Dry run
dwcli node unset ./my_task spec.config --dry-run
```

### Inspect Command

Query and display spec JSON values.

```bash
dwcli node inspect <dirpath> [<path>...] [--output <format>]
```

**Examples:**

```bash
# Inspect entire spec
dwcli node inspect ./my_task

# Inspect specific field
dwcli node inspect ./my_task spec.timeout

# Inspect multiple fields
dwcli node inspect ./my_task metadata.name spec.nodes[0].type

# Output as JSON
dwcli node inspect ./my_task --output json

# Output as YAML
dwcli node inspect ./my_task --output yaml

# Raw output (default)
dwcli node inspect ./my_task spec.timeout --output raw
```

### Validate Command

Validate spec JSON against schema.

```bash
dwcli node validate <dirpath>
```

**Examples:**

```bash
dwcli node validate ./my_task
```

**Output:**
- ✓ Success: "Validation passed"
- ✗ Failure: Detailed error report with:
  - Error type
  - JSON path
  - Line number
  - Error reason
  - Fix command suggestion

### Code Subcommands

Manage code files within object directories.

#### Code Edit

Open code file in editor.

```bash
dwcli node code edit <dirpath>
```

Uses `$EDITOR` environment variable, or system default (vim, nano, notepad).

**Examples:**

```bash
dwcli node code edit ./my_task

# Set custom editor
export EDITOR=vim
dwcli node code edit ./my_task
```

#### Code Set

Replace code file content.

```bash
dwcli node code set <dirpath> [--file <file>] [--content <string>]
```

**Examples:**

```bash
# From file
dwcli node code set ./my_task --file script.sql

# From string
dwcli node code set ./my_task --content "SELECT * FROM table;"

# From stdin
cat script.sql | dwcli node code set ./my_task

echo "#!/bin/bash" | dwcli node code set ./shell_task
```

#### Code Get

Output code file content to stdout.

```bash
dwcli node code get <dirpath>
```

**Examples:**

```bash
# Display code
dwcli node code get ./my_task

# Save to file
dwcli node code get ./my_task > backup.sql

# Pipe to other commands
dwcli node code get ./my_task | grep SELECT
```

## Workflow Examples

### Create and Configure a Task

```bash
# 1. Create task
dwcli node create ./daily_agg --name daily_aggregation --template odps-sql-daily

# 2. Configure timeout and retry
dwcli node set ./daily_agg \
  spec.nodes[0].timeout=7200 --type int \
  spec.nodes[0].retry=3 --type int

# 3. Set code
dwcli node code set ./daily_agg --file my_query.sql

# 4. Validate
dwcli node validate ./daily_agg

# 5. Inspect configuration
dwcli node inspect ./daily_agg spec.nodes[0]
```

### Batch Update Multiple Tasks

```bash
#!/bin/bash
for task in task1 task2 task3; do
  dwcli node set ./$task spec.nodes[0].timeout=3600 --type int
  dwcli node validate ./$task
done
```

### Migration Workflow

```bash
# 1. Create from template
dwcli node create ./migrated_task --name etl_job --template odps-sql-daily

# 2. Update configuration
dwcli node set ./migrated_task \
  metadata.owner=data_team \
  spec.nodes[0].timeout=10800 --type int

# 3. Set migrated code
dwcli node code set ./migrated_task --file legacy_script.sql

# 4. Validate and inspect
dwcli node validate ./migrated_task
dwcli node inspect ./migrated_task
```

## Directory Structure

The CLI creates a hierarchical structure:

```
<parent-dir>/                  # Parent directory (you specify)
└── <node-name>/              # Node directory (auto-created)
    ├── <node-name>.schedule.json    # Spec file
    └── <node-name>.<ext>            # Code file
```

**Example:**
```bash
dwcli node create ./tasks --name daily_sales_report --template odps-sql-daily
```

Creates:
```
tasks/
└── daily_sales_report/
    ├── daily_sales_report.schedule.json
    └── daily_sales_report.sql
```

**Multiple nodes in same parent:**
```bash
dwcli node create ./tasks --name task1 --template odps-sql-daily
dwcli node create ./tasks --name task2 --template python-daily
```

Results in:
```
tasks/
├── task1/
│   ├── task1.schedule.json
│   └── task1.sql
└── task2/
    ├── task2.schedule.json
    └── task2.py
```

**Operating on nodes:**
All other commands operate on the node directory:
```bash
dwcli node inspect ./tasks/task1 metadata.name
dwcli node set ./tasks/task1 'spec.timeout=3600' --type int
dwcli node code get ./tasks/task1
```

## Schema Validation

Schemas are stored in `~/.config/dwcli/schemas/` and loaded automatically.

**Schema Naming Convention:**
- Schema file: `<name>.schedule.schema.json`
- Matches spec file: `<name>.schedule.json`

**Error Reporting:**

When validation fails, dwcli provides:
1. **Error Type**: Type Mismatch, Missing Required Field, Invalid Enum Value, etc.
2. **Error Path**: JSON path to the error (e.g., `spec.timeout`)
3. **Error Line**: Line number in the JSON file
4. **Fix Command**: Suggested `dwcli set` command to fix the error

Example error output:
```
❌ VALIDATION FAILED: Changes Reverted!
--------------------------------------------------------------------------
#1. Error Type: Type Mismatch
   Error Path: spec.timeout
   Error Reason: Expected an integer (number), got string.
   Error Line: 12
   Solution: Use 'dwcli node set ./my_task spec.timeout=3600 --type int'
--------------------------------------------------------------------------
```

## Tips

1. **Use Dry Run**: Test changes before applying
   ```bash
   dwcli node set ./task spec.timeout=1800 --dry-run
   ```

2. **Validate After Changes**: Always validate after modifications
   ```bash
   dwcli node set ./task spec.timeout=1800 --type int
   dwcli node validate ./task
   ```

3. **Inspect Before Editing**: Check current values
   ```bash
   dwcli node inspect ./task spec.nodes[0]
   ```

4. **Backup Code**: Save before major edits
   ```bash
   dwcli node code get ./task > task_backup.sql
   ```

5. **Atomic Operations**: All `set` and `unset` operations are atomic with automatic rollback on validation failure

## Troubleshooting

**Multiple spec files error:**
```
ValueError: Multiple *.schedule.json files found
```
Solution: Ensure only one `*.schedule.json` file exists in the directory.

**No code file error:**
```
Error: No code file found in directory
```
Solution: Create a code file matching the script.path in the spec.

**Validation errors:**
- Check error message for specific issue
- Use suggested fix command
- Verify schema requirements

**Type conversion errors:**
```
JSONPathError: Failed to convert 'abc' to int
```
Solution: Provide correct value type or use `--type` flag.