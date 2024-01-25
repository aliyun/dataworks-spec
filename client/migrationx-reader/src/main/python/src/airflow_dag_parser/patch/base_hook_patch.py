import os
import random
from typing import Iterable

from airflow.models import Connection
from airflow.exceptions import AirflowException
from airflow.utils.db import provide_session
from airflow.utils.log.logging_mixin import LoggingMixin
from airflow_dag_parser.connections import CsvConnections

@classmethod
def get_connections(cls, conn_id):  # type: (str) -> Iterable[Connection]
    csv  = os.environ.get("connections_csv")

    if not csv and not cls.connections:
        return None

    if not cls.connections:
        cls.connections = CsvConnections(csv)
        cls.connections.load()
    
    return cls.connections.get_connection(conn_id)
