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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.task.parameters.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.enums.ResourceType;

public class ResourceParametersHelper {

    private Map<ResourceType, Map<Integer, AbstractResourceParameters>> resourceMap = new HashMap<>();

    public void put(ResourceType resourceType, Integer id) {
        put(resourceType, id, null);
    }

    public void put(ResourceType resourceType, Integer id, AbstractResourceParameters parameters) {
        Map<Integer, AbstractResourceParameters> resourceParametersMap = resourceMap.get(resourceType);
        if (Objects.isNull(resourceParametersMap)) {
            resourceParametersMap = new HashMap<>();
            resourceMap.put(resourceType, resourceParametersMap);
        }
        resourceParametersMap.put(id, parameters);
    }

    public void setResourceMap(Map<ResourceType, Map<Integer, AbstractResourceParameters>> resourceMap) {
        this.resourceMap = resourceMap;
    }

    public Map<ResourceType, Map<Integer, AbstractResourceParameters>> getResourceMap() {
        return resourceMap;
    }

    public Map<Integer, AbstractResourceParameters> getResourceMap(ResourceType resourceType) {
        return this.getResourceMap().get(resourceType);
    }

    public AbstractResourceParameters getResourceParameters(ResourceType resourceType, Integer code) {
        return this.getResourceMap(resourceType).get(code);
    }
}
