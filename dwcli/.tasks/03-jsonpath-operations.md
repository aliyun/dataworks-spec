# Task 03: JSON Path Operations

## Status: Pending

## Objectives
- [ ] Implement `set` with type inference and coercion
- [ ] Implement `unset` for path deletion
- [ ] Implement `inspect` for querying
- [ ] Add atomic file operations (backup/restore)
- [ ] Support dry-run mode

## Commands
1. `dwcli node set <dir> path=value --type int`
2. `dwcli node unset <dir> path`
3. `dwcli node inspect <dir> path --output json`

## Test Cases
- Set simple string value
- Set nested object value
- Set with type conversion
- Unset field and validate result
- Inspect returns correct value
- Dry-run doesn't modify file
- Rollback on validation failure
