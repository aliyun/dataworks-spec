/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.migrationx.common.utils;

import org.dozer.DozerBeanMapper;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-06
 */
public class BeanUtils {

    private static final DozerBeanMapper DOZER_BEAN_MAPPER = new DozerBeanMapper();

    public static <T> T deepCopy(Object obj, Class<T> clazz) {
        if (obj == null || clazz == null) {
            return null;
        }
        if (!clazz.isInstance(obj)) {
            throw new IllegalArgumentException("obj is not instance of clazz");
        }
        return DOZER_BEAN_MAPPER.map(obj, clazz);
    }

    private BeanUtils() {

    }
}