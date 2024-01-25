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

CUR_PATH=$(cd `dirname $0`; pwd)
source $CUR_PATH/common.sh

SETUP_ENV=$1
log_error "xxx" $SETUP_ENV $1
if [[ "X$SETUP_ENV" == "X" ]]; then
  SETUP_ENV="false"
fi

export DEPLOY_PATH=$CUR_PATH/deploy
export DEPLOY_FILE=$DEPLOY_PATH/airflow-exporter.tgz
export DEPLOY_BUILD_PATH=$DEPLOY_PATH/airflow-exporter
export AIRFLOW_VERSION="apache-airflow==1.10.1"
export SLUGIFY_USES_TEXT_UNIDECODE=yes
export AIRFLOW_GPL_UNIDECODE=yes
export PYTHON_CMD="python"

log $INFO "Setup env: $SETUP_ENV"
log $INFO "Current path: $CUR_PATH"
log $INFO "Deploy path: $DEPLOY_PATH"
log $INFO "Python version: `$PYTHON_CMD -V 2>&1 | awk '{print $NF}'`"
log $INFO "Airflow version: $AIRFLOW_VERSION"
log $INFO "Environment variable AIRFLOW_GPL_UNIDECODE=$AIRFLOW_GPL_UNIDECODE"
log $INFO "Environment variable SLUGIFY_USES_TEXT_UNIDECODE=$SLUGIFY_USES_TEXT_UNIDECODE"

if [[ "$SETUP_ENV" == "true" ]]; then
  log $INFO "Setup virtual python environment with airflow $AIRFLOW_VERSION"
  $CUR_PATH/setup_env.sh
  log $INFO "Setup virtual python environment success"
fi

log $INFO "Copying source files"
rm -rf $DEPLOY_PATH
mkdir -p $DEPLOY_BUILD_PATH

chmod +x $CUR_PATH/src/parser
cp -r $CUR_PATH/src/* $DEPLOY_BUILD_PATH
cd $DEPLOY_BUILD_PATH

find $DEPLOY_BUILD_PATH -name *.pyc | xargs rm -f
find $DEPLOY_BUILD_PATH -name __pycache__ | xargs rm -rf

log $INFO "Making package file"
tar czf $DEPLOY_FILE .

cd -

log $INFO "Pacakge tgz file: $DEPLOY_FILE"


