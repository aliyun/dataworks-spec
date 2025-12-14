import json
import re
from pathlib import Path
from typing import Dict, Any, List, Optional, Tuple
from dataclasses import dataclass
from jsonschema import validate, ValidationError, Draft7Validator
from rich.console import Console
from rich.panel import Panel
from rich.text import Text


@dataclass
class ValidationErrorDetail:
    error_type: str
    path: str
    line_number: Optional[int]
    message: str
    fix_command: Optional[str]


class SchemaValidator:
    def __init__(self, schema_dir: Optional[Path] = None):
        if schema_dir is None:
            schema_dir = Path.home() / ".config" / "dwcli" / "schemas"
        
        self.schema_dir = Path(schema_dir)
        self.schemas: Dict[str, Dict[str, Any]] = {}
        self._load_builtin_schemas()
        self._load_schemas()

    def _load_builtin_schemas(self):
        """Load built-in schemas from package"""
        builtin_dir = Path(__file__).parent.parent.parent / "schemas"
        
        if not builtin_dir.exists():
            return
        
        for schema_file in builtin_dir.glob("*.schema.json"):
            try:
                with open(schema_file, 'r', encoding='utf-8') as f:
                    schema = json.load(f)
                    kind = schema_file.stem.replace('.schema', '')
                    self.schemas[kind] = schema
            except Exception as e:
                print(f"Warning: Failed to load built-in schema {schema_file}: {e}")

    def _load_schemas(self):
        """Load user schemas from config directory (overrides built-in)"""
        if not self.schema_dir.exists():
            return
        
        for schema_file in self.schema_dir.glob("*.schema.json"):
            try:
                with open(schema_file, 'r', encoding='utf-8') as f:
                    schema = json.load(f)
                    kind = schema_file.stem.replace('.schema', '')
                    self.schemas[kind] = schema
            except Exception as e:
                print(f"Warning: Failed to load schema {schema_file}: {e}")

    def validate(self, data: Dict[str, Any], spec_file: Path) -> Tuple[bool, List[ValidationErrorDetail]]:
        kind = data.get('kind', 'unknown')
        
        schema_key = f"{kind}.schedule"
        
        if schema_key not in self.schemas:
            schema_key = kind
        
        if schema_key not in self.schemas:
            return True, []
        
        schema = self.schemas[schema_key]
        validator = Draft7Validator(schema)
        errors = []
        
        line_map = self._build_line_map(spec_file)
        
        for error in validator.iter_errors(data):
            detail = self._build_error_detail(error, line_map, spec_file)
            errors.append(detail)
        
        return len(errors) == 0, errors

    def _build_line_map(self, filepath: Path) -> Dict[str, int]:
        line_map = {}
        
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        lines = content.split('\n')
        current_path = []
        
        for line_num, line in enumerate(lines, start=1):
            stripped = line.strip()
            
            key_match = re.match(r'"([^"]+)"\s*:', stripped)
            if key_match:
                key = key_match.group(1)
                
                indent = len(line) - len(line.lstrip())
                
                while len(current_path) > 0 and current_path[-1][1] >= indent:
                    current_path.pop()
                
                path_str = '.'.join([p[0] for p in current_path] + [key])
                line_map[path_str] = line_num
                
                current_path.append((key, indent))
        
        return line_map

    def _build_error_detail(self, error: ValidationError, line_map: Dict[str, int], spec_file: Path) -> ValidationErrorDetail:
        # Build JSON Path in standard format: $.field[0].subfield
        json_path = self._build_json_path(error.path) if error.path else '$'
        
        line_number = line_map.get(json_path.lstrip('$.').replace('[', '.').replace(']', ''))
        
        error_type = self._classify_error(error)
        
        fix_command = self._generate_fix_command(error, json_path, spec_file)
        
        return ValidationErrorDetail(
            error_type=error_type,
            path=json_path,
            line_number=line_number,
            message=error.message,
            fix_command=fix_command
        )
    
    def _build_json_path(self, path_parts) -> str:
        """Build standard JSON Path format: $.spec.nodes[0].timeout"""
        if not path_parts:
            return '$'
        
        result = '$'
        for part in path_parts:
            if isinstance(part, int):
                result += f'[{part}]'
            else:
                result += f'.{part}'
        return result

    def _classify_error(self, error: ValidationError) -> str:
        if error.validator == 'type':
            return "Type Mismatch"
        elif error.validator == 'required':
            return "Missing Required Field"
        elif error.validator == 'enum':
            return "Invalid Enum Value"
        elif error.validator == 'pattern':
            return "Pattern Mismatch"
        elif error.validator == 'minLength' or error.validator == 'maxLength':
            return "Length Constraint Violation"
        elif error.validator == 'minimum' or error.validator == 'maximum':
            return "Range Constraint Violation"
        else:
            return "Validation Error"

    def _generate_fix_command(self, error: ValidationError, json_path: str, spec_file: Path) -> Optional[str]:
        dirpath = spec_file.parent
        
        # Convert JSON Path to dwcli path format (remove $. prefix, keep array notation)
        dwcli_path = json_path.lstrip('$.')
        
        if error.validator == 'type':
            expected_type = error.validator_value
            if isinstance(expected_type, list):
                expected_type = expected_type[0]
            
            type_map = {
                'integer': 'int',
                'number': 'float',
                'string': 'string',
                'boolean': 'bool',
            }
            
            cli_type = type_map.get(expected_type, 'string')
            
            example_values = {
                'int': '0',
                'float': '0.0',
                'string': '""',
                'bool': 'true',
            }
            
            example = example_values.get(cli_type, '""')
            return f"dwcli node set {dirpath} {dwcli_path}={example} --type {cli_type}"
        
        elif error.validator == 'required':
            missing_field = error.message.split("'")[1] if "'" in error.message else "field"
            full_path = f"{dwcli_path}.{missing_field}" if json_path != '$' else missing_field
            return f"dwcli node set {dirpath} {full_path}=<value>"
        
        elif error.validator == 'enum':
            valid_values = error.validator_value
            if valid_values:
                example = valid_values[0]
                return f"dwcli node set {dirpath} {dwcli_path}={example}"
        
        elif error.validator in ('minimum', 'maximum'):
            constraint = error.validator_value
            return f"dwcli node set {dirpath} {dwcli_path}={constraint} --type int"
        
        return None

    def print_errors(self, errors: List[ValidationErrorDetail]):
        console = Console()
        
        console.print("\n")
        console.print("❌ [bold red]VALIDATION FAILED: Changes Reverted![/bold red]")
        console.print("-" * 74)
        
        for i, error in enumerate(errors, start=1):
            console.print(f"\n[bold yellow]#{i}. Error Type:[/bold yellow] {error.error_type}")
            console.print(f"   [cyan]Error Path:[/cyan] {error.path}")
            console.print(f"   [cyan]Error Reason:[/cyan] {error.message}")
            
            if error.line_number:
                console.print(f"   [cyan]Error Line:[/cyan] {error.line_number}")
            
            if error.fix_command:
                console.print(f"   [green]Solution:[/green] Use '{error.fix_command}'")
            
            console.print("-" * 74)
        
        console.print("\n")