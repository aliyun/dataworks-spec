from builtins import str

from pyhive import presto
from pyhive.exc import DatabaseError
from requests.auth import HTTPBasicAuth

from airflow.hooks.dbapi_hook import DbApiHook

def get_conn(self):
    """Returns a connection object"""
    db = self.get_connection(self.presto_conn_id)
    reqkwargs = None
    if db.password is not None:
        reqkwargs = {'auth': HTTPBasicAuth(db.login, db.password)}
    conn = {
        "host": db.host,
        "port": db.port,
        "username": db.login,
        "source": db.extra_dejson.get('source', 'airflow'),
        "protocol": db.extra_dejson.get('protocol', 'http'),
        "catalog": db.extra_dejson.get('catalog', 'hive'),
        "schema": db.schema
    }
    return conn
