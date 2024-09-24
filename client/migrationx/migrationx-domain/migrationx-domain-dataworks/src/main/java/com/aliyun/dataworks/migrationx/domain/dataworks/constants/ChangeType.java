package com.aliyun.dataworks.migrationx.domain.dataworks.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 文件的变更类型
 *
 * @author 戒迷
 * @date 2024/04/17
 */
@RequiredArgsConstructor
@Getter
public enum ChangeType {

    /**
     * NEW
     */
    NEW(0),
    /**
     * UPDATE
     */
    UPDATE(1),
    /**
     * DELETE
     */
    DELETE(2),
    ;

    private final Integer code;

    /**
     * 通过code来获取枚举对象
     *
     * @param code
     * @return
     */
    public static ChangeType getByCode(Integer code) {
        for (ChangeType type : ChangeType.values()) {
            if (code.equals(type.getCode())) {
                return type;
            }
        }
        return null;
    }
}
