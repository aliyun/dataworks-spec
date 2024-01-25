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
import java.util.Optional;

import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.google.gson.reflect.TypeToken;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 赋值节点代码
 *
 * @author 聿剑
 * @date 2023/9/14
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ToString
public class MultiLanguageScriptingCode extends AbstractBaseCode {
    public static final String LANGUAGE_PYTHON = "python";
    public static final String LANGUAGE_SHELL = "shell";
    public static final String LANGUAGE_ODPS_SQL = "odps";

    @Getter
    @Setter
    private String language;
    private String content;

    @Override
    public Code parse(String code) {
        Type type = new TypeToken<MultiLanguageScriptingCode>() {}.getType();
        MultiLanguageScriptingCode msc = GsonUtils.fromJsonString(code, type);
        Optional.ofNullable(msc).ifPresent(m -> {
            this.content = m.getSourceCode();
            this.language = m.getLanguage();
        });
        return this;
    }

    @Override
    public void setSourceCode(String sourceCode) {
        this.content = sourceCode;
    }

    @Override
    public String getSourceCode() {
        return content;
    }
}
