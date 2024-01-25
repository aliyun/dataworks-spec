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

package com.aliyun.dataworks.common.spec.domain.dw.nodemodel;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * @author 聿剑
 * @date 2023/11/11
 */
@Data
@RequiredArgsConstructor
public class DwNodeDependentTypeInfo {
    public static final int NONE = 0;
    public static final int USER_DEFINE = 1;
    public static final int CHILD = 2;
    public static final int SELF = 3;
    public static final int USER_DEFINE_AND_SELF = 13;
    public static final int CHILD_AND_SELF = 23;

    /**
     * NONE(0),
     * USER_DEFINE(1),
     * CHILD(2),
     * SELF(3),
     * USER_DEFINE_AND_SELF(13),
     * CHILD_AND_SELF(23);
     */
    private final int dependentType;
    private final List<Long> dependentNodeIdList;
    private final List<String> dependentNodeOutputList;

    public static DwNodeDependentTypeInfo ofNode() {
        return new DwNodeDependentTypeInfo(NONE, null, null);
    }

    public static DwNodeDependentTypeInfo ofUserDefine(List<Long> dependentNodeOutputList) {
        return ofUserDefine(dependentNodeOutputList, null);
    }

    public static DwNodeDependentTypeInfo ofUserDefine(List<Long> dependentNodeIdList, List<String> dependentNodeOutputList) {
        DwNodeDependentTypeInfo info = new DwNodeDependentTypeInfo(USER_DEFINE, dependentNodeIdList, dependentNodeOutputList);
        return info;
    }

    public static DwNodeDependentTypeInfo ofChild() {
        return new DwNodeDependentTypeInfo(CHILD, null, null);
    }

    public static DwNodeDependentTypeInfo ofSelf() {
        return new DwNodeDependentTypeInfo(SELF, null, null);
    }

    public static DwNodeDependentTypeInfo ofUserDefineAndSelf(List<Long> dependentNodeOutputList) {
        return ofUserDefineAndSelf(dependentNodeOutputList, null);
    }

    public static DwNodeDependentTypeInfo ofUserDefineAndSelf(List<Long> dependentNodeIdList, List<String> dependentNodeOutputList) {
        return new DwNodeDependentTypeInfo(USER_DEFINE_AND_SELF, dependentNodeIdList, dependentNodeOutputList);
    }

    public static DwNodeDependentTypeInfo ofChildAndSelf() {
        return new DwNodeDependentTypeInfo(CHILD_AND_SELF, null, null);
    }
}
