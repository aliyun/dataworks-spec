import logging
try:
    import json
except Exception as e:
    import simplejson as json
import inspect
import types
import traceback
from datetime import datetime, timedelta
from decimal import Decimal
from airflow.models import BaseOperator, DAG

from logging import Logger
try:
    from functools import singledispatch
except ImportError as e:
    from singledispatch import singledispatch

logger = logging.getLogger(__name__)


@singledispatch
def convert(o):
    type_str = str(type(o))
    if "Variable'" in type_str:
        logger.info(type_str)
        return o.__repr_()
    return type(o)


@convert.register(datetime)
def _(o):
    return o.strftime('%b-%d-%Y %H:%M:%S')


@convert.register(timedelta)
def _(o):
    return ""


@convert.register(BaseOperator)
def _(o):
    d = o.__dict__
    d['operator'] = o.__class__.__name__
    return d


@convert.register(set)
def _(s):
    return list(s)


@convert.register(DAG)
def _(dag):
    return dag.dag_id


@convert.register(Logger)
def _(logger):
    return "Logger"


try:
    from airflow.models.xcom_arg import XComArg

    @convert.register(XComArg)
    def _(xcomarg):
        return xcomarg.__str__()
except ImportError as e:
    logger.warning("ERROR: %s", e)

try:
    from airflow.models import DagRun

    @convert.register(DagRun)
    def _(dagrun):
        return dagrun.__str__()
except ImportError as e:
    logger.warning("ERROR: %s", e)

try:
    from airflow.util.operator_resources import Resource, Resources, CpuResource, RamResource, GpuResource, DiskResource

    @convert.register(Resource)
    def _(res):
        return res.__repr__()

    @convert.register(Resources)
    def _(res):
        return res.__repr__()
except ImportError as e:
    logger.warning("ERROR: %s", e)


class ExtendJSONEncoder(json.JSONEncoder):
    def default(self, obj):
        try:
            if callable(obj):
                return inspect.getsource(obj)

            if inspect.ismodule(obj):
                return obj.__name__
            return convert(obj)
        except TypeError as e:
            traceback.print_exc(e)
            return obj.__str__()


class DwBase(object):
    def __init__(self):
        pass

    def to_json(self):
        return json.dumps(self, cls=ExtendJSONEncoder)
