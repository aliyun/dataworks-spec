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

package com.aliyun.dataworks.migrationx.transformer.dataworks.converter;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Asset;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.transformer.core.report.Reportable;

import java.util.List;

/**
 * @author sam.liux
 * @date 2019/04/24
 */
public interface WorkflowConverter extends Reportable {
    /**
     * 转换接口
     *
     * @param asset
     * @return
     * @throws Exception
     */
    List<DwWorkflow> convert(Asset asset) throws Exception;
}
