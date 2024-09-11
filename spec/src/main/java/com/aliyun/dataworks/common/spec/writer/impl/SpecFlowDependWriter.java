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

import java.util.List;

import com.aliyun.dataworks.common.spec.annotation.SpecWriter;
import com.aliyun.dataworks.common.spec.domain.noref.SpecDepend;
import com.aliyun.dataworks.common.spec.domain.noref.SpecFlowDepend;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

/**
 * @author 聿剑
 * @date 2023/8/27
 */
@Slf4j
@SpecWriter
public class SpecFlowDependWriter extends DefaultJsonObjectWriter<SpecFlowDepend> {
    public SpecFlowDependWriter(SpecWriterContext context) {
        super(context);
    }

    @Override
    public JSONObject write(SpecFlowDepend specObj, SpecWriterContext context) {
        if (specObj.getNodeId() == null || specObj.getNodeId().getId() == null) {
            log.error("invalid spec flow depend: {}", JSON.toJSONString(specObj));
            throw new RuntimeException("invalid spec flow dependent");
        }

        JSONObject json = writeJsonObject(specObj, true);
        json.put("nodeId", specObj.getNodeId().getId());
        json.put("depends", writeDepends(specObj.getDepends()));
        return json;
    }

    private JSONArray writeDepends(List<SpecDepend> depends) {
        if (CollectionUtils.isEmpty(depends)) {
            return null;
        }

        JSONArray jsonArray = new JSONArray();
        ListUtils.emptyIfNull(depends).stream()
                .map(this::writeByWriter)
                .forEach(jsonArray::add);
        return jsonArray;
    }
}
