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

package com.aliyun.dataworks.common.spec.adapter.handler;

import com.aliyun.dataworks.common.spec.adapter.SpecEntityHandler;
import com.aliyun.dataworks.common.spec.adapter.SpecHandlerContext;
import com.aliyun.dataworks.common.spec.domain.SpecEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract entity spec handler
 *
 * @author 聿剑
 * @date 2024/6/17
 */
@Slf4j
public abstract class AbstractEntityHandler<E, T extends SpecEntity> implements SpecEntityHandler<E, T> {
    protected SpecHandlerContext context;

    @Override
    public void setContext(SpecHandlerContext context) {
        this.context = context;
    }

    @Override
    public int getClassHierarchyLevel() {
        Class<?> clz = this.getClass();
        int count = 0;
        do {
            log.debug("clz: {}", clz.getName());
            if (clz.getSuperclass() != null && clz.getSuperclass() != Object.class) {
                clz = clz.getSuperclass();
                count++;
            } else {
                break;
            }
        } while (true);
        return count;
    }
}
