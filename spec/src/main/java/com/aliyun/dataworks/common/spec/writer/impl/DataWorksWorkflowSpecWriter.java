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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.annotation.SpecWriter;
import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.ref.SpecArtifact;
import com.aliyun.dataworks.common.spec.utils.MapKeyMatchUtils;
import com.aliyun.dataworks.common.spec.writer.SpecWriterContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

/**
 * DataWorksWorkflowSpec writer
 *
 * @author 聿剑
 * @date 2023/8/27
 */
@SpecWriter
public class DataWorksWorkflowSpecWriter extends DefaultJsonObjectWriter<DataWorksWorkflowSpec> {
    public DataWorksWorkflowSpecWriter(SpecWriterContext context) {
        super(context);
    }

    @Override
    public JSONObject write(DataWorksWorkflowSpec specObj, SpecWriterContext context) {
        JSONObject jsonObject = writeJsonObject(specObj, true);
        if (CollectionUtils.isNotEmpty(specObj.getNodes())) {
            jsonObject.put("nodes", writeByWriter(specObj.getNodes()));
        }

        if (CollectionUtils.isNotEmpty(specObj.getFlow())) {
            jsonObject.put("flow", writeFlow(specObj));
        }

        if (CollectionUtils.isNotEmpty(specObj.getFiles())) {
            jsonObject.put("files", writeByWriter(specObj.getFiles()));
        }

        if (CollectionUtils.isNotEmpty(specObj.getFileResources())) {
            jsonObject.put("fileResources", writeByWriter(specObj.getFileResources()));
        }

        if (CollectionUtils.isNotEmpty(specObj.getFunctions())) {
            jsonObject.put("functions", writeByWriter(specObj.getFunctions()));
        }

        if (CollectionUtils.isNotEmpty(specObj.getArtifacts())) {
            jsonObject.put("artifacts", writeArtifacts(specObj.getArtifacts()));
        }

        if (CollectionUtils.isNotEmpty(specObj.getDatasources())) {
            jsonObject.put("datasources", writeByWriter(specObj.getDatasources()));
        }

        if (CollectionUtils.isNotEmpty(specObj.getDqcRules())) {
            jsonObject.put("dqcRules", writeByWriter(specObj.getDqcRules()));
        }

        if (CollectionUtils.isNotEmpty(specObj.getVariables())) {
            jsonObject.put("variables", writerListByWriter(new ArrayList<>(specObj.getVariables())));
        }
        return jsonObject;
    }

    private JSONObject writeArtifacts(List<SpecArtifact> artifacts) {
        JSONObject json = new JSONObject();
        ListUtils.emptyIfNull(artifacts).stream().collect(Collectors.groupingBy(SpecArtifact::getArtifactType))
            .forEach((group, list) -> json.put(MapKeyMatchUtils.pluralizeWithS(group.getLabel().toLowerCase()), writeByWriter(list)));
        return json;
    }

    private JSONArray writeFlow(DataWorksWorkflowSpec specObj) {
        if (CollectionUtils.isEmpty(specObj.getFlow())) {
            return new JSONArray();
        }

        JSONArray arr = new JSONArray();
        ListUtils.emptyIfNull(specObj.getFlow()).stream()
            .map(this::writeByWriter)
            .forEach(arr::add);
        return arr;
    }
}