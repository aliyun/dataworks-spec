# Task 01: Project Setup

## Status: In Progress

## Objectives
- [x] Create Go module structure
- [ ] Add dependencies (cobra, gjson, sjson, gojsonschema)
- [ ] Setup project layout
- [ ] Create Makefile for build

## Structure
```
dwcli/
├── cmd/              # Cobra commands
├── pkg/
│   ├── domain/       # Entity models
│   ├── jsonpath/     # JSON path operations
│   ├── schema/       # Validation logic
│   ├── template/     # Template engine
│   └── fileops/      # File operations
├── internal/         # Internal utilities
├── templates/        # Built-in templates
└── main.go
```

## Dependencies
- github.com/spf13/cobra
- github.com/tidwall/gjson
- github.com/tidwall/sjson
- github.com/xeipuuv/gojsonschema
- github.com/fatih/color
