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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 聿剑
 * @date 2022/10/18
 */
@ToString
@Accessors(chain = true)
@Data
@EqualsAndHashCode
public class PackageInfo {
    /**
     * x.y.z
     */
    private String version;

    public DolphinSchedulerVersion getDolphinSchedulerVersion() {
        if (StringUtils.isBlank(this.getVersion())) {
            throw new RuntimeException("pacakge info version is blank");
        }

        String[] versionNumbers = StringUtils.split(this.getVersion(), ".");
        if (versionNumbers == null || versionNumbers.length == 0
                || Arrays.stream(versionNumbers).anyMatch(num -> !StringUtils.isNumeric(num))) {
            throw new RuntimeException("package info version invalid: " + this.getVersion());
        }

        List<Integer> numbers = Arrays.stream(versionNumbers).map(Integer::valueOf).collect(Collectors.toList());
        if (numbers.get(0) < 2) {
            return DolphinSchedulerVersion.V1;
        }
        if (numbers.get(0) < 3) {
            return DolphinSchedulerVersion.V2;
        }
        return DolphinSchedulerVersion.V3;
    }
}
