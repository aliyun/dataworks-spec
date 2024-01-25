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

package com.aliyun.dataworks.common.spec.domain.dw.codemodel;

import java.util.Map;

import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author sam.liux
 * @date 2020/04/03
 */
@Data
@Accessors(chain = true)
@ToString
public class EmrAllocationSpec {
    private String queue;
    private String vcores;
    private String memory;
    private String priority;
    private String userName;
    @SerializedName("USE_GATEWAY")
    private Boolean useGateway;
    @SerializedName("REUSE_SESSION")
    private Boolean reuseSession;
    @SerializedName("FLOW_SKIP_SQL_ANALYZE")
    private Boolean batchMode;

    public static EmrAllocationSpec of(Map<String, Object> allocateSpec) {
        if (allocateSpec == null) {
            return null;
        }

        return GsonUtils.fromJsonString(GsonUtils.toJsonString(allocateSpec), EmrAllocationSpec.class);
    }

    public Map<String, Object> toMap() {
        return GsonUtils.fromJsonString(GsonUtils.toJsonString(this), new TypeToken<Map<String, Object>>() {}.getType());
    }
}
