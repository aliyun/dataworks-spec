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

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrAllocationSpec;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrLauncher;
import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.emr.EmrJobExecuteMode;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.emr.EmrJobSubmitMode;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntity;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

/**
 * EMR节点处理器
 *
 * @author 聿剑
 * @date 2023/12/8
 */
@Slf4j
public class EmrNodeSpecHandler extends BasicNodeSpecHandler {
    @Override
    public boolean support(DwNodeEntity dwNode) {
        return CodeProgramType.matchEngine(dwNode.getType(), CalcEngineType.EMR);
    }

    @Override
    public SpecScriptRuntime toSpecScriptRuntime(DwNodeEntity scr) {
        SpecScriptRuntime runtime = super.toSpecScriptRuntime(scr);
        SpecScriptRuntime emrRuntime = new SpecScriptRuntime();
        BeanUtils.copyProperties(runtime, emrRuntime);

        CodeModel<EmrCode> code = CodeModelFactory.getCodeModel(scr.getType(), scr.getCode());
        Map<String, Object> emrJobConfig = Maps.newHashMap();
        Optional.ofNullable(code.getCodeModel()).flatMap(emrCode -> Optional.ofNullable(emrCode.getLauncher()).map(EmrLauncher::getAllocationSpec))
            .ifPresent(allocSpecMap -> {
                EmrAllocationSpec allocSpec = EmrAllocationSpec.of(allocSpecMap);
                emrJobConfig.put("session_enabled", allocSpec.getReuseSession());
                emrJobConfig.put("priority", allocSpec.getPriority());
                emrJobConfig.put("cores", allocSpec.getVcores());
                emrJobConfig.put("memory", allocSpec.getMemory());
                emrJobConfig.put("queue", allocSpec.getQueue());
                emrJobConfig.put("submit_mode", Optional.ofNullable(allocSpec.getUseGateway())
                    .map(useGateway -> useGateway ? EmrJobSubmitMode.LOCAL : EmrJobSubmitMode.YARN));
                emrJobConfig.put("submitter", allocSpec.getUserName());
                emrJobConfig.put("execute_mode", Optional.ofNullable(allocSpec.getBatchMode())
                    .map(batchMode -> batchMode ? EmrJobExecuteMode.BATCH : EmrJobExecuteMode.SINGLE));
            });
        emrRuntime.setEmrJobConfig(emrJobConfig);
        return emrRuntime;
    }

    @Override
    public String toSpecScriptContent(DwNodeEntity dmNodeBO) {
        CodeModel<EmrCode> code = CodeModelFactory.getCodeModel(dmNodeBO.getType(), dmNodeBO.getCode());
        return code.getSourceCode();
    }
}
