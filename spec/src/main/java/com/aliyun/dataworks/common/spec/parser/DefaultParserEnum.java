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

package com.aliyun.dataworks.common.spec.parser;

/**
 * @author yiwei.qyw
 * @date 2023/7/24
 */

import com.aliyun.dataworks.common.spec.domain.noref.SpecBranches;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecForEach;
import com.aliyun.dataworks.common.spec.domain.noref.SpecNodeRef;
import com.aliyun.dataworks.common.spec.domain.ref.SpecArtifact;
import com.aliyun.dataworks.common.spec.domain.ref.SpecDatasource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFile;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFileResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecFunction;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.common.spec.domain.ref.SpecRuntimeResource;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTable;
import com.aliyun.dataworks.common.spec.domain.ref.SpecTrigger;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import lombok.Getter;

@Getter
public enum DefaultParserEnum {

    /**
     * SpecArtifact class
     */

    SpecArtifact(SpecArtifact.class),

    /**
     * SpecTable class
     */
    SpecTable(SpecTable.class),

    /**
     * SpecNodeOutput class
     */
    SpecNodeOutput(SpecNodeOutput.class),

    /**
     * SpecBranches class
     */
    SpecBranches(SpecBranches.class),

    /**
     * SpecDepend class
     */
    SpecDepend(SpecDepend.class),

    /**
     * SpecFileResource class
     */
    SpecFileResource(SpecFileResource.class),

    /**
     * SpecFlowDepend class
     */
    SpecFlowDepend(SpecFlowDepend.class),

    /**
     * SpecForEach class
     */
    SpecForEach(SpecForEach.class),

    /**
     * SpecFunction class
     */
    SpecFunction(SpecFunction.class),

    /**
     * SpecNodeRef class
     */
    SpecNodeRef(SpecNodeRef.class),

    /**
     * SpecRuntimeResource class
     */
    SpecRuntimeResource(SpecRuntimeResource.class),

    /**
     * Datasource class
     */
    SpecDatasource(SpecDatasource.class),

    /**
     * SpecScript class
     */
    SpecScript(SpecScript.class),

    /**
     * SpecFile class
     */
    SpecFile(SpecFile.class),

    /**
     * SpecScriptRuntime class
     */
    SpecScriptRuntime(SpecScriptRuntime.class),

    /**
     * SpecTrigger class
     */
    SpecTrigger(SpecTrigger.class),

    /**
     * SpecVariable class
     */
    SpecVariable(SpecVariable.class),
    ;
    private final Class<?> specClz;

    DefaultParserEnum(Class<?> specClz) {
        this.specClz = specClz;
    }

}