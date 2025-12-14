import pytest
import json
import tempfile
import shutil
from pathlib import Path
from dwcli.pkg.domain.directory import DirectoryManager, ObjectDirectory


class TestDirectoryManager:
    @pytest.fixture
    def temp_dir(self):
        tmpdir = tempfile.mkdtemp()
        yield Path(tmpdir)
        shutil.rmtree(tmpdir)

    def test_create_directory(self, temp_dir):
        new_dir = temp_dir / "test_obj"
        result = DirectoryManager.create_directory(str(new_dir))
        
        assert result.exists()
        assert result.is_dir()

    def test_create_directory_already_exists(self, temp_dir):
        new_dir = temp_dir / "test_obj"
        new_dir.mkdir()
        
        with pytest.raises(FileExistsError):
            DirectoryManager.create_directory(str(new_dir))

    def test_create_directory_force(self, temp_dir):
        new_dir = temp_dir / "test_obj"
        new_dir.mkdir()
        
        result = DirectoryManager.create_directory(str(new_dir), force=True)
        assert result.exists()

    def test_validate_directory_success(self, temp_dir):
        obj_dir = temp_dir / "test_obj"
        obj_dir.mkdir()
        
        spec_file = obj_dir / "test.schedule.json"
        spec_file.write_text('{"version": "1.0", "kind": "CycleWorkflow"}')
        
        code_file = obj_dir / "test.sql"
        code_file.write_text("SELECT 1;")
        
        result = DirectoryManager.validate_directory(str(obj_dir))
        
        assert isinstance(result, ObjectDirectory)
        assert result.spec_file.resolve() == spec_file.resolve()
        assert result.code_file.resolve() == code_file.resolve()

    def test_validate_directory_no_spec(self, temp_dir):
        obj_dir = temp_dir / "test_obj"
        obj_dir.mkdir()
        
        with pytest.raises(ValueError, match="No.*schedule.json"):
            DirectoryManager.validate_directory(str(obj_dir))

    def test_validate_directory_multiple_specs(self, temp_dir):
        obj_dir = temp_dir / "test_obj"
        obj_dir.mkdir()
        
        (obj_dir / "test1.schedule.json").write_text('{}')
        (obj_dir / "test2.schedule.json").write_text('{}')
        
        with pytest.raises(ValueError, match="Multiple"):
            DirectoryManager.validate_directory(str(obj_dir))

    def test_validate_directory_multiple_code_files(self, temp_dir):
        obj_dir = temp_dir / "test_obj"
        obj_dir.mkdir()
        
        (obj_dir / "test.schedule.json").write_text('{}')
        (obj_dir / "test.sql").write_text("SELECT 1;")
        (obj_dir / "test.sh").write_text("echo hi")
        
        with pytest.raises(ValueError, match="Multiple code files"):
            DirectoryManager.validate_directory(str(obj_dir))

    def test_backup_and_restore(self, temp_dir):
        test_file = temp_dir / "test.json"
        test_file.write_text('{"original": true}')
        
        backup = DirectoryManager.backup_file(test_file)
        
        assert backup.exists()
        assert backup.read_text() == '{"original": true}'
        
        test_file.write_text('{"modified": true}')
        
        DirectoryManager.restore_backup(backup, test_file)
        
        assert test_file.read_text() == '{"original": true}'
        assert not backup.exists()