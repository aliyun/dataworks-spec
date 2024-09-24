import os
import sys
import json
from jinja2 import meta
from datetime import datetime, timedelta
from airflow.utils.log.logging_mixin import LoggingMixin
from airflow.operators.bash_operator import BashOperator
from airflow.models import DAG, DagBag, TaskInstance
from airflow_dag_parser.dag_parser import DagParser
from airflow_dag_parser.patch.task_instance_patch import get_template_context
from airflow_dag_parser.models.dw_workflow import DwWorkflow
from airflow_dag_parser.models.dw_node import DwNode
from airflow_dag_parser.models.dw_node_io import DwNodeIo
from airflow_dag_parser.models.dw_base import ExtendJSONEncoder
from airflow_dag_parser.common.supports import Supports

'''
basic implementation for airflow task converter
'''


class BaseTaskConverter(LoggingMixin):
    def __init__(self, task):
        self.task = task
        self.task_instance = None

    def convert(self):
        node = DwNode()

        self.log.info("converting task: %s.%s, dag file: %s",
                      self.task.dag.dag_id, self.task.task_id, self.task.dag.full_filepath)
        node.code = self.get_node_code()

        self.task_instance = TaskInstance(
            task=self.task, execution_date=datetime.now())
        # patch self.task_instance.get_template_context
        self.task_instance.get_template_context = get_template_context.__get__(
            self.task_instance, TaskInstance)
        node.parameter = self.get_node_parameters()

        # self.task_instance.render_templates()
        self.log.debug("task dumps: %s", json.dumps(
            self.task, cls=ExtendJSONEncoder))
        node.airflowTask = self.task
        node.name = self.task.task_id
        node.cronExpress = self.get_cron_express()
        node.type = self.get_node_type()
        node.inputs = self.get_node_inputs()
        node.outputs = self.get_node_outputs()
        return node

    def get_node_parameters(self):
        env = self.task.dag.get_template_env() if hasattr(
            self.task.dag, 'get_template_env') else self.task.dag.get_template_env()
        if not env:
            self.log.warn("template env none")
            return None

        parsed_content = env.parse(self.get_node_code())
        variables_set = meta.find_undeclared_variables(parsed_content)
        context = self.task_instance.get_template_context()
        params = []
        for var in variables_set:
            self.log.info("variable: %s, value: %s", var, context[var])
            params.append(
                "=".join([var, json.dumps(context[var], cls=ExtendJSONEncoder)]))

        self.log.info("param: %s", params)
        return " ".join(params)

    def get_node_code(self):
        return None

    def get_node_outputs(self):
        ios = []
        io = DwNodeIo()
        io.data = "_".join([self.task.dag.dag_id, self.task.task_id])
        ios.append(io)
        return ios

    def get_node_inputs(self):
        ios = []
        for upstream in self.task._upstream_task_ids:
            io = DwNodeIo()
            io.data = "_".join([self.task.dag.dag_id, upstream])
            ios.append(io)
        return ios

    def get_cron_express(self):
        dag = self.task.dag
        if not dag.schedule_interval:
            return None

        self.log.debug("dag: %s, schedule_interval: %s, %s" % (
            dag.dag_id, dag.schedule_interval, type(dag.schedule_interval)))
        if type(dag.schedule_interval) == type(""):
            if dag.schedule_interval == "@once":
                return None

            if dag.schedule_interval == "@daily":
                return "day"

            tokens = dag.schedule_interval.split(" ")
            if len(tokens) == 5:
                tokens.insert(0, "0")
            return " ".join(tokens)
        elif type(dag.schedule_interval) == type(timedelta(days=1)):
            return self._timedelta_to_cron(dag.schedule_interval)
        return dag.schedule_interval

    def _timedelta_to_cron(self, delta):
        cron = ["0", "0", "0", "*", "*", "*"]
        minutes = int(delta.total_seconds()) / 60
        steps = [1, 60, 1440, 43200]
        indices = list(reversed(range(len(steps))))
        self.log.debug("indices: %s", indices)
        for i in indices:
            self.log.debug("minutes: %s, steps: %s" % (minutes, steps[i]))
            mini_step = int(minutes / steps[i])
            if mini_step > 0:
                cron[i + 1] = "*/" + str(mini_step)
                break
        return " ".join(cron)

    def get_node_type(self):
        return None
