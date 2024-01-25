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

package com.aliyun.dataworks.common.spec.writer.impl;

import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.annotation.SpecWriter;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.noref.SpecForEach;
import com.aliyun.dataworks.common.spec.parser.impl.SpecForEachParser;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import com.aliyun.dataworks.common.spec.writer.WriterFactory;

/**
 * ForEachWriter
 *
 * @author 聿剑
 * @date 2023/10/30
 * @see com.aliyun.dataworks.common.spec.domain.noref.SpecForEach
 */
@SpecWriter
public class SpecForEachWriter extends DefaultJsonObjectWriter<SpecForEach> {
    public SpecForEachWriter(SpecWriterContext context) {
        super(context);
    }

    @Override
    public JSONObject write(SpecForEach specObj, SpecWriterContext context) {
        DataWorksWorkflowSpec constructedSpec = new DataWorksWorkflowSpec();
        constructedSpec.setNodes(specObj.getNodes());
        constructedSpec.setFlow(specObj.getFlow());

        DataWorksWorkflowSpecWriter writer = (DataWorksWorkflowSpecWriter)WriterFactory.getWriter(DataWorksWorkflowSpec.class, context);
        JSONObject json = writer.write(constructedSpec, context);
        json.put(SpecForEachParser.ARRAY, writeByWriter(specObj.getArray()));
        return json;
    }
}
