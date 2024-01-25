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

import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2022/12/28
 */
public class CodeModelFactory {
    @SuppressWarnings("unchecked")
    public static <M extends Code> CodeModel<M> getCodeModel(String programType, String content) {
        if (programType == null) {
            return new CodeModel<>();
        }

        CodeModel<M> model = new CodeModel<>();
        model.setProgramType(programType);
        model.setCodeModel((M)parseCodeModel(programType, content));
        return model;
    }

    private static Code parseCodeModel(String programType, String code) {
        if (StringUtils.equalsIgnoreCase(programType, CodeProgramType.CONTROLLER_ASSIGNMENT.name())) {
            return new MultiLanguageScriptingCode().parse(code);
        }

        if (StringUtils.equalsIgnoreCase(programType, CodeProgramType.CONTROLLER_CYCLE_END.name())) {
            return new MultiLanguageScriptingCode().parse(code);
        }

        if (StringUtils.equalsIgnoreCase(programType, CodeProgramType.CONTROLLER_BRANCH.name())) {
            return new ControllerBranchCode().parse(code);
        }

        if (StringUtils.equalsIgnoreCase(programType, CodeProgramType.CONTROLLER_JOIN.name())) {
            return new ControllerJoinCode().parse(code);
        }

        if (StringUtils.equalsIgnoreCase(programType, CodeProgramType.DI.name())) {
            return new DataIntegrationCode().parse(code);
        }

        if (CodeProgramType.matchEngine(programType, CalcEngineType.EMR)) {
            return new EmrCode().parse(code);
        }

        if (StringUtils.equalsIgnoreCase(programType, CodeProgramType.ODPS_SPARK.name())) {
            return new OdpsSparkCode().parse(code);
        }

        return new PlainTextCode().parse(code);
    }
}
