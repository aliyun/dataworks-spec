import airflow
import traceback
import logging

from datetime import datetime
from airflow.models import DAG, DagBag, TaskInstance
from airflow.utils.log.logging_mixin import LoggingMixin

logger = logging.getLogger(__name__)
OPERATORS = {}
try:
    from airflow.operators.bash_operator import BashOperator
    OPERATORS[BashOperator] = BashOperator
except ImportError as e:
    logger.warning(e)

try:
    from airflow.operators.hive_operator import HiveOperator
    OPERATORS[HiveOperator] = HiveOperator
except ImportError as e:
    logger.warning(e)

try:
    from airflow.operators.http_operator import SimpleHttpOperator
    OPERATORS[SimpleHttpOperator] = SimpleHttpOperator
except ImportError as e:
    logger.warning(e)

try:
    from airflow.operators.presto_to_mysql import PrestoToMySqlTransfer
    OPERATORS[PrestoToMySqlTransfer] = PrestoToMySqlTransfer
except ImportError as e:
    logger.warning(e)

try:
    from airflow.operators.hive_to_mysql import HiveToMySqlTransfer
    OPERATORS[HiveToMySqlTransfer] = HiveToMySqlTransfer
except ImportError as e:
    logger.warning(e)

try:
    from airflow.contrib.operators.spark_sql_operator import SparkSqlOperator
    OPERATORS[SparkSqlOperator] = SparkSqlOperator
except ImportError as e:
    logger.warning(e)

try:
    from airflow.contrib.operators.spark_submit_operator import SparkSubmitOperator
    OPERATORS[SparkSubmitOperator] = SparkSubmitOperator
except ImportError as e:
    logger.warning(e)

try:
    from airflow.contrib.operators.sqoop_operator import SqoopOperator
    OPERATORS[SqoopOperator] = SqoopOperator
except ImportError as e:
    logger.warning(e)

try:
    from airflow.contrib.operators.python_operator import PythonOperator
    OPERATORS[PythonOperator] = PythonOperator
except ImportError as e:
    logger.warning(e)

try:
    from airflow.sensors.external_task_sensor import ExternalTaskSensor
    OPERATORS[ExternalTaskSensor] = ExternalTaskSensor
except ImportError as e:
    logger.warning(e)

logger.info("supported operators: %s", OPERATORS)


class Supports(LoggingMixin):
    @classmethod
    def is_operator_available(cls, operator_clz):
        res = [clz for clz in OPERATORS if clz.__name__ == operator_clz]
        if len(res) > 0:
            return res[0]
        return None

    @classmethod
    def is_operator_supported(cls, task):
        return cls.find_operator_class(task) != None

    @classmethod
    def find_operator_class(cls, task):
        res = [clz for clz in OPERATORS if clz.__name__ ==
               task.__class__.__name__]
        if len(res) > 0:
            return res[0]
        return None
