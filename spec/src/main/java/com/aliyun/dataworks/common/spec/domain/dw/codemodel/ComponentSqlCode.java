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
import java.util.Optional;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponentParameter;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.google.gson.annotations.JsonAdapter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Component Sql Code Content
 *
 * @author 聿剑
 * @date 2024/6/6
 * @see CodeProgramType#COMPONENT_SQL
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class ComponentSqlCode extends AbstractBaseCode {
    private String code;
    @JsonAdapter(SqlComponentCode.ComponentAdapter.class)
    private SpecComponent config;
    private ComponentInfo component;

    @Data
    @ToString
    public static class ComponentInfo {
        private Long id;
        private Integer version;
        private String name;
    }

    @Override
    public ComponentSqlCode parse(String content) {
        ComponentSqlCode componentSqlCode;
        try {
            componentSqlCode = Optional.ofNullable(GsonUtils.fromJsonString(content, ComponentSqlCode.class))
                .map(c -> (ComponentSqlCode)c)
                .orElse(new ComponentSqlCode());
        } catch (Exception e) {
            log.warn("parse component sql code error: {}, code: {}", e.getMessage(), content);
            componentSqlCode = new ComponentSqlCode();
        }

        if (StringUtils.isNotBlank(content)) {
            this.code = componentSqlCode.getCode();
            this.config = componentSqlCode.getConfig();
            this.component = componentSqlCode.getComponent();
            this.programType = componentSqlCode.getProgramType();
            this.resourceReferences = componentSqlCode.getResourceReferences();
        }
        return componentSqlCode;
    }

    @Override
    public List<String> getProgramTypes() {
        return Collections.singletonList(CodeProgramType.COMPONENT_SQL.name());
    }

    @Override
    public String getSourceCode() {
        return mergeSqlComponentParamsIntoCode();
    }

    @Override
    public void setSourceCode(String sourceCode) {
        this.code = sourceCode;
    }

    /**
     * 把组件节点的参数合并到组件节点代码中，并返回最终的代码内容
     *
     * @return 合并参数后的代码
     */
    public String mergeSqlComponentParamsIntoCode() {
        if (config == null) {
            return code;
        }

        return renderCode(renderCode(code, config.getInputs()), config.getOutputs());
    }

    public static String renderCode(String code, List<SpecComponentParameter> parameters) {
        if (StringUtils.isBlank(code)) {
            return code;
        }

        try {
            String resultCode = code;
            for (SpecComponentParameter input : ListUtils.emptyIfNull(parameters)) {
                resultCode = StringUtils.replace(resultCode, "@@{" + input.getName() + "}", input.getValue());
            }
            return resultCode;
        } catch (Exception e) {
            log.error("Malformed sql component code content|ErrMsg={}", e.getMessage(), e);
            return code;
        }
    }
}
