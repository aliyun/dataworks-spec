import os
import sys
import traceback

from airflow.utils.log.logging_mixin import LoggingMixin
from airflow_dag_parser.dag_parser import DagParser
from airflow_dag_parser.models.dw_workflow import DwWorkflow
from airflow_dag_parser.models.dw_node import DwNode
from airflow_dag_parser.common.supports import Supports
from airflow_dag_parser.converter.converters import getConverter
from airflow_dag_parser.converter.task_converter import BaseTaskConverter


class DagConverter(LoggingMixin):
    def __init__(self, dag):
        self.dag = dag
        pass

    def convert(self):
        workflow = DwWorkflow()
        workflow.name = self.dag.dag_id
        workflow.scheduled = True if self.dag.schedule_interval else False
        for task in self.dag.tasks:
            task_converter = None
            node = DwNode()
            node.name = task.task_id
            node.airflowTask = task
            exception = None
            try:
                task_converter = getConverter(task)
            except Exception as e:
                self.log.error(traceback.format_exc())
                exception = traceback.format_exc()
                task_converter = BaseTaskConverter(task)
            try:
                node = task_converter.convert()
            except Exception as e:
                self.log.error(traceback.format_exc())
                exception = traceback.format_exc()
            node.exception = exception
            workflow.nodes.append(node)
        return workflow
