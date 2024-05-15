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

package com.aliyun.dataworks.common.spec.domain.dw.codemodel;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.reflections.Reflections;

/**
 * @author 聿剑
 * @date 2022/12/28
 */
@Slf4j
public class CodeModelFactory {
    @SuppressWarnings("unchecked")
    public static <M extends Code> CodeModel<M> getCodeModel(String programType, String content) {
        if (programType == null) {
            return new CodeModel<>();
        }

        CodeModel<M> model = new CodeModel<>();
        model.setProgramType(programType);
        model.setCodeModel((M)parseCodeModel(programType, content));
        return model;
    }

    private static Code parseCodeModel(String programType, String code) {
        Reflections reflections = new Reflections(Code.class.getPackage().getName());
        List<? extends AbstractBaseCode> list = reflections.getSubTypesOf(AbstractBaseCode.class).stream()
            .filter(subType -> !Modifier.isAbstract(subType.getModifiers()) && !Modifier.isInterface(subType.getModifiers()))
            .map(subType -> {
                try {
                    return subType.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            })
            .filter(inst -> ListUtils.emptyIfNull(inst.getProgramTypes()).stream().anyMatch(t -> t.equalsIgnoreCase(programType)))
            .collect(Collectors.toList());
        log.debug("code model list: {}", list);
        Code theOne = list.stream().max(Comparator.comparing(AbstractBaseCode::getClassHierarchyLevel))
            .map(inst -> inst.parse(code))
            .orElseGet(() -> new PlainTextCode().parse(code));
        log.debug("code model: {}", theOne);
        return theOne;
    }
}
