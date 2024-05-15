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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.fastjson2.JSON;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * json表单默认代码类型
 * - getContent() 获取发给调度的代码内容
 * - getExtraContent() 获取其他额外配置内容
 *
 * @author 聿剑
 * @date 2024/4/12
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DefaultJsonFormCode extends JsonObjectCode implements JsonFormCode {
    public static final String FIELD_EXTRA_CONTENT = "extraContent";
    public static final String FIELD_CONTENT = "content";

    @Override
    public List<String> getProgramTypes() {
        return Stream.of(CodeProgramType.HOLOGRES_SYNC_DATA, CodeProgramType.HOLOGRES_SYNC_DDL)
            .map(CodeProgramType::name).distinct().collect(Collectors.toList());
    }

    public DefaultJsonFormCode setExtraContent(String extraContent) {
        Optional.ofNullable(JSON.parseObject(rawContent)).ifPresent(js -> {
            js.put(FIELD_EXTRA_CONTENT, extraContent);
            rawContent = JSON.toJSONString(js);
        });
        return this;
    }

    public DefaultJsonFormCode setContent(String content) {
        Optional.ofNullable(JSON.parseObject(rawContent)).ifPresent(js -> {
            js.put(FIELD_CONTENT, content);
            rawContent = JSON.toJSONString(js);
        });
        return this;
    }

    public String getExtraContent() {
        if (StringUtils.isBlank(rawContent)) {
            return null;
        }

        return Optional.ofNullable(JSON.parseObject(rawContent))
            .map(js -> js.getString(FIELD_EXTRA_CONTENT))
            .orElse(null);
    }

    @Override
    public String getContent() {
        if (StringUtils.isBlank(rawContent)) {
            return null;
        }

        return Optional.ofNullable(JSON.parseObject(rawContent))
            .map(js -> js.getString(FIELD_CONTENT))
            .orElse(null);
    }

    @Override
    public String getRawContent() {
        return rawContent;
    }
}
