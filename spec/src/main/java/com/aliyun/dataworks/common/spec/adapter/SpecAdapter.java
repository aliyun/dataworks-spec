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

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.adapter.handler.AbstractEntityHandler;
import com.aliyun.dataworks.common.spec.domain.SpecEntity;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;

/**
 * @author 聿剑
 * @date 2024/6/17
 */
@Slf4j
public class SpecAdapter<E, T extends SpecEntity> {
    private final Set<Class<? extends AbstractEntityHandler<E, T>>> specEntityHandlers = new HashSet<>();
    private Class<? extends AbstractEntityHandler<E, T>> defaultHandler;

    synchronized public void registerHandler(Class<? extends AbstractEntityHandler<E, T>> handler) {
        this.specEntityHandlers.add(handler);
    }

    synchronized public void setDefaultHandler(Class<? extends AbstractEntityHandler<E, T>> defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    /**
     * get spec object handler
     *
     * @param entity source entity instance
     * @return spec handler
     */
    public SpecEntityHandler<E, T> getHandler(E entity, Locale locale) {
        if (entity == null) {
            return null;
        }

        log.info("entity type: {}", entity.getClass());
        SpecHandlerContext context = new SpecHandlerContext();
        context.setSpecAdapter(this);
        context.setLocale(locale);
        List<? extends AbstractEntityHandler<E, T>> matchedNodeSpecHandler = SetUtils.emptyIfNull(this.specEntityHandlers).stream()
                .map(this::newHandlerInstance)
                .peek(handler -> handler.setContext(context))
                .filter(handler -> handler.support(entity))
                .sorted(Comparator.comparing(handler -> -1 * handler.getClassHierarchyLevel()))
                .collect(Collectors.toList());

        log.info("node spec handlers: {}", ListUtils.emptyIfNull(matchedNodeSpecHandler).stream().map(Object::getClass)
                .map(Class::getName).collect(Collectors.toList()));

        Optional<? extends AbstractEntityHandler<E, T>> opt = matchedNodeSpecHandler.stream().findFirst();
        if (opt.isPresent()) {
            log.info("use the first spec handler: {}", opt.get());
            return opt.get();
        }

        AbstractEntityHandler<E, T> handler = newHandlerInstance(defaultHandler);
        handler.setContext(context);
        log.info("use the default spec handler: {}", defaultHandler);
        return handler;
    }

    @SuppressWarnings("unchecked")
    private AbstractEntityHandler<E, T> newHandlerInstance(Class<?> handlerClz) {
        try {
            return (AbstractEntityHandler<E, T>) handlerClz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
