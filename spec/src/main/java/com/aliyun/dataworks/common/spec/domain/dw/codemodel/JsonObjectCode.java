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

import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * Code mode for json code
 *
 * @author 聿剑
 * @date 2022/12/28
 */
@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public abstract class JsonObjectCode extends AbstractBaseCode {
    protected transient String rawContent;

    @Override
    public JsonObjectCode parse(String code) {
        this.rawContent = code;
        if (StringUtils.isBlank(code)) {
            return this;
        }

        JsonObjectCode joc = GsonUtils.fromJsonString(code, getClass());
        if (joc == null) {
            return this;
        }
        joc.setRawContent(code);
        return joc;
    }

    @Override
    public void setSourceCode(String sourceCode) {
        this.rawContent = sourceCode;
    }

    @Override
    public String getSourceCode() {
        return rawContent;
    }

    public String getContent() {
        return super.getContent();
    }

    public JsonObjectCode setRawContent(String rawContent) {
        this.rawContent = rawContent;
        return this;
    }
}
