from airflow.models.baseoperatorlink import BaseOperatorLink

class DataWorksLink(BaseOperatorLink):
    name = 'DataWorks'
    operators = [DataWorksOperator]
