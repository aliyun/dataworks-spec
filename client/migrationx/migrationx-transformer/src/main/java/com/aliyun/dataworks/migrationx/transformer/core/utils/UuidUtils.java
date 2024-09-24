/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.core.utils;

import java.util.UUID;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-23
 */
public class UuidUtils {

    public static String genUuidWithoutHorizontalLine() {
        // 采用UUID低64位作为生成逻辑，冲突概率增加一倍
        long least = UUID.randomUUID().getLeastSignificantBits();
        // 最小值
        if (least == Long.MIN_VALUE) {
            least = Long.MAX_VALUE;
        } else {
            least = Math.abs(least);
        }
        return String.valueOf(least);
    }
}