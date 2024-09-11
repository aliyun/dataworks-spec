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

package com.aliyun.dataworks.common.spec.domain.dw.nodemodel;

import java.util.Collections;
import java.util.List;

import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.interfaces.Output;
import com.aliyun.dataworks.common.spec.domain.ref.InputOutputWired;
import com.aliyun.dataworks.common.spec.domain.ref.ScriptWired;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;

/**
 * @author 聿剑
 * @date 2024/7/17
 */
@Getter
public class SpecEntityDelegate<T extends SpecRefEntity> implements InputOutputWired, ScriptWired {
    private final T object;

    public SpecEntityDelegate(T object) {
        this.object = object;
    }

    public String getId() {
        return object.getId();
    }

    public SpecScript getScript() {
        return object instanceof ScriptWired ? ((ScriptWired)object).getScript() : null;
    }

    @Override
    public List<Input> getInputs() {
        return object instanceof InputOutputWired ?
            Collections.unmodifiableList(ListUtils.emptyIfNull(((InputOutputWired)object).getInputs())) : Collections.emptyList();
    }

    @Override
    public List<Output> getOutputs() {
        return object instanceof InputOutputWired ?
            Collections.unmodifiableList(ListUtils.emptyIfNull(((InputOutputWired)object).getOutputs())) : Collections.emptyList();
    }
}
