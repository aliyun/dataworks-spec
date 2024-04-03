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

import java.util.Collections;
import java.util.List;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Cross tenant node code mode, identity with old cross_tenant_info table
 * <p>
 * sender node code: {"actionType":"send","nodeIdentify":"path/to/node","receiveNodeIdentify":"","receiveTimeout":30,"nodeId":""}
 * receive node code: {"actionType":"receive","nodeIdentify":"","receiveNodeIdentify":"path/to/node","receiveTimeout":60,"nodeId":"123"}
 * </p>
 *
 * @author 聿剑
 * @date 2024/3/14
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CrossTenantCode extends JsonObjectCode {
    @Override
    public List<String> getProgramTypes() {
        return Collections.singletonList(CodeProgramType.CROSS_TENANTS.name());
    }

    @Data
    @ToString
    @EqualsAndHashCode
    @Accessors(chain = true)
    public static class Receiver {
        private String receiverAccount;
        private String receiverProject;
    }

    private String actionType;
    private String nodeIdentify;
    private String receiveNodeIdentify;
    /**
     * 超时时间，单位：min(s)
     */
    private Integer receiveTimeout;
    /**
     * cross_tenant_info表的主键ID
     */
    private String nodeId;
    private List<Receiver> receivers;
}
