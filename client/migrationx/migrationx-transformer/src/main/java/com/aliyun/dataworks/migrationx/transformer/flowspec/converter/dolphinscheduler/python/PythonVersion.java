/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.python;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-24
 */
@Getter
@AllArgsConstructor
public enum PythonVersion {
    /**
     * python2
     */
    PYTHON2(CodeProgramType.PYODPS.getName()),
    /**
     * python3
     */
    PYTHON3(CodeProgramType.PYODPS3.getName());

    @JsonValue
    private final String command;
}
