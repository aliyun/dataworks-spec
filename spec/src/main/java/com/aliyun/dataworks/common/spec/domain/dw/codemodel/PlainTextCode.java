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

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Code mode for plain text
 *
 * @author 聿剑
 * @date 2022/12/28
 */
@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class PlainTextCode extends AbstractBaseCode {
    private String content;

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public PlainTextCode parse(String code) {
        content = code;
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

    @Override
    public Map<String, Object> getTemplate() {
        return null;
    }
}
