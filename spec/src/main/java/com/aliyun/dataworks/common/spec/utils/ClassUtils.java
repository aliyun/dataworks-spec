package com.aliyun.dataworks.common.spec.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author 聿剑
 * @date 2023/11/21
 */
@Slf4j
public class ClassUtils {
    /**
     * 获取类的继承层级
     *
     * @return 层级
     */
    public static int getClassHierarchyLevel(Object object) {
        if (object == null) {
            return 0;
        }

        Class<?> clz = object.getClass();
        int count = 0;
        do {
            log.info("clz: {}", clz.getName());
            if (clz.getSuperclass() != null && clz.getSuperclass() != Object.class) {
                clz = clz.getSuperclass();
                count++;
            } else {
                break;
            }
        } while (true);
        return count;
    }
}
