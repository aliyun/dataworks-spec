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

package com.aliyun.dataworks.common.spec.writer;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import com.aliyun.dataworks.common.spec.writer.impl.AbstractWriter;
import org.reflections.Reflections;

/**
 * @author 聿剑
 * @date 2023/8/27
 */
@SuppressWarnings("rawtypes")
public class WriterFactory {
    private static final Set<Class<? extends AbstractWriter>> writers = new HashSet<>();

    static {
        Reflections reflections = new Reflections(Writer.class.getPackage().getName());
        Set<Class<? extends AbstractWriter>> domains = reflections.getSubTypesOf(AbstractWriter.class);
        writers.addAll(domains);
    }

    @SuppressWarnings("unchecked")
    public static Writer getWriter(Class from, SpecWriterContext context) {
        return writers.stream()
            .map(w -> {
                try {
                    return w.getConstructor(SpecWriterContext.class).newInstance(context);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            })
            .filter(w -> w.matchType(from))
            .findFirst().orElse(null);
    }
}
