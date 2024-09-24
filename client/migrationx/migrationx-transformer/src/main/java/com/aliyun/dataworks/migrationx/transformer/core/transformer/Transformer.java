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

package com.aliyun.dataworks.migrationx.transformer.core.transformer;

/**
 * @author 聿剑
 * @date 2023/02/10
 */
public interface Transformer {
    /**
     * initialize
     * @throws Exception ex
     */
    void init() throws Exception;

    /**
     * 加载需要转换的内容
     *
     * @throws Exception loading exception
     */
    void load() throws Exception;

    /**
     * 转换接口
     * @throws Exception transform exception
     */
    void transform() throws Exception;

    /**
     * 生成转换后的内容
     * @throws Exception write exception
     */
    void write() throws Exception;
}
