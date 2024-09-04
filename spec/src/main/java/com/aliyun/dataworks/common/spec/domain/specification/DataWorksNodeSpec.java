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

package com.aliyun.dataworks.common.spec.domain.specification;

import java.util.Collections;
import java.util.List;

import com.aliyun.dataworks.common.spec.domain.Spec;
import com.aliyun.dataworks.common.spec.domain.SpecNoRefEntity;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 聿剑
 * @date 2024/5/20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DataWorksNodeSpec extends SpecNoRefEntity implements Spec {
    private SpecNode node;
    private SpecFlowDepend flow;

    @Override
    public List<SpecKind> getKinds() {
        return Collections.singletonList(SpecKind.NODE);
    }
}
