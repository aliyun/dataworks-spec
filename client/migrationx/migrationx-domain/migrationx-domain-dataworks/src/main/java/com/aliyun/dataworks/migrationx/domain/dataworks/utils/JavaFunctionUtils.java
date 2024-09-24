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

package com.aliyun.dataworks.migrationx.domain.dataworks.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author 聿剑
 * @date 2022/05/07
 */
@Slf4j
public class JavaFunctionUtils {
    public static <T> Predicate<? super T> withExceptionIgnored(Predicate<? super T> predicate) {
        return (Predicate<T>)t -> {
            try {
                return predicate.test(t);
            } catch (Throwable e) {
                log.warn("ignored exception: ", e);
            }
            return false;
        };
    }

    public static <T> Consumer<T> withResultAndExceptionIgnored(Consumer<T> function) {
        return t -> {
            try {
                function.accept(t);
            } catch (Throwable e) {
                log.warn("ignored exception: ", e);
            }
        };
    }
}
