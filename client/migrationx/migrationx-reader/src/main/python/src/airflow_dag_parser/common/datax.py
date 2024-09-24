import os
import sys
import json
import copy

_DI_CODE_JSON = {
    "type": "job",
    "steps": [],
    "version": "2.0",
    "order": {
        "hops": [
            {
                "from": "Reader",
                "to": "Writer"
            }
        ]
    },

    "setting": {
        "executeMode": None,
        "errorLimit": {
            "record": ""
        },
        "speed": {
            "concurrent": 2,
            "throttle": False
        }
    }    
}

def datax_json_to_di_code(datax_json):
    if "job" in datax_json:
        datax_json = datax_json["job"]

    res_json = copy.deepcopy(_DI_CODE_JSON)
    if "setting" in datax_json:
        res_json["setting"] = datax_json["setting"]
    
    if "content" in datax_json:
        content_arr = datax_json["content"]
        if len(content_arr) > 0:
            content = content_arr[0]
            if "reader" in content:
                reader = content["reader"]
                name = reader["name"]
                reader["name"] = "Reader"
                reader["category"] = "reader"
                reader["stepType"] = name.replace("reader", "")
                res_json["steps"].append(reader)
            if "writer" in content:
                writer = content["writer"]
                name = writer["name"]
                writer["name"] = "Writer"
                writer["category"] = "writer"
                writer["stepType"] = name.replace("writer", "")
                res_json["steps"].append(writer)
            return res_json
    return datax_json
