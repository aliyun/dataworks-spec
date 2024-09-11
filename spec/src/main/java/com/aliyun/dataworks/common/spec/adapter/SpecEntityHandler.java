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

package com.aliyun.dataworks.common.spec.adapter;

import com.aliyun.dataworks.common.spec.domain.SpecEntity;

/**
 * @author 聿剑
 * @date 2024/6/17
 */
public interface SpecEntityHandler<E, T extends SpecEntity> {
    /**
     * set context
     *
     * @param context context
     */
    void setContext(SpecHandlerContext context);

    /**
     * handle
     *
     * @param entity Source Entity
     * @return Spec Entity
     */
    T handle(E entity);

    /**
     * to judge if the entity is supported
     *
     * @param entity entity
     * @return true/false
     */
    boolean support(E entity);

    /**
     * get class hierarchy level
     *
     * @return class hierarchy level
     */
    int getClassHierarchyLevel();
}
