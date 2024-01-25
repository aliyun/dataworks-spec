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
public enum OozieActionType {
    HIVE(OozieConstants.HIVE_ACTION),
    HIVE2(OozieConstants.HIVE2_ACTION),
    SQOOP(OozieConstants.SQOOP_ACTION),
    OK(OozieConstants.HIVE_OK),
    ERROR(OozieConstants.HIVE_ERROR),
    EMAIL(OozieConstants.EMAIL_NODE),
    SHELL(OozieConstants.SHELL_NODE);

    private String action;

    OozieActionType(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public static OozieActionType getOozieActionType(String action) throws Exception {
        for (OozieActionType oozieActionType : values()) {
            if (oozieActionType.getAction().equalsIgnoreCase(action)) {
                return oozieActionType;
            }
        }
        throw new Exception("unsupported oozie action type: " + action);
    }
}
