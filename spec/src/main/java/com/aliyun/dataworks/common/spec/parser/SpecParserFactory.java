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

package com.aliyun.dataworks.common.spec.parser;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.aliyun.dataworks.common.spec.parser.impl.DefaultSpecParser;
import com.aliyun.dataworks.common.spec.parser.impl.SpecParser;
import org.reflections.Reflections;

/**
 * Load parsers
 *
 * @author yiwei.qyw
 * @date 2023/7/7
 */
public class SpecParserFactory {
    private static final HashMap<String, Parser<?>> parserMap = new HashMap<>();

    static {
        // load default parsers
        loadDefaultParser();
        // load all parsers
        @SuppressWarnings("rawtypes")
        Set<Class<? extends Parser>> parsers = new Reflections(SpecParser.class.getPackage().getName()).getSubTypesOf(Parser.class);
        for (Class<?> clazz : parsers) {
            try {
                Parser<?> parser = (Parser<?>)clazz.getDeclaredConstructor().newInstance();
                // load parser key type to parserMap
                if (parser.getClass().getSuperclass() == DefaultSpecParser.class) {
                    parserMap.put(((DefaultSpecParser<?>)parser).getParameterizedObjectType().getSimpleName(), parser);
                    continue;
                }

                if (parser.getKeyType() != null) {
                    parserMap.put(parser.getKeyType(), parser);
                }

                // set Parser‘s Class type to parserMap
                Arrays.stream(parser.getClass().getGenericInterfaces())
                    .filter(type -> type instanceof ParameterizedType)
                    .map(type -> (ParameterizedType)type)
                    .findFirst()
                    .flatMap(specType -> Arrays.stream(specType.getActualTypeArguments())
                        .filter(Objects::nonNull)
                        .findFirst()
                        .filter(type -> type instanceof Class)
                        .map(type -> (Class<?>)type))
                    .ifPresent(clz -> parserMap.put(clz.getSimpleName(), parser));

            } catch (Exception e) {
                throw new SpecException(e, SpecErrorCode.PARSER_LOAD_ERROR, "load parser error: " + e.getMessage());
            }
        }

    }

    private static void loadDefaultParser() {
        DefaultParserEnum[] values = DefaultParserEnum.values();
        Arrays.stream(values)
            .forEach(v -> parserMap.put(v.getSpecClz().getSimpleName(), new DefaultSpecParser<>(v.getSpecClz())));
    }

    public static Parser<?> getParser(String parserName) {
        return parserMap.get(parserName);
    }

}