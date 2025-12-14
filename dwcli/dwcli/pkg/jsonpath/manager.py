import json
from pathlib import Path
from typing import Any, Dict, List, Optional, Union
from jsonpath_ng import parse
from jsonpath_ng.ext import parser


class JSONPathError(Exception):
    pass


class JSONPathManager:
    @staticmethod
    def read_json(filepath: Path) -> Dict[str, Any]:
        with open(filepath, 'r', encoding='utf-8') as f:
            return json.load(f)

    @staticmethod
    def write_json(filepath: Path, data: Dict[str, Any], indent: int = 2):
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=indent, ensure_ascii=False)
            f.write('\n')

    @staticmethod
    def get_value(data: Dict[str, Any], path: str) -> Any:
        try:
            jsonpath_expr = parser.ExtentedJsonPathParser().parse(path)
            matches = jsonpath_expr.find(data)
            
            if not matches:
                return None
            
            if len(matches) == 1:
                return matches[0].value
            
            return [match.value for match in matches]
        except Exception as e:
            raise JSONPathError(f"Failed to get value at path '{path}': {e}")

    @staticmethod
    def set_value(data: Dict[str, Any], path: str, value: Any, value_type: Optional[str] = None) -> Dict[str, Any]:
        try:
            converted_value = JSONPathManager._convert_value(value, value_type)
            
            parts = path.replace('[', '.').replace(']', '').split('.')
            parts = [p for p in parts if p]
            
            current = data
            for i, part in enumerate(parts[:-1]):
                if part.isdigit():
                    idx = int(part)
                    if not isinstance(current, list):
                        raise JSONPathError(f"Expected list at path segment '{parts[:i+1]}', got {type(current)}")
                    while len(current) <= idx:
                        current.append({})
                    current = current[idx]
                else:
                    if part not in current:
                        next_part = parts[i + 1] if i + 1 < len(parts) else None
                        if next_part and next_part.isdigit():
                            current[part] = []
                        else:
                            current[part] = {}
                    current = current[part]
            
            last_part = parts[-1]
            if last_part.isdigit():
                idx = int(last_part)
                if not isinstance(current, list):
                    raise JSONPathError(f"Expected list at final path segment, got {type(current)}")
                while len(current) <= idx:
                    current.append(None)
                current[idx] = converted_value
            else:
                current[last_part] = converted_value
            
            return data
        except JSONPathError:
            raise
        except Exception as e:
            raise JSONPathError(f"Failed to set value at path '{path}': {e}")

    @staticmethod
    def unset_value(data: Dict[str, Any], path: str) -> Dict[str, Any]:
        try:
            parts = path.replace('[', '.').replace(']', '').split('.')
            parts = [p for p in parts if p]
            
            current = data
            for part in parts[:-1]:
                if part.isdigit():
                    current = current[int(part)]
                else:
                    current = current[part]
            
            last_part = parts[-1]
            if last_part.isdigit():
                del current[int(last_part)]
            else:
                del current[last_part]
            
            return data
        except (KeyError, IndexError, TypeError) as e:
            raise JSONPathError(f"Path '{path}' does not exist: {e}")
        except Exception as e:
            raise JSONPathError(f"Failed to unset value at path '{path}': {e}")

    @staticmethod
    def _convert_value(value: str, value_type: Optional[str]) -> Any:
        if value_type is None:
            return JSONPathManager._infer_type(value)
        
        type_map = {
            'string': str,
            'str': str,
            'int': int,
            'integer': int,
            'float': float,
            'number': float,
            'bool': lambda v: v.lower() in ('true', '1', 'yes'),
            'boolean': lambda v: v.lower() in ('true', '1', 'yes'),
            'json': json.loads,
        }
        
        converter = type_map.get(value_type.lower())
        if converter is None:
            raise JSONPathError(f"Unknown type: {value_type}")
        
        try:
            return converter(value)
        except Exception as e:
            raise JSONPathError(f"Failed to convert '{value}' to {value_type}: {e}")

    @staticmethod
    def _infer_type(value: str) -> Any:
        if value.lower() in ('true', 'false'):
            return value.lower() == 'true'
        
        try:
            if '.' in value:
                return float(value)
            return int(value)
        except ValueError:
            pass
        
        if value.startswith(('{', '[')):
            try:
                return json.loads(value)
            except json.JSONDecodeError:
                pass
        
        return value
