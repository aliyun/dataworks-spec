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
public enum OozieNodeType {
    /**
     * start
     */
    START(OozieConstants.START_NODE),
    /**
     * kill
     */
    KILL(OozieConstants.KILL_NODE),
    /**
     * end
     */
    END(OozieConstants.END_NODE),
    /**
     * action
     */
    ACTION(OozieConstants.ACTION_NODE),
    /**
     * fork
     */
    FORK(OozieConstants.FORK_NODE),
    /**
     * join
     */
    JOIN(OozieConstants.JOIN_NODE);

    private String type;

    OozieNodeType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static OozieNodeType getOozieNodeType(String type) throws Exception {
        for (OozieNodeType node : values()) {
            if (node.getType().equalsIgnoreCase(type)) {
                return node;
            }
        }
        throw new RuntimeException("unsupported oozie node type: " + type);
    }
}