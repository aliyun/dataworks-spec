import pytest
import tempfile
import shutil
from pathlib import Path
from dwcli.pkg.fileops.code import CodeFileManager


class TestCodeFileManager:
    @pytest.fixture
    def temp_dir(self):
        tmpdir = tempfile.mkdtemp()
        yield Path(tmpdir)
        shutil.rmtree(tmpdir)

    def test_create_code_file(self, temp_dir):
        content = "SELECT 1;"
        
        code_file = CodeFileManager.create_code_file(temp_dir, "test.sql", content)
        
        assert code_file.exists()
        assert code_file.read_text() == content

    def test_read_code(self, temp_dir):
        code_file = temp_dir / "test.sql"
        content = "SELECT * FROM table;"
        code_file.write_text(content)
        
        result = CodeFileManager.read_code(code_file)
        
        assert result == content

    def test_read_code_not_found(self, temp_dir):
        code_file = temp_dir / "nonexistent.sql"
        
        with pytest.raises(FileNotFoundError):
            CodeFileManager.read_code(code_file)

    def test_write_code(self, temp_dir):
        code_file = temp_dir / "test.sql"
        original_content = "SELECT 1;"
        code_file.write_text(original_content)
        
        new_content = "SELECT 2;"
        CodeFileManager.write_code(code_file, new_content)
        
        assert code_file.read_text() == new_content

    def test_write_code_creates_backup(self, temp_dir):
        code_file = temp_dir / "test.sql"
        original_content = "SELECT 1;"
        code_file.write_text(original_content)
        
        new_content = "SELECT 2;"
        CodeFileManager.write_code(code_file, new_content, backup=True)
        
        assert code_file.read_text() == new_content
        
        backup_file = temp_dir / "test.sql.backup"
        assert not backup_file.exists()

    def test_write_code_new_file(self, temp_dir):
        code_file = temp_dir / "new.sql"
        content = "SELECT 1;"
        
        CodeFileManager.write_code(code_file, content)
        
        assert code_file.exists()
        assert code_file.read_text() == content
