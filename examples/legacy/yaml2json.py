#!/usr/bin/env python3.10
import sys
from datetime import date, datetime

import json
import yaml


def json_serial(obj):
    """JSON serializer for objects not serializable by default json code"""
    if isinstance(obj, (datetime, date)):
        return obj.isoformat()
    raise TypeError("Type %s not serializable" % type(obj))


configuration = None
yl = sys.argv[1]
js = sys.argv[2]
print("yaml: " + yl + ", file: " + js)
with open(yl, 'r') as file:
    configuration = yaml.safe_load(file)

with open(js, 'w') as json_file:
    try:
        json.dump(configuration, json_file, indent=2, default=json_serial, ensure_ascii=False)
    except Exception as e:
        print("error: ", e)
        print("json: ", configuration)
