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

package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.handler;

import java.util.Map;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntity;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

/**
 * EMR节点处理器
 *
 * @author 聿剑
 * @date 2023/12/8
 */
@Slf4j
public class CdhNodeSpecHandler extends BasicNodeSpecHandler {
    @Override
    public boolean support(DwNodeEntity node) {
        return CodeProgramType.matchEngine(node.getType(), CalcEngineType.HADOOP_CDH);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public SpecScriptRuntime toSpecScriptRuntime(DwNodeEntity scr) {
        SpecScriptRuntime runtime = super.toSpecScriptRuntime(scr);

        Optional.ofNullable(scr.getAdvanceSettings())
            .map(advanceSettings -> (Map<String, Object>)GsonUtils.fromJsonString(advanceSettings, new TypeToken<Map<String, Object>>() {}.getType()))
            .ifPresent(runtime::setCdhJobConfig);
        return runtime;
    }
}
