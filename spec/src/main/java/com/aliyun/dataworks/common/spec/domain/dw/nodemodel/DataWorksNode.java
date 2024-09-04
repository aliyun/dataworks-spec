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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.interfaces.Input;
import com.aliyun.dataworks.common.spec.domain.interfaces.Output;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;

/**
 * DataWorks Node Domain Interface
 *
 * @author 聿剑
 * @date 2023/11/9
 */
public interface DataWorksNode {
    /**
     * Get dataworks scheduler node ids by node outputs
     *
     * @param getNodeIdsByOutputs the function to get node ids by node outputs
     * @return the dependent type info
     * @see SpecNodeOutput
     * @see DwNodeDependentTypeInfo
     */
    DwNodeDependentTypeInfo getDependentType(Function<List<SpecNodeOutput>, List<Long>> getNodeIdsByOutputs);

    /**
     * Get dataworks node code
     *
     * @return the node code
     */
    String getCode();

    /**
     * Get dataworks node inputs dependents
     *
     * @return the node inputs
     */
    List<Input> getInputs();

    /**
     * Get dataworks node outputs dependents
     *
     * @return the node outputs
     */
    List<Output> getOutputs();

    /**
     * Get dataworks node input contexts
     *
     * @return the node input contexts
     * @see InputContext
     */
    List<InputContext> getInputContexts();

    /**
     * Get dataworks node output contexts
     *
     * @return the node output contexts
     * @see OutputContext
     */
    List<OutputContext> getOutputContexts();

    /**
     * Get dataworks node para value
     *
     * @return the node para value
     */
    String getParaValue();

    /**
     * * Get dataworks node ext config
     *
     * @return the node ext config
     */
    Map<String, Object> getExtConfig();

    /**
     * get schedule node type
     *
     * @return node type code
     */
    Integer getNodeType();

    /**
     * get program type
     *
     * @param getCodeByTypeName function to convert program type name to code
     * @return program type integer
     * @see CodeProgramType#getCode()
     */
    Integer getPrgType(Function<String, Integer> getCodeByTypeName);
}
