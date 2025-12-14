import os
import json
import shutil
from pathlib import Path
from typing import Optional, Tuple
from dataclasses import dataclass


@dataclass
class ObjectDirectory:
    path: Path
    spec_file: Path
    code_file: Optional[Path]

    @property
    def exists(self) -> bool:
        return self.path.exists()


class DirectoryManager:
    SPEC_SUFFIX = ".schedule.json"

    @staticmethod
    def validate_directory(dirpath: str) -> ObjectDirectory:
        path = Path(dirpath).resolve()
        
        if not path.exists():
            raise FileNotFoundError(f"Directory does not exist: {dirpath}")
        
        if not path.is_dir():
            raise NotADirectoryError(f"Path is not a directory: {dirpath}")
        
        spec_files = list(path.glob(f"*{DirectoryManager.SPEC_SUFFIX}"))
        
        if len(spec_files) == 0:
            raise ValueError(f"No *{DirectoryManager.SPEC_SUFFIX} file found in {dirpath}")
        
        if len(spec_files) > 1:
            raise ValueError(f"Multiple *{DirectoryManager.SPEC_SUFFIX} files found in {dirpath}")
        
        spec_file = spec_files[0]
        
        code_file = DirectoryManager._find_code_file(path, spec_file)
        
        return ObjectDirectory(path=path, spec_file=spec_file, code_file=code_file)

    @staticmethod
    def _find_code_file(dirpath: Path, spec_file: Path) -> Optional[Path]:
        all_files = [f for f in dirpath.iterdir() if f.is_file()]
        code_files = [f for f in all_files if f != spec_file and not f.name.startswith('.')]
        
        if len(code_files) == 0:
            return None
        
        if len(code_files) > 1:
            raise ValueError(f"Multiple code files found in {dirpath}. Expected exactly one.")
        
        return code_files[0]

    @staticmethod
    def create_directory(dirpath: str, force: bool = False) -> Path:
        path = Path(dirpath).resolve()
        
        if path.exists() and not force:
            raise FileExistsError(f"Directory already exists: {dirpath}")
        
        path.mkdir(parents=True, exist_ok=force)
        return path

    @staticmethod
    def backup_file(filepath: Path) -> Path:
        backup_path = filepath.with_suffix(filepath.suffix + '.backup')
        shutil.copy2(filepath, backup_path)
        return backup_path

    @staticmethod
    def restore_backup(backup_path: Path, original_path: Path):
        if backup_path.exists():
            shutil.move(str(backup_path), str(original_path))

    @staticmethod
    def remove_backup(backup_path: Path):
        if backup_path.exists():
            backup_path.unlink()
