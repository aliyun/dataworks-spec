# AGENTS.md

This file provides guidance to coding agents operating in this repository. It outlines build/test commands, code style conventions, and project-wide rules to ensure consistent changes.

**Cursor & Copilot Rules**

- Cursor rules: Not detected in this repository (no .cursor or .cursorrules). If such rules are added later, document them here and enforce during edits.
- Copilot instructions: No dedicated .github/copilot-instructions.md found. If present in subfolders, summarize in this file and align PRs accordingly.

**Build, Lint, Test Commands**

- Build (full, with tests):
  - `mvn -B clean install`
  - Runs all modules and executes tests by default
- Build (skip tests):
  - `mvn -B clean install -DskipTests`
- Package only (skip tests):
  - `mvn -B -DskipTests package`
- Run tests (all):
  - `mvn test`
- Run a single test class:  
  - `mvn -Dtest=ClassName test`  
  - Example: `mvn -Dtest=com.aliyun.dataworks.SomeTest test`
- Run a single test method:  
  - `mvn -Dtest=ClassName#methodName test`  
  - Example: `mvn -Dtest=MyTest#testEdgeCase test`
- Run tests for a specific module:  
  - `mvn -pl module-name -am test`
- Linting / formatting:
  - Java: checkstyle, spotless (if configured)
  - Python: black, isort, ruff (where Python code exists)
  - Common steps:
    - `mvn spotless:check` (if Spotless is configured)
    - `mvn spotless:apply` to auto-format
    - `mvn checkstyle:check` to enforce Java style
- Quick validation in CI-like fashion:
  - `mvn -B -DskipTests -Dcheckstyle.skip=false checkstyle:check` if needed

**Code Style Guidelines**

- Language scope
  - Java (primary)
  - Python (dwcli and related tools)
- General guidance: keep code readable, consistent, and maintainable.

**Java Style**

- Imports
  - Group imports as: java.*, javax.*, third-party, then project.
  - Use a single import line per class (avoid wildcard import). Static imports after normal imports.
  - Alphabetize within groups; leave a blank line between groups.
- Formatting
  - 4-space indentation; line length target 120 chars; trailing whitespace removed.
  - Braces: K&R style; opening braces on same line as statement; closing braces on own line.
  - End-of-file newline required; avoid multiple blank lines.
- Naming conventions
  - Packages: lowercase (e.g., com.aliyun.dataworks).
  - Classes/Enums: PascalCase; Interfaces: PascalCase with descriptive name.
  - Methods/Variables: camelCase; Constants: ALL_CAPS with underscores.
  - Generics: descriptive type names; avoid overly short names.
- Types & null-safety
  - Prefer final for locals where possible; use Optional where absence is meaningful.
  - Annotate nullability (e.g., @NotNull, @Nullable) where a static analysis tool exists.
- Error handling & logging
  - Do not swallow exceptions; wrap with context; preserve original cause.
  - Use SLF4J/logging with parameterized messages (logger.error("... {} ...", arg)).
  - Avoid catching generic exceptions unless rethrowing with context.
- Testing style
  - Follow existing test naming; use @Test; use descriptive method names.
  - Use mocks for external dependencies; verify interactions where meaningful.
  - Test small units; prefer focused tests over large integration tests in unit scope.
- Documentation & comments
  - Javadoc for public APIs; inline comments sparing and purposeful.
  - Provide class-level description and method-level behavior notes where not obvious.

**Python Style (dwcli, CLI tools)**

- Formatting & tooling
  - Use black for formatting; isort for imports; flake8/ruff for lint checks.
  - Type hints with typing; use from __future__ import annotations when helpful.
- Imports
  - Group imports: stdlib, third-party, local; alphabetical within groups.
  - Avoid wildcard imports; prefer explicit names.
- Naming & style
  - Functions and variables: snake_case; classes: PascalCase; constants: UPPER_SNAKE.
  - Docstrings: module/function/class docstrings with concise descriptions.
- Testing
  - Use pytest; name tests with test_*.py; descriptive test function names.
- Error handling
  - Raise specific exceptions; include context; avoid bare except.

**General Guidelines**

- Code structure
  - Keep modules focused; a file should not exceed ~400-600 lines without good reason.
  - Avoid large diff churn; prefer small, logical commits.
- Versioning & packaging
  - Align with Maven for Java; follow mvn version tooling as defined in project.
- Documentation
  - Update AGENTS.md, README, and inline docs where needed.
- Security & secrets
  - Do not commit credentials; review .env and credential-like files; redact secrets in diffs.

**Git & PR Workflow**

- Commits
  - One logical change per commit; 1-2 sentence message focusing on why, not what.
- Branching
  - Use feature branches (e.g., feature/xxxx) or bugfix branches (e.g., fix/xxxx).
- PRs
  - Include a concise summary, impact analysis, and any testing results; reference affected modules.

**Testing & Validation**

- Local validation
  - Build locally; run unit tests first, then integration tests if available.
- CI expectations
  - Ensure new changes pass in CI; comply with existing workflows in .github/workflows.

**Appendix**

- Quick references:
  - Java: `mvn -Dtest=MyTest#testMethod test` to run a single test, `mvn spotless:check` for formatting checks.
  - Python: `pytest tests/test_*.py -k keyword` to filter tests; `black` / `ruff` for formatting/linting.

