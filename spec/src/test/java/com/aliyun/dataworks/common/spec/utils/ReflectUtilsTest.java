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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import com.aliyun.dataworks.common.spec.writer.Writer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.reflections.Reflections;

/**
 * @author 聿剑
 * @date 2024/5/22
 */
@Slf4j
@Ignore
public class ReflectUtilsTest {
    @Test
    public void testScanPackageInDependencies() {
        Set<Class<?>> set = new HashSet<>();
        ReflectUtils.scanPackage(Reflections.class.getPackage().getName(), clz -> true, set);
        System.out.println(set);
        Assert.assertNotNull(set);
        Assert.assertFalse(set.isEmpty());
    }

    @Test
    public void testScanPackageInProject() {
        Set<Class<?>> set = new HashSet<>();
        ReflectUtils.scanPackage(Writer.class.getPackage().getName(), clz -> true, set);
        System.out.println(set);
        Assert.assertNotNull(set);
        Assert.assertFalse(set.isEmpty());
    }

    @Test
    public void testScanParallel() {
        IntStream.range(0, 50).boxed().parallel().forEach(i -> {
            Set<Class<?>> set = new HashSet<>();
            ReflectUtils.scanPackage(Reflections.class.getPackage().getName(), clz -> true, set);
            log.info("set: {}", set.size());
        });
    }
}
