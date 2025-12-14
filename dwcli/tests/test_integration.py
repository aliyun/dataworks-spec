import pytest
import json
import tempfile
import shutil
from pathlib import Path
from click.testing import CliRunner
from dwcli.cli import cli


class TestCLIIntegration:
    @pytest.fixture
    def runner(self):
        return CliRunner()

    @pytest.fixture
    def temp_dir(self):
        tmpdir = tempfile.mkdtemp()
        yield Path(tmpdir)
        shutil.rmtree(tmpdir)

    def test_create_command(self, runner, temp_dir):
        obj_dir = temp_dir / "my_tasks" / "my_task"
        
        result = runner.invoke(cli, [
            'node', 'create', str(temp_dir / "my_tasks"),
            '--name', 'my_task',
            '--template', 'odps-sql-daily'
        ])
        
        assert result.exit_code == 0
        assert obj_dir.exists()
        
        spec_file = obj_dir / "my_task.schedule.json"
        assert spec_file.exists()
        
        code_file = obj_dir / "my_task.sql"
        assert code_file.exists()
        
        with open(spec_file) as f:
            spec = json.load(f)
            assert spec['metadata']['name'] == 'my_task'

    def test_set_command(self, runner, temp_dir):
        obj_dir = temp_dir / "my_task"
        obj_dir.mkdir()
        
        spec_file = obj_dir / "test.schedule.json"
        spec_data = {
            "version": "1.0",
            "kind": "CycleWorkflow",
            "spec": {"timeout": 3600}
        }
        with open(spec_file, 'w') as f:
            json.dump(spec_data, f)
        
        code_file = obj_dir / "test.sql"
        code_file.write_text("SELECT 1;")
        
        result = runner.invoke(cli, [
            'node', 'set', str(obj_dir),
            'spec.timeout=7200',
            '--type', 'int'
        ])
        
        assert result.exit_code == 0
        
        with open(spec_file) as f:
            updated = json.load(f)
            assert updated['spec']['timeout'] == 7200

    def test_inspect_command(self, runner, temp_dir):
        obj_dir = temp_dir / "my_task"
        obj_dir.mkdir()
        
        spec_file = obj_dir / "test.schedule.json"
        spec_data = {
            "version": "1.0",
            "spec": {"timeout": 3600}
        }
        with open(spec_file, 'w') as f:
            json.dump(spec_data, f)
        
        code_file = obj_dir / "test.sql"
        code_file.write_text("SELECT 1;")
        
        result = runner.invoke(cli, [
            'node', 'inspect', str(obj_dir),
            'spec.timeout'
        ])
        
        assert result.exit_code == 0
        assert '3600' in result.output

    def test_unset_command(self, runner, temp_dir):
        obj_dir = temp_dir / "my_task"
        obj_dir.mkdir()
        
        spec_file = obj_dir / "test.schedule.json"
        spec_data = {
            "version": "1.0",
            "spec": {"timeout": 3600, "keep": "value"}
        }
        with open(spec_file, 'w') as f:
            json.dump(spec_data, f)
        
        code_file = obj_dir / "test.sql"
        code_file.write_text("SELECT 1;")
        
        result = runner.invoke(cli, [
            'node', 'unset', str(obj_dir),
            'spec.timeout'
        ])
        
        assert result.exit_code == 0
        
        with open(spec_file) as f:
            updated = json.load(f)
            assert 'timeout' not in updated['spec']
            assert updated['spec']['keep'] == 'value'

    def test_code_get_command(self, runner, temp_dir):
        obj_dir = temp_dir / "my_task"
        obj_dir.mkdir()
        
        spec_file = obj_dir / "test.schedule.json"
        spec_file.write_text('{"version": "1.0"}')
        
        code_file = obj_dir / "test.sql"
        code_content = "SELECT * FROM table;"
        code_file.write_text(code_content)
        
        result = runner.invoke(cli, ['node', 'code', 'get', str(obj_dir)])
        
        assert result.exit_code == 0
        assert code_content in result.output

    def test_code_set_command_with_content(self, runner, temp_dir):
        obj_dir = temp_dir / "my_task"
        obj_dir.mkdir()
        
        spec_file = obj_dir / "test.schedule.json"
        spec_file.write_text('{"version": "1.0"}')
        
        code_file = obj_dir / "test.sql"
        code_file.write_text("OLD CONTENT")
        
        new_content = "SELECT 1 FROM dual;"
        result = runner.invoke(cli, [
            'node', 'code', 'set', str(obj_dir),
            '--content', new_content
        ])
        
        assert result.exit_code == 0
        assert code_file.read_text() == new_content

    def test_dry_run_set(self, runner, temp_dir):
        obj_dir = temp_dir / "my_task"
        obj_dir.mkdir()
        
        spec_file = obj_dir / "test.schedule.json"
        spec_data = {"version": "1.0", "spec": {"timeout": 3600}}
        with open(spec_file, 'w') as f:
            json.dump(spec_data, f)
        
        code_file = obj_dir / "test.sql"
        code_file.write_text("SELECT 1;")
        
        result = runner.invoke(cli, [
            'node', 'set', str(obj_dir),
            'spec.timeout=7200',
            '--dry-run'
        ])
        
        assert result.exit_code == 0
        assert 'DRY RUN' in result.output
        
        with open(spec_file) as f:
            unchanged = json.load(f)
            assert unchanged['spec']['timeout'] == 3600

    def test_full_workflow(self, runner, temp_dir):
        obj_dir = temp_dir / "tasks" / "workflow_test"
        
        create_result = runner.invoke(cli, [
            'node', 'create', str(temp_dir / "tasks"),
            '--name', 'workflow_test',
            '--template', 'shell-daily',
            '--owner', 'testuser'
        ])
        assert create_result.exit_code == 0
        
        set_result = runner.invoke(cli, [
            'node', 'set', str(obj_dir),
            'spec.nodes[0].timeout=1800',
            '--type', 'int'
        ])
        assert set_result.exit_code == 0
        
        inspect_result = runner.invoke(cli, [
            'node', 'inspect', str(obj_dir),
            'metadata.owner'
        ])
        assert inspect_result.exit_code == 0
        assert 'testuser' in inspect_result.output
        
        code_result = runner.invoke(cli, [
            'node', 'code', 'set', str(obj_dir),
            '--content', 'echo "Updated script"'
        ])
        assert code_result.exit_code == 0
        
        get_result = runner.invoke(cli, [
            'node', 'code', 'get', str(obj_dir)
        ])
        assert get_result.exit_code == 0
        assert 'Updated script' in get_result.output