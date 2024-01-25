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

package com.aliyun.dataworks.common.spec.utils;

import com.aliyun.dataworks.common.spec.SpecUtil;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecRuntimeResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Spec校验器
 *
 * @author 宇洛
 * @date 2023/12/21
 */
public class SpecValidateUtil {
    void validateDataworksWorkflowSpec(String specCode) {
        if (StringUtils.isBlank(specCode)) {
            throw new IllegalArgumentException("specCode is null");
        }
        Specification<DataWorksWorkflowSpec> spec = SpecUtil.parseToDomain(specCode);
        if (spec == null || spec.getSpec() == null) {
            throw new IllegalArgumentException("spec is null");
        }

        if (CollectionUtils.isEmpty(spec.getSpec().getNodes())) {
            throw new IllegalArgumentException("spec nodes is empty");
        }

        for (SpecNode node : spec.getSpec().getNodes()) {
            SpecScript script = node.getScript();
            if (script == null) {
                throw new IllegalArgumentException("script is null");
            }
            if (script.getParameters() != null) {
                for (SpecVariable variable : script.getParameters()) {
                    if (StringUtils.isBlank(variable.getValue())) {
                        throw new SpecException(SpecErrorCode.VALIDATE_ERROR, "variable {" + variable.getName() + "} value not config");
                    }
                }
            }

            if (node.getRuntimeResource() == null) {
                throw new IllegalArgumentException("runtimeResource is null");
            }

            SpecRuntimeResource runtimeResource = node.getRuntimeResource();
            if (StringUtils.isBlank(runtimeResource.getResourceGroupId())) {
                throw new SpecException(SpecErrorCode.VALIDATE_ERROR, "resourceGroupId not config");
            }
        }
    }
}
