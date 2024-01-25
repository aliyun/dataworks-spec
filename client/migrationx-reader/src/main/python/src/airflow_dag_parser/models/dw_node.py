from airflow_dag_parser.models.dw_base import DwBase, convert


class DwNode(DwBase):
    def __init__(self):
        self.name = None
        self.type = None
        self.cronExpress = None
        self.owner = None
        self.startRightNow = False
        self.rerunable = False
        self.pauseSchedule = False
        self.code = None
        self.parameter = None
        self.folder = None
        self.inputContexts = []
        self.outputContexts = []
        self.inputs = []
        self.outputs = []
        self.innerNodes = []
        self.ref = None
        self.airflowTask = {}
        self.exception = None

@convert.register(DwNode)
def _(o):
    return o.__dict__
        
