/*
 * Copyright (c) 2024, Alibaba Cloud;
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aliyun.dataworks.migrationx.domain.dataworks.oozie;

/**
 * @author sam.liux
 * @date 2019/04/24
 */
public class OozieConstants {
    public static final String START_NODE = "start";
    public static final String KILL_NODE = "kill";
    public static final String END_NODE = "end";
    public static final String ACTION_NODE = "action";
    public static final String EMAIL_NODE = "email";

    public static final String HIVE_ACTION = "hive";
    public static final String HIVE2_ACTION = "hive2";
    public static final String SQOOP_ACTION = "sqoop";
    public static final String HIVE_OK = "ok";
    public static final String HIVE_ERROR = "error";
    public static final String SCRIPT = "script";
    public static final String PARAM = "param";
    public static final String SHELL_NODE = "shell";
    public static final String SHELL_EXEC = "exec";
    public static final String SHELL_ARGUMENT = "argument";
    public static final String SHELL_ENV_VAR = "env-var";
    public static final String SQOOP_COMMAND = "command";
    public static final String FORK_NODE = "fork";
    public static final String JOIN_NODE = "join";

    public static String wfUser() {
        return "user";
    }
}
