#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os
import sys
import json
import common

cur_path = common.get_cur_path()
conf_path = common.get_conf_path()
migrationx_home = common.get_migrationx_home()
os.environ["MIGRATIONX_HOME"] = migrationx_home
migrationx_config_file = os.path.join(conf_path, "migrationx.json")
migrationx_config = json.load(open(migrationx_config_file))

cmd_list = []
if "reader" in migrationx_config:
    reader_bin = os.path.abspath(os.path.join(cur_path, "reader.py"))
    reader_cmd = ["python", reader_bin]
    reader_cmd.extend([common.replace_os_env_variables(param) for param in migrationx_config["reader"]["params"]])
    cmd_list.append(" ".join(reader_cmd))

if "transformer" in migrationx_config:
    transformer_bin = os.path.abspath(os.path.join(cur_path, "transformer.py"))
    transformer_cmd = ["python", transformer_bin]
    transformer_cmd.extend([common.replace_os_env_variables(param) for param in migrationx_config["transformer"]["params"]])
    cmd_list.append(" ".join(transformer_cmd))

if "writer" in migrationx_config:
    writer_bin = os.path.abspath(os.path.join(cur_path, "writer.py"))
    writer_cmd = ["python", writer_bin]
    writer_cmd.extend([common.replace_os_env_variables(param) for param in migrationx_config["writer"]["params"]])
    cmd_list.append(" ".join(writer_cmd))

for cmd in cmd_list:
    print("Running command: " + cmd)
    print("=====================================")
    ret = os.system(cmd)
    if ret != 0:
        print("Command failed, exit with code: " + str(ret))
        sys.exit(ret)

    print("=====================================")
    print("Command success: " + cmd + "\n")
