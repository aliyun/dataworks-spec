import os
import sys
import subprocess
from pathlib import Path
from typing import Optional


class CodeFileManager:
    @staticmethod
    def read_code(code_file: Path) -> str:
        if not code_file.exists():
            raise FileNotFoundError(f"Code file not found: {code_file}")
        
        with open(code_file, 'r', encoding='utf-8') as f:
            return f.read()

    @staticmethod
    def write_code(code_file: Path, content: str, backup: bool = True) -> Optional[Path]:
        backup_path = None
        
        if code_file.exists() and backup:
            backup_path = code_file.with_suffix(code_file.suffix + '.backup')
            code_file.rename(backup_path)
        
        try:
            with open(code_file, 'w', encoding='utf-8') as f:
                f.write(content)
            
            if backup_path and backup_path.exists():
                backup_path.unlink()
            
            return None
        except Exception as e:
            if backup_path and backup_path.exists():
                backup_path.rename(code_file)
            raise e

    @staticmethod
    def edit_code(code_file: Path) -> bool:
        if not code_file.exists():
            raise FileNotFoundError(f"Code file not found: {code_file}")
        
        editor = os.environ.get('EDITOR')
        
        if not editor:
            if sys.platform == 'win32':
                editor = 'notepad'
            elif sys.platform == 'darwin':
                editor = 'open -e'
            else:
                editor = 'nano'
        
        try:
            if ' ' in editor:
                subprocess.run(f"{editor} {code_file}", shell=True, check=True)
            else:
                subprocess.run([editor, str(code_file)], check=True)
            return True
        except subprocess.CalledProcessError as e:
            raise RuntimeError(f"Failed to open editor: {e}")

    @staticmethod
    def create_code_file(dirpath: Path, filename: str, content: str = "") -> Path:
        code_file = dirpath / filename
        
        with open(code_file, 'w', encoding='utf-8') as f:
            f.write(content)
        
        return code_file
