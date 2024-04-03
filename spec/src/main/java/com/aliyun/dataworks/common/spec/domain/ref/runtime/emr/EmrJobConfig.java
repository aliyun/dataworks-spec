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

package com.aliyun.dataworks.common.spec.domain.ref.runtime.emr;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.interfaces.LabelEnum;
import com.aliyun.dataworks.common.spec.utils.SpecDevUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.ListUtils;

/**
 * EMR Job Config
 *
 * @author 聿剑
 * @date 2023/12/8
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class EmrJobConfig extends SpecRefEntity {
    /**
     * <a href="https://help.aliyun.com/zh/dataworks/user-guide/create-an-emr-spark-node?spm=a2c4g.11186623.0.0.5b997a6c6eiCoN#4098470371j5v">Advanced
     * setting: USE_GATEWAY</a>
     * use gateway or header, e.g. Local|Yarn
     */
    private EmrJobSubmitMode submitMode;
    /**
     * submit user, e.g. root
     */
    private String submitter;
    /**
     * <a href="https://help.aliyun.com/zh/dataworks/user-guide/create-an-emr-spark-node?spm=a2c4g.11186623.0.0.5b997a6c6eiCoN#4098470371j5v">Advanced
     * setting: FLOW_SKIP_SQL_ANALYZE</a>
     * FLOW_SKIP_SQL_ANALYZE, e.g. Batch|Single
     */
    private EmrJobExecuteMode executeMode;
    /**
     * e.g. 1, 2, 3
     */
    private Integer priority;
    /**
     * e.g. default
     */
    private String queue;
    /**
     * e.g. 1
     */
    private Integer cores;
    /**
     * Unit: MB, e.g. 1024
     */
    private Integer memory;
    /**
     * Enable session
     * e.g. true
     */
    private Boolean sessionEnabled;

    /**
     * Enable JDBC SQL
     * e.g. true
     */
    private Boolean enableJdbcSql;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        ListUtils.emptyIfNull(SpecDevUtil.getPropertyFields(this)).forEach(field -> {
            field.setAccessible(true);
            try {
                Object val = field.get(this);
                if (val instanceof LabelEnum) {
                    val = ((LabelEnum)val).getLabel();
                }
                Optional.ofNullable(val).ifPresent(v -> map.put(field.getName(), v));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return map;
    }
}