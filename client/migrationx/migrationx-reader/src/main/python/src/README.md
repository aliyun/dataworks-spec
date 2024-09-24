# 使用方法

1 解压airflow-exporter.tgz

```shell
tar zxvf airflow-exporter.tgz
```

2 设置PTYHONPATH
指定PYTHONPATH到airflow的python lib目录，例如

```shell
export PYTHONPATH=/usr/local/lib/python3.6/site-packages
# 路径只是举例子，实际export以现场环境为准
export AIRFLOW_HOME=/var/lib/airflow
export AIRFLOW_CONFIG=/var/run/cloudera-scm-agent/process/2531-airflow-AIRFLOW_SCHEDULER/airflow.cfg
```

3 执行airflow任务导出

```shell
cd airflow-exporter
python3.6 ./parser -d /path/to/airflow/dag/floder/ -o output.json
```
