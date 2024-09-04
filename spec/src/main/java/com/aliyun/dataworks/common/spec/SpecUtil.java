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

package com.aliyun.dataworks.common.spec;

import java.util.Optional;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;

import com.aliyun.dataworks.common.spec.domain.Spec;
import com.aliyun.dataworks.common.spec.domain.SpecEntity;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.parser.SpecParserFactory;
import com.aliyun.dataworks.common.spec.parser.ToDomainRootParser;
import com.aliyun.dataworks.common.spec.utils.ParserUtil;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import com.aliyun.dataworks.common.spec.writer.WriterFactory;
import com.aliyun.dataworks.common.spec.writer.impl.SpecificationWriter;
import com.google.common.base.Preconditions;

/**
 * @author yiwei.qyw
 * @date 2023/7/6
 */
public class SpecUtil {

    /**
     * Parse json to spec Domain object
     *
     * @param spec Json string
     * @return spec Domain object
     */
    @SuppressWarnings("unchecked")
    public static <T extends Spec> Specification<T> parseToDomain(String spec) {
        if (spec == null) {
            return null;
        }
        return (Specification<T>)new ToDomainRootParser().parseToDomain(spec);
    }

    public static <T extends Spec> String writeToSpec(Specification<T> specification) {
        if (specification == null) {
            return null;
        }

        Preconditions.checkNotNull(specification.getVersion(), "version is null");
        Preconditions.checkNotNull(specification.getSpec(), "spec is null");
        Preconditions.checkNotNull(specification.getKind(), "kind is null");

        SpecWriterContext context = new SpecWriterContext();
        context.setVersion(specification.getVersion());
        SpecificationWriter writer = (SpecificationWriter)WriterFactory.getWriter(specification.getClass(), context);
        if (writer == null) {
            throw new SpecException(SpecErrorCode.PARSER_LOAD_ERROR, "no available registered writer found for type: " + specification.getClass());
        }
        return JSON.toJSONString(writer.write(specification, context), Feature.PrettyFormat);
    }

    @SuppressWarnings("unchecked")
    public static <T> Object write(T specObject, SpecWriterContext context) {
        if (specObject == null) {
            return null;
        }

        return Optional.ofNullable(WriterFactory.getWriter(specObject.getClass(), context))
            .map(writer -> writer.write(specObject, context))
            .orElse(JSON.toJSON(specObject));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends SpecEntity> T parse(String json, Class specCls, SpecParserContext context) {
        Parser<T> parser = (Parser<T>)SpecParserFactory.getParser(specCls.getSimpleName());
        Preconditions.checkNotNull(parser, specCls.getSimpleName() + " parser not found");
        return parser.parse(ParserUtil.jsonToMap(JSON.parseObject(json)), context);
    }
}