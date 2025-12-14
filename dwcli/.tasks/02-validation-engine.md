# Task 02: Schema Validation Engine

## Status: Pending

## Objectives
- [ ] Implement schema loader from ~/.config/dwcli/schemas/
- [ ] Create JSON path to line number mapper
- [ ] Build detailed error reporter with:
  - Error path
  - Line number
  - Fix suggestion (dwcli set command)
- [ ] Add colored output for errors

## Key Features
- Preload schemas on startup
- Map JSON paths to line numbers using custom parser
- Generate actionable fix commands based on error type
- Atomic operations with rollback on validation failure

## Test Cases
- Valid spec passes validation
- Invalid type triggers detailed error
- Missing required field shows fix command
- Enum mismatch suggests valid values
