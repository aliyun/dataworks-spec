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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author 聿剑
 * @date 2022/12/28
 */
@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode
public class CodeModel<T extends Code> implements Code {
    protected String programType;
    @Getter
    protected T codeModel;

    @Override
    public String getContent() {
        return Optional.ofNullable(codeModel).map(Code::getContent).orElse(null);
    }

    @Override
    public String getRawContent() {
        return Optional.ofNullable(codeModel).map(Code::getRawContent).orElse(null);
    }

    @Override
    public void setSourceCode(String sourceCode) {
        codeModel.setSourceCode(sourceCode);
    }

    @Override
    public String getSourceCode() {
        return codeModel.getSourceCode();
    }

    @Override
    public Code parse(String code) {
        return Optional.ofNullable(codeModel).map(m -> m.parse(code)).orElse(codeModel);
    }

    @Override
    public List<String> getResourceReferences() {
        return Optional.ofNullable(codeModel).map(Code::getResourceReferences).orElse(null);
    }

    @Override
    public Map<String, Object> getTemplate() {
        return codeModel.getTemplate();
    }

    @Override
    public List<String> getProgramTypes() {
        return Collections.singletonList(programType);
    }

    @Override
    public int getClassHierarchyLevel() {
        return 0;
    }
}
