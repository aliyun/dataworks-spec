# Version 1.1.0

## New

* add top level field `spec` to specification
* add `files` field type `File` to DataWorksWorkflowSpec to separate concept from `Script`
* [TODO] new: emr node supports

## Update

* ArtifactType.OUTPUT to ArtifactType.NODE_OUTPUT, and compatible with old version spec `output` key
* delete currently unused Specification.engine field
* update SpecResource/SpecDatasource/SpecTable define and parser/writer

# Version 1.0.0

## initial release of `FlowSpec` and `MigrationX` tools