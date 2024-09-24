import sys

import copy
import functools
import getpass
import hashlib
import logging
import os
import signal
import time
from datetime import timedelta

import dill
from sqlalchemy import Column, String, Float, Integer, PickleType, Index, func
from sqlalchemy.orm import reconstructor

from airflow import configuration, settings
from airflow.exceptions import (
    AirflowException, AirflowTaskTimeout, AirflowSkipException, AirflowRescheduleException
)
from airflow.models import DagRun, TaskInstance, TaskReschedule, Variable
from airflow.utils import timezone
from airflow.utils.email import send_email
from airflow.utils.helpers import is_container
from airflow.utils.log.logging_mixin import LoggingMixin
from airflow.utils.net import get_hostname
from airflow.utils.sqlalchemy import UtcDateTime
from airflow.utils.state import State
from airflow.utils.timeout import timeout

from subprocess import Popen, STDOUT, PIPE
from datetime import datetime

import airflow
from airflow.models import DAG, DagBag, TaskInstance
from airflow.operators.bash_operator import BashOperator
from airflow.operators.http_operator import SimpleHttpOperator

'''
patch method for TaskInstance.get_template_context method.
No need to provide_session for get_template_context
'''


def get_template_context(self, session=None):
    task = self.task
    from airflow import macros
    tables = None
    if 'tables' in task.params:
        tables = task.params['tables']

    params = {}
    run_id = ''
    dag_run = DagRun(
        dag_id=self.task.dag.dag_id,
        run_id='dag_parser_' + datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
        execution_date=datetime.now(),
        start_date=datetime.now(),
        state=State.RUNNING
    )

    ds = self.execution_date.strftime('%Y-%m-%d')
    ts = self.execution_date.isoformat()
    yesterday_ds = (self.execution_date - timedelta(1)).strftime('%Y-%m-%d')
    tomorrow_ds = (self.execution_date + timedelta(1)).strftime('%Y-%m-%d')

    # For manually triggered dagruns that aren't run on a schedule, next/previous
    # schedule dates don't make sense, and should be set to execution date for
    # consistency with how execution_date is set for manually triggered tasks, i.e.
    # triggered_date == execution_date.
    if dag_run and dag_run.external_trigger:
        prev_execution_date = self.execution_date
        next_execution_date = self.execution_date
    else:
        prev_execution_date = task.dag.previous_schedule(self.execution_date)
        next_execution_date = task.dag.following_schedule(self.execution_date)

    next_ds = None
    next_ds_nodash = None
    if next_execution_date:
        next_ds = next_execution_date.strftime('%Y-%m-%d')
        next_ds_nodash = next_ds.replace('-', '')

    prev_ds = None
    prev_ds_nodash = None
    if prev_execution_date:
        prev_ds = prev_execution_date.strftime('%Y-%m-%d')
        prev_ds_nodash = prev_ds.replace('-', '')

    ds_nodash = ds.replace('-', '')
    ts_nodash = self.execution_date.strftime('%Y%m%dT%H%M%S')
    ts_nodash_with_tz = ts.replace('-', '').replace(':', '')
    yesterday_ds_nodash = yesterday_ds.replace('-', '')
    tomorrow_ds_nodash = tomorrow_ds.replace('-', '')

    ti_key_str = "{dag_id}__{task_id}__{ds_nodash}".format(
        dag_id=task.dag_id, task_id=task.task_id, ds_nodash=ds_nodash)

    if task.params:
        params.update(task.params)

    if configuration.getboolean('core', 'dag_run_conf_overrides_params'):
        self.overwrite_params_with_dag_run_conf(params=params, dag_run=dag_run)

    class VariableAccessor:
        """
        Wrapper around Variable. This way you can get variables in templates by using
        {var.value.your_variable_name}.
        """

        def __init__(self):
            self.var = None

        def __getattr__(self, item):
            self.var = Variable.get(item)
            return self.var

        def __repr__(self):
            return str(self.var)

    class VariableJsonAccessor:
        """
            Wrapper around deserialized Variables. This way you can get variables
            in templates by using {var.json.your_variable_name}.
            """

        def __init__(self):
            self.var = None

        def __getattr__(self, item):
            self.var = Variable.get(item, deserialize_json=True)
            return self.var

        def __repr__(self):
            return str(self.var)

    return {
        'dag': task.dag,
        'ds': ds,
        'next_ds': next_ds,
        'next_ds_nodash': next_ds_nodash,
        'prev_ds': prev_ds,
        'prev_ds_nodash': prev_ds_nodash,
        'ds_nodash': ds_nodash,
        'ts': ts,
        'ts_nodash': ts_nodash,
        'ts_nodash_with_tz': ts_nodash_with_tz,
        'yesterday_ds': yesterday_ds,
        'yesterday_ds_nodash': yesterday_ds_nodash,
        'tomorrow_ds': tomorrow_ds,
        'tomorrow_ds_nodash': tomorrow_ds_nodash,
        'END_DATE': ds,
        'end_date': ds,
        'dag_run': dag_run,
        'run_id': run_id,
        'execution_date': self.execution_date,
        'prev_execution_date': prev_execution_date,
        'next_execution_date': next_execution_date,
        'latest_date': ds,
        'macros': macros,
        'params': params,
        'tables': tables,
        'task': task,
        'task_instance': self,
        'ti': self,
        'task_instance_key_str': ti_key_str,
        'conf': configuration,
        'test_mode': self.test_mode,
        'var': {
            'value': VariableAccessor(),
            'json': VariableJsonAccessor()
        },
        'inlets': task.inlets,
        'outlets': task.outlets,
    }
    pass
