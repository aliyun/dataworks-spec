import os
import re
import sys
import json
import logging

from datetime import datetime
from airflow.utils.log.logging_mixin import LoggingMixin
from airflow.hooks.base_hook import BaseHook
from airflow.models import DAG, DagBag, TaskInstance

from airflow_dag_parser.patch.task_instance_patch import get_template_context
from airflow_dag_parser.patch.base_hook_patch import get_connections
from airflow_dag_parser.converter.task_converter import BaseTaskConverter
from airflow_dag_parser.models.dw_workflow import DwWorkflow
from airflow_dag_parser.models.dw_node import DwNode
from airflow_dag_parser.models.dw_node_io import DwNodeIo
from airflow_dag_parser.common.supports import Supports
from airflow_dag_parser.common.datax import datax_json_to_di_code

BaseHook._get_connections_from_db = get_connections
BaseHook.connections = None

logger = logging.getLogger(__name__)


def getConverter(task):
    converter = BaseTaskConverter(task)
    clz = Supports.find_operator_class(task)
    if clz:
        for sub_clz in BaseTaskConverter.__subclasses__():
            logger.debug("clz: %s, sub_clz: %s",
                         clz.__name__, sub_clz.__name__)
            op_clz = sub_clz.get_operator_class()
            if op_clz and clz.__name__ == op_clz.__name__:
                converter = sub_clz(task)
                return converter

    logger.warning("no unsupported converter implementation for operator type: %s, path: %s, use default converter",
                   task.__class__.__name__, task.dag.full_filepath)
    return converter


class BashOperatorTaskConverter(BaseTaskConverter, LoggingMixin):
    node_type = "DIDE_SHELL"
    parameters = None

    @classmethod
    def get_operator_class(cls):
        if Supports.is_operator_available("BashOperator"):
            from airflow.operators.bash_operator import BashOperator
            return BashOperator
        return None

    def __init__(self, task):
        super(BashOperatorTaskConverter, self).__init__(task)
        pass

    def get_node_code(self):
        code = self.task.bash_command
        # for hive -f / beeline -f
        if self._is_hive_run_sql_file_command(code):
            code = self._process_hive_run_sql_file_command(code)
        if self._is_datax_command(code):
            code = self._process_datax_command(code)
        return code

    def get_node_parameters(self):
        if self.parameters:
            return self.parameters

        return super(BashOperatorTaskConverter, self).get_node_parameters()

    def _process_hive_run_sql_file_command(self, code):
        tokens = code.split(" ")
        for i in range(len(tokens)):
            if tokens[i] == '-f':
                if os.path.exists(tokens[i+1]) and len(tokens) > i + 1:
                    tokens[i+1] = '"' + self._read_file(tokens[i+1]) + '"'
                    tokens[i] = '-e'
        return ' '.join(tokens)

    def _process_datax_command(self, code):
        tokens = code.split(' ')
        json_file = tokens[-1]
        if os.path.exists(json_file):
            self.node_type = 'DI'
            params = []
            for i in range(len(tokens)):
                if tokens[i].startswith('-D'):
                    params.append(tokens[i][2:])
            if params:
                self.parameters = ' '.join(params)
            json_str = self._read_file(json_file)
            json_str = re.sub(':\\s+(\\$\\{\\w+\\})', r': "\1"', json_str)
            js = None
            try:
                js = json.loads(json_str)
            except:
                js = eval(json_str)
            finally:
                pass
            di_js = datax_json_to_di_code(js)
            return json.dumps(di_js)

        return code

    def _is_hive_run_sql_file_command(self, code):
        if not code:
            return False

        return (code.startswith("hive ") or code.startswith("beeline ")) and " -f " in code

    def _is_datax_command(self, code):
        if not code:
            return False

        tokens = code.split(' ')
        if len(tokens) == 0:
            return False

        if code.startswith('python '):
            if len(tokens) > 1 and tokens[1].endswith("datax.py"):
                return True
        else:
            if tokens[0].endswith('datax.py'):
                return True
        return False

    def get_node_type(self):
        return self.node_type

    def _read_file(self, file_path):
        if not os.path.exists(file_path):
            return file_path

        return open(file_path).read()


class PythonOperatorTaskConverter(BaseTaskConverter, LoggingMixin):
    node_type = "PYODPS"

    @classmethod
    def get_operator_class(cls):
        if Supports.is_operator_available("PythonOperator"):
            from airflow.operators.python_operator import PythonOperator
            return PythonOperator
        return None

    def __init__(self, task):
        super(PythonOperatorTaskConverter, self).__init__(task)
        pass

    def get_node_code(self):
        code = self.task.python_callable
        return code

    def get_node_type(self):
        return self.node_type


class HiveOperatorTaskConverter(BaseTaskConverter, LoggingMixin):
    @classmethod
    def get_operator_class(cls):
        if Supports.is_operator_available("HiveOperator"):
            from airflow.operators.hive_operator import HiveOperator
            return HiveOperator
        return None

    def __init__(self, task):
        super(HiveOperatorTaskConverter, self).__init__(task)
        pass

    def get_node_code(self):
        return self.task.hql

    def get_node_type(self):
        return "EMR_HIVE"


class PrestoToMySqlTransferTaskConverter(BaseTaskConverter, LoggingMixin):
    @classmethod
    def get_operator_class(cls):
        if Supports.is_operator_available("PrestoToMySqlTransfer"):
            from airflow.operators.presto_to_mysql import PrestoToMySqlTransfer
            return PrestoToMySqlTransfer
        return None

    def __init__(self, task):
        super(PrestoToMySqlTransferTaskConverter, self).__init__(task)
        pass

    def get_node_code(self):
        self.log.info(self.task.__dict__)
        from airflow_dag_parser.patch.presto_to_mysql_operator_patch import execute
        from airflow_dag_parser.patch.mysql_hook_patch import get_conn
        from airflow_dag_parser.patch.presto_hook_patch import get_conn as presto_get_conn
        from airflow.hooks.mysql_hook import MySqlHook
        from airflow.hooks.presto_hook import PrestoHook
        MySqlHook.get_conn = get_conn
        PrestoHook.get_conn = presto_get_conn
        PrestoToMySqlTransfer.execute = execute
        self.task.execute(self.task_instance.get_template_context())
        code = {
            "type": 'PrestoToMySqlTransfer',
            "mysql": {
                "connection": self.task.mysql.get_conn(),
                "mysql_table": self.task.mysql_table
            },
            "sql": self.task.sql,
            "presto": self.task.presto.get_conn()
        }
        return json.dumps(code, intent=4)

    def get_node_type(self):
        return "DI"


class HiveToMySqlTransferTaskConverter(BaseTaskConverter, LoggingMixin):
    @classmethod
    def get_operator_class(cls):
        if Supports.is_operator_available("HiveToMySqlTransfer"):
            from airflow.operators.hive_to_mysql import HiveToMySqlTransfer
            return HiveToMySqlTransfer
        return None

    def __init__(self, task):
        super(HiveToMySqlTransferTaskConverter, self).__init__(task)
        pass

    def get_node_code(self):
        self.log.info(self.task.hiveserver2_conn_id)
        self.log.info(self.task.sql)
        self.log.info(self.task.mysql_table)
        self.log.info(self.task.mysql_preoperator)
        self.log.info(self.task.mysql_postoperator)
        self.log.info(self.task.mysql_conn_id)
        return self.task.sql

    def get_node_type(self):
        return "DI"


class SparkSqlOperatorTaskConverter(BaseTaskConverter, LoggingMixin):
    @classmethod
    def get_operator_class(cls):
        if Supports.is_operator_available("SparkSqlOperator"):
            from airflow.contrib.operators.spark_sql_operator import SparkSqlOperator
            return SparkSqlOperator
        return None

    def __init__(self, task):
        super(SparkSqlOperatorTaskConverter, self).__init__(task)
        pass

    def get_node_code(self):
        self.log.info("sparkt sql: %s" % self.task._sql)
        return self.task._sql

    def get_node_type(self):
        return "EMR_SPARK_SQL"


class SparkSubmitOperatorTaskConverter(BaseTaskConverter, LoggingMixin):
    @classmethod
    def get_operator_class(cls):
        if Supports.is_operator_available("SparkSubmitOperator"):
            from airflow.contrib.operators.spark_submit_operator import SparkSubmitOperator
            return SparkSubmitOperator
        return None

    def __init__(self, task):
        super(SparkSubmitOperatorTaskConverter, self).__init__(task)
        pass

    def get_node_code(self):
        from airflow_dag_parser.patch.spark_submit_hook_patch import submit
        from airflow.contrib.hooks.spark_submit_hook import SparkSubmitHook
        SparkSubmitHook.submit = submit
        self.task.execute(self.task_instance.get_template_context())
        code = " ".join(self.task._hook.spark_submit_cmd)
        self.log.info("spark submit: %s" % code)
        return code

    def get_node_type(self):
        return "EMR_SPARK"


class SqoopOperatorTaskConverter(BaseTaskConverter, LoggingMixin):
    @classmethod
    def get_operator_class(cls):
        if Supports.is_operator_available("SqoopOperator"):
            from airflow.contrib.operators.sqoop_operator import SqoopOperator
            return SqoopOperator
        return None

    def __init__(self, task):
        super(SqoopOperatorTaskConverter, self).__init__(task)
        pass

    def get_node_code(self):
        from airflow_dag_parser.patch.sqoop_hook_patch import import_table, export_table, import_query
        from airflow.contrib.hooks.sqoop_hook import SqoopHook
        SqoopHook.import_table = import_table
        SqoopHook.export_table = export_table
        SqoopHook.import_query = import_query
        self.task.execute(self.task_instance.get_template_context())
        self.log.info("sqoop cmd: %s" % self.task.hook.cmd)
        return " ".join(self.task.hook.cmd)

    def get_node_type(self):
        return "EMR_SQOOP"


class ExternalTaskSensorConverter(BaseTaskConverter, LoggingMixin):
    @classmethod
    def get_operator_class(cls):
        if Supports.is_operator_available("ExternalTaskSensor"):
            from airflow.sensors.external_task_sensor import ExternalTaskSensor
            return ExternalTaskSensor
        return None

    def __init__(self, task):
        super(ExternalTaskSensorConverter, self).__init__(task)
        pass

    def get_node_inputs(self):
        inputs = super(ExternalTaskSensorConverter, self).get_node_inputs()
        if not inputs:
            inputs = []

        if not self.task.external_dag_id or not self.task.external_task_id:
            return inputs

        external_io = DwNodeIo()
        external_io.data = "_".join(
            [self.task.external_dag_id, self.task.external_task_id])
        inputs.append(external_io)
        return inputs

    def get_node_type(self):
        return "VIRTUAL"
