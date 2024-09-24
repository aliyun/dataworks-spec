import os
import sys
import csv

from airflow.utils.log.logging_mixin import LoggingMixin
try: 
    from airflow.models.connection import Connection
except Exception as e:
    from airflow.models import Connection

class CsvConnections(LoggingMixin):
    def __init__(self, csv):
        self.csv_file = csv
        self.rows = []
        self.connections = {}

    def load(self):
        with open(self.csv_file) as fd:
            rd = csv.DictReader(fd)
            self.rows = [row for row in rd]
            for row in self.rows:
                key = row['conn_id']
                conn = Connection(
                    conn_id=row['conn_id'],
                    conn_type=row['conn_type'],
                    host=row['host'],
                    login=row['login'],
                    password=row['password'],
                    schema=row['schema'],
                    port=row['port'],
                    extra=row['extra']
                )
                self.connections[key] = conn
        pass

    def get_connection(self, conn_id):
        if conn_id not in self.connections:
            return None
            
        return [self.connections[conn_id]]

