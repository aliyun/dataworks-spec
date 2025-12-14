# Task 04: Object Scaffolding (Create)

## Status: Pending

## Objectives
- [ ] Create template system using Go text/template
- [ ] Implement `create` command
- [ ] Add built-in templates (odps-sql-daily, shell-daily, etc.)
- [ ] Validate generated spec against schema

## Commands
`dwcli node create <dir> --name my-task --template odps-sql-daily`

## Template Variables
- {{.Name}}: Object name/ID
- {{.Timestamp}}: Creation time
- Auto-generate: spec.id, spec.name, script.path

## Test Cases
- Create from default template
- Create from named template
- Generated spec passes validation
- Directory structure is correct
- Code file matches spec.script.path
