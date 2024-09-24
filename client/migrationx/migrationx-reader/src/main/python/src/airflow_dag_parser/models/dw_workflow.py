from airflow_dag_parser.models.dw_base import DwBase, convert

class DwWorkflow(DwBase):
    def __init__(self):
        self.name = None
        self.scheduled = False
        self.parameters = []
        self.nodes = []
        self.resources = []
        self.functions = []
        self.tables = []
        self.ref = None

@convert.register(DwWorkflow)
def _(o):
    return o.__dict__
