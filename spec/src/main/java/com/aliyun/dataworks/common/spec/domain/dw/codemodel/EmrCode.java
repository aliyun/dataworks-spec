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

package com.aliyun.dataworks.common.spec.domain.dw.codemodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.utils.JSONUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.ListUtils;

/**
 * Code mode for EMR series
 *
 * @author sam.liux
 * @date 2020/04/03
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ToString
public class EmrCode extends AbstractBaseCode {
    public static final String ENVS_KEY_FLOW_SKIP_SQL_ANALYZE = "FLOW_SKIP_SQL_ANALYZE";

    private String name;
    private EmrJobType type;
    private EmrLauncher launcher = new EmrLauncher();
    private EmrProperty properties = new EmrProperty();
    private String description;

    @Override
    public EmrCode parse(String code) {
        EmrCode m = JSONUtils.parseObject(code, EmrCode.class);
        if (m == null) {
            return new EmrCode();
        }
        Optional.ofNullable(m).ifPresent(mm -> {
            this.setName(mm.getName());
            this.setType(mm.getType());
            this.setLauncher(mm.getLauncher());
            this.setProperties(mm.getProperties());
            this.setDescription(mm.getDescription());
        });
        this.setType(Optional.ofNullable(getType()).orElse(getEmrJobType(programType)));
        return this;
    }

    public static EmrJobType getEmrJobType(String defaultNodeType) {
        CodeProgramType codeProgramType = CodeProgramType.getNodeTypeByName(defaultNodeType);
        if (defaultNodeType == null) {
            return null;
        }

        switch (codeProgramType) {
            case HIVE:
            case EMR_HIVE_CLI:
                return EmrJobType.HIVE;
            case EMR_HIVE:
                return EmrJobType.HIVE_SQL;
            case EMR_SPARK_SQL:
                return EmrJobType.SPARK_SQL;
            case EMR_SPARK_SHELL:
                return EmrJobType.SPARK_SHELL;
            case EMR_SPARK_STREAMING:
                return EmrJobType.SPARK_STREAMING;
            case EMR_SPARK:
                return EmrJobType.SPARK;
            case EMR_IMPALA:
                return EmrJobType.IMPALA_SQL;
            case EMR_PRESTO:
                return EmrJobType.PRESTO_SQL;
            case EMR_TRINO:
                return EmrJobType.TRINO_SQL;
            case EMR_MR:
                return EmrJobType.MR;
            case EMR_SCOOP:
                return EmrJobType.SQOOP;
            case EMR_SHELL:
                return EmrJobType.SHELL;
            case EMR_KYUUBI:
                return EmrJobType.KYUUBI;
        }
        return null;
    }

    @Override
    public List<String> getProgramTypes() {
        return Arrays.stream(CodeProgramType.values())
                .map(Enum::name)
                .filter(named -> CodeProgramType.matchEngine(named, CalcEngineType.EMR)).collect(Collectors.toList());
    }

    @Override
    public void setSourceCode(String sourceCode) {
        if (properties == null) {
            properties = new EmrProperty();
        }

        if (properties.getArguments() == null) {
            properties.setArguments(new ArrayList<>());
        }

        Optional.ofNullable(sourceCode).ifPresent(code -> {
            properties.getArguments().clear();
            properties.getArguments().add(0, code);
        });
    }

    @Override
    public String getSourceCode() {
        return Optional.ofNullable(properties)
                .map(EmrProperty::getArguments)
                .orElse(ListUtils.emptyIfNull(null))
                .stream()
                .findFirst().orElse(null);
    }
}
