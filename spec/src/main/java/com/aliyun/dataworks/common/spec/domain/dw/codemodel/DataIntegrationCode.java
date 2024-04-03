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

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.utils.GsonUtils;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Code mode for Data Integration
 *
 * @author 聿剑
 * @date 2022/12/28
 */
@Data
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DataIntegrationCode extends AbstractBaseCode implements JsonFormCode {
    private transient JsonObject jsonObject;

    @Override
    public DataIntegrationCode parse(String code) {
        jsonObject = GsonUtils.fromJsonString(code, JsonObject.class);
        return this;
    }

    @Override
    public Map<String, Object> getTemplate() {
        return null;
    }

    @Override
    public List<String> getProgramTypes() {
        return Collections.singletonList(CodeProgramType.DI.name());
    }

    @Override
    public String getContent() {
        if (this.jsonObject == null) {
            return null;
        }

        return GsonUtils.defaultGson.toJson(jsonObject);
    }
}
