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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL Component Code Content
 *
 * @author 聿剑
 * @date 2024/6/6
 * @see CodeProgramType#SQL_COMPONENT
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class SqlComponentCode extends AbstractBaseCode {
    private String code;
    @JsonAdapter(ComponentAdapter.class)
    private SpecComponent config;

    protected static class ComponentAdapter implements JsonSerializer<SpecComponent> {
        /**
         * 标准Spec代码中使用inputs/outputs的复数形式，这里兼容老板数据开发代码中的input/output字段名
         *
         * @return content json
         */
        @Override
        public JsonElement serialize(SpecComponent src, Type typeOfSrc, JsonSerializationContext context) {
            JsonElement ele = context.serialize(src);
            if (ele != null && ele.isJsonObject()) {
                if (ele.getAsJsonObject().has("inputs")) {
                    ele.getAsJsonObject().add("input", ele.getAsJsonObject().get("inputs"));
                    ele.getAsJsonObject().remove("inputs");
                }
                if (ele.getAsJsonObject().has("outputs")) {
                    ele.getAsJsonObject().add("output", ele.getAsJsonObject().get("outputs"));
                    ele.getAsJsonObject().remove("outputs");
                }
            }
            return ele;
        }
    }

    @Override
    public SqlComponentCode parse(String content) {
        SqlComponentCode sqlComponentCode;
        try {
            sqlComponentCode = Optional.ofNullable(GsonUtils.fromJsonString(content, SqlComponentCode.class))
                .map(c -> (SqlComponentCode)c)
                .orElse(new SqlComponentCode());
        } catch (Exception e) {
            log.warn("parse sql component code error: {}, code: {}", e.getMessage(), content);
            sqlComponentCode = new SqlComponentCode();
        }

        this.code = sqlComponentCode.getCode();
        this.config = sqlComponentCode.getConfig();
        this.programType = sqlComponentCode.getProgramType();
        this.resourceReferences = sqlComponentCode.getResourceReferences();
        return sqlComponentCode;
    }

    @Override
    public List<String> getProgramTypes() {
        return Collections.singletonList(CodeProgramType.SQL_COMPONENT.name());
    }

    @Override
    public String getSourceCode() {
        return code;
    }

    @Override
    public void setSourceCode(String sourceCode) {
        this.code = sourceCode;
    }
}
