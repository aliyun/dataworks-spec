import MySQLdb
import MySQLdb.cursors
import json
import six

from airflow.hooks.dbapi_hook import DbApiHook


def get_conn(self):
    """
    Returns a mysql connection object
    """
    conn = self.get_connection(self.mysql_conn_id)
    conn_config = {
        "user": conn.login,
        "passwd": conn.password or '',
        "host": conn.host or 'localhost',
        "db": self.schema or conn.schema or ''
    }

    if not conn.port:
        conn_config["port"] = 3306
    else:
        conn_config["port"] = int(conn.port)

    if conn.extra_dejson.get('charset', False):
        conn_config["charset"] = conn.extra_dejson["charset"]
        if (conn_config["charset"]).lower() == 'utf8' or\
           (conn_config["charset"]).lower() == 'utf-8':
            conn_config["use_unicode"] = True
    if conn.extra_dejson.get('cursor', False):
        if (conn.extra_dejson["cursor"]).lower() == 'sscursor':
            conn_config["cursorclass"] = MySQLdb.cursors.SSCursor
        elif (conn.extra_dejson["cursor"]).lower() == 'dictcursor':
            conn_config["cursorclass"] = MySQLdb.cursors.DictCursor
        elif (conn.extra_dejson["cursor"]).lower() == 'ssdictcursor':
            conn_config["cursorclass"] = MySQLdb.cursors.SSDictCursor
    local_infile = conn.extra_dejson.get('local_infile', False)
    if conn.extra_dejson.get('ssl', False):
        # SSL parameter for MySQL has to be a dictionary and in case
        # of extra/dejson we can get string if extra is passed via
        # URL parameters
        dejson_ssl = conn.extra_dejson['ssl']
        if isinstance(dejson_ssl, six.string_types):
            dejson_ssl = json.loads(dejson_ssl)
        conn_config['ssl'] = dejson_ssl
    if conn.extra_dejson.get('unix_socket'):
        conn_config['unix_socket'] = conn.extra_dejson['unix_socket']
    if local_infile:
        conn_config["local_infile"] = 1    
    return conn_config
