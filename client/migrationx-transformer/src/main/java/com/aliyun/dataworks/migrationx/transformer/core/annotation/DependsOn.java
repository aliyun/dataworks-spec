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

package com.aliyun.dataworks.migrationx.transformer.core.annotation;

import com.aliyun.dataworks.migrationx.transformer.core.controller.Task;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Task的依赖关系注解
 *
 * @author sam.liux
 * @date 2019/08/30
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DependsOn {
    /**
     * Task的标识，用户在tasks map中定位唯一的task
     *
     * @return
     */
    String qualifier() default "";

    /**
     * 根据任务的类型依赖
     *
     * @return
     */
    Class<? extends Task<?>>[] types() default {};
}
