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

package com.aliyun.dataworks.migrationx.transformer.core.utils;

import java.util.Arrays;
import java.util.Map;

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrAllocationSpec;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrJobType;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrLauncher;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.EmrProperty;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Node;
import com.aliyun.dataworks.migrationx.domain.dataworks.utils.NodeUtils;
import com.aliyun.migrationx.common.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sam.liux
 * @date 2020/12/31
 */
public class EmrCodeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmrCodeUtils.class);

    public static EmrCode asEmrCode(Node node) {
        try {
            if (NodeUtils.isEmrNode(node.getType())) {
                return GsonUtils.fromJsonString(node.getCode(), EmrCode.class);
            }
        } catch (Exception e) {
            LOGGER.error("convert to EmrCode failed code: {}, exception: ", node.getCode(), e);
        }
        return null;
    }

    public static String toEmrCode(Node node) {
        CodeProgramType nodeType = CodeProgramType.valueOf(node.getType());
        EmrCode emrCode = new EmrCode();
        emrCode.setName(node.getName());
        emrCode.setDescription("DataWorks Migration");
        EmrLauncher launcher = new EmrLauncher();
        launcher.setAllocationSpec(getDefaultAllocationSpec());
        emrCode.setLauncher(launcher);
        EmrProperty properties = new EmrProperty();
        properties.setArguments(Arrays.asList(node.getCode()));
        properties.setTags(Arrays.asList(node.getName()));
        emrCode.setProperties(properties);
        switch (nodeType) {
            case EMR_HIVE:
                emrCode.setType(EmrJobType.HIVE_SQL);
                return GsonUtils.toJsonString(emrCode);
            case EMR_SHELL:
                emrCode.setType(EmrJobType.SHELL);
                return GsonUtils.toJsonString(emrCode);
            case EMR_IMPALA:
                emrCode.setType(EmrJobType.IMPALA_SQL);
                return GsonUtils.toJsonString(emrCode);
            case EMR_MR:
                emrCode.setType(EmrJobType.MR);
                return GsonUtils.toJsonString(emrCode);
            case EMR_PRESTO:
                emrCode.setType(EmrJobType.PRESTO_SQL);
                return GsonUtils.toJsonString(emrCode);
            case EMR_SPARK:
                emrCode.setType(EmrJobType.SPARK);
                return GsonUtils.toJsonString(emrCode);
            case EMR_SPARK_SHELL:
                emrCode.setType(EmrJobType.SPARK_SHELL);
                return GsonUtils.toJsonString(emrCode);
            case EMR_SPARK_SQL:
                emrCode.setType(EmrJobType.SPARK_SQL);
                return GsonUtils.toJsonString(emrCode);
            default:
                return node.getCode();
        }
    }

    private static Map<String, Object> getDefaultAllocationSpec() {
        EmrAllocationSpec allocationSpec = new EmrAllocationSpec();
        allocationSpec.setMemory(String.valueOf(2048));
        allocationSpec.setPriority(String.valueOf(1));
        allocationSpec.setVcores(String.valueOf(1));
        allocationSpec.setQueue("default");
        return allocationSpec.toMap();
    }

    public static String toString(EmrCode emrCode) {
        return GsonUtils.toJsonString(emrCode);
    }
}
