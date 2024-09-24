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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v5;

import java.util.Date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 新版数据开发快照对象
 *
 * @author 聿剑
 * @date 2024/5/28
 */
@Data
@Builder
@EqualsAndHashCode
public class DataSnapshot {
    private String uuid;
    private Date gmtCreate;
    private Date gmtModified;
    private String entityType;
    private String entityName;
    private String entityUuid;
    private String type;
    private String content;
    private String submitterId;
    private String submitterName;
    private String submitRemark;
    private String namespace;
    private Integer version;

    @Data
    @Builder
    @EqualsAndHashCode
    public static class DataSnapshotContent {
        private String spec;
        private String content;

        public static DataSnapshotContent of(String snapShotContent) {
            JSONObject json = JSON.parseObject(snapShotContent, JSONObject.class);
            if (json == null) {
                return null;
            }

            return DataSnapshotContent.builder()
                .spec(json.getString("spec"))
                .content(json.getString("content"))
                .build();
        }
    }
}
