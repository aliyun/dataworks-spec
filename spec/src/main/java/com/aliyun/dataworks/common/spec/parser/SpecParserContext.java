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

package com.aliyun.dataworks.common.spec.parser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONObject;

import com.aliyun.dataworks.common.spec.domain.SpecContext;
import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import static com.aliyun.dataworks.common.spec.utils.ParserUtil.jsonToMap;

/**
 * @author yiwei.qyw
 * @date 2023/7/6
 */
public class SpecParserContext extends SpecContext {

    /**
     * all SpecEntity map
     */
    private final HashMap<String, SpecRefEntity> entityMap = new HashMap<>();

    /**
     * all ref map
     * -- GETTER --
     * Get all ref entity list
     */
    @Getter
    private final List<SpecEntityContext> refEntityList = new ArrayList<>();

    /**
     * parse JsonObject
     * -- GETTER --
     * Get all context map
     */
    @Getter
    private Map<String, Object> contextMap;
    @Getter
    @Setter
    private Boolean ignoreMissingFields;

    /**
     * Convert json to ctxMap
     *
     * @param spec spec
     */
    public void specInit(String spec) {
        try {
            contextMap = jsonToMap(JSONObject.parseObject(spec));
        } catch (Exception e) {
            throw new SpecException(e, SpecErrorCode.PARSE_ERROR,
                "Spec JSON parse failed.Please check the JSON format.");
        }

    }

    /**
     * Get all SpecEntity map
     *
     * @return entityMap
     */

    public Map<String, SpecRefEntity> getEntityMap() {
        return this.entityMap;
    }

    /**
     * Save reference info
     */
    @Data
    public static class SpecEntityContext {
        private static final String KEY_SEPARATOR = "#";
        /**
         * Saves who owns the referenced object
         */
        private Object ownerObject;

        /**
         * entity name
         */
        @Getter
        private String entityName;
        /**
         * Get target object in entityMap by this key
         */
        @Getter
        private Object entityValue;

        /**
         * Save ownerObject's set method. If ownerObject is list type ,no need to set
         */
        @Getter
        private Field entityField;

        public String getEntityKey() {
            return entityName + KEY_SEPARATOR + entityValue;
        }
    }

}