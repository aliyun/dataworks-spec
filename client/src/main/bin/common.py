import os
import sys
import re
import subprocess


def get_cur_path():
    return os.path.abspath(os.path.dirname(__file__))


def get_lib_path(sub_dir):
    cur_path = get_cur_path()
    return os.path.abspath(os.path.join(cur_path, "../lib", sub_dir))


def get_migrationx_home():
    return os.path.abspath(os.path.join(get_cur_path(), ".."))


def get_log_path():
    return os.path.abspath(os.path.join(get_migrationx_home(), "logs"))


def get_conf_path():
    cur_path = get_cur_path()
    return os.path.abspath(os.path.join(cur_path, "../conf"))


def get_jars(app_type):
    jars = []
    for fpath, dirs, fs in os.walk(get_lib_path("common")):
        for f in fs:
            jars.append(os.path.join(fpath, f))

    for fpath, dirs, fs in os.walk(get_lib_path(app_type)):
        for f in fs:
            jars.append(os.path.join(fpath, f))

    jars = list(reversed(jars))
    pull_jars = []
    for jar in pull_jars:
        jars.insert(0, jar)

    return jars


def get_classpath(app_type):
    delimiter = ":"
    if sys.platform.startswith("win32") or sys.platform.startswith("cygwin"):
        delimiter = ";"

    return delimiter.join(get_jars(app_type))


def run_command(app_type, main_class=None):
    log_dir = get_log_path()
    if not os.path.exists(log_dir):
        os.mkdir(log_dir)

    cmd = [
        "java", "-cp", get_classpath(app_type),
        "-DcurrentDir=" + os.path.abspath(os.path.join(get_cur_path(), "..")),
        "-DappType=" + app_type,
        "com.aliyun.migrationx.common.command.appbase.CommandAppEntrance" if not main_class else main_class
    ]

    verbose = True if 'MIGRATIONX_VERBOSE' not in os.environ else bool(os.environ['MIGRATIONX_VERBOSE'])
    if verbose:
        logging_file = os.path.abspath(os.path.join(get_log_path(), app_type + ".log"))
        logback_xml = os.path.abspath(os.path.join(get_conf_path(), "logback.xml"))
        print("Logging file refer to: " + logging_file)
        cmd.insert(3, "-Dlogging.file=" + logging_file)
        cmd.insert(4, "-Dlogging.appenderRef=CONSOLE")
        cmd.insert(5, "-Dlogback.configurationFile=" + logback_xml)

    cmd.extend(sys.argv[1:])
    cmd_stdout_log = sys.stdout if verbose else open(os.path.abspath(os.path.join(log_dir, 'console.log')), 'a')
    cmd_stderr_log = sys.stderr if verbose else open(os.path.abspath(os.path.join(log_dir, 'console.log')), 'a')
    ret = subprocess.call(" ".join(cmd), stdout=cmd_stdout_log, stderr=cmd_stderr_log, shell=True)
    sys.exit(ret)


def replace_os_env_variables(content):
    for env in os.environ.items():
        content = re.sub(r"\$\{" + env[0] + "}", env[1], content, flags=re.IGNORECASE)
    return content
