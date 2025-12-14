import pytest
import json
import tempfile
import shutil
from pathlib import Path
from dwcli.pkg.schema.validator import SchemaValidator, ValidationErrorDetail


class TestSchemaValidator:
    @pytest.fixture
    def temp_dir(self):
        tmpdir = tempfile.mkdtemp()
        yield Path(tmpdir)
        shutil.rmtree(tmpdir)

    @pytest.fixture
    def schema_dir(self, temp_dir):
        schema_dir = temp_dir / "schemas"
        schema_dir.mkdir()
        
        test_schema = {
            "$schema": "http://json-schema.org/draft-07/schema#",
            "type": "object",
            "required": ["version", "kind", "spec"],
            "properties": {
                "version": {"type": "string"},
                "kind": {"type": "string", "enum": ["CycleWorkflow", "ManualWorkflow"]},
                "spec": {
                    "type": "object",
                    "required": ["nodes"],
                    "properties": {
                        "timeout": {"type": "integer"},
                        "nodes": {"type": "array"}
                    }
                }
            }
        }
        
        schema_file = schema_dir / "test.schedule.schema.json"
        with open(schema_file, 'w') as f:
            json.dump(test_schema, f)
        
        schema_file2 = schema_dir / "invalid.schedule.schema.json"
        with open(schema_file2, 'w') as f:
            json.dump(test_schema, f)
        
        return schema_dir

    @pytest.fixture
    def validator(self, schema_dir):
        return SchemaValidator(schema_dir)

    def test_load_schemas(self, validator):
        assert "test.schedule" in validator.schemas
        assert "invalid.schedule" in validator.schemas

    def test_validate_success(self, validator, temp_dir):
        valid_data = {
            "version": "1.0",
            "kind": "CycleWorkflow",
            "spec": {
                "timeout": 3600,
                "nodes": []
            }
        }
        
        spec_file = temp_dir / "test.schedule.json"
        with open(spec_file, 'w') as f:
            json.dump(valid_data, f, indent=2)
        
        is_valid, errors = validator.validate(valid_data, spec_file)
        
        assert is_valid
        assert len(errors) == 0

    def test_validate_type_error(self, validator, temp_dir):
        invalid_data = {
            "version": "1.0",
            "kind": "CycleWorkflow",
            "spec": {
                "timeout": "not_an_int",
                "nodes": []
            }
        }
        
        spec_file = temp_dir / "test.schedule.json"
        with open(spec_file, 'w') as f:
            json.dump(invalid_data, f, indent=2)
        
        is_valid, errors = validator.validate(invalid_data, spec_file)
        
        assert not is_valid
        assert len(errors) > 0
        assert any(e.error_type == "Type Mismatch" for e in errors)

    def test_validate_missing_required(self, validator, temp_dir):
        invalid_data = {
            "version": "1.0",
            "kind": "CycleWorkflow",
            "spec": {}
        }
        
        spec_file = temp_dir / "test.schedule.json"
        with open(spec_file, 'w') as f:
            json.dump(invalid_data, f, indent=2)
        
        is_valid, errors = validator.validate(invalid_data, spec_file)
        
        assert not is_valid
        assert len(errors) > 0
        assert any(e.error_type == "Missing Required Field" for e in errors)

    def test_validate_enum_error(self, validator, temp_dir):
        invalid_data = {
            "version": "1.0",
            "kind": "InvalidKind",
            "spec": {
                "timeout": 3600,
                "nodes": []
            }
        }
        
        spec_file = temp_dir / "invalid.schedule.json"
        with open(spec_file, 'w') as f:
            json.dump(invalid_data, f, indent=2)
        
        is_valid, errors = validator.validate(invalid_data, spec_file)
        
        assert not is_valid
        assert any(e.error_type == "Invalid Enum Value" for e in errors)

    def test_build_line_map(self, validator, temp_dir):
        spec_file = temp_dir / "test.schedule.json"
        spec_content = '''{
  "version": "1.0",
  "spec": {
    "timeout": 3600
  }
}'''
        spec_file.write_text(spec_content)
        
        line_map = validator._build_line_map(spec_file)
        
        assert "version" in line_map
        assert "spec" in line_map
        assert "spec.timeout" in line_map

    def test_generate_fix_command(self, validator, temp_dir):
        from jsonschema import ValidationError
        
        spec_file = temp_dir / "mydir" / "test.schedule.json"
        spec_file.parent.mkdir(parents=True)
        spec_file.write_text('{}')
        
        error = ValidationError("Expected an integer")
        error.validator = 'type'
        error.validator_value = 'integer'
        error.path = ['spec', 'timeout']
        
        fix_cmd = validator._generate_fix_command(error, 'spec.timeout', spec_file)
        
        assert fix_cmd is not None
        assert 'dwcli node set' in fix_cmd
        assert 'spec.timeout' in fix_cmd
        assert '--type int' in fix_cmd