import json
import shutil
from pathlib import Path
from typing import List, Optional, Tuple, Dict, Any
from dataclasses import dataclass

from dwcli.pkg.domain.directory import DirectoryManager, ObjectDirectory
from dwcli.pkg.template.manager import TemplateManager
from dwcli.pkg.jsonpath.manager import JSONPathManager
from dwcli.pkg.schema.validator import SchemaValidator

@dataclass
class WorkspaceProject:
    path: Path
    project_file: Path
    
    @property
    def exists(self) -> bool:
        return self.project_file.exists()

class WorkspaceManager:
    PROJECT_FILE = "dw-project.json"
    
    @staticmethod
    def create_workspace(dirpath: str, name: str, owner: str = "admin", force: bool = False) -> WorkspaceProject:
        path = Path(dirpath).resolve()
        if path.exists() and not force and any(path.iterdir()):
             raise FileExistsError(f"Directory already exists and is not empty: {dirpath}")
        
        path.mkdir(parents=True, exist_ok=True)
        
        # Create subdirectories
        (path / "node").mkdir(exist_ok=True)
        (path / "workflow").mkdir(exist_ok=True)
        
        # Render project spec
        context = {"name": name, "owner": owner}
        # Note: We need to ensure TemplateManager can find workspace.json
        # For now, let's just write it directly or use a helper if TemplateManager supports it.
        # TemplateManager.render_spec usually looks in templates/
        project_data = TemplateManager.render_spec("workspace", context)
        
        project_file = path / WorkspaceManager.PROJECT_FILE
        JSONPathManager.write_json(project_file, project_data)
        
        return WorkspaceProject(path=path, project_file=project_file)

    @staticmethod
    def is_workspace(dirpath: str) -> bool:
        path = Path(dirpath).resolve()
        return (path / WorkspaceManager.PROJECT_FILE).exists()

    @staticmethod
    def is_object(dirpath: str) -> bool:
        """Check if a directory is a DataWorks object (node/workflow)."""
        path = Path(dirpath).resolve()
        if not path.is_dir():
            return False
        spec_files = list(path.glob(f"*{DirectoryManager.SPEC_SUFFIX}"))
        return len(spec_files) == 1

    @staticmethod
    def get_workspace(dirpath: str) -> WorkspaceProject:
        path = Path(dirpath).resolve()
        project_file = path / WorkspaceManager.PROJECT_FILE
        if not project_file.exists():
            raise FileNotFoundError(f"Not a workspace: {dirpath} (Missing {WorkspaceManager.PROJECT_FILE})")
        return WorkspaceProject(path=path, project_file=project_file)

    @staticmethod
    def find_objects_recursive(path: Path) -> List[Path]:
        """Find all object directories recursively under the given path."""
        objects = []
        
        # If the path itself is an object, add it
        if WorkspaceManager.is_object(str(path)):
            objects.append(path)
            # Short-circuit: usually we don't have objects inside objects 
            # unless it's a specific container like ForEach which we'll handle by 
            # NOT short-circuiting and letting it recurse if needed.
        
        # Recurse into subdirectories
        for d in path.iterdir():
            if d.is_dir() and not d.name.startswith('.'):
                objects.extend(WorkspaceManager.find_objects_recursive(d))
                
        return objects

    @staticmethod
    def validate_recursive(start_path: Path) -> List[Tuple[Path, bool, List[Any]]]:
        """Validate a workspace, a workflow, or a node recursively."""
        results = []
        validator = SchemaValidator()
        
        # 1. If it's a workspace, validate the project file
        if WorkspaceManager.is_workspace(str(start_path)):
            project_file = start_path / WorkspaceManager.PROJECT_FILE
            project_data = JSONPathManager.read_json(project_file)
            is_valid, errors, _ = validator.validate(project_data, project_file)
            results.append((project_file, is_valid, errors))
        
        # 2. Find and validate all objects under this path
        # If start_path is a single node, find_objects_recursive will return [start_path]
        for obj_dir in WorkspaceManager.find_objects_recursive(start_path):
            try:
                obj = DirectoryManager.validate_directory(str(obj_dir))
                data = JSONPathManager.read_json(obj.spec_file)
                is_valid, errors, _ = validator.validate(data, obj.spec_file)
                results.append((obj_dir, is_valid, errors))
            except Exception as e:
                from dwcli.pkg.schema.validator import ValidationErrorDetail
                error = ValidationErrorDetail(
                    error_type="Directory Error",
                    path=".",
                    line_number=None,
                    message=str(e),
                    fix_command=None
                )
                results.append((obj_dir, False, [error]))
                
        return results
