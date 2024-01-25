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

package com.aliyun.dataworks.migrationx.transformer.core.sqoop;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SqoopJsonUtil {

    public static final Logger LOG = LoggerFactory.getLogger(SqoopJsonUtil.class.getName());

    private SqoopJsonUtil() {
    }

    public static String getJsonStringforMap(Map<String, String> map) {
        JSONObject pathPartMap = new JSONObject(map);
        return pathPartMap.toString();
    }

    public static Map<String, String> getMapforJsonString(String mapJsonStr) {
        if ("".equals(mapJsonStr) || null == mapJsonStr) {
            throw new IllegalArgumentException("Passed Null for map " + mapJsonStr);
        }

        LOG.debug("Passed mapJsonStr ::" + mapJsonStr + " to parse");
        Map<String, String> partPathMap = new HashMap<String, String>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            partPathMap = mapper.readValue(mapJsonStr,
                new TypeReference<HashMap<String, String>>() {
                });
            return partPathMap;
        } catch (JsonParseException e) {
            LOG.error("JsonParseException:: Illegal json to parse into map :"
                + mapJsonStr + e.getMessage());
            throw new IllegalArgumentException("Illegal json to parse into map :"
                + mapJsonStr + e.getMessage(), e);
        } catch (JsonMappingException e) {
            LOG.error("JsonMappingException:: Illegal json to parse into map :"
                + mapJsonStr + e.getMessage());
            throw new IllegalArgumentException("Illegal json to parse into map :"
                + mapJsonStr + e.getMessage(), e);
        } catch (IOException e) {
            LOG.error("IOException while parsing json into map :" + mapJsonStr
                + e.getMessage());
            throw new IllegalArgumentException(
                "IOException while parsing json into map :" + mapJsonStr
                    + e.getMessage(), e);
        }
    }

    public static boolean isEmptyJSON(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr) || "{}".equals(jsonStr)) {
            return true;
        } else {
            return false;
        }
    }
}