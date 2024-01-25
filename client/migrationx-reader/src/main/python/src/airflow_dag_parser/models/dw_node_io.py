from airflow_dag_parser.models.dw_base import DwBase, convert

class DwNodeIo(DwBase):
    def __init__(self):
        self.data = None
        self.refTableName = None

@convert.register(DwNodeIo)
def _(o):
    return o.__dict__
