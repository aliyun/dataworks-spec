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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author sam.liux
 * @date 2019/12/24
 */
@ToString(callSuper = true, exclude = {"nodeRef"})
public class DwNodeIo extends NodeIo {
    @JsonIgnore
    private transient Node nodeRef;

    @JsonIgnore
    private String type;

    @JsonIgnore
    private String parentId;

    @JsonIgnore
    private Boolean isDifferentApp;

    public Boolean getIsDifferentApp() {
        return isDifferentApp;
    }

    public void setIsDifferentApp(Boolean differentApp) {
        isDifferentApp = differentApp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Node getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(Node nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public String getUniqueKey() {
        List<String> parts = new ArrayList<>();
        parts.add(nodeRef.getUniqueKey());
        parts.add(getType());
        parts.add(getData());
        if (StringUtils.isNotBlank(parentId)) {
            parts.add(parentId);
        }
        String str = Joiner.on("#").join(parts);
        return UUID.nameUUIDFromBytes(str.getBytes()).toString();
    }
}
