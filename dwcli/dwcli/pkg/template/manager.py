import json
from pathlib import Path
from typing import Dict, Any, Optional
from jinja2 import Template


class TemplateManager:
    _builtin_templates: Dict[str, Dict[str, Any]] = {}
    _user_templates: Dict[str, Dict[str, Any]] = {}
    _builtin_loaded: bool = False
    _user_loaded: bool = False

    @classmethod
    def _load_builtin_templates(cls):
        if cls._builtin_loaded:
            return
        
        builtin_dir = Path(__file__).parent.parent.parent / "templates"
        
        if not builtin_dir.exists():
            return
        
        for template_file in builtin_dir.glob("*.template.json"):
            try:
                with open(template_file, 'r', encoding='utf-8') as f:
                    template_data = json.load(f)
                    template_name = template_file.stem.replace('.template', '')
                    cls._builtin_templates[template_name] = template_data
            except Exception as e:
                print(f"Warning: Failed to load builtin template {template_file}: {e}")
        
        cls._builtin_loaded = True

    @classmethod
    def _load_user_templates(cls, template_dir: Optional[Path] = None):
        if cls._user_loaded:
            return
        
        if template_dir is None:
            template_dir = Path.home() / ".config" / "dwcli" / "templates"
        
        template_dir = Path(template_dir)
        
        if not template_dir.exists():
            cls._user_loaded = True
            return
        
        for template_file in template_dir.glob("*.template.json"):
            try:
                with open(template_file, 'r', encoding='utf-8') as f:
                    template_data = json.load(f)
                    template_name = template_file.stem.replace('.template', '')
                    cls._user_templates[template_name] = template_data
            except Exception as e:
                print(f"Warning: Failed to load user template {template_file}: {e}")
        
        cls._user_loaded = True

    @classmethod
    def get_template(cls, template_name: str) -> Optional[Dict[str, Any]]:
        if not cls._builtin_loaded:
            cls._load_builtin_templates()
        
        if not cls._user_loaded:
            cls._load_user_templates()
        
        if template_name in cls._user_templates:
            return cls._user_templates[template_name]
        
        return cls._builtin_templates.get(template_name)

    @classmethod
    def list_templates(cls) -> list:
        if not cls._builtin_loaded:
            cls._load_builtin_templates()
        
        if not cls._user_loaded:
            cls._load_user_templates()
        
        builtin = list(cls._builtin_templates.keys())
        user = list(cls._user_templates.keys())
        
        return builtin + user

    @classmethod
    def list_builtin_templates(cls) -> list:
        if not cls._builtin_loaded:
            cls._load_builtin_templates()
        
        return list(cls._builtin_templates.keys())

    @classmethod
    def list_user_templates(cls) -> list:
        if not cls._user_loaded:
            cls._load_user_templates()
        
        return list(cls._user_templates.keys())

    @staticmethod
    def render_spec(template_name: str, context: Dict[str, Any]) -> Dict[str, Any]:
        template_def = TemplateManager.get_template(template_name)
        
        if template_def is None:
            raise ValueError(f"Template '{template_name}' not found")
        
        spec_template_str = json.dumps(template_def['spec'])
        template = Template(spec_template_str)
        rendered_str = template.render(**context)
        
        return json.loads(rendered_str)

    @staticmethod
    def render_code(template_name: str, context: Dict[str, Any]) -> tuple[str, str]:
        template_def = TemplateManager.get_template(template_name)
        
        if template_def is None:
            raise ValueError(f"Template '{template_name}' not found")
        
        code_def = template_def['code']
        template = Template(code_def['content'])
        rendered_content = template.render(**context)
        
        return code_def['extension'], rendered_content