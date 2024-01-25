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

package com.aliyun.migrationx.common.exception;

/**
 * @author <a href="https://work.alibaba-inc.com/nwpipe/u/64152">聿剑</a>
 * @date 2022/12/20
 */
public enum ErrorCode {
    UNKNOWN_ENUM_TYPE("UnknownEnumType", "unknown enumeration type: {0}, value: {1}"),
    PARAMETER_NOT_SET("ParameterNotSet", "parameter not set: {0}"),
    PARAMETER_NOT_INVALID("ParameterInvalid", "parameter invalid: {0}"),
    PROJECT_NOT_FOUND("ProjectNotFound", "project not found: {0}"),
    PROCEDURE_CANCELED("ProcedureCanceled", "process procedure canceled"),
    NO_PERMISSION("NoPermission", "no permission: {0}"),
    NO_PERMISSION_PROJECT_ADMIN("NoPermissionProjectAdmin", "user {0} has no admin permission of project: {1}"),
    NO_PERMISSION_PROJECT_MEMBER("NoPermissionProjectMember", "user {0} is not member of project: {1}"),
    TRANSLATE_NODE_ERROR("TranslateNodeError", "translate node error: {0}"),
    PACKAGE_ANALYZE_FAILED("PackageAnalyzeError", "analyze package error: {0}"),
    PACKAGE_CONVERT_FAILED("PackageConvertError", "convert package error: {0}"),
    PACKAGE_NOT_LOADED("PackageNotLoaded", "package file not loaded"),
    UNSUPPORTED_PACKAGE("UnsupportedPackage", "unsupported package: {0}"),
    UNKNOWN_COMMAND_APP("UnknownCommandApp", "unknown command app: {0}"),
    FILE_NOT_FOUND("FileNotFound", "file not found: {0}"),
    PARSE_CONFIG_FILE_FAILED("ParseConfigError", "parse config error: {0}"),
    CONFIG_ITEM_INVALID("InvalidConfigItem", "invalid config item: {0}, reason: {1}");

    final String code;

    final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getMessage(Object ... info) {
         return BizException.of(this).with(info).getMessage();
    }
}
