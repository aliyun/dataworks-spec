import subprocess

from airflow.exceptions import AirflowException
from airflow.hooks.base_hook import BaseHook
from copy import deepcopy

def export_table(self, table, export_dir, input_null_string,
                 input_null_non_string, staging_table,
                 clear_staging_table, enclosed_by,
                 escaped_by, input_fields_terminated_by,
                 input_lines_terminated_by,
                 input_optionally_enclosed_by, batch,
                 relaxed_isolation, extra_export_options=None):
    self.cmd = self._export_cmd(table, export_dir, input_null_string,
                           input_null_non_string, staging_table,
                           clear_staging_table, enclosed_by, escaped_by,
                           input_fields_terminated_by,
                           input_lines_terminated_by,
                           input_optionally_enclosed_by, batch,
                           relaxed_isolation, extra_export_options)
    # fake execute
    # self.Popen(cmd)  
    self.patched = True
  
def import_query(self, query, target_dir, append=False, file_type="text",
                 split_by=None, direct=None, driver=None, extra_import_options=None):
    self.cmd = self._import_cmd(target_dir, append, file_type, split_by, direct, driver, extra_import_options)
    self.cmd += ["--query", query]
    
    # fake execute
    # self.Popen(cmd)
    self.patched = True

def import_table(self, table, target_dir=None, append=False, file_type="text",
                 columns=None, split_by=None, where=None, direct=False,
                 driver=None, extra_import_options=None):
    self.cmd = self._import_cmd(target_dir, append, file_type, split_by, direct, driver, extra_import_options)
    self.cmd += ["--table", table]
    if columns:
        self.cmd += ["--columns", columns]
    if where:
        self.cmd += ["--where", where]
      
    # fake execute
    # self.Popen(cmd)
    self.patched = True
