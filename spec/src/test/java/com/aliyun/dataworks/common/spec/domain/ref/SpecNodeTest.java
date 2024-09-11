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

package com.aliyun.dataworks.common.spec.domain.ref;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author 聿剑
 * @date 2024/7/29
 */
public class SpecNodeTest {
    @Test
    public void testEquals() {
        SpecNode n1 = new SpecNode();
        n1.setId("n1");
        n1.setName("n1");
        SpecDatasource ds1 = new SpecDatasource();
        ds1.setName("ds1");
        ds1.setType("odps");
        n1.setDatasource(ds1);

        SpecNode n2 = new SpecNode();
        n2.setId("n1");
        n2.setName("n1");
        SpecDatasource ds2 = new SpecDatasource();
        ds2.setName("ds2");
        ds2.setType("odps");
        n2.setDatasource(ds2);

        Assert.assertNotEquals(n1, n2);

        ds2.setName("ds1");
        Assert.assertEquals(ds1, ds2);
        Assert.assertEquals(n1, n2);

        n2.setName("n2");
        Assert.assertNotEquals(n1, n2);
    }

    @Test
    public void testEqualsOutputInput() {
        SpecNode n1 = new SpecNode();
        n1.setId("n1");
        n1.setName("n1");
        SpecDatasource ds1 = new SpecDatasource();
        ds1.setName("ds1");
        ds1.setType("odps");
        n1.setDatasource(ds1);

        SpecNode n2 = new SpecNode();
        n2.setId("n1");
        n2.setName("n1");
        SpecDatasource ds2 = new SpecDatasource();
        ds2.setName("ds2");
        ds2.setType("odps");
        n2.setDatasource(ds2);

        Assert.assertNotEquals(n1, n2);

        ds2.setName("ds1");
        Assert.assertEquals(ds1, ds2);
        Assert.assertEquals(n1, n2);

        SpecNodeOutput in1 = new SpecNodeOutput();
        in1.setData("output1");
        in1.setRefTableName("refTable1");
        n1.setInputs(Collections.singletonList(in1));

        SpecNodeOutput in2 = new SpecNodeOutput();
        in2.setData("output1");
        in2.setRefTableName("refTable1");
        n2.setInputs(Collections.singletonList(in2));
        Assert.assertEquals(n1, n2);
    }
}
