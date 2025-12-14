# dwcli - DataWorks CLI Tool

A powerful Configuration-as-Code (CaC) management tool for DataWorks FlowSpec.

## Features

- **Scaffolding**: Create structured object directories with templates
- **JSON Path Operations**: Precise configuration management (set/unset/inspect)
- **Schema Validation**: Strict validation with detailed error reporting
- **Code Management**: Edit and manage code files seamlessly
- **Cross-Platform**: Works on Windows, macOS, and Linux

## Installation

```bash
pip install -e .
```

## Quick Start

```bash
# Create a new node
dwcli node create ./tasks --name my-daily-task --template odps-sql-daily

# Set configuration
dwcli node set ./tasks/my-daily-task spec.timeout=3600 --type int

# Inspect configuration
dwcli node inspect ./tasks/my-daily-task spec.timeout

# Edit code
dwcli node code edit ./tasks/my-daily-task

# Validate
dwcli node validate ./tasks/my-daily-task
```

## Development

Run tests:
```bash
pytest tests/ -v --cov=dwcli
```