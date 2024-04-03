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
import java.util.Map;

/**
 * @author 聿剑
 * @date 2022/12/28
 */
public interface Code {
    /**
     * get serialized code content string,
     * 给调度的节点代码整体完整内容
     *
     * @return code content string
     */
    String getContent();

    /**
     * set user source code
     * 设置用户编辑的代码部分
     *
     * @param sourceCode user code
     */
    void setSourceCode(String sourceCode);

    /**
     * get user source code
     * 获取用户编辑代码内容部分
     *
     * @return user code
     */
    String getSourceCode();

    /**
     * parse code
     *
     * @param code code content
     * @return Code
     */
    Code parse(String code);

    /**
     * ##@resource_reference{xxx}
     * --@resource_reference{xxx}
     *
     * @return List of resource name
     */
    List<String> getResourceReferences();

    /**
     * 节点代码模板
     *
     * @return Map<String, Object>
     */
    Map<String, Object> getTemplate();

    /**
     * 节点代码类型
     *
     * @return List of program type
     */
    List<String> getProgramTypes();

    /**
     * 类集成层级
     *
     * @return level
     */
    int getClassHierarchyLevel();
}
