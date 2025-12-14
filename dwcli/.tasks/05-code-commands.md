# Task 05: Code File Management

## Status: Pending

## Objectives
- [ ] Implement `code edit` with $EDITOR support
- [ ] Implement `code set` with multiple input methods
- [ ] Implement `code get` to stdout
- [ ] Ensure atomic writes with backup

## Commands
1. `dwcli node code edit <dir>`
2. `dwcli node code set <dir> --file script.sql`
3. `dwcli node code set <dir> --content "SELECT 1"`
4. `echo "SELECT 1" | dwcli node code set <dir>`
5. `dwcli node code get <dir>`

## Test Cases
- Edit opens correct editor
- Set from file works
- Set from stdin works
- Get outputs correct content
- Atomic write preserves backup on error
