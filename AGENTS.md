# AGENTS.md

This file provides guidance to Qoder (qoder.com) when working with code in this repository.

## Project Overview

DataWorks Spec defines a generic workflow description specification (FlowSpec) and provides MigrationX, a tool to migrate workflow models from different scheduling systems (DolphinScheduler, Airflow, Oozie, etc.) to DataWorks FlowSpec format.

### Core Components

1. **FlowSpec Specification** (`spec/`): JSON-based workflow description format supporting CycleWorkflow (scheduled) and ManualWorkflow (manual trigger)
2. **MigrationX Tool** (`client/migrationx/`): Multi-stage workflow migration tool with Reader → Transformer → Writer architecture
3. **Schema Definitions** (`schema/`): JSON schemas for workflow entities (nodes, flows, artifacts, triggers, etc.)

### Architecture

MigrationX follows a pipeline pattern:
- **Domain Model** (`migrationx-domain/`): Domain models for workflow engines (Airflow, DolphinScheduler, Azkaban, Oozie, DataWorks, etc.)
- **Reader** (`migrationx-reader/`): Export tools that read workflows from source systems
- **Transformer** (`migrationx-transformer/`): Convert between workflow formats
- **Writer** (`migrationx-writer/`): Import tools that write FlowSpec to DataWorks

### Key Java Packages

- `com.aliyun.dataworks.common.spec`: FlowSpec core specification classes
  - `domain/`: Spec entity definitions (SpecNode, SpecWorkflow, SpecVariable, etc.)
  - `parser/`: Parse JSON to spec domain objects (use `SpecUtil.parseToDomain()`)
  - `writer/`: Write spec objects to JSON (use `SpecUtil.writeToSpec()`)
  - `adapter/`: Transform between different spec versions
- `com.aliyun.dataworks.migrationx`: MigrationX implementation
  - Command apps registered in `client/migrationx/src/main/conf/apps.json`
  - Entry point: `CommandAppEntrance` loads apps.json and dispatches to CommandApp implementations

## Build Commands

Build requirements: Java 1.8, Maven 3.6.3+, Python 3.10

```bash
mvn clean install -DskipTests=true && mvn package -DskipTests=true
```

Run tests:
```bash
mvn test
```

Version management:
```bash
mvn versions:set -DnewVersion={major.minor.patch}
```

After build, the MigrationX tool is assembled at `client/target/migrationx/` with bin/, conf/, lib/, and python/ directories.

## Testing

- Test files follow standard Maven structure: `src/test/java/`
- Run specific test: `mvn test -Dtest=ClassName`
- Tests use JUnit 4.13.2 and Mockito 3.9.0

## FlowSpec Structure

FlowSpec JSON contains:
- `version`: Spec version
- `kind`: CycleWorkflow or ManualWorkflow
- `metadata`: Owner, description
- `spec`: Workflow definition with nodes, flows (dependencies), variables, scripts, triggers, artifacts, resources, functions

The spec module provides utilities to parse/write FlowSpec and transform between workflow systems.

## MigrationX Command Apps

Available apps defined in `apps.json`:
- **reader**: dolphinscheduler, aliyunemr, airflow, dolphinscheduler_single_json
- **transformer**: dolphinscheduler_to_dataworks, dolphinscheduler_to_flowspec, airflowv2_to_dataworks
- **writer**: dataworks, dataworks_flowspec, flowspec_to_dataworks

Each app extends `CommandApp` and is invoked via `CommandAppEntrance`.
