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

package com.aliyun.dataworks.migrationx.domain.dataworks.datago;

/**
 * @author 聿剑
 * @date 2023/01/12
 */
public class DataGoConstants {
    public static final String PROPERTIES_CONVERTER_PERL2SHELL_ENABLE = "workflow.converter.perl2shell.enable";
    public static final String PROPERTIES_CONVERTER_DETECT2SHELL_ENABLE = "workflow.converter.detect2shell.enable";
    public static final String PROPERTIES_CONVERTER_DETECT_TASK_STOP_AT = "workflow.converter.detectTask.stopAt";
    public static final String PROPERTIES_CONVERTER_PERL2SHELL_PERL_BIN = "workflow.converter.perl2shell.perlBin";
    public static final String PROPERTIES_CONVERTER_PERL2SHELL_PERL_INCLUDE_PATHS
        = "workflow.converter.perl2shell.perlIncludePaths";

    public static final String NODE_IO_SUFFIX_LOWERCASE = "_0";
    public static final String NODE_IO_SUFFIX_UPPERCASE = "_1";
    public static final String NODE_IO_SUFFIX_MIXED = "_2";
}
