# Project Overview

The `dataworks-spec` project defines a generic workflow description specification called FlowSpec and provides a migration tool (MigrationX) to convert workflow models from different scheduling systems to DataWorks workflow models. This allows for standardization and migration of workflows across different platforms.

## Project Structure

The project is organized into several main directories:

- `spec/`: Contains the core FlowSpec schema definitions and domain models
- `client/`: Contains client-side tools including the MigrationX migration tool
- `schema/`: Contains JSON schema definitions for various FlowSpec components
- `docs/`: Documentation for the project
- `pom.xml`: Parent Maven configuration for the multi-module project

## Key Components

### FlowSpec
FlowSpec is a specification for describing workflows that includes:
- CycleWorkflow: For scheduled workflows
- ManualWorkflow: For manually triggered workflows
- Various components like nodes, variables, scripts, triggers, artifacts, and functions

### MigrationX
The MigrationX tool provides capabilities for:
- Exporting workflow models from various scheduling engines
- Converting between different workflow formats
- Importing to DataWorks using OpenAPI

## Technical Details

The project is built with Java using Maven as the build system. Key dependencies include:
- Jackson for JSON processing
- Gson for JSON serialization
- Various Apache Commons libraries
- JSON schema validator
- Lombok for reducing boilerplate code

The main modules are:
- `dw-common-spec`: Contains the core FlowSpec definitions
- `client`: Contains client tools including migration tools
- `migrationx`: The workflow migration framework
- `migrationx-transformer`: Conversion logic between workflow engines

## Building and Running

To build the project:

```bash
mvn clean install
```

This will compile all modules and run tests. The project uses Java 11 as the target version.

## Development Conventions

- Java 11 is used as the target version
- Maven is used for dependency management and builds
- JSON schemas are used for validation
- The codebase includes comprehensive example files for reference
- Testing is performed using JUnit and Mockito

## Usage

The specification supports various workflow scenarios including:
- Simple workflows with basic nodes
- Branching and joining logic
- Looping constructs (for-each, do-while)
- Manual and scheduled workflows
- Resource and function definitions
- Complex dependency relationships between nodes