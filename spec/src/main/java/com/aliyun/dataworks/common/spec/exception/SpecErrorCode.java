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

package com.aliyun.dataworks.common.spec.exception;

/**
 * @author yiwei.qyw
 * @date 2023/7/18
 */
public enum SpecErrorCode implements SpecIErrorCode {

    /**
     * Parser not found
     */
    PARSER_NOT_FOUND("0x5083000000000000"),

    /**
     * Get id error
     */
    GET_ID_ERROR("0x5083000000000001"),

    /**
     * Field set value fail
     */
    FIELD_SET_FAIL("0x5083000000000002"),

    /**
     * field need to parse not found
     */
    FIELD_NOT_FOUND("0x5083000000000003"),

    /**
     * enum not exist
     */
    ENUM_NOT_EXIST("0x5083000000000004"),

    /**
     * Specification parse error
     */
    PARSE_ERROR("0x5083000000000005"),

    /**
     * Parser load error
     */
    PARSER_LOAD_ERROR("0x5083000000000006"),

    /**
     * Duplicate id
     */
    DUPLICATE_ID("0x5083000000000007"),

    /**
     * RefId parser error
     */
    REFID_PARSER_ERROR("0x5083000000000008"),

    /**
     * Target entity not found
     */
    TARGET_ENTITY_NOT_FOUND("0x5083000000000009"),
    /**
     * validate error
     */
    VALIDATE_ERROR("0x5083000000000010");

    private String code;

    private String msg;

    SpecErrorCode(String code) {
        this.code = code;
        this.msg = code;
    }

    SpecErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getModule() {
        return "Default";
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}