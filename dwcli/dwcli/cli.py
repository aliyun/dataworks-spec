import sys
import click
from pathlib import Path
from rich.console import Console

from dwcli.pkg.domain.directory import DirectoryManager
from dwcli.pkg.domain.workspace import WorkspaceManager
from dwcli.pkg.jsonpath.manager import JSONPathManager, JSONPathError
from dwcli.pkg.schema.validator import SchemaValidator
from dwcli.pkg.template.manager import TemplateManager
from dwcli.pkg.fileops.code import CodeFileManager


console = Console()


@click.group()
@click.version_option(version="1.0.0")
def cli():
    pass


@cli.group()
def node():
    pass


@node.command()
@click.argument('dirpath', type=click.Path())
@click.option('--name', required=True, help='Object name/ID')
@click.option('--template', default='odps-sql-daily', help='Template name')
@click.option('--owner', default='admin', help='Owner name')
def create(dirpath: str, name: str, template: str, owner: str):
    try:
        from pathlib import Path
        parent_path = Path(dirpath)
        object_path = parent_path / name
        
        path = DirectoryManager.create_directory(str(object_path), force=False)
        
        context = {'name': name, 'owner': owner}
        
        spec_data = TemplateManager.render_spec(template, context)
        
        spec_file = path / f"{name}.schedule.json"
        JSONPathManager.write_json(spec_file, spec_data)
        
        code_result = TemplateManager.render_code(template, context)
        
        # Support for multiple code files (dictionary format)
        if isinstance(code_result, dict):
            code_files = []
            for file_path, (ext, code_content) in code_result.items():
                # Handle subdirectories in file path
                full_path = path / file_path
                if '/' in file_path or '\\' in file_path:
                    # Create subdirectory if needed
                    subdir = full_path.parent
                    subdir.mkdir(parents=True, exist_ok=True)
                code_file = CodeFileManager.create_code_file(full_path.parent, full_path.name, code_content)
                code_files.append(code_file)
            
            console.print(f"[green]✓[/green] Created object directory: {object_path}")
            console.print(f"  Node directory: {name}/")
            console.print(f"  Spec file: {spec_file.name}")
            console.print(f"  Code files: {len(code_files)} file(s)")
            for code_file in code_files[:3]:  # Show first 3
                console.print(f"    - {code_file.relative_to(path)}")
            if len(code_files) > 3:
                console.print(f"    ... and {len(code_files) - 3} more")
        else:
            # Original single file format
            ext, code_content = code_result
            code_file = CodeFileManager.create_code_file(path, f"{name}.{ext}", code_content)
            
            console.print(f"[green]✓[/green] Created object directory: {object_path}")
            console.print(f"  Node directory: {name}/")
            console.print(f"  Spec file: {spec_file.name}")
            console.print(f"  Code file: {code_file.name}")
        
    except Exception as e:
        console.print(f"[red]Error:[/red] {e}", style="bold red")
        sys.exit(1)


@node.command()
@click.argument('dirpath', type=click.Path(exists=True))
@click.argument('assignments', nargs=-1, required=True)
@click.option('--type', 'value_type', help='Value type (string|int|float|bool|json)')
@click.option('--dry-run', is_flag=True, help='Show changes without applying')
def set(dirpath: str, assignments: tuple, value_type: str, dry_run: bool):
    try:
        obj_dir = DirectoryManager.validate_directory(dirpath)
        
        data = JSONPathManager.read_json(obj_dir.spec_file)
        
        original_data = data.copy()
        backup_path = None
        
        if not dry_run:
            backup_path = DirectoryManager.backup_file(obj_dir.spec_file)
        
        try:
            for assignment in assignments:
                if '=' not in assignment:
                    raise ValueError(f"Invalid assignment format: {assignment}. Expected 'path=value'")
                
                path, value = assignment.split('=', 1)
                data = JSONPathManager.set_value(data, path, value, value_type)
                
                console.print(f"[cyan]Set[/cyan] {path} = {value}")
            
            if dry_run:
                console.print("\n[yellow]DRY RUN - No changes applied[/yellow]")
                console.print("\nResulting spec:")
                import json
                console.print(json.dumps(data, indent=2))
                return
            
            JSONPathManager.write_json(obj_dir.spec_file, data)
            
            validator = SchemaValidator()
            is_valid, errors, schema_key = validator.validate(data, obj_dir.spec_file)
            
            if not is_valid:
                DirectoryManager.restore_backup(backup_path, obj_dir.spec_file)
                schema_file = f"{schema_key}.schema.json" if schema_key else None
                validator.print_errors(errors, schema_file)
                sys.exit(1)
            
            DirectoryManager.remove_backup(backup_path)
            console.print("\n[green]✓ Changes applied successfully[/green]")
            
        except Exception as e:
            if backup_path:
                DirectoryManager.restore_backup(backup_path, obj_dir.spec_file)
            raise e
            
    except Exception as e:
        console.print(f"[red]Error:[/red] {e}", style="bold red")
        sys.exit(1)


@node.command()
@click.argument('dirpath', type=click.Path(exists=True))
@click.argument('paths', nargs=-1, required=True)
@click.option('--dry-run', is_flag=True, help='Show changes without applying')
def unset(dirpath: str, paths: tuple, dry_run: bool):
    try:
        obj_dir = DirectoryManager.validate_directory(dirpath)
        
        data = JSONPathManager.read_json(obj_dir.spec_file)
        
        backup_path = None
        
        if not dry_run:
            backup_path = DirectoryManager.backup_file(obj_dir.spec_file)
        
        try:
            for path in paths:
                data = JSONPathManager.unset_value(data, path)
                console.print(f"[cyan]Unset[/cyan] {path}")
            
            if dry_run:
                console.print("\n[yellow]DRY RUN - No changes applied[/yellow]")
                import json
                console.print(json.dumps(data, indent=2))
                return
            
            JSONPathManager.write_json(obj_dir.spec_file, data)
            
            validator = SchemaValidator()
            is_valid, errors, schema_key = validator.validate(data, obj_dir.spec_file)
            
            if not is_valid:
                DirectoryManager.restore_backup(backup_path, obj_dir.spec_file)
                schema_file = f"{schema_key}.schema.json" if schema_key else None
                validator.print_errors(errors, schema_file)
                sys.exit(1)
            
            DirectoryManager.remove_backup(backup_path)
            console.print("\n[green]✓ Changes applied successfully[/green]")
            
        except Exception as e:
            if backup_path:
                DirectoryManager.restore_backup(backup_path, obj_dir.spec_file)
            raise e
            
    except Exception as e:
        console.print(f"[red]Error:[/red] {e}", style="bold red")
        sys.exit(1)


@node.command()
@click.argument('dirpath', type=click.Path(exists=True))
@click.argument('paths', nargs=-1)
@click.option('--output', '-o', type=click.Choice(['json', 'yaml', 'raw']), default='raw')
def inspect(dirpath: str, paths: tuple, output: str):
    try:
        obj_dir = DirectoryManager.validate_directory(dirpath)
        
        data = JSONPathManager.read_json(obj_dir.spec_file)
        
        if not paths:
            paths = ('',)
        
        import json
        
        for path in paths:
            if path:
                value = JSONPathManager.get_value(data, path)
            else:
                value = data
            
            if output == 'json':
                console.print(json.dumps(value, indent=2, ensure_ascii=False))
            elif output == 'yaml':
                import yaml
                console.print(yaml.dump(value, allow_unicode=True))
            else:
                if isinstance(value, (dict, list)):
                    console.print(json.dumps(value, indent=2, ensure_ascii=False))
                else:
                    console.print(str(value))
            
    except Exception as e:
        console.print(f"[red]Error:[/red] {e}", style="bold red")
        sys.exit(1)


@node.command()
@click.argument('dirpath', type=click.Path(exists=True))
def validate(dirpath: str):
    try:
        obj_dir = DirectoryManager.validate_directory(dirpath)
        
        data = JSONPathManager.read_json(obj_dir.spec_file)
        
        validator = SchemaValidator()
        is_valid, errors, schema_key = validator.validate(data, obj_dir.spec_file)
        
        if is_valid:
            console.print(f"[green]✓ Validation passed:[/green] {dirpath}")
        else:
            schema_file = f"{schema_key}.schema.json" if schema_key else None
            validator.print_errors(errors, schema_file)
            sys.exit(1)
            
    except Exception as e:
        console.print(f"[red]Error:[/red] {e}", style="bold red")
        sys.exit(1)


@node.command()
@click.argument('dirpath', type=click.Path(exists=True))
def compile(dirpath: str):
    """
    Compile (validate) a node, a workflow, or a whole workspace recursively.
    """
    try:
        path = Path(dirpath).resolve()
        console.print(f"[cyan]Compiling:[/cyan] {dirpath}")
        
        results = WorkspaceManager.validate_recursive(path)
        
        if not results:
            console.print(f"[yellow]No DataWorks objects or workspace found in {dirpath}[/yellow]")
            return

        failed = False
        for obj_path, is_valid, errors in results:
            try:
                rel_path = obj_path.relative_to(path)
                if str(rel_path) == ".":
                    rel_path = obj_path.name
            except ValueError:
                rel_path = obj_path
                
            if is_valid:
                console.print(f"[green]✓[/green] {rel_path}")
            else:
                console.print(f"[red]✗[/red] {rel_path}")
                for error in errors:
                    console.print(f"  - [red]{error.error_type}:[/red] {error.message} (Path: {error.path})")
                failed = True
        
        if failed:
            console.print("\n[red]Compilation failed.[/red]")
            sys.exit(1)
        else:
            console.print("\n[green]Compilation successful.[/green]")
            
    except Exception as e:
        console.print(f"[red]Error:[/red] {e}", style="bold red")
        sys.exit(1)


@node.group()
def code():
    pass


@code.command()
@click.argument('dirpath', type=click.Path(exists=True))
def edit(dirpath: str):
    try:
        obj_dir = DirectoryManager.validate_directory(dirpath)
        
        if obj_dir.code_file is None:
            console.print("[red]Error:[/red] No code file found in directory", style="bold red")
            sys.exit(1)
        
        CodeFileManager.edit_code(obj_dir.code_file)
        console.print(f"[green]✓[/green] Editor closed")
        
    except Exception as e:
        console.print(f"[red]Error:[/red] {e}", style="bold red")
        sys.exit(1)


@code.command('set')
@click.argument('dirpath', type=click.Path(exists=True))
@click.option('--file', '-f', 'input_file', type=click.Path(exists=True), help='Read content from file')
@click.option('--content', '-c', help='Content string')
def code_set(dirpath: str, input_file: str, content: str):
    try:
        obj_dir = DirectoryManager.validate_directory(dirpath)
        
        if obj_dir.code_file is None:
            console.print("[red]Error:[/red] No code file found in directory", style="bold red")
            sys.exit(1)
        
        if input_file:
            with open(input_file, 'r', encoding='utf-8') as f:
                content = f.read()
        elif content is None:
            if not sys.stdin.isatty():
                content = sys.stdin.read()
            else:
                console.print("[red]Error:[/red] No content provided. Use --file, --content, or pipe to stdin", style="bold red")
                sys.exit(1)
        
        CodeFileManager.write_code(obj_dir.code_file, content)
        console.print(f"[green]✓[/green] Code file updated")
        
    except Exception as e:
        console.print(f"[red]Error:[/red] {e}", style="bold red")
        sys.exit(1)


@code.command('get')
@click.argument('dirpath', type=click.Path(exists=True))
def code_get(dirpath: str):
    try:
        obj_dir = DirectoryManager.validate_directory(dirpath)
        
        if obj_dir.code_file is None:
            console.print("[red]Error:[/red] No code file found in directory", style="bold red")
            sys.exit(1)
        
        content = CodeFileManager.read_code(obj_dir.code_file)
        click.echo(content, nl=False)
        
    except Exception as e:
        console.print(f"[red]Error:[/red] {e}", style="bold red")
        sys.exit(1)


@cli.group()
def workspace():
    pass


@workspace.command()
@click.argument('dirpath', type=click.Path())
@click.option('--name', required=True, help='Workspace name')
@click.option('--owner', default='admin', help='Owner name')
def create(dirpath: str, name: str, owner: str):
    try:
        WorkspaceManager.create_workspace(dirpath, name, owner)
        console.print(f"[green]✓[/green] Created workspace: {dirpath}")
        console.print(f"  Project file: dw-project.json")
        console.print(f"  Directories: node/, workflow/")
    except Exception as e:
        console.print(f"[red]Error:[/red] {e}", style="bold red")
        sys.exit(1)


@workspace.command()
@click.argument('dirpath', type=click.Path(exists=True))
def validate(dirpath: str):
    try:
        path = Path(dirpath).resolve()
        results = WorkspaceManager.validate_recursive(path)
        
        failed = False
        for obj_path, is_valid, errors in results:
            try:
                rel_path = obj_path.relative_to(path)
                if str(rel_path) == ".":
                    rel_path = obj_path.name
            except ValueError:
                rel_path = obj_path
                
            if is_valid:
                console.print(f"[green]✓[/green] {rel_path}")
            else:
                console.print(f"[red]✗[/red] {rel_path}")
                for error in errors:
                    console.print(f"  - [red]{error.error_type}:[/red] {error.message} (Path: {error.path})")
                failed = True
        
        if failed:
            sys.exit(1)
    except Exception as e:
        console.print(f"[red]Error:[/red] {e}", style="bold red")
        sys.exit(1)


def main():
    cli()


if __name__ == '__main__':
    main()