#
# Copyright (c) 2024, Alibaba Cloud;
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# init venv
CUR_PATH=$(cd `dirname $0`; pwd)
source $CUR_PATH/common.sh

log $INFO "clearing .env directory ..."
rm -rf .env

log $INFO "initializing new venv .env directory ..."
major_version=`$PYTHON_CMD -V 2>&1 | awk '{print $NF}' | awk -F. '{print $1}'`

log $INFO "python major version is: $major_version"
if [[ $major_version=="2" ]]; then
  $PYTHON_CMD -m virtualenv .env
else
  $PYTHON_CMD -m venv .env
fi

log $INFO "installing $AIRFLOW_VERSION ..."
.env/bin/pip install $AIRFLOW_VERSION


