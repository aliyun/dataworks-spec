from airflow.hooks.mysql_hook import MySqlHook
from airflow.hooks.presto_hook import PrestoHook
from airflow.models import BaseOperator
from airflow.utils.decorators import apply_defaults

def execute(self, context):
    self.presto = PrestoHook(presto_conn_id=self.presto_conn_id)
    self.log.info("Extracting data from Presto: %s", self.sql)
    # results = presto.get_records(self.sql)

    self.mysql = MySqlHook(mysql_conn_id=self.mysql_conn_id)
    
    # self.log.info("Inserting rows into MySQL")
    # mysql.insert_rows(table=self.mysql_table, rows=results)
