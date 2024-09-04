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

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.PaiFlowCode.TaskRelation;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author 聿剑
 * @date 2024/7/1
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class PaiCode extends DefaultJsonFormCode {
    @Data
    @ToString
    public static class Task {
        private String taskName;
        private String taskUniqueCode;
        private String owner;
        private Integer prgType;
        private String paraValueSource;
        private String paraValue;
        private Boolean root;
        private String position;
        private String callbackUrl;
        private String code;
        private Long cloudUuid;
        private String connection;
    }

    private Long appId;
    private String name;
    private String cronExpress;
    private String flowPara;
    private String flowUniqueCode;
    private Long flowId;
    private String callbackUrl;
    private List<String> inputs;
    private List<String> outputs;
    private String paraValue;
    private String prgType;
    private List<TaskRelation> taskRelations;
    private List<Task> tasks;
    private String projectEnv;

    @Override
    public PaiCode parse(String code) {
        return (PaiCode)super.parse(code);
    }

    @Override
    public List<String> getProgramTypes() {
        return Collections.singletonList(CodeProgramType.PAI.name());
    }
}
