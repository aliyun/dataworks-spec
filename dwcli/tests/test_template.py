import pytest
from dwcli.pkg.template.manager import TemplateManager


class TestTemplateManager:
    def test_list_templates(self):
        templates = TemplateManager.list_templates()
        
        assert 'odps-sql-daily' in templates
        assert 'shell-daily' in templates
        assert 'python-daily' in templates
        assert 'manual-workflow' in templates

    def test_get_template_exists(self):
        template = TemplateManager.get_template('odps-sql-daily')
        
        assert template is not None
        assert 'spec' in template
        assert 'code' in template

    def test_get_template_not_exists(self):
        template = TemplateManager.get_template('nonexistent')
        
        assert template is None

    def test_render_spec(self):
        context = {'name': 'my_task', 'owner': 'admin'}
        
        spec = TemplateManager.render_spec('odps-sql-daily', context)
        
        assert spec['metadata']['name'] == 'my_task'
        assert spec['metadata']['owner'] == 'admin'
        assert spec['spec']['nodes'][0]['id'] == 'my_task'
        assert spec['spec']['nodes'][0]['script']['path'] == 'my_task.sql'

    def test_render_code_sql(self):
        context = {'name': 'my_task'}
        
        ext, content = TemplateManager.render_code('odps-sql-daily', context)
        
        assert ext == 'sql'
        assert 'my_task' in content
        assert 'SELECT 1' in content

    def test_render_code_shell(self):
        context = {'name': 'my_task'}
        
        ext, content = TemplateManager.render_code('shell-daily', context)
        
        assert ext == 'sh'
        assert 'my_task' in content
        assert '#!/bin/bash' in content

    def test_render_code_python(self):
        context = {'name': 'my_task'}
        
        ext, content = TemplateManager.render_code('python-daily', context)
        
        assert ext == 'py'
        assert 'my_task' in content
        assert 'if __name__' in content

    def test_render_manual_workflow(self):
        context = {'name': 'manual_task', 'owner': 'user1'}
        
        spec = TemplateManager.render_spec('manual-workflow', context)
        
        assert spec['kind'] == 'ManualWorkflow'
        assert spec['metadata']['name'] == 'manual_task'

    def test_render_with_default_owner(self):
        context = {'name': 'my_task'}
        
        spec = TemplateManager.render_spec('odps-sql-daily', context)
        
        assert spec['metadata']['owner'] == 'admin'

    def test_render_invalid_template(self):
        with pytest.raises(ValueError):
            TemplateManager.render_spec('invalid', {'name': 'test'})
