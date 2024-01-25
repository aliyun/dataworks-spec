import os
import subprocess
import re
import time

from airflow.hooks.base_hook import BaseHook
from airflow.exceptions import AirflowException
from airflow.utils.log.logging_mixin import LoggingMixin

def submit(self, application="", **kwargs):
    """
    Remote Popen to execute the spark-submit job

    :param application: Submitted application, jar or py file
    :type application: str
    :param kwargs: extra arguments to Popen (see subprocess.Popen)
    """
    self.spark_submit_cmd = self._build_spark_submit_command(application)
    pass
