from airflow.models import BaseOperator

class DataWorksNodeOperator(BaseOperator):
    region_id = None
    
    
    def __init__(self):
        super.__init__(BaseOperator)
        pass