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

package com.aliyun.dataworks.common.spec.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * @author yiwei.qyw
 * @date 2023/7/4
 */
public class ParserUtil {
    public static Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> map = new HashMap<>(json.size());
        for (String key : json.keySet()) {
            Object value = json.get(key);
            if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject)value);
            } else if (value instanceof JSONArray) {
                value = jsonArrayToList((JSONArray)value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> jsonArrayToList(JSONArray array) {
        List<Object> list = new ArrayList<>();
        for (Object value : array) {
            if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject)value);
            } else if (value instanceof JSONArray) {
                value = jsonArrayToList((JSONArray)value);
            }
            list.add(value);
        }
        return list;
    }
}