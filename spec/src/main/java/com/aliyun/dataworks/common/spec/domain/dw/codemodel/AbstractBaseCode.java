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

import java.util.List;
import java.util.Map;

import com.aliyun.dataworks.common.spec.utils.ClassUtils;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author 聿剑
 * @date 2022/12/28
 */
@Data
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
public abstract class AbstractBaseCode implements Code {
    protected List<String> resourceReferences;

    @Override
    public String getContent() {
        return GsonUtils.toJsonString(this);
    }

    @Override
    public List<String> getResourceReferences() {
        return resourceReferences;
    }

    @Override
    public String getSourceCode() {
        return getContent();
    }

    @Override
    public void setSourceCode(String sourceCode) {

    }

    @Override
    public Map<String, Object> getTemplate() {
        Code c = parse(getContent());
        c.setSourceCode("");
        String content = c.getContent();
        return GsonUtils.fromJsonString(content, new TypeToken<Map<String, Object>>() {}.getType());
    }

    @Override
    public int getClassHierarchyLevel() {
        return ClassUtils.getClassHierarchyLevel(this);
    }
}
