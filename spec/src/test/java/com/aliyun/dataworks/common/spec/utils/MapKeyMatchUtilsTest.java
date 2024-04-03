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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/3/3
 */
public class MapKeyMatchUtilsTest {
    @Test
    public void testSetIgnoreCaseSingleAndPluralFormValue() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");

        MapKeyMatchUtils.setIgnoreCaseSingleAndPluralFormValue(map, "value2", "key1s");
        Assert.assertEquals("value2", map.get("key1"));

        MapKeyMatchUtils.setIgnoreCaseSingleAndPluralFormValue(map, "value3", "KEY1S");
        Assert.assertEquals("value3", map.get("key1"));

        Assert.assertEquals("value3", MapKeyMatchUtils.getIgnoreCaseSingleAndPluralForm(map, "key1s"));
        Assert.assertEquals("value3", MapKeyMatchUtils.getIgnoreCaseSingleAndPluralForm(map, "KEY1S"));

        Assert.assertTrue(MapKeyMatchUtils.containsIgnoreCase(map, "keY1"));

        MapKeyMatchUtils.removeIgnoreCaseSingleAndPluralForm(map, "keY1");

        System.out.println(map);
        Assert.assertNull(MapKeyMatchUtils.getIgnoreCaseSingleAndPluralForm(map, "key1"));
    }

    @Test
    public void testGetValue() {
        Map<String, String> map = new HashMap<>();
        map.put("nodeOutputs", "output1");
        Assert.assertEquals("output1", MapKeyMatchUtils.getValue(map, StringUtils::equalsIgnoreCase, "nodeoutputs"));
    }
}
