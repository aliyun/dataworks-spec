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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.aliyun.dataworks.common.spec.annotation.SpecWriter;
import com.aliyun.dataworks.common.spec.utils.ReflectUtils;
import com.aliyun.dataworks.common.spec.writer.impl.AbstractWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.reflections.Reflections;

/**
 * @author 聿剑
 * @date 2023/8/27
 */
@SuppressWarnings("rawtypes")
@Slf4j
public class WriterFactory {
    private static final Set<Class<? extends AbstractWriter>> writers = new CopyOnWriteArraySet<>();

    /**
     * scan package writer by system class loader instead of Reflections, for the reason of the stackoverflow link:
     * <a href="https://stackoverflow.com/questions/68863307/reflections-throwing-java-lang-illegalstateexception-zip-file-closed">reflections
     * throwing java lang IllegalStateException zip file closed</a>
     *
     * @see Package#getName()
     */
    @SuppressWarnings("unchecked")
    private synchronized static void scanWritersByClassLoader() {
        if (writers.isEmpty()) {
            Set<Class<?>> subTypeClasses = ReflectUtils.getSubTypeOf(Writer.class.getPackage().getName(), AbstractWriter.class);
            SetUtils.emptyIfNull(subTypeClasses).stream()
                .filter(clz -> clz.isAnnotationPresent(SpecWriter.class))
                .forEach(clz -> writers.add((Class<? extends AbstractWriter>)clz));
        }
    }

    private synchronized static void scanWriterByReflections() {
        Reflections reflections = new Reflections(Writer.class.getPackage().getName());
        Set<Class<? extends AbstractWriter>> clzSet = reflections.getSubTypesOf(AbstractWriter.class);
        SetUtils.emptyIfNull(clzSet).stream()
            .filter(AbstractWriter.class::isAssignableFrom)
            .filter(clz -> clz.getAnnotation(SpecWriter.class) != null)
            .forEach(writers::add);
    }

    @SuppressWarnings("unchecked")
    public static Writer getWriter(Class from, SpecWriterContext context) {
        if (writers.isEmpty()) {
            // scanWritersByClassLoader();
            scanWriterByReflections();
        }

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
