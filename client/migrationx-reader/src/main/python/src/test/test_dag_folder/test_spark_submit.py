# -*- coding: utf-8 -*-
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

"""
### Tutorial Documentation
Documentation that goes along with the Airflow tutorial located
[here](https://airflow.apache.org/tutorial.html)
"""
from datetime import timedelta

import airflow
from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from airflow.operators.hive_operator import HiveOperator
from airflow.operators.hive_to_mysql import HiveToMySqlTransfer
from airflow.contrib.operators.spark_submit_operator import SparkSubmitOperator
from airflow.contrib.operators.spark_sql_operator import SparkSqlOperator
from airflow.contrib.operators.sqoop_operator import SqoopOperator

# These args will get passed on to each operator
# You can override them on a per-task basis during operator initialization
default_args = {
    'owner': 'sam.liux',
    'depends_on_past': False,
    'start_date': airflow.utils.dates.days_ago(2),
    'email': ['airflow@example.com'],
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
    # 'queue': 'bash_queue',
    # 'pool': 'backfill',
    # 'priority_weight': 10,
    # 'end_date': datetime(2016, 1, 1),
    # 'wait_for_downstream': False,
    # 'dag': dag,
    # 'sla': timedelta(hours=2),
    # 'execution_timeout': timedelta(seconds=300),
    # 'on_failure_callback': some_function,
    # 'on_success_callback': some_other_function,
    # 'on_retry_callback': another_function,
    # 'trigger_rule': 'all_success'
}

dag = DAG(
    'test_spark_submit',
    default_args=default_args,
    description='A simple demo for hive operator',
    schedule_interval=timedelta(minutes=1),
)

t1 = BashOperator(
    task_id='print_date',
    bash_command='date',
    dag=dag,
)

spark = SparkSubmitOperator(
    task_id='spark_submit_job',
    application='/usr/lib/hadoop-current/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.2.jar',
    application_args=[
        'wordcount',
        '/tmp/input',
        '/tmp/output'
    ],
    spark_binary="sparky",
    dag=dag
)

spark_sql = SparkSqlOperator(
    task_id='spark_sql_job',
    sql='select * from tbl_test',
    dag=dag    
)

sqoop_config = {
    'conn_id': 'sqoop_default',
    'cmd_type': 'export',
    'table': 'target_table',
    'query': 'SELECT * FROM schema.table',
    'target_dir': '/path/on/hdfs/to/import',
    'append': True,
    'file_type': 'avro',
    'columns': 'a,b,c',
    'num_mappers': 22,
    'split_by': 'id',
    'export_dir': '/path/on/hdfs/to/export',
    'input_null_string': '\n',
    'input_null_non_string': '\t',
    'staging_table': 'target_table_staging',
    'clear_staging_table': True,
    'enclosed_by': '"',
    'escaped_by': '\\',
    'input_fields_terminated_by': '|',
    'input_lines_terminated_by': '\n',
    'input_optionally_enclosed_by': '"',
    'batch': True,
    'relaxed_isolation': True,
    'direct': True,
    'driver': 'com.microsoft.jdbc.sqlserver.SQLServerDriver',
    'create_hcatalog_table': True,
    'hcatalog_database': 'hive_database',
    'hcatalog_table': 'hive_table',
    'properties': {
        'mapred.map.max.attempts': '1'
    },
    'extra_import_options': {
        'hcatalog-storage-stanza': "\"stored as orcfile\"",
        'show': ''
    },
    'extra_export_options': {
        'update-key': 'id',
        'update-mode': 'allowinsert',
        'fetch-size': 1
    }
}

sqoop_job = SqoopOperator(
    task_id='sqoop_job',
    dag=dag,
    **sqoop_config
)

sqoop_import = SqoopOperator(
    task_id='sqoop_import_with_partition',
    cmd_type='import',
    conn_id='sqoop_default',
    table='company',
    verbose=True,
    num_mappers=None,
    hcatalog_database='default',
    hcatalog_table='import_table_3',
    create_hcatalog_table=True,
    extra_import_options={
        'hcatalog-storage-stanza': "\"stored as orcfile\"",
        'hive-partition-key': 'day',
        'hive-partition-value': '2017-10-18',
        'fetch-size': 1
    },
    dag=dag
)

t1 >> spark >> spark_sql >> sqoop_job
t1 >> sqoop_import
